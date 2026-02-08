# ShoppMate - Your Smart Shopping List Manager

ShoppMate is a RESTful API designed to help users manage their shopping lists efficiently. It allows users to create, update, and delete shopping lists, add items to lists, manage user permissions for lists, manage categories and units, and more. The API also includes user authentication and authorization features.

## Table of Contents

* [Features](#features)
* [Technologies Used](#technologies-used)
* [Getting Started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Installation](#installation)
    * [Environment Variables](#environment-variables)
    * [Running the Application](#running-the-application)
* [API Endpoints](#api-endpoints)
    * [Authentication (/auth)](#authentication-auth)
    * [Shopping Lists (/shopplists)](#shopping-lists-shopplists)
    * [Shopping List Items (/shopplist/{shopplistId}/items)](#shopping-list-items-shopplistshopplistiditems)
    * [Shopping List User Permissions (/shopplist/{shopplistId}/permissions)](#shopping-list-user-permissions-shopplistshopplistidpermissions)
    * [Categories (/category)](#categories-category)
    * [Units (/unit)](#units-unit)
    * [Items (/item)](#items-item)
* [Testing](#testing)
* [Project Architecture](#architecture)
* [Contributing](#contributing)
* [License](#license)
* [Contact](#contact)

## Features

* **Shopping List Management:**
    * Create, read, update, and delete shopping lists.
* **Item Management:**
    * Add, read, update, and delete items within a shopping list.
* **User Permission Management:**
    * Grant, read, update, and revoke user permissions for specific shopping lists.
* **Category Management:**
    * Create, read, update, and delete item categories.
* **Unit Management:**
    * Create, read, update, and delete units of measurement.
* **RESTful API:**
    * Well-defined RESTful endpoints for easy integration.
* **Error Handling:**
    * Centralized error handling for common exceptions.
* **Database Integration:**
    * Uses PostgreSQL for persistent data storage.
* **Auditing:**
    * Tracks creation and modification timestamps for entities.
* **Code Quality:**
    * Includes unit tests for core services.
* **Authentication and Authorization:**
    * Secure user authentication and authorization using JWT (JSON Web Tokens).
* **API Documentation:**
    * Integrated with SpringDoc and Swagger for clear and interactive API documentation.
* **Database Versioning:**
    * Uses Flyway for database migrations, ensuring consistency and ease of updates.

## Technologies Used

* Java: Core programming language.
* Spring Boot: Framework for building the application.
* Spring Web: For creating RESTful web services.
* Spring Data JPA: For database access and management.
* PostgreSQL: Relational database for data storage.
* Lombok: For reducing boilerplate code.
* JUnit 5: For unit testing.
* Mockito: For mocking dependencies in tests.
* Maven: For project management and dependency resolution.
* Swagger (SpringDoc): For API documentation.
* Spring Security: For authentication and authorization.
* OAuth2: For authentication and authorization.
* Nimbus-JOSE-JWT: For JWT handling.
* Flyway: For database migration.
* BCrypt: For password encoding.

## Getting Started

### Prerequisites

* **Java Development Kit (JDK)**: Version 17 (required)
  * This project uses Java 17 and is configured to automatically switch to it via SDKMAN
  * If you use SDKMAN, the correct Java version will be activated automatically when you enter the project directory
* **Maven**: For building and managing the project
* **PostgreSQL**: A running PostgreSQL database instance
* **Git**: For version control
* **SDKMAN** (recommended): For automatic Java version management

### Java Version Management

This project is configured to use **Java 17** automatically via SDKMAN.

**If you use SDKMAN:**
- The `.sdkmanrc` file ensures the correct Java version is used
- Enable auto-environment: `sdk config sdkman_auto_env true` (already configured)
- Java 17 will be activated automatically when you enter the project directory
- Manual activation: `sdk env`

**If you don't use SDKMAN:**
- Make sure you have JDK 17 installed and configured
- Set `JAVA_HOME` to point to your JDK 17 installation

**Verify your Java version:**
```bash
java -version  # Should show Java 17
```

### Installation

1.  Clone the repository:

    ```bash
    git clone https://github.com/omatheusmesmo/ShoppMateAPI.git
    cd ShoppMateAPI
    ```

    **Note:** If you use SDKMAN with auto-environment enabled, Java 17 will be automatically activated.

2.  Configure the Database:
    * **Create a database in your PostgreSQL instance.**
    * Update the `application.properties` file with your database credentials (see [Environment Variables](#environment-variables)).

3.  Build the project:

    ```bash
    mvn clean install
    ```

### Environment Variables

You need to set the following environment variables in your `application.properties` file (or as system environment variables):

```properties
# Database Configurations
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
spring.datasource.username=your_database_username
spring.datasource.password=your_database_password

# JWT Secret Key
secret.key.jwt=your_secret_key_here

# Flyway
spring.flyway.enabled=true
```

* `spring.datasource.url`: The JDBC URL for your PostgreSQL database.
* `spring.datasource.username`: Your PostgreSQL database username.
* `spring.datasource.password`: Your PostgreSQL database password.
* `secret.key.jwt`: A secret key used for JWT token generation.
* `spring.flyway.enabled`: Enable or disable flyway.

### Running the Application

1.  Navigate to the project root directory.
2.  Run the application:

    ```bash
    mvn spring-boot:run
    ```

    Or, if you prefer to run the JAR file directly:

    ```bash
    java -jar target/shoppmate-0.0.1-SNAPSHOT.jar
    ```

The application will start on `http://localhost:8080`.

## Bruno Collection

A [Bruno](https://www.usebruno.com/) collection is available in the `bruno/` directory to help you test the API endpoints easily.

1.  Open the Bruno app.
2.  Click **Open Collection** and select the `bruno` folder in this project.
3.  Select the **Development** environment.
4.  Start with `Auth -> Register` and then `Auth -> Login` to automatically set the `token` variable for subsequent requests.

For more details, see [bruno/README.md](bruno/README.md).

## API Endpoints

### Authentication (/auth)

* **POST /auth/sign:** Register a new user.

  ```json
  {
    "email": "user@example.com",
    "fullName": "User Name",
    "password": "password"
  }
  ```

* **POST /auth/login:** User login. Returns a JWT token.

  ```json
  {
    "email": "user@example.com",
    "password": "password"
  }
  ```

### Shopping Lists (/lists)

* **GET /lists:** Get all shopping lists.
* **POST /lists:** Create a new shopping list.

  ```json
  {
    "name": "My Shopping List",
    "idUser": 1
  }
  ```

* **GET /lists/{id}:** Get a shopping list by ID.
* **DELETE /lists/{id}:** Delete a shopping list by ID.
* **PUT /lists/{id}:** Update a shopping list (name only).

  ```json
  {
    "name": "Updated Shopping List"
  }
  ```

### Shopping List Items (/lists/{listId}/items)

* **GET /lists/{listId}/items:** Get all items for a specific shopping list.
* **GET /lists/{listId}/items/{id}:** Get a specific item within a shopping list.
* **POST /lists/{listId}/items:** Add an item to a shopping list.

  ```json
  {
    "listId": 1,
    "itemId": 1,
    "quantity": 2
  }
  ```

* **DELETE /lists/{listId}/items/{id}:** Delete an item from a shopping list.
* **PUT /lists/{listId}/items/{id}:** Update an item's quantity or purchased status.

  ```json
  {
    "listId": 1,
    "itemId": 1,
    "quantity": 3,
    "purchased": true
  }
  ```

### Shopping List User Permissions (/lists/{listId}/permissions)

* **GET /lists/{listId}/permissions:** Get all permissions for a specific shopping list.
* **POST /lists/{listId}/permissions:** Grant a user permission to a shopping list.

  ```json
  {
    "idList": 1,
    "idUser": 2,
    "permission": "WRITE"
  }
  ```

* **DELETE /lists/{listId}/permissions/{id}:** Revoke a user permission.
* **PUT /lists/{listId}/permissions/{id}:** Update a user permission.

  ```json
  {
    "permission": "READ"
  }
  ```

### Categories (/category)

* **GET /category:** Get all categories.
* **POST /category:** Add a new category.

  ```json
  {
    "name": "Category Name"
  }
  ```

* **DELETE /category/{id}:** Delete a category by ID.
* **PUT /category/{id}:** Update a category name.

  ```json
  {
    "name": "Updated Category Name"
  }
  ```

### Units (/unit)

* **GET /unit:** Get all units.
* **POST /unit:** Add a new unit.

  ```json
  {
    "name": "Unit Name",
    "abbreviation": "un"
  }
  ```

* **DELETE /unit/{id}:** Delete a unit by ID.
* **PUT /unit:** Update a unit (requires ID in body).

  ```json
  {
    "id": 1,
    "name": "Unit Name",
    "abbreviation": "un"
  }
  ```

### Items (/item)

* **GET /item:** Get all items.
* **GET /item/{id}:** Get an item by ID.
* **POST /item:** Add a new item.

  ```json
  {
    "name": "Item Name",
    "idCategory": 1,
    "idUnit": 1
  }
  ```

* **DELETE /item/{id}:** Delete an item by ID.
* **PUT /item/{id}:** Update an item.

  ```json
  {
    "name": "Updated Item Name",
    "idCategory": 1,
    "idUnit": 1
  }
  ```

### Users (/users)

* **GET /users/users:** Get all registered users.
* **GET /users/userDetailsService/{id}:** Get user details by ID.

## Testing
To run the unit tests:

```bash
mvn test
```

## Architecture

ShoppMate API adopts a domain-driven architecture, where the code is organized around the main business areas. Each domain (such as Authentication, Categories, Items, Shopping Lists, Units, and Users) has its own internal structure, following a pattern that includes:

```
├── auth/        (Authentication Domain)
│   ├── configs/
│   ├── controller/ (Responsible for receiving and responding to authentication-related requests)
│   ├── service/    (Contains the business logic for authentication)
│   └── ...
├── category/    (Categories Domain)
│   ├── controller/ (Responsible for category requests)
│   ├── entity/     (Represents the category entity in the domain)
│   ├── repository/ (Responsible for persisting category data)
│   └── service/    (Business logic for categories)
├── item/        (Items Domain)
│   ├── controller/
│   ├── entity/
│   ├── repository/
│   └── service/
├── list/        (Shopping Lists Domain)
│   ├── controller/
│   ├── entity/
│   ├── repository/
│   └── service/
├── unit/        (Units Domain)
│   ├── controller/
│   ├── entity/
│   ├── repository/
│   └── service/
├── user/        (Users Domain)
│   ├── controller/
│   ├── entity/
│   ├── repository/
│   └── service/
├── shared/      (Concepts shared between domains)
│   └── domain/
├── utils/       (General utility classes)
└── ShoppMateApplication.java (Spring Boot application entry point)
```

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

Please refer to the [CONTRIBUTING.md](CONTRIBUTING.md) file for detailed guidelines on how to report bugs, suggest enhancements, and submit pull requests.

## License

This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
Public License. See LICENSE.md

## Contact

If you have any questions or suggestions, feel free to contact me at matheus.6148@gmail.com
