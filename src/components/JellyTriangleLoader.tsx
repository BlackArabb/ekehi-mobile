import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, Animated } from 'react-native';

interface JellyTriangleLoaderProps {
  size?: number;
  color?: string;
  speed?: number;
}

const JellyTriangleLoader: React.FC<JellyTriangleLoaderProps> = ({ 
  size = 40, 
  color = '#183153',
  speed = 1750 
}) => {
  const dotScale = useRef(new Animated.Value(1.5)).current;
  const travelerPosition = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const animateDot = () => {
      Animated.sequence([
        Animated.timing(dotScale, {
          toValue: 1,
          duration: speed * 0.2,
          useNativeDriver: true,
        }),
        Animated.timing(dotScale, {
          toValue: 1.5,
          duration: speed * 0.3,
          useNativeDriver: true,
        }),
      ]).start(() => animateDot());
    };

    const animateTraveler = () => {
      Animated.sequence([
        Animated.timing(travelerPosition, {
          toValue: 1,
          duration: speed * 0.333,
          useNativeDriver: true,
        }),
        Animated.timing(travelerPosition, {
          toValue: 2,
          duration: speed * 0.333,
          useNativeDriver: true,
        }),
        Animated.timing(travelerPosition, {
          toValue: 0,
          duration: speed * 0.333,
          useNativeDriver: true,
        }),
      ]).start(() => animateTraveler());
    };

    animateDot();
    animateTraveler();
  }, [dotScale, travelerPosition, speed]);

  const getTravelerTransform = () => {
    const translateX = travelerPosition.interpolate({
      inputRange: [0, 1, 2],
      outputRange: [0, size * 1.2, -size * 0.95],
    });
    
    const translateY = travelerPosition.interpolate({
      inputRange: [0, 1, 2],
      outputRange: [0, size * 1.75, size * 1.75],
    });
    
    return [{ translateX }, { translateY }];
  };

  const getBeforeDotTransform = () => {
    return dotScale.interpolate({
      inputRange: [1, 1.5],
      outputRange: [1, 1.5],
    });
  };

  const getAfterDotTransform = () => {
    return dotScale.interpolate({
      inputRange: [1, 1.5],
      outputRange: [1, 1.5],
    });
  };

  return (
    <View style={[styles.container, { width: size, height: size }]}>
      {/* Main dot */}
      <Animated.View 
        style={[
          styles.dot, 
          { 
            backgroundColor: color,
            transform: [{ scale: dotScale }],
            top: size * 0.06,
            left: size * 0.3,
            width: size * 0.33,
            height: size * 0.33,
          }
        ]} 
      />
      
      {/* Bottom right dot */}
      <Animated.View 
        style={[
          styles.dot, 
          { 
            backgroundColor: color,
            transform: [{ scale: getBeforeDotTransform() }],
            bottom: size * 0.06,
            right: 0,
            width: size * 0.33,
            height: size * 0.33,
          }
        ]} 
      />
      
      {/* Bottom left dot */}
      <Animated.View 
        style={[
          styles.dot, 
          { 
            backgroundColor: color,
            transform: [{ scale: getAfterDotTransform() }],
            bottom: size * 0.06,
            left: 0,
            width: size * 0.33,
            height: size * 0.33,
          }
        ]} 
      />
      
      {/* Traveler dot */}
      <Animated.View 
        style={[
          styles.traveler, 
          { 
            backgroundColor: color,
            top: size * 0.06,
            left: size * 0.3,
            width: size * 0.33,
            height: size * 0.33,
            transform: getTravelerTransform(),
          }
        ]} 
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'relative',
  },
  dot: {
    position: 'absolute',
    borderRadius: 100,
    shadowColor: '#121f35',
    shadowOffset: {
      width: 0,
      height: 0,
    },
    shadowOpacity: 0.3,
    shadowRadius: 20,
    elevation: 5,
  },
  traveler: {
    position: 'absolute',
    borderRadius: 100,
  },
});

export default JellyTriangleLoader;