# Start.io Migration Guide

This document explains how to migrate from Google AdMob to Start.io (formerly StartApp) in the Ekehi Mobile application.

## Overview

The Ekehi Mobile application has been migrated from Google AdMob to Start.io for advertising services. This change was made to improve ad performance and potentially increase revenue.

## Changes Made

### 1. New Start.io Service

A new service `StartIoService.ts` has been created to replace `AdMobService.ts`. This service provides the same interface for loading and showing rewarded ads.

### 2. Dependency Updates

- Removed: `react-native-google-mobile-ads`
- Added: `react-native-startio-ads`

### 3. Configuration Updates

- Removed AdMob configuration from `app.json`
- Added Start.io App ID configuration (to be added when available)

### 4. Code Updates

- Updated `AdMobWrapper.tsx` to `StartIoWrapper.tsx`
- Updated `AdModal.tsx` to use Start.io service
- Updated `mine.tsx` to use Start.io service

## Implementation Details

### Start.io Service

The new `StartIoService.ts` provides the following methods:

- `initialize()`: Initializes the Start.io SDK
- `loadRewardedAd()`: Loads a rewarded ad
- `showRewardedAd()`: Shows a rewarded ad and returns the result
- `getAppId()`: Returns the Start.io App ID
- `isStartIoInitialized()`: Checks if Start.io is initialized
- `isStartIoAvailable()`: Checks if Start.io is available
- `getModuleLoadError()`: Returns any module loading errors
- `setAppId()`: Sets a custom App ID

### Platform Support

Start.io integration currently only supports Android platform. iOS and web platforms will show a message indicating ads are unavailable.

## Installation

1. Install the Start.io SDK:
   ```bash
   npm install react-native-startio-ads
   ```

2. For Android, follow the manual installation steps in the `react-native-startio-ads` documentation.

## Configuration

1. Create an account at Start.io developer portal
2. Create a new app and obtain your App ID
3. Replace `YOUR_START_IO_APP_ID` in `StartIoService.ts` with your actual App ID

## Testing

The application includes test controls in development mode to simulate ad success and failure scenarios.

## Limitations

1. Currently only supports Android platform
2. Requires manual installation steps for Android
3. May require additional configuration for advanced features

## Troubleshooting

If ads are not showing:

1. Verify your Start.io App ID is correct
2. Check that the Start.io SDK is properly installed
3. Ensure you're testing on a physical Android device
4. Check the console logs for any error messages

## Future Improvements

1. Add iOS support when Start.io SDK becomes available
2. Implement additional ad formats (banner ads, interstitial ads)
3. Add more detailed analytics and tracking