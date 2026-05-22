package br.com.fiap.resource; // Ajuste para o seu pacote

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Path("/IA")
public class IAResource {

    @POST
    @Path("/consultar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response consultarGemini(Map<String, String> requestBody) {
        try {
            // 1. Recebe a pergunta e o JSON da fila enviados pelo React
            String pergunta = requestBody.get("texto");
            String dadosJson = requestBody.get("fila_json");
            String apiKey = System.getenv("GEMINI_API_KEY");

            // 2. Monta o contexto EXATAMENTE como no seu Python
            String prompt = "És a IA de triagem da Turma do Bem.\n" +
                    "DADOS DA FILA (JSON): " + dadosJson + "\n" +
                    "PERGUNTA: " + pergunta + "\n" +
                    "REGRAS: Responde de forma curta, direta e usa emojis para níveis de dor.";

            // 3. Limpeza de segurança para o JSON não quebrar na hora de ir pro Google
            String promptEscapado = prompt.replace("\"", "\\\"").replace("\n", "\\n");
            String jsonBody = "{\"contents\": [{\"parts\": [{\"text\": \"" + promptEscapado + "\"}]}]}";

            // 4. Dispara nativamente usando HttpClient (Eficiência Máxima)
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return Response.ok(response.body()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("{\"error\": \"Falha ao contatar a IA\"}").build();
        }
    }
}