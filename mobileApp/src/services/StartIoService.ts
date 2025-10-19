import { Platform, NativeModules } from 'react-native';

console.log('[StartIoService] Platform:', Platform.OS);
console.log('[StartIoService] Available native modules:', Object.keys(NativeModules));

// Start.io module state
let isModuleAvailable = false;
let moduleLoadError: Error | null = null;
let startIoModule: any = null;

// Load Start.io module only on Android
if (Platform.OS === 'android') {
  console.log('[StartIoService] Android platform detected, loading Start.io...');
  
  // Try NativeModules first (more reliable)
  try {
    console.log('[StartIoService] Trying NativeModules.ExpoStartio...');
    const nativeStartio = NativeModules.ExpoStartio;
    console.log('[StartIoService] NativeModules.ExpoStartio available:', !!nativeStartio);
    
    if (nativeStartio) {
      console.log('[StartIoService] NativeModules.ExpoStartio methods:', Object.keys(nativeStartio));
      startIoModule = nativeStartio;
      isModuleAvailable = true;
      console.log('[StartIoService] ✅ Loaded Start.io via NativeModules');
    } else {
      console.warn('[StartIoService] NativeModules.ExpoStartio is null');
    }
  } catch (error) {
    console.warn('[StartIoService] NativeModules approach failed:', error);
  }

  // Fallback to require if NativeModules didn't work
  if (!isModuleAvailable) {
    try {
      console.log('[StartIoService] Attempting to load @kastorcode/expo-startio via require...');
      startIoModule = require('@kastorcode/expo-startio');
      
      console.log('[StartIoService] Module loaded, type:', typeof startIoModule);
      console.log('[StartIoService] Module contents:', Object.keys(startIoModule));

      if (!startIoModule || typeof startIoModule !== 'object') {
        throw new Error('Start.io module is not a valid object');
      }

      // Verify essential functions exist
      const hasInit = typeof startIoModule.initStartio === 'function';
      const hasShowAd = typeof startIoModule.showAdStartio === 'function';

      console.log('[StartIoService] hasInit:', hasInit, 'hasShowAd:', hasShowAd);

      if (!hasInit || !hasShowAd) {
        const available = Object.keys(startIoModule).filter(
          key => typeof startIoModule[key] === 'function'
        );
        throw new Error(
          `Missing essential Start.io functions. Available: ${available.join(', ')}`
        );
      }

      isModuleAvailable = true;
      console.log('[StartIoService] ✅ Start.io module loaded successfully via require');
    } catch (error) {
      console.error('[StartIoService] ❌ Failed to load Start.io module:', error);
      moduleLoadError = error as Error;
      isModuleAvailable = false;
      startIoModule = null;
    }
  }

  if (!isModuleAvailable) {
    console.error('[StartIoService] ❌ Start.io module is not available on this device');
    console.warn('[StartIoService] This is likely because:');
    console.warn('[StartIoService] 1. The app was not built with expo prebuild');
    console.warn('[StartIoService] 2. The native module was not properly compiled');
    console.warn('[StartIoService] 3. The @kastorcode/expo-startio package is not installed');
  }
}

class StartIoService {
  private isInitialized: boolean = false;
  private appId: string;
  private isAdLoaded: boolean = false;
  private exitAdShown: boolean = false;

  constructor() {
    if (Platform.OS !== 'android') {
      this.appId = '';
      console.log('[StartIoService] Start.io ads not supported on this platform');
    } else {
      this.appId = '209257659';
      console.log('[StartIoService] Using Start.io App ID:', this.appId);
    }
  }

  /**
   * Safely get a function from the Start.io module
   */
  private getSafeFunction(functionName: string): ((...args: any[]) => Promise<any>) | null {
    if (!isModuleAvailable || !startIoModule) {
      console.warn(`[StartIoService] Module not available, cannot get ${functionName}`);
      return null;
    }

    const fn = startIoModule[functionName];
    if (typeof fn !== 'function') {
      console.warn(`[StartIoService] Function ${functionName} is not available. Available functions: ${Object.keys(startIoModule).join(', ')}`);
      return null;
    }

    return fn.bind(startIoModule);
  }

  /**
   * Initialize Start.io
   */
  async initialize(): Promise<void> {
    if (Platform.OS !== 'android') {
      console.log('[StartIoService] Skipping initialization on unsupported platform');
      return;
    }

    if (!isModuleAvailable) {
      console.warn('[StartIoService] Start.io modules not available');
      if (moduleLoadError) {
        console.warn('[StartIoService] Module load error:', moduleLoadError.message);
      }
      return;
    }

    if (this.isInitialized) {
      console.log('[StartIoService] Already initialized, skipping');
      return;
    }

    const initStartio = this.getSafeFunction('initStartio');
    if (!initStartio) {
      console.error('[StartIoService] initStartio function not available');
      return;
    }

    try {
      console.log('[StartIoService] Initializing with App ID:', this.appId, 'Dev mode:', __DEV__);
      const initResult = await initStartio(this.appId, __DEV__);
      console.log('[StartIoService] ✅ Initialization result:', initResult);

      // Set user consent
      const setUserConsent = this.getSafeFunction('setUserConsentStartio');
      if (setUserConsent) {
        try {
          const consentResult = await setUserConsent('pas');
          console.log('[StartIoService] User consent set:', consentResult);
        } catch (err) {
          console.warn('[StartIoService] Failed to set user consent:', err);
        }
      }

      // Set ad frequency
      const setSecondsBetweenAds = this.getSafeFunction('setSecondsBetweenAdsStartio');
      if (setSecondsBetweenAds) {
        try {
          const secondsResult = await setSecondsBetweenAds(60);
          console.log('[StartIoService] Seconds between ads set:', secondsResult);
        } catch (err) {
          console.warn('[StartIoService] Failed to set seconds between ads:', err);
        }
      }

      const setActivitiesBetweenAds = this.getSafeFunction('setActivitiesBetweenAdsStartio');
      if (setActivitiesBetweenAds) {
        try {
          const activitiesResult = await setActivitiesBetweenAds(3);
          console.log('[StartIoService] Activities between ads set:', activitiesResult);
        } catch (err) {
          console.warn('[StartIoService] Failed to set activities between ads:', err);
        }
      }

      this.isInitialized = true;
      console.log('[StartIoService] ✅ Initialized successfully');
    } catch (error) {
      console.error('[StartIoService] ❌ Initialization failed:', error);
      this.isInitialized = false;
    }
  }

  /**
   * Load a rewarded ad
   */
  async loadRewardedAd(): Promise<boolean> {
    if (Platform.OS !== 'android' || !isModuleAvailable) {
      console.warn('[StartIoService] Cannot load ad on this platform');
      return false;
    }

    try {
      await this.initialize();
      this.isAdLoaded = true;
      console.log('[StartIoService] Rewarded ad ready to show');
      return true;
    } catch (error) {
      console.error('[StartIoService] Failed to load rewarded ad:', error);
      return false;
    }
  }

  /**
   * Show a rewarded ad
   */
  async showRewardedAd(): Promise<{ success: boolean; reward?: number; error?: string }> {
    console.log('[StartIoService] showRewardedAd called');
    console.log('[StartIoService] Platform:', Platform.OS, 'isAvailable:', isModuleAvailable);

    if (Platform.OS !== 'android') {
      return { success: false, error: 'Ads not supported on this platform' };
    }

    if (!isModuleAvailable) {
      return { success: false, error: 'Start.io not available' };
    }

    const showAd = this.getSafeFunction('showAdStartio');
    if (!showAd) {
      return { success: false, error: 'showAdStartio function not available' };
    }

    try {
      await this.initialize();

      if (!this.isAdLoaded) {
        const loaded = await this.loadRewardedAd();
        if (!loaded) {
          return { success: false, error: 'Failed to load ad' };
        }
      }

      console.log('[StartIoService] Calling showAdStartio...');
      const result = await showAd();
      console.log('[StartIoService] showAdStartio result:', result);

      if (result) {
        console.log('[StartIoService] ✅ Rewarded ad shown successfully');
        return { success: true, reward: 0.5 };
      }

      return { success: false, error: 'Ad failed to show' };
    } catch (error: any) {
      console.error('[StartIoService] ❌ Failed to show rewarded ad:', error);
      this.isAdLoaded = false;
      return { success: false, error: error?.message || 'Failed to show ad' };
    }
  }

  /**
   * Show an exit ad
   */
  async showExitAd(): Promise<boolean> {
    if (Platform.OS !== 'android') {
      console.log('[StartIoService] Exit ads not supported on this platform');
      return false;
    }

    if (!isModuleAvailable) {
      console.warn('[StartIoService] Start.io not available for exit ad');
      return false;
    }

    const showAd = this.getSafeFunction('showAdStartio');
    if (!showAd) {
      console.error('[StartIoService] showAdStartio function not available');
      return false;
    }

    if (this.exitAdShown) {
      console.log('[StartIoService] Exit ad already shown, skipping');
      return false;
    }

    try {
      await this.initialize();
      console.log('[StartIoService] Showing exit ad...');
      const result = await showAd();
      console.log('[StartIoService] Exit ad show result:', result);

      if (result) {
        this.exitAdShown = true;
        console.log('[StartIoService] ✅ Exit ad shown successfully');
        return true;
      }

      console.warn('[StartIoService] Exit ad failed to show');
      return false;
    } catch (error) {
      console.error('[StartIoService] ❌ Failed to show exit ad:', error);
      return false;
    }
  }

  // Utility methods
  getAppId(): string {
    return this.appId;
  }

  isStartIoInitialized(): boolean {
    return this.isInitialized && isModuleAvailable;
  }

  isStartIoAvailable(): boolean {
    console.log('[StartIoService] isStartIoAvailable called, returning:', isModuleAvailable);
    return isModuleAvailable;
  }

  getModuleLoadError(): Error | null {
    return moduleLoadError;
  }

  setAppId(appId: string): void {
    if (Platform.OS !== 'android') return;
    this.appId = appId;
    this.isInitialized = false;
    this.isAdLoaded = false;
    this.exitAdShown = false;
  }

  resetExitAd(): void {
    this.exitAdShown = false;
  }
}

export default new StartIoService();