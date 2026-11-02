# API de Portabilidade de Convênios

Backend Java para registrar a autorização do beneficiário, consultar a agenda da Central e acompanhar operações de portabilidade. O frontend React está em outro projeto.

## Executar localmente

Requisitos: Java 17 ou superior e Node.js.

Abra dois terminais na raiz deste projeto:

**Terminal 1 — simulador da Central**

```powershell
node mock-central.js
```

O simulador ficará disponível em `http://localhost:4000`.

**Terminal 2 — API**

```powershell
.\mvnw.cmd spring-boot:run
```

A API ficará disponível em `http://localhost:8082`. Se a porta estiver ocupada, escolha outra:

```powershell
$env:PORT=8083
.\mvnw.cmd spring-boot:run
```

Configure o frontend para enviar as chamadas à API, por exemplo, `http://localhost:8082/api`. O simulador precisa estar ativo para autorizações, consultas de agenda e operações.

Para executar os testes automatizados:

```powershell
.\mvnw.cmd clean test
```

## Fluxo e endpoints

1. O beneficiário autoriza o acesso: `POST /api/autorizacoes`.
2. O backend verifica a autorização e consulta as parcelas: `GET /api/beneficiarios/{beneficiarioId}/agenda`.
3. O beneficiário seleciona parcelas abertas e informa a conta de destino: `POST /api/operacoes`, enviando o cabeçalho `Idempotency-Key`.
4. O backend salva a operação como `PENDENTE`, envia à Central e consulta o resultado por `GET /api/operacoes/{id}`. O resultado pode ser `CONFIRMADA`, `REJEITADA` ou `ERRO`.

O backend valida a autorização e a elegibilidade das parcelas. Repetir uma chave de idempotência com os mesmos dados retorna a operação existente; reutilizá-la com dados diferentes resulta em HTTP 409.

Use estes IDs para experimentar os cenários do simulador:

- `BEN001`: duas parcelas; operação confirmada após alguns segundos.
- `BEN002`: uma parcela; operação rejeitada por limite de margem.
- `BEN_ERRO`: Central retorna HTTP 503; operação fica registrada como `ERRO`.

O simulador incluído neste projeto adiciona uma parcela de demonstração a `BEN_ERRO` para permitir testar a falha pelo fluxo completo.

## Organização

- `controller`: recebe requisições HTTP.
- `service`: aplica as regras e coordena as etapas do fluxo.
- `entity` e `repository`: representam e persistem os dados locais.
- `dto`: define os dados de entrada e saída da API e da Central.
- `integration`: contém o `CentralClient`, responsável pelas chamadas HTTP ao sistema externo.
- `config` e `exception`: configuração do Spring e tratamento de erros.

O `CentralClient` existe para isolar a comunicação de rede com a Central. Ele não decide as regras de portabilidade; essas ficam nos services.

## Decisões técnicas

- **Spring Boot e Java 17:** fornecem a API REST, validação e integração com o banco.
- **H2 em memória:** mantém a execução local simples, sem instalar um banco. Os dados são perdidos ao reiniciar a API.
- **Chave de idempotência obrigatória e única no banco:** evita criar operações duplicadas quando a mesma solicitação é repetida.
- **Integração isolada em `CentralClient`:** separa chamadas HTTP externas das regras de negócio.
- **Falhas explícitas:** erros ao enviar à Central são registrados como `ERRO`; falhas ao consultar são retornadas sem inventar um resultado.

## Bibliotecas externas

- Spring Boot Web: endpoints REST e cliente HTTP.
- Spring Data JPA: acesso ao banco de dados.
- Spring Boot Validation: validação das requisições.
- H2: banco em memória para desenvolvimento e demonstração.
- Spring Boot Test, JUnit e Mockito: testes do contexto e dos fluxos do service.

As dependências são declaradas em `pom.xml`; suas versões são gerenciadas pelo Spring Boot.

## Uso de IA

Usei um assistente de IA para apoiar a análise dos requisitos, implementação e revisão do backend, além da criação de testes. Conferi as alterações com testes automatizados e chamadas locais ao simulador.

## Fora do escopo e próximos passos

Este repositório contém apenas o backend e o simulador; a interface React será desenvolvida no outro projeto. Também ficaram fora autenticação, banco persistente, histórico de eventos, HMAC e implantação.

Como próximos passos, eu adicionaria autenticação, persistência durável, trilha de eventos por operação e testes de integração HTTP automatizados contra o simulador.
