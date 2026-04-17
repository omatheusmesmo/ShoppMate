# AGENTS.md - ShoppMate Backend

Guidelines for AI coding agents working on the ShoppMate Spring Boot REST API.

## Project Overview

ShoppMate is a Spring Boot 3.3.5 REST API for managing shopping lists. It uses Java 17, PostgreSQL, JWT authentication (RSA keys), and follows a domain-driven architecture.

## Build, Test, and Lint Commands

### Build
```bash
mvn clean install          # Clean and build project
mvn compile                # Compile source code
mvn package                # Package into JAR
```

### Run Application
```bash
mvn spring-boot:run        # Run the application
java -jar target/shoppmate-0.0.1-SNAPSHOT.jar
```

### Test Commands
```bash
mvn test                   # Run all tests
mvn test -Dtest=ItemServiceTest                    # Run single test class
mvn test -Dtest=ItemServiceTest#addItem_ValidItem_ReturnsSavedItem  # Run single test method
mvn test -Dtest="*ServiceTest"                     # Run tests matching pattern
mvn test -Dtest="ItemControllerTest,ItemServiceTest"  # Run multiple test classes
mvn verify                 # Run tests + validate code formatting
```

### Code Formatting
```bash
mvn formatter:format       # Format code
mvn formatter:validate     # Validate formatting (runs in verify phase)
```

### Coverage
```bash
mvn test jacoco:report     # Run tests + generate coverage report
```

## Project Architecture

```
src/main/java/com/omatheusmesmo/shoppmate/
├── auth/                  # Authentication domain
│   ├── configs/           # Security config, JWT filters, RSA keys
│   ├── controller/        # Auth endpoints
│   ├── service/           # JWT, UserDetailsService
│   └── dtos/              # Login, Register DTOs
├── category/              # Category domain
├── item/                  # Item domain
├── list/                  # Shopping list domain
│   ├── entity/            # ShoppingList, ListItem, ListPermission
│   └── dtos/listpermission/
├── unit/                  # Unit of measurement domain
├── user/                  # User domain
├── shared/                # Shared utilities
│   ├── domain/            # Base entities (DomainEntity, AuditableEntity)
│   ├── service/           # AuditService
│   └── utils/             # SnowflakeIdGenerator
└── utils/exception/       # GlobalExceptionHandler
```

Each domain follows: `controller/`, `service/`, `repository/`, `entity/`, `dto/`, `mapper/`

## Code Style Guidelines

### Imports
- Package: `com.omatheusmesmo.shoppmate.<domain>`
- Order: java.*, jakarta.*, org.springframework.*, third-party, project packages
- Use specific imports (no wildcards)

### Formatting
- Uses `formatter-maven-plugin` (config in pom.xml)
- Indent: 4 spaces (no tabs)
- Max line length: default
- Braces: K&R style

### Types
- Java 17 features: `var` for local variables when type is obvious
- Use `record` for DTOs: `public record ItemRequestDTO(@NotBlank String name, ...) {}`
- Prefer immutable objects where possible

### Naming Conventions
- **Classes**: PascalCase: `ItemService`, `ItemController`
- **Methods**: camelCase: `addItem()`, `findById()`
- **Constants**: UPPER_SNAKE_CASE: `MAX_RETRY_COUNT`
- **Packages**: lowercase: `com.omatheusmesmo.shoppmate.item`
- **DTO suffixes**: `RequestDTO`, `ResponseDTO`
- **Entity suffixes**: No suffix (e.g., `Item`, `User`)
- **Mapper suffix**: `Mapper` (e.g., `ItemMapper`)
- **Repository suffix**: `Repository` (e.g., `ItemRepository`)
- **Service suffix**: `Service` (e.g., `ItemService`)

### Error Handling
- Use `NoSuchElementException` for not found: `throw new NoSuchElementException("Item not found with id: " + id)`
- Use `IllegalArgumentException` for validation: `throw new IllegalArgumentException("Name cannot be null!")`
- Global exception handler at `GlobalExceptionHandler.java`
- Return proper HTTP status codes:
  - 200 OK for successful GET/PUT
  - 201 CREATED for POST
  - 204 NO CONTENT for DELETE
  - 400 BAD REQUEST for validation errors
  - 404 NOT FOUND for missing resources

### Validation
- Use Jakarta Bean Validation: `@NotBlank`, `@NotNull`
- Annotate DTOs, not entities: `@Valid @RequestBody ItemRequestDTO dto`
- Validation in domain entities for business rules: `item.checkName()`

### Lombok Usage
- Use `@Getter`, `@Setter` for entities
- Use `@NoArgsConstructor` for JPA entities
- Use `@AllArgsConstructor` for DTOs if needed
- Avoid `@Data` - prefer specific annotations

### Entity Design
- Extend `DomainEntity` for entities with `name` field
- Extend `BaseAuditableEntity` for entities without `name`
- Use Snowflake ID generation (configured in base classes)
- Audit fields: `createdAt`, `updatedAt`, `deleted` (soft delete)
- Relationships: Use `@OneToOne(fetch = FetchType.LAZY)` by default

### Controller Patterns
- Use `@RestController` and `@RequestMapping`
- Add Swagger docs: `@Operation(summary = "...")`
- Return `ResponseEntity<T>` for flexibility
- Use `ServletUriComponentsBuilder` for Location header on POST

### Service Patterns
- Annotate with `@Service`
- Inject repositories via constructor injection
- Call `auditService.setAuditData(entity, isNew)` before save
- Validate business rules in service, not controller

### Testing Patterns
- **Unit tests**: Use JUnit 5 + Mockito
  - Use domain-specific factories (e.g., `ItemTestFactory`) with **Datafaker** to generate dynamic test data.
  - `@WebMvcTest(Controller.class)` for controller tests
  - `@AutoConfigureMockMvc(addFilters = false)` to disable security
  - `@MockBean` for dependencies
  - `@InjectMocks` for service under test
- **Integration tests**: Extend `AbstractIntegrationTest`
  - Uses Testcontainers with PostgreSQL
  - Use REST Assured for API testing
  - Use `TestUserFactory` for JWT token creation
- Test naming: `methodName_Scenario_ExpectedBehavior`
- Structure tests: Arrange, Act, Assert (with comments)

### Database
- Flyway migrations in `src/main/resources/db/migration/`
- Naming: `V{number}__DESCRIPTION.sql` (e.g., `V4__CREATE_TABLE_ITEMS.sql`)
- Use `spring.jpa.hibernate.ddl-auto=none` (migrations only)

### Security
- JWT authentication with RSA keys
- Keys stored in `certs/` directory (git-ignored)
- Use `@WithMockUser` in tests when security is enabled

## Environment Variables
- `DB_SHOPP_MATE`: Database name (default: SHOPPMATE)
- `DB_USER`: Database user (default: user)
- `DB_PASS`: Database password (default: password)
- `JWT_PRIVATE_KEY_PATH`: Path to private key
- `JWT_PUBLIC_KEY_PATH`: Path to public key
- `JWT_TOKEN_EXPIRATION`: Token expiry in ms (default: 3600000)
