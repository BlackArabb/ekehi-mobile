# PWA Setup for Admin Dashboard

Your admin dashboard is now configured as a Progressive Web App (PWA). This means it can be installed as a mobile app without React Native or external hosting.

## What was added:

1. **manifest.json** - PWA configuration file that defines the app name, icons, colors, and shortcuts
2. **Service Worker (sw.js)** - Handles offline functionality, caching, and background sync
3. **PWA Meta Tags** - HTML headers for mobile app functionality
4. **Service Worker Registration** - Automatic registration on page load

## To use the PWA:

### On Android:
1. Open the app in Chrome or Edge
2. Tap the menu (three dots)
3. Select "Install app" or "Add to Home Screen"
4. The app will install as a standalone app

### On iOS:
1. Open the app in Safari
2. Tap the Share button
3. Select "Add to Home Screen"
4. Name it and add
5. The app will appear on your home screen

## Mobile App Icons (TODO):

You need to create and add these icon files to the `public/` folder:
- `icon-192x192.png` (192x192 pixels)
- `icon-512x512.png` (512x512 pixels)
- `icon-maskable.png` (192x192 pixels, with safe zone)
- `screenshot-540x720.png` (narrow form factor)
- `screenshot-1280x720.png` (wide form factor)

You can use:
- Figma to design icons
- Online tools like https://www.favicon-generator.org/
- Or your own icon design assets

## Service Worker Features:

The service worker provides:
- **Offline Support**: Pages and API responses are cached
- **Network-First for APIs**: Tries network first, falls back to cache
- **Cache-First for Assets**: Uses cached assets for faster loading
- **Automatic Cache Updates**: Old caches are cleaned up on updates

## To Deploy as Native App (Optional):

With Capacitor (recommended):
```bash
npm install @capacitor/core @capacitor/cli
npx cap init
npx cap add android
npx cap add ios
npx cap open android  # Opens Android Studio
npx cap open ios      # Opens Xcode
```

Then build and submit to app stores.

## Browser Support:

- ✅ Chrome/Edge (Android & Desktop)
- ✅ Safari (iOS & macOS)
- ✅ Firefox (Desktop & Android)
- ✅ Samsung Internet
- ✅ Opera

## Environment Variables:

No additional environment variables needed. The PWA uses the same API configuration as the web app.

## Testing the PWA:

1. Build the app: `npm run build`
2. Start the server: `npm start`
3. Open DevTools (F12)
4. Go to Application → Service Workers
5. Check that the service worker is registered
6. Go to Application → Manifest to verify the manifest is loaded

## Next Steps:

1. Add proper app icons to `public/` folder
2. Test on Android and iOS devices
3. Customize manifest.json with your branding (colors, icons, etc.)
4. Optional: Deploy with Capacitor for native app store submission
