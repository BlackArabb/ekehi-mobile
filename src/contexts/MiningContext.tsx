import React, { createContext, useContext, useState, useEffect, useCallback, useRef, ReactNode } from 'react';
import { UserProfile, MiningSession } from '@/types';
import { useAuth } from '@/contexts/AuthContext';
import { databases, appwriteConfig } from '@/config/appwrite';
import { Query, ID } from 'appwrite';

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
}

const MiningContext = createContext<MiningContextType | undefined>(undefined);

export function MiningProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
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

  useEffect(() => {
    if (user) {
      console.log('MiningContext: User detected, fetching profile');
      refreshProfile();
    } else {
      // Clear profile data when user is null (signed out)
      console.log('MiningContext: User is null, clearing profile data');
      setProfile(null);
      setIsLoading(false);
    }
  }, [user]);

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

  const refreshProfile = useCallback(async () => {
    // Debounce profile refreshes
    const now = Date.now();
    if (now - lastRefreshTimeRef.current < 2000) {
      console.log('⚠️ Skipping profile refresh (too frequent)');
      return;
    }
    
    // If there's already a fetch in progress, return that promise
    if (profileFetchPromiseRef.current) {
      console.log('🔄 Profile fetch already in progress, returning existing promise');
      return profileFetchPromiseRef.current;
    }
    
    // Create a new promise for the fetch operation
    profileFetchPromiseRef.current = (async () => {
      if (!user) return;
      
      console.log('🔄 Refreshing user profile for user:', user.id);
      lastRefreshTimeRef.current = now;
      setIsLoading(true);
      
      try {
        // Fetch user profile from Appwrite database
        const response = await databases.listDocuments(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          [Query.equal('userId', [user.id])]
        );

        console.log('Profile fetch result:', response.total, 'profiles found');

        if (response.documents.length > 0) {
          const doc = response.documents[0];
          const userProfile: UserProfile = {
            id: doc.$id,
            userId: doc.userId?.[0] || doc.userId, // Handle both array and string formats
            username: doc.username,
            totalCoins: doc.totalCoins,
            coinsPerClick: doc.coinsPerClick,
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
              console.log('✅ User profile loaded (first load):', userProfile);
              return userProfile;
            }
            
            // Deep comparison to check if data actually changed
            const hasChanged = JSON.stringify(prevProfile) !== JSON.stringify(userProfile);
            if (hasChanged) {
              console.log('✅ User profile updated (data changed):', userProfile);
              return userProfile;
            }
            console.log('ℹ️ User profile unchanged, skipping update');
            return prevProfile;
          });
        } else {
          console.log('❌ No user profile found for user:', user.id);
          setProfile(null);
        }
      } catch (error: any) {
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
      }
    })();
    
    return profileFetchPromiseRef.current;
  }, [user]);

  // Silent refresh function that updates profile without visual loading state
  const silentRefreshProfile = useCallback(async () => {
    // Debounce profile refreshes
    const now = Date.now();
    if (now - lastRefreshTimeRef.current < 2000) {
      console.log('⚠️ Skipping silent profile refresh (too frequent)');
      return;
    }
    
    // If there's already a fetch in progress, return that promise
    if (profileFetchPromiseRef.current) {
      console.log('🔄 Silent profile fetch already in progress, returning existing promise');
      return profileFetchPromiseRef.current;
    }
    
    // Create a new promise for the fetch operation
    profileFetchPromiseRef.current = (async () => {
      if (!user) return;
      
      console.log('🔄 Silently refreshing user profile for user:', user.id);
      lastRefreshTimeRef.current = now;
      
      try {
        // Fetch user profile from Appwrite database
        const response = await databases.listDocuments(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          [Query.equal('userId', [user.id])]
        );

        console.log('Silent profile fetch result:', response.total, 'profiles found');

        if (response.documents.length > 0) {
          const doc = response.documents[0];
          const userProfile: UserProfile = {
            id: doc.$id,
            userId: doc.userId?.[0] || doc.userId, // Handle both array and string formats
            username: doc.username,
            totalCoins: doc.totalCoins,
            coinsPerClick: doc.coinsPerClick,
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
              console.log('✅ User profile silently loaded (first load):', userProfile);
              return userProfile;
            }
            
            // Deep comparison to check if data actually changed
            const hasChanged = JSON.stringify(prevProfile) !== JSON.stringify(userProfile);
            if (hasChanged) {
              console.log('✅ User profile silently updated (data changed):', userProfile);
              return userProfile;
            }
            console.log('ℹ️ User profile silently unchanged, skipping update');
            return prevProfile;
          });
        } else {
          console.log('❌ No user profile found for user:', user.id);
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
      }
    })();
    
    return profileFetchPromiseRef.current;
  }, [user]);

  // Create a separate state for coins to avoid full profile re-renders
  const [totalCoins, setTotalCoins] = useState<number>(0);

  const performMine = async () => {
    if (!user || !profile) return;

    try {
      const newTotalCoins = profile.totalCoins + profile.coinsPerClick;
      const newTodayEarnings = profile.todayEarnings + profile.coinsPerClick;
      
      // Update user profile with new mining data
      const updatedProfile = {
        ...profile,
        totalCoins: newTotalCoins,
        todayEarnings: newTodayEarnings,
        updatedAt: new Date().toISOString()
      };

      // Update document in Appwrite database
      await databases.updateDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        profile.id,
        updatedProfile
      );

      // Update local states efficiently
      setProfile(updatedProfile as UserProfile);
      setTotalCoins(newTotalCoins);
      setSessionCoins(prev => prev + profile.coinsPerClick);
      setSessionClicks(prev => prev + 1);
      
      // Notify listeners of coin update without full profile refresh
      // This will be handled by the context consumers
      
    } catch (error: any) {
      console.error('Mining failed:', error);
    }
  };

  const addCoins = async (amount: number) => {
    if (!user || !profile) return;

    try {
      const newTotalCoins = profile.totalCoins + amount;
      
      // Update user profile with new coin balance
      const updatedProfile = {
        ...profile,
        totalCoins: newTotalCoins,
        updatedAt: new Date().toISOString()
      };

      // Update document in Appwrite database
      await databases.updateDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        profile.id,
        updatedProfile
      );

      // Update local states efficiently
      setProfile(updatedProfile as UserProfile);
      setTotalCoins(newTotalCoins);
      
    } catch (error: any) {
      console.error('Failed to add coins:', error);
    }
  };

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
      updateCoinsOnly // Add the new function
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