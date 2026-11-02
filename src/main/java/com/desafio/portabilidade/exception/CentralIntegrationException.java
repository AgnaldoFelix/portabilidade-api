package com.desafio.portabilidade.exception;

public class CentralIntegrationException extends RuntimeException {

    public CentralIntegrationException(String message) {
        super(message);
    }

    public CentralIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
