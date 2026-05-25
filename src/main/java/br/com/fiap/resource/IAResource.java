package br.com.fiap.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Path("/IA")
@ApplicationScoped
public class IAResource {

    private static final Logger LOG = Logger.getLogger(IAResource.class.getName());
    private static final int LIMITE_POR_IP = 10;
    private static final long TTL_MS = 60 * 60 * 1000L;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static class CachedResponse {
        String json;
        long expiraEm;
        CachedResponse(String json, long expiraEm) { this.json = json; this.expiraEm = expiraEm; }
    }

    private final ConcurrentHashMap<String, AtomicInteger> contadorPorIp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CachedResponse> cache = new ConcurrentHashMap<>();

    @ConfigProperty(name = "gemini.api.key", defaultValue = "")
    String apiKey;

    @PostConstruct
    void validarApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY nao configurada. Defina a variavel de ambiente antes de iniciar.");
        }
        LOG.info("GEMINI_API_KEY configurada");
    }

    @Scheduled(every = "1m")
    void resetarContadores() {
        contadorPorIp.clear();
    }

    @Scheduled(every = "10m")
    void limparExpirados() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(e -> e.getValue().expiraEm < now);
    }

    @POST
    @Path("/consultar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response consultarGemini(Map<String, String> requestBody,
                                    @Context HttpServerRequest httpRequest) {
        try {
            // Rate limiting por IP
            String ip = httpRequest.remoteAddress().host();
            if (contadorPorIp.computeIfAbsent(ip, k -> new AtomicInteger(0))
                    .incrementAndGet() > LIMITE_POR_IP) {
                return Response.status(429)
                        .entity(Map.of("erro", "Limite de requisicoes atingido. Tente novamente em 1 minuto."))
                        .build();
            }

            // Sanitizar input
            String pergunta = requestBody.get("texto");
            String dadosJson = requestBody.get("fila_json");

            if (pergunta == null || pergunta.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("erro", "Campo 'texto' e obrigatorio."))
                        .build();
            }
            if (pergunta.length() > 500)  pergunta  = pergunta.substring(0, 500);
            if (dadosJson != null && dadosJson.length() > 2000) dadosJson = dadosJson.substring(0, 2000);

            // Verificar cache
            String cacheKey = String.valueOf((pergunta + "|" + dadosJson).hashCode());
            CachedResponse cached = cache.get(cacheKey);
            if (cached != null && cached.expiraEm > System.currentTimeMillis()) {
                LOG.info("[IA] cache hit");
                return Response.ok(cached.json).build();
            }

            // Montar prompt
            String prompt = "Es a IA de triagem da Turma do Bem.\n" +
                    "DADOS DA FILA (JSON): " + dadosJson + "\n" +
                    "PERGUNTA: " + pergunta + "\n" +
                    "REGRAS: Responde de forma curta, direta e usa emojis para niveis de dor.";

            // Montar JSON body via ObjectMapper
            String jsonBody = MAPPER.writeValueAsString(Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt))))));

            // Disparar requisição com timeout de 30s por request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                if (cache.size() > 500) {
                    cache.entrySet().stream()
                            .sorted(java.util.Comparator.comparingLong(e -> e.getValue().expiraEm))
                            .limit(100)
                            .map(Map.Entry::getKey)
                            .forEach(cache::remove);
                }
                cache.put(cacheKey, new CachedResponse(response.body(), System.currentTimeMillis() + TTL_MS));
                LOG.info("[IA] cache miss - armazenado");
            }
            return Response.ok(response.body()).build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Response.status(504)
                    .entity(Map.of("erro", "Timeout ao contatar a IA. Tente novamente."))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity(Map.of("erro", "Falha ao contatar a IA"))
                    .build();
        }
    }
}
