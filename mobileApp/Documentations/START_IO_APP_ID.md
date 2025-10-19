# Start.io App ID Configuration

This document contains the Start.io App ID configuration for the Ekehi Mobile application.

## App ID Information

- **App ID**: 209257659
- **Platform**: Android
- **Status**: Active

## Configuration

The Start.io App ID has been configured in the following file:

**File**: `src/services/StartIoService.ts`

```typescript
this.appId = '209257659'; // Your Start.io App ID
```

## SDK Initialization

The Start.io SDK is initialized with the following settings:

1. **App ID**: 209257659
2. **Test Mode**: Enabled in development (`__DEV__`), disabled in production
3. **Ad Frequency**:
   - Minimum 60 seconds between ads
   - Minimum 3 activities between ads

## Testing

To test the Start.io integration:

1. Ensure you're testing on a physical Android device (not emulator)
2. Verify that ads are showing correctly in the app
3. Check the console logs for any initialization errors
4. Confirm that exit ads show when signing out

## Troubleshooting

If ads are not showing:

1. Verify the App ID is correct (209257659)
2. Check that the Start.io package is properly installed
3. Ensure you have an active internet connection
4. Check the console logs for any error messages
5. Verify that you're testing on a physical device

## References

- [Start.io Developer Portal](https://www.start.io/)
- [@kastorcode/expo-startio Documentation](https://github.com/kastorcode/expo-startio)