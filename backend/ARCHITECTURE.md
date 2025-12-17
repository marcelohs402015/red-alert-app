# Arquitetura do Red Alert Backend

## 📐 Visão Geral - Arquitetura Hexagonal

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│  ┌──────────────────┐         ┌─────────────────────────────┐  │
│  │ HealthController │         │ GlobalExceptionHandler      │  │
│  │  /api/v1/health  │         │ (Error handling)            │  │
│  └──────────────────┘         └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                       APPLICATION LAYER                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              EmailPollingService (Use Case)              │  │
│  │  • @Scheduled polling every 60s                          │  │
│  │  • Orchestrates domain logic                             │  │
│  │  • Depends on Ports (interfaces)                         │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                   Custom Exceptions                       │  │
│  │  • GmailIntegrationException                             │  │
│  │  • CalendarIntegrationException                          │  │
│  │  • AiAnalysisException                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                          DOMAIN LAYER                            │
│  ┌─────────────────────┐      ┌──────────────────────────────┐ │
│  │   ClassAlertDto     │      │         PORTS                │ │
│  │   (Domain Model)    │      │  • AiAnalysisPort            │ │
│  │                     │      │  • NotificationPort          │ │
│  │  • title            │      │                              │ │
│  │  • date             │      │  (Interfaces defining        │ │
│  │  • url              │      │   what domain needs)         │ │
│  │  • description      │      │                              │ │
│  │  • isUrgent         │      └──────────────────────────────┘ │
│  └─────────────────────┘                                        │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                     INFRASTRUCTURE LAYER                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    ADAPTERS (Implementations)             │  │
│  │                                                           │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  GeminiServiceAdapter (implements AiAnalysisPort)  │  │  │
│  │  │  • Calls Gemini AI API                             │  │  │
│  │  │  • Circuit Breaker enabled                         │  │  │
│  │  │  • Fallback on failure                             │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │                                                           │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  WebSocketNotificationAdapter                      │  │  │
│  │  │  (implements NotificationPort)                     │  │  │
│  │  │  • Broadcasts to /topic/alerts                     │  │  │
│  │  │  • Uses SimpMessagingTemplate                      │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                    CONFIGURATIONS                         │  │
│  │                                                           │  │
│  │  ┌──────────────────┐  ┌──────────────────────────────┐  │  │
│  │  │  GoogleConfig    │  │  WebSocketConfig             │  │  │
│  │  │  • OAuth2 Flow   │  │  • STOMP over WebSocket      │  │  │
│  │  │  • Gmail Bean    │  │  • Endpoint: /ws-Red Alert      │  │  │
│  │  │  • Calendar Bean │  │  • Broker: /topic            │  │  │
│  │  └──────────────────┘  └──────────────────────────────┘  │  │
│  │                                                           │  │
│  │  ┌──────────────────────────────────────────────────┐    │  │
│  │  │  WebClientConfig                                 │    │  │
│  │  │  • HTTP client for Gemini API                    │    │  │
│  │  └──────────────────────────────────────────────────┘    │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                      EXTERNAL SERVICES                           │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │  Gmail API   │  │ Calendar API │  │   Gemini AI API    │    │
│  │              │  │              │  │                    │    │
│  │  • Read      │  │  • Create    │  │  • Analyze email   │    │
│  │  • Modify    │  │    events    │  │  • Extract info    │    │
│  └──────────────┘  └──────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 Fluxo de Dados

```
1. SCHEDULED TRIGGER (every 60s)
   │
   ▼
2. EmailPollingService.pollEmails()
   │
   ├─► Gmail API: Fetch unread emails
   │   └─► Query: "is:unread label:INBOX"
   │
   ▼
3. For each email:
   │
   ├─► Extract email body (Base64 decode)
   │
   ├─► AiAnalysisPort.analyzeEmailContent()
   │   └─► GeminiServiceAdapter
   │       └─► Gemini AI API (with Circuit Breaker)
   │           └─► Returns ClassAlertDto or null
   │
   ▼
4. If alert.isUrgent():
   │
   ├─► Calendar API: Create event
   │   └─► Event with title, date, description, url
   │
   ├─► NotificationPort.sendAlert()
   │   └─► WebSocketNotificationAdapter
   │       └─► Broadcast to /topic/alerts
   │           └─► All connected clients receive alert
   │
   └─► Gmail API: Mark as read
       └─► Remove UNREAD label
```

## 🎯 Princípios Arquiteturais Aplicados

### 1. **Hexagonal Architecture (Ports & Adapters)**
- **Domain** no centro, sem dependências externas
- **Ports** definem contratos (interfaces)
- **Adapters** implementam integrações externas

### 2. **Dependency Inversion Principle (SOLID)**
- Use Cases dependem de **Ports** (abstrações)
- Não dependem de implementações concretas
- Infraestrutura implementa Ports

### 3. **Single Responsibility Principle**
- Cada classe tem uma única responsabilidade
- `EmailPollingService`: Orquestração
- `GeminiServiceAdapter`: Integração com IA
- `WebSocketNotificationAdapter`: Notificações

### 4. **Circuit Breaker Pattern**
- Proteção contra falhas em cascata
- Fallback automático quando serviços falham
- Configurado via Resilience4j

### 5. **Clean Code**
- Nomes descritivos e significativos
- Métodos pequenos e focados
- JavaDoc em métodos públicos
- Tratamento robusto de exceções

## 📦 Estrutura de Pacotes Detalhada

```
com.Red Alert.backend/
│
├── domain/
│   ├── model/
│   │   └── ClassAlertDto.java          # Domain DTO
│   └── port/
│       ├── AiAnalysisPort.java         # Port for AI
│       └── NotificationPort.java       # Port for notifications
│
├── application/
│   ├── usecase/
│   │   └── EmailPollingService.java    # Main use case
│   └── exception/
│       ├── AiAnalysisException.java
│       ├── GmailIntegrationException.java
│       └── CalendarIntegrationException.java
│
├── infrastructure/
│   ├── config/
│   │   ├── GoogleConfig.java           # OAuth2 + Gmail/Calendar beans
│   │   ├── WebSocketConfig.java        # STOMP configuration
│   │   └── WebClientConfig.java        # HTTP client
│   ├── ai/
│   │   └── adapter/
│   │       └── GeminiServiceAdapter.java  # AI adapter
│   └── messaging/
│       └── adapter/
│           └── WebSocketNotificationAdapter.java  # WebSocket adapter
│
└── presentation/
    ├── controller/
    │   └── HealthController.java       # Health check endpoint
    └── exception/
        └── GlobalExceptionHandler.java # Global error handling
```

## 🔐 Segurança e Resiliência

### OAuth2 Flow
1. Primeira execução: Browser abre para autenticação
2. Token salvo em `tokens/` directory
3. Execuções subsequentes: Token reutilizado

### Circuit Breaker
- **Gmail Service**: 50% failure rate, 10s wait
- **Gemini Service**: 60% failure rate, 15s wait
- Fallback automático em caso de falha

### Error Handling
- Exceções customizadas por tipo de falha
- Global exception handler para respostas consistentes
- Logging detalhado para debugging

## 🚀 Próximos Passos

1. **Integração Real com Gemini**:
   - Substituir stub por chamada real à API
   - Implementar parsing de JSON response

2. **Testes**:
   - Testes unitários para Use Cases
   - Testes de integração para Adapters
   - Mocks para serviços externos

3. **Melhorias**:
   - Persistência de alertas em banco de dados
   - Dashboard de monitoramento
   - Configuração de filtros personalizados
