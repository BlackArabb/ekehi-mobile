import { useState, useEffect } from 'react';
import { View, Text, Modal, TouchableOpacity, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Play, X, Gift, Coins } from 'lucide-react-native';

interface AdModalProps {
  isVisible: boolean;
  onClose: () => void;
  onComplete: () => Promise<{ success: boolean; error?: string }>;
  title?: string;
  description?: string;
  reward?: number;
}

const AdModal: React.FC<AdModalProps> = ({ 
  isVisible, 
  onClose, 
  onComplete, 
  title = 'Watch Advertisement',
  description = 'Watch a short advertisement to earn EKH rewards!',
  reward = 50
}) => {
  const [isWatching, setIsWatching] = useState(false);
  const [countdown, setCountdown] = useState(30);
  const [canSkip, setCanSkip] = useState(false);

  useEffect(() => {
    if (isVisible) {
      setIsWatching(false);
      setCountdown(30);
      setCanSkip(false);
    }
  }, [isVisible]);

  useEffect(() => {
    let timer: ReturnType<typeof setInterval> | null = null;
    
    if (isWatching && countdown > 0) {
      timer = setInterval(() => {
        setCountdown(prev => {
          const newCount = prev - 1;
          if (newCount <= 5) {
            setCanSkip(true);
          }
          return newCount;
        });
      }, 1000);
    } else if (countdown === 0 && isWatching) {
      handleAdComplete();
    }

    return () => {
      if (timer) clearInterval(timer);
    };
  }, [isWatching, countdown]);

  const handleStartAd = () => {
    setIsWatching(true);
    setCountdown(30);
    setCanSkip(false);
  };

  const handleAdComplete = async () => {
    try {
      const result = await onComplete();
      if (result.success) {
        // Show success briefly
        setTimeout(() => {
          onClose();
        }, 1500);
      } else {
        console.error('Ad reward failed:', result.error);
        onClose();
      }
    } catch (error) {
      console.error('Ad completion error:', error);
      onClose();
    } finally {
      setIsWatching(false);
      setCountdown(30);
    }
  };

  const handleSkip = () => {
    onClose();
  };

  if (isWatching) {
    return (
      <Modal visible={isVisible} transparent animationType="fade">
        <View style={styles.overlay}>
          <View style={styles.watchingContainer}>
            <LinearGradient
              colors={['#1a1a2e', '#16213e', '#0f3460']}
              style={styles.watchingCard}
            >
              {/* Ad Playing Indicator */}
              <View style={styles.playingIconContainer}>
                <LinearGradient
                  colors={['#ffa000', '#ff8f00']}
                  style={styles.playingIcon}
                >
                  <Play size={40} color="#ffffff" />
                </LinearGradient>
                <View style={styles.pulseRing} />
              </View>
              
              <Text style={styles.watchingTitle}>Advertisement Playing</Text>
              <Text style={styles.watchingSubtitle}>Watch to earn {reward} EKH reward!</Text>
              
              {/* Countdown */}
              <Text style={styles.countdown}>{countdown}</Text>
              
              {/* Progress Bar */}
              <View style={styles.progressContainer}>
                <View style={styles.progressBar}>
                  <LinearGradient
                    colors={['#ffa000', '#ff8f00']}
                    style={[styles.progressFill, { width: `${((30 - countdown) / 30) * 100}%` }]}
                  />
                </View>
              </View>
              
              {/* Skip Button */}
              {canSkip ? (
                <TouchableOpacity style={styles.skipButton} onPress={handleSkip}>
                  <Text style={styles.skipButtonText}>Skip Advertisement</Text>
                </TouchableOpacity>
              ) : (
                <Text style={styles.skipText}>
                  Skip available in {Math.max(0, countdown - 5)}s
                </Text>
              )}
            </LinearGradient>
          </View>
        </View>
      </Modal>
    );
  }

  return (
    <Modal visible={isVisible} transparent animationType="slide">
      <View style={styles.overlay}>
        <View style={styles.container}>
          <LinearGradient
            colors={['#1a1a2e', '#16213e', '#0f3460']}
            style={styles.card}
          >
            {/* Close Button */}
            <TouchableOpacity style={styles.closeButton} onPress={onClose}>
              <X size={24} color="rgba(255, 255, 255, 0.6)" />
            </TouchableOpacity>

            {/* Reward Icon */}
            <View style={styles.rewardIconContainer}>
              <LinearGradient
                colors={['#ffa000', '#ff8f00']}
                style={styles.rewardIcon}
              >
                <Gift size={32} color="#ffffff" />
              </LinearGradient>
            </View>
            
            <Text style={styles.title}>{title}</Text>
            <Text style={styles.description}>{description}</Text>
            
            {/* Reward Display */}
            <View style={styles.rewardDisplay}>
              <LinearGradient
                colors={['rgba(255, 160, 0, 0.2)', 'rgba(255, 143, 0, 0.2)']}
                style={styles.rewardCard}
              >
                <View style={styles.rewardContent}>
                  <Coins size={24} color="#ffa000" />
                  <Text style={styles.rewardAmount}>+{reward} EKH</Text>
                </View>
                <Text style={styles.rewardLabel}>Reward for watching</Text>
              </LinearGradient>
            </View>
            
            {/* Action Buttons */}
            <View style={styles.buttonContainer}>
              <TouchableOpacity style={styles.cancelButton} onPress={onClose}>
                <Text style={styles.cancelButtonText}>Cancel</Text>
              </TouchableOpacity>
              
              <TouchableOpacity style={styles.watchButton} onPress={handleStartAd}>
                <LinearGradient
                  colors={['#ffa000', '#ff8f00']}
                  style={styles.watchButtonGradient}
                >
                  <Play size={16} color="#ffffff" />
                  <Text style={styles.watchButtonText}>Watch Ad</Text>
                </LinearGradient>
              </TouchableOpacity>
            </View>
          </LinearGradient>
        </View>
      </View>
    </Modal>
  );
};

export default AdModal;

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.9)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  container: {
    width: '100%',
    maxWidth: 400,
  },
  card: {
    borderRadius: 24,
    padding: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
    position: 'relative',
  },
  watchingContainer: {
    width: '100%',
    maxWidth: 350,
  },
  watchingCard: {
    borderRadius: 24,
    padding: 32,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  closeButton: {
    position: 'absolute',
    top: 16,
    right: 16,
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1,
  },
  rewardIconContainer: {
    alignItems: 'center',
    marginBottom: 20,
  },
  rewardIcon: {
    width: 80,
    height: 80,
    borderRadius: 40,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#ffa000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  playingIconContainer: {
    alignItems: 'center',
    marginBottom: 24,
    position: 'relative',
  },
  playingIcon: {
    width: 100,
    height: 100,
    borderRadius: 50,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#ffa000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  pulseRing: {
    position: 'absolute',
    width: 120,
    height: 120,
    borderRadius: 60,
    borderWidth: 2,
    borderColor: 'rgba(255, 160, 0, 0.3)',
    // Note: For pulse animation, you'd need react-native-reanimated
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    textAlign: 'center',
    marginBottom: 8,
  },
  description: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
    lineHeight: 20,
    marginBottom: 24,
  },
  watchingTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#ffffff',
    textAlign: 'center',
    marginBottom: 8,
  },
  watchingSubtitle: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
    marginBottom: 32,
  },
  countdown: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#ffa000',
    textAlign: 'center',
    marginBottom: 24,
  },
  progressContainer: {
    width: '100%',
    marginBottom: 24,
  },
  progressBar: {
    width: '100%',
    height: 8,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 4,
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    borderRadius: 4,
  },
  rewardDisplay: {
    width: '100%',
    marginBottom: 24,
  },
  rewardCard: {
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  rewardContent: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginBottom: 4,
  },
  rewardAmount: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffa000',
  },
  rewardLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    textAlign: 'center',
  },
  buttonContainer: {
    flexDirection: 'row',
    gap: 12,
  },
  cancelButton: {
    flex: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  cancelButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: 'rgba(255, 255, 255, 0.8)',
  },
  watchButton: {
    flex: 1,
    borderRadius: 12,
    overflow: 'hidden',
  },
  watchButtonGradient: {
    paddingVertical: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  watchButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  skipButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    paddingHorizontal: 24,
    paddingVertical: 12,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  skipButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: 'rgba(255, 255, 255, 0.8)',
  },
  skipText: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.5)',
    textAlign: 'center',
  },
});
