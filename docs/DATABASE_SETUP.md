# 🐘 Red Alert - PostgreSQL + Docker Setup

## 📋 **Pré-requisitos**
- Docker Desktop instalado e rodando
- Docker Compose disponível

---

## 🚀 **Como Subir o Banco de Dados**

### **1. Subir PostgreSQL + PgAdmin**

No diretório raiz do projeto (`red-alert-app`), execute:

```bash
docker-compose up -d
```

Isso vai:
- ✅ Criar container PostgreSQL na porta **5432**
- ✅ Criar container PgAdmin na porta **5050**
- ✅ Criar banco de dados `redalert`
- ✅ Configurar usuário `redalert` com senha `redalert123`

### **2. Verificar se está rodando**

```bash
docker ps
```

Você deve ver:
```
CONTAINER ID   IMAGE                    STATUS         PORTS
xxxxx          postgres:16-alpine       Up             0.0.0.0:5432->5432/tcp
xxxxx          dpage/pgadmin4:latest    Up             0.0.0.0:5050->80/tcp
```

---

## 🗄️ **Acessar o Banco**

### **Via PgAdmin (Interface Gráfica)**

1. Acesse: http://localhost:5050
2. Login:
   - **Email**: `admin@redalert.com`
   - **Password**: `admin123`

3. Adicionar servidor:
   - **Name**: Red Alert
   - **Host**: `postgres` (nome do container)
   - **Port**: `5432`
   - **Database**: `redalert`
   - **Username**: `redalert`
   - **Password**: `redalert123`

### **Via Terminal (psql)**

```bash
docker exec -it red-alert-postgres psql -U redalert -d redalert
```

---

## 📊 **Schema do Banco**

O Flyway vai criar automaticamente a tabela `categories`:

```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    email_query VARCHAR(500) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**Dados iniciais:**
- ✅ Full Cycle (`from:fullcycle.com.br is:unread`)
- ✅ FCTECH (`FCTECH is:unread`)

---

## 🔄 **Comandos Úteis**

### **Parar containers**
```bash
docker-compose down
```

### **Parar e remover volumes (limpar dados)**
```bash
docker-compose down -v
```

### **Ver logs**
```bash
docker-compose logs -f postgres
```

### **Reiniciar apenas o PostgreSQL**
```bash
docker-compose restart postgres
```

---

## 🛠️ **Troubleshooting**

### **Erro: "port 5432 already in use"**
Você já tem PostgreSQL rodando localmente. Opções:
1. Pare o PostgreSQL local: `net stop postgresql-x64-XX`
2. Mude a porta no `docker-compose.yml`: `"5433:5432"`

### **Erro: "connection refused"**
Aguarde alguns segundos. O PostgreSQL leva ~10s para inicializar.

Verifique health:
```bash
docker-compose ps
```

---

## 📝 **Próximos Passos**

Após subir o banco:

1. **Compile o backend**:
   ```bash
   cd backend
   mvn clean install
   ```

2. **Execute o backend**:
   ```bash
   mvn spring-boot:run
   ```

3. **Flyway vai executar automaticamente** a migration `V1__initial_schema.sql`

4. **Verifique no PgAdmin** se a tabela `categories` foi criada

---

## 🎯 **Testando**

Após o backend iniciar, teste a API de categorias:

```bash
# Listar categorias
curl http://localhost:8081/api/v1/categories

# Adicionar nova categoria
curl -X POST http://localhost:8081/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Minha Categoria",
    "description": "Descrição",
    "emailQuery": "from:exemplo.com is:unread"
  }'
```

---

**Banco configurado e pronto para uso!** 🎉
