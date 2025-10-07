import React from 'react';
import { View, StyleSheet } from 'react-native';
import Animated, { useSharedValue, useAnimatedStyle, withRepeat, withTiming, Easing } from 'react-native-reanimated';

interface LoadingDotsProps {
  size?: number;
  color?: string;
}

export default function LoadingDots({ size = 8, color = '#000000' }: LoadingDotsProps) {
  // Create shared values for animation
  const dot1 = useSharedValue(1);
  const dot2 = useSharedValue(1);
  const dot3 = useSharedValue(1);

  // Start animations
  React.useEffect(() => {
    dot1.value = withRepeat(withTiming(0, { duration: 1000, easing: Easing.inOut(Easing.ease) }), -1, true);
    setTimeout(() => {
      dot2.value = withRepeat(withTiming(0, { duration: 1000, easing: Easing.inOut(Easing.ease) }), -1, true);
    }, 200);
    setTimeout(() => {
      dot3.value = withRepeat(withTiming(0, { duration: 1000, easing: Easing.inOut(Easing.ease) }), -1, true);
    }, 400);
  }, []);

  // Create animated styles
  const animatedStyle1 = useAnimatedStyle(() => {
    return {
      opacity: dot1.value,
    };
  });

  const animatedStyle2 = useAnimatedStyle(() => {
    return {
      opacity: dot2.value,
    };
  });

  const animatedStyle3 = useAnimatedStyle(() => {
    return {
      opacity: dot3.value,
    };
  });

  return (
    <View style={styles.container}>
      <Animated.View style={[styles.dot, animatedStyle1, { width: size, height: size, backgroundColor: color }]} />
      <Animated.View style={[styles.dot, animatedStyle2, { width: size, height: size, backgroundColor: color }]} />
      <Animated.View style={[styles.dot, animatedStyle3, { width: size, height: size, backgroundColor: color }]} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
  },
  dot: {
    borderRadius: 4,
  },
});