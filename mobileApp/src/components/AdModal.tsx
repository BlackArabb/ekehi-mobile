import { useState, useEffect } from 'react';
import { View, Text, Modal, TouchableOpacity, StyleSheet, Platform } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Play, X, Gift, Coins } from 'lucide-react-native';

// Conditional import for StartIoService - only import on Android platform
let StartIoService: any = null;
let isStartIoAvailable = false;
if (Platform.OS === 'android') {
  try {
    StartIoService = require('@/services/StartIoService').default;
    isStartIoAvailable = StartIoService.isStartIoAvailable && StartIoService.isStartIoAvailable();
  } catch (error) {
    console.error('[AdModal] Failed to import StartIoService:', error);
    isStartIoAvailable = false;
  }
}

interface AdModalProps {
  isVisible: boolean;
  onClose: () => void;
  onComplete: (result: { success: boolean; reward?: number; error?: string }) => Promise<void>;
  title?: string;
  description?: string;
  reward?: number;
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
  const [adState, setAdState] = useState<'idle' | 'loading' | 'playing' | 'completing'>('idle');
  const [countdown, setCountdown] = useState(30);
  const [canSkip, setCanSkip] = useState(false);
  const [isWebPlatform] = useState(Platform.OS === 'web');
  const [isAdAvailable] = useState(isStartIoAvailable && Platform.OS === 'android');
  const [error, setError] = useState<string | null>(null);

  // Notify test system of events
  const notifyTestEvent = (event: string, data?: any) => {
    if (onTestEvent) {
      onTestEvent(event, data);
    }
  };

  // Reset state when modal opens/closes
  useEffect(() => {
    if (isVisible) {
      setAdState('idle');
      setCountdown(30);
      setCanSkip(false);
      setError(null);
      notifyTestEvent('modal_opened', { title, reward });
    }
  }, [isVisible]);

  // Countdown timer - only runs when ad is actually playing
  useEffect(() => {
    let timer: ReturnType<typeof setInterval> | null = null;
    
    if (adState === 'playing' && countdown > 0) {
      timer = setInterval(() => {
        setCountdown(prev => {
          const newCount = prev - 1;
          if (newCount <= 5) {
            setCanSkip(true);
          }
          return newCount;
        });
      }, 1000);
    } else if (countdown === 0 && adState === 'playing') {
      // Ad completed naturally via countdown
      handleAdComplete(true);
    }

    return () => {
      if (timer) clearInterval(timer);
    };
  }, [adState, countdown]);

  const handleStartAd = async () => {
    // Validate platform
    if (isWebPlatform || !isAdAvailable || Platform.OS !== 'android') {
      console.log('[AdModal] Ads not supported on this platform');
      await onComplete({ success: false, error: 'Ads not supported on this platform' });
      onClose();
      return;
    }

    setAdState('loading');
    setError(null);
    
    try {
      notifyTestEvent('ad_loading', { appId: StartIoService.getAppId() });
      console.log('[AdModal] Starting to load rewarded ad...');

      // Call Start.io to show the ad
      const result = await StartIoService.showRewardedAd();
      
      console.log('[AdModal] Ad result:', result);

      if (result.success) {
        // Ad was shown successfully - now start the countdown
        notifyTestEvent('ad_playing', { reward: result.reward });
        setAdState('playing');
        setCountdown(30);
        setCanSkip(false);
      } else {
        // Ad failed to show
        const errorMsg = result.error || 'Failed to load advertisement';
        notifyTestEvent('ad_error', { error: errorMsg });
        console.error('[AdModal] Ad failed:', errorMsg);
        setError(errorMsg);
        setAdState('idle');
        
        // Notify parent of failure
        await onComplete({ success: false, error: errorMsg });
        onClose();
      }
    } catch (error: any) {
      const errorMsg = error.message || 'Unknown error occurred';
      notifyTestEvent('ad_exception', { error: errorMsg });
      console.error('[AdModal] Exception:', error);
      setError(errorMsg);
      setAdState('idle');
      
      // Notify parent of failure
      await onComplete({ success: false, error: errorMsg });
      onClose();
    }
  };

  const handleAdComplete = async (success: boolean) => {
    if (adState === 'completing') return; // Prevent duplicate calls
    
    setAdState('completing');
    
    try {
      if (success) {
        notifyTestEvent('ad_completed', { reward });
        await onComplete({ success: true, reward });
      } else {
        notifyTestEvent('ad_skipped', { timeRemaining: countdown });
        await onComplete({ success: false, error: 'Ad was skipped' });
      }
    } catch (error: any) {
      notifyTestEvent('ad_completion_error', { error: error.message });
      console.error('[AdModal] Completion error:', error);
    } finally {
      setAdState('idle');
      setCountdown(30);
      onClose();
    }
  };

  const handleSkip = () => {
    handleAdComplete(false);
  };

  // Test controls - development only
  const renderTestControls = () => {
    // @ts-ignore - __DEV__ is a React Native global
    if (!__DEV__) return null;
    
    return (
      <View style={styles.testControls}>
        <Text style={styles.testLabel}>TEST MODE</Text>
        <View style={styles.testButtons}>
          <TouchableOpacity 
            style={[styles.testButton, styles.testSuccessButton]}
            onPress={() => {
              setAdState('playing');
              setCountdown(0);
            }}
          >
            <Text style={styles.testButtonText}>Simulate Success</Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.testButton, styles.testErrorButton]}
            onPress={() => {
              setError('Test error');
              setAdState('idle');
              onClose();
            }}
          >
            <Text style={styles.testButtonText}>Simulate Error</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  // Show loading/playing state
  if (adState === 'loading' || adState === 'playing' || adState === 'completing') {
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
                {adState === 'loading' ? 'Loading Advertisement...' : 'Advertisement Playing'}
              </Text>
              <Text style={styles.watchingSubtitle}>
                {adState === 'loading' 
                  ? 'Preparing your ad...' 
                  : `Watch to earn ${reward} EKH reward!`}
              </Text>
              
              {/* Countdown */}
              {adState === 'playing' && <Text style={styles.countdown}>{countdown}</Text>}
              
              {/* Progress Bar */}
              {adState === 'playing' && (
                <View style={styles.progressContainer}>
                  <View style={styles.progressBar}>
                    <LinearGradient
                      colors={['#ffa000', '#ff8f00']}
                      style={[styles.progressFill, { width: `${((30 - countdown) / 30) * 100}%` }]}
                    />
                  </View>
                </View>
              )}
              
              {/* Skip Button */}
              {adState === 'playing' && canSkip ? (
                <TouchableOpacity style={styles.skipButton} onPress={handleSkip}>
                  <Text style={styles.skipButtonText}>Skip Advertisement</Text>
                </TouchableOpacity>
              ) : adState === 'playing' ? (
                <Text style={styles.skipText}>
                  Skip available in {Math.max(0, countdown - 5)}s
                </Text>
              ) : null}
              
              {renderTestControls()}
            </LinearGradient>
          </View>
        </View>
      </Modal>
    );
  }

  // Show initial prompt
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
            
            {/* Error Message */}
            {error && (
              <View style={styles.errorContainer}>
                <Text style={styles.errorText}>{error}</Text>
              </View>
            )}
            
            {/* Platform Messages */}
            {isWebPlatform && (
              <View style={styles.webMessageContainer}>
                <Text style={styles.webMessageText}>
                  Ads are not available on web platform. Please use the mobile app.
                </Text>
              </View>
            )}
            
            {!isWebPlatform && !isAdAvailable && (
              <View style={styles.webMessageContainer}>
                <Text style={styles.webMessageText}>
                  Ads are temporarily unavailable. Please try again later.
                </Text>
              </View>
            )}
            
            {/* Action Buttons */}
            <View style={styles.buttonContainer}>
              <TouchableOpacity style={styles.cancelButton} onPress={onClose}>
                <Text style={styles.cancelButtonText}>Cancel</Text>
              </TouchableOpacity>
              
              <TouchableOpacity 
                style={styles.watchButton} 
                onPress={handleStartAd}
                disabled={adState !== 'idle' || isWebPlatform || !isAdAvailable}
              >
                <LinearGradient
                  colors={(isWebPlatform || !isAdAvailable) ? ['#6b7280', '#4b5563'] : ['#ffa000', '#ff8f00']}
                  style={styles.watchButtonGradient}
                >
                  <Play size={16} color="#ffffff" />
                  <Text style={styles.watchButtonText}>
                    {isWebPlatform ? 'Not Available' : (!isAdAvailable ? 'Not Available' : 'Watch Ad')}
                  </Text>
                </LinearGradient>
              </TouchableOpacity>
            </View>
            
            {renderTestControls()}
          </LinearGradient>
        </View>
      </View>
    </Modal>
  );
};

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
  errorContainer: {
    width: '100%',
    padding: 12,
    backgroundColor: 'rgba(255, 0, 0, 0.2)',
    borderRadius: 8,
    marginBottom: 20,
  },
  errorText: {
    color: '#ff6b6b',
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

export default AdModal;
