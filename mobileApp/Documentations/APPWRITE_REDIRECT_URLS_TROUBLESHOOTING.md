# Appwrite Redirect URLs Troubleshooting Guide

This guide addresses the specific issue of not being able to find where to add redirect URLs in the React Native platform configuration in Appwrite Console.

## The Specific Problem

You're trying to add these redirect URLs but can't find the section in Appwrite Console:
```
ekehi://oauth/return
ekehi://auth
```

## Root Cause Analysis

The issue is typically one of these:

1. **Wrong Platform Type**: You're selecting Android/iOS instead of Flutter/React Native
2. **Navigation Confusion**: You're looking in the wrong section of Appwrite Console
3. **UI Changes**: Appwrite Console UI has changed between versions
4. **Missing Step**: You haven't completed the previous steps before reaching the redirect URLs section

## Detailed Troubleshooting Steps

### Step 1: Verify Platform Type Selection

**What you should see:**
When adding a new platform, you should see options like:
- Web
- Flutter/React Native ← SELECT THIS ONE
- Android
- iOS
- Other

**What you should NOT select:**
- Android only
- iOS only
- Multiple separate platforms

### Step 2: Complete Platform Details First

The redirect URLs section typically appears **AFTER** you've filled in the basic platform details:

1. Platform Name: `Ekehi Mobile App`
2. App ID/Bundle ID: `com.ekehi.network`
3. **THEN** the redirect URLs section appears

If you're not seeing the redirect URLs section, it's likely because:
- You haven't filled in the required fields above it
- You've selected the wrong platform type
- You're looking at the wrong part of the form

### Step 3: Look for These Visual Cues

The redirect URLs section usually looks like one of these:

#### Option A: Text Area (Most Common)
```
[ Platform Name     ] Ekehi Mobile App
[ App ID/Bundle ID ] com.ekehi.network

Redirect URLs:
[_______________________________________________]
ekehi://oauth/return
ekehi://auth
[_______________________________________________]
```

#### Option B: List Format
```
[ Platform Name     ] Ekehi Mobile App
[ App ID/Bundle ID ] com.ekehi.network

Redirect URLs:
□ [ ekehi://oauth/return                     ] [×]
□ [ ekehi://auth                            ] [×]
[+ Add URL]
```

#### Option C: Comma-Separated
```
[ Platform Name     ] Ekehi Mobile App
[ App ID/Bundle ID ] com.ekehi.network

Redirect URLs (comma separated):
[ ekehi://oauth/return, ekehi://auth           ]
```

### Step 4: Check for Form Validation

Some versions of Appwrite require you to:
1. Fill in the Platform Name
2. Fill in the App ID/Bundle ID
3. **Click "Continue" or "Next"** before the redirect URLs section appears

Look for buttons like:
- "Continue"
- "Next"
- "Proceed"
- "Validate"

### Step 5: Alternative Navigation Paths

If you can't find it in the standard locations, try these:

1. **Search Function**: Use the search bar in Appwrite Console for "redirect" or "platform"
2. **Different Menu Items**:
   - Users & Teams → Platforms
   - Applications → Platforms
   - Clients → Platforms
   - OAuth → Clients

## Version-Specific Instructions

### Appwrite Console v1.x
1. Left sidebar → Authentication → Platforms
2. Click "Add Platform"
3. Select "Flutter/React Native"
4. Fill in details
5. Redirect URLs section appears automatically

### Appwrite Console v0.x
1. Left sidebar → Settings → Platforms
2. Click "+ New Platform"
3. Select "Flutter/React Native"
4. Fill in details
5. Scroll down to find Redirect URLs

## Common Mistakes and Solutions

### Mistake 1: Selecting Wrong Platform Type
**Problem**: Selecting Android or iOS instead of Flutter/React Native
**Solution**: Go back and select "Flutter/React Native"

### Mistake 2: Incomplete Form
**Problem**: Redirect URLs section doesn't appear
**Solution**: Fill in all required fields first, then look for "Continue" button

### Mistake 3: Looking in Wrong Section
**Problem**: Can't find the redirect URLs input
**Solution**: Scroll down or look for a "Next" button after filling basic details

### Mistake 4: UI Confusion
**Problem**: Thinking the section is missing when it's just not visible yet
**Solution**: Complete previous steps, look for form validation or continuation buttons

## Alternative Approaches

If the standard platform configuration isn't working, see our detailed guide:
[APPWRITE_ALTERNATIVE_REDIRECT_URL_CONFIG.md](./APPWRITE_ALTERNATIVE_REDIRECT_URL_CONFIG.md)

This guide provides 11 alternative approaches including:
1. Checking OAuth provider settings
2. Looking in project settings
3. Using the search function
4. Trying different platform types
5. And more...

## Visual Guide with Screenshots

Since I can't provide actual screenshots, here's what to look for:

### What the Complete Form Should Look Like:
```
┌─────────────────────────────────────────────────┐
│              Add Platform                       │
├─────────────────────────────────────────────────┤
│ Platform Name:    [ Ekehi Mobile App      ]     │
│ Platform Type:    [ Flutter/React Native  ] ▼   │
│ App ID/Bundle ID: [ com.ekehi.network     ]     │
│                                                 │
│ Redirect URLs:                                  │
│ [ ekehi://oauth/return                        ] │
│ [ ekehi://auth                               ] │
│                                                 │
│                [ Cancel ] [ Register ]          │
└─────────────────────────────────────────────────┘
```

## If You Still Can't Find It

1. **Document What You See**: Take notes or a screenshot of exactly what you see
2. **Check Appwrite Version**: Look in the footer of the Appwrite Console
3. **Try Different Platform Types**: If Flutter/React Native isn't available, try "Other" or "Mobile"
4. **Contact Support**: Provide them with:
   - Your Appwrite version
   - Screenshots of what you see
   - The exact error message you're getting

## Verification Steps

After adding the platform:

1. You should see your new platform in the platforms list
2. Click on the platform to edit it
3. Verify both redirect URLs are listed:
   - `ekehi://oauth/return`
   - `ekehi://auth`

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

## Need More Help?

1. Check the detailed navigation guide:
   - [APPWRITE_CONSOLE_NAVIGATION_GUIDE.md](./APPWRITE_CONSOLE_NAVIGATION_GUIDE.md)

2. Review the OAuth configuration fix:
   - [APPWRITE_OAUTH_FIX.md](./APPWRITE_OAUTH_FIX.md)

3. Try alternative configuration approaches:
   - [APPWRITE_ALTERNATIVE_REDIRECT_URL_CONFIG.md](./APPWRITE_ALTERNATIVE_REDIRECT_URL_CONFIG.md)

4. Contact Appwrite community support with specific details about what you're seeing

## Additional Resources

- [Appwrite Official Documentation](https://appwrite.io/docs)
- [Appwrite Community Discord](https://appwrite.io/discord)
- [Appwrite GitHub Issues](https://github.com/appwrite/appwrite/issues)