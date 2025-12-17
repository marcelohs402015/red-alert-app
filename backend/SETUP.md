# 🚀 Guia de Setup Rápido - Red Alert Backend

## ⚡ Setup em 5 Minutos

### 1️⃣ Pré-requisitos
```bash
# Verificar Java 21
java -version

# Verificar Maven
mvn -version
```

### 2️⃣ Configurar Google OAuth2

#### Passo 1: Google Cloud Console
1. Acesse: https://console.cloud.google.com/
2. Crie um projeto novo: **"Red Alert Email Monitor"**
3. Ative as APIs:
   - Gmail API: https://console.cloud.google.com/apis/library/gmail.googleapis.com
   - Calendar API: https://console.cloud.google.com/apis/library/calendar-json.googleapis.com

#### Passo 2: Criar Credenciais OAuth2
1. Vá em: **APIs & Services > Credentials**
2. Clique: **+ CREATE CREDENTIALS > OAuth client ID**
3. Tipo de aplicativo: **Desktop app**
4. Nome: **Red Alert Desktop Client**
5. Clique: **CREATE**
6. **Download JSON** (botão de download)

#### Passo 3: Configurar Credenciais
```bash
# Renomear o arquivo baixado
mv ~/Downloads/client_secret_*.json src/main/resources/credentials.json
```

### 3️⃣ Configurar Gemini API (Opcional)

```bash
# Obter API Key: https://makersuite.google.com/app/apikey
export GEMINI_API_KEY=your-actual-api-key-here
```

Ou edite `application.yml`:
```yaml
gemini:
  api:
    key: your-actual-api-key-here
```

### 4️⃣ Compilar e Executar

```bash
# Compilar
mvn clean install

# Executar
mvn spring-boot:run
```

### 5️⃣ Primeira Autenticação

Na primeira execução:
1. ✅ Browser abrirá automaticamente
2. ✅ Faça login com sua conta Google
3. ✅ Autorize acesso ao Gmail e Calendar
4. ✅ Token salvo em `tokens/` (reutilizado depois)

### 6️⃣ Verificar Funcionamento

```bash
# Health check
curl http://localhost:8081/api/v1/health

# Logs
# Você verá: "Starting email polling cycle" a cada 1 minuto
```

## 🔧 Troubleshooting

### Erro: "credentials.json not found"
```bash
# Verificar se arquivo existe
ls -la src/main/resources/credentials.json

# Se não existir, copie do template e edite
cp src/main/resources/credentials.json.template src/main/resources/credentials.json
# Edite com suas credenciais reais
```

### Erro: "Port 8081 already in use"
```bash
# Mudar porta em application.yml
server:
  port: 8082
```

### Erro: OAuth2 "redirect_uri_mismatch"
1. Vá em Google Cloud Console > Credentials
2. Edite seu OAuth Client ID
3. Adicione: `http://localhost:8888` em **Authorized redirect URIs**

## 📊 Estrutura de Pastas Criada

```
backend/
├── src/main/java/com/Red Alert/backend/
│   ├── domain/              ✅ Domínio (regras de negócio)
│   ├── application/         ✅ Casos de uso
│   ├── infrastructure/      ✅ Adaptadores (Google, WebSocket)
│   └── presentation/        ✅ Controllers REST
├── src/main/resources/
│   ├── application.yml      ✅ Configurações
│   └── credentials.json     ⚠️  VOCÊ PRECISA CRIAR
├── pom.xml                  ✅ Dependências Maven
└── README.md                ✅ Documentação completa
```

## 🎯 Próximos Passos

1. **Testar WebSocket**:
   - Use o frontend React (próxima fase)
   - Ou teste com: https://www.websocket.org/echo.html

2. **Monitorar Logs**:
   ```bash
   tail -f logs/spring.log
   ```

3. **Customizar Polling**:
   - Edite `application.yml` > `email.polling.query`
   - Exemplo: `"is:unread from:professor@universidade.edu"`

## 📚 Documentação Completa

- **README.md**: Documentação geral
- **ARCHITECTURE.md**: Diagramas e arquitetura detalhada
- **application.yml**: Todas as configurações

## 🆘 Suporte

Se encontrar problemas:
1. Verifique logs em `logs/`
2. Consulte `ARCHITECTURE.md` para entender o fluxo
3. Verifique se todas as APIs estão ativadas no Google Cloud Console

---

**✨ Pronto! Seu backend Red Alert está rodando!**
