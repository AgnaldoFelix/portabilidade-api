package com.desafio.portabilidade.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CriarOperacaoRequest(
        @NotBlank String beneficiarioId,
        @NotBlank String contaDestino,
        @NotEmpty List<@NotBlank String> parcelas
) {}