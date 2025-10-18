import React, { useEffect, useRef } from 'react';
import { View, StyleSheet, Animated, Easing } from 'react-native';
import Svg, { Circle, Path } from 'react-native-svg';

interface CircularProgressBarProps {
  size?: number;
  strokeWidth?: number;
  progress: number; // 0 to 100
  strokeColor?: string;
  backgroundColor?: string;
  children?: React.ReactNode;
  showStars?: boolean; // New prop for showing stars
  pulsate?: boolean; // New prop for pulsating effect
}

const CircularProgressBar: React.FC<CircularProgressBarProps> = ({
  size = 180,
  strokeWidth = 8,
  progress,
  strokeColor = '#ffa000',
  backgroundColor = 'rgba(255, 255, 255, 0.1)',
  children,
  showStars = false,
  pulsate = false,
}) => {
  const radius = (size - strokeWidth) / 2;
  const circumference = radius * 2 * Math.PI;
  const normalizedProgress = Math.min(Math.max(progress, 0), 100);
  const strokeDashoffset = circumference - (normalizedProgress / 100) * circumference;

  // Pulsating animation
  const pulseAnimation = useRef(new Animated.Value(1)).current;

  // Star animation values
  const starAnimations = useRef(
    Array(12) // Increased to 12 stars for more activity
      .fill(0)
      .map(() => ({
        scale: new Animated.Value(0),
        opacity: new Animated.Value(0),
        x: new Animated.Value(0),
        y: new Animated.Value(0),
        pulse: new Animated.Value(1), // For pulsing effect
        rotate: new Animated.Value(0), // For rotation effect
      }))
  ).current;

  // Pulsating animation effect
  useEffect(() => {
    let animationActive = true;
    
    if (pulsate) {
      const animate = () => {
        if (!animationActive) return;
        
        Animated.sequence([
          Animated.timing(pulseAnimation, {
            toValue: 1.2,
            duration: 1000,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: true,
          }),
          Animated.timing(pulseAnimation, {
            toValue: 1,
            duration: 1000,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: true,
          }),
        ]).start(() => {
          // Continue animation if still pulsating
          if (animationActive && pulsate) {
            animate();
          }
        });
      };
      
      animate();
    } else {
      // Reset animation when not pulsating
      pulseAnimation.setValue(1);
    }
    
    return () => {
      animationActive = false;
    };
  }, [pulsate, pulseAnimation]);

  // Animate stars
  useEffect(() => {
    if (!showStars) return;

    const animateStar = (index: number) => {
      const star = starAnimations[index];
      
      // Random position within the circle
      const angle = Math.random() * Math.PI * 2;
      const distance = radius * (0.3 + Math.random() * 0.6); // Random distance within the circle
      const targetX = Math.cos(angle) * distance;
      const targetY = Math.sin(angle) * distance;
      
      // Random rotation
      const targetRotation = Math.random() * 360;
      
      // Start pulsing animation
      const pulseAnimation = Animated.loop(
        Animated.sequence([
          Animated.timing(star.pulse, {
            toValue: 1.3,
            duration: 800,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: true,
          }),
          Animated.timing(star.pulse, {
            toValue: 0.9,
            duration: 800,
            easing: Easing.inOut(Easing.ease),
            useNativeDriver: true,
          }),
        ])
      );
      pulseAnimation.start();
      
      // Start rotation animation
      const rotateAnimation = Animated.loop(
        Animated.timing(star.rotate, {
          toValue: 360,
          duration: 3000 + Math.random() * 2000, // Random rotation speed
          easing: Easing.linear,
          useNativeDriver: true,
        })
      );
      rotateAnimation.start();
      
      Animated.parallel([
        Animated.timing(star.scale, {
          toValue: 0.6 + Math.random() * 0.8, // Random scale between 0.6 and 1.4
          duration: 400,
          easing: Easing.out(Easing.ease),
          useNativeDriver: true,
        }),
        Animated.timing(star.opacity, {
          toValue: 0.6 + Math.random() * 0.4, // Random opacity between 0.6 and 1.0
          duration: 400,
          useNativeDriver: true,
        }),
        Animated.timing(star.x, {
          toValue: targetX,
          duration: 400,
          useNativeDriver: true,
        }),
        Animated.timing(star.y, {
          toValue: targetY,
          duration: 400,
          useNativeDriver: true,
        }),
        Animated.timing(star.rotate, {
          toValue: targetRotation,
          duration: 400,
          useNativeDriver: true,
        }),
      ]).start(() => {
        // After appearing, stars will fade out and move slightly
        setTimeout(() => {
          // Stop pulsing animation
          pulseAnimation.stop();
          
          Animated.parallel([
            Animated.timing(star.scale, {
              toValue: 0,
              duration: 1000,
              useNativeDriver: true,
            }),
            Animated.timing(star.opacity, {
              toValue: 0,
              duration: 1000,
              useNativeDriver: true,
            }),
          ]).start(() => {
            // Restart animation after a delay
            setTimeout(() => {
              if (showStars) {
                animateStar(index);
              }
            }, Math.random() * 300); // Faster respawn
          });
        }, 1000 + Math.random() * 500); // Shorter display time
      });
      
      // Clean up animations on unmount
      return () => {
        pulseAnimation.stop();
        rotateAnimation.stop();
      };
    };

    // Start animating all stars
    const cleanupFunctions: (() => void)[] = [];
    starAnimations.forEach((_, index) => {
      // Stagger the start times
      setTimeout(() => {
        const cleanup = animateStar(index);
        if (cleanup) cleanupFunctions.push(cleanup);
      }, index * 100); // Faster stagger
    });

    // Cleanup function
    return () => {
      cleanupFunctions.forEach(cleanup => cleanup());
      starAnimations.forEach(star => {
        star.scale.stopAnimation();
        star.opacity.stopAnimation();
        star.x.stopAnimation();
        star.y.stopAnimation();
        star.pulse.stopAnimation();
        star.rotate.stopAnimation();
      });
    };
  }, [showStars, radius, starAnimations]);

  // Star SVG path - Fixed by ensuring it's properly defined as a string
  const starPath = "M12 2 L15.09 8.26 L22 9.27 L17 14.14 L18.18 21.02 L12 17.77 L5.82 21.02 L7 14.14 L2 9.27 L8.91 8.26 Z";

  return (
    <View style={[styles.container, { width: size, height: size }]}>
      <Svg width={size} height={size}>
        {/* Background circle */}
        <Circle
          stroke={backgroundColor}
          fill="none"
          cx={size / 2}
          cy={size / 2}
          r={radius}
          strokeWidth={strokeWidth}
        />
        {/* Progress circle */}
        <Circle
          stroke={strokeColor}
          fill="none"
          cx={size / 2}
          cy={size / 2}
          r={radius}
          strokeWidth={strokeWidth}
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          strokeLinecap="round"
          transform={`rotate(-90, ${size / 2}, ${size / 2})`}
        />
      </Svg>
      
      {/* Floating Stars */}
      {showStars && (
        <View style={[styles.starsContainer, { width: size, height: size }]}>
          {starAnimations.map((star, index) => (
            <Animated.View
              key={index}
              style={[
                styles.star,
                {
                  transform: [
                    { translateX: star.x },
                    { translateY: star.y },
                    { scale: Animated.multiply(star.scale, star.pulse) }, // Combine scale and pulse
                    { rotate: star.rotate.interpolate({
                      inputRange: [0, 360],
                      outputRange: ['0deg', '360deg']
                    })},
                  ],
                  opacity: star.opacity,
                },
              ]}
            >
              <View style={styles.starWrapper}>
                <Svg width="16" height="16" viewBox="0 0 24 24">
                  <Path 
                    d={starPath} 
                    fill="#ffffff" 
                    stroke="#ffa000" 
                    strokeWidth="1"
                  />
                </Svg>
              </View>
            </Animated.View>
          ))}
        </View>
      )}
      
      {/* Children with pulsating effect */}
      <View style={[styles.childrenContainer, { width: size, height: size }]}>
        {pulsate ? (
          <Animated.View style={{ transform: [{ scale: pulseAnimation }] }}>
            {children}
          </Animated.View>
        ) : (
          children
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'relative',
    justifyContent: 'center',
    alignItems: 'center',
  },
  starsContainer: {
    position: 'absolute',
    justifyContent: 'center',
    alignItems: 'center',
  },
  star: {
    position: 'absolute',
    justifyContent: 'center',
    alignItems: 'center',
  },
  starWrapper: {
    shadowColor: '#ffa000',
    shadowOffset: {
      width: 0,
      height: 0,
    },
    shadowOpacity: 0.9,
    shadowRadius: 8,
    elevation: 10,
    backgroundColor: 'transparent',
  },
  childrenContainer: {
    position: 'absolute',
    justifyContent: 'center',
    alignItems: 'center',
  },
});

export default CircularProgressBar;