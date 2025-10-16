# Appwrite Console Navigation Guide for Redirect URLs Setup

This guide provides step-by-step visual instructions for finding and configuring redirect URLs in the Appwrite Console.

## Overview

For the Ekehi Network mobile app, you need to add these redirect URLs:
- `ekehi://oauth/return`
- `ekehi://auth`

## Step-by-Step Navigation

### Step 1: Access Appwrite Console
1. Open your web browser
2. Go to [https://cloud.appwrite.io/console](https://cloud.appwrite.io/console)
3. Log in with your credentials

### Step 2: Select Your Project
1. From the dashboard, find your project with ID: `68c2dd6e002112935ed2`
2. Click on the project name to enter the project

### Step 3: Navigate to Authentication Section
Look for one of these navigation paths in the left sidebar:
- **Path A**: Authentication → Platforms
- **Path B**: Settings → Platforms
- **Path C**: Project Settings → Platforms
- **Path D**: Users & Teams → Platforms

### Step 4: Add Platform
1. Click the **"Add Platform"** or **"+ New Platform"** button (usually at the top right)
2. Select **"Flutter/React Native"** from the platform options
   - ⚠️ Do NOT select "Android" or "iOS" separately
   - ✅ Choose "Flutter/React Native" for React Native apps

### Step 5: Configure Platform Details
Fill in the following fields:
- **Platform Name**: `Ekehi Mobile App`
- **App ID/Bundle ID**: `com.ekehi.network`

### Step 6: Add Redirect URLs
This is the section you're looking for. Look for a field labeled:
- "Redirect URLs"
- "Allowed Redirect URLs"
- "Valid Redirect URLs"

In this field, add both URLs exactly as shown:
```
ekehi://oauth/return
ekehi://auth
```

Each URL should be on a separate line or separated by commas, depending on the Appwrite version.

### Step 7: Save Configuration
Click **"Register"** or **"Save"** to save the platform configuration.

## Visual Guide

### What You're Looking For
The redirect URLs section typically looks like one of these:

**Option 1: Text Area**
```
Redirect URLs:
[_______________________________________________]
ekehi://oauth/return
ekehi://auth
[_______________________________________________]
```

**Option 2: List Format**
```
Redirect URLs:
□ ekehi://oauth/return
□ ekehi://auth
[+ Add URL]
```

**Option 3: Comma-Separated**
```
Redirect URLs (comma separated):
[ ekehi://oauth/return, ekehi://auth           ]
```

## Common Locations by Appwrite Version

### Appwrite v1.x
- Left sidebar → Authentication → Platforms

### Appwrite v0.x
- Left sidebar → Settings → Platforms

### If You Can't Find It
1. Use the search function in the Appwrite Console
2. Look for "Platforms" or "Redirect" in the search bar
3. Check all menu items in the left sidebar

## Troubleshooting Navigation Issues

### Issue: Can't Find "Platforms" Section
**Solution**:
1. Expand all menu items in the left sidebar
2. Look for alternative names like:
   - "Applications"
   - "Clients"
   - "OAuth Clients"
   - "Mobile Apps"
3. Check under "Project Settings" if not under "Authentication"

### Issue: No "Flutter/React Native" Option
**Solution**:
1. Look for "Mobile" or "Other" platform type
2. If only Android/iOS options are available, choose either one
3. The important part is adding the correct redirect URLs

## Verification

After adding the platform:

1. You should see your new platform in the platforms list
2. The platform should show:
   - Name: Ekehi Mobile App
   - Type: Flutter/React Native (or Mobile)
   - Bundle ID: com.ekehi.network
   - Redirect URLs: Both URLs listed

## Testing the Configuration

1. Restart your development server:
   ```bash
   npm start --reset-cache
   ```

2. Test the OAuth flow in your app

3. Run the verification script:
   ```bash
   npm run verify-redirect-urls
   ```

## Need Help?

If you're still having trouble:

1. Take a screenshot of the Appwrite Console showing what you see
2. Check your Appwrite version in the console footer
3. Refer to the official Appwrite documentation for your version
4. Contact Appwrite community support

## Additional Resources

- [Appwrite Redirect URLs Setup](./APPWRITE_REDIRECT_URLS_SETUP.md)
- [Appwrite OAuth Configuration Fix](./APPWRITE_OAUTH_FIX.md)
- [Appwrite Mobile Platform Setup Guide](./APPWRITE_MOBILE_PLATFORM_SETUP.md)