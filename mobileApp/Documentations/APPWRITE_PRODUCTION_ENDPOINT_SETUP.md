# Appwrite Production Endpoint Setup

This document explains how the Appwrite configuration has been updated to support production environments.

## Current Configuration

The mobile app and admin panel are configured to use the Frankfurt region Appwrite Cloud endpoint:
- **Endpoint**: `https://fra.cloud.appwrite.io/v1`
- **Project ID**: `68c2dd6e002112935ed2`
- **Database ID**: `68c336e7000f87296feb`

## Environment Variable Support

Both applications now support environment variables for configuration, which is a best practice for production deployments.

### Mobile App Environment Variables

The mobile app uses the following environment variables:

- `EXPO_PUBLIC_APPWRITE_ENDPOINT` - Appwrite API endpoint
- `EXPO_PUBLIC_APPWRITE_PROJECT_ID` - Appwrite project ID
- `EXPO_PUBLIC_APPWRITE_DATABASE_ID` - Database ID
- `EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID` - Google OAuth Web Client ID
- `EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID` - Google OAuth Android Client ID
- `EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID` - Google OAuth iOS Client ID

### Admin Panel Environment Variables

The admin panel uses the following environment variables:

- `APPWRITE_ENDPOINT` - Appwrite API endpoint
- `APPWRITE_PROJECT_ID` - Appwrite project ID
- `APPWRITE_API_KEY` - Appwrite API key (for admin operations)
- `DATABASE_ID` - Database ID
- `COLLECTION_USERS` - Users collection ID
- `COLLECTION_USER_PROFILES` - User profiles collection ID
- `COLLECTION_MINING_SESSIONS` - Mining sessions collection ID
- `COLLECTION_SOCIAL_TASKS` - Social tasks collection ID
- `COLLECTION_USER_SOCIAL_TASKS` - User social tasks collection ID
- `COLLECTION_ACHIEVEMENTS` - Achievements collection ID
- `COLLECTION_USER_ACHIEVEMENTS` - User achievements collection ID
- `COLLECTION_PRESALE_PURCHASES` - Presale purchases collection ID
- `COLLECTION_AD_VIEWS` - Ad views collection ID

## Environment Files

### Mobile App

1. `.env.production` - Production environment variables
2. `.env` - Development environment variables

### Admin Panel

1. `.env.example` - Example environment variables file
2. `.env.production` - Production environment variables template

## Deployment Instructions

### Mobile App

For production builds, the app will automatically use the values from `.env.production`. To create a production build:

```bash
npm run build:android
# or
npm run build:ios
```

### Admin Panel

For production deployment, create a `.env` file based on `.env.example` and populate it with your production values.

## Security Considerations

1. Never commit sensitive values (like API keys) to version control
2. Use different environment files for different deployments
3. Ensure proper CORS settings in Appwrite Console
4. Use HTTPS for all endpoints

## Testing

To verify the configuration is working correctly:

1. Run the validation script:
   ```bash
   npm run validate-appwrite
   ```

2. Test Appwrite connectivity:
   ```bash
   npm run test-appwrite
   ```

## Troubleshooting

If you encounter issues with the Appwrite connection:

1. Verify all environment variables are correctly set
2. Check that the Appwrite endpoint is accessible
3. Confirm the project ID and database ID are correct
4. Ensure the Appwrite Console has the correct platform configurations