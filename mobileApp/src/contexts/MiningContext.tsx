import React, { createContext, useContext, useState, useEffect, useCallback, useRef, ReactNode } from 'react';
import { UserProfile, MiningSession } from '@/types';
import { useAuth } from '@/contexts/AuthContext';
import { usePresale } from '@/contexts/PresaleContext';
import { databases, appwriteConfig } from '@/config/appwrite';
import { Query, ID } from 'appwrite';
import { retryWithBackoff, isNetworkError } from '@/utils/retry';
import LoggingService from '@/services/LoggingService';
import PerformanceMonitor from '@/services/PerformanceMonitor';

interface MiningContextType {
  profile: UserProfile | null;
  isLoading: boolean;
  isMining: boolean;
  sessionCoins: number;
  sessionClicks: number;
  performMine: () => Promise<void>;
  addCoins: (amount: number) => Promise<void>;
  refreshProfile: () => Promise<void>;
  startMiningSession: () => void;
  endMiningSession: () => Promise<void>;
  // Add a silent refresh function for real-time updates
  silentRefreshProfile: () => Promise<void>;
  // Add a function to update only coins for better performance
  updateCoinsOnly: (newTotalCoins: number) => void;
  // Add a function to subscribe to profile updates
  subscribeToProfileUpdates: (callback: () => void) => () => void;
}

const MiningContext = createContext<MiningContextType | undefined>(undefined);

export function MiningProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const { purchases, fetchPurchases, calculateAutoMiningRate } = usePresale();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isMining, setIsMining] = useState(false);
  const [sessionCoins, setSessionCoins] = useState(0);
  const [sessionClicks, setSessionClicks] = useState(0);
  const [sessionStartTime, setSessionStartTime] = useState<number | null>(null);
  
  // Refs for debouncing and optimization
  const lastRefreshTimeRef = useRef<number>(0);
  const refreshTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const profileFetchPromiseRef = useRef<Promise<void> | null>(null);
  const profileUpdateListenersRef = useRef<(() => void)[]>([]);

  useEffect(() => {
    if (user) {
      LoggingService.info('User detected, fetching profile', 'MiningContext');
      console.log('MiningContext: User detected, fetching profile');
      refreshProfile();
    } else {
      // Clear profile data when user is null (signed out)
      LoggingService.info('User is null, clearing profile data', 'MiningContext');
      console.log('MiningContext: User is null, clearing profile data');
      setProfile(null);
      setIsLoading(false);
    }
  }, [user]);

  // Effect to update auto mining rate when purchases change
  useEffect(() => {
    if (user && profile) {
      updateAutoMiningRate();
    }
  }, [purchases, user, profile]);

  useEffect(() => {
    // Record mining session when component unmounts or when mining stops
    return () => {
      if (isMining) {
        recordMiningSession();
      }
      // Cleanup timeout on unmount
      if (refreshTimeoutRef.current) {
        clearTimeout(refreshTimeoutRef.current);
      }
    };
  }, [isMining]);

  // Add a function to subscribe to profile updates
  const subscribeToProfileUpdates = useCallback((callback: () => void) => {
    profileUpdateListenersRef.current.push(callback);
    return () => {
      profileUpdateListenersRef.current = profileUpdateListenersRef.current.filter(cb => cb !== callback);
    };
  }, []);

  // Add a function to notify all subscribers of profile updates
  const notifyProfileSubscribers = useCallback(() => {
    profileUpdateListenersRef.current.forEach(callback => {
      try {
        callback();
      } catch (error) {
        console.error('Error in profile update subscriber:', error);
      }
    });
  }, []);

  const refreshProfile = useCallback(async () => {
    PerformanceMonitor.startTiming('refreshProfile');
    
    // Debounce profile refreshes
    const now = Date.now();
    if (now - lastRefreshTimeRef.current < 2000) {
      LoggingService.debug('Skipping profile refresh (too frequent)', 'MiningContext');
      console.log('⚠️ Skipping profile refresh (too frequent)');
      PerformanceMonitor.endTiming('refreshProfile');
      return;
    }
    
    // If there's already a fetch in progress, return that promise
    if (profileFetchPromiseRef.current) {
      LoggingService.debug('Profile fetch already in progress, returning existing promise', 'MiningContext');
      console.log('🔄 Profile fetch already in progress, returning existing promise');
      PerformanceMonitor.endTiming('refreshProfile');
      return profileFetchPromiseRef.current;
    }
    
    // Create a new promise for the fetch operation
    profileFetchPromiseRef.current = (async () => {
      if (!user) return;
      
      LoggingService.info(`Refreshing user profile for user: ${user.id}`, 'MiningContext');
      console.log('🔄 Refreshing user profile for user:', user.id);
      lastRefreshTimeRef.current = now;
      setIsLoading(true);
      
      try {
        // Use retry mechanism for network operations
        const result = await retryWithBackoff(
          async () => {
            PerformanceMonitor.startTiming('fetchUserProfile');
            // Fetch user profile from Appwrite database
            const response = await databases.listDocuments(
              appwriteConfig.databaseId,
              appwriteConfig.collections.userProfiles,
              [Query.equal('userId', [user.id])]
            );
            PerformanceMonitor.endTiming('fetchUserProfile');
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

        LoggingService.debug(`Profile fetch result: ${response.total} profiles found`, 'MiningContext');
        console.log('Profile fetch result:', response.total, 'profiles found');

        if (response.documents.length > 0) {
          const doc = response.documents[0];
          const userProfile: UserProfile = {
            id: doc.$id,
            userId: doc.userId?.[0] || doc.userId, // Handle both array and string formats
            username: doc.username,
            totalCoins: doc.totalCoins,
            coinsPerSecond: doc.coinsPerSecond,
            miningPower: doc.miningPower,
            currentStreak: doc.currentStreak,
            longestStreak: doc.longestStreak,
            lastLoginDate: doc.lastLoginDate,
            referralCode: doc.referralCode?.[0] || doc.referralCode, // Handle both array and string formats
            referredBy: doc.referredBy,
            totalReferrals: doc.totalReferrals,
            lifetimeEarnings: doc.lifetimeEarnings,
            dailyMiningRate: doc.dailyMiningRate,
            maxDailyEarnings: doc.maxDailyEarnings,
            todayEarnings: doc.todayEarnings,
            lastMiningDate: doc.lastMiningDate,
            streakBonusClaimed: doc.streakBonusClaimed,
            createdAt: doc.createdAt,
            updatedAt: doc.updatedAt
          };
          
          // Only update state if profile data actually changed
          setProfile(prevProfile => {
            if (!prevProfile) {
              LoggingService.info(`User profile loaded (first load): ${userProfile.username || userProfile.userId}`, 'MiningContext');
              console.log('✅ User profile loaded (first load):', userProfile);
              return userProfile;
            }
            
            // Deep comparison to check if data actually changed
            const hasChanged = JSON.stringify(prevProfile) !== JSON.stringify(userProfile);
            if (hasChanged) {
              LoggingService.info(`User profile updated (data changed): ${userProfile.username || userProfile.userId}`, 'MiningContext');
              console.log('✅ User profile updated (data changed):', userProfile);
              return userProfile;
            }
            LoggingService.debug('User profile unchanged, skipping update', 'MiningContext');
            console.log('ℹ️ User profile unchanged, skipping update');
            return prevProfile;
          });
        } else {
          LoggingService.warn(`No user profile found for user: ${user.id}`, 'MiningContext');
          console.log('❌ No user profile found for user:', user.id);
          setProfile(null);
        }
      } catch (error: any) {
        LoggingService.error('Failed to fetch profile', 'MiningContext', { userId: user?.id }, error);
        console.error('Failed to fetch profile:', error);
        console.error('Error details:', {
          message: error.message,
          code: error.code,
          type: error.type
        });
        setProfile(null);
      } finally {
        setIsLoading(false);
        profileFetchPromiseRef.current = null; // Clear the promise reference
        PerformanceMonitor.endTiming('refreshProfile');
      }
    })();
    
    return profileFetchPromiseRef.current;
  }, [user]);

  // Silent refresh function that updates profile without visual loading state
  const silentRefreshProfile = useCallback(async () => {
    PerformanceMonitor.startTiming('silentRefreshProfile');
    
    // Debounce profile refreshes
    const now = Date.now();
    if (now - lastRefreshTimeRef.current < 2000) {
      console.log('⚠️ Skipping silent profile refresh (too frequent)');
      PerformanceMonitor.endTiming('silentRefreshProfile');
      return;
    }
    
    // If there's already a fetch in progress, return that promise
    if (profileFetchPromiseRef.current) {
      console.log('🔄 Silent profile fetch already in progress, returning existing promise');
      PerformanceMonitor.endTiming('silentRefreshProfile');
      return profileFetchPromiseRef.current;
    }
    
    // Create a new promise for the fetch operation
    profileFetchPromiseRef.current = (async () => {
      if (!user) return;
      
      console.log('🔄 Silently refreshing user profile for user:', user.id);
      lastRefreshTimeRef.current = now;
      
      try {
        // Use retry mechanism for network operations
        const result = await retryWithBackoff(
          async () => {
            PerformanceMonitor.startTiming('fetchUserProfileSilent');
            // Fetch user profile from Appwrite database
            const response = await databases.listDocuments(
              appwriteConfig.databaseId,
              appwriteConfig.collections.userProfiles,
              [Query.equal('userId', [user.id])]
            );
            PerformanceMonitor.endTiming('fetchUserProfileSilent');
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

        console.log('Silent profile fetch result:', response.total, 'profiles found');

        if (response.documents.length > 0) {
          const doc = response.documents[0];
          const userProfile: UserProfile = {
            id: doc.$id,
            userId: doc.userId?.[0] || doc.userId, // Handle both array and string formats
            username: doc.username,
            totalCoins: doc.totalCoins,
            coinsPerSecond: doc.coinsPerSecond,
            miningPower: doc.miningPower,
            currentStreak: doc.currentStreak,
            longestStreak: doc.longestStreak,
            lastLoginDate: doc.lastLoginDate,
            referralCode: doc.referralCode?.[0] || doc.referralCode, // Handle both array and string formats
            referredBy: doc.referredBy,
            totalReferrals: doc.totalReferrals,
            lifetimeEarnings: doc.lifetimeEarnings,
            dailyMiningRate: doc.dailyMiningRate,
            maxDailyEarnings: doc.maxDailyEarnings,
            todayEarnings: doc.todayEarnings,
            lastMiningDate: doc.lastMiningDate,
            streakBonusClaimed: doc.streakBonusClaimed,
            createdAt: doc.createdAt,
            updatedAt: doc.updatedAt
          };
          
          // Only update state if profile data actually changed
          setProfile(prevProfile => {
            if (!prevProfile) {
              console.log('✅ User profile loaded (silent):', userProfile);
              return userProfile;
            }
            
            // Deep comparison to check if data actually changed
            const hasChanged = JSON.stringify(prevProfile) !== JSON.stringify(userProfile);
            if (hasChanged) {
              console.log('✅ User profile updated (silent):', userProfile);
              return userProfile;
            }
            console.log('ℹ️ User profile unchanged (silent), skipping update');
            return prevProfile;
          });
        } else {
          console.log('❌ No user profile found for user (silent):', user.id);
          setProfile(null);
        }
      } catch (error: any) {
        console.error('Failed to silently fetch profile:', error);
        console.error('Error details:', {
          message: error.message,
          code: error.code,
          type: error.type
        });
        setProfile(null);
      } finally {
        profileFetchPromiseRef.current = null; // Clear the promise reference
        PerformanceMonitor.endTiming('silentRefreshProfile');
      }
    })();
    
    return profileFetchPromiseRef.current;
  }, [user]);

  const addCoins = useCallback(async (amount: number) => {
    PerformanceMonitor.startTiming('addCoins');
    
    if (!profile) {
      PerformanceMonitor.endTiming('addCoins');
      return;
    }

    try {
      // Use retry mechanism for network operations
      const result = await retryWithBackoff(
        async () => {
          PerformanceMonitor.startTiming('updateUserProfile');
          
          // Update user profile with new coin balance
          const updatedProfile = {
            ...profile,
            totalCoins: profile.totalCoins + amount,
            lifetimeEarnings: profile.lifetimeEarnings + amount,
            todayEarnings: profile.todayEarnings + amount,
            updatedAt: new Date().toISOString()
          };

          const response = await databases.updateDocument(
            appwriteConfig.databaseId,
            appwriteConfig.collections.userProfiles,
            profile.id,
            {
              totalCoins: updatedProfile.totalCoins,
              lifetimeEarnings: updatedProfile.lifetimeEarnings,
              todayEarnings: updatedProfile.todayEarnings,
              updatedAt: updatedProfile.updatedAt
            }
          );
          
          PerformanceMonitor.endTiming('updateUserProfile');
          return { response, updatedProfile };
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

      const data = result.data!;
      const updatedProfile = data.updatedProfile;

      // Update local state immediately for responsive UI
      setProfile(updatedProfile);
      
      // Notify subscribers of profile updates
      notifyProfileSubscribers();
      
      console.log(`✅ Added ${amount} coins to user profile`);
    } catch (error: any) {
      console.error('Failed to add coins:', error);
      throw error;
    } finally {
      PerformanceMonitor.endTiming('addCoins');
    }
  }, [profile]);

  const performMine = async () => {
    PerformanceMonitor.startTiming('performMine');
    
    if (!user || !profile) {
      PerformanceMonitor.endTiming('performMine');
      return;
    }

    try {
      // For the new 24-hour session mining, we don't add coins immediately
      // Instead, we track the session and add the reward at the end
      // The 2 EKH reward is added when the 24-hour session completes
      
      // Update local states efficiently to track the mining session
      setSessionClicks(prev => prev + 1);
      
      // Notify listeners of coin update without full profile refresh
      // This will be handled by the context consumers
      
    } catch (error: any) {
      console.error('Mining failed:', error);
    } finally {
      PerformanceMonitor.endTiming('performMine');
    }
  };

  // Create a separate state for coins to avoid full profile re-renders
  const [totalCoins, setTotalCoins] = useState<number>(0);

  // Function to update only coins for better performance
  const updateCoinsOnly = useCallback((newTotalCoins: number) => {
    setTotalCoins(newTotalCoins);
    
    // Update profile with new coin value if it exists
    setProfile(prevProfile => {
      if (!prevProfile) return null;
      return {
        ...prevProfile,
        totalCoins: newTotalCoins
      };
    });
  }, []);

  const startMiningSession = () => {
    setIsMining(true);
    setSessionCoins(0);
    setSessionClicks(0);
    setSessionStartTime(Date.now());
  };

  const endMiningSession = async () => {
    setIsMining(false);
    await recordMiningSession();
  };

  const recordMiningSession = async () => {
    if (!user || !profile || sessionStartTime === null) return;

    try {
      const sessionDuration = Math.floor((Date.now() - sessionStartTime) / 1000); // in seconds
      
      // Only record sessions that lasted more than 5 seconds to avoid spam
      if (sessionDuration < 5) {
        console.log('Session too short to record (< 5 seconds)');
        return;
      }

      const miningSession: Omit<MiningSession, 'id' | 'createdAt' | 'updatedAt'> = {
        userId: user.id,
        coinsEarned: sessionCoins,
        clicksMade: sessionClicks,
        sessionDuration: sessionDuration
      };

      // Create mining session record in Appwrite database
      await databases.createDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.miningSessions,
        ID.unique(),
        {
          userId: miningSession.userId,
          coinsEarned: miningSession.coinsEarned,
          clicksMade: miningSession.clicksMade,
          sessionDuration: miningSession.sessionDuration,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
      );

      console.log('✅ Mining session recorded:', miningSession);
    } catch (error: any) {
      console.error('Failed to record mining session:', error);
    }
  };

  // Function to update auto mining rate based on presale purchases
  const updateAutoMiningRate = useCallback(async () => {
    if (!user || !profile) return;

    try {
      // Calculate the new auto mining rate
      const newCoinsPerSecond = calculateAutoMiningRate();
      
      // Only update if the rate has changed
      if (profile.coinsPerSecond !== newCoinsPerSecond) {
        console.log(`Updating auto mining rate from ${profile.coinsPerSecond} to ${newCoinsPerSecond}`);
        
        // Update user profile with new mining rate
        const updatedProfile = {
          ...profile,
          coinsPerSecond: newCoinsPerSecond,
          updatedAt: new Date().toISOString()
        };

        // Update document in Appwrite database
        await databases.updateDocument(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          profile.id,
          {
            coinsPerSecond: updatedProfile.coinsPerSecond,
            updatedAt: updatedProfile.updatedAt
          }
        );

        // Update local state
        setProfile(updatedProfile as UserProfile);
        
        // Notify subscribers of profile updates
        notifyProfileSubscribers();
        
        console.log(`✅ Auto mining rate updated to ${newCoinsPerSecond} EKH/second`);
      }
    } catch (error: any) {
      console.error('Failed to update auto mining rate:', error);
    }
  }, [user, profile, calculateAutoMiningRate]);

  return (
    <MiningContext.Provider value={{
      profile,
      isLoading,
      isMining,
      sessionCoins,
      sessionClicks,
      performMine,
      addCoins,
      refreshProfile,
      startMiningSession,
      endMiningSession,
      silentRefreshProfile,
      updateCoinsOnly, // Add the new function
      subscribeToProfileUpdates // Add the subscription function
    }}>
      {children}
    </MiningContext.Provider>
  );
}

export function useMining() {
  const context = useContext(MiningContext);
  if (context === undefined) {
    throw new Error('useMining must be used within a MiningProvider');
  }
  return context;
}

export const createNewUserProfile = async (userData: any, referralCode: string, referredByCode?: string) => {
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

  try {
    const response = await databases.createDocument(
      appwriteConfig.databaseId,
      appwriteConfig.collections.userProfiles,
      ID.unique(),
      userProfile
    );

    console.log('✅ New user profile created:', response);
    return response;
  } catch (error: any) {
    console.error('Failed to create new user profile:', error);
    throw error;
  }
};
