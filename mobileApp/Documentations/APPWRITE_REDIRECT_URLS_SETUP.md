# Appwrite Redirect URLs Setup Guide

This document specifically addresses the issue of where to add the redirect URLs in Appwrite Console for the Ekehi Network mobile application.

## The Problem

You're seeing an error because Appwrite requires explicit registration of all redirect URLs that your OAuth flow will use, but you can't find where to add these URLs in the Appwrite Console.

## Solution Overview

For React Native applications like Ekehi Network, you need to register a **Flutter/React Native** platform in Appwrite (not separate Android and iOS platforms) and add the redirect URLs there.

## Step-by-Step Instructions

### 1. Access Appwrite Console
1. Go to [Appwrite Console](https://cloud.appwrite.io/console)
2. Log in with your credentials
3. Select your project (`68c2dd6e002112935ed2`)

### 2. Navigate to Platform Settings
The exact location may vary depending on your Appwrite version:

- **Option 1**: Left sidebar → "Authentication" → "Platforms"
- **Option 2**: Left sidebar → "Settings" → "Platforms"
- **Option 3**: Left sidebar → "Project Settings" → "Platforms"

### 3. Add Flutter/React Native Platform
1. Click the **"Add Platform"** or **"+ New Platform"** button
2. Select **"Flutter/React Native"** as the platform type
   - ⚠️ **IMPORTANT**: Do NOT select separate Android and iOS platforms
   - ✅ Use "Flutter/React Native" platform type for React Native apps

### 4. Configure Platform Details
Fill in the following information:

- **Platform Name**: `Ekehi Mobile App`
- **App ID/Bundle ID**: `com.ekehi.network`
- **Redirect URLs**: Add both URLs exactly as shown:
  ```
  ekehi://oauth/return
  ekehi://auth
  ```

### 5. Save Configuration
Click **"Register"** or **"Save"** to save the platform configuration.

## Required Redirect URLs

You must add these exact URLs:
1. `ekehi://oauth/return`
2. `ekehi://auth`

These URLs match the deep linking configuration in your [app.json](file://c:\ekehi-mobile\mobileApp\app.json) file.

## Why Flutter/React Native Platform?

For React Native applications, Appwrite recommends using the Flutter/React Native platform type rather than separate Android and iOS platforms because:

1. It simplifies configuration management
2. It ensures consistent OAuth behavior across platforms
3. It reduces the chance of configuration errors
4. It's the officially recommended approach for React Native apps

## Verification

After adding the platform:

1. Restart your development server:
   ```bash
   npm start --reset-cache
   ```

2. Test the OAuth flow in your app

3. Run the verification script:
   ```bash
   npm run verify-redirect-urls
   ```

## Troubleshooting

### If you still see "Invalid URL" errors:

1. Double-check that you selected **"Flutter/React Native"** as the platform type
2. Verify that both redirect URLs are added exactly:
   - `ekehi://oauth/return`
   - `ekehi://auth`
3. Ensure there are no extra spaces or characters
4. Confirm the Bundle ID is exactly `com.ekehi.network`

### If you can't find the Platforms section:

Different Appwrite versions may have different navigation:
- Try all the navigation options listed above
- Look for "Users & Teams" → "Platforms"
- Check the search function in the Appwrite Console

### If OAuth still doesn't work:

1. Verify your Google OAuth provider is enabled
2. Confirm your Google Client IDs are correct
3. Check that your [app.json](file://c:\ekehi-mobile\mobileApp\app.json) has the correct deep linking configuration
4. Run the OAuth configuration test:
   ```bash
   npm run test-oauth-config
   ```

## Additional Resources

- [Appwrite OAuth Configuration Fix](./APPWRITE_OAUTH_FIX.md)
- [Appwrite Mobile Platform Setup Guide](./APPWRITE_MOBILE_PLATFORM_SETUP.md)
- [Appwrite OAuth Configuration Guide](./APPWRITE_OAUTH_CONFIGURATION.md)

## Need Help?

If you're still having trouble:

1. Check your Appwrite version in the console footer
2. Refer to the official Appwrite documentation for your version
3. Contact Appwrite community support
4. Review the detailed documentation files mentioned above