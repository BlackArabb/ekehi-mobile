# CDN Setup Guide for ekehi.xyz

## Overview

This guide explains how to set up a Content Delivery Network (CDN) for the Ekehi Network landing page using cdn.ekehi.xyz. The project is already configured to work with a CDN, but requires proper deployment setup to upload assets to the CDN.

## Prerequisites

1. A CDN provider account (Cloudflare, AWS CloudFront, Azure CDN, etc.)
2. A custom domain (cdn.ekehi.xyz)
3. SSL certificate for the CDN domain
4. Access to your DNS management console

## Current Configuration

The project is already configured for CDN usage in `vite.config.ts`:

```typescript
base: process.env.NODE_ENV === 'production' ? 'https://cdn.ekehi.xyz/' : '/'
```

And asset naming is configured for optimal caching:

```typescript
entryFileNames: 'assets/[name].[hash].js',
chunkFileNames: 'assets/[name].[hash].js',
assetFileNames: 'assets/[name].[hash].[ext]'
```

## CDN Setup Options

### Option 1: Cloudflare (Recommended)

1. **Create a Cloudflare Account**
   - Sign up at https://www.cloudflare.com/
   - Add your domain (cdn.ekehi.xyz)

2. **Configure DNS**
   - Point your cdn.ekehi.xyz DNS to Cloudflare nameservers
   - Add a CNAME record pointing to your origin server

3. **Set up SSL**
   - Cloudflare provides free SSL certificates
   - Choose "Full" SSL/TLS encryption mode

4. **Configure Cache Settings**
   - In the Cloudflare dashboard, go to "Caching" > "Configuration"
   - Set caching level to "Standard" or "Aggressive"
   - Add page rules for assets:
     - Pattern: `cdn.ekehi.xyz/assets/*`
     - Setting: "Cache Level" = "Cache Everything"
     - Setting: "Browser Cache TTL" = "1 year"

### Option 2: AWS CloudFront

1. **Create CloudFront Distribution**
   - Go to AWS CloudFront console
   - Click "Create Distribution"
   - Set "Origin Domain" to your web server
   - Set "Origin Path" to where your built assets are hosted

2. **Configure Default Cache Behavior**
   - Set "Viewer Protocol Policy" to "Redirect HTTP to HTTPS"
   - Set "Allowed HTTP Methods" to "GET, HEAD"
   - Set "Cache Policy" to "CachingOptimized"

3. **Add Custom Domain**
   - In "Alternate Domain Names (CNAMEs)", add "cdn.ekehi.xyz"
   - Configure SSL certificate using AWS Certificate Manager

4. **Configure DNS**
   - Point cdn.ekehi.xyz to the CloudFront distribution domain
   - Use a CNAME record in your DNS provider

### Option 3: Azure CDN

1. **Create Azure CDN Profile**
   - In Azure portal, create a new CDN profile
   - Choose a pricing tier (Microsoft, Verizon, or Akamai)

2. **Create CDN Endpoint**
   - Add a new endpoint
   - Set origin hostname to your web server
   - Set origin path to where assets are hosted

3. **Configure Custom Domain**
   - Add "cdn.ekehi.xyz" as a custom domain
   - Configure SSL certificate

4. **Configure DNS**
   - Point cdn.ekehi.xyz to the Azure CDN endpoint

## Deployment Process

### 1. Build the Project

```bash
# Navigate to the LandPage directory
cd c:\ekehi-mobile\LandPage

# Install dependencies
pnpm install

# Build for production
pnpm build
```

This will generate files in the `dist` directory with content-hashed filenames.

### 2. Upload Assets to CDN

Depending on your CDN provider, you have several options:

#### Option A: Manual Upload
1. Upload the contents of the `dist/assets` directory to your CDN
2. Ensure all files are publicly accessible
3. Verify that URLs like `https://cdn.ekehi.xyz/assets/index.[hash].js` work

#### Option B: Automated Deployment
1. Set up a CI/CD pipeline (GitHub Actions, GitLab CI, etc.)
2. Configure the pipeline to:
   - Build the project
   - Upload assets to CDN
   - Invalidate CDN cache if needed

Example GitHub Actions workflow:
```yaml
name: Deploy to CDN
on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Setup Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '22'
          
      - name: Install dependencies
        run: |
          npm install -g pnpm
          pnpm install
        
      - name: Build
        run: pnpm build
        
      - name: Deploy to CDN
        run: |
          # Add your CDN deployment commands here
          # This will vary depending on your CDN provider
```

### 3. Verify CDN Configuration

1. Check that assets are being served from the CDN:
   - Open your website
   - Open browser developer tools
   - Check Network tab to verify assets are loaded from cdn.ekehi.xyz

2. Verify cache headers:
   - Assets should have appropriate cache headers (Cache-Control, Expires)
   - Content-hashed filenames should have long cache durations

## Environment Variables

To ensure the CDN is used in production builds, you may need to set the NODE_ENV environment variable:

```bash
# For Linux/Mac
export NODE_ENV=production

# For Windows Command Prompt
set NODE_ENV=production

# For Windows PowerShell
$env:NODE_ENV="production"
```

## Testing

1. **Local Testing**
   - Test locally with development server: `pnpm dev`
   - Assets should load from local server (not CDN)

2. **Production Testing**
   - Build with production environment: `NODE_ENV=production pnpm build`
   - Deploy and verify assets load from cdn.ekehi.xyz
   - Check browser network tab for asset origins

3. **Performance Testing**
   - Use tools like Google PageSpeed Insights
   - Verify assets are properly cached
   - Check loading times improvement

## Troubleshooting

### Common Issues

1. **Assets not loading from CDN**
   - Check that NODE_ENV is set to production during build
   - Verify CDN domain is correctly configured in vite.config.ts
   - Ensure assets are uploaded to the correct location

2. **Mixed Content Warnings**
   - Ensure all asset URLs use HTTPS
   - Check that your CDN is configured with SSL

3. **Cache Issues**
   - When updating assets, invalidate CDN cache
   - Verify content-hashing is working (filenames should change when content changes)

### Debugging Steps

1. Check build output:
   ```bash
   pnpm build
   # Check that dist/index.html references CDN URLs
   ```

2. Verify asset URLs:
   - Open dist/index.html
   - Check that script and link tags reference cdn.ekehi.xyz

3. Test CDN directly:
   - Try accessing an asset directly via CDN URL
   - Check HTTP response headers for cache information

## Best Practices

1. **Cache Invalidation**
   - Use content-based hashing (already configured)
   - Invalidate cache only when necessary

2. **Compression**
   - Enable gzip/Brotli compression on your CDN
   - Most CDNs enable this by default

3. **Monitoring**
   - Set up monitoring for CDN performance
   - Monitor cache hit rates
   - Track bandwidth usage

4. **Security**
   - Use HTTPS for all CDN assets
   - Implement proper CORS headers if needed
   - Restrict access to sensitive assets if any

## Related Documentation

- [Vite Optimization Documentation](./VITE_OPTIMIZATION.md) - Details about the Vite configuration
- [Project Documentation](../DOCUMENTATION.md) - General project information

## Support

For issues with CDN setup, contact your CDN provider's support team. For issues with the build configuration, refer to the project documentation or create an issue in the repository.