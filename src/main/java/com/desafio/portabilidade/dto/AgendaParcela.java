package com.desafio.portabilidade.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AgendaParcela(
        String parcelaId,
        String convenioId,
        BigDecimal valor,
        LocalDate dataDesconto,
        String status
) {
}
