# Ekehi Mobile Admin Dashboard Documentation

## Table of Contents
1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Getting Started](#getting-started)
4. [Authentication](#authentication)
5. [Modules](#modules)
   - [Dashboard](#dashboard)
   - [Presale Management](#presale-management)
   - [Ad Management](#ad-management)
   - [Wallet Activities](#wallet-activities)
   - [Social Tasks](#social-tasks)
   - [User Management](#user-management)
6. [UI Components](#ui-components)
7. [API Integration](#api-integration)
8. [Testing](#testing)
9. [Deployment](#deployment)

## Overview

The Ekehi Mobile Admin Dashboard is a comprehensive administration platform built with Next.js 14, React, and TypeScript. It provides full control over app activities and data manipulation capabilities for presale, ads, wallet activities, social tasks, and user management.

## Project Structure

```
admin/
├── app/                 # Next.js 14 App Router pages
│   ├── api/             # API routes
│   ├── auth/            # Authentication pages
│   ├── dashboard/       # Dashboard pages
│   ├── presale/         # Presale management pages
│   ├── ads/             # Ad management pages
│   ├── wallet/          # Wallet activity pages
│   ├── social/          # Social task pages
│   ├── users/           # User management pages
│   └── layout.tsx       # Root layout
├── components/          # Reusable UI components
├── contexts/            # React context providers
├── lib/                 # Utility functions and helpers
├── __tests__/           # Test files
├── public/              # Static assets
└── styles/              # Global styles
```

## Getting Started

### Prerequisites
- Node.js 18+
- pnpm package manager
- Git

### Installation
```bash
cd admin
pnpm install
```

### Development
```bash
pnpm dev
```
The application will be available at http://localhost:3000

### Build
```bash
pnpm build
```

### Production
```bash
pnpm start
```

## Authentication

The admin system uses a token-based authentication system with protected routes.

### Login Process
1. User navigates to `/auth/login`
2. Credentials are submitted to `/api/auth/login`
3. On successful authentication, a JWT token is stored in localStorage
4. User is redirected to the dashboard

### Protected Routes
All routes except authentication pages are protected by middleware that checks for a valid JWT token.

## Modules

### Dashboard
The dashboard provides an overview of key metrics and analytics:
- User statistics
- Revenue overview
- Recent activities
- Performance charts

### Presale Management
Manage presale campaigns with features:
- Create and edit presale campaigns
- Set pricing and allocation limits
- Monitor progress and participation
- View participant details

### Ad Management
Control advertising content and campaigns:
- Create and manage ad campaigns
- Set targeting parameters
- Monitor performance metrics
- Schedule ad placements

### Wallet Activities
Monitor and manage wallet transactions:
- View transaction history
- Track user balances
- Process withdrawals
- Audit financial activities

### Social Tasks
Manage social engagement activities:
- Create social challenges
- Track user participation
- Monitor completion rates
- Award rewards

### User Management
Comprehensive user administration:
- View user profiles and activity
- Manage user roles and permissions
- Handle account issues
- Monitor user growth metrics

## UI Components

The admin system includes a comprehensive set of reusable UI components:

### Button
Customizable button with multiple variants and sizes.

### Alert
Notification component with different status types.

### Card
Container component for content sections.

### StatCard
Specialized card for displaying statistics with icons.

### Modal
Dialog component for overlays and forms.

### Table
Data table with sorting, filtering, and pagination.

### SearchFilter
Search and filtering component.

### Chart
Data visualization components using Recharts.

## API Integration

All data operations are performed through REST API endpoints:

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### Dashboard
- `GET /api/dashboard/stats` - Dashboard statistics
- `GET /api/dashboard/chart` - Chart data

### Presale
- `GET /api/presale` - List presale campaigns
- `POST /api/presale` - Create presale campaign
- `GET /api/presale/[id]` - Get presale campaign details
- `PUT /api/presale/[id]` - Update presale campaign
- `DELETE /api/presale/[id]` - Delete presale campaign

### Ads
- `GET /api/ads` - List advertisements
- `POST /api/ads` - Create advertisement
- `GET /api/ads/[id]` - Get advertisement details
- `PUT /api/ads/[id]` - Update advertisement
- `DELETE /api/ads/[id]` - Delete advertisement

### Wallet
- `GET /api/wallet/transactions` - List transactions
- `GET /api/wallet/balances` - List user balances

### Social
- `GET /api/social/tasks` - List social tasks
- `POST /api/social/tasks` - Create social task
- `GET /api/social/tasks/[id]` - Get social task details
- `PUT /api/social/tasks/[id]` - Update social task
- `DELETE /api/social/tasks/[id]` - Delete social task

### Users
- `GET /api/users` - List users
- `GET /api/users/[id]` - Get user details
- `PUT /api/users/[id]` - Update user
- `DELETE /api/users/[id]` - Delete user

## Testing

The project includes a comprehensive test suite with 41 tests covering all UI components:

```bash
pnpm test          # Run all tests
pnpm test:watch    # Run tests in watch mode
```

Test coverage includes:
- Component rendering
- User interactions
- State management
- Props handling
- Accessibility features

## Deployment

### Environment Variables
Create a `.env.local` file with the following variables:
```
NEXT_PUBLIC_API_URL=your_api_url
JWT_SECRET=your_jwt_secret
```

### Build and Deploy
```bash
pnpm build
pnpm start
```

The application can be deployed to any Node.js hosting platform such as Vercel, Netlify, or a custom server.