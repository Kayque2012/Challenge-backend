package br.com.fiap.resource;

import br.com.fiap.bo.DentistaBO;
import br.com.fiap.entities.Dentista;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/dentistas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DentistaResource {

    @Inject
    DentistaBO bo;

    @GET
    public Response listarTodos() {
        List<Dentista> lista = bo.listar();
        return Response.ok(lista).build();
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
    public Response cadastrar(Dentista dentista) {
        return Response.status(Response.Status.CREATED).entity(bo.cadastrar(dentista)).build();
    }

    @RolesAllowed({"dentista", "admin"})
    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") int id, Dentista dentista) {
        return Response.ok(bo.atualizar(id, dentista)).build();
    }

    @RolesAllowed("dentista")
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
        return Response.ok(Map.of("mensagem", "Dentista reativado com sucesso.")).build();
    }

    @GET
    @Path("/{id}/vaga")
    public Response temVaga(@PathParam("id") int id) {
        return Response.ok(Map.of("id", id, "temVaga", bo.temVagaDisponivel(id))).build();
    }
}
