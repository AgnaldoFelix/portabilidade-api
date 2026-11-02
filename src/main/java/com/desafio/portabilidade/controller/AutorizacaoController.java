package com.desafio.portabilidade.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.portabilidade.entity.Autorizacao;
import com.desafio.portabilidade.dto.CriarAutorizacaoRequest;
import com.desafio.portabilidade.service.AutorizacaoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/autorizacoes")
public class AutorizacaoController {

    private final AutorizacaoService service;

    public AutorizacaoController(AutorizacaoService service) {
        this.service = service;
    }

    @PostMapping
    public Map<String, Object> criar(@RequestBody @Valid CriarAutorizacaoRequest req) {
        Autorizacao a = service.autorizar(req.beneficiarioId());
        return Map.of(
                "autorizacaoId", a.getId().toString(),
                "beneficiarioId", a.getBeneficiarioId()
        );
    }
}