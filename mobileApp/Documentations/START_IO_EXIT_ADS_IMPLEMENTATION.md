# Start.io Exit Ads Implementation

This document explains how exit ads have been implemented in the Ekehi Mobile application using Start.io (formerly StartApp) SDK.

## Overview

Exit ads are interstitial ads that are displayed when a user is about to leave the application, such as when they sign out. This implementation shows an exit ad when users tap the "Sign Out" button in the profile screen.

## Implementation Details

### 1. StartIoService Updates

The `StartIoService.ts` file has been updated with new functionality to support exit ads:

- **showExitAd()**: A new method that displays an interstitial ad when the user is exiting the app
- **exitAdShown**: A flag to prevent multiple exit ads from being shown in the same session
- **resetExitAd()**: A method to reset the exit ad shown flag for future sessions

### 2. Profile Screen Integration

The sign out functionality in `profile.tsx` has been enhanced to show an exit ad:

1. When the user taps "Sign Out", an alert confirms their intention
2. If the user confirms, and they're on Android with Start.io available:
   - An exit ad is displayed before the sign out process continues
   - The app waits for the ad to complete before proceeding
3. After the ad (or immediately on other platforms), the normal sign out process continues

### 3. Platform Support

Exit ads are currently only implemented for Android, as the [react-native-startio-ads](https://github.com/devdaniellima/react-native-startio-ads) library only supports Android at this time.

## Code Implementation

### StartIoService.ts

```typescript
/**
 * Show an exit ad
 */
async showExitAd(): Promise<boolean> {
  // Don't show ads on unsupported platforms
  if (Platform.OS !== 'android') {
    console.log('[StartIoService] Exit ads not supported on this platform');
    return false;
  }

  // Check if Start.io is available
  if (!isModuleAvailable) {
    console.warn('[StartIoService] Start.io not available, cannot show exit ad');
    return false;
  }

  try {
    await this.initialize();

    if (!RNStartIoAds) {
      console.error('[StartIoService] Start.io instance not initialized');
      return false;
    }

    // Check if we've already shown an exit ad to prevent multiple ads
    if (this.exitAdShown) {
      console.log('[StartIoService] Exit ad already shown, skipping');
      return false;
    }

    // Try to show an interstitial ad as exit ad
    try {
      // Load the interstitial ad
      const result = await RNStartIoAds.loadInterstitial();
      
      if (result === Types.INTERSTITIAL_LOAD_SUCCESS) {
        console.log('[StartIoService] Exit ad loaded successfully');
        
        // Show the ad
        await RNStartIoAds.showInterstitial();
        
        // Mark that we've shown an exit ad
        this.exitAdShown = true;
        
        console.log('[StartIoService] Exit ad shown successfully');
        return true;
      } else {
        console.warn('[StartIoService] Exit ad load failed with result:', result);
        return false;
      }
    } catch (adError) {
      console.warn('[StartIoService] Failed to show exit ad:', adError);
      return false;
    }
  } catch (error: any) {
    console.error('[StartIoService] Failed to show exit ad:', error);
    return false;
  }
}

/**
 * Reset exit ad shown flag
 */
resetExitAd(): void {
  this.exitAdShown = false;
}
```

### profile.tsx

```typescript
// Show exit ad on Android if available
if (Platform.OS === 'android' && isStartIoAvailable && StartIoService) {
  console.log('Showing exit ad before sign out');
  try {
    // Show exit ad and wait for it to complete
    await StartIoService.showExitAd();
    console.log('Exit ad shown successfully');
  } catch (adError) {
    console.warn('Failed to show exit ad:', adError);
  }
}

// Reset exit ad shown flag for next time
if (Platform.OS === 'android' && StartIoService) {
  StartIoService.resetExitAd();
}
```

## Error Handling

The implementation includes comprehensive error handling:

1. If the exit ad fails to load or show, the sign out process continues normally
2. If Start.io is not available or not properly initialized, the sign out process continues normally
3. On non-Android platforms, exit ads are simply skipped

## User Experience

The exit ad implementation is designed to be non-intrusive:

1. Users must first confirm they want to sign out
2. The exit ad is shown only after confirmation
3. If the ad fails to show, users are still signed out
4. The process includes appropriate loading states and error messages

## Testing

To test the exit ad functionality:

1. Ensure you're testing on an Android device (not emulator)
2. Make sure your Start.io App ID is properly configured
3. Tap the "Sign Out" button in the profile screen
4. Confirm the sign out in the alert dialog
5. Observe if an interstitial ad is displayed before signing out

## Limitations

1. **Platform Support**: Currently only supports Android
2. **Ad Availability**: Exit ads depend on ad inventory and may not always be available
3. **User Experience**: Some users may find exit ads disruptive

## Future Improvements

1. Add configuration options to enable/disable exit ads
2. Implement analytics tracking for exit ad performance
3. Add support for iOS when Start.io SDK becomes available
4. Implement more sophisticated ad frequency capping