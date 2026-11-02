package com.desafio.portabilidade.exception;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import com.desafio.portabilidade.exception.CentralIntegrationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> notFound(NoSuchElementException exception) {
        return ResponseEntity.status(404).body(Map.of("erro", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Dados inválidos"));
    }

    @ExceptionHandler({RestClientException.class, CentralIntegrationException.class})
    public ResponseEntity<Map<String, String>> falhaCentral(RuntimeException e) {
        return ResponseEntity.status(502)
                .body(Map.of("erro", "Falha na comunicação com a Central: " + e.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> erroRequisicao(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(Map.of("erro", e.getReason() == null ? "Solicitação inválida" : e.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> erroGenerico(Exception e) {
        return ResponseEntity.status(500)
                .body(Map.of("erro", "Erro interno: " + e.getMessage()));
    }
}