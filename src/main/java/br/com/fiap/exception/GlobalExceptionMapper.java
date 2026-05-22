package br.com.fiap.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException ex) {
        if (ex instanceof ResourceNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("erro", ex.getMessage()))
                    .build();
        }
        if (ex instanceof ValidationException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("erro", ex.getMessage()))
                    .build();
        }
        if (ex instanceof DatabaseException) {
            // Loga a causa real no servidor sem expô-la ao cliente
            String causa = ex.getCause() != null ? ex.getCause().getMessage() : "sem causa";
            System.err.println("[DatabaseException] " + ex.getMessage() + " | Causa: " + causa);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("erro", ex.getMessage()))
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("erro", "Erro interno: " + ex.getMessage()))
                .build();
    }
}
