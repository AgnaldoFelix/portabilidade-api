package com.desafio.portabilidade.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.portabilidade.entity.Operacao;
import com.desafio.portabilidade.dto.CriarOperacaoRequest;
import com.desafio.portabilidade.dto.OperacaoResponse;
import com.desafio.portabilidade.service.OperacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/operacoes")
public class OperacaoController {

    private final OperacaoService service;

    public OperacaoController(OperacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OperacaoResponse> criar(
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @RequestBody @Valid CriarOperacaoRequest req) {
        if (chave == null || chave.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe o cabeçalho Idempotency-Key."
            );
        }

        Operacao op = service.criar(chave, req);
        return ResponseEntity.ok(OperacaoResponse.from(op));
    }

    @GetMapping("/{id}")
    public OperacaoResponse consultar(@PathVariable UUID id) {
        return OperacaoResponse.from(service.consultarStatus(id));
    }
}