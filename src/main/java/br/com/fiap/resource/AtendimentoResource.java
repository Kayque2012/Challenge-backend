package br.com.fiap.resource;

import br.com.fiap.bo.AtendimentoBO;
import br.com.fiap.entities.Atendimento;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/atendimentos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AtendimentoResource {

    @Inject
    AtendimentoBO bo;

    @GET
    public Response listarTodos() {
        List<Atendimento> lista = bo.listar();
        return Response.ok(lista).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") int id) {
        return Response.ok(bo.buscarPorId(id)).build();
    }

    @POST
    public Response cadastrar(Atendimento atendimento) {
        return Response.status(Response.Status.CREATED).entity(bo.cadastrar(atendimento)).build();
    }

    @PUT
    @Path("/{id}")
    public Response atualizar(@PathParam("id") int id, Atendimento atendimento) {
        return Response.ok(bo.atualizar(id, atendimento)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") int id) {
        bo.excluir(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}/concluir")
    public Response concluir(@PathParam("id") int id) {
        return Response.ok(bo.concluir(id)).build();
    }
}
