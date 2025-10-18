import React, { memo, useEffect, useRef } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Animated } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Coins, Pickaxe } from 'lucide-react-native';
import CircularProgressBar from '@/components/CircularProgressBar';

interface MemoizedMiningButtonProps {
  is24HourMiningActive: boolean;
  remainingTime: number;
  progressPercentage: number;
  BUTTON_SIZE: number;
  handleMine: () => void;
  formatTime: (seconds: number) => string;
  sessionReward: number;
}

const MemoizedMiningButton: React.FC<MemoizedMiningButtonProps> = ({
  is24HourMiningActive,
  remainingTime,
  progressPercentage,
  BUTTON_SIZE,
  handleMine,
  formatTime,
  sessionReward
}) => {
  // Create animated value for the pulsating effect
  const pulseAnimation = useRef(new Animated.Value(1)).current;

  // Start the pulsating animation when mining is active
  useEffect(() => {
    let animation: Animated.CompositeAnimation | null = null;
    
    if (is24HourMiningActive && remainingTime > 0) {
      animation = Animated.loop(
        Animated.sequence([
          Animated.timing(pulseAnimation, {
            toValue: 1.5,
            duration: 2000,
            useNativeDriver: true,
          }),
          Animated.timing(pulseAnimation, {
            toValue: 1,
            duration: 2000,
            useNativeDriver: true,
          }),
        ])
      );
      animation.start();
    } else {
      // Reset animation when not mining
      pulseAnimation.setValue(1);
    }

    // Cleanup animation on unmount or when conditions change
    return () => {
      if (animation) {
        animation.stop();
      }
    };
  }, [is24HourMiningActive, remainingTime, pulseAnimation]);

  return (
    <View style={styles.miningContainer}>
      <View style={styles.miningButtonWrapper}>
        {/* Circular Progress Bar */}
        {is24HourMiningActive && remainingTime > 0 && (
          <View style={[styles.circularProgressContainer, { width: BUTTON_SIZE + 40, height: BUTTON_SIZE + 40 }]} pointerEvents="none">
            <CircularProgressBar 
              size={BUTTON_SIZE + 40} 
              strokeWidth={10} 
              progress={progressPercentage}
              strokeColor="#10b981" // Fixed: Removed rgba() format which might cause issues
              backgroundColor="rgba(255, 255, 255, 0.1)"
              showStars={false}
              pulsate={true}
            />
          </View>
        )}

        {/* Main Mining Button */}
        <TouchableOpacity
          style={[styles.miningButton, { width: BUTTON_SIZE, height: BUTTON_SIZE, borderRadius: BUTTON_SIZE / 2 }]}
          onPress={handleMine}
          activeOpacity={0.8}
          disabled={is24HourMiningActive && remainingTime > 0}
        >
          <LinearGradient
            colors={
              is24HourMiningActive
                ? remainingTime > 0
                  ? ['#10b981', '#059669']
                  : ['#ffa000', '#ff8f00']
                : ['#ffa000', '#ff8f00', '#ff6f00']
            }
            style={[styles.miningButtonGradient, { borderRadius: BUTTON_SIZE / 2 }]}
          >
            {/* Mining Time Display - Overlays when active */}
            {is24HourMiningActive && remainingTime > 0 ? (
              <View style={styles.miningTimeOverlay} pointerEvents="none">
                <Text style={styles.miningTimeText}>{formatTime(remainingTime)}</Text>
                <Text style={styles.miningTimeLabelText}>Remaining</Text>
              </View>
            ) : is24HourMiningActive && remainingTime <= 0 ? (
              <Coins size={60} color="#ffffff" />
            ) : (
              <Pickaxe size={60} color="#ffffff" />
            )}
          </LinearGradient>
        </TouchableOpacity>
      </View>

      <View style={styles.miningRateContainer}>
        {is24HourMiningActive && remainingTime > 0 ? (
          <View style={styles.statusIndicator}>
            <Animated.View style={[styles.statusDot, { backgroundColor: '#10b981', transform: [{ scale: pulseAnimation }] }]} />
            <Text style={[styles.statusText, { color: '#10b981' }]}>
              Mining
            </Text>
          </View>
        ) : (
          <Text style={styles.miningRateText}>
            {is24HourMiningActive
              ? `Claim ${sessionReward} EKH Reward`
              : `Start Mining`}
          </Text>
        )}
      </View>
    </View>
  );
};

// Memoize the entire component
export default memo(MemoizedMiningButton, (prevProps, nextProps) => {
  // Custom comparison function to prevent unnecessary re-renders
  return (
    prevProps.is24HourMiningActive === nextProps.is24HourMiningActive &&
    prevProps.remainingTime === nextProps.remainingTime &&
    prevProps.progressPercentage === nextProps.progressPercentage &&
    prevProps.BUTTON_SIZE === nextProps.BUTTON_SIZE &&
    prevProps.sessionReward === nextProps.sessionReward
  );
});

const styles = StyleSheet.create({
  miningContainer: {
    alignItems: 'center',
    marginVertical: 30,
    marginBottom: 20,
  },
  miningButtonWrapper: {
    position: 'relative',
    alignItems: 'center',
    justifyContent: 'center',
  },
  circularProgressContainer: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  
  },
  miningButton: {
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 8,
    },
    shadowOpacity: 0.4,
    shadowRadius: 16,
    elevation: 12,
  },
  miningButtonGradient: {
    width: '100%',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
  },
  miningTimeOverlay: {
    position: 'absolute',
    justifyContent: 'center',
    alignItems: 'center',
  },
  miningTimeText: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  miningTimeLabelText: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.8)',
    marginTop: 2,
  },
  miningRateContainer: {
    marginTop: 16,
  },
  statusIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    marginTop: 16,
    borderColor: 'rgba(255, 255, 255, 0.2)',
    gap: 8,
    backgroundColor: 'rgba(16, 185, 129, 0.2)',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  statusText: {
    fontSize: 14,
    fontWeight: '600',
  },
  miningRateText: {
    fontSize: 16,
    color: '#ffffff',
    fontWeight: '600',
    textAlign: 'center',
  },
});