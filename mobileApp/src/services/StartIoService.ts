import { Platform, NativeModules } from 'react-native';

console.log('[StartIoService] Platform:', Platform.OS);

// Start.io module state
let isModuleAvailable = false;
let moduleLoadError: Error | null = null;
let startIoNative: any = null;

// Load Start.io native module only on Android
if (Platform.OS === 'android') {
  console.log('[StartIoService] Android platform detected, loading Start.io native module...');
  
  try {
    // The native module is exposed through NativeModules
    // First, let's try to get it from the expo-startio package's native binding
    console.log('[StartIoService] Available NativeModules:', Object.keys(NativeModules));
    
    // Try different possible names for the Start.io native module
    const possibleNames = ['ExpoStartio', 'StartIo', 'RNStartio', 'StartioModule'];
    
    for (const name of possibleNames) {
      if (NativeModules[name]) {
        console.log(`[StartIoService] Found native module: ${name}`);
        startIoNative = NativeModules[name];
        break;
      }
    }

    // If not found in NativeModules, try to access via the package
    if (!startIoNative) {
      console.log('[StartIoService] Trying to access native module via @kastorcode/expo-startio...');
      try {
        const expoStartio = require('@kastorcode/expo-startio');
        console.log('[StartIoService] expo-startio exports:', Object.keys(expoStartio));
        
        // The native module might be exposed as a property
        if (expoStartio.NativeStartioModule) {
          startIoNative = expoStartio.NativeStartioModule;
          console.log('[StartIoService] Found NativeStartioModule');
        } else if (expoStartio.default && typeof expoStartio.default.initStartio === 'function') {
          // If it's a wrapper with actual functions, use it directly
          startIoNative = expoStartio.default;
          console.log('[StartIoService] Using exported functions from package');
        }
      } catch (error) {
        console.warn('[StartIoService] Failed to access via package:', error);
      }
    }

    if (startIoNative) {
      console.log('[StartIoService] Native module available, methods:', Object.keys(startIoNative));
      isModuleAvailable = true;
      console.log('[StartIoService] ✅ Start.io native module loaded');
    } else {
      throw new Error('Could not locate Start.io native module');
    }
  } catch (error) {
    console.error('[StartIoService] ❌ Failed to load Start.io native module:', error);
    moduleLoadError = error as Error;
    isModuleAvailable = false;
    startIoNative = null;
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
      console.log('[StartIoService] Start.io not supported on this platform');
    } else {
      this.appId = '209257659';
      console.log('[StartIoService] Using Start.io App ID:', this.appId);
    }
  }

  /**
   * Safely call a native function
   */
  private async callNativeFunction(
    functionName: string,
    ...args: any[]
  ): Promise<any> {
    if (!isModuleAvailable || !startIoNative) {
      console.error(`[StartIoService] Native module not available, cannot call ${functionName}`);
      return false;
    }

    const fn = startIoNative[functionName];
    if (typeof fn !== 'function') {
      console.error(`[StartIoService] Function ${functionName} is not a function. Type: ${typeof fn}`);
      console.error(`[StartIoService] Available methods:`, Object.keys(startIoNative));
      return false;
    }

    try {
      console.log(`[StartIoService] Calling native function: ${functionName} with args:`, args);
      const result = await fn.apply(startIoNative, args);
      console.log(`[StartIoService] ${functionName} returned:`, result);
      return result;
    } catch (error) {
      console.error(`[StartIoService] Error calling ${functionName}:`, error);
      throw error;
    }
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
      console.warn('[StartIoService] Start.io not available');
      if (moduleLoadError) {
        console.warn('[StartIoService] Error:', moduleLoadError.message);
      }
      return;
    }

    if (this.isInitialized) {
      console.log('[StartIoService] Already initialized');
      return;
    }

    try {
      console.log('[StartIoService] Initializing with App ID:', this.appId, 'Dev:', __DEV__);
      const result = await this.callNativeFunction('initStartio', this.appId, __DEV__);
      console.log('[StartIoService] Init result:', result);

      // Try to set user consent
      try {
        await this.callNativeFunction('setUserConsentStartio', 'pas');
      } catch (err) {
        console.warn('[StartIoService] Failed to set consent:', err);
      }

      // Try to set ad frequency
      try {
        await this.callNativeFunction('setSecondsBetweenAdsStartio', 60);
      } catch (err) {
        console.warn('[StartIoService] Failed to set seconds:', err);
      }

      try {
        await this.callNativeFunction('setActivitiesBetweenAdsStartio', 3);
      } catch (err) {
        console.warn('[StartIoService] Failed to set activities:', err);
      }

      this.isInitialized = true;
      console.log('[StartIoService] ✅ Initialized successfully');
    } catch (error) {
      console.error('[StartIoService] ❌ Initialization failed:', error);
      this.isInitialized = false;
    }
  }

  /**
   * Show a rewarded ad
   */
  async showRewardedAd(): Promise<{ success: boolean; reward?: number; error?: string }> {
    console.log('[StartIoService] showRewardedAd called');

    if (Platform.OS !== 'android') {
      return { success: false, error: 'Not supported on this platform' };
    }

    if (!isModuleAvailable) {
      return { success: false, error: 'Start.io not available' };
    }

    try {
      await this.initialize();

      console.log('[StartIoService] Showing rewarded ad...');
      const result = await this.callNativeFunction('showAdStartio');
      console.log('[StartIoService] Show ad result:', result);

      if (result) {
        console.log('[StartIoService] ✅ Ad shown successfully');
        return { success: true, reward: 0.5 };
      }

      return { success: false, error: 'Ad failed to show' };
    } catch (error: any) {
      console.error('[StartIoService] ❌ Failed to show ad:', error);
      return { success: false, error: error?.message || 'Failed to show ad' };
    }
  }

  /**
   * Show an exit ad
   */
  async showExitAd(): Promise<boolean> {
    if (Platform.OS !== 'android' || !isModuleAvailable) {
      return false;
    }

    if (this.exitAdShown) {
      return false;
    }

    try {
      await this.initialize();
      console.log('[StartIoService] Showing exit ad...');
      const result = await this.callNativeFunction('showAdStartio');

      if (result) {
        this.exitAdShown = true;
        console.log('[StartIoService] ✅ Exit ad shown');
        return true;
      }

      return false;
    } catch (error) {
      console.error('[StartIoService] ❌ Exit ad failed:', error);
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
    console.log('[StartIoService] isStartIoAvailable:', isModuleAvailable);
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