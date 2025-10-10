# Mobile OAuth Setup Guide

This document explains how to set up Google OAuth 2.0 authentication for the Ekehi mobile app through the landing website.

## Overview

The OAuth flow works as follows:
1. User clicks "Sign in with Google" on the website
2. User is redirected to Google's OAuth endpoint
3. After authentication, Google redirects back to our OAuth callback handler
4. The callback handler processes the authentication and redirects to the mobile app
5. The mobile app receives the authentication data and completes the login

## URL Structure

### OAuth Initiation
```
https://fra.cloud.appwrite.io/v1/account/tokens/oauth2/google?project=68c2dd6e002112935ed2&success=http%3A%2F%2Fekehi.xyz%2Foauth%2Fcallback&failure=http%3A%2F%2Fekehi.xyz%2Foauth%2Fcallback%3Ferror%3Dauth_failed
```

### Success Redirect
```
http://ekehi.xyz/oauth/callback?code=AUTH_CODE&state=STATE
```

### Failure Redirect
```
http://ekehi.xyz/oauth/callback?error=ERROR_DESCRIPTION
```

### Mobile App Deep Link
```
ekehi://oauth/return?code=AUTH_CODE&state=STATE
```

## Implementation Details

### 1. Mobile Authentication Page
Located at: `/mobile-auth`

This page provides users with options to authenticate either through the mobile app or web interface.

Key features:
- Google OAuth button that redirects to Appwrite
- Clear instructions for mobile app users
- Links to download the mobile app

### 2. OAuth Callback Handler
Located at: `/oauth/callback`

This page handles the OAuth response from Google and redirects to the mobile app.

Key features:
- Processes OAuth code from Google
- Detects if user is on mobile device
- Redirects to mobile app using deep linking
- Provides fallback instructions if redirect fails

### 3. Mobile App Deep Linking
The mobile app must be configured to handle the deep link:
```
ekehi://oauth/return
```

All query parameters from the OAuth callback are passed through to the mobile app.

## Configuration Requirements

### Appwrite Configuration
1. Ensure the Appwrite project (`68c2dd6e002112935ed2`) has Google OAuth configured
2. Add the following redirect URLs in Appwrite OAuth settings:
   - Success URL: `http://ekehi.xyz/oauth/callback`
   - Failure URL: `http://ekehi.xyz/oauth/callback?error=auth_failed`

### Mobile App Configuration
1. Configure the mobile app to handle deep links with scheme `ekehi`
2. Register the path `/oauth/return` to handle OAuth responses
3. Implement OAuth token exchange in the mobile app

### Web Server Configuration
1. Ensure the web server properly handles the `/oauth/callback` route
2. Configure CORS settings if needed for OAuth redirects

## Testing the Flow

### Test on Desktop
1. Navigate to `http://ekehi.xyz/mobile-auth`
2. Click "Sign in with Google"
3. Complete Google authentication
4. Verify you're redirected to `http://ekehi.xyz/oauth/callback`
5. Check that instructions for opening the mobile app are displayed

### Test on Mobile
1. Navigate to `http://ekehi.xyz/mobile-auth` on a mobile device
2. Click "Sign in with Google"
3. Complete Google authentication
4. Verify the mobile app opens automatically
5. Check that authentication is completed in the app

## Troubleshooting

### Common Issues

1. **Redirect Loop**
   - Ensure Appwrite redirect URLs exactly match the configured URLs
   - Check that the project ID is correct

2. **Mobile App Not Opening**
   - Verify deep linking is properly configured in the mobile app
   - Check that the mobile app scheme matches `ekehi`

3. **Authentication Fails**
   - Confirm Google OAuth is properly configured in Appwrite
   - Check that the Google OAuth client credentials are correct

### Debugging Steps

1. Check browser console for JavaScript errors
2. Verify network requests to Appwrite OAuth endpoint
3. Confirm deep link handling in mobile app
4. Review Appwrite project OAuth settings

## Security Considerations

1. OAuth tokens are never exposed to the frontend
2. All OAuth communication happens through Appwrite
3. Deep links use secure custom URL schemes
4. State parameters are used to prevent CSRF attacks

## Future Improvements

1. Add support for other OAuth providers (Apple, Twitter, etc.)
2. Implement QR code based authentication for easier mobile setup
3. Add analytics tracking for OAuth flow completion rates
4. Implement progressive web app features for better mobile experience