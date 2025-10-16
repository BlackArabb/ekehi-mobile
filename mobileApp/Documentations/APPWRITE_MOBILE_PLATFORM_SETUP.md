# Appwrite Mobile Platform Setup for OAuth

This guide will help you configure your Appwrite project to properly handle OAuth redirects for the mobile app.

## Prerequisites

- Access to your Appwrite Console
- Your project should already be created
- Your app's bundle ID (e.g., com.ekehi.network)

## Configuration Steps

### 1. Access Your Project
1. Log in to your Appwrite Console
2. Select the project for your Ekehi Network app

### 2. Navigate to Platform Settings
Depending on your Appwrite version, the platform settings can be found in one of these locations:
- **Option A**: Left sidebar → "Settings" → "Platforms"
- **Option B**: Left sidebar → "Authentication" → "Platforms"  
- **Option C**: Left sidebar → "Project Settings" → "Platforms"
- **Option D**: Left sidebar → "Users & Teams" → "Platforms"

### 3. Add a New Mobile Platform
1. Click the "Add Platform" or "+ New Platform" button
2. Select "Flutter/React Native" as the platform type (IMPORTANT: Do not use separate Android and iOS platforms)
3. Fill in the required information:
   - **Platform Name**: Ekehi Mobile App
   - **App ID/Bundle ID**: com.ekehi.network
   - **Redirect URLs**: Add both of the following URLs:
     ```
     ekehi://oauth/return
     ekehi://auth
     ```

### 4. Configure OAuth Providers
1. Navigate to "Authentication" → "Providers" in the left sidebar
2. Enable the Google OAuth provider
3. Add your Google OAuth Client ID and Secret
4. Make sure the redirect URLs are properly configured for the provider

### 5. Save and Test
1. Save all your changes
2. Restart your development server
3. Test the OAuth flow in your app

## Troubleshooting

### If you can't find the Platforms section:
- Check all menu items in the left sidebar
- Look for "Project Settings" or "Authentication"
- In some versions, it might be under "Users" or "Security"

### If OAuth still doesn't work:
1. Verify that the scheme in your app.json matches the redirect URLs
2. Make sure you've added the intent filter in your app.json:
   ```json
   {
     "expo": {
       "scheme": "ekehi",
       "android": {
         "intentFilters": [
           {
             "action": "VIEW",
             "data": [
               {
                 "scheme": "ekehi",
                 "host": "oauth",
                 "pathPrefix": "/return"
               }
             ],
             "category": ["BROWSABLE", "DEFAULT"]
           }
         ]
       }
     }
   }
   ```
3. Clear your development server cache and restart

## Common Issues and Solutions

### Issue: "Invalid URI" Error
**Solution**: Make sure the redirect URLs exactly match what you've configured in Appwrite:
- `ekehi://oauth/return`
- `ekehi://auth`

### Issue: Platform not found
**Solution**: Double-check that you've added the platform with the correct bundle ID and redirect URLs.
**IMPORTANT**: Use "Flutter/React Native" as the platform type, not separate Android and iOS platforms.

## Need Help?

If you're still having trouble finding the platform configuration section:
1. Check your Appwrite version in the console footer
2. Refer to the Appwrite documentation for your specific version
3. Consider reaching out to Appwrite community support