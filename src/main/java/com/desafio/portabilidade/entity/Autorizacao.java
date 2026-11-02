package com.desafio.portabilidade.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "autorizacoes")
public class Autorizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String beneficiarioId;

    private String autorizacaoCentralId;
    private Instant criadaEm;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getBeneficiarioId() { return beneficiarioId; }
    public void setBeneficiarioId(String b) { this.beneficiarioId = b; }
    public String getAutorizacaoCentralId() { return autorizacaoCentralId; }
    public void setAutorizacaoCentralId(String a) { this.autorizacaoCentralId = a; }
    public Instant getCriadaEm() { return criadaEm; }
    public void setCriadaEm(Instant i) { this.criadaEm = i; }
}