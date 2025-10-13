# Vite Configuration Optimization

## Overview

This document details the optimizations made to the Vite configuration to improve performance, enable code splitting, and configure CDN usage for the Ekehi Network landing page.

For detailed instructions on setting up your CDN, see:
[CDN Setup Guide](./CDN_SETUP_GUIDE.md)

## Key Optimizations

### 1. Code Splitting Configuration

The Vite configuration has been enhanced with manual code splitting to optimize bundle sizes and loading performance:

```typescript
manualChunks: {
  vendor: ['react', 'react-dom'],
  ui: ['lucide-react'],
  charts: ['chart.js', 'react-chartjs-2'],
  routing: ['react-router'],
  validation: ['zod', '@hono/zod-validator']
}
```

This configuration separates commonly used libraries into their own chunks, allowing for better caching and reduced initial bundle size.

### 2. Cache-Busting Asset Naming

Assets are now configured with content-based hashes for effective cache busting:

```typescript
entryFileNames: 'assets/[name].[hash].js',
chunkFileNames: 'assets/[name].[hash].js',
assetFileNames: 'assets/[name].[hash].[ext]
```

This ensures that when files are updated, users receive the latest versions rather than cached ones.

### 3. CDN Configuration

The build process is configured to work with CDN deployment:

```typescript
base: process.env.NODE_ENV === 'production' ? 'https://cdn.ekehi.xyz/' : '/'
```

This allows assets to be served from a CDN in production while maintaining local development functionality.

### 4. Mobile-Specific Optimizations

Several mobile-specific optimizations have been added:

- Disabled sourcemaps in production to reduce bundle size
- Enabled CSS code splitting
- Set appropriate chunk size warning limits
- Added mobile-specific define constants

## Benefits

### 1. Performance Improvements
- Reduced initial bundle size through code splitting
- Faster loading times with optimized asset delivery
- Better caching strategies with content-based hashing

### 2. Mobile Optimization
- Smaller bundles suitable for mobile networks
- Optimized asset handling for mobile devices
- Reduced memory footprint

### 3. Deployment Flexibility
- CDN-ready configuration
- Environment-specific base URLs
- Cache-friendly asset naming

## Files Modified

- `vite.config.ts` - Main Vite configuration with all optimizations

## Configuration Details

### Manual Chunks Strategy
The manual chunks configuration separates libraries by function:
- `vendor`: Core React libraries
- `ui`: UI component libraries
- `charts`: Charting libraries
- `routing`: Routing libraries
- `validation`: Validation libraries

### Asset Optimization
- CSS code splitting enabled
- Sourcemaps disabled in production
- Assets inlined only when below 4096 bytes threshold

### Mobile Optimizations
- Chunk size warning limit increased to 5000
- Mobile-specific define constants
- Host configuration optimized for mobile development

## Testing

The optimizations have been verified to ensure:

1. Build process completes successfully
2. Code splitting works as expected
3. Assets are correctly named with hashes
4. CDN configuration works in production builds
5. Mobile performance is improved
6. No functionality is broken by the optimizations

## Related Documentation

- [CDN Setup Guide](./CDN_SETUP_GUIDE.md) - Instructions for setting up your CDN
- [Project Documentation](../DOCUMENTATION.md) - General project information

## Future Considerations

### 1. Dynamic Imports
Consider implementing dynamic imports for route-based code splitting.

### 2. Preloading Strategies
Add link preload headers for critical resources.

### 3. Compression
Implement additional compression strategies for assets.