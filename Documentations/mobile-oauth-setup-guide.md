# Mobile OAuth Setup Guide

This guide will help you resolve the "Invalid URL" error and properly configure OAuth for your Ekehi Network mobile app.

## Prerequisites

- Appwrite Project ID: `68c2dd6e002112935ed2`
- Google OAuth Client ID: `842046112756-rk2jcdf9l7f4cbh136u4tm591qmtl6bq.apps.googleusercontent.com`
- Mobile App Bundle ID: `com.ekehi.network`
- Deep Link URLs: 
  - Success: `ekehi://oauth/callback`
  - Failure: `ekehi://auth`
- Hosted OAuth System: `https://ekehi-oauth.netlify.app`

## Step 1: Configure Flutter/React Native Platform in Appwrite

1. Log in to your Appwrite Console
2. Select your project
3. Navigate to **Auth** > **Settings**
4. Scroll down to **Platforms** section
5. Click **Add Platform**
6. Select **Flutter/React Native** platform type
7. Fill in the details:
   - **Name**: Ekehi Network Mobile
   - **App ID/Bundle ID**: `com.ekehi.network`
   - **Redirect URLs**:
     ```
     ekehi://oauth/callback
     ekehi://auth
     ```
8. Click **Register**

## Step 2: Configure Web Platform in Appwrite (for hosted OAuth)

1. In the same **Auth** > **Settings** section, click **Add Platform**
2. Select **Web** platform type
3. Fill in the details:
   - **Name**: Ekehi OAuth Web
   - **Hostname**: `ekehi-oauth.netlify.app`
4. Click **Register**

## Step 3: Configure Google OAuth Provider

1. In the same **Auth** section, click on **Google**
2. Enable the provider if not already enabled
3. Enter your credentials:
   - **Client ID**: `842046112756-rk2jcdf9l7f4cbh136u4tm591qmtl6bq.apps.googleusercontent.com`
   - **Client Secret**: [Your Google OAuth Client Secret]
4. Click **Update**

## Step 4: Add Redirect URI in Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project
3. Navigate to **APIs & Services** > **Credentials**
4. Find your OAuth 2.0 Client ID
5. Click the edit icon (pencil)
6. In **Authorized redirect URIs**, add:
   ```
   https://fra.cloud.appwrite.io/v1/account/sessions/oauth2/callback/google/68c2dd6e002112935ed2
   ```
7. Click **Save**

## Step 5: Verify Deep Link Configuration

Ensure your [app.json](file://c:\Users\ARQAM%20TV\Downloads\mobile\app.json) has the correct deep linking configuration:

```json
{
  "expo": {
    "scheme": "ekehi",
    "android": {
      "package": "com.ekehi.network"
    },
    "ios": {
      "bundleIdentifier": "com.ekehi.network"
    }
  }
}
```

## Step 6: Test the Configuration

1. Restart your mobile app
2. Try the "Continue with Google" option
3. The "Invalid URL" error should be resolved

## Troubleshooting

### If you still see "Invalid URL" error:

1. Double-check that you've added BOTH platforms:
   - Flutter/React Native with Bundle ID `com.ekehi.network`
   - Web with Hostname `ekehi-oauth.netlify.app`
2. Ensure all redirect URLs are exactly as specified
3. Verify Google OAuth provider is enabled
4. Confirm the redirect URI is added in Google Cloud Console

### If OAuth opens but doesn't complete:

1. Check that your hosted OAuth system is deployed and accessible
2. Verify the success page properly handles [userId](file://c:\Users\ARQAM%20TV\Downloads\mobile\src\types\index.ts#L2-L2) and `secret` parameters
3. Ensure users return to the app and tap "Check OAuth Status"

## Testing

After completing the setup:

1. Use the test account:
   - Email: `test@ekehi.network`
   - Password: `testpassword123`
2. Or try "Continue with Google" after configuration

## Important Notes

- The Flutter/React Native platform configuration is mandatory for mobile OAuth
- The Web platform configuration is required for hosted OAuth
- Both platforms must be configured for full OAuth functionality
- The redirect URIs must match exactly what the app expects
- Your hosted OAuth system at `ekehi-oauth.netlify.app` is correctly implemented
- No changes are needed to your hosted OAuth system code