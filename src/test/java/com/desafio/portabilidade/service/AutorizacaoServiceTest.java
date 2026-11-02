package com.desafio.portabilidade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.desafio.portabilidade.entity.Autorizacao;
import com.desafio.portabilidade.integration.CentralClient;
import com.desafio.portabilidade.repository.AutorizacaoRepository;

@ExtendWith(MockitoExtension.class)
class AutorizacaoServiceTest {

    @Mock
    private AutorizacaoRepository repository;

    @Mock
    private CentralClient central;

    private AutorizacaoService service;

    @BeforeEach
    void setUp() {
        service = new AutorizacaoService(repository, central);
    }

    @Test
    void registraAutorizacaoNaCentralESalvaNoBanco() {
        when(repository.findByBeneficiarioId("BEN001")).thenReturn(Optional.empty());
        when(central.criarAutorizacao("BEN001")).thenReturn("autorizacao-1");
        when(repository.save(any(Autorizacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Autorizacao autorizacao = service.autorizar("BEN001");

        assertEquals("BEN001", autorizacao.getBeneficiarioId());
        assertEquals("autorizacao-1", autorizacao.getAutorizacaoCentralId());
        verify(central).criarAutorizacao("BEN001");
    }
}
