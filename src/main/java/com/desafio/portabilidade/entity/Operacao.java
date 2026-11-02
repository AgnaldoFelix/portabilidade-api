package com.desafio.portabilidade.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "operacoes",
        uniqueConstraints = @UniqueConstraint(columnNames = "chave_idempotencia"))
public class Operacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "chave_idempotencia", nullable = false, unique = true)
    private String chaveIdempotencia;

    @Column(nullable = false)
    private String beneficiarioId;

    @Column(nullable = false)
    private String contaDestino;

    @Column(nullable = false)
    private String parcelasSelecionadas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOperacao status;

    private String protocoloCentral;

    @Column(length = 500)
    private String motivoRejeicao;

    private Instant criadaEm;
    private Instant atualizadaEm;

    // getters e setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String c) { this.chaveIdempotencia = c; }
    public String getBeneficiarioId() { return beneficiarioId; }
    public void setBeneficiarioId(String b) { this.beneficiarioId = b; }
    public String getContaDestino() { return contaDestino; }
    public void setContaDestino(String c) { this.contaDestino = c; }
    public String getParcelasSelecionadas() { return parcelasSelecionadas; }
    public void setParcelasSelecionadas(String p) { this.parcelasSelecionadas = p; }
    public StatusOperacao getStatus() { return status; }
    public void setStatus(StatusOperacao s) { this.status = s; }
    public String getProtocoloCentral() { return protocoloCentral; }
    public void setProtocoloCentral(String p) { this.protocoloCentral = p; }
    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String m) { this.motivoRejeicao = m; }
    public Instant getCriadaEm() { return criadaEm; }
    public void setCriadaEm(Instant i) { this.criadaEm = i; }
    public Instant getAtualizadaEm() { return atualizadaEm; }
    public void setAtualizadaEm(Instant i) { this.atualizadaEm = i; }
}