import React, { useEffect, useRef } from 'react';
import { View, Animated, StyleSheet } from 'react-native';

interface PulseLoaderProps {
  size?: number;
  color?: string;
  backgroundColor?: string;
  style?: object;
}

const PulseLoader: React.FC<PulseLoaderProps> = ({ 
  size = 10, 
  color = '#FFFFFF', 
  backgroundColor = 'transparent',
  style = {}
}) => {
  const leftDotColor = useRef(new Animated.Value(0)).current;
  const centerDotColor = useRef(new Animated.Value(0)).current;
  const rightDotColor = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const createPulseAnimation = () => {
      return Animated.loop(
        Animated.sequence([
          // Phase 1: Left dot turns orange (33%)
          Animated.timing(leftDotColor, {
            toValue: 1,
            duration: 660, // 33% of 2000ms
            useNativeDriver: false,
          }),
          // Phase 2: Center dot turns orange, left turns white (66%)
          Animated.parallel([
            Animated.timing(leftDotColor, {
              toValue: 0,
              duration: 660, // 33% of 2000ms
              useNativeDriver: false,
            }),
            Animated.timing(centerDotColor, {
              toValue: 1,
              duration: 660,
              useNativeDriver: false,
            }),
          ]),
          // Phase 3: Right dot turns orange, center turns white (100%)
          Animated.parallel([
            Animated.timing(centerDotColor, {
              toValue: 0,
              duration: 680, // 34% of 2000ms
              useNativeDriver: false,
            }),
            Animated.timing(rightDotColor, {
              toValue: 1,
              duration: 680,
              useNativeDriver: false,
            }),
          ]),
          // Reset: Right dot turns white
          Animated.timing(rightDotColor, {
            toValue: 0,
            duration: 0,
            useNativeDriver: false,
          }),
        ]),
      );
    };

    const animation = createPulseAnimation();
    animation.start();

    return () => animation.stop();
  }, [leftDotColor, centerDotColor, rightDotColor]);

  const getAnimatedColor = (colorValue: Animated.Value) => {
    return colorValue.interpolate({
      inputRange: [0, 1],
      outputRange: [color, '#FFA000'],
    });
  };

  const dotSize = size;
  const dotRadius = size / 2;
  const spacing = size / 2;

  return (
    <View style={[styles.container, { backgroundColor }, style]}>
      <View style={[styles.loader, { height: size * 2.5 }]}>
        <Animated.View
          style={[
            styles.dot,
            styles.leftDot,
            { 
              width: dotSize, 
              height: dotSize, 
              borderRadius: dotRadius, 
              backgroundColor: getAnimatedColor(leftDotColor),
              marginRight: spacing / 2,
            },
          ]}
        />
        <Animated.View
          style={[
            styles.dot,
            styles.centerDot,
            { 
              width: dotSize, 
              height: dotSize, 
              borderRadius: dotRadius, 
              backgroundColor: getAnimatedColor(centerDotColor),
              marginHorizontal: spacing / 2,
            },
          ]}
        />
        <Animated.View
          style={[
            styles.dot,
            styles.rightDot,
            { 
              width: dotSize, 
              height: dotSize, 
              borderRadius: dotRadius, 
              backgroundColor: getAnimatedColor(rightDotColor),
              marginLeft: spacing / 2,
            },
          ]}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  loader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  dot: {
    backgroundColor: '#FFFFFF',
  },
  leftDot: {
  },
  centerDot: {
  },
  rightDot: {
  },
});

export default PulseLoader;