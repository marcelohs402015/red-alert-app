# Red Alert Frontend

Sistema de monitoramento de emails google em tempo real com alertas visuais dramáticos.

## 🎨 Stack Tecnológica

- **React 19** - UI Library
- **TypeScript** - Type safety
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **Framer Motion** - Animations
- **STOMP.js** - WebSocket client
- **Lucide React** - Icons

## 🚀 Setup Rápido

### 1. Instalar Dependências

```bash
npm install
```

### 2. Executar em Desenvolvimento

```bash
npm run dev
```

A aplicação estará disponível em: `http://localhost:5173`

### 3. Build para Produção

```bash
npm run build
```

## 📁 Estrutura do Projeto

```
src/
├── components/
│   └── AlertOverlay.tsx       # Componente de alerta full-screen
├── hooks/
│   └── useRedAlertSocket.ts   # Custom hook para WebSocket
├── types/
│   └── alert.ts               # TypeScript types
├── App.tsx                     # Dashboard principal
├── main.tsx                    # Entry point
└── index.css                   # Estilos globais + Tailwind
```

## 🔌 Integração com Backend

### WebSocket Connection

O frontend se conecta automaticamente ao backend via WebSocket:

- **URL**: `http://localhost:8081/ws-red-alert`
- **Protocol**: STOMP over SockJS
- **Topic**: `/topic/alerts`

### Payload Esperado

```typescript
{
  "title": "Pós Graduação: IA Generativa",
  "date": "2023-10-27T19:00:00",
  "url": "https://meet.google.com/...",
  "description": "Aula ao vivo",
  "isUrgent": true
}
```

## 🎯 Funcionalidades

### 1. Dashboard de Monitoramento

- Status de conexão em tempo real
- Indicadores visuais (conectado/desconectado/erro)
- Animações suaves com Framer Motion
- Design moderno com gradientes

### 2. Alert Overlay

Quando um alerta é recebido:

- ✅ **Full-screen overlay** vermelho impossível de ignorar
- ✅ **Animações dramáticas**: Scale-in com bounce + pulsação
- ✅ **Som de alerta**: Beep gerado via Web Audio API
- ✅ **Informações claras**: Título, data/hora, descrição
- ✅ **Ações rápidas**: Botão "Entrar na Aula" + "Dispensar"

### 3. Reconexão Automática

- Reconecta automaticamente se perder conexão
- Heartbeat a cada 4 segundos
- Delay de 5 segundos entre tentativas

## 🎨 Design System

### Cores

```css
Red Alert Palette:
- Primary: #dc2626 (red-600)
- Background: Gradient slate-900 → slate-800
- Text: White/Gray scale
- Accent: Green (status conectado)
```

### Animações

- **Scale-in**: Entrada dramática do alerta
- **Pulse**: Fundo pulsante para urgência
- **Rotate**: Ícone de status girando
- **Opacity**: Transições suaves

## 🔧 Configuração

### Vite Config

```typescript
server: {
  port: 5173,
  proxy: {
    '/api': 'http://localhost:8081'
  }
}
```

### Tailwind Config

Cores customizadas e animações definidas em `tailwind.config.js`.

## 🧪 Desenvolvimento

### Testar Conexão WebSocket

1. Inicie o backend: `mvn spring-boot:run` (porta 8081)
2. Inicie o frontend: `npm run dev` (porta 5173)
3. Verifique o console: Deve mostrar "✅ Connected to Red Alert WebSocket"

### Simular Alerta

No backend, envie um email que contenha palavras-chave como "aula" ou "reunião". O sistema irá:

1. Detectar o email
2. Analisar com Gemini AI
3. Criar evento no Calendar
4. Enviar via WebSocket
5. Frontend exibe alerta dramático

## 🎭 Componentes Principais

### `useRedAlertSocket` Hook

```typescript
const { 
  connectionStatus,  // 'connected' | 'disconnected' | 'connecting' | 'error'
  latestAlert,       // ClassAlert | null
  clearAlert,        // () => void
  reconnect          // () => void
} = useRedAlertSocket();
```

### `AlertOverlay` Component

```typescript
<AlertOverlay 
  alert={latestAlert} 
  onDismiss={clearAlert} 
/>
```

### `App` Component

Dashboard principal que orquestra tudo.

## 🚨 Troubleshooting

### WebSocket não conecta

1. Verifique se backend está rodando na porta 8081
2. Verifique CORS no backend (`application.yml`)
3. Verifique console do browser para erros

### Alerta não aparece

1. Verifique se `latestAlert` não é `null` no console
2. Verifique se `isUrgent: true` no payload
3. Verifique animações do Framer Motion

### Som não toca

- Alguns browsers bloqueiam áudio sem interação do usuário
- Clique na página antes para habilitar áudio

## 📚 Scripts Disponíveis

```bash
npm run dev      # Desenvolvimento
npm run build    # Build produção
npm run preview  # Preview do build
npm run lint     # ESLint
```

## 🎯 Próximos Passos

1. **Histórico de Alertas**: Persistir alertas recebidos
2. **Configurações**: Permitir customizar som e cores
3. **Notificações Desktop**: Usar Notification API
4. **PWA**: Transformar em Progressive Web App
5. **Testes**: Adicionar testes com Vitest

## 🌟 Destaques Técnicos

- ✅ **TypeScript Strict Mode**: Zero `any`
- ✅ **Componentes Funcionais**: Hooks only
- ✅ **Framer Motion**: Animações performáticas
- ✅ **Tailwind CSS**: Sem CSS customizado
- ✅ **Acessibilidade**: ARIA labels e keyboard navigation
- ✅ **Responsivo**: Mobile-first design

---

**🎉 Frontend Red Alert pronto para uso!**
