# 🛒 ShoppMate

![Angular](https://img.shields.io/badge/Angular-19.2.7-red?style=for-the-badge&logo=angular)
![Material](https://img.shields.io/badge/Material-19.2.9-purple?style=for-the-badge&logo=angular)
![TypeScript](https://img.shields.io/badge/TypeScript-5.7.2-blue?style=for-the-badge&logo=typescript)

ShoppMate is a modern, high-performance web application designed to simplify shopping list management. Built with **Angular 19+** and **Material Design 3**, it provides a seamless and accessible experience for users to organize their purchases.

## ✨ Key Features

- 🔐 **Secure Authentication**: JWT-based login and signup with route guards.
- 📋 **List Management**: Create, edit, and delete multiple shopping lists.
- 📦 **Item Catalog**: Comprehensive management of items, including custom categories and units.
- ⚡ **Reactive UI**: Real-time state management using Angular Signals (work in progress).
- 🎨 **Material Design 3**: Modern UI/UX with support for light and dark color schemes.
- 📱 **Responsive Design**: Fully optimized for mobile, tablet, and desktop views.

## 🛠️ Tech Stack

- **Framework:** [Angular 19.2.0](https://angular.dev/)
- **UI Components:** [Angular Material 19.2.9](https://material.angular.io/)
- **Styling:** SCSS with Material 3 Design Tokens
- **Language:** TypeScript 5.7.2
- **Testing:** Jasmine & Karma

## 📐 Architecture & Standards

The project follows strict development guidelines to ensure scalability, maintainability, and accessibility.

### 🧩 Angular Best Practices
- **Standalone Components**: All components are standalone, promoting better modularity.
- **Signals for State**: Moving towards Signals-based state management for optimized change detection.
- **OnPush Change Detection**: Components are configured with `ChangeDetectionStrategy.OnPush` for better performance.
- **Functional Services**: Services are designed around single responsibilities using `inject()`.
- **Modern Control Flow**: Templates utilize `@if`, `@for`, and `@switch` syntax.

### ⌨️ TypeScript Standards
- Strict type checking enabled.
- Preference for type inference.
- Avoidance of `any`, favoring `unknown` for uncertain types.

### ♿ Accessibility (a11y)
- Designed to pass all **AXE checks**.
- Adheres to **WCAG AA** minimums.
- Careful focus management, color contrast, and ARIA attribute usage.

## 🚀 Getting Started

### Prerequisites

- **Node.js**: v18.0.0 or higher
- **npm**: v9.0.0 or higher
- **Angular CLI**: v19.2.7

### Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/omatheusmesmo/ShoppMateFront.git
   cd shopp-mate-front
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Environment Setup:**
   Ensure the API URL is correctly configured in `src/environments/environment.ts`.

### Development Server

Run the following command to start the development server:

```bash
ng serve
```
Navigate to `http://localhost:4200/`.

## 📁 Project Structure

```text
src/
├── app/
│   ├── auth/           # Authentication modules (Login, Signup, Guards)
│   ├── layout/         # Core layout (Navbar, Landing Page)
│   ├── list/           # Shopping list and management features
│   ├── shared/         # Reusable components, services, and interfaces
│   │   ├── components/ # Common UI elements (Category, Item, etc.)
│   │   ├── services/   # Business logic and API interaction
│   │   └── interfaces/ # Type definitions
│   └── app.config.ts   # Main application configuration
├── assets/             # Static assets and icons
└── styles/             # Global styles and Material theme tokens
```

## ⚙️ Available Commands

| Command | Description |
|---------|-------------|
| `ng serve` | Runs the app in development mode |
| `ng build` | Builds the app for production in `dist/` |
| `ng test` | Executes unit tests via Karma |
| `ng lint` | Runs linting checks (if configured) |

## 🤝 Contributing

We welcome contributions! Please refer to the [CONTRIBUTING.md](../CONTRIBUTING.md) file in the project root for detailed guidelines on how to report bugs, suggest enhancements, and submit pull requests.

## 📄 License

This project is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International Public License. See the [LICENSE.md](../LICENSE.md) file in the project root for details.

---
Developed with ❤️ by [Matheus](https://github.com/omatheusmesmo)