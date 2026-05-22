package br.com.fiap.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, int id) {
        super(recurso + " com id " + id + " não encontrado.");
    }
}
