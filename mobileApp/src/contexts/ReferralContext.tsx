import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { useMining } from '@/contexts/MiningContext';
import { databases, appwriteConfig } from '@/config/appwrite';
import { Query } from 'appwrite';

interface ReferralContextType {
  referralCode: string | null;
  totalReferrals: number;
  referralReward: number;
  referredBy: string | null;
  isLoading: boolean;
  claimReferral: (code: string) => Promise<{ success: boolean; message: string }>;
  generateReferralLink: () => string;
  refreshReferralData: () => Promise<void>;
  getReferralHistory: () => Promise<any[]>;
  generateUniqueReferralCode: () => Promise<string>;
}

const ReferralContext = createContext<ReferralContextType | undefined>(undefined);

export function ReferralProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const { refreshProfile } = useMining();
  const [referralCode, setReferralCode] = useState<string | null>(null);
  const [totalReferrals, setTotalReferrals] = useState(0);
  const [referralReward, setReferralReward] = useState(0);
  const [referredBy, setReferredBy] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (user) {
      console.log('ReferralContext: User detected, fetching referral data');
      refreshReferralData();
    }
  }, [user]);

  const refreshReferralData = async () => {
    if (!user) return;

    setIsLoading(true);
    try {
      // Fetch user profile to get referral data
      const response = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [Query.equal('userId', [user.id])]
      );

      if (response.documents.length > 0) {
        const doc = response.documents[0];
        setReferralCode(doc.referralCode || null);
        setTotalReferrals(doc.totalReferrals || 0);
        setReferredBy(doc.referredBy || null);
        setReferralReward(0); // No direct reward for referrer in new system
      }
    } catch (error) {
      console.error('Failed to fetch referral data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const generateUniqueReferralCode = async (): Promise<string> => {
    let code: string;
    let isUnique = false;
    let attempts = 0;
    const maxAttempts = 10;

    while (!isUnique && attempts < maxAttempts) {
      // Generate a more robust referral code
      code = Math.random().toString(36).substring(2, 10).toUpperCase() + 
             Math.random().toString(36).substring(2, 6).toUpperCase();
      
      // Check if code already exists
      try {
        const response = await databases.listDocuments(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          [Query.equal('referralCode', code)]
        );
        
        if (response.documents.length === 0) {
          isUnique = true;
          return code;
        }
      } catch (error) {
        console.error('Error checking referral code uniqueness:', error);
      }
      
      attempts++;
    }
    
    // Fallback: timestamp-based code
    return 'REF' + Date.now().toString().slice(-8);
  };

  const claimReferral = async (code: string): Promise<{ success: boolean; message: string }> => {
    if (!user) {
      return { success: false, message: 'User not authenticated' };
    }

    if (!code || code.trim() === '') {
      return { success: false, message: 'Please enter a referral code' };
    }

    setIsLoading(true);
    try {
      // Check if the referral code exists and belongs to another user
      const response = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [Query.equal('referralCode', code.trim())]
      );

      if (response.documents.length === 0) {
        return { success: false, message: 'Invalid referral code' };
      }

      const referrerDoc = response.documents[0];
      
      // Make sure the user is not referring themselves
      if (referrerDoc.userId === user.id) {
        return { success: false, message: 'You cannot refer yourself' };
      }
      
      // Check if referrer has reached maximum referrals (50)
      if (referrerDoc.totalReferrals >= 50) {
        return { success: false, message: 'This referrer has reached the maximum number of referrals (50)' };
      }
      
      // Check if user already has a referrer
      const currentUserResponse = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [Query.equal('userId', [user.id])]
      );

      if (currentUserResponse.documents.length === 0) {
        return { success: false, message: 'User profile not found' };
      }

      const currentUserDoc = currentUserResponse.documents[0];
      
      // If already referred, don't allow claiming another referral
      if (currentUserDoc.referredBy && currentUserDoc.referredBy !== '') {
        return { success: false, message: 'You have already been referred' };
      }
      
      // Calculate new mining rate for referrer (increase by 0.2 EKH per referral)
      const newMiningRate = (referrerDoc.coinsPerSecond || 0) + 0.2;
      
      // Update referrer's profile with increased mining rate and referral count
      await databases.updateDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        referrerDoc.$id,
        {
          totalReferrals: (referrerDoc.totalReferrals || 0) + 1,
          coinsPerSecond: newMiningRate, // Increase mining rate by 0.2 EKH
          updatedAt: new Date().toISOString()
        }
      );
      
      // Update the current user's referredBy field and give them 2 EKH
      await databases.updateDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        currentUserDoc.$id,
        {
          referredBy: referrerDoc.userId,
          totalCoins: (currentUserDoc.totalCoins || 0) + 2.0, // 2 EKH for referee
          updatedAt: new Date().toISOString()
        }
      );
      
      // Refresh profiles for both users
      await refreshProfile();
      
      // Refresh referral data
      await refreshReferralData();
      return { success: true, message: 'Referral claimed successfully! You received 2 EKH.' };
    } catch (error) {
      console.error('Failed to claim referral:', error);
      return { success: false, message: 'Failed to claim referral. Please try again.' };
    } finally {
      setIsLoading(false);
    }
  };

  const generateReferralLink = (): string => {
    if (!referralCode) return '';
    return `ekehi://referral/${referralCode}`;
  };

  const getReferralHistory = async (): Promise<any[]> => {
    if (!user) return [];
    
    try {
      // Get users who were referred by current user
      const response = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [Query.equal('referredBy', user.id)]
      );
      
      return response.documents.map(doc => ({
        id: doc.$id,
        username: doc.username,
        joinedDate: doc.createdAt,
        // Add more fields as needed
      }));
    } catch (error) {
      console.error('Failed to fetch referral history:', error);
      return [];
    }
  };

  return (
    <ReferralContext.Provider value={{
      referralCode,
      totalReferrals,
      referralReward,
      referredBy,
      isLoading,
      claimReferral,
      generateReferralLink,
      refreshReferralData,
      getReferralHistory,
      generateUniqueReferralCode
    }}>
      {children}
    </ReferralContext.Provider>
  );
}

export function useReferral() {
  const context = useContext(ReferralContext);
  if (context === undefined) {
    throw new Error('useReferral must be used within a ReferralProvider');
  }
  return context;
}