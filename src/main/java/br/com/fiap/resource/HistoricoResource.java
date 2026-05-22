package br.com.fiap.resource;

import br.com.fiap.bo.HistoricoBO;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

/**
 * Endpoint do historico de atendimentos do paciente.
 *
 * Consumido pelo PacienteDashboard.tsx, que faz:
 *   GET /paciente/historico/{nomeUsuario}
 *
 * Retorna um array de consultas formatadas no padrao HistoricoConsulta do front.
 */
@Path("/paciente/historico")
@Produces(MediaType.APPLICATION_JSON)
public class HistoricoResource {

    @Inject
    HistoricoBO historicoBO;

    /**
     * GET /paciente/historico/{identificador}
     *
     * Aceita tanto um numero (ID do paciente) quanto uma string (nome).
     * Esse padrao permite o front continuar passando o nome
     * (como o codigo atual faz) ou migrar pro id no futuro sem quebrar.
     */
    @GET
    @Path("/{identificador}")
    public Response buscar(@PathParam("identificador") String identificador) {
        List<Map<String, Object>> historico;

        // Tenta interpretar como ID numerico primeiro
        try {
            int id = Integer.parseInt(identificador);
            historico = historicoBO.buscarHistoricoPorId(id);
        } catch (NumberFormatException e) {
            // Se nao for numero, busca por nome
            historico = historicoBO.buscarHistoricoPorNome(identificador);
        }

        return Response.ok(historico).build();
    }
}
