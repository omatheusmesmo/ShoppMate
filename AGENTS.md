# AGENTS.md - ShoppMate Monorepo

Guidelines for AI coding agents working on the ShoppMate full-stack shopping list management system.

## Project Overview

ShoppMate is a full-stack application with:
- **Backend**: Spring Boot 3.3.5 REST API (Java 17, PostgreSQL, JWT auth, Flyway)
- **Frontend**: Angular 19 SPA (Material Design 3, Signals, standalone components)

---

## Backend (Spring Boot)

### Build/Lint/Test Commands

```bash
cd backend

# Build
./mvnw clean install          # Clean and build project
./mvnw compile                # Compile source code
./mvnw package                # Package into JAR

# Run
./mvnw spring-boot:run        # Run the application

# Test
./mvnw test                                                      # Run all tests
./mvnw test -Dtest=ItemServiceTest                               # Run single test class
./mvnw test -Dtest=ItemServiceTest#addItem_ValidItem_ReturnsSavedItem  # Run single test method
./mvnw test -Dtest="*ServiceTest"                                # Run tests matching pattern
./mvnw test -Dtest="ItemControllerTest,ItemServiceTest"          # Run multiple test classes
./mvnw verify                    # Run tests + validate code formatting

# Formatting
./mvnw formatter:format          # Format code
./mvnw formatter:validate        # Validate formatting

# Coverage
./mvnw test jacoco:report        # Run tests + generate coverage report
```

### Architecture

```
backend/src/main/java/com/omatheusmesmo/shoppmate/
├── auth/           # Authentication (configs, controller, service, dtos)
├── category/       # Category domain
├── item/           # Item domain
├── list/           # Shopping list domain (entity, dtos, controller, service)
├── unit/           # Unit of measurement domain
├── user/           # User domain
├── shared/         # Shared (domain base classes, AuditService, SnowflakeIdGenerator)
└── utils/exception/ # GlobalExceptionHandler
```

Each domain follows: `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `mapper/`

### Code Style

**Imports**: Package `com.omatheusmesmo.shoppmate.<domain>`. Order: java.*, jakarta.*, org.springframework.*, third-party, project. No wildcards.

**Formatting**: 4 spaces indent, K&R braces. Uses `formatter-maven-plugin`.

**Types**: Use `var` when type is obvious. Use `record` for DTOs.

**Naming**:
- Classes: `PascalCase` (e.g., `ItemService`)
- Methods: `camelCase` (e.g., `addItem()`)
- Constants: `UPPER_SNAKE_CASE`
- DTOs: `*RequestDTO`, `*ResponseDTO`
- Entities: No suffix (e.g., `Item`, `User`)

**Error Handling**: Use `NoSuchElementException` for not found, `IllegalArgumentException` for validation. Return proper HTTP codes (200, 201, 204, 400, 404).

**Validation**: Jakarta Bean Validation (`@NotBlank`, `@NotNull`) on DTOs, not entities.

**Lombok**: Use `@Getter`, `@Setter`, `@NoArgsConstructor`. Avoid `@Data`.

**Entities**: Extend `DomainEntity` (for entities with `name`) or `BaseAuditableEntity`. Use `@OneToOne(fetch = FetchType.LAZY)` by default.

**Controllers**: Use `@RestController`, `@Operation(summary)`, `ResponseEntity<T>`, `ServletUriComponentsBuilder` for Location header.

**Services**: Constructor injection, call `auditService.setAuditData(entity, isNew)` before save.

### Testing

- **Unit**: JUnit 5 + Mockito. Use `@WebMvcTest(Controller.class)`, `@AutoConfigureMockMvc(addFilters = false)`, `@MockBean`, `@InjectMocks`.
- **Integration**: Extend `AbstractIntegrationTest` (uses Testcontainers with PostgreSQL). Use REST Assured.
- **Naming**: `methodName_Scenario_ExpectedBehavior`
- **Structure**: Arrange, Act, Assert (with comments)

### Database

- Flyway migrations in `src/main/resources/db/migration/`
- Naming: `V{number}__DESCRIPTION.sql`
- Use `spring.jpa.hibernate.ddl-auto=none`

---

## Frontend (Angular)

### Build/Lint/Test Commands

```bash
cd frontend

# Development
npm start                       # Start dev server (ng serve)
npm run build                   # Production build
npm run build:staging           # Staging build
npm run watch                   # Watch mode build

# Linting
npm run lint                    # ESLint via Angular CLI
npm run lint:custom             # Custom signals/OnPush check
npm run prettier:check          # Check formatting
npm run prettier                # Auto-format code

# Testing
npm test                        # Run all tests (watch mode)
npm run test:ci                 # CI tests (headless, no watch)

# Running a single test
npx ng test --include='src/app/shared/services/item.service.spec.ts'
npx ng test --include='**/auth.interceptor.spec.ts'
```

### Architecture

```
frontend/src/app/
├── auth/           # Authentication (guards, login, signup)
├── layout/         # Layout components
├── list/           # List feature module
└── shared/         # Shared (components, interceptors, interfaces, services)
```

### Code Style

**Imports Order**: 1) Angular core/common, 2) Angular Material, 3) Third-party (rxjs), 4) Application imports.

**Components**: Standalone with `ChangeDetectionStrategy.OnPush` (REQUIRED). Selectors: `kebab-case` with `app` prefix.

**Signals**: REQUIRED for all UI state (enforced by custom lint). Use `readonly property = signal<Type>(value)`.

**Dependency Injection**: Use `inject()` function, not constructor injection.

**Services**: Extend `BaseService` for HTTP. Use `catchError` in `pipe()`.

**Interfaces**: Use `RequestDTO`/`ResponseDTO` suffix for API DTOs.

**Error Handling**: Use `FeedbackService` for user messages (`success()`, `error()`).

**Forms**: Use typed forms with `nonNullable`: `this.fb.nonNullable.group({...})`.

**Routing**: Use lazy loading with `loadComponent`.

**Formatting**: 2 spaces, single quotes, semicolons required. Run `npm run prettier` before committing.

### Testing

Use Jasmine/Karma with TestBed. Mock services from `shared/mocks/mock-services.ts`:

```typescript
beforeEach(() => {
  authServiceSpy = jasmine.createSpyObj("AuthService", ["getToken", "logout"]);
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(withInterceptors([authInterceptor])),
      provideHttpClientTesting(),
      { provide: AuthService, useValue: authServiceSpy }
    ],
  });
});
afterEach(() => httpTestingController.verify());
```

---

## Key Patterns Summary

### Backend
1. Domain-driven architecture with layered structure
2. Constructor injection in services
3. `auditService.setAuditData()` before entity save
4. Jakarta Validation on DTOs, business validation in entities
5. Global exception handling with proper HTTP codes
6. Flyway for database migrations

### Frontend
1. **ChangeDetectionStrategy.OnPush** - Required on all components
2. **Signals** - Required for all UI state properties
3. **Standalone components** - No NgModules
4. **inject()** - Preferred over constructor injection
5. **RxJS operators** - Use `pipe()` with `catchError`, `finalize`
6. **Lazy loading** - Use `loadComponent` for feature routes
7. **Typed forms** - Use `nonNullable` for forms

---

## Environment

- **Backend**: Java 17, PostgreSQL, JWT with RSA keys (in `certs/`)
- **Frontend**: Node.js 18+, Angular 19, Material 3
- **Docker**: Run full stack with `docker compose up --build` from root
