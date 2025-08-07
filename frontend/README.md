# Employee Management System - Frontend

A modern React frontend for the Employee Management System built with TypeScript, Vite, and Mantine UI.

## Tech Stack

- **React 19+** - Frontend framework with modern features
- **TypeScript** - Type-safe JavaScript development
- **Vite** - Fast build tool and development server
- **Mantine** - Comprehensive React component library
- **Zustand** - Lightweight global state management
- **TanStack Query** - Server state management and caching
- **React Router v6** - Client-side routing
- **React Hook Form** - Form state management and validation
- **Zod** - Runtime type validation and schema definition
- **Socket.IO Client** - WebSocket client for real-time features
- **Axios** - HTTP client for API communication
- **Vitest** - Fast unit test runner
- **React Testing Library** - Component testing utilities

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── ui/             # Basic UI components
│   ├── forms/          # Form components
│   └── layout/         # Layout components
├── features/           # Feature-based modules
│   ├── auth/           # Authentication feature
│   ├── employees/      # Employee management
│   ├── departments/    # Department management
│   ├── chat/           # Chat functionality
│   ├── email/          # Email management
│   ├── notifications/  # Notification system
│   └── permissions/    # Permission management
├── hooks/              # Global custom React hooks
├── services/           # API and external services
├── stores/             # Zustand stores
├── types/              # TypeScript type definitions
├── utils/              # Utility functions
├── constants/          # Application constants
├── assets/             # Static assets
└── test/               # Test utilities and setup
```

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn

### Installation

1. Install dependencies:
```bash
npm install
```

2. Copy environment variables:
```bash
cp .env.example .env
```

3. Start the development server:
```bash
npm run dev
```

The application will be available at `http://localhost:3000`.

## Available Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run lint:fix` - Fix ESLint errors
- `npm run format` - Format code with Prettier
- `npm run format:check` - Check code formatting
- `npm run test` - Run tests
- `npm run test:ui` - Run tests with UI
- `npm run test:coverage` - Run tests with coverage
- `npm run test:watch` - Run tests in watch mode
- `npm run type-check` - Run TypeScript type checking

## Development Guidelines

### Code Style
- Use TypeScript strict mode for enhanced type safety
- Follow functional component patterns with hooks
- Implement proper error boundaries for error handling
- Use feature-based folder organization
- Write comprehensive tests for components and hooks

### State Management
- **Global State (Zustand)**: Authentication, UI preferences, notifications
- **Server State (TanStack Query)**: API data with caching and synchronization
- **Local State (useState/useReducer)**: Component-specific state
- **Form State (React Hook Form)**: Form data and validation

### Testing
- Write unit tests for all components and hooks
- Use React Testing Library for component testing
- Mock API calls with MSW
- Maintain high test coverage

## API Integration

The frontend communicates with the Spring Boot backend via REST APIs and WebSocket connections:

- **REST API**: `http://localhost:8080/api`
- **WebSocket**: `ws://localhost:8080`

## Contributing

1. Follow the established project structure
2. Write tests for new features
3. Ensure TypeScript strict mode compliance
4. Follow accessibility guidelines (WCAG 2.1)
5. Use semantic commit messages