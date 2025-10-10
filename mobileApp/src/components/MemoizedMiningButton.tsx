import React, { memo } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Coins } from 'lucide-react-native';
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
  return (
    <View style={styles.miningContainer}>
      <View style={styles.miningButtonWrapper}>
        {/* Circular Progress Bar */}
        {is24HourMiningActive && remainingTime > 0 && (
          <View style={[styles.circularProgressContainer, { width: BUTTON_SIZE + 20, height: BUTTON_SIZE + 20 }]} pointerEvents="none">
            <CircularProgressBar 
              size={BUTTON_SIZE + 20} 
              strokeWidth={10} 
              progress={progressPercentage}
              strokeColor="#ffa000"
              backgroundColor="rgba(255, 255, 255, 0.1)"
              showStars={true}
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
            ) : null}
          </LinearGradient>
        </TouchableOpacity>
      </View>

      <Text style={styles.miningRate}>
        {is24HourMiningActive
          ? remainingTime > 0
            ? `Mining in progress...`
            : `Claim ${sessionReward} EKH Reward`
          : `Start Extended Session (+${sessionReward} EKH)`}
      </Text>
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
  miningRate: {
    fontSize: 16,
    color: '#ffffff',
    fontWeight: '600',
    marginTop: 16,
    textAlign: 'center',
  },
});