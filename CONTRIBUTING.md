# Contributing to ShoppMate

First off, thank you for considering contributing to ShoppMate! It's people like you that make the open-source community such an amazing place to learn, inspire, and create.

## Code of Conduct
By participating in this project, you agree to abide by our Code of Conduct (standard Contributor Covenant).

## How Can I Contribute?

### Reporting Bugs
- Use the GitHub issue tracker.
- Describe the bug with enough detail for others to reproduce it.
- Include your operating system, Java version, and any relevant logs.

### Suggesting Enhancements
- Check if the enhancement has already been suggested.
- Open a new issue with a clear title and description of the benefits.

### Pull Requests
1. **Fork the repository** and create your branch from `main`.
2. **Install dependencies** using Maven: `./mvnw install`.
3. **Follow the coding style**:
   - Match the existing indentation (4 spaces for Java).
   - Use meaningful variable and method names.
   - Adhere to SOLID principles and Clean Code practices.
4. **Pass code quality checks**:
   - **Backend**: Run `mvn checkstyle:check` to verify Java code style compliance.
   - **Frontend**: Run `npm run lint && npm run prettier:check` to verify TypeScript/Angular code quality.
   - All PRs must pass these checks in the CI pipeline before merging.
5. **Write tests**: If you're adding a feature or fixing a bug, please include tests.
6. **Update documentation**: If you change how the API works, update the relevant README or Bruno collection.
7. **Self-review**: Read through your changes before submitting.
8. **Submit the PR**: Link it to the relevant issue (e.g., `Closes #15`).

## Development Workflow

### Branch Naming Convention
- `feature/description-of-feature`
- `fix/description-of-bug`
- `docs/description-of-change`

### Commit Message Guidelines
We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:
- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, etc.)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests or correcting existing tests
- **chore**: Changes to the build process or auxiliary tools and libraries

**Example:**
`feat(auth): add JWT expiration validation`
`fix(list): resolve N+1 query in shopping list retrieval`

## Technical Stack
- **Backend**: Java 17+, Spring Boot 3+
- **Database**: PostgreSQL (Migrations via Flyway)
- **Security**: Spring Security + JWT
- **API Testing**: Bruno (collections found in `/bruno`)

## Questions?
Feel free to open a discussion or an issue if you have any questions about the setup or the roadmap.
