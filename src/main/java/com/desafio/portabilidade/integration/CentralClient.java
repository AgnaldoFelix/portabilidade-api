package com.desafio.portabilidade.integration;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import com.desafio.portabilidade.dto.AgendaParcela;
import com.desafio.portabilidade.dto.StatusCentralOperacao;
import com.desafio.portabilidade.exception.CentralIntegrationException;

@Component
public class CentralClient {

    private static final ParameterizedTypeReference<List<AgendaParcela>> AGENDA_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public CentralClient(
            RestClient.Builder builder,
            @Value("${central.base-url}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public String criarAutorizacao(String beneficiarioId) {
        Map<?, ?> resposta = restClient.post()
                .uri("/autorizacoes")
                .body(Map.of("beneficiarioId", beneficiarioId))
                .retrieve()
                .body(Map.class);
        Object autorizacaoId = resposta == null ? null : resposta.get("autorizacaoId");
        if (!(autorizacaoId instanceof String id) || id.isBlank()) {
            throw new CentralIntegrationException("A Central não retornou o ID da autorização.");
        }
        return id;
    }

    public List<AgendaParcela> buscarAgenda(String beneficiarioId) {
        List<AgendaParcela> agenda = restClient.get()
                .uri("/beneficiarios/{id}/agenda", beneficiarioId)
                .retrieve()
                .body(AGENDA_TYPE);
        if (agenda == null) {
            throw new CentralIntegrationException("A Central retornou uma agenda vazia.");
        }
        return agenda;
    }

    public String criarOperacao(String beneficiarioId, String contaDestino, List<String> parcelas) {
        Map<?, ?> resposta = restClient.post()
                .uri("/operacoes-portabilidade")
                .body(Map.of(
                        "beneficiarioId", beneficiarioId,
                        "contaDestino", contaDestino,
                        "parcelas", parcelas))
                .retrieve()
                .body(Map.class);
        Object protocolo = resposta == null ? null : resposta.get("protocolo");
        if (!(protocolo instanceof String id) || id.isBlank()) {
            throw new CentralIntegrationException("A Central não retornou o protocolo da operação.");
        }
        return id;
    }

    public StatusCentralOperacao consultarOperacao(String protocolo) {
        return restClient.get()
                .uri("/operacoes-portabilidade/{p}", protocolo)
                .exchange((req, res) -> {
                    if (res.getStatusCode().value() == 204) {
                        return null;
                    }
                    if (!res.getStatusCode().is2xxSuccessful()) {
                        throw new CentralIntegrationException(
                                "A Central respondeu com HTTP " + res.getStatusCode().value() + "."
                        );
                    }
                    StatusCentralOperacao status = res.bodyTo(StatusCentralOperacao.class);
                    if (status == null || status.status() == null) {
                        throw new CentralIntegrationException("A Central retornou um status inválido.");
                    }
                    return status;
                });
    }
}