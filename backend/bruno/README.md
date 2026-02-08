# ShoppMate Bruno Collection

This is the collection of API requests for ShoppMate.

## How to use

1. Open the [Bruno](https://www.usebruno.com/) app.
2. Click on "Open Collection" and select this folder (`bruno`).
3. Select the `Development` environment in the top right corner.
4. Run the `Auth -> Register` request to create a new user.
5. Run the `Auth -> Login` request. The JWT token will be automatically saved to the `token` variable.
6. All other requests inherit Bearer Token authentication using this variable.

## Variables



- `baseUrl`: The API's base URL (default: `http://localhost:8080`).

- `token`: The JWT token obtained after login.

- `listId`, `itemId`, `categoryId`, etc.: IDs used as parameters in various requests (configured in the `Vars` tabs of each request).



> **Note:** The request paths in this collection follow the actual implementation in the Spring Boot Controllers (e.g., `/lists` instead of `/shopplists`), which might differ from some outdated documentation.
