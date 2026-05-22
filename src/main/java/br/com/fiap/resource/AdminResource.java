package br.com.fiap.resource;

import br.com.fiap.bo.RelatorioBO;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Endpoints administrativos consumidos pelo AdminDashboard.tsx do front.
 *
 * Acesso restrito por convencao do front (via sessionStorage userRole),
 * sem validacao real no backend (autenticacao por JWT ficaria pra proxima sprint).
 */
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject
    RelatorioBO relatorioBO;

    /**
     * GET /admin/estatisticas
     *
     * Retorna o objeto consumido pelo AdminDashboard com:
     * - total de pacientes (beneficiarios)
     * - total de dentistas
     * - distribuicao de dentistas por cidade
     * - lista dos ultimos atendimentos
     */
    @GET
    @Path("/estatisticas")
    public Response estatisticas() {
        Map<String, Object> stats = relatorioBO.gerarEstatisticasAdmin();
        return Response.ok(stats).build();
    }

    /** GET /admin/elegiveis - Total de pacientes elegiveis (renda <= 3 SM). */
    @GET
    @Path("/elegiveis")
    public Response totalElegiveis() {
        return Response.ok(Map.of("total", relatorioBO.contarPacientesElegiveis())).build();
    }

    /** GET /admin/dentistas-com-vaga - Total de dentistas com vagas disponiveis. */
    @GET
    @Path("/dentistas-com-vaga")
    public Response totalDentistasComVaga() {
        return Response.ok(Map.of("total", relatorioBO.contarDentistasComVaga())).build();
    }
}
