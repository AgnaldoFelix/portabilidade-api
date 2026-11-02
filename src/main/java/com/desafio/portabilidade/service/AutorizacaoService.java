package com.desafio.portabilidade.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.desafio.portabilidade.entity.Autorizacao;
import com.desafio.portabilidade.integration.CentralClient;
import com.desafio.portabilidade.repository.AutorizacaoRepository;

@Service
public class AutorizacaoService {

    private final AutorizacaoRepository repo;
    private final CentralClient central;

    public AutorizacaoService(AutorizacaoRepository repo, CentralClient central) {
        this.repo = repo;
        this.central = central;
    }

    public Autorizacao autorizar(String beneficiarioId) {
        return repo.findByBeneficiarioId(beneficiarioId)
                .orElseGet(() -> criarAutorizacao(beneficiarioId));
    }

    private Autorizacao criarAutorizacao(String beneficiarioId) {
        Autorizacao autorizacao = new Autorizacao();
        autorizacao.setBeneficiarioId(beneficiarioId);
        autorizacao.setAutorizacaoCentralId(central.criarAutorizacao(beneficiarioId));
        autorizacao.setCriadaEm(Instant.now());
        return repo.save(autorizacao);
    }

    public boolean estaAutorizado(String beneficiarioId) {
        return repo.existsByBeneficiarioId(beneficiarioId);
    }

    public void validarAutorizacao(String beneficiarioId) {
        if (!estaAutorizado(beneficiarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "O beneficiário precisa autorizar o acesso antes de consultar a agenda."
            );
        }
    }
}