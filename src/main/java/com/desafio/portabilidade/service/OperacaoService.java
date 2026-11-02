package com.desafio.portabilidade.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.desafio.portabilidade.dto.AgendaParcela;
import com.desafio.portabilidade.dto.CriarOperacaoRequest;
import com.desafio.portabilidade.dto.StatusCentralOperacao;
import com.desafio.portabilidade.entity.Operacao;
import com.desafio.portabilidade.entity.StatusOperacao;
import com.desafio.portabilidade.integration.CentralClient;
import com.desafio.portabilidade.repository.AutorizacaoRepository;
import com.desafio.portabilidade.repository.OperacaoRepository;
import com.desafio.portabilidade.exception.CentralIntegrationException;

@Service
public class OperacaoService {

    private final OperacaoRepository repo;
    private final CentralClient central;
    private final AutorizacaoRepository autorizacaoRepo;

    public OperacaoService(
            OperacaoRepository repo,
            CentralClient central,
            AutorizacaoRepository autorizacaoRepo
    ) {
        this.repo = repo;
        this.central = central;
        this.autorizacaoRepo = autorizacaoRepo;
    }

    public Operacao criar(String chaveIdempotencia, CriarOperacaoRequest req) {
        Operacao existente = repo.findByChaveIdempotencia(chaveIdempotencia).orElse(null);
        if (existente != null) {
            return retornarSeMesmoPedido(existente, req);
        }

        validarAutorizacao(req.beneficiarioId());
        validarParcelasElegiveis(req);

        Operacao operacao;
        try {
            operacao = repo.saveAndFlush(criarOperacaoPendente(chaveIdempotencia, req));
        } catch (DataIntegrityViolationException exception) {
            Operacao criadaEmParalelo = repo.findByChaveIdempotencia(chaveIdempotencia)
                    .orElseThrow(() -> exception);
            return retornarSeMesmoPedido(criadaEmParalelo, req);
        }
        return enviarParaCentral(operacao, req);
    }

    private void validarAutorizacao(String beneficiarioId) {
        if (!autorizacaoRepo.existsByBeneficiarioId(beneficiarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "O beneficiário precisa autorizar o acesso antes de iniciar uma operação."
            );
        }
    }

    private void validarParcelasElegiveis(CriarOperacaoRequest req) {
        Set<String> selecionadas = new HashSet<>(req.parcelas());
        if (selecionadas.size() != req.parcelas().size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A lista contém parcelas duplicadas."
            );
        }

        Set<String> elegiveis = central.buscarAgenda(req.beneficiarioId()).stream()
                .filter(parcela -> "ABERTA".equalsIgnoreCase(parcela.status()))
                .map(AgendaParcela::parcelaId)
                .collect(java.util.stream.Collectors.toSet());

        if (!elegiveis.containsAll(selecionadas)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecione apenas parcelas abertas da agenda do beneficiário."
            );
        }
    }

    private Operacao retornarSeMesmoPedido(Operacao existente, CriarOperacaoRequest req) {
        boolean mesmoPedido = existente.getBeneficiarioId().equals(req.beneficiarioId())
                && existente.getContaDestino().equals(req.contaDestino())
                && existente.getParcelasSelecionadas().equals(String.join(",", req.parcelas()));
        if (!mesmoPedido) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A chave Idempotency-Key já foi usada para outra solicitação."
            );
        }
        return existente;
    }

    private Operacao criarOperacaoPendente(String chaveIdempotencia, CriarOperacaoRequest req) {
        Instant agora = Instant.now();
        Operacao operacao = new Operacao();
        operacao.setChaveIdempotencia(chaveIdempotencia);
        operacao.setBeneficiarioId(req.beneficiarioId());
        operacao.setContaDestino(req.contaDestino());
        operacao.setParcelasSelecionadas(String.join(",", req.parcelas()));
        operacao.setStatus(StatusOperacao.PENDENTE);
        operacao.setCriadaEm(agora);
        operacao.setAtualizadaEm(agora);
        return operacao;
    }

    private Operacao enviarParaCentral(Operacao operacao, CriarOperacaoRequest req) {
        try {
            String protocolo = central.criarOperacao(
                    req.beneficiarioId(), req.contaDestino(), req.parcelas());
            operacao.setProtocoloCentral(protocolo);
        } catch (RestClientException | CentralIntegrationException e) {
            operacao.setStatus(StatusOperacao.ERRO);
            operacao.setMotivoRejeicao("Falha na comunicação com a Central: " + e.getMessage());
        }
        operacao.setAtualizadaEm(Instant.now());
        return repo.save(operacao);
    }

    public Operacao consultarStatus(UUID id) {
        Operacao operacao = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Operação não encontrada"));

        if (operacaoFinalizada(operacao) || operacao.getProtocoloCentral() == null) {
            return operacao;
        }

        StatusCentralOperacao resposta = central.consultarOperacao(operacao.getProtocoloCentral());
        if (resposta != null) {
            try {
                operacao.setStatus(StatusOperacao.valueOf(resposta.status()));
            } catch (IllegalArgumentException exception) {
                throw new CentralIntegrationException(
                        "A Central retornou um status de operação desconhecido.", exception
                );
            }
            operacao.setMotivoRejeicao(resposta.motivoRejeicao());
            operacao.setAtualizadaEm(Instant.now());
            repo.save(operacao);
        }
        return operacao;
    }

    private boolean operacaoFinalizada(Operacao operacao) {
        return operacao.getStatus() == StatusOperacao.CONFIRMADA
                || operacao.getStatus() == StatusOperacao.REJEITADA
                || operacao.getStatus() == StatusOperacao.ERRO;
    }
}