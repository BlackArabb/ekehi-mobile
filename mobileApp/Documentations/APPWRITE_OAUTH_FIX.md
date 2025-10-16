# Appwrite OAuth Configuration Fix

## Problem
When clicking the Google OAuth button, you're getting the following error:
```
Error 400: Invalid success param: Invalid URI. Register your new client (oauth) as a new platform on your project console dashboard
```

## Root Cause
The issue is not with your code implementation but with the Appwrite project configuration. The redirect URLs `ekehi://oauth/return` and `ekehi://auth` need to be registered in your Appwrite project as valid redirect URLs for a mobile platform.

## Solution

### Step 1: Register Platforms in Appwrite Console
1. Log into your Appwrite Console
2. Select your project
3. Navigate to the "Platforms" section (see detailed instructions below if you can't find it)
4. Click "Add Platform" 
5. Select "Flutter/React Native" as the platform type (IMPORTANT: Use Flutter/React Native, not separate Android and iOS platforms)
6. Fill in the platform details:
   - **Platform Name**: Ekehi Mobile App
   - **App ID/Bundle ID**: com.ekehi.network
7. Add the following redirect URLs:
   ```
   ekehi://oauth/return
   ekehi://auth
   ```
8. Save the platform configuration

### Step 2: Configure OAuth Provider
1. Navigate to "Authentication" → "Providers" in the left sidebar
2. Find and enable the Google OAuth provider
3. Add your Google OAuth Client ID and Secret
4. Save the configuration

### Step 3: Verify Configuration
Run the test script to verify your OAuth configuration:
```bash
npm run test-oauth-config
```

### Step 4: Clear Cache and Restart
```bash
npm start --reset-cache
```

## Detailed Platform Setup Instructions

For step-by-step visual guidance on finding and configuring the platform settings in Appwrite Console, please refer to our detailed guide:
[Appwrite Mobile Platform Setup Guide](./APPWRITE_MOBILE_PLATFORM_SETUP.md)

## Troubleshooting

### If you still get the error:
1. Double-check that the redirect URLs exactly match:
   - `ekehi://oauth/return`
   - `ekehi://auth`
2. Verify your App ID/Bundle ID matches what's in your app.json
3. Make sure you've enabled the Google OAuth provider
4. Confirm your Google OAuth Client ID and Secret are correct
5. IMPORTANT: Ensure you've selected "Flutter/React Native" as the platform type, not separate Android and iOS platforms

### If you can't find the Platforms section:
Different versions of Appwrite may have the platform settings in different locations:
- Try looking under "Settings" → "Platforms"
- Or "Authentication" → "Platforms"
- Or "Project Settings" → "Platforms"
- Or "Users & Teams" → "Platforms"

The important thing is to find where you can add a new mobile platform and configure redirect URLs.
