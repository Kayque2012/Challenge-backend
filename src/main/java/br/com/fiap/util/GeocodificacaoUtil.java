package br.com.fiap.util;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class GeocodificacaoUtil {

    private static final ConcurrentHashMap<String, double[]> CACHE = new ConcurrentHashMap<>();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private GeocodificacaoUtil() {}

    /**
     * Retorna [lat, lng] para a cidade+estado+país informados.
     * Usa cache em memória — o Nominatim só é chamado uma vez por cidade por sessão do servidor.
     * Retorna null se a cidade não for encontrada ou ocorrer erro.
     */
    public static double[] buscar(String cidade, String estado, String pais) {
        if (cidade == null || cidade.isBlank()) return null;
        String paisEfetivo = (pais != null && !pais.isBlank()) ? pais : "Brasil";
        String estadoEfetivo = (estado != null && !estado.isBlank()) ? estado : "";
        String chave = cidade + "|" + estadoEfetivo + "|" + paisEfetivo;

        if (CACHE.containsKey(chave)) return CACHE.get(chave);

        try {
            String localQuery = estadoEfetivo.isBlank()
                    ? cidade + ", " + paisEfetivo
                    : cidade + ", " + estadoEfetivo + ", " + paisEfetivo;
            String query = URLEncoder.encode(localQuery, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&limit=1&featuretype=city"))
                    .header("Accept-Language", "pt-BR")
                    .header("User-Agent", "TurmaDoBem-FIAP/1.0")
                    .build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            String body = resp.body();
            if (body.contains("\"lat\"")) {
                double lat = Double.parseDouble(extrairCampo(body, "lat"));
                double lon = Double.parseDouble(extrairCampo(body, "lon"));
                double[] coords = {lat, lon};
                CACHE.put(chave, coords);
                Thread.sleep(1100); // respeita rate limit do Nominatim (1 req/s)
                return coords;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String extrairCampo(String json, String campo) {
        String chave = "\"" + campo + "\":\"";
        int inicio = json.indexOf(chave) + chave.length();
        int fim = json.indexOf("\"", inicio);
        return json.substring(inicio, fim);
    }
}
