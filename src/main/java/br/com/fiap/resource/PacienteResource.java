package br.com.fiap.resource;

import br.com.fiap.bo.HistoricoBO;
import br.com.fiap.bo.PacienteBO;
import br.com.fiap.entities.Paciente;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/pacientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PacienteResource {

    @Inject
    PacienteBO bo;

    @Inject
    HistoricoBO historicoBO;

    @GET
    public Response listarPacientes(@QueryParam("cidade") String cidade) {
        List<Paciente> fila = bo.listar();

        if (cidade != null && !cidade.trim().isEmpty()) {
            fila = fila.stream()
                    .filter(p -> cidade.equalsIgnoreCase(p.getCidade()))
                    .filter(p -> p.getTipoDor() != null && !p.getTipoDor().isEmpty())
                    .filter(p -> !"adotado".equals(p.getStatus()))
                    .collect(Collectors.toList());
        }
        return Response.ok(fila).build();
    }

    @GET
    @Path("/adotados")
    public Response listarAdotados(@QueryParam("idDentista") int idDentista) {
        if (idDentista <= 0) return Response.status(Response.Status.BAD_REQUEST)
                .entity(java.util.Map.of("erro", "idDentista obrigatorio")).build();
        return Response.ok(bo.listarAdotadosPorDentista(idDentista)).build();
    }

    @RolesAllowed("dentista")
    @GET
    @Path("/inativos")
    public Response listarInativos() {
        return Response.ok(bo.listarInativos()).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") int id) {
        return Response.ok(bo.buscarPorId(id)).build();
    }

    @POST
    public Response cadastrar(Paciente paciente) {
        Paciente criado = bo.cadastrar(paciente);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @RolesAllowed({"dentista", "paciente", "admin"})
    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") int id, Paciente paciente) {
        return Response.ok(bo.atualizar(id, paciente)).build();
    }

    @RolesAllowed({"dentista", "paciente"})
    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") int id) {
        bo.excluir(id);
        return Response.noContent().build();
    }

    @RolesAllowed("dentista")
    @PATCH
    @Path("/{id}/reativar")
    public Response reativar(@PathParam("id") int id) {
        bo.reativar(id);
        return Response.ok(Map.of("mensagem", "Paciente reativado com sucesso.")).build();
    }

    @GET
    @Path("/{id}/idade")
    public Response calcularIdade(@PathParam("id") int id) {
        return Response.ok(Map.of("id", id, "idade", bo.calcularIdade(id))).build();
    }

    @GET
    @Path("/{id}/elegibilidade")
    public Response verificarElegibilidade(@PathParam("id") int id) {
        return Response.ok(Map.of("id", id, "elegivel", bo.verificarElegibilidade(id))).build();
    }

    @RolesAllowed({"dentista", "paciente", "admin"})
    @PUT
    @Path("/redefinir-senha")
    public Response redefinirSenha(Map<String, String> body) {
        bo.redefinirSenha(
                body != null ? body.get("email") : null,
                body != null ? body.get("novaSenha") : null);
        return Response.ok(Map.of("mensagem", "Senha redefinida com sucesso.")).build();
    }

    @GET
    @Path("/{id}/historico")
    public Response listarHistoricoPaciente(@PathParam("id") int id) {
        return Response.ok(historicoBO.buscarHistoricoPorId(id)).build();
    }
}
