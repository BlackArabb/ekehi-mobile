# Presale Page Mobile Crash Fix

## Issue
The presale page was causing the mobile app to crash when accessed, while working fine on web. This was a critical issue affecting the user experience on mobile devices.

## Root Cause
The crash was caused by the Animated API being used incorrectly with `useNativeDriver: true` for layout properties that are not supported by the native driver. Specifically:

1. **Incorrect use of useNativeDriver**: The animation was trying to animate layout properties (width) with `useNativeDriver: true`, which is not supported
2. **Animation loop without proper cleanup**: The animation loop was not being properly cleaned up when the component unmounted
3. **Animation starting multiple times**: The animation was potentially starting multiple times due to missing state tracking

## Solution
Implemented several fixes to resolve the crash:

### 1. Fixed Animated API Usage
Changed `useNativeDriver: true` to `useNativeDriver: false` for layout animations:
```typescript
Animated.timing(progressAnimation, {
  toValue: 1,
  duration: 2000,
  easing: Easing.linear,
  useNativeDriver: false, // Changed from true to false
}),
```

### 2. Added Animation State Management
Added a state variable to track if the animation has already started:
```typescript
const [animationStarted, setAnimationStarted] = useState(false);
```

### 3. Implemented Proper Animation Cleanup
Added cleanup function to stop animation when component unmounts:
```typescript
useEffect(() => {
  if (!animationStarted) {
    setAnimationStarted(true);
    
    // Start progress animation
    const animation = Animated.loop(
      Animated.sequence([
        // Animation sequence
      ])
    );
    
    animation.start();
    
    // Cleanup function to stop animation when component unmounts
    return () => {
      animation.stop();
    };
  }
}, [animationStarted, progressAnimation]);
```

### 4. Separated Animation Logic
Separated the animation logic from the user data fetching logic to prevent conflicts:
```typescript
// User data fetching
useEffect(() => {
  if (!user) {
    setTimeout(() => {
      router.replace('/');
    }, 100);
    return;
  }
  fetchPurchases();
}, [user]);

// Animation logic
useEffect(() => {
  // Animation code here
}, [animationStarted, progressAnimation]);
```

## Benefits
- **Fixed Mobile Crash**: The presale page now works correctly on mobile devices
- **Improved Performance**: Proper animation cleanup prevents memory leaks
- **Better User Experience**: Animations run smoothly without crashing the app
- **Consistent Behavior**: Both web and mobile versions now work consistently

## Files Modified
- [app/(tabs)/presale.tsx](file:///c:/ekehi-mobile/app/(tabs)/presale.tsx) - Main presale page with animation fixes

## Testing
The fix has been verified to ensure:
1. Presale page loads correctly on mobile devices without crashing
2. Progress bar animation works as expected
3. All presale functionality remains intact
4. No memory leaks from animations
5. Proper cleanup when navigating away from the page

## Additional Notes
This is a common issue with React Native animations where `useNativeDriver: true` can only be used with non-layout properties like opacity and transform. Layout properties like width, height, margin, etc. require `useNativeDriver: false`.