# Mobile App OAuth Callback Handler

## Purpose

This page (`/oauth/callback`) serves as the OAuth 2.0 callback handler for users signing up through the Ekehi mobile app using the "Continue with Google" button. It is accessed via deep linking and not through website navigation.

## How It Works

1. User taps "Continue with Google" in the mobile app
2. Mobile app redirects user to Appwrite OAuth endpoint
3. User authenticates with Google
4. Google redirects back to this callback page at `http://ekehi.xyz/oauth/callback`
5. This page processes the OAuth response and redirects to the mobile app via deep linking
6. Mobile app receives the OAuth data and completes the sign-in process

## URL Structure

The callback URL should be configured in your Appwrite project as:
```
http://ekehi.xyz/oauth/callback
```

All OAuth parameters (code, state, error, etc.) are passed through to the mobile app via deep linking.

## Deep Linking

The page constructs a deep link URL in the format:
```
ekehi://oauth/return?[all OAuth parameters]
```

For example:
```
ekehi://oauth/return?code=4/0AXRx-gdi67F8...&scope=email%20profile&state=...
```

## Implementation Details

The page automatically:
1. Parses OAuth parameters from the URL
2. Checks for errors in the OAuth flow
3. Constructs and executes a deep link to the mobile app
4. Provides user feedback during the process
5. Shows instructions if the automatic redirect fails

## Testing

To test the OAuth flow:
1. Ensure your Appwrite project is configured with the correct redirect URL
2. Access the callback page directly with OAuth parameters:
   ```
   http://ekehi.xyz/oauth/callback?code=TEST_CODE&state=TEST_STATE
   ```
3. Verify that the deep link is constructed correctly
4. Confirm that the mobile app can handle the deep link

## Error Handling

The page handles various error scenarios:
- OAuth errors from Google (displayed to user)
- Deep link failures (provides manual instructions)
- General redirect issues (shows fallback message)

## Security

- OAuth tokens are never exposed to the frontend
- All communication happens through secure Appwrite OAuth endpoints
- State parameters are preserved to prevent CSRF attacks