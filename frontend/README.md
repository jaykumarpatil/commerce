# E-Commerce Frontend

A modern Angular 21 e-commerce frontend application with standalone components, signals-based state management, and dark theme UI.

## Features

- **Authentication**: JWT-based login/register with OAuth2 integration
- **Product Catalog**: Browse, search, filter products with pagination
- **Shopping Cart**: Add/remove items, quantity management, persistent storage
- **Checkout Flow**: Multi-step checkout with shipping and payment
- **Order Management**: View order history and details
- **User Profile**: Manage profile and shipping addresses
- **Dark Theme**: Modern dark UI inspired by popular e-commerce platforms

## Tech Stack

- Angular 21 with standalone components
- Signals for reactive state management
- Angular Router with lazy loading
- Angular HttpClient with interceptors
- SCSS + Tailwind CSS
- RxJS for async operations

## Project Structure

```
src/
├── app/
│   ├── core/                    # Core services, guards, interceptors
│   │   ├── interceptors/        # Auth interceptor
│   │   ├── guards/             # Route guards
│   │   ├── services/          # API services
│   │   ├── models/             # TypeScript interfaces
│   │   └── stores/             # Signal-based stores
│   ├── shared/
│   │   └── components/         # Reusable UI components
│   └── features/              # Feature modules
│       ├── auth/                # Login, Register, Forgot Password
│       ├── home/                # Home page
│       ├── products/            # Product list and detail
│       ├── cart/                # Shopping cart
│       ├── checkout/            # Checkout flow
│       ├── orders/              # Order history
│       └── user/                # Profile management
├── environments/                # Environment configurations
└── styles/                      # Global SCSS styles
```

## Prerequisites

- Node.js 18+
- Angular CLI 21
- Java 17 (for backend services)
- Running backend services on https://localhost:8443

## Setup

1. Install dependencies:
   ```bash
   npm install
   ```

2. Generate SSL certificates for HTTPS (or use the provided script):
   ```bash
   # On macOS/Linux
   ./generate-ssl.sh
   
   # Or manually create self-signed certificates:
   openssl req -x509 -newkey rsa:2048 -keyout ssl/server.key -out ssl/server.crt -days 365 -nodes -subj "/CN=localhost"
   ```

3. Start the development server:
   ```bash
   npm start
   ```

4. Open https://localhost:4200 in your browser

## API Integration

The frontend connects to the Spring Cloud Gateway at `https://localhost:8443`.

### API Endpoints

| Service | Endpoint | Description |
|---------|----------|-------------|
| Auth | POST /v1/users/login | User login |
| Auth | POST /v1/users/register | User registration |
| Products | GET /v1/products | List products |
| Products | GET /v1/products/{id} | Get product details |
| Products | GET /v1/products/search | Search products |
| Categories | GET /v1/categories | List categories |
| Cart | GET /v1/carts/user/{userId} | Get user cart |
| Orders | POST /v1/orders | Create order |
| Orders | GET /v1/orders/user/{userId} | Get user orders |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| API_URL | https://localhost:8443 | API Gateway URL |
| TOKEN_KEY | auth_token | LocalStorage key for JWT |
| REFRESH_TOKEN_KEY | refresh_token | LocalStorage key for refresh token |
| CART_STORAGE_KEY | shopping_cart | LocalStorage key for cart |

## Development

### Build

```bash
npm run build           # Production build
npm run build:watch     # Watch mode
```

### Testing

```bash
npm test                # Run unit tests
npm run lint            # Lint code
```

## Design System

### Color Palette

| Name | Hex | Usage |
|------|-----|-------|
| Background Primary | #0d0d0f | Main background |
| Background Secondary | #16161a | Cards, modals |
| Background Tertiary | #1e1e24 | Elevated surfaces |
| Accent Primary | #6366f1 | Primary actions, links |
| Accent Secondary | #22d3ee | Secondary accent |
| Success | #10b981 | Success states |
| Warning | #f59e0b | Warning states |
| Danger | #ef4444 | Error states |

### Typography

- Primary Font: Inter
- Monospace Font: JetBrains Mono

## Browser Support

- Chrome/Edge 90+
- Firefox 90+
- Safari 15+

## License

MIT
