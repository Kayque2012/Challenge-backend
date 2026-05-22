package br.com.fiap.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
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
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ConcurrentHashMap<String, AtomicInteger> contadorPorIp = new ConcurrentHashMap<>();

    private String apiKey;

    @PostConstruct
    void validarApiKey() {
        apiKey = System.getenv("GEMINI_API_KEY");
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
