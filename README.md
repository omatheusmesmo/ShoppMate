# 🛒 ShoppMate

ShoppMate is a full-stack, smart shopping list management solution. It empowers users to organize their purchases, manage custom categories and units, and collaborate on shared lists.

This monorepo contains both the **Spring Boot Backend** and the **Angular Frontend**.

## 🚀 What can ShoppMate do? (Features)

Based on our core API capabilities, ShoppMate offers a comprehensive suite of features:

### 🔐 Security & Identity
- **User Registration & Login**: Secure JWT-based authentication.
- **Profile Management**: View and manage user details.

### 📋 Shopping Lists
- **Multi-list Management**: Create, view, update, and delete multiple shopping lists.
- **Item Organization**: Add items to specific lists with quantity tracking and "purchased" status.
- **Collaborative Lists**: Share lists with other users by granting specific permissions (`READ`, `WRITE`, etc.).

### 📦 Catalog Management
- **Custom Categories**: Organize your items into categories (e.g., Fruits, Cleaning, Dairy).
- **Measurement Units**: Define units like `kg`, `unit`, `liters`, or any custom measurement.
- **Item Database**: Create a reusable catalog of items to quickly add to your lists.

## 🏗️ Project Structure

- **`/backend`**: Spring Boot 3 REST API (Java 17, PostgreSQL, Flyway).
- **`/frontend`**: Angular 19+ Web Application (Material Design 3, Signals).
- **`/backend/bruno`**: API Testing collections for the [Bruno](https://www.usebruno.com/) client.

## 🛠️ Getting Started

### 1. Environment Setup

Before running the project, you need to configure the environment variables.

1.  From the project root directory:
2.  Copy `backend/.env.example` to a new file named `.env` in the **root** directory:
    ```bash
    cp backend/.env.example .env
    ```
3.  (Optional) Edit `.env` to customize your database credentials or JWT settings.

### 2. Running with Docker (Recommended)

The easiest way to run the entire stack (Backend, Frontend, and Database) is using Docker Compose from the root directory:

```bash
docker compose up --build
```

Access the application at:
- **Frontend**: [http://localhost](http://localhost)
- **Backend API**: [http://localhost:8080](http://localhost:8080)

### 3. Running Manually (Development)

If you prefer to run the components individually for development:

#### Database
You still need a PostgreSQL instance. You can run just the database via Docker:
```bash
docker compose up postgres -d
```

#### Backend
1. Navigate to `backend/`.
2. Ensure you have Java 17 installed.
3. Run with:
   ```bash
   ./mvnw spring-boot:run
   ```

#### Frontend
1. Navigate to `frontend/`.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run with:
   ```bash
   npm start
   ```
4. Access at [http://localhost:4200](http://localhost:4200).

## 🤝 Contributing

Contributions are welcome! Please check our [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on our development workflow and commit standards.

## 📄 License

This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International Public License. See [LICENSE.md](LICENSE.md) for details.
