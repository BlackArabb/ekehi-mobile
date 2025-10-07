# Mobile OAuth Setup Guide

This guide explains how to properly configure the OAuth system for the Ekehi Network mobile app.

## How the Mobile OAuth System Works

1. User taps "Continue with Google" in the mobile app
2. App generates an OAuth URL with deep link redirect URLs
3. App opens OAuth flow in browser
4. User completes Google authentication
5. Google redirects back to Appwrite
6. Appwrite redirects to the deep link URL with authentication parameters
7. Mobile app receives the deep link and processes authentication

## Required Configuration

### 1. Appwrite Platform Configuration

In your Appwrite Console, you need to configure these platforms:

#### Android Platform
- **Type**: Android
- **Name**: Ekehi Network Android
- **Package Name**: `com.ekehi.network`
- **Redirect URLs**: 
  - `ekehi://oauth/return`
  - `ekehi://auth`

#### iOS Platform
- **Type**: iOS
- **Name**: Ekehi Network iOS
- **Bundle ID**: `com.ekehi.network`
- **Redirect URLs**: 
  - `ekehi://oauth/return`
  - `ekehi://auth`

#### Web Platform (for hosted OAuth)
- **Type**: Web
- **Name**: Ekehi OAuth Web
- **Hostname**: `ekehi-oauth.netlify.app`

### 2. Google OAuth Configuration

In Google Cloud Console, configure your OAuth clients:

#### Web Application Client
- **Authorized JavaScript origins**:
  - `https://ekehi-oauth.netlify.app`
- **Authorized redirect URIs**:
  - `https://fra.cloud.appwrite.io/v1/account/sessions/oauth2/callback/google/68c2dd6e002112935ed2`
  - `https://ekehi-oauth.netlify.app/oauth/return`
  - `https://ekehi-oauth.netlify.app/auth`

### 3. Appwrite Google OAuth Provider

In Appwrite Console > Authentication > Settings:
- Enable Google OAuth provider
- Enter your Google OAuth Web Client ID and Secret
- Save the configuration

## Deep Link Handling

The mobile app expects to receive authentication parameters through deep links:

- **Success URL**: `ekehi://oauth/return`
- **Failure URL**: `ekehi://auth`

When using the hosted OAuth system, after successful authentication, it should redirect to:
`ekehi://oauth/return?userId=USER_ID&secret=SECRET`

## Troubleshooting

### If the app doesn't redirect back automatically:

1. Ensure deep links are properly configured in [app.json](file://c:\Users\ARQAM%20TV\Downloads\mobile\app.json)
2. Verify Appwrite platform configurations include the correct redirect URLs
3. Check that the hosted OAuth system is properly configured to redirect with the correct parameters

### If "Check OAuth Status" doesn't work:

1. Make sure you've completed the OAuth flow in the hosted system
2. Ensure you've returned to the app manually if automatic redirect didn't work
3. Try refreshing the auth status multiple times with delays between attempts

### If you get "Invalid URL" errors:

1. Verify all platform configurations in Appwrite
2. Check that Google OAuth client is properly configured
3. Ensure redirect URIs match exactly what's configured in Appwrite and Google Cloud Console

## Testing the Flow

1. Tap "Continue with Google" in the app
2. Complete authentication on the hosted system
3. You should be automatically redirected back to the app
4. If not redirected, return to the app manually
5. Tap "Check OAuth Status" to verify authentication

## Manual Verification

If automatic redirect isn't working:

1. After completing authentication on the hosted system, note the `userId` and `secret` parameters
2. Return to the app manually
3. Tap "Check OAuth Status"
4. The app should detect the session and log you in

This OAuth system eliminates the need for complex mobile OAuth configurations while providing a seamless user experience.