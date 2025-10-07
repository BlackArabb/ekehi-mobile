# Complete OAuth Setup Guide

This guide will help you resolve the "Invalid URL" error and properly configure OAuth for your Ekehi Network mobile app.

## Prerequisites

- Appwrite Project ID: `68c2dd6e002112935ed2`
- Google OAuth Client ID: `842046112756-rk2jcdf9l7f4cbh136u4tm591qmtl6bq.apps.googleusercontent.com`
- Hosted OAuth System: `https://ekehi-oauth.netlify.app`

## Step 1: Configure Web Platform in Appwrite

1. Log in to your Appwrite Console
2. Select your project
3. Navigate to **Auth** > **Settings**
4. Scroll down to **Platforms** section
5. Click **Add Platform**
6. Select **Web** platform type
7. Fill in the details:
   - **Name**: Ekehi OAuth
   - **Hostname**: `ekehi-oauth.netlify.app`
8. Click **Register**

## Step 2: Configure Google OAuth Provider

1. In the same **Auth** section, click on **Google**
2. Enable the provider
3. Enter your credentials:
   - **Client ID**: `842046112756-rk2jcdf9l7f4cbh136u4tm591qmtl6bq.apps.googleusercontent.com`
   - **Client Secret**: [Your Google OAuth Client Secret]
4. Click **Update**

## Step 3: Add Redirect URI in Google Cloud Console

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

## Step 4: Verify Configuration

1. Restart your mobile app
2. Try the "Continue with Google" option
3. The "Invalid URL" error should be resolved

## Troubleshooting

### If you still see "Invalid URL" error:

1. Double-check the hostname is exactly `ekehi-oauth.netlify.app` (no trailing slash)
2. Ensure the Web platform is properly registered
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

- The Web platform configuration is mandatory, not optional
- The redirect URI must match exactly what Appwrite expects
- Your hosted OAuth system at `ekehi-oauth.netlify.app` is correctly implemented
- No changes are needed to your hosted OAuth system code