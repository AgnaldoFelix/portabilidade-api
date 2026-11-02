package com.desafio.portabilidade.repository;

import com.desafio.portabilidade.entity.Autorizacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AutorizacaoRepository extends JpaRepository<Autorizacao, UUID> {
    Optional<Autorizacao> findByBeneficiarioId(String beneficiarioId);

    boolean existsByBeneficiarioId(String beneficiarioId);
}