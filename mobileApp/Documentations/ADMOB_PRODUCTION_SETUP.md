# AdMob Production Setup Guide

This guide explains how to configure real AdMob ads for production use in the Ekehi Network mobile app.

## Current Configuration

The app is currently configured to use AdMob test ads for both development and production environments to ensure functionality without violating AdMob policies during testing.

## Setting Up Real AdMob Ads

### 1. Create AdMob Account and Ad Units

1. Go to [Google AdMob](https://admob.google.com/)
2. Create an account or sign in to your existing account
3. Create a new app or select your existing app
4. Create ad units for rewarded ads:
   - Navigate to Apps > [Your App] > Ad Units
   - Click "Add Ad Unit"
   - Select "Rewarded"
   - Configure ad settings
   - Note the Ad Unit ID (looks like: `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX`)

### 2. Update AdMobService.ts

In `src/services/AdMobService.ts`, update the production ad unit ID:

```typescript
// Replace this placeholder with your real AdMob unit ID
const PROD_REWARDED_AD_UNIT_ID = 'ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX'; // Your real ID here
```

Also update the constructor to differentiate between development and production:

```typescript
constructor() {
  // Use test ads in development, real ads in production
  this.adUnitId = __DEV__ ? TEST_REWARDED_AD_UNIT_ID : PROD_REWARDED_AD_UNIT_ID;
  console.log('[AdMobService] Using ad unit ID:', this.adUnitId, 'Environment:', __DEV__ ? 'Development' : 'Production');
}
```

### 3. Update App Configuration

Ensure your `app.json` has the correct AdMob App ID for both iOS and Android:

```json
{
  "expo": {
    "ios": {
      "config": {
        "googleMobileAdsAppId": "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
      }
    },
    "android": {
      "config": {
        "googleMobileAdsAppId": "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
      }
    }
  }
}
```

### 4. Testing Real Ads

Before publishing, test with real ads using your test device:

1. Add your device as a test device in the AdMob console
2. Use the device's advertising ID for testing
3. Ensure ads load and display correctly
4. Verify reward functionality works

### 5. Compliance Notes

- Never click your own ads in production
- Ensure proper disclosure of ad usage in your app's privacy policy
- Follow all AdMob policies and guidelines
- Test thoroughly before releasing to production

## Troubleshooting

### Common Issues

1. **Ads not loading**: Check ad unit IDs and app configuration
2. **No rewards received**: Verify event listener setup in AdMobService
3. **Ad display issues**: Check device registration for testing

### Testing Checklist

- [ ] Ad unit IDs are correct
- [ ] App is properly configured in app.json
- [ ] Test devices are registered
- [ ] Ads load successfully
- [ ] Rewards are properly processed
- [ ] Error handling works correctly

## Important Notes

1. The current implementation uses test ad unit IDs for both development and production to prevent policy violations during development
2. Replace `PROD_REWARDED_AD_UNIT_ID` with your real AdMob unit ID before publishing
3. Always test with real ads before publishing to ensure everything works correctly
4. Monitor ad performance and earnings through the AdMob console