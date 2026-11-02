package com.desafio.portabilidade.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import com.desafio.portabilidade.dto.AgendaParcela;
import com.desafio.portabilidade.dto.CriarOperacaoRequest;
import com.desafio.portabilidade.dto.StatusCentralOperacao;
import com.desafio.portabilidade.entity.Operacao;
import com.desafio.portabilidade.entity.StatusOperacao;
import com.desafio.portabilidade.integration.CentralClient;
import com.desafio.portabilidade.repository.AutorizacaoRepository;
import com.desafio.portabilidade.repository.OperacaoRepository;

@ExtendWith(MockitoExtension.class)
class OperacaoServiceTest {

    @Mock
    private OperacaoRepository operacaoRepository;

    @Mock
    private AutorizacaoRepository autorizacaoRepository;

    @Mock
    private CentralClient central;

    private OperacaoService service;

    @BeforeEach
    void setUp() {
        service = new OperacaoService(operacaoRepository, central, autorizacaoRepository);
    }

    @Test
    void criaOperacaoPendenteEEnviaParaCentral() {
        CriarOperacaoRequest request = request();
        when(operacaoRepository.findByChaveIdempotencia("chave-1")).thenReturn(Optional.empty());
        when(autorizacaoRepository.existsByBeneficiarioId("BEN001")).thenReturn(true);
        when(central.buscarAgenda("BEN001")).thenReturn(agendaAberta());
        when(central.criarOperacao("BEN001", "conta-1", List.of("P001")))
                .thenReturn("protocolo-1");
        when(operacaoRepository.saveAndFlush(any(Operacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(operacaoRepository.save(any(Operacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Operacao resultado = service.criar("chave-1", request);

        assertEquals(StatusOperacao.PENDENTE, resultado.getStatus());
        assertEquals("protocolo-1", resultado.getProtocoloCentral());
        assertEquals("P001", resultado.getParcelasSelecionadas());
        verify(central).criarOperacao("BEN001", "conta-1", List.of("P001"));
    }

    @Test
    void mesmaChaveEPedidoRetornaOperacaoExistenteSemChamarCentral() {
        Operacao existente = operacaoExistente();
        when(operacaoRepository.findByChaveIdempotencia("chave-1"))
                .thenReturn(Optional.of(existente));

        Operacao resultado = service.criar("chave-1", request());

        assertSame(existente, resultado);
        verify(central, never()).buscarAgenda("BEN001");
        verify(central, never()).criarOperacao("BEN001", "conta-1", List.of("P001"));
    }

    @Test
    void falhaDaCentralFicaRegistradaComoErro() {
        when(operacaoRepository.findByChaveIdempotencia("chave-1")).thenReturn(Optional.empty());
        when(autorizacaoRepository.existsByBeneficiarioId("BEN001")).thenReturn(true);
        when(central.buscarAgenda("BEN001")).thenReturn(agendaAberta());
        when(operacaoRepository.saveAndFlush(any(Operacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(central.criarOperacao("BEN001", "conta-1", List.of("P001")))
                .thenThrow(new RestClientException("Central indisponível"));
        when(operacaoRepository.save(any(Operacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Operacao resultado = service.criar("chave-1", request());

        assertEquals(StatusOperacao.ERRO, resultado.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(
                resultado.getMotivoRejeicao().contains("Central indisponível")
        );
    }

    @Test
    void atualizaOperacaoQuandoCentralConfirma() {
        Operacao operacao = operacaoExistente();
        when(operacaoRepository.findById(operacao.getId())).thenReturn(Optional.of(operacao));
        when(central.consultarOperacao("protocolo-1"))
                .thenReturn(new StatusCentralOperacao("CONFIRMADA", null));
        when(operacaoRepository.save(operacao)).thenReturn(operacao);

        Operacao resultado = service.consultarStatus(operacao.getId());

        assertEquals(StatusOperacao.CONFIRMADA, resultado.getStatus());
        verify(operacaoRepository).save(operacao);
    }

    private CriarOperacaoRequest request() {
        return new CriarOperacaoRequest("BEN001", "conta-1", List.of("P001"));
    }

    private List<AgendaParcela> agendaAberta() {
        return List.of(new AgendaParcela(
                "P001", "CONV01", new BigDecimal("150.00"),
                LocalDate.parse("2026-01-10"), "ABERTA"
        ));
    }

    private Operacao operacaoExistente() {
        Operacao operacao = new Operacao();
        operacao.setId(UUID.randomUUID());
        operacao.setChaveIdempotencia("chave-1");
        operacao.setBeneficiarioId("BEN001");
        operacao.setContaDestino("conta-1");
        operacao.setParcelasSelecionadas("P001");
        operacao.setStatus(StatusOperacao.PENDENTE);
        operacao.setProtocoloCentral("protocolo-1");
        return operacao;
    }
}
