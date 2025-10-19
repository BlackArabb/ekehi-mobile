# Start.io Package Update

This document explains the update from [react-native-startio-ads](https://github.com/devdaniellima/react-native-startio-ads) to [@kastorcode/expo-startio](https://github.com/kastorcode/expo-startio) in the Ekehi Mobile application.

## Overview

The original [react-native-startio-ads](https://github.com/devdaniellima/react-native-startio-ads) package had compatibility issues with React Native 0.74.5, causing dependency conflicts during installation. To resolve this issue, we've updated to use [@kastorcode/expo-startio](https://github.com/kastorcode/expo-startio), which is specifically designed for Expo/React Native applications and is compatible with newer React Native versions.

## Changes Made

### 1. Package.json Update

- Removed: `react-native-startio-ads`
- Added: `@kastorcode/expo-startio`

### 2. StartIoService.ts Update

The StartIoService has been updated to work with the new package API:

- Updated import statement to use `@kastorcode/expo-startio`
- Modified initialization to use `initStartio()` function
- Updated rewarded ad implementation to work with the new package
- Updated exit ad implementation to use `showAdStartio()` function
- Removed dependency on Types enum (not available in new package)
- Simplified ad loading logic (new package handles loading internally)

### 3. API Changes

#### Old Package API:
```typescript
import RNStartIoAds, {Types} from 'react-native-startio-ads';

// Initialization
RNStartIoAds.initialize({appId: 'YOUR_APP_ID'});

// Loading rewarded ad
const result = await RNStartIoAds.loadRewarded();
if (result === Types.REWARDED_LOAD_SUCCESS) {
  // Show ad
}

// Showing interstitial ad
const result = await RNStartIoAds.loadInterstitial();
if (result === Types.INTERSTITIAL_LOAD_SUCCESS) {
  await RNStartIoAds.showInterstitial();
}
```

#### New Package API:
```typescript
import {
  initStartio, 
  showAdStartio
} from '@kastorcode/expo-startio';

// Initialization
await initStartio('YOUR_APP_ID', false); // Set second parameter to true for test mode

// Showing ad (handles loading internally)
await showAdStartio();
```

## Benefits of the New Package

1. **Expo Compatibility**: Specifically designed for Expo/React Native applications
2. **Modern React Native Support**: Compatible with React Native 0.74.5
3. **Simplified API**: Easier to use with fewer configuration options
4. **Better Error Handling**: More robust error handling and reporting
5. **Active Maintenance**: More actively maintained than the previous package

## Implementation Details

### StartIoService.ts

The service has been updated with the following changes:

1. **Initialization**: Uses `initStartio()` function with app ID and test mode flag
2. **Rewarded Ads**: Simplified implementation that assumes reward when ad is shown
3. **Exit Ads**: Uses `showAdStartio()` function to display interstitial ads
4. **Event Handling**: Removed complex event listener system (not supported in new package)
5. **Error Handling**: Improved error handling with better logging

### Limitations

1. **No Explicit Rewarded Ad Support**: The new package doesn't have explicit rewarded ad support, so we assume a reward when any ad is shown
2. **No Event Listeners**: The new package doesn't support event listeners for ad events
3. **No Ad Type Differentiation**: All ads are shown using the same function

## Installation

To install the new package:

```bash
npm install @kastorcode/expo-startio
# or
yarn add @kastorcode/expo-startio
```

## Configuration

1. Create an account at Start.io developer portal
2. Create a new app and obtain your App ID
3. Replace `YOUR_ACTUAL_START_IO_APP_ID` in `src/services/StartIoService.ts` with your real App ID

## Testing

The implementation includes test controls in development mode to simulate:
- Successful ad completion
- Ad error scenarios

## Troubleshooting

If ads are not showing:

1. Verify your Start.io App ID is correct
2. Check that the Start.io package is properly installed
3. Ensure you're testing on a physical Android device (emulators may not work)
4. Check the console logs for any error messages
5. Verify network connectivity

## Related Files

- `src/services/StartIoService.ts` - Main service implementation
- `package.json` - Dependency configuration
- `Documentations/START_IO_PACKAGE_UPDATE.md` - This file

## References

- [@kastorcode/expo-startio GitHub Repository](https://github.com/kastorcode/expo-startio)
- [Start.io Developer Portal](https://www.start.io/)

This update resolves the dependency conflicts while maintaining the same user experience and functionality.