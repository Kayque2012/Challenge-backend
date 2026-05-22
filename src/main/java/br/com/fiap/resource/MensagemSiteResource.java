package br.com.fiap.resource;

import br.com.fiap.bo.MensagemSiteBO;
import br.com.fiap.entities.MensagemSite;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

/**
 * Endpoints do formulario "Fale Conosco" do front (FormularioContato.tsx).
 */
@Path("/mensagens")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MensagemSiteResource {

    @Inject
    MensagemSiteBO bo;

    @GET
    public Response listarTodas() {
        List<MensagemSite> lista = bo.listar();
        return Response.ok(lista).build();
    }

    @POST
    public Response enviar(MensagemSite mensagem) {
        MensagemSite registrada = bo.registrar(mensagem);
        return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                        "id", registrada.getId(),
                        "mensagem", "Mensagem enviada com sucesso!"))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") int id) {
        bo.excluir(id);
        return Response.noContent().build();
    }
}
