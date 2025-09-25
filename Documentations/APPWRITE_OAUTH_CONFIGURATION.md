# Appwrite OAuth Configuration Guide

This guide will help you resolve the "Invalid URL" error by properly configuring OAuth in your Appwrite project.

## Root Cause

The "Invalid URL" error occurs because Appwrite requires explicit registration of all redirect URLs that your OAuth flow will use. When you try to initiate OAuth with URLs that aren't registered in Appwrite, you get this error.

## Solution

You need to configure platforms in your Appwrite Console. Here's exactly what you need to do:

## Step 1: Configure Flutter/React Native Platform (for mobile)

1. Log in to your [Appwrite Console](https://cloud.appwrite.io/console)
2. Select your project (`68c2dd6e002112935ed2`)
3. Navigate to **Auth** > **Settings**
4. Scroll down to **Platforms** section
5. Click **Add Platform**
6. Select **Flutter/React Native** platform type
7. Fill in the details:
   - **Name**: Ekehi OAuth
   - **App ID/Bundle ID**: `com.ekehi.network`
   - **Redirect URLs**:
     ```
     ekehi://oauth/return
     ekehi://auth
     ekehi://verify-email
     ekehi://forgot-password
     ekehi://reset-password
     ```
8. Click **Register**

## Step 2: Configure Web Platform (for hosted OAuth)

1. In the same **Auth** > **Settings** section, click **Add Platform**
2. Select **Web** platform type
3. Fill in the details:
   - **Name**: Ekehi OAuth Web
   - **Hostname**: `ekehi-oauth.netlify.app`
4. Click **Register**

## Step 3: Configure Google OAuth Provider

1. In the same **Auth** section, find the **Google** provider
2. Click the toggle switch to enable it
3. Enter your credentials:
   - **Client ID**: Your Google OAuth Client ID
   - **Client Secret**: Your Google OAuth Client Secret
4. Click **Update**

## Verification

After completing these steps:

1. Restart your mobile app
2. Try the "Continue with Google" option again
3. The "Invalid URL" error should be resolved

## Troubleshooting

### If you still see "Invalid URL" error:

1. Double-check that you've added BOTH platforms:
   - Flutter/React Native with Bundle ID `com.ekehi.network`
   - Web with Hostname `ekehi-oauth.netlify.app`
2. Ensure all redirect URLs are exactly as specified (no extra spaces or characters)
3. Verify Google OAuth provider is enabled
4. Make sure you clicked "Update" after entering Google OAuth credentials

### If OAuth opens but doesn't complete:

1. Check that your hosted OAuth system is deployed and accessible
2. Verify the success page properly handles userId and secret parameters
3. Ensure users return to the app and tap "Check OAuth Status"

## Important Notes

- Both platforms (Flutter/React Native and Web) must be configured
- The redirect URLs must match exactly what the app expects
- Your hosted OAuth system at `ekehi-oauth.netlify.app` should be correctly implemented
- No changes are needed to your mobile app code