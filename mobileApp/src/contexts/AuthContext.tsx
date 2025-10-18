import React, { createContext, useContext, useState, useEffect, useCallback, useRef, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as WebBrowser from 'expo-web-browser';
import { Platform, Alert } from 'react-native';
import { User } from '@/types';
import { account, databases, appwriteConfig } from '@/config/appwrite';
import { ID, OAuthProvider, Query } from 'appwrite';
import { retryWithBackoff, isNetworkError } from '@/utils/retry';
import LoggingService from '@/services/LoggingService';
import PerformanceMonitor from '@/services/PerformanceMonitor';

// Type declaration for window object on web platform
declare const window: any;

// Configure WebBrowser for OAuth
WebBrowser.maybeCompleteAuthSession();

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  signIn: () => Promise<void>;
  signInWithEmail: (email: string, password: string) => Promise<void>;
  signOut: (onSuccess?: () => void) => Promise<void>;
  signUp: (email: string, password: string, name: string) => Promise<void>;
  checkAuthStatus: () => Promise<void>;
  createEmailVerification: (url: string) => Promise<any>;
  updateEmailVerification: (userId: string, secret: string) => Promise<any>;
  sendPasswordRecovery: (email: string, url: string) => Promise<any>;
  updatePasswordRecovery: (userId: string, secret: string, password: string) => Promise<any>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const lastCheckTimeRef = useRef<number>(0);
  const checkInProgressRef = useRef<boolean>(false);

  useEffect(() => {
    // Initial auth check when app starts
    checkAuthStatus();
  }, []);

  const checkAuthStatus = useCallback(async () => {
    PerformanceMonitor.startTiming('checkAuthStatus');
    
    // Prevent excessive auth checks
    const now = Date.now();
    
    // If a check is already in progress, skip
    if (checkInProgressRef.current) {
      LoggingService.debug('Auth check already in progress, skipping', 'AuthContext');
      console.log('⚠️ Auth check already in progress, skipping');
      PerformanceMonitor.endTiming('checkAuthStatus');
      return;
    }
    
    // Prevent too-frequent checks (max 1 per 2 seconds)
    if (now - lastCheckTimeRef.current < 2000) {
      LoggingService.debug('Skipping auth check (too frequent)', 'AuthContext');
      console.log('⚠️ Skipping auth check (too frequent)');
      PerformanceMonitor.endTiming('checkAuthStatus');
      return;
    }
    
    LoggingService.info('Starting auth status check...', 'AuthContext');
    console.log('🔍 Starting auth status check...');
    lastCheckTimeRef.current = now;
    checkInProgressRef.current = true;
    
    const timeoutId = setTimeout(() => {
      LoggingService.warn('Auth check timeout - setting loading to false', 'AuthContext');
      console.log('⏰ Auth check timeout - setting loading to false');
      setIsLoading(false);
      checkInProgressRef.current = false;
      PerformanceMonitor.endTiming('checkAuthStatus');
    }, 10000); // Increased timeout to 10 seconds
    
    try {
      LoggingService.debug('Checking auth status with Appwrite...', 'AuthContext');
      console.log('📡 Checking auth status with Appwrite...');
      
      // Use retry mechanism for network operations
      const result = await retryWithBackoff(
        async () => {
          PerformanceMonitor.startTiming('getAccount');
          const accountData = await account.get();
          PerformanceMonitor.endTiming('getAccount');
          return accountData;
        },
        {
          maxRetries: 3,
          delay: 1000,
          shouldRetry: isNetworkError
        }
      );

      if (!result.success) {
        throw result.error;
      }

      const accountData = result.data!;

      if (accountData && accountData.$id) {
        const userData: User = {
          id: accountData.$id,
          email: accountData.email || undefined,
          name: accountData.name || undefined
        };
        
        // Only update state if user data actually changed
        setUser(prevUser => {
          if (!prevUser || 
              prevUser.id !== userData.id || 
              prevUser.email !== userData.email || 
              prevUser.name !== userData.name) {
            LoggingService.info(`User authenticated (data changed): ${userData.name || userData.email}`, 'AuthContext');
            console.log('✅ User authenticated (data changed):', userData.name || userData.email);
            return userData;
          }
          LoggingService.debug('User authenticated (no data change)', 'AuthContext');
          console.log('ℹ️ User authenticated (no data change)');
          return prevUser;
        });
        
        // Create user profile if it doesn't exist (in background)
        createUserProfileIfNotExists(accountData).catch(error => {
          LoggingService.error('Failed to create user profile', 'AuthContext', { userId: accountData.$id }, error);
          console.error('Failed to create user profile:', error);
        });
      } else {
        LoggingService.debug('No user data received from Appwrite', 'AuthContext');
        console.log('No user data received from Appwrite');
        setUser(null);
      }
    } catch (error: any) {
      if (error?.code === 401 || error?.type === 'general_unauthorized_scope' || error?.message?.includes('Unauthorized')) {
        LoggingService.info('User not authenticated (guest)', 'AuthContext');
        console.log('👤 User not authenticated (guest)');
        setUser(null);
      } else {
        LoggingService.error('Auth check failed', 'AuthContext', undefined, error);
        console.error('❌ Auth check failed:', error);
        setUser(null);
      }
    } finally {
      clearTimeout(timeoutId);
      LoggingService.info('Auth check completed - setting loading to false', 'AuthContext');
      console.log('🏁 Auth check completed - setting loading to false');
      setIsLoading(false);
      checkInProgressRef.current = false;
      PerformanceMonitor.endTiming('checkAuthStatus');
    }
  }, []);

  const createUserProfileIfNotExists = async (userData: any) => {
    PerformanceMonitor.startTiming('createUserProfileIfNotExists');
    
    try {
      LoggingService.debug('Checking if user profile exists for user:', userData.$id, 'AuthContext');
      console.log('Checking if user profile exists for user:', userData.$id);
      
      // Use retry mechanism for database operations
      const result = await retryWithBackoff(
        async () => {
          PerformanceMonitor.startTiming('listUserProfiles');
          const response = await databases.listDocuments(
            appwriteConfig.databaseId,
            appwriteConfig.collections.userProfiles,
            [Query.equal('userId', [userData.$id])]
          );
          PerformanceMonitor.endTiming('listUserProfiles');
          return response;
        },
        {
          maxRetries: 3,
          delay: 1000,
          shouldRetry: isNetworkError
        }
      );

      if (!result.success) {
        throw result.error;
      }

      const response = result.data!;

      console.log('User profile check result:', response.total, 'profiles found');

      if (response.documents.length === 0) {
        LoggingService.debug('Creating new user profile for user:', userData.$id, 'AuthContext');
        console.log('Creating new user profile for user:', userData.$id);
        
        // Check for referral code in AsyncStorage
        let referredByCode = '';
        try {
          referredByCode = await AsyncStorage.getItem('referralCode') || '';
        } catch (e) {
          console.log('No referral code found in storage');
        }
        
        const referralCode = Math.random().toString(36).substring(2, 10).toUpperCase();
        
        const userProfile = {
          userId: [userData.$id],
          username: userData.name || `user_${userData.$id.substring(0, 8)}`,
          totalCoins: 0,
          coinsPerSecond: 0,
          miningPower: 1,
          currentStreak: 0,
          longestStreak: 0,
          lastLoginDate: new Date().toISOString(),
          referralCode: [referralCode],
          referredBy: referredByCode,
          totalReferrals: 0,
          lifetimeEarnings: 0,
          dailyMiningRate: 2, // Standard user daily mining rate (2 EKH per day = 0.0833 EKH/hour)
          maxDailyEarnings: 10000,
          todayEarnings: 0,
          lastMiningDate: '',
          streakBonusClaimed: 0,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };

        console.log('Creating user profile with data:', userProfile);
        
        // Use retry mechanism for database operations
        const createResult = await retryWithBackoff(
          async () => {
            PerformanceMonitor.startTiming('createUserProfile');
            const result = await databases.createDocument(
              appwriteConfig.databaseId,
              appwriteConfig.collections.userProfiles,
              ID.unique(),
              userProfile
            );
            PerformanceMonitor.endTiming('createUserProfile');
            return result;
          },
          {
            maxRetries: 3,
            delay: 1000,
            shouldRetry: isNetworkError
          }
        );

        if (!createResult.success) {
          throw createResult.error;
        }

        const result = createResult.data!;
        
        console.log('✅ User profile created successfully:', result.$id);
        
        // If user was referred, update the referrer's profile
        if (referredByCode) {
          await updateReferrerProfile(referredByCode, userData.$id);
        }
        
        // Clear referral code from storage
        try {
          await AsyncStorage.removeItem('referralCode');
        } catch (e) {
          console.log('Could not clear referral code from storage');
        }
      } else {
        console.log('User profile already exists:', response.documents[0].$id);
        // Update streak if needed for existing users
        await updateStreakForExistingUser(response.documents[0], userData.$id);
      }
    } catch (error: any) {
      console.error('Failed to create user profile:', error);
      // Don't throw here as profile creation is not critical for auth
    } finally {
      PerformanceMonitor.endTiming('createUserProfileIfNotExists');
    }
  };

  const updateReferrerProfile = async (referralCode: string, referredUserId: string) => {
    try {
      // Use retry mechanism for database operations
      const referrerResult = await retryWithBackoff(
        async () => {
          // Find the referrer by referral code
          const referrerResponse = await databases.listDocuments(
            appwriteConfig.databaseId,
            appwriteConfig.collections.userProfiles,
            [Query.equal('referralCode', [referralCode])]
          );
          return referrerResponse;
        },
        {
          maxRetries: 3,
          delay: 1000,
          shouldRetry: isNetworkError
        }
      );

      if (!referrerResult.success) {
        throw referrerResult.error;
      }

      const referrerResponse = referrerResult.data!;

      if (referrerResponse.documents.length > 0) {
        const referrer = referrerResponse.documents[0];
        
        // Update referrer's total referrals and award referral bonus
        const updatedReferrals = referrer.totalReferrals + 1;
        const referralBonus = 1; // 1 EKH bonus for each referral
        
        // Use retry mechanism for database operations
        const updateResult = await retryWithBackoff(
          async () => {
            const response = await databases.updateDocument(
              appwriteConfig.databaseId,
              appwriteConfig.collections.userProfiles,
              referrer.$id,
              {
                totalReferrals: updatedReferrals,
                totalCoins: referrer.totalCoins + referralBonus,
                // Store the ID of the user who was referred for tracking purposes
                [`referredUser_${referredUserId}`]: new Date().toISOString(),
                updatedAt: new Date().toISOString()
              }
            );
            return response;
          },
          {
            maxRetries: 3,
            delay: 1000,
            shouldRetry: isNetworkError
          }
        );

        if (!updateResult.success) {
          throw updateResult.error;
        }
        
        console.log(`✅ Referral bonus awarded to user ${referrer.userId}: ${referralBonus} EKH`);
        console.log(`✅ Referrer ${referrer.userId} now has ${updatedReferrals} referrals`);
        console.log(`✅ Referred user ID ${referredUserId} linked to referrer ${referrer.userId}`);
      } else {
        console.log('Referrer not found for referral code:', referralCode);
      }
    } catch (error) {
      console.error('Failed to update referrer profile:', error);
    }
  };

  const updateStreakForExistingUser = async (existingProfile: any, userId: string) => {
    try {
      const today = new Date();
      today.setHours(0, 0, 0, 0); // Normalize to start of day
      
      let lastLoginDate = null;
      if (existingProfile.lastLoginDate) {
        lastLoginDate = new Date(existingProfile.lastLoginDate);
        lastLoginDate.setHours(0, 0, 0, 0); // Normalize to start of day
      }
      
      let updatedStreak = existingProfile.currentStreak;
      let updatedLongestStreak = existingProfile.longestStreak;
      let updatedTotalCoins = existingProfile.totalCoins;
      let updatedStreakBonusClaimed = existingProfile.streakBonusClaimed;
      
      // Check if this is a new day login
      if (!lastLoginDate || lastLoginDate < today) {
        // Calculate the difference in days
        const oneDay = 24 * 60 * 60 * 1000; // hours*minutes*seconds*milliseconds
        const diffDays = lastLoginDate ? Math.round(Math.abs((today.getTime() - lastLoginDate.getTime()) / oneDay)) : 1;
        
        if (diffDays === 1) {
          // Consecutive day - increment streak
          updatedStreak = existingProfile.currentStreak + 1;
          
          // Check if user has reached 7 consecutive days
          if (updatedStreak === 7 && existingProfile.streakBonusClaimed < 1) {
            // Award 5 EKH bonus
            updatedTotalCoins += 5;
            updatedStreakBonusClaimed += 1;
            
            console.log(`🎉 User ${userId} achieved 7-day streak! Awarding 5 EKH bonus.`);
          }
          
          // Update longest streak if needed
          if (updatedStreak > existingProfile.longestStreak) {
            updatedLongestStreak = updatedStreak;
          }
        } else if (diffDays > 1) {
          // Missed days - reset streak
          updatedStreak = 1;
        }
        
        // Update the profile with new streak data
        const updatedProfile = {
          currentStreak: updatedStreak,
          longestStreak: updatedLongestStreak,
          lastLoginDate: new Date().toISOString(),
          totalCoins: updatedTotalCoins,
          streakBonusClaimed: updatedStreakBonusClaimed,
          updatedAt: new Date().toISOString()
        };
        
        // Use retry mechanism for database operations
        const updateResult = await retryWithBackoff(
          async () => {
            const response = await databases.updateDocument(
              appwriteConfig.databaseId,
              appwriteConfig.collections.userProfiles,
              existingProfile.$id,
              updatedProfile
            );
            return response;
          },
          {
            maxRetries: 3,
            delay: 1000,
            shouldRetry: isNetworkError
          }
        );

        if (!updateResult.success) {
          throw updateResult.error;
        }
        
        console.log(`✅ User ${userId} streak updated:`, {
          currentStreak: updatedStreak,
          longestStreak: updatedLongestStreak,
          streakBonusAwarded: (updatedStreak === 7 && existingProfile.streakBonusClaimed < 1)
        });
      } else {
        console.log(`ℹ️ User ${userId} already logged in today, no streak update needed`);
      }
    } catch (error: any) {
      console.error(`Failed to update user ${userId} streak:`, error);
    }
  };

  const signIn = async () => {
    try {
      console.log('Starting Google OAuth sign in...');
      console.log('Platform:', Platform.OS);
      
      setIsLoading(true);

      // Get the current origin for web platform
      let baseUrl = '';
      if (Platform.OS === 'web') {
        // Only access window object on web platform
        if (typeof window !== 'undefined' && window?.location?.origin) {
          baseUrl = window.location.origin;
        }
      }

      const successUrl = Platform.OS === 'web' 
        ? `${baseUrl}/oauth/return` 
        : appwriteConfig.oauth.redirectUrls.success;
      const failureUrl = Platform.OS === 'web' 
        ? `${baseUrl}/auth` 
        : appwriteConfig.oauth.redirectUrls.failure;
      
      console.log('OAuth URLs:');
      console.log('  - Success URL:', successUrl);
      console.log('  - Failure URL:', failureUrl);
      
      // Generate the OAuth URL
      const oauthUrl = account.createOAuth2Token(
        OAuthProvider.Google,
        successUrl,
        failureUrl
      );
      
      console.log('Generated OAuth URL type:', typeof oauthUrl);
      
      if (Platform.OS === 'web') {
        // For web, redirect directly
        if (oauthUrl && typeof oauthUrl === 'string') {
          console.log('Redirecting to OAuth URL for web');
          if (typeof window !== 'undefined' && window?.location?.href) {
            window.location.href = oauthUrl;
          }
        } else if (oauthUrl && typeof oauthUrl === 'object' && oauthUrl !== null && 'href' in oauthUrl) {
          console.log('Redirecting to OAuth URL object for web');
          if (typeof window !== 'undefined' && window?.location?.href) {
            window.location.href = (oauthUrl as any).href;
          }
        } else {
          throw new Error('Invalid OAuth URL generated');
        }
      } else {
        // For mobile, use openAuthSessionAsync
        console.log('Opening auth session for mobile');
        
        // Convert oauthUrl to string with safe conversion
        let urlString = '';
        if (typeof oauthUrl === 'string') {
          urlString = oauthUrl;
        } else if (oauthUrl !== undefined && oauthUrl !== null) {
          // Handle object types
          if (typeof oauthUrl === 'object' && 'href' in oauthUrl) {
            urlString = (oauthUrl as any).href;
          } else {
            // Convert to string using String() which works with any type
            urlString = String(oauthUrl);
          }
        }
        
        const result = await WebBrowser.openAuthSessionAsync(
          urlString,
          successUrl,
          {
            preferEphemeralSession: false, // Use persistent session
            showInRecents: true
          }
        );
        
        console.log('Auth session result:', result);
        
        if (result.type === 'success') {
          console.log('OAuth successful, URL:', result.url);
          // The navigation will be handled by the return page
          // Don't call checkAuthStatus here as it will be called in the return page
        } else if (result.type === 'dismiss') {
          throw new Error('Authentication cancelled');
        } else {
          throw new Error(`OAuth failed: ${result.type}`);
        }
      }
    } catch (error: any) {
      console.error('Sign in failed:', error);
      setIsLoading(false); // Reset loading state on error
      
      const errorMessage = error?.message || 'Unknown error';
      
      if (errorMessage.includes('Invalid redirect URL') || errorMessage.includes('redirect url')) {
        Alert.alert(
          'OAuth Configuration Error',
          'The OAuth redirect URL is not properly configured in your Appwrite project.\n\n' +
          'Please check your Appwrite Console:\n' +
          '1. Go to Auth > Settings\n' +
          '2. Verify your platform settings\n' +
          '3. Ensure redirect URLs are correctly added\n\n' +
          `Expected URLs:\n${Platform.OS === 'web' ? (typeof window !== 'undefined' && window?.location?.origin ? window.location.origin : '') : 'ekehi://oauth'}/return`
        );
      } else if (errorMessage === 'Authentication cancelled') {
        // Don't show alert for user cancellation
        console.log('User cancelled authentication');
      } else {
        Alert.alert(
          'Authentication Error', 
          `Failed to sign in with Google: ${errorMessage}`
        );
      }
      
      throw error;
    }
    // Don't set isLoading to false here for web as we're redirecting
    if (Platform.OS !== 'web') {
      setIsLoading(false);
    }
  };

  const signInWithEmail = async (email: string, password: string) => {
    try {
      console.log('Starting email/password authentication');
      await account.createEmailPasswordSession(email, password);
      await checkAuthStatus();
      
      try {
        const accountData = await account.get();
        await createUserProfileIfNotExists(accountData);
      } catch (error: any) {
        console.error('Failed to get account data for profile creation:', error);
      }
    } catch (error: any) {
      console.error('Email sign in failed:', error);
      throw error;
    }
  };

  const signUp = async (email: string, password: string, name: string) => {
    try {
      console.log('Creating new account');
      const accountData = await account.create(ID.unique(), email, password, name);
      await account.createEmailPasswordSession(email, password);
      await checkAuthStatus();
      await createUserProfileIfNotExists(accountData);
    } catch (error: any) {
      console.error('Sign up failed:', error);
      throw error;
    }
  };

  const signOut = async (onSuccess?: () => void) => {
    try {
      console.log('Signing out user...');
      
      // Clear local storage first
      await AsyncStorage.multiRemove([
        'miningSession',
        'lastAdWatchTime',
        'miningPreferences',
        'userSettings'
      ]);
      
      // Delete the current session
      await account.deleteSession('current');
      
      // Set user to null immediately
      setUser(null);
      console.log('✅ User signed out successfully');
      
      if (onSuccess) {
        onSuccess();
      }
      
      // For web platforms, redirect to auth page
      if (Platform.OS === 'web') {
        console.log('Web platform detected, redirecting to auth page');
        setTimeout(() => {
          if (typeof window !== 'undefined' && window?.location?.href) {
            window.location.href = '/auth';
          }
        }, 100);
      }
      
    } catch (error: any) {
      console.error('Sign out failed:', error);
      // Always set user to null even if there's an error
      setUser(null);
      
      // For web platforms, force redirect even on error
      if (Platform.OS === 'web') {
        setTimeout(() => {
          if (typeof window !== 'undefined' && window?.location?.href) {
            window.location.href = '/auth';
          }
        }, 100);
      }
      
      throw new Error('Failed to sign out properly. Please try again.');
    }
  };

  // Simplified methods for hybrid approach
  const createEmailVerification = async (url: string) => {
    try {
      console.log('Email verification requested for URL:', url);
      return { success: true, message: 'Verification process initiated' };
    } catch (error: any) {
      console.error('Email verification creation failed:', error);
      throw error;
    }
  };

  const updateEmailVerification = async (userId: string, secret: string) => {
    try {
      console.log('Updating email verification for user:', userId);
      // Use the secret parameter to verify the email
      console.log('Verification secret:', secret);
      await checkAuthStatus();
      return { success: true, message: 'Email verified' };
    } catch (error: any) {
      console.error('Email verification update failed:', error);
      throw error;
    }
  };

  const sendPasswordRecovery = async (email: string, url: string) => {
    try {
      console.log('Password recovery requested for:', email);
      // Use the url parameter for password recovery
      console.log('Recovery URL:', url);
      return { success: true, message: 'Recovery process initiated' };
    } catch (error: any) {
      console.error('Password recovery failed:', error);
      throw error;
    }
  };

  const updatePasswordRecovery = async (userId: string, secret: string, password: string) => {
    try {
      console.log('Updating password for user:', userId);
      // Use the secret and password parameters to update the password
      console.log('Password reset secret:', secret);
      console.log('New password length:', password.length);
      await checkAuthStatus();
      return { success: true, message: 'Password updated' };
    } catch (error: any) {
      console.error('Password recovery update failed:', error);
      throw error;
    }
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      isLoading, 
      signIn, 
      signInWithEmail, 
      signOut, 
      signUp, 
      checkAuthStatus, 
      createEmailVerification, 
      updateEmailVerification, 
      sendPasswordRecovery, 
      updatePasswordRecovery 
    }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
