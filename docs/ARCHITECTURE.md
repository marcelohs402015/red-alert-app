# Arquitetura do Red Alert Backend

## Visão Geral – Arquitetura em Camadas

Projeto com propósito único: **ler emails e colocar no calendário**. A arquitetura em camadas é mais simples de explicar e mantém SOLID, Clean Code e padrões GoF (Strategy, Adapter).

```
┌─────────────────────────────────────────────────────────────────┐
│                     CAMADA DE APRESENTAÇÃO                       │
│  Controllers (REST) · DTOs (request/response) · GlobalException  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CAMADA DE SERVIÇO                           │
│  AlertHistoryService · CategoryService · EmailPollingService     │
│  ProcessedEmailService · Interfaces: AiAnalyzer, GmailClient,   │
│  NotificationSender · Exceções de negócio                        │
└─────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
┌──────────────────────────────┐   ┌─────────────────────────────────┐
│        CAMADA DE DOMÍNIO     │   │    CAMADA DE INFRAESTRUTURA     │
│  Entidades: Alert, Category, │   │  Persistência (JPA) · Gmail     │
│  ProcessedEmail · DTOs:      │   │  Adapter · AI (Gemini/Ollama)   │
│  ClassAlertDto, EmailDto    │   │  WebSocket · Config             │
└──────────────────────────────┘   └─────────────────────────────────┘
```

## Fluxo de Dados (Email → Calendário)

1. **Agendamento** (a cada 60s): `EmailPollingService.pollEmails()`
2. **Gmail**: busca emails não lidos conforme categorias ativas
3. **Por email**: extrai corpo, salva em `ProcessedEmail`, chama **AiAnalyzer** (Gemini ou Ollama)
4. **Alerta**: cria evento no **Google Calendar**, persiste em **Alert**, envia via **NotificationSender** (WebSocket), marca email como lido

## Princípios Aplicados

| Princípio | Aplicação |
|-----------|-----------|
| **SOLID – DIP** | Serviços dependem de interfaces (`AiAnalyzer`, `GmailClient`, `NotificationSender`); implementações na infraestrutura |
| **SOLID – SRP** | Cada classe com uma responsabilidade (ex.: `EmailPollingService` orquestra; `GeminiServiceAdapter` só integra com a API) |
| **GoF – Strategy** | `AiAnalyzer`: troca de provedor (Gemini vs Ollama) sem alterar serviço |
| **GoF – Adapter** | `GmailAdapter`, `GeminiServiceAdapter`, `WebSocketNotificationAdapter`: adaptam APIs externas ao contrato da camada de serviço |
| **Clean Code** | Nomes claros, métodos pequenos, exceções específicas, fail-fast com validação |

## Estrutura de Pacotes (Layered)

```
com.redalert.backend/
├── domain/
│   └── model/                    # Entidades e DTOs de domínio
│       ├── Alert.java
│       ├── Category.java
│       ├── ProcessedEmail.java
│       ├── ClassAlertDto.java
│       └── EmailDto.java
│
├── service/                      # Casos de uso e contratos
│   ├── AiAnalyzer.java           # Interface (Strategy)
│   ├── GmailClient.java          # Interface (Adapter)
│   ├── NotificationSender.java   # Interface (Adapter)
│   ├── AlertHistoryService.java
│   ├── CategoryService.java
│   ├── EmailPollingService.java
│   ├── ProcessedEmailService.java
│   └── exception/
│       ├── AiAnalysisException.java
│       ├── CalendarIntegrationException.java
│       └── GmailIntegrationException.java
│
├── infrastructure/
│   ├── persistence/              # Repositórios JPA
│   │   ├── AlertRepository.java
│   │   ├── CategoryRepository.java
│   │   └── ProcessedEmailRepository.java
│   ├── gmail/adapter/
│   │   └── GmailAdapter.java     # implements GmailClient
│   ├── ai/adapter/
│   │   ├── GeminiServiceAdapter.java  # implements AiAnalyzer
│   │   └── OllamaServiceAdapter.java  # implements AiAnalyzer
│   ├── messaging/adapter/
│   │   └── WebSocketNotificationAdapter.java  # implements NotificationSender
│   └── config/
│       ├── CorsConfig.java
│       ├── GoogleConfig.java
│       ├── WebClientConfig.java
│       └── WebSocketConfig.java
│
└── presentation/
    ├── controller/
    ├── dto/
    └── exception/
        └── GlobalExceptionHandler.java
```

## Resiliência

- **Circuit Breaker** (Resilience4j): Gmail e Gemini/Ollama com fallback
- **Exceções**: `GmailIntegrationException`, `CalendarIntegrationException`, `AiAnalysisException` tratadas no `GlobalExceptionHandler`

## Resumo

- **Apresentação** → expõe REST e trata erros.
- **Serviço** → orquestra regras e define contratos (interfaces).
- **Domínio** → apenas modelos (entidades e DTOs).
- **Infraestrutura** → persistência, Gmail, IA e WebSocket implementam os contratos do serviço.

Dependências: apresentação → serviço → domínio; infraestrutura → domínio e implementa interfaces do serviço.
