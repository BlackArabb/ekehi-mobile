import { Platform } from 'react-native';
import type { RewardedAd as TRewardedAd, RewardedAdEventType as TRewardedAdEventType, TestIds as TTestIds, AdEventType as TAdEventType } from 'react-native-google-mobile-ads';

// Only import AdMob modules if we're not on web platform
let RewardedAd: typeof TRewardedAd | undefined, 
    RewardedAdEventType: typeof TRewardedAdEventType | undefined, 
    TestIds: typeof TTestIds | undefined, 
    AdEventType: typeof TAdEventType | undefined;

if (Platform.OS !== 'web') {
  try {
    ({ RewardedAd, RewardedAdEventType, TestIds, AdEventType } = require('react-native-google-mobile-ads'));
  } catch (error) {
    console.error('[AdMobService] Failed to import react-native-google-mobile-ads:', error);
  }
}

// Test Ad Unit ID for rewarded ads (Android)
const TEST_REWARDED_AD_UNIT_ID = Platform.OS !== 'web' && TestIds ? TestIds.REWARDED : '';

// Production Ad Unit ID - Using your real ID
const PROD_REWARDED_AD_UNIT_ID = 'ca-app-pub-6750107449379811/9311091493'; // Ekehi Bonus

class AdMobService {
  private isInitialized: boolean = false;
  private adUnitId: string;
  private rewardedAd: any = null;
  private isAdLoaded: boolean = false;

  constructor() {
    // Use test ads in development, real ads in production
    // Don't initialize ads on web platform
    if (Platform.OS === 'web') {
      this.adUnitId = '';
      console.log('[AdMobService] Ads not supported on web platform');
    } else {
      this.adUnitId = __DEV__ ? TEST_REWARDED_AD_UNIT_ID : PROD_REWARDED_AD_UNIT_ID;
      console.log('[AdMobService] Using ad unit ID:', this.adUnitId, 'Environment:', __DEV__ ? 'Development' : 'Production');
    }
  }

  /**
   * Initialize AdMob
   */
  async initialize(): Promise<void> {
    // Don't initialize on web
    if (Platform.OS === 'web') {
      console.log('[AdMobService] Skipping initialization on web platform');
      return;
    }

    // Check if required modules are available
    if (!RewardedAd || !RewardedAdEventType || !AdEventType) {
      console.error('[AdMobService] Required AdMob modules not available');
      return;
    }

    if (this.isInitialized) {
      return;
    }

    try {
      // Create rewarded ad instance
      this.rewardedAd = RewardedAd.createForAdRequest(this.adUnitId, {
        requestNonPersonalizedAdsOnly: false,
      });

      // Set up event listeners
      this.setupAdListeners();

      this.isInitialized = true;
      console.log('[AdMobService] Initialized successfully with ad unit ID:', this.adUnitId);
    } catch (error) {
      console.error('[AdMobService] Initialization failed:', error);
      // Don't set isInitialized to true if initialization failed
      this.isInitialized = false;
    }
  }

  /**
   * Set up ad event listeners
   */
  private setupAdListeners(): void {
    // Don't set up listeners on web
    if (Platform.OS === 'web' || !this.rewardedAd || !RewardedAdEventType || !AdEventType) return;

    this.rewardedAd.addAdEventListener(RewardedAdEventType?.LOADED, () => {
      console.log('[AdMobService] Rewarded ad loaded');
      this.isAdLoaded = true;
    });

    this.rewardedAd.addAdEventListener(RewardedAdEventType?.EARNED_REWARD, (reward: any) => {
      console.log('[AdMobService] User earned reward:', reward);
    });
  
    // Add error listener
    this.rewardedAd.addAdEventListener(AdEventType?.ERROR, (error: any) => {
      console.error('[AdMobService] Ad error:', error);
      this.isAdLoaded = false;
    });
  }

  /**
   * Load a rewarded ad
   */
  async loadRewardedAd(): Promise<boolean> {
    // Don't load ads on web
    if (Platform.OS === 'web') {
      console.log('[AdMobService] Skipping ad load on web platform');
      return false;
    }

    try {
      await this.initialize();

      if (!this.rewardedAd) {
        console.error('[AdMobService] Rewarded ad instance not initialized');
        return false;
      }

      // Reset loaded state
      this.isAdLoaded = false;

      // Load the ad
      this.rewardedAd.load();

      // Wait for ad to load (with timeout)
      const loadTimeout = 10000; // 10 seconds
      const startTime = Date.now();
      
      while (!this.isAdLoaded && Date.now() - startTime < loadTimeout) {
        await new Promise(resolve => setTimeout(resolve, 100));
      }

      if (this.isAdLoaded) {
        console.log('[AdMobService] Rewarded ad loaded successfully');
        return true;
      } else {
        console.error('[AdMobService] Rewarded ad load timeout');
        return false;
      }
    } catch (error) {
      console.error('[AdMobService] Failed to load rewarded ad:', error);
      return false;
    }
  }

  /**
   * Show a rewarded ad
   */
  async showRewardedAd(): Promise<{ success: boolean; reward?: number; error?: string }> {
    // Don't show ads on web
    if (Platform.OS === 'web') {
      console.log('[AdMobService] Ads not supported on web platform');
      return {
        success: false,
        error: 'Ads not supported on web platform'
      };
    }

    // Check if required modules are available
    if (!RewardedAdEventType || !AdEventType) {
      console.error('[AdMobService] Required AdMob modules not available');
      return {
        success: false,
        error: 'AdMob modules not available'
      };
    }

    try {
      await this.initialize();

      if (!this.rewardedAd) {
        return {
          success: false,
          error: 'Ad instance not initialized'
        };
      }

      // Load the ad if not already loaded
      if (!this.isAdLoaded) {
        const loaded = await this.loadRewardedAd();
        if (!loaded) {
          return {
            success: false,
            error: 'Failed to load ad'
          };
        }
      }

      // Set up promise to track ad completion
      return new Promise((resolve) => {
        let rewardEarned = false;
        let rewardAmount = 0;

        // Create temporary event listeners
        const unsubscribeEarned = this.rewardedAd!.addAdEventListener(
          RewardedAdEventType?.EARNED_REWARD,
          (reward: any) => {
            console.log('[AdMobService] User earned reward:', reward);
            rewardEarned = true;
            rewardAmount = reward.amount || 0.5;
          }
        );

        const unsubscribeDismissed = this.rewardedAd!.addAdEventListener(
          AdEventType?.CLOSED,
          () => {
            console.log('[AdMobService] Ad closed');
            this.isAdLoaded = false;

            // Clean up listeners
            unsubscribeEarned();
            unsubscribeDismissed();

            if (rewardEarned) {
              resolve({
                success: true,
                reward: rewardAmount
              });
            } else {
              resolve({
                success: false,
                error: 'Ad closed without reward'
              });
            }
          }
        );

        // Show the ad
        this.rewardedAd!.show();
      });
    } catch (error: any) {
      console.error('[AdMobService] Failed to show rewarded ad:', error);
      this.isAdLoaded = false;
      return {
        success: false,
        error: error.message || 'Failed to show ad'
      };
    }
  }

  /**
   * Get the current ad unit ID
   */
  getAdUnitId(): string {
    return this.adUnitId;
  }

  /**
   * Check if AdMob is initialized
   */
  isAdMobInitialized(): boolean {
    return this.isInitialized;
  }

  /**
   * Set a custom ad unit ID (useful for testing different ad units)
   */
  setAdUnitId(adUnitId: string): void {
    // Don't set ad unit ID on web
    if (Platform.OS === 'web') return;
    
    this.adUnitId = adUnitId;
    this.isInitialized = false;
    this.rewardedAd = null;
    this.isAdLoaded = false;
  }
}

// Export singleton instance
export default new AdMobService();