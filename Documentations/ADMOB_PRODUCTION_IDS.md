# AdMob Production IDs

This document contains the production AdMob IDs for the Ekehi Network application.

## Production AdMob App ID
- **App ID**: `ca-app-pub-6750107449379811~7479135078`
- **Name**: Ekehi-Network-T

## Production Ad Unit IDs

### Rewarded Ads
- **Ad Unit ID**: `ca-app-pub-6750107449379811/9311091493`
- **Name**: Ekehi Bonus

## Implementation Details

The AdMobService is configured to use:
- Test IDs in development mode (`__DEV__` = true)
- Production IDs in production mode (`__DEV__` = false)

### Test IDs (Development)
- **App ID**: `ca-app-pub-3940256099942544~3347511713`
- **Rewarded Ad Unit ID**: `ca-app-pub-3940256099942544/1712485313`

### Production IDs (Production)
- **App ID**: `ca-app-pub-6750107449379811~7479135078`
- **Rewarded Ad Unit ID**: `ca-app-pub-6750107449379811/9311091493`

## Configuration Files

### app.json
```json
{
  "expo": {
    "ios": {
      "config": {
        "googleMobileAdsAppId": "ca-app-pub-6750107449379811~7479135078"
      }
    },
    "android": {
      "config": {
        "googleMobileAdsAppId": "ca-app-pub-6750107449379811~7479135078"
      }
    }
  }
}
```

### src/services/AdMobService.ts
```typescript
// Production Ad Unit ID
const PROD_REWARDED_AD_UNIT_ID = 'ca-app-pub-6750107449379811/9311091493';
```

## Testing

To test with production IDs in development:
1. Temporarily set `__DEV__` to false in AdMobService.ts
2. Or use the `setAdUnitId` method to override the ad unit ID
3. Remember to revert changes before committing

## Important Notes

1. Never use production IDs in development for extended testing
2. Always use test IDs during development to avoid policy violations
3. Monitor ad performance and earnings through the AdMob console
4. Keep this document updated with any changes to ad IDs