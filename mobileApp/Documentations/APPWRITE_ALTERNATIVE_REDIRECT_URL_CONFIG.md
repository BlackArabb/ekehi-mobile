# Alternative Locations for Appwrite Redirect URL Configuration

This document provides alternative approaches and locations where you might find or configure redirect URLs in Appwrite Console if the standard platform configuration is not working.

## Approach 1: Check OAuth Provider Settings

Instead of (or in addition to) platform configuration, redirect URLs might be configured at the OAuth provider level.

### Steps:
1. In Appwrite Console, navigate to **Authentication** → **Providers**
2. Find the **Google** provider and click on it
3. Look for fields like:
   - "Redirect URL"
   - "Callback URL"
   - "Authorized Redirect URIs"
4. Add your URLs:
   ```
   ekehi://oauth/return
   ekehi://auth
   ```

## Approach 2: Check Project Settings

Some Appwrite versions have redirect URL configuration in project-level settings.

### Steps:
1. In Appwrite Console, navigate to **Project Settings**
2. Look for sections like:
   - "Security"
   - "Authentication Settings"
   - "OAuth Settings"
   - "Redirect Configuration"
3. Check if there's a global redirect URL section

## Approach 3: Check Application Settings

If "Platforms" isn't working, look for "Applications" or "Clients".

### Steps:
1. In Appwrite Console, look for menu items like:
   - "Applications"
   - "Clients"
   - "OAuth Clients"
   - "Mobile Apps"
2. Try adding your application there
3. Look for redirect URL configuration in the application settings

## Approach 4: Use the Search Function

Appwrite Console has a search feature that might help you find the right section.

### Steps:
1. Look for a search bar in the Appwrite Console
2. Search for terms like:
   - "redirect"
   - "callback"
   - "oauth"
   - "url"
   - "platform"
3. Review the search results for relevant sections

## Approach 5: Check All Menu Items

Sometimes the configuration is in an unexpected location.

### Steps:
1. Expand all menu items in the left sidebar
2. Check every section for:
   - Settings
   - Security
   - Authentication
   - Users & Teams
   - Integrations
   - API
   - Functions (if applicable)

## Approach 6: Create Platform Without Redirect URLs First

If the redirect URLs section isn't appearing, try creating the platform without them first.

### Steps:
1. Add Platform → Select "Flutter/React Native"
2. Fill in:
   - Platform Name: Ekehi Mobile App
   - App ID/Bundle ID: com.ekehi.network
3. Leave redirect URLs empty and save
4. Edit the platform afterward to add redirect URLs

## Approach 7: Try Different Platform Types

If "Flutter/React Native" doesn't show the redirect URLs section, try other options.

### Steps:
1. Try "Mobile" platform type
2. Try "Other" platform type
3. Try "Web" platform type (even though it's a mobile app)
4. After creating the platform, check if you can edit it to add redirect URLs

## Approach 8: Check for Multi-Step Forms

Some Appwrite versions use multi-step forms where redirect URLs appear on a second screen.

### Steps:
1. Add Platform → Select "Flutter/React Native"
2. Fill in basic details
3. Look for "Continue", "Next", or "Proceed" buttons
4. Click the button to advance to the next step
5. Look for redirect URLs on the next screen

## Approach 9: Browser Developer Tools

Use browser developer tools to inspect the page and find hidden or JavaScript-controlled elements.

### Steps:
1. Open browser developer tools (F12)
2. Go to the Elements/Inspector tab
3. Search for:
   - "redirect"
   - "oauth"
   - "url"
   - "ekehi"
4. Look for hidden form fields or JavaScript variables

## Approach 10: Check API Documentation

Appwrite might allow setting redirect URLs via API calls.

### Steps:
1. Check Appwrite API documentation for platform management
2. Look for endpoints like:
   - `POST /platforms`
   - `PUT /platforms/{platformId}`
3. Check if redirect URLs can be set via API

## Approach 11: Contact Appwrite Support

If none of the above approaches work, reach out to Appwrite support.

### Information to Provide:
1. Your Appwrite version (check footer of console)
2. Screenshots of what you see
3. Description of what you're trying to accomplish
4. Error messages you're receiving

## Testing Alternative Configurations

After trying any of the above approaches:

1. Restart your development server:
   ```bash
   npm start --reset-cache
   ```

2. Test the OAuth flow in your app

3. Run the verification script:
   ```bash
   npm run verify-redirect-urls
   ```

## Additional Resources

- [Appwrite Official Documentation](https://appwrite.io/docs)
- [Appwrite Community Discord](https://appwrite.io/discord)
- [Appwrite GitHub Issues](https://github.com/appwrite/appwrite/issues)

## Need More Help?

If you're still unable to find where to configure redirect URLs:

1. Take detailed screenshots of your Appwrite Console
2. Note your Appwrite version from the console footer
3. Document exactly what steps you've tried
4. Reach out to Appwrite community support with this information

The key is that redirect URLs are essential for OAuth to work properly, so they must be configured somewhere in the Appwrite Console. If the standard platform configuration isn't working, one of these alternative approaches should help you find the right location.