import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
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
    };
  }, [isMining]);

  const refreshProfile = async () => {
    if (!user) return;
    
    console.log('🔄 Refreshing user profile for user:', user.id);
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
        setProfile(userProfile);
        console.log('✅ User profile loaded:', userProfile);
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
    }
  };

  const performMine = async () => {
    if (!user || !profile) return;

    try {
      // Update user profile with new mining data
      const updatedProfile = {
        ...profile,
        totalCoins: profile.totalCoins + profile.coinsPerClick,
        todayEarnings: profile.todayEarnings + profile.coinsPerClick,
        updatedAt: new Date().toISOString()
      };

      // Update document in Appwrite database
      await databases.updateDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        profile.id,
        updatedProfile
      );

      // Update local state
      setProfile(updatedProfile as UserProfile);
      setSessionCoins(prev => prev + profile.coinsPerClick);
      setSessionClicks(prev => prev + 1);
    } catch (error: any) {
      console.error('Mining failed:', error);
    }
  };

  const addCoins = async (amount: number) => {
    if (!user || !profile) return;

    try {
      // Update user profile with new coin balance
      const updatedProfile = {
        ...profile,
        totalCoins: profile.totalCoins + amount,
        updatedAt: new Date().toISOString()
      };

      // Update document in Appwrite database
      await databases.updateDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        profile.id,
        updatedProfile
      );

      // Update local state
      setProfile(updatedProfile as UserProfile);
    } catch (error: any) {
      console.error('Failed to add coins:', error);
    }
  };

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