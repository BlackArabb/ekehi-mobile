# Ekehi Network Splash Screen Implementation

## Requirements
1. Reduce logo size to 100x100 pixels
2. Add "ekehi network" text effect under the logo
3. Apply drawing text effect with shadows

## Implementation Steps

### 1. Resize the Logo Image

First, resize the current splash.png from 200x200 to 100x100 pixels:

```bash
# Using ImageMagick (if available)
convert assets/splash.png -resize 100x100 assets/splash.png

# Or using any image editing software
# Open assets/splash.png and resize to 100x100 pixels, then save
```

### 2. Update the App Configuration

Update `app.json` to ensure proper splash screen configuration:

```json
{
  "expo": {
    "splash": {
      "image": "./assets/splash.png",
      "resizeMode": "contain",
      "backgroundColor": "#1a1a2e"
    }
  }
}
```

### 3. Create a Custom Splash Screen Component

Create `app/components/CustomSplashScreen.tsx`:

```tsx
import React, { useEffect } from 'react';
import { View, Text, StyleSheet, Image } from 'react-native';
import * as SplashScreen from 'expo-splash-screen';

export default function CustomSplashScreen() {
  useEffect(() => {
    SplashScreen.preventAutoHideAsync();
    
    // Simulate loading time or actual app initialization
    setTimeout(async () => {
      await SplashScreen.hideAsync();
    }, 2000);
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        <Image 
          source={require('../../assets/splash.png')} 
          style={styles.logo}
          resizeMode="contain"
        />
        
        <View style={styles.textContainer}>
          <Text style={[styles.mainText, styles.textShadow]}>ekehi</Text>
          <Text style={[styles.networkText, styles.textShadow]}>network</Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
    justifyContent: 'center',
    alignItems: 'center',
  },
  content: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  logo: {
    width: 100,
    height: 100,
    marginBottom: 30,
  },
  textContainer: {
    alignItems: 'center',
  },
  mainText: {
    fontSize: 32,
    fontWeight: '800',
    color: '#ffffff',
    letterSpacing: 1.5,
  },
  networkText: {
    fontSize: 24,
    fontWeight: '600',
    color: '#ffa000',
    marginTop: 5,
    letterSpacing: 1,
  },
  textShadow: {
    textShadowColor: 'rgba(255, 160, 0, 0.5)',
    textShadowOffset: { width: 2, height: 2 },
    textShadowRadius: 4,
  },
});
```

### 4. Update the Root Layout

Modify `app/_layout.tsx` to use the custom splash screen:

```tsx
// Add this import at the top
import CustomSplashScreen from '../components/CustomSplashScreen';

// Replace the existing splash screen handling with:
function AuthenticatedApp() {
  const { user, isLoading, checkAuthStatus } = useAuth();
  const router = useRouter();
  const segments = useSegments();
  const [backgroundAuthChecked, setBackgroundAuthChecked] = useState(false);


  // Show custom splash screen while authentication is being checked
  if (!backgroundAuthChecked || isLoading) {
    return <CustomSplashScreen />;
  }

  // ... rest of the component ...
}
```

### 5. Alternative: Enhanced Text Effects

For more advanced text effects, you can use libraries like `react-native-svg`:

```bash
npm install react-native-svg
```

Then create enhanced text effects:

```tsx
import Svg, { Text as SvgText, Defs, LinearGradient, Stop } from 'react-native-svg';

// In your component:
<Svg height="80" width="300" style={styles.svgContainer}>
  <Defs>
    <LinearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="0%">
      <Stop offset="0%" stopColor="#ffffff" />
      <Stop offset="100%" stopColor="#ffa000" />
    </LinearGradient>
  </Defs>
  <SvgText
    fill="url(#grad)"
    fontSize="32"
    fontWeight="800"
    x="150"
    y="30"
    textAnchor="middle"
  >
    ekehi
  </SvgText>
  <SvgText
    fill="url(#grad)"
    fontSize="24"
    fontWeight="600"
    x="150"
    y="60"
    textAnchor="middle"
  >
    network
  </SvgText>
</Svg>
```

## Testing

1. Run the app in development mode:
   ```bash
   npm start
   ```

2. Verify that:
   - The logo is displayed at 100x100 pixels
   - The "ekehi network" text appears below the logo
   - Text has the desired shadow effect
   - Splash screen hides automatically after 2 seconds or when app is ready

## Notes

- The text shadow effect may vary slightly between platforms
- Ensure the logo maintains its quality when resized
- Test on both iOS and Android devices
- Consider accessibility by ensuring sufficient color contrast