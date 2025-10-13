# Ekehi Landing Page Documentation

## Project Overview

This is the landing page for the Ekehi Network cryptocurrency project. The site provides information about the project, tokenomics, roadmap, and instructions for downloading and using the mobile mining app.

## Table of Contents

1. [Project Structure](#project-structure)
2. [Key Pages](#key-pages)
3. [Components](#components)
4. [Styling](#styling)
5. [Deployment](#deployment)
6. [Mobile App OAuth](#mobile-app-oauth)
7. [Performance Optimizations](#performance-optimizations)
8. [CDN Setup](#cdn-setup)

## Project Structure

```
src/
├── react-app/
│   ├── components/     # Reusable UI components
│   ├── pages/          # Page components
│   ├── docs/           # Documentation files
│   └── main.tsx        # Entry point
├── shared/             # Shared utilities
├── worker/             # Cloudflare Worker code
└── index.html          # Main HTML file
```

## Key Pages

### Home Page
- Main landing page with all sections
- Navigation to all other sections
- Hero section with call-to-action

### FAQ Page
- Comprehensive list of frequently asked questions
- Organized by categories
- Search functionality

### Privacy Policy & Terms
- Legal documents for the platform
- Regularly updated to comply with regulations

### OAuth Callback Handler
- Processes OAuth responses from Google for mobile app users
- Redirects users to the mobile app via deep linking
- Located at `/oauth/callback`

## Components

### Navigation
- Responsive navigation bar
- Smooth scrolling to sections
- Mobile-friendly hamburger menu

### Hero Section
- Eye-catching introduction to Ekehi
- Call-to-action buttons
- Animated background elements

### About Section
- Project overview and mission
- Key features and benefits

### Whitepaper Section
- Access to project whitepaper
- Download options

### Ecosystem Section
- Overview of the Ekehi ecosystem
- Visual representation of components

### Tokenomics Section
- Detailed breakdown of token distribution
- Visual charts and graphs

### Roadmap Section
- Project timeline and milestones
- Interactive timeline component

### Mining App Section
- Information about the mobile mining app
- Download links and instructions

### Contact Section
- Contact information
- Social media links
- Contact form

## Styling

The site uses Tailwind CSS for styling with a custom color palette:

- Primary: `#FFD700` (Ekehi Gold)
- Background: `#000000` (Black)
- Text: `#FFFFFF` (White)
- Secondary Text: `#A0A0A0` (Medium Gray)
- Borders: `#2A2A2A` (Charcoal Gray)

## Deployment

The site is deployed on Vercel and accessible at http://ekehi.xyz

To deploy updates:
```bash
npm run build
# Deploy using Vercel CLI or GitHub integration
```

## Mobile App OAuth

For detailed information about the OAuth callback handler for mobile app users, see:
[Mobile App OAuth Documentation](./src/react-app/docs/MOBILE_APP_OAUTH.md)

This handler is specifically designed for users who authenticate through the mobile app's "Continue with Google" button and is accessed via deep linking rather than direct website navigation.

## Performance Optimizations

The Vite configuration has been optimized for performance with code splitting and CDN support:

- Code splitting for better caching and reduced initial bundle size
- Cache-busting with content-based hashes
- CDN-ready configuration for production deployment
- Mobile-specific optimizations

For detailed information about Vite optimizations, see:
[Vite Optimization Documentation](./docs/VITE_OPTIMIZATION.md)

## CDN Setup

The project is configured to work with a Content Delivery Network (CDN) at cdn.ekehi.xyz. For instructions on how to set up and configure your CDN, see:
[CDN Setup Guide](./docs/CDN_SETUP_GUIDE.md)

This guide provides detailed instructions for setting up CDN with various providers including Cloudflare, AWS CloudFront, and Azure CDN.