package br.com.fiap.resource;

import br.com.fiap.bo.OfertaBO;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

/**
 * Slot-offer scheduling persisted in Oracle.
 *
 * POST   /ofertas                          – dentist creates an offer with proposed slots
 * GET    /ofertas/paciente/{idPaciente}    – patient fetches their pending offer
 * GET    /ofertas/dentista/{idDentista}    – dentist fetches all their offers
 * GET    /ofertas/dentista/{id}/agenda     – dentist fetches confirmed appointments only
 * PUT    /ofertas/{id}/confirmar           – patient confirms a chosen slot
 * DELETE /ofertas/{id}                     – dentist cancels / removes an offer
 */
@Path("/ofertas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OfertaResource {

    @Inject
    OfertaBO bo;

    /**
     * POST /ofertas
     * Body: { "idDentista": 55, "idPaciente": 36, "procedimento": "...",
     *         "slots": [{"data":"2026-05-20","hora":"10:00"}, ...] }
     */
    @POST
    public Response criar(Map<String, Object> body) {
        int idDentista = toInt(body.get("idDentista"));
        int idPaciente = toInt(body.get("idPaciente"));
        String procedimento = (String) body.get("procedimento");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> slots = (List<Map<String, String>>) body.get("slots");

        if (idDentista == 0 || idPaciente == 0 || slots == null || slots.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("erro", "idDentista, idPaciente e slots são obrigatórios"))
                    .build();
        }

        Map<String, Object> criada = bo.criar(idDentista, idPaciente, procedimento, slots);
        return Response.status(Response.Status.CREATED).entity(criada).build();
    }

    /** GET /ofertas/paciente/{idPaciente} — latest pending offer for the patient */
    @GET
    @Path("/paciente/{idPaciente}")
    public Response buscarPorPaciente(@PathParam("idPaciente") int idPaciente) {
        Map<String, Object> oferta = bo.buscarPendentePorPaciente(idPaciente);
        if (oferta == null) {
            return Response.ok(Map.of()).build(); // empty object = no offer
        }
        return Response.ok(oferta).build();
    }

    /** GET /ofertas/dentista/{idDentista} — all offers (any status) for the dentist */
    @GET
    @Path("/dentista/{idDentista}")
    public Response buscarPorDentista(@PathParam("idDentista") int idDentista) {
        return Response.ok(bo.buscarTodosPorDentista(idDentista)).build();
    }

    /** GET /ofertas/dentista/{idDentista}/agenda — confirmed appointments only */
    @GET
    @Path("/dentista/{idDentista}/agenda")
    public Response agenda(@PathParam("idDentista") int idDentista) {
        return Response.ok(bo.buscarConfirmadosPorDentista(idDentista)).build();
    }

    /**
     * PUT /ofertas/{id}/confirmar
     * Body: { "data": "2026-05-20", "hora": "10:00" }
     */
    @PUT
    @Path("/{id}/confirmar")
    public Response confirmar(@PathParam("id") int id, Map<String, String> body) {
        String data = body.get("data");
        String hora = body.get("hora");

        if (data == null || hora == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("erro", "data e hora são obrigatórios"))
                    .build();
        }

        bo.confirmar(id, data, hora);
        return Response.ok(Map.of("status", "confirmado", "data", data, "hora", hora)).build();
    }

    /** PATCH /ofertas/{id}/concluir — dentist marks appointment as concluded */
    @PATCH
    @Path("/{id}/concluir")
    public Response concluir(@PathParam("id") int id) {
        bo.concluir(id);
        return Response.ok(Map.of("status", "concluido")).build();
    }

    /** DELETE /ofertas/{id} — cancel / remove an offer */
    @DELETE
    @Path("/{id}")
    public Response cancelar(@PathParam("id") int id) {
        bo.cancelar(id);
        return Response.noContent().build();
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
