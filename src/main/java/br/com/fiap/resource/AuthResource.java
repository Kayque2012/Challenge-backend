package br.com.fiap.resource;

import br.com.fiap.bo.AuthBO;
import br.com.fiap.entities.Dentista;
import br.com.fiap.entities.Paciente;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Endpoints de autenticacao consumidos pelo front-end.
 * - POST /login  -> Login.tsx (email + senha)
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthBO authBO;

    /**
     * POST /login
     * Body: { "email": "...", "senha": "..." }
     * Sucesso (200): objeto Paciente ou Dentista com campos "nome", "tipo", "cidade".
     * Erro (401): { "erro": "Email ou senha incorretos" }
     */
    @POST
    @Path("/login")
    public Response login(Map<String, String> credenciais) {
        String email = credenciais.get("email");
        String senha = credenciais.get("senha");

        if (email == null || senha == null || email.isBlank() || senha.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("erro", "Email e senha sao obrigatorios"))
                    .build();
        }

        Object usuario = authBO.autenticar(email, senha);

        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("erro", "Email ou senha incorretos"))
                    .build();
        }

        return Response.ok(usuario).build();
    }
}
