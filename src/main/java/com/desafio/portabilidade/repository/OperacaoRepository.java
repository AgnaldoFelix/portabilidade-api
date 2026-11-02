package com.desafio.portabilidade.repository;

import com.desafio.portabilidade.entity.Operacao;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacaoRepository extends JpaRepository<Operacao, UUID> {
    Optional<Operacao> findByChaveIdempotencia(String chaveIdempotencia);
}