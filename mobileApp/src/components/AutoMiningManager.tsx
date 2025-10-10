import React, { useEffect, useCallback } from 'react';
import { usePresale } from '@/contexts/PresaleContext';
import { useMining } from '@/contexts/MiningContext';
import { useAuth } from '@/contexts/AuthContext';
import { databases, appwriteConfig } from '@/config/appwrite';

const AutoMiningManager: React.FC = () => {
  const { user } = useAuth();
  const { profile, refreshProfile } = useMining();
  const { purchases, calculateAutoMiningRate } = usePresale();

  // Function to update user's auto mining rate in the database
  const updateAutoMiningRate = useCallback(async () => {
    if (!user || !profile) return;

    try {
      // Calculate the new auto mining rate based on purchases
      const newCoinsPerSecond = calculateAutoMiningRate();
      
      // Only update if the rate has changed
      if (profile.coinsPerSecond !== newCoinsPerSecond) {
        console.log(`Updating auto mining rate from ${profile.coinsPerSecond} to ${newCoinsPerSecond}`);
        
        // Update user profile with new mining rate
        await databases.updateDocument(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          profile.id,
          {
            coinsPerSecond: newCoinsPerSecond,
            updatedAt: new Date().toISOString()
          }
        );

        // Refresh the profile to get updated data
        await refreshProfile();
        
        console.log(`✅ Auto mining rate updated to ${newCoinsPerSecond} EKH/second`);
      }
    } catch (error: any) {
      console.error('Failed to update auto mining rate:', error);
    }
  }, [user, profile, calculateAutoMiningRate, refreshProfile]);

  // Effect to update auto mining rate when purchases change
  useEffect(() => {
    updateAutoMiningRate();
  }, [purchases, updateAutoMiningRate]);

  // This component doesn't render anything, it just manages the auto mining logic
  return null;
};

export default AutoMiningManager;