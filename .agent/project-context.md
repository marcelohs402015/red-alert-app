# Red Alert App - Contexto do Projeto

**Última atualização:** 2025-12-17T16:49:40-03:00

## 🎯 Visão Geral

Sistema de monitoramento de emails em tempo real que detecta alertas urgentes (como aulas ao vivo) e notifica o usuário com overlay full-screen.

## 🏗️ Arquitetura

### Backend (Java 21 + Spring Boot 3)
- **Arquitetura em camadas** (Presentation → Service → Domain; Infrastructure implementa contratos do Service)
- **SOLID, Clean Code, GoF** (Strategy: AiAnalyzer; Adapter: GmailClient, NotificationSender)
- **Banco de dados:** PostgreSQL
- **Migrations:** Flyway (V1 e V2)
- **Integrações:** Gmail API, Google Calendar API, Gemini/Ollama AI
- **WebSocket:** STOMP para notificações em tempo real

### Frontend (React 19 + TypeScript + Vite 7)
- **UI:** Tailwind CSS + Framer Motion
- **Ícones:** Lucide React
- **WebSocket:** STOMP.js para receber alertas

## 📁 Estrutura do Backend (Layered)

```
backend/src/main/java/com/redalert/backend/
├── domain/model/
│   ├── Alert.java, Category.java, ProcessedEmail.java
│   ├── ClassAlertDto.java, EmailDto.java
├── service/
│   ├── AiAnalyzer.java, GmailClient.java, NotificationSender.java  # Interfaces
│   ├── AlertHistoryService, CategoryService, EmailPollingService, ProcessedEmailService
│   └── exception/  # AiAnalysisException, CalendarIntegrationException, GmailIntegrationException
├── infrastructure/
│   ├── persistence/  # AlertRepository, CategoryRepository, ProcessedEmailRepository (JPA)
│   ├── gmail/adapter/GmailAdapter.java      # implements GmailClient
│   ├── ai/adapter/   # GeminiServiceAdapter, OllamaServiceAdapter (implement AiAnalyzer)
│   ├── messaging/adapter/WebSocketNotificationAdapter.java  # implements NotificationSender
│   └── config/       # CORS, Google, WebClient, WebSocket
└── presentation/
    ├── controller/   # Alert, Category, Email, ProcessedEmail, Health
    └── dto/          # Request/Response DTOs
```

## 📁 Estrutura do Frontend

```
frontend/src/
├── components/
│   ├── AlertHistory.tsx        # Card de histórico de alertas
│   ├── AlertOverlay.tsx        # Overlay full-screen (ALERTA!)
│   ├── CategoryManager.tsx     # CRUD de categorias
│   ├── ConfirmModal.tsx        # Modal de confirmação
│   ├── EmailList.tsx           # Lista de emails capturados
│   ├── Portal.tsx              # Portal para modais
│   └── ProcessedEmailsModal.tsx # Modal de emails processados
├── hooks/
│   └── useRedAlertSocket.ts    # Hook de conexão WebSocket
├── services/
│   └── api.ts                  # Cliente HTTP para backend
├── types/
│   └── alert.ts                # Tipos TypeScript
├── App.tsx                     # Componente principal
└── main.tsx                    # Entry point
```

## 🗄️ Banco de Dados (PostgreSQL)

### Tabelas:
1. **categories** - Categorias de monitoramento
   - `id`, `name`, `from_filter`, `subject_keywords`, `body_keywords`, `is_active`

2. **alerts** - Histórico de alertas
   - `id`, `title`, `description`, `alert_date`, `url`, `is_urgent`, `category_id`

3. **processed_emails** - Emails processados
   - `id`, `email_id`, `from_address`, `subject`, `snippet`, `received_at`, `category_id`, `processed_at`

## 🔌 APIs Disponíveis

### Categorias
- `GET /api/v1/categories` - Listar todas
- `POST /api/v1/categories` - Criar
- `PUT /api/v1/categories/{id}` - Atualizar
- `DELETE /api/v1/categories/{id}` - Deletar
- `PATCH /api/v1/categories/{id}/toggle` - Alternar ativo/inativo

### Alertas
- `GET /api/v1/alerts/history` - Histórico
- `GET /api/v1/alerts/urgent` - Apenas urgentes
- `GET /api/v1/alerts/stats` - Estatísticas
- `DELETE /api/v1/alerts/history` - Limpar histórico
- `POST /api/v1/alerts/simulate/{processedEmailId}` - Simular alerta de email
- `POST /api/v1/alerts/simulate/test` - Simular alerta de teste

### Emails Processados
- `GET /api/v1/processed-emails` - Listar todos
- `GET /api/v1/processed-emails/category/{categoryId}` - Por categoria
- `GET /api/v1/processed-emails/count` - Contagem
- `DELETE /api/v1/processed-emails/{id}` - Deletar um
- `DELETE /api/v1/processed-emails` - Deletar todos

### Polling
- `POST /api/v1/emails/trigger-polling` - Disparar polling manual

## ⚙️ Configurações

### Variáveis de Ambiente
```properties
GEMINI_API_KEY=sua-chave-aqui
```

### Polling
```properties
email.polling.fixed-delay=60000  # 60 segundos
```

## 🚀 Comandos para Executar

### Backend
```powershell
cd backend
$env:GEMINI_API_KEY="sua-chave"
mvn spring-boot:run -DskipTests
```

### Frontend
```powershell
cd frontend
npm run dev
```

## 🔮 Integrações Futuras Sugeridas

1. **Notificações Desktop** - Notification API do browser
2. **PWA para Mobile** - Service Worker + Manifest
3. **Microsoft Teams** - Webhook para enviar alertas
4. **Slack** - Webhook para enviar alertas
5. **Discord** - Bot/Webhook para enviar alertas
6. **Telegram Bot** - Enviar alertas via Telegram
7. **SMS** - Twilio/AWS SNS para alertas críticos
8. **Push Notifications** - Firebase Cloud Messaging
9. **Agregador de RSS/Atom** - Monitorar feeds
10. **Monitoramento de YouTube** - Detectar lives

## 📝 Notas Técnicas

- **Autenticação Google**: OAuth2 com tokens armazenados localmente
- **CORS**: Configurado para localhost:5173 e localhost:3000
- **WebSocket**: Endpoint `/ws` com STOMP, tópico `/topic/alerts`
- **Análise IA**: Gemini analisa corpo do email e extrai título, data, URL, urgência
- **Polling**: A cada 60s busca emails não lidos que correspondem às categorias ativas
