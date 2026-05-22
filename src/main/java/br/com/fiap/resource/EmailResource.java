package br.com.fiap.resource;

import br.com.fiap.dto.EmailLembreteDTO;
import br.com.fiap.service.EmailService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/email")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Email", description = "Envio de lembretes e confirmações de consulta via Gmail")
public class EmailResource {

    @Inject
    EmailService emailService;

    /**
     * Envia um e-mail de confirmação ou lembrete de consulta.
     *
     * Body JSON:
     * {
     *   "tipo":         "confirmacao" | "lembrete",
     *   "para":         "paciente@email.com",
     *   "nome":         "Lucas Gomes",
     *   "procedimento": "Restauração (Cárie)",
     *   "data":         "20/06/2026",
     *   "hora":         "14:00",
     *   "dentista":     "Dr. Carlos Mendes",
     *   "diasAntes":    3   // só para tipo "lembrete"
     * }
     */
    @POST
    @Path("/enviar")
    @Operation(summary = "Envia e-mail de confirmação ou lembrete ao paciente via Gmail")
    public Response enviar(EmailLembreteDTO dto) {
        if (dto == null || dto.para == null || dto.para.isBlank()) {
            return Response.status(400).entity("{\"erro\":\"Campo 'para' é obrigatório\"}").build();
        }
        if (dto.tipo == null || dto.tipo.isBlank()) {
            return Response.status(400).entity("{\"erro\":\"Campo 'tipo' é obrigatório (confirmacao|lembrete)\"}").build();
        }

        try {
            if ("lembrete".equalsIgnoreCase(dto.tipo)) {
                emailService.enviarLembrete(dto);
            } else {
                emailService.enviarConfirmacao(dto);
            }
            return Response.ok("{\"status\":\"enviado\"}").build();
        } catch (Exception e) {
            // Retorna 200 mesmo em erro de envio para não quebrar o fluxo do paciente
            return Response.ok("{\"status\":\"erro\",\"detalhe\":\"" + e.getMessage() + "\"}").build();
        }
    }
}
