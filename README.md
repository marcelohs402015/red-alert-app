# 🚨 Red Alert - Sistema de Monitoramento de Emails

Sistema completo de monitoramento em tempo real que detecta emails importantes do google, usa IA para análise e envia alertas dramáticos via WebSocket.

## 📋 Visão Geral

**Red Alert** é uma aplicação full-stack que:

1. 📧 **Monitora emails** no Gmail a cada minuto (configurável)
2. 🤖 **Analisa com IA Local** (Ollama + Llama3) para extrair detalhes sem custos ou limites de API
3. ✨ **Backup com Nuvem** (Gemini 2.0 Flash) disponível como alternativa de alta performance
4. 📅 **Cria eventos** inteligentes no Google Calendar (com proteção anti-duplicidade e logs ricas)
5. 🔔 **Envia alertas** em tempo real via WebSocket
6. 💥 **Exibe overlay** full-screen vermelho impossível de ignorar no frontend
7. 📜 **Histórico Persistente** com confirmações modernas e modais personalizados

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                         FRONTEND                             │
│  React + TypeScript + Tailwind + Framer Motion              │
│  • Dashboard de monitoramento                                │
│  • WebSocket client (STOMP)                                  │
│  • Alert overlay com animações                               │
└─────────────────────────────────────────────────────────────┘
                            ▲
                            │ WebSocket (STOMP)
                            │ /ws-red-alert
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                         BACKEND                              │
│  Java 21 + Spring Boot 3 (Arquitetura em Camadas)           │
│  • Presentation → Service → Domain ← Infrastructure          │
│  • Email polling (@Scheduled), IA (Ollama/Gemini)            │
│  • Google Calendar, WebSocket server (STOMP)                 │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    EXTERNAL & LOCAL SERVICES                 │
│  • Ollama (Local LLM - Llama 3) - 🛡️ Privacidade Total        │
│  • Gmail API (leitura de emails)                            │
│  • Google Calendar API (criação de eventos)                 │
│  • Gemini AI API (Cloud AI Alternative)                     │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Stack Tecnológica

### Backend
- **Java 21** (Virtual Threads)
- **Spring Boot 3.3.6**
- **Google Gmail API**
- **Google Calendar API** (com log visual e proteção anti-duplicidade)
- **Ollama (Local LLM)** - Modelo Llama 3 (Principal)
- **Gemini AI 2.0 Flash** (Backup/Alternativa)
- **Flyway** (Gerenciamento de banco de dados)
- **PostgreSQL** (Persistência de histórico e categorias)
- **WebSocket (STOMP)**
- **Resilience4j** (Circuit Breaker para Gmail, Gemini e Ollama)
- **Maven**

### Frontend
- **React 19**
- **TypeScript 5.9**
- **Vite 7**
- **Tailwind CSS 3.4**
- **Framer Motion 11**
- **STOMP.js**
- **Lucide React**

## 📦 Estrutura do Projeto

```
red-alert-app/
├── backend/                    # Java Spring Boot
│   ├── src/main/java/com/redalert/backend/
│   │   ├── domain/             # Modelo (entidades e DTOs)
│   │   ├── service/            # Casos de uso e interfaces (AiAnalyzer, GmailClient, NotificationSender)
│   │   ├── infrastructure/    # Persistência (JPA), Gmail, IA, WebSocket, config
│   │   └── presentation/       # Controllers REST e DTOs
│   ├── pom.xml
│   ├── README.md
│   ├── ARCHITECTURE.md
│   └── SETUP.md
│
├── frontend/                   # React + TypeScript
│   ├── src/
│   │   ├── components/         # Componentes React
│   │   ├── hooks/               # Custom hooks
│   │   ├── types/               # TypeScript types
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   └── README.md
│
├── docker-compose.yml          # PostgreSQL (porta 5432)
└── docs/                       # Documentação
```

## 🔧 Setup Completo

### Pré-requisitos

- **Java 21** ou superior
- **Maven 3.8+**
- **Node.js 18+** e npm
- **Docker** (para PostgreSQL)
- **Conta Google** (Gmail + Calendar)
- **Ollama** (opcional, para IA local)
- **Gemini API Key** (opcional; sem ela o backend sobe, mas análise por Gemini fica desabilitada)

### 1️⃣ Subir o banco de dados (Docker)

Na raiz do projeto:

```bash
docker-compose up -d
```

Isso sobe o **PostgreSQL** na porta **5432** (banco `redalert`, user/senha `postgres`). Não é necessário PgAdmin.

### 2️⃣ Configurar IA Local (Ollama) – opcional

1. Instale o [Ollama](https://ollama.com/)
2. No terminal, baixe o modelo Llama 3:
   ```bash
   ollama run llama3
   ```
3. Mantenha o Ollama rodando (ícone da Lhama no System Tray)

### 3️⃣ Configurar Backend

```bash
cd backend

# 1. Google OAuth2 (ver backend/SETUP.md)
#    - Criar projeto no Google Cloud Console
#    - Ativar Gmail API e Calendar API
#    - Baixar credentials.json para src/main/resources/

# 2. (Opcional) Definir API key do Gemini para análise em nuvem
#    PowerShell:
$env:GEMINI_API_KEY="sua-chave-aqui"

# 3. Compilar e executar
mvn clean install
mvn spring-boot:run
```

Backend rodará em: **`http://localhost:8086`**  
Swagger UI: **`http://localhost:8086/swagger-ui.html`**

### 4️⃣ Configurar Frontend

```bash
cd frontend

npm install
npm run dev
```

Frontend rodará em: **`http://localhost:5173`**

### 5️⃣ Primeira Execução

1. **Backend**: o navegador abrirá para autenticação Google (Gmail + Calendar).
2. Faça login e autorize o acesso.
3. O token é salvo em `backend/tokens/`.
4. **Frontend**: conecta automaticamente ao WebSocket em `http://localhost:8086/ws-red-alert`.

## 🎯 Como Funciona

### Fluxo Completo

```
1. POLLING (a cada 60s)
   └─► Backend busca emails não lidos no Gmail baseados em Categorias/Filtros

2. ANÁLISE IA LOCAL (Ollama + Llama 3)
   └─► Processamento privado no seu hardware.
   └─► Extrai: Título, Data/Hora Exata, URL da Reunião e Descrição Rica.

3. PROCESSAMENTO & PERSISTÊNCIA
   ├─► Salva Alerta no Banco de Dados (PostgreSQL)
   └─► Google Calendar:
       ├─► Verifica se evento já existe (evita duplicatas)
       └─► Cria evento com link direto e resumo automático

4. NOTIFICAÇÃO (WebSocket)
   └─► Frontend recebe alerta e FORÇA o estado de Urgência (Red Alert)
       ├─► Toca som de alerta
       ├─► Exibe overlay full-screen vermelho pulsante
       └─► Botão "VER NO CALENDAR" disponível imediatamente
```

## 🎨 Screenshots

### Dashboard (Estado Normal)
- Status de conexão em tempo real
- Indicadores visuais animados
- Design moderno com gradientes

### Alert Overlay (Aula Detectada)
- Full-screen vermelho pulsante
- Título gigante impossível de ignorar
- Botões de ação grandes e claros
- Animações dramáticas com Framer Motion

## 🧪 Testar o Sistema

### Opção 1: Email Real

1. Envie um email para sua conta Gmail
2. Assunto: "Aula de IA Generativa"
3. Corpo: Inclua palavras como "aula", "reunião", "meeting"
4. Aguarde até 1 minuto (polling)
5. Alerta aparecerá no frontend

### Opção 2: Simulação manual (API)

```powershell
# Alerta de teste
Invoke-RestMethod -Uri "http://localhost:8086/api/v1/alerts/simulate/test" -Method POST
```

## 📚 Documentação Detalhada

- **Backend**:
  - [`backend/README.md`](backend/README.md) - Documentação geral
  - [`backend/ARCHITECTURE.md`](backend/ARCHITECTURE.md) - Arquitetura em camadas (SOLID, Clean Code, GoF)
  - [`backend/SETUP.md`](backend/SETUP.md) - Setup em 5 minutos

- **Frontend**:
  - [`frontend/README.md`](frontend/README.md) - Documentação completa

## 🛡️ Segurança

- ✅ OAuth2 para Google APIs
- ✅ Tokens armazenados localmente
- ✅ CORS configurado
- ✅ WebSocket com SockJS fallback
- ✅ Sem credenciais hardcoded

## 🔒 Variáveis de Ambiente

### Backend

| Variável          | Obrigatória | Descrição |
|-------------------|-------------|-----------|
| `GEMINI_API_KEY`  | Não         | API key do Google AI (Gemini). Se não definida, o backend sobe normalmente e a análise por Gemini fica desabilitada. |

**Definir no PowerShell (antes de `mvn spring-boot:run`):**
```powershell
$env:GEMINI_API_KEY="sua-chave-aqui"
```

O restante (Google OAuth2, Ollama, WebSocket) é lido do `application.yml`. Banco: `localhost:5432/redalert` (via Docker Compose).

## 🚨 Troubleshooting

### Backend não inicia (GEMINI_API_KEY)

O backend **não exige** mais a variável. Se quiser usar o Gemini, defina antes de rodar:
```powershell
$env:GEMINI_API_KEY="sua-chave"; mvn spring-boot:run
```

### Backend não conecta ao Gmail

1. Verifique `credentials.json` em `backend/src/main/resources/`
2. Delete a pasta `tokens/` e rode de novo para reautenticar
3. Confirme no Google Cloud Console se Gmail API e Calendar API estão ativadas

### Backend não conecta ao banco

1. Suba o PostgreSQL: `docker-compose up -d`
2. Banco: `localhost:5432`, database `redalert`, user/senha `postgres`

### Frontend não recebe alertas

1. Confirme se o backend está rodando em **porta 8086**
2. No console do browser deve aparecer algo como "Connected" ao WebSocket
3. CORS está configurado para `http://localhost:5173` e `http://localhost:3000`

### Alerta não aparece

1. Verifique se email contém palavras-chave
2. Verifique logs do backend
3. Verifique se `isUrgent: true` no payload

## 🎯 Próximas Funcionalidades (Roadmap)

- [ ] **Versão Desktop (Electron/Tauri):** App no System Tray com notificações nativas.
- [ ] **Serviço Windows:** Rodar backend como serviço oficial (`Services.msc`).
- [ ] **Deploy Cloud (GKE):** Publicação no Google Kubernetes Engine com IAP (Identity-Aware Proxy).
- [ ] Configurações personalizadas (filtros dinâmicos via UI, sons customizados)
- [ ] Integração com Microsoft Teams
- [ ] Testes automatizados (JUnit + Vitest)

## 🤝 Contribuindo

Este projeto segue:
- **Arquitetura em camadas** (Presentation → Service → Domain ← Infrastructure)
- **SOLID**, **Clean Code** (Robert C. Martin), **GoF** (Strategy, Adapter)
- **TypeScript Strict Mode**
- **Tailwind CSS** (sem CSS customizado)

## 📄 Licença

MIT License

---

## 🎉 Pronto para Usar!

1. Suba o banco: `docker-compose up -d`
2. Configure Google OAuth2 (credentials em `backend/src/main/resources/`)
3. (Opcional) Defina `GEMINI_API_KEY` se quiser análise com Gemini
4. Execute o backend: `cd backend && mvn spring-boot:run`
5. Execute o frontend: `cd frontend && npm run dev`
6. Envie um email de teste ou use o endpoint de simulação
7. Aguarde o alerta no overlay! 🚨

**Desenvolvido com ❤️ por Marcelo Hernandes da Silva — MSTech IA Solutions**
