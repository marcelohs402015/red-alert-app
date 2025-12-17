# Red Alert Backend

Sistema de monitoramento em tempo real que lê emails, usa IA para identificar compromissos e notifica o frontend via WebSocket.

## 🏗️ Arquitetura

Este projeto segue **Arquitetura Hexagonal (Ports & Adapters)** com princípios de **Clean Architecture**.

### Estrutura de Pacotes

```
com.Red Alert.backend/
├── domain/                    # Camada de Domínio (regras de negócio puras)
│   ├── model/                 # Entidades e DTOs de domínio
│   └── port/                  # Interfaces (Ports) - contratos
├── application/               # Camada de Aplicação (casos de uso)
│   ├── usecase/               # Casos de uso (Use Cases)
│   └── exception/             # Exceções de domínio
├── infrastructure/            # Camada de Infraestrutura (adaptadores)
│   ├── config/                # Configurações (Google OAuth, WebSocket)
│   ├── ai/                    # Adaptador Gemini AI
│   └── messaging/             # Adaptador WebSocket
└── presentation/              # Camada de Apresentação (REST API)
    ├── controller/            # Controllers REST
    └── exception/             # Exception handlers globais
```

## 🚀 Stack Tecnológica

- **Java 21** (Virtual Threads)
- **Spring Boot 3.3.6**
- **Google Gmail API** - Leitura de emails
- **Google Calendar API** - Criação de eventos
- **Gemini AI** - Análise de conteúdo
- **WebSocket (STOMP)** - Notificações em tempo real
- **Resilience4j** - Circuit Breaker pattern
- **Maven** - Gerenciamento de dependências

## 📋 Pré-requisitos

1. **Java 21** instalado
2. **Maven 3.8+** instalado
3. **Conta Google** com acesso ao Gmail e Calendar
4. **Gemini API Key** (opcional para fase inicial)

## 🔧 Configuração

### 1. Google OAuth2 Credentials

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um novo projeto ou selecione um existente
3. Ative as APIs:
   - Gmail API
   - Google Calendar API
4. Crie credenciais OAuth 2.0:
   - Tipo: **Desktop Application**
   - Download do arquivo `credentials.json`
5. Coloque `credentials.json` em `src/main/resources/`

### 2. Gemini API Key (Opcional)

1. Acesse [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Gere uma API Key
3. Configure em `application.yml` ou variável de ambiente:
   ```bash
   export GEMINI_API_KEY=your-api-key-here
   ```

### 3. Configuração do application.yml

O arquivo já está configurado com valores padrão. Ajuste se necessário:

```yaml
google:
  credentials:
    file-path: classpath:credentials.json
  tokens:
    directory: tokens

gemini:
  api:
    key: ${GEMINI_API_KEY:your-api-key-here}

email:
  polling:
    fixed-delay: 60000  # 1 minuto
    query: "is:unread label:INBOX"

websocket:
  allowed-origins: http://localhost:3000,http://localhost:5173
```

## 🏃 Como Executar

### 1. Compilar o projeto

```bash
mvn clean install
```

### 2. Executar a aplicação

```bash
mvn spring-boot:run
```

### 3. Primeira execução - OAuth2

Na primeira execução, o navegador abrirá automaticamente para autenticação Google:

1. Faça login com sua conta Google
2. Autorize o acesso ao Gmail e Calendar
3. O token será salvo em `tokens/` para uso futuro

### 4. Verificar saúde da aplicação

```bash
curl http://localhost:8081/api/v1/health
```

## 🔌 WebSocket

### Endpoint

- **URL**: `ws://localhost:8081/ws-Red Alert`
- **Protocol**: STOMP over WebSocket
- **Topic**: `/topic/alerts`

### Exemplo de conexão (JavaScript)

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const socket = new SockJS('http://localhost:8081/ws-Red Alert');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  stompClient.subscribe('/topic/alerts', (message) => {
    const alert = JSON.parse(message.body);
    console.log('Alert received:', alert);
  });
});
```

## 📊 Fluxo de Funcionamento

1. **Polling de Emails** (a cada 1 minuto):
   - Busca emails não lidos no Gmail
   - Query: `is:unread label:INBOX`

2. **Análise com IA**:
   - Envia corpo do email para Gemini
   - Extrai informações de compromissos

3. **Processamento de Alertas Urgentes**:
   - Cria evento no Google Calendar
   - Envia notificação via WebSocket
   - Marca email como lido

4. **Notificação Frontend**:
   - Clientes conectados recebem alerta em tempo real

## 🧪 Testes

```bash
mvn test
```

## 🛡️ Resiliência

O projeto implementa **Circuit Breaker** pattern usando Resilience4j:

- **Gmail Service**: 10 requisições, 50% falha, 10s espera
- **Gemini Service**: 5 requisições, 60% falha, 15s espera

## 📝 Logs

Logs são gerados com níveis:
- **INFO**: Operações principais (polling, alertas criados)
- **DEBUG**: Detalhes de processamento
- **ERROR**: Falhas e exceções

## 🔒 Segurança

- OAuth2 para autenticação Google
- Tokens armazenados localmente em `tokens/`
- CORS configurado para origens permitidas

## 📚 Referências

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Google Gmail API](https://developers.google.com/gmail/api)
- [Google Calendar API](https://developers.google.com/calendar)
- [Gemini API](https://ai.google.dev/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)

## 🤝 Contribuindo

Este projeto segue princípios de **Clean Code** e **SOLID**. Ao contribuir:

1. Mantenha a arquitetura hexagonal
2. Use injeção de dependência via construtor
3. Crie testes unitários para novos use cases
4. Documente métodos públicos com JavaDoc

## 📄 Licença

MIT License
