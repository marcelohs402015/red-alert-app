# 🚨 Red Alert - Sistema de Monitoramento de Emails

Sistema completo de monitoramento em tempo real que detecta emails importantes do google, usa IA para análise e envia alertas dramáticos via WebSocket.

## 📋 Visão Geral

**Red Alert** é uma aplicação full-stack que:

1. 📧 **Monitora emails** no Gmail a cada minuto (configurável)
2. 🤖 **Analisa com IA** (Gemini 2.0 Flash) para extrair detalhes de eventos
3. 📅 **Cria eventos** inteligentes no Google Calendar (com proteção anti-duplicidade)
4. 🔔 **Envia alertas** em tempo real via WebSocket
5. 💥 **Exibe overlay** full-screen vermelho impossível de ignorar no frontend
6. 📜 **Histórico Persistente** de alertas salvos em banco de dados PostgreSQL

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
│  Java 21 + Spring Boot 3 (Hexagonal Architecture)           │
│  • Email polling service (@Scheduled)                        │
│  • Gemini AI integration                                     │
│  • Google Calendar integration                               │
│  • WebSocket server (STOMP)                                  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    EXTERNAL SERVICES                         │
│  • Gmail API (leitura de emails)                            │
│  • Google Calendar API (criação de eventos)                 │
│  • Gemini AI API (análise de conteúdo)                      │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Stack Tecnológica

### Backend
- **Java 21** (Virtual Threads)
- **Spring Boot 3.3.6**
- **Google Gmail API**
- **Google Calendar API** (com lógica de detecção de duplicatas)
- **Gemini AI 2.0 Flash**
- **Flyway** (Gerenciamento de banco de dados)
- **PostgreSQL** (Persistência de histórico e categorias)
- **WebSocket (STOMP)**
- **Resilience4j** (Circuit Breaker)
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
│   ├── src/main/java/com/pulsar/backend/
│   │   ├── domain/            # Camada de Domínio
│   │   ├── application/       # Casos de Uso
│   │   ├── infrastructure/    # Adaptadores
│   │   └── presentation/      # Controllers REST
│   ├── pom.xml
│   ├── README.md
│   ├── ARCHITECTURE.md
│   └── SETUP.md
│
├── frontend/                   # React + TypeScript
│   ├── src/
│   │   ├── components/        # Componentes React
│   │   ├── hooks/             # Custom hooks
│   │   ├── types/             # TypeScript types
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   └── README.md
│
└── docs/                       # Documentação
    ├── persona-front
    ├── persona-java
    ├── project-arch
    ├── rules-front
    └── rules-java
```

## 🔧 Setup Completo

### Pré-requisitos

- **Java 21** ou superior
- **Maven 3.8+**
- **Node.js 18+** e npm
- **Conta Google** (Gmail + Calendar)
- **Gemini API Key** (opcional)

### 1️⃣ Configurar Backend

```bash
cd backend

# Configurar Google OAuth2 (ver backend/SETUP.md)
# 1. Criar projeto no Google Cloud Console
# 2. Ativar Gmail API e Calendar API
# 3. Baixar credentials.json para src/main/resources/

# Compilar e executar
mvn clean install
mvn spring-boot:run
```

Backend rodará em: `http://localhost:8081`

### 2️⃣ Configurar Frontend

```bash
cd frontend

# Instalar dependências
npm install

# Executar em desenvolvimento
npm run dev
```

Frontend rodará em: `http://localhost:5173`

### 3️⃣ Primeira Execução

1. **Backend**: Browser abrirá para autenticação Google
2. Faça login e autorize acesso
3. Token salvo em `backend/tokens/`
4. **Frontend**: Conectará automaticamente ao WebSocket

## 🎯 Como Funciona

### Fluxo Completo

```
1. POLLING (a cada 60s)
   └─► Backend busca emails não lidos no Gmail baseados em Categorias/Filtros

2. ANÁLISE IA (Gemini 2.0 Flash)
   └─► Extrai: Título, Data/Hora Exata, URL da Reunião e Descrição Rica

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

### Opção 2: Simulação Manual

No backend, você pode criar um endpoint de teste para enviar alertas diretamente.

## 📚 Documentação Detalhada

- **Backend**:
  - [`backend/README.md`](backend/README.md) - Documentação geral
  - [`backend/ARCHITECTURE.md`](backend/ARCHITECTURE.md) - Arquitetura hexagonal
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

### Backend (`application.yml`)

```yaml
google:
  credentials:
    file-path: classpath:credentials.json

gemini:
  api:
    key: ${GEMINI_API_KEY:your-api-key}

websocket:
  allowed-origins: http://localhost:3000,http://localhost:5173
```

## 🚨 Troubleshooting

### Backend não conecta ao Gmail

1. Verifique `credentials.json` em `src/main/resources/`
2. Delete pasta `tokens/` e reautentique
3. Verifique se APIs estão ativadas no Google Cloud Console

### Frontend não recebe alertas

1. Verifique se backend está rodando (porta 8081)
2. Abra console do browser: deve mostrar "✅ Connected"
3. Verifique CORS no backend

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
- **Clean Architecture** e **Hexagonal Architecture**
- **SOLID Principles**
- **Clean Code** (Robert C. Martin)
- **TypeScript Strict Mode**
- **Tailwind CSS** (sem CSS customizado)

## 📄 Licença

MIT License

---

## 🎉 Pronto para Usar!

1. Configure Google OAuth2
2. Execute backend: `mvn spring-boot:run`
3. Execute frontend: `npm run dev`
4. Envie um email de teste
5. Aguarde o alerta dramático! 🚨

**Desenvolvido com ❤️ por Marcelo Hernandes da Silva usando Java 21, Spring Boot 3, React 19 e Gemini AI**
