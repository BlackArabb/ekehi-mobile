import { useState, useEffect } from 'react';
import { View, Text, Modal, TouchableOpacity, StyleSheet, Platform } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Play, X, Gift, Coins } from 'lucide-react-native';
// Conditional import for AdMobService - only import on native platforms
let AdMobService: any = null;
let isAdMobAvailable = false;
if (Platform.OS !== 'web') {
  try {
    AdMobService = require('@/services/AdMobService').default;
    isAdMobAvailable = AdMobService.isAdMobAvailable && AdMobService.isAdMobAvailable();
  } catch (error) {
    console.error('[AdModal] Failed to import AdMobService:', error);
    isAdMobAvailable = false;
  }
}

interface AdModalProps {
  isVisible: boolean;
  onClose: () => void;
  onComplete: (result: { success: boolean; reward?: number; error?: string }) => Promise<void>;
  title?: string;
  description?: string;
  reward?: number;
  // Testing props
  onTestEvent?: (event: string, data?: any) => void;
}

const AdModal: React.FC<AdModalProps> = ({ 
  isVisible, 
  onClose, 
  onComplete, 
  title = 'Watch Advertisement',
  description = 'Watch a short advertisement to earn EKH rewards!',
  reward = 0.5,
  onTestEvent
}) => {
  const [isWatching, setIsWatching] = useState(false);
  const [countdown, setCountdown] = useState(30);
  const [canSkip, setCanSkip] = useState(false);
  const [isAdLoading, setIsAdLoading] = useState(false);
  const [isWebPlatform] = useState(Platform.OS === 'web');
  const [isAdAvailable, setIsAdAvailable] = useState(isAdMobAvailable && Platform.OS !== 'web');

  // Notify test system of events
  const notifyTestEvent = (event: string, data?: any) => {
    if (onTestEvent) {
      onTestEvent(event, data);
    }
  };

  useEffect(() => {
    if (isVisible) {
      setIsWatching(false);
      setCountdown(30);
      setCanSkip(false);
      setIsAdLoading(false);
      notifyTestEvent('modal_opened', { title, reward });
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
      // Only call handleAdComplete if we're actually watching (not loading)
      if (!isAdLoading) {
        handleAdComplete();
      }
    }

    return () => {
      if (timer) clearInterval(timer);
    }
  }, [isWatching, countdown, isAdLoading]);

  const handleStartAd = async () => {
    // Don't start ads on web platform
    if (isWebPlatform || !isAdAvailable) {
      console.log('[AdModal] Ads not supported on this platform');
      onClose();
      return;
    }

    // Set watching state immediately when user clicks "Watch Ad"
    setIsWatching(true);
    setCountdown(30);
    setCanSkip(false);
    
    // Always use real AdMob ads
    try {
      setIsAdLoading(true);
      notifyTestEvent('ad_loading', { adUnitId: AdMobService.getAdUnitId() });

      const result = await AdMobService.showRewardedAd();
      
      setIsAdLoading(false);
      setIsWatching(false); // Reset watching state
      
      if (result.success) {
        notifyTestEvent('ad_completed', { reward: result.reward });
        // Call onComplete with the actual reward from AdMob
        await onComplete({ success: true, reward: result.reward });
        // Close after successful completion
        onClose();
      } else {
        notifyTestEvent('ad_error', { error: result.error });
        console.error('AdMob error:', result.error);
        // Call onComplete with error
        await onComplete({ success: false, error: result.error });
        // Close on error
        onClose();
      }
    } catch (error: any) {
      setIsAdLoading(false);
      setIsWatching(false); // Reset watching state on error
      notifyTestEvent('ad_exception', { error: error.message || 'Unknown error' });
      console.error('AdMob exception:', error);
      // Call onComplete with error
      await onComplete({ success: false, error: error.message || 'Ad failed to show' });
      // Close on exception
      onClose();
    }
  };

  const handleAdComplete = async () => {
    try {
      notifyTestEvent('ad_completed', { reward });
      // Call onComplete for consistency
      await onComplete({ success: true, reward });
      
      // Close the modal after ad completion
      onClose();
    } catch (error: any) {
      notifyTestEvent('ad_error', { error: error.message });
      console.error('Ad completion error:', error);
      onClose();
    } finally {
      setIsWatching(false);
      setCountdown(30);
    }
  };

  const handleSkip = () => {
    notifyTestEvent('ad_skipped', { timeRemaining: countdown });
    onClose();
  };

  // Test controls (only visible in development for testing purposes)
  const renderTestControls = () => {
    // Only show test controls in development
    // @ts-ignore - __DEV__ is a React Native global
    if (!__DEV__) return null;
    
    return (
      <View style={styles.testControls}>
        <Text style={styles.testLabel}>TEST MODE</Text>
        <View style={styles.testButtons}>
          <TouchableOpacity 
            style={[styles.testButton, styles.testSuccessButton]}
            onPress={() => {
              // Simulate successful ad completion
              setIsWatching(false);
              setCountdown(0);
            }}
          >
            <Text style={styles.testButtonText}>Simulate Success</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.testButton, styles.testErrorButton]}
            onPress={() => {
              // Simulate ad error
              setIsWatching(false);
              onClose();
            }}
          >
            <Text style={styles.testButtonText}>Simulate Error</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  // Web platform message
  const renderWebPlatformMessage = () => {
    if (!isWebPlatform) return null;
    
    return (
      <View style={styles.webMessageContainer}>
        <Text style={styles.webMessageText}>
          Ads are not available on web platform. Please use the mobile app to watch ads and earn rewards.
        </Text>
      </View>
    );
  };

  // Ad not available message
  const renderAdNotAvailableMessage = () => {
    if (isAdAvailable || isWebPlatform) return null;
    
    return (
      <View style={styles.webMessageContainer}>
        <Text style={styles.webMessageText}>
          Ads are temporarily unavailable. Please try again later.
        </Text>
      </View>
    );
  };

  if (isWatching || isAdLoading) {
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
              
              <Text style={styles.watchingTitle}>
                {isAdLoading ? 'Loading Advertisement...' : 'Advertisement Playing'}
              </Text>
              <Text style={styles.watchingSubtitle}>
                {isAdLoading 
                  ? 'Preparing your ad...' 
                  : `Watch to earn ${reward} EKH reward!`}
              </Text>
              
              {/* Countdown or Loading Indicator */}
              {isAdLoading ? (
                <View style={styles.loadingIndicator}>
                  <Text style={styles.loadingText}>Loading...</Text>
                </View>
              ) : null}
              
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
              
              {/* Test Controls */}
              {renderTestControls()}
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
            
            {/* Platform Messages */}
            {renderWebPlatformMessage()}
            {renderAdNotAvailableMessage()}
            
            {/* Action Buttons */}
            <View style={styles.buttonContainer}>
              <TouchableOpacity style={styles.cancelButton} onPress={onClose}>
                <Text style={styles.cancelButtonText}>Cancel</Text>
              </TouchableOpacity>
              
              <TouchableOpacity 
                style={styles.watchButton} 
                onPress={handleStartAd}
                disabled={isAdLoading || isWebPlatform || !isAdAvailable}
              >
                <LinearGradient
                  colors={(isWebPlatform || !isAdAvailable) ? ['#6b7280', '#4b5563'] : ['#ffa000', '#ff8f00']}
                  style={styles.watchButtonGradient}
                >
                  <Play size={16} color="#ffffff" />
                  <Text style={styles.watchButtonText}>
                    {isWebPlatform ? 'Not Available' : (!isAdAvailable ? 'Not Available' : (isAdLoading ? 'Loading...' : 'Watch Ad'))}
                  </Text>
                </LinearGradient>
              </TouchableOpacity>
            </View>
            
            {/* Test Controls */}
            {renderTestControls()}
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
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  container: {
    width: '85%',
    maxWidth: 350,
    borderRadius: 20,
    overflow: 'hidden',
  },
  card: {
    padding: 24,
    alignItems: 'center',
  },
  closeButton: {
    position: 'absolute',
    top: 16,
    right: 16,
    zIndex: 1,
  },
  rewardIconContainer: {
    width: 60,
    height: 60,
    borderRadius: 30,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
  },
  rewardIcon: {
    width: '100%',
    height: '100%',
    borderRadius: 30,
    justifyContent: 'center',
    alignItems: 'center',
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 8,
    textAlign: 'center',
  },
  description: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
    marginBottom: 24,
  },
  rewardDisplay: {
    width: '100%',
    marginBottom: 30,
  },
  rewardCard: {
    borderRadius: 16,
    padding: 16,
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
    color: '#ffffff',
  },
  rewardLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
  },
  webMessageContainer: {
    width: '100%',
    padding: 12,
    backgroundColor: 'rgba(255, 160, 0, 0.2)',
    borderRadius: 8,
    marginBottom: 20,
  },
  webMessageText: {
    color: '#ffa000',
    fontSize: 12,
    textAlign: 'center',
  },
  buttonContainer: {
    flexDirection: 'row',
    gap: 12,
    width: '100%',
  },
  cancelButton: {
    flex: 1,
    paddingVertical: 16,
    paddingHorizontal: 20,
    borderRadius: 16,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    alignItems: 'center',
  },
  cancelButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  watchButton: {
    flex: 2,
    borderRadius: 16,
    overflow: 'hidden',
  },
  watchButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
    gap: 8,
  },
  watchButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  watchingContainer: {
    width: '85%',
    maxWidth: 350,
    borderRadius: 20,
    overflow: 'hidden',
  },
  watchingCard: {
    padding: 24,
    alignItems: 'center',
  },
  playingIconContainer: {
    position: 'relative',
    width: 80,
    height: 80,
    marginBottom: 20,
  },
  playingIcon: {
    width: '100%',
    height: '100%',
    borderRadius: 40,
    justifyContent: 'center',
    alignItems: 'center',
  },
  pulseRing: {
    position: 'absolute',
    width: '100%',
    height: '100%',
    borderRadius: 40,
    borderWidth: 2,
    borderColor: 'rgba(255, 160, 0, 0.5)',
    top: 0,
    left: 0,
  },
  watchingTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 8,
    textAlign: 'center',
  },
  watchingSubtitle: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
    marginBottom: 24,
  },
  loadingIndicator: {
    marginBottom: 24,
  },
  loadingText: {
    fontSize: 16,
    color: '#ffffff',
    textAlign: 'center',
  },
  countdown: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 20,
  },
  progressContainer: {
    width: '100%',
    height: 8,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 4,
    overflow: 'hidden',
    marginBottom: 20,
  },
  progressBar: {
    height: '100%',
    borderRadius: 4,
  },
  progressFill: {
    height: '100%',
    borderRadius: 4,
  },
  skipButton: {
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 12,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
  },
  skipButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
  skipText: {
    color: 'rgba(255, 255, 255, 0.5)',
    fontSize: 14,
  },
  // Test mode styles
  testControls: {
    marginTop: 20,
    padding: 12,
    backgroundColor: 'rgba(255, 0, 0, 0.2)',
    borderRadius: 8,
    width: '100%',
  },
  testLabel: {
    color: '#ff6b6b',
    fontSize: 12,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 8,
  },
  testButtons: {
    flexDirection: 'row',
    gap: 8,
  },
  testButton: {
    flex: 1,
    padding: 8,
    borderRadius: 4,
    alignItems: 'center',
  },
  testSuccessButton: {
    backgroundColor: 'rgba(0, 255, 0, 0.2)',
  },
  testErrorButton: {
    backgroundColor: 'rgba(255, 0, 0, 0.2)',
  },
  testButtonText: {
    color: '#ffffff',
    fontSize: 10,
    fontWeight: '600',
  },
});