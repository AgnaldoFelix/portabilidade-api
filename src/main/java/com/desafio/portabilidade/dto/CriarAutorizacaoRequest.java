package com.desafio.portabilidade.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarAutorizacaoRequest(@NotBlank String beneficiarioId) {}