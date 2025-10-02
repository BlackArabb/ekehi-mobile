### Ad Unit IDs

- **Test Ad Unit ID**: `ca-app-pub-3940256099942544/1712485313` (Android rewarded ads)
- **Production Ad Unit ID**: `ca-app-pub-6750107449379811/9311091493` (Ekehi Bonus)

### Key Components

1. **AdMobService** (`src/services/AdMobService.ts`) - Handles AdMob initialization and ad display
2. **AdModal** (`src/components/AdModal.tsx`) - UI component for ad interaction
3. **Mine Screen** (`app/(tabs)/mine.tsx`) - Integration point for ad rewards

## How It Works

### Development Mode (Test)
In development mode (`__DEV__`), the app uses real AdMob test ads:
- Test ad content from Google AdMob network
- Fixed rewards (0.5 EKH)
- Test mode UI indicators for development

### Production Mode
In production mode, the app uses real AdMob ads:
- Actual ad content from Google AdMob network
- Real user rewards based on ad performance
- Full AdMob event handling

## Testing the Implementation

### Manual Testing

1. **Run the app in development mode**
   ```bash
   npm start
   ```

2. **Navigate to the Mine tab**

3. **Click "Watch Ad for +0.5 EKH" button**

4. **Observe the AdModal with TEST MODE indicator**

5. **Watch a real test ad from AdMob**
   - The ad will play like a real ad but is from AdMob's test network
   - Upon completion, you'll receive the reward

6. **Verify rewards are processed correctly**
   - Check user balance updates
   - Verify database records
   - Confirm notifications

### Automated Testing

Run the ad testing script:
```bash
node Scripts/test-ads.js
```

This script tests:
- Different reward scenarios
- Cooldown functionality
- Database integration
- Error handling

## Configuration

### App Configuration (`app.json`)

The app is configured with production AdMob app IDs:
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
    },
    "plugins": [
      "expo-ads-admob"
    ]
  }
}
```

### AdMob Service

The AdMobService handles:
- AdMob initialization
- Rewarded ad loading
- Event handling
- Error management

## Event Tracking

The implementation includes comprehensive event tracking for debugging:
- `modal_opened` - AdModal appears
- `ad_loading` - Ad is loading
- `ad_started` - Ad playback begins
- `ad_completed` - Ad finishes successfully
- `ad_error` - Ad encounters an error
- `ad_exception` - Unexpected exception
- `ad_skipped` - User skips ad

## Reward System

Rewards are processed through:
1. Ad completion verification
2. Database record creation
3. User balance update
4. Cooldown activation
5. User notification

## Troubleshooting

### Common Issues

#### Ad Not Loading
- Check internet connection
- Verify AdMob configuration
- Confirm ad unit ID is correct

#### Rewards Not Processing
- Check Appwrite database connection
- Verify user authentication
- Confirm sufficient permissions

#### Test Mode Not Working
- Ensure `__DEV__` flag is set correctly
- Check development environment configuration

### Debugging Tips

1. **Enable verbose logging**
   ```javascript
   // In AdMobService.ts
   console.log('[AdMobService] Debug information');
   ```

2. **Use test device IDs**
   ```javascript
   AdMobRewarded.setTestDeviceID('EMULATOR');
   ```

3. **Monitor events**
   Check console logs for event tracking messages

## Production Deployment

### Steps for Production

1. **Production IDs are already configured**
   - App ID: `ca-app-pub-6750107449379811~7479135078`
   - Rewarded Ad Unit ID: `ca-app-pub-6750107449379811/9311091493`

2. **Test thoroughly**
   - Verify ad loading
   - Confirm reward processing
   - Check error handling

3. **Monitor analytics**
   - Track ad performance
   - Monitor user engagement
   - Review reward distribution

## Security Considerations

### Reward Validation
- Server-side validation of ad completions
- Prevention of reward manipulation
- Database integrity checks

### User Privacy
- Compliance with AdMob privacy policies
- Proper handling of user data
- Transparent reward system

## Performance Optimization

### Loading Strategies
- Preload ads when possible
- Implement retry mechanisms
- Handle network failures gracefully