package com.desafio.portabilidade.dto;

import java.time.Instant;
import java.util.UUID;

import com.desafio.portabilidade.entity.Operacao;

public record OperacaoResponse(
        UUID id,
        String beneficiarioId,
        String contaDestino,
        String parcelasSelecionadas,
        String status,
        String protocoloCentral,
        String motivoRejeicao,
        Instant criadaEm,
        Instant atualizadaEm
) {
    public static OperacaoResponse from(Operacao op) {
        return new OperacaoResponse(
                op.getId(),
                op.getBeneficiarioId(),
                op.getContaDestino(),
                op.getParcelasSelecionadas(),
                op.getStatus().name(),
                op.getProtocoloCentral(),
                op.getMotivoRejeicao(),
                op.getCriadaEm(),
                op.getAtualizadaEm()
        );
    }
}