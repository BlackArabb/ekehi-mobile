# Complete Guide: Migrating to Cloudflare for Ekehi Network

This document explains how to migrate your Ekehi Network mobile app backend from Mocha to direct Cloudflare hosting.

## Why Migrate to Cloudflare?

1. **Better Performance**: Cloudflare's global edge network provides sub-100ms response times
2. **Lower Costs**: Eliminate the middleman (Mocha) and reduce hosting costs
3. **More Control**: Direct access to your infrastructure and data
4. **Scalability**: Automatic scaling with Cloudflare Workers
5. **Reliability**: 99.9% uptime with built-in DDoS protection

## Migration Overview

The migration involves:
1. Setting up Cloudflare Workers for your API
2. Creating a Cloudflare D1 database
3. Updating your mobile app to point to the new endpoints
4. Testing everything works correctly

## Step-by-Step Migration Guide

### 1. Prerequisites

- A Cloudflare account (free tier works)
- Node.js 18+ installed
- npm or yarn package manager

### 2. Set Up Cloudflare Workers

1. **Install Wrangler CLI**:
   ```bash
   npm install -g wrangler
   ```

2. **Login to Cloudflare**:
   ```bash
   wrangler login
   ```

3. **Navigate to backend directory**:
   ```bash
   cd cloudflare-backend
   ```

4. **Install dependencies**:
   ```bash
   npm install
   ```

### 3. Create Required Resources

1. **Create D1 database**:
   ```bash
   wrangler d1 create ekehi-network-db
   ```
   Save the database ID from the output.

2. **Update configuration**:
   Edit [wrangler.toml](file://c:\Users\ARQAM%20TV\Downloads\mobile\cloudflare-backend\wrangler.toml) and replace `YOUR_ACTUAL_DATABASE_ID_HERE` with your actual database ID.

   For a simpler setup without KV caching, you can rename [wrangler-simple.toml](file://c:\Users\ARQAM%20TV\Downloads\mobile\cloudflare-backend\wrangler-simple.toml) to [wrangler.toml](file://c:\Users\ARQAM%20TV\Downloads\mobile\cloudflare-backend\wrangler.toml):
   ```bash
   cp wrangler-simple.toml wrangler.toml
   ```

### 4. Set Secrets

Set your required secrets:
```bash
wrangler secret put GOOGLE_CLIENT_ID
wrangler secret put GOOGLE_CLIENT_SECRET
wrangler secret put JWT_SECRET
```

### 5. Deploy Your API

1. **Deploy to Cloudflare**:
   ```bash
   wrangler deploy
   ```

2. **Find your endpoint**: The deployment output will show your endpoint:
   ```
   https://ekehi-network-api.YOUR_SUBDOMAIN.workers.dev
   ```

3. **Use helper scripts**: For easier endpoint discovery:
   - On macOS/Linux: `./find-endpoint.sh`
   - On Windows: `.\find-endpoint.ps1`

### 6. Deploy Web Frontend to Vercel

1. **Build the web app**:
   ```bash
   npm run build
   ```

2. **Deploy to Vercel**:
   ```bash
   vercel --prod
   ```

3. **Note the deployment URL**: The deployment output will show your frontend URL:
   ```
   https://ekehi-network-nojxotiyc-kamal-s-projects.vercel.app
   ```

### 7. Update Mobile App

1. **Update API configuration** in `src/config/api.ts`:
   ```typescript
   export const API_CONFIG = {
     BASE_URL: 'https://ekehi-network-api.YOUR_SUBDOMAIN.workers.dev',
     // ... rest of config
   };
   ```

2. **Build and test** the mobile app:
   ```bash
   npm install
   npm start
   ```

### 8. Testing

1. **Run integration tests**:
   ```bash
   npm test
   ```

2. **Monitor with**:
   ```bash
   wrangler tail
   ```

## Finding Your Cloudflare Endpoint

After deployment, your endpoint will be in the format:
- `https://ekehi-network-api.YOUR_ACCOUNT.workers.dev`

Examples:
- `https://ekehi-network-api.abc123xyz.workers.dev`
- `https://ekehi-network-api.mycompany.workers.dev`

## Custom Domain Setup (Optional)

To use a custom domain:

1. Purchase or transfer a domain to Cloudflare
2. In Cloudflare dashboard:
   - Go to Workers & Pages
   - Select your worker
   - Go to Settings > Triggers
   - Add Custom Domain (e.g., `api.yourdomain.com`)
3. Update your mobile app config:
   ```typescript
   export const API_CONFIG = {
     BASE_URL: 'https://api.yourdomain.com',
     // ... rest of config
   };
   ```

## Troubleshooting Common Issues

### Authentication Errors
- Make sure you've run `wrangler login`
- Verify your Google OAuth credentials are correct

### Deployment Errors
- Check that your Cloudflare account is properly set up
- Ensure you have the Workers Paid plan (required for D1)
- Verify that [wrangler.toml](file://c:\Users\ARQAM%20TV\Downloads\mobile\cloudflare-backend\wrangler.toml) has the correct database ID

### Database Errors
- Confirm you've created the D1 database
- Ensure migrations have been applied
- Check that the database ID in [wrangler.toml](file://c:\Users\ARQAM%20TV\Downloads\mobile\cloudflare-backend\wrangler.toml) is correct

### Secret Errors
- Verify all required secrets are set
- Check that secret values are correct

### KV Namespace Errors
If you're getting KV namespace errors like:
```
KV namespace 'your-kv-namespace-id' is not valid
```

This means the KV namespace configuration in [wrangler.toml](file://c:\Users\ARQAM%20TV\Downloads\mobile\cloudflare-backend\wrangler.toml) is incorrect. You can either:

1. **Create the KV namespace**:
   ```bash
   wrangler kv namespace create CACHE
   ```
   Then update [wrangler.toml](file://c:\Users\ARQAM%20TV\Downloads\mobile\cloudflare-backend\wrangler.toml) with the actual IDs.

2. **Use the simple configuration**:
   ```bash
   cp wrangler-simple.toml wrangler.toml
   ```

### OAuth Flow Issues

#### Web Authentication Flow
For web authentication, the flow works as follows:
1. User clicks "Continue with Google" on the web app
2. User is redirected to Google OAuth
3. After authentication, Google redirects to the Cloudflare Worker OAuth return endpoint
4. The OAuth return endpoint exchanges the code for a session token
5. The user is redirected to the Vercel-deployed frontend app at `/mine`

#### Mobile Authentication Flow
For mobile authentication, the flow works as follows:
1. User clicks "Continue with Google" in the mobile app
2. User is redirected to Google OAuth
3. After authentication, Google redirects back to the mobile app via deep linking (`ekehi://auth`)
4. The mobile app exchanges the code for a session token
5. The user is redirected to the mining page within the app

## Benefits After Migration

1. **Performance**: Sub-100ms response times globally
2. **Cost Savings**: Reduced hosting costs by eliminating middleman
3. **Control**: Direct access to your data and infrastructure
4. **Scalability**: Automatic scaling with no additional configuration
5. **Security**: Built-in DDoS protection and security features

## Next Steps

1. Monitor your new Cloudflare deployment
2. Update any documentation with new endpoints
3. Test all app features thoroughly
4. Inform your users of any maintenance windows if switching live traffic

## Getting Help

- Cloudflare documentation: https://developers.cloudflare.com/workers/
- View logs: `wrangler tail`
- Check deployment status: `wrangler deployments list`
- Contact Cloudflare support for account issues