# Start.io Integration Fix

This document explains the fix for the Start.io integration issues in the Ekehi Mobile application.

## Problem

The Start.io integration was failing with the following errors:
- `[TypeError: Cannot read property 'init' of null]`
- `[TypeError: Cannot read property 'showAd' of null]`

These errors occurred because the module was not being imported and accessed correctly.

## Root Cause

The issue was in the `StartIoService.ts` file where:
1. The entire module was being imported instead of individual functions
2. The code was trying to access functions as properties of the module object
3. The module object was null, causing the "Cannot read property of null" errors

## Solution

The fix involved updating the import and usage pattern to match the [@kastorcode/expo-startio](https://github.com/kastorcode/expo-startio) package documentation:

### Before (Incorrect):
```typescript
const startIoModule = require('@kastorcode/expo-startio');
StartIoModule = startIoModule;
// Then trying to use:
await StartIoModule.initStartio(this.appId, __DEV__); // This failed
```

### After (Correct):
```typescript
const startIoModule = require('@kastorcode/expo-startio');
initStartio = startIoModule.initStartio;
showAdStartio = startIoModule.showAdStartio;
setSecondsBetweenAdsStartio = startIoModule.setSecondsBetweenAdsStartio;
setActivitiesBetweenAdsStartio = startIoModule.setActivitiesBetweenAdsStartio;
// Then using directly:
await initStartio(this.appId, __DEV__); // This works
```

## Changes Made

1. **Updated imports**: Import individual functions instead of the entire module
2. **Fixed function calls**: Use the imported functions directly instead of accessing them as properties
3. **Added null checks**: Check if each function exists before calling it
4. **Updated all references**: Fixed all usages throughout the service

## Files Modified

1. `src/services/StartIoService.ts` - Main fix
2. `package.json` - Added test script
3. `Scripts/test-startio.js` - Test script to verify integration

## Testing

To test the fix:

1. Run the app on an Android device (Start.io only works on Android)
2. Check the console logs for successful initialization messages:
   - `[StartIoService] Start.io module loaded successfully`
   - `[StartIoService] Initialized successfully with App ID: 209257659`
3. Try watching an ad from the "Watch Ad for Bonus" button
4. Try signing out to trigger the exit ad

## Expected Behavior

After the fix:
- Start.io should initialize without errors
- Ads should show when requested
- Exit ads should show when signing out
- No more "Cannot read property of null" errors

## Troubleshooting

If you still encounter issues:

1. Verify the package is installed: `npm list @kastorcode/expo-startio`
2. Check that you're testing on an Android device
3. Ensure you have an active internet connection
4. Check the Start.io dashboard to verify your App ID (209257659) is active
5. Look for any new error messages in the console logs

## References

- [@kastorcode/expo-startio GitHub Repository](https://github.com/kastorcode/expo-startio)
- [Start.io Developer Portal](https://www.start.io/)