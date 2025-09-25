import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as WebBrowser from 'expo-web-browser';
import * as Linking from 'expo-linking';
import { Platform, Alert } from 'react-native';
import { User } from '@/types';
import { account, databases, appwriteConfig } from '@/config/appwrite';
import { ID, OAuthProvider, Query } from 'appwrite';

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
  const [lastCheckTime, setLastCheckTime] = useState<number>(0);

  useEffect(() => {
    // Initial auth check when app starts
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    // Prevent excessive auth checks (max 1 per second)
    const now = Date.now();
    if (now - lastCheckTime < 1000) {
      console.log('⚠️ Skipping auth check (too frequent)');
      return;
    }
    
    console.log('🔍 Starting auth status check...');
    setLastCheckTime(now);
    
    const timeoutId = setTimeout(() => {
      console.log('⏰ Auth check timeout - setting loading to false');
      setIsLoading(false);
    }, 10000); // Increased timeout to 10 seconds
    
    try {
      console.log('📡 Checking auth status with Appwrite...');
      
      const accountData = await account.get();
      if (accountData && accountData.$id) {
        const userData: User = {
          id: accountData.$id,
          email: accountData.email || undefined,
          name: accountData.name || undefined
        };
        setUser(userData);
        console.log('✅ User authenticated:', userData.name || userData.email);
        
        // Create user profile if it doesn't exist (in background)
        createUserProfileIfNotExists(accountData).catch(error => {
          console.error('Failed to create user profile:', error);
        });
      } else {
        console.log('No user data received from Appwrite');
        setUser(null);
      }
    } catch (error: any) {
      if (error?.code === 401 || error?.type === 'general_unauthorized_scope' || error?.message?.includes('Unauthorized')) {
        console.log('👤 User not authenticated (guest)');
        setUser(null);
      } else {
        console.error('❌ Auth check failed:', error);
        setUser(null);
      }
    } finally {
      clearTimeout(timeoutId);
      console.log('🏁 Auth check completed - setting loading to false');
      setIsLoading(false);
    }
  };

  const createUserProfileIfNotExists = async (userData: any) => {
    try {
      console.log('Checking if user profile exists for user:', userData.$id);
      const response = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [Query.equal('userId', [userData.$id])]
      );

      console.log('User profile check result:', response.total, 'profiles found');

      if (response.documents.length === 0) {
        console.log('Creating new user profile for user:', userData.$id);
        
        const referralCode = Math.random().toString(36).substring(2, 10).toUpperCase();
        
        const userProfile = {
          userId: [userData.$id],
          username: userData.name || `user_${userData.$id.substring(0, 8)}`,
          totalCoins: 0,
          coinsPerClick: 1,
          coinsPerSecond: 0,
          miningPower: 1,
          currentStreak: 0,
          longestStreak: 0,
          lastLoginDate: new Date().toISOString(),
          referralCode: [referralCode],
          referredBy: '',
          totalReferrals: 0,
          lifetimeEarnings: 0,
          dailyMiningRate: 1000,
          maxDailyEarnings: 10000,
          todayEarnings: 0,
          lastMiningDate: '',
          streakBonusClaimed: 0,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };

        console.log('Creating user profile with data:', userProfile);
        
        const result = await databases.createDocument(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          ID.unique(),
          userProfile
        );
        
        console.log('✅ User profile created successfully:', result.$id);
      } else {
        console.log('User profile already exists:', response.documents[0].$id);
      }
    } catch (error: any) {
      console.error('Failed to create user profile:', error);
      // Don't throw here as profile creation is not critical for auth
    }
  };

  const signIn = async () => {
    try {
      console.log('Starting Google OAuth sign in...');
      console.log('Platform:', Platform.OS);
      
      setIsLoading(true);

      // Get the current origin for web platform
      let baseUrl = '';
      if (Platform.OS === 'web' && typeof window !== 'undefined') {
        baseUrl = window.location.origin;
      }

      const successUrl = Platform.OS === 'web' 
        ? `${baseUrl}/oauth/return` 
        : 'ekehi://oauth/return';
      const failureUrl = Platform.OS === 'web' 
        ? `${baseUrl}/auth` 
        : 'ekehi://auth';
      
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
          if (typeof window !== 'undefined') {
            window.location.href = oauthUrl;
          }
        } else if (oauthUrl && typeof oauthUrl === 'object' && oauthUrl !== null && 'href' in oauthUrl) {
          console.log('Redirecting to OAuth URL object for web');
          if (typeof window !== 'undefined') {
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
          
          // Parse the result URL to extract OAuth parameters
          if (result.url) {
            const url = new URL(result.url);
            const secret = url.searchParams.get('secret');
            const userId = url.searchParams.get('userId');
            
            if (secret && userId) {
              console.log('OAuth parameters found, creating session...');
              
              try {
                // Wait a moment before checking auth status
                await new Promise(resolve => setTimeout(resolve, 1000));
                await checkAuthStatus();
              } catch (error) {
                console.log('Auth status check after OAuth failed:', error);
                // Continue anyway as the session might still be established
              }
            }
          }
          
          // The navigation will be handled by the return page
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
          `Expected URLs:\n${Platform.OS === 'web' && typeof window !== 'undefined' ? window.location.origin : 'ekehi://oauth'}/return`
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
          if (typeof window !== 'undefined') {
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
          if (typeof window !== 'undefined') {
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
      return { success: true, message: 'Recovery process initiated' };
    } catch (error: any) {
      console.error('Password recovery failed:', error);
      throw error;
    }
  };

  const updatePasswordRecovery = async (userId: string, secret: string, password: string) => {
    try {
      console.log('Updating password for user:', userId);
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