# 🚀 Guia de Início Rápido - Red Alert

## ⚡ Setup em 10 Minutos

### Passo 1: Clonar e Navegar (30s)

```bash
cd red-alert-app
```

### Passo 2: Backend - Google OAuth2 (5min)

#### 2.1 Google Cloud Console

1. Acesse: https://console.cloud.google.com/
2. Crie projeto: **"Red Alert Monitor"**
3. Ative APIs:
   - Gmail API: https://console.cloud.google.com/apis/library/gmail.googleapis.com
   - Calendar API: https://console.cloud.google.com/apis/library/calendar-json.googleapis.com

#### 2.2 Criar Credenciais

1. **APIs & Services > Credentials**
2. **+ CREATE CREDENTIALS > OAuth client ID**
3. Tipo: **Desktop app**
4. Nome: **Red Alert Desktop**
5. **CREATE** → **Download JSON**

#### 2.3 Configurar

```bash
# Mover arquivo baixado
mv ~/Downloads/client_secret_*.json backend/src/main/resources/credentials.json
```

### Passo 3: Backend - Executar (2min)

```bash
cd backend

# Compilar
mvn clean install

# Executar
mvn spring-boot:run
```

**Na primeira vez**: Browser abrirá → Login Google → Autorizar

✅ Backend rodando em: `http://localhost:8081`

### Passo 4: Frontend - Executar (2min)

```bash
# Em outro terminal
cd frontend

# Instalar dependências
npm install

# Executar
npm run dev
```

✅ Frontend rodando em: `http://localhost:5173`

### Passo 5: Verificar Funcionamento (1min)

1. Abra: http://localhost:5173
2. Deve mostrar: **"🟢 Monitorando Red-Alert System"**
3. Console do browser: **"✅ Connected to Red Alert WebSocket"**

---

## 🧪 Testar Alerta

### Opção 1: Email Real

```
1. Envie email para sua conta Gmail
   Assunto: "Aula de IA Generativa"
   Corpo: "Reunião importante às 19h"

2. Aguarde até 1 minuto

3. 💥 BOOM! Alerta vermelho explode na tela
```

### Opção 2: Endpoint de Teste (Criar)

Adicione no backend um controller de teste:

```java
@PostMapping("/api/v1/test/alert")
public void testAlert() {
    ClassAlertDto alert = new ClassAlertDto(
        "Teste de Alerta",
        LocalDateTime.now().plusMinutes(5),
        "https://meet.google.com/test",
        "Alerta de teste",
        true
    );
    notificationPort.sendAlert(alert);
}
```

Então:

```bash
curl -X POST http://localhost:8081/api/v1/test/alert
```

---

## ✅ Checklist de Sucesso

- [ ] Backend rodando na porta 8081
- [ ] Frontend rodando na porta 5173
- [ ] Status: "🟢 Monitorando Red-Alert System"
- [ ] Console: "✅ Connected to Red Alert WebSocket"
- [ ] Logs backend: "Starting email polling cycle"

---

## 🚨 Problemas Comuns

### Backend: "credentials.json not found"

```bash
# Verificar
ls backend/src/main/resources/credentials.json

# Se não existir, copie do template e edite
cp backend/src/main/resources/credentials.json.template backend/src/main/resources/credentials.json
```

### Frontend: "Cannot connect to WebSocket"

1. Verifique se backend está rodando
2. Verifique porta 8081: `curl http://localhost:8081/api/v1/health`
3. Verifique CORS em `application.yml`

### OAuth: "redirect_uri_mismatch"

1. Google Cloud Console > Credentials
2. Edite OAuth Client ID
3. Adicione: `http://localhost:8888` em **Authorized redirect URIs**

---

## 📊 Estrutura de Pastas

```
red-alert-app/
├── backend/
│   ├── src/main/
│   │   ├── java/com/pulsar/backend/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── credentials.json  ⚠️ VOCÊ PRECISA CRIAR
│   └── pom.xml
│
└── frontend/
    ├── src/
    │   ├── components/
    │   ├── hooks/
    │   └── App.tsx
    └── package.json
```

---

## 🎯 Próximos Passos

1. ✅ Sistema funcionando
2. 📧 Envie email de teste
3. 🎨 Customize cores em `tailwind.config.js`
4. 🔧 Ajuste polling em `application.yml`
5. 🤖 Configure Gemini API Key (opcional)

---

## 📚 Documentação Completa

- **Visão Geral**: [`README.md`](README.md)
- **Backend**: [`backend/README.md`](backend/README.md)
- **Frontend**: [`frontend/README.md`](frontend/README.md)
- **Arquitetura**: [`backend/ARCHITECTURE.md`](backend/ARCHITECTURE.md)

---

**🎉 Pronto! Seu Red Alert está funcionando!**

Agora você tem um sistema completo de monitoramento de emails com:
- ✅ Polling automático de emails
- ✅ Análise com IA
- ✅ Criação de eventos no Calendar
- ✅ Alertas em tempo real
- ✅ Interface impossível de ignorar

**Boa sorte e não perca mais nenhuma aula! 🚀**
