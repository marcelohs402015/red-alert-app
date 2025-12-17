# Prompt Template para Criação de Novo Projeto

Use este prompt para alimentar uma IA (como Cursor) ao criar um novo projeto seguindo o mesmo padrão tecnológico e arquitetural 

---

## 📋 CONTEXTO DO PROJETO BASE

Este prompt é baseado em uma arquitetura moderna e escalável usando as melhores práticas de engenharia de software enterprise.

---

## 🏗️ ARQUITETURA E PADRÕES

### Backend - Arquitetura Hexagonal (Ports & Adapters)

**Estrutura de Pacotes:**
```
com.{projeto}.{modulo}/
├── domain/              # Camada de Domínio (regras de negócio puras)
│   ├── model/           # Entidades de domínio (Rich Domain Model)
│   ├── port/            # Interfaces (Ports) - contratos de entrada/saída
│   └── event/           # Eventos de domínio (Domain Events)
├── application/         # Camada de Aplicação (casos de uso)
│   ├── usecase/         # Casos de uso (Use Cases)
│   ├── saga/            # Orquestradores de Saga (se aplicável)
│   └── exception/       # Exceções de domínio
├── infrastructure/      # Camada de Infraestrutura (adaptadores)
│   ├── persistence/     # Adaptadores de persistência (JPA)
│   │   ├── adapter/     # Implementações de ports
│   │   ├── entity/      # Entidades JPA
│   │   ├── repository/  # Repositórios JPA
│   │   └── mapper/      # Mappers domínio ↔ entidade
│   ├── messaging/       # Adaptadores de mensageria (Kafka, RabbitMQ)
│   │   ├── adapter/     # Implementações de EventPublisherPort
│   │   ├── factory/     # Factory Pattern para escolher broker
│   │   └── config/      # Configurações de Kafka/RabbitMQ
│   ├── payment/         # Adaptadores de gateway de pagamento
│   ├── ai/              # Adaptadores de IA (OpenAI, etc.)
│   └── config/          # Configurações de infraestrutura
└── presentation/        # Camada de Apresentação (REST API)
    ├── controller/      # Controllers REST
    ├── dto/             # DTOs de request/response (Java Records)
    ├── mapper/          # Mappers domínio ↔ DTO
    └── exception/       # Exception handlers globais
```

**Princípios Arquiteturais:**
- ✅ **Hexagonal Architecture (Ports & Adapters)**: Isolamento completo do domínio
- ✅ **Rich Domain Model**: Regras de negócio no domínio, não em services
- ✅ **Dependency Inversion**: Domínio não depende de frameworks
- ✅ **SOLID Principles**: Especialmente Single Responsibility e Dependency Inversion
- ✅ **Clean Architecture**: Dependências apontam para dentro (domínio no centro)

**Padrões de Design Implementados:**
- ✅ **Saga Pattern (Orchestration)**: Para transações distribuídas
- ✅ **Factory Pattern**: Para escolha de message brokers
- ✅ **Adapter Pattern**: Para integrações externas
- ✅ **Strategy Pattern**: Para diferentes implementações de ports
- ✅ **State Machine**: Para controle de transições de estado

---

## 🚀 STACK TECNOLÓGICA

### Backend

**Core:**
- **Java 21** (Virtual Threads - Project Loom)
- **Spring Boot 3.3.6+**
- **Maven** (gerenciamento de dependências)

**Dependências Principais:**
```xml
<!-- Spring Boot Starters -->
- spring-boot-starter-web (REST API)
- spring-boot-starter-webflux (WebClient reativo)
- spring-boot-starter-validation (Bean Validation)
- spring-boot-starter-data-jpa (JPA/Hibernate)
- spring-boot-starter-actuator (Health checks, metrics)

<!-- Database -->
- postgresql (driver PostgreSQL)
- flyway-core (migrations versionadas)
- flyway-database-postgresql

<!-- Resilience -->
- resilience4j-spring-boot3 (Circuit Breaker, Retry)
- resilience4j-reactor (suporte reativo)

<!-- Messaging -->
- spring-kafka (Apache Kafka)

<!-- Documentation -->
- springdoc-openapi-starter-webmvc-ui (Swagger/OpenAPI)

<!-- Utilities -->
- lombok (redução de boilerplate)

<!-- Testing -->
- spring-boot-starter-test
- mockito-core
- mockito-junit-jupiter
```

**Configurações Importantes:**
- **Virtual Threads habilitados**: `spring.threads.virtual.enabled=true`
- **Flyway para migrations**: `src/main/resources/db/migration/`
- **Resilience4j** para Circuit Breaker e Retry
- **Kafka** para Event-Driven Architecture
- **Swagger/OpenAPI** em `/swagger-ui/index.html`

**Convenções de Código:**
- ✅ **Lombok**: `@RequiredArgsConstructor` para DI, `@Getter`, `@Builder` (evitar `@Data` em entidades JPA)
- ✅ **Java Records** para DTOs (CreateOrderRequest, OrderResponse, etc.)
- ✅ **Bean Validation** (`@NotNull`, `@NotBlank`, `@Valid`, etc.)
- ✅ **Javadoc completo** em classes públicas e métodos importantes
- ✅ **Nomenclatura**: camelCase para métodos/variáveis, PascalCase para classes
- ✅ **Imports organizados**: agrupar por: Java, Spring, Third-party, Local

### Frontend

**Core:**
- **React 18.2+**
- **TypeScript 5.3+**
- **Vite 5.0+** (build tool)
- **Node.js 18+**

**Dependências Principais:**
```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-router-dom": "^6.20.0",
    "zustand": "^4.4.7",
    "axios": "^1.6.2",
    "react-hook-form": "^7.48.2",
    "zod": "^3.22.4",
    "@hookform/resolvers": "^3.3.2",
    "clsx": "^2.0.0",
    "lucide-react": "^0.294.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.43",
    "@types/react-dom": "^18.2.17",
    "@typescript-eslint/eslint-plugin": "^6.13.1",
    "@typescript-eslint/parser": "^6.13.1",
    "@vitejs/plugin-react": "^4.2.1",
    "autoprefixer": "^10.4.16",
    "eslint": "^8.54.0",
    "eslint-plugin-react-hooks": "^4.6.0",
    "eslint-plugin-react-refresh": "^0.4.4",
    "postcss": "^8.4.32",
    "prettier": "^3.1.0",
    "tailwindcss": "^3.3.6",
    "typescript": "^5.3.2",
    "vite": "^5.0.5"
  }
}
```

**Estrutura de Pastas Frontend:**
```
src/
├── components/          # Componentes reutilizáveis
│   ├── ui/             # Componentes de UI base (Button, Input, Card, etc.)
│   └── Layout.tsx       # Layout principal
├── pages/               # Páginas/rotas da aplicação
├── services/            # Serviços de API (Axios)
├── store/               # Zustand stores (state management)
├── types/               # TypeScript types/interfaces
├── utils/               # Funções utilitárias
├── lib/                 # Configurações de bibliotecas (axios.ts)
├── App.tsx              # Componente raiz com rotas
├── main.tsx             # Entry point
└── index.css            # Estilos globais (Tailwind)
```

**Convenções de Código Frontend:**
- ✅ **Early returns** sempre que possível
- ✅ **TailwindCSS** para estilização (evitar CSS customizado)
- ✅ **Nomenclatura**: camelCase para funções/variáveis, PascalCase para componentes
- ✅ **Event handlers**: prefixo `handle` (ex: `handleClick`, `handleSubmit`)
- ✅ **Consts ao invés de functions**: `const toggle = () => {}`
- ✅ **TypeScript strict**: tipos explícitos, evitar `any`
- ✅ **React Hook Form + Zod** para validação de formulários
- ✅ **Zustand** para state management global
- ✅ **Axios** configurado em `lib/axios.ts` com interceptors

**Configurações:**
- **Vite**: Porta 5173, proxy para `/api` → `http://localhost:8081`
- **TailwindCSS**: Configurado com cores customizadas (opcional)
- **TypeScript**: Strict mode habilitado
- **ESLint + Prettier**: Para qualidade de código

---

## 📝 PADRÕES DE CÓDIGO E BOAS PRÁTICAS

### Backend

**1. Entidades de Domínio (Domain Model):**
```java
// ✅ CORRETO: Rich Domain Model sem anotações JPA
@Getter
@Builder
public class Order {
    private UUID id;
    private OrderNumber orderNumber;
    private OrderStatus status;
    // ... campos
    
    // Métodos de negócio encapsulados
    public Money calculateTotal() { ... }
    public void updateStatus(OrderStatus newStatus) { ... }
}

// ❌ ERRADO: Anemic Domain Model com JPA no domínio
@Entity
public class Order {
    // Apenas getters/setters, sem lógica de negócio
}
```

**2. Ports (Interfaces):**
```java
// Port define contrato que o domínio precisa
public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    Optional<Order> findByOrderNumber(OrderNumber orderNumber);
}
```

**3. Adapters (Implementações):**
```java
// Adapter implementa port usando tecnologia específica (JPA)
@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {
    private final JpaOrderRepository jpaRepository;
    private final OrderPersistenceMapper mapper;
    
    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
}
```

**4. Use Cases:**
```java
@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {
    private final OrderRepositoryPort orderRepository;
    private final EventPublisherPort eventPublisher;
    
    public Order execute(CreateOrderCommand command) {
        // Validações
        // Lógica de negócio
        // Persistência
        // Publicação de eventos
    }
}
```

**5. DTOs (Java Records):**
```java
// Request DTO
public record CreateOrderRequest(
    @NotBlank String customerName,
    @Valid @NotNull List<OrderItemRequest> items,
    @Valid @NotNull AddressRequest address
) {}

// Response DTO
public record OrderResponse(
    UUID id,
    String orderNumber,
    String status,
    BigDecimal total,
    LocalDateTime createdAt
) {}
```

**6. Controllers:**
```java
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    
    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(
        @Valid @RequestBody CreateOrderRequest request
    ) {
        // Mapear request para command
        // Executar use case
        // Mapear resultado para response
    }
}
```

**7. Exception Handling:**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(...));
    }
}
```

### Frontend

**1. Componentes:**
```typescript
// ✅ CORRETO: Componente funcional com TypeScript
interface ButtonProps {
  onClick: () => void;
  children: React.ReactNode;
  variant?: 'primary' | 'secondary';
}

const Button: React.FC<ButtonProps> = ({ onClick, children, variant = 'primary' }) => {
  return (
    <button
      onClick={onClick}
      className={clsx('px-4 py-2 rounded', {
        'bg-blue-500': variant === 'primary',
        'bg-gray-500': variant === 'secondary',
      })}
    >
      {children}
    </button>
  );
};
```

**2. Services (API):**
```typescript
// lib/axios.ts - Configuração do Axios
import axios from 'axios';

export const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// services/orderService.ts
import { api } from '@/lib/axios';

export const orderService = {
  create: async (data: CreateOrderRequest): Promise<OrderResponse> => {
    const response = await api.post('/v1/orders', data);
    return response.data;
  },
  findAll: async (): Promise<OrderResponse[]> => {
    const response = await api.get('/v1/orders');
    return response.data;
  },
};
```

**3. State Management (Zustand):**
```typescript
// store/orderStore.ts
import { create } from 'zustand';

interface OrderState {
  orders: Order[];
  isLoading: boolean;
  fetchOrders: () => Promise<void>;
  createOrder: (data: CreateOrderRequest) => Promise<void>;
}

export const useOrderStore = create<OrderState>((set) => ({
  orders: [],
  isLoading: false,
  fetchOrders: async () => {
    set({ isLoading: true });
    const orders = await orderService.findAll();
    set({ orders, isLoading: false });
  },
  createOrder: async (data) => {
    const order = await orderService.create(data);
    set((state) => ({ orders: [...state.orders, order] }));
  },
}));
```

**4. Formulários (React Hook Form + Zod):**
```typescript
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const schema = z.object({
  customerName: z.string().min(1, 'Nome é obrigatório'),
  items: z.array(z.object({ ... })).min(1, 'Adicione pelo menos um item'),
});

const CreateOrderPage = () => {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  });
  
  const onSubmit = async (data: FormData) => {
    await orderService.create(data);
  };
  
  return <form onSubmit={handleSubmit(onSubmit)}>...</form>;
};
```

---

## 🔧 CONFIGURAÇÕES E SETUP

### Backend - application.yml

**Principais Configurações:**
```yaml
spring:
  threads:
    virtual:
      enabled: true  # Virtual Threads (Java 21)
  
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/dbname}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:postgres}
  
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway gerencia schema
    show-sql: true
  
  flyway:
    enabled: true
    locations: classpath:db/migration

resilience4j:
  circuitbreaker:
    instances:
      serviceName:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s

server:
  port: 8081
```

### Frontend - vite.config.ts

```typescript
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
});
```

### Frontend - tailwind.config.js

```javascript
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: { /* cores customizadas */ },
      },
    },
  },
  plugins: [],
};
```

---

## 🧪 TESTES

### Backend

**Estrutura de Testes:**
- ✅ **Testes Unitários**: Use Cases, Domain Models (sem dependências externas)
- ✅ **Testes de Integração**: Controllers, Adapters (com banco de dados)
- ✅ **Mockito** para mocks
- ✅ **JUnit 5** como framework de testes

**Exemplo:**
```java
@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {
    @Mock
    private OrderRepositoryPort orderRepository;
    
    @InjectMocks
    private CreateOrderUseCase useCase;
    
    @Test
    void shouldCreateOrderSuccessfully() {
        // Arrange
        // Act
        // Assert
    }
}
```

### Frontend

**Estrutura de Testes:**
- ✅ **Vitest** ou **Jest** para testes unitários
- ✅ **React Testing Library** para testes de componentes
- ✅ Testes de integração para services

---

## 📦 DEPLOY E INFRAESTRUTURA

**Docker:**
- Docker Compose para PostgreSQL local
- Dockerfile para build da aplicação

**Variáveis de Ambiente:**
- Backend: `application.yml` com valores padrão + variáveis de ambiente
- Frontend: `.env` para configurações (opcional)

**Scripts:**
- Backend: `mvn spring-boot:run`
- Frontend: `npm run dev`

---

## ✅ CHECKLIST PARA NOVO PROJETO

Ao criar um novo projeto, garantir:

### Backend
- [ ] Estrutura de pacotes seguindo Hexagonal Architecture
- [ ] Java 21 com Virtual Threads habilitado
- [ ] Spring Boot 3.3.6+
- [ ] Flyway configurado para migrations
- [ ] Resilience4j para Circuit Breaker/Retry
- [ ] Swagger/OpenAPI configurado
- [ ] Lombok configurado corretamente
- [ ] DTOs como Java Records
- [ ] Global Exception Handler
- [ ] Testes unitários e de integração

### Frontend
- [ ] React 18+ com TypeScript
- [ ] Vite configurado
- [ ] TailwindCSS configurado
- [ ] Zustand para state management
- [ ] React Hook Form + Zod para formulários
- [ ] Axios configurado com interceptors
- [ ] Estrutura de pastas organizada
- [ ] Componentes reutilizáveis em `components/ui/`

### Geral
- [ ] README.md com instruções de setup
- [ ] .gitignore configurado
- [ ] Docker Compose para dependências (PostgreSQL, Kafka, etc.)
- [ ] Variáveis de ambiente documentadas

---

## 🎯 INSTRUÇÕES PARA A IA

Ao usar este prompt em um novo projeto, forneça:

1. **Nome do projeto e domínio**
2. **Funcionalidades principais**
3. **Entidades de domínio principais**
4. **Integrações externas necessárias** (se houver)
5. **Requisitos específicos** (ex: autenticação, autorização, etc.)

A IA deve seguir **estritamente**:
- ✅ Esta arquitetura Hexagonal
- ✅ Esta stack tecnológica
- ✅ Estes padrões de código
- ✅ Estas convenções de nomenclatura
- ✅ Esta estrutura de pastas

**NÃO aceitar:**
- ❌ Anemic Domain Model
- ❌ Dependências do domínio em frameworks
- ❌ Código sem Javadoc em classes públicas
- ❌ DTOs como classes ao invés de Records
- ❌ Componentes React sem TypeScript
- ❌ CSS customizado ao invés de TailwindCSS

---

## 📚 REFERÊNCIAS E DOCUMENTAÇÃO

**Arquitetura:**
- Hexagonal Architecture (Alistair Cockburn)
- Clean Architecture (Robert C. Martin)
- Domain-Driven Design (Eric Evans)

**Padrões:**
- Saga Pattern (Microservices Patterns)
- Circuit Breaker Pattern (Resilience4j)
- Factory Pattern (Design Patterns - Gang of Four)

**Tecnologias:**
- Spring Boot 3.3+ Documentation
- Java 21 Virtual Threads
- React 18 Documentation
- Vite Documentation
- TailwindCSS Documentation

---

**💡 Este prompt garante que novos projetos sigam o mesmo padrão de qualidade, arquitetura e stack tecnológica do projeto base.**

