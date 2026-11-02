package com.desafio.portabilidade.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desafio.portabilidade.dto.AgendaParcela;
import com.desafio.portabilidade.integration.CentralClient;
import com.desafio.portabilidade.service.AutorizacaoService;

@RestController
@RequestMapping("/api")
public class AgendaController {

    private final CentralClient central;
    private final AutorizacaoService autorizacaoService;

    public AgendaController(CentralClient central, AutorizacaoService autorizacaoService) {
        this.central = central;
        this.autorizacaoService = autorizacaoService;
    }

    @GetMapping("/beneficiarios/{id}/agenda")
    public List<AgendaParcela> agenda(@PathVariable String id) {
        autorizacaoService.validarAutorizacao(id);
        return central.buscarAgenda(id);
    }
}