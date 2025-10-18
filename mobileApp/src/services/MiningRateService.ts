import { databases, appwriteConfig } from '@/config/appwrite';
import { UserProfile } from '@/types';
import { retryWithBackoff, isNetworkError } from '@/utils/retry';
import { Query } from 'appwrite';

/**
 * Mining Rate Service
 * 
 * This service provides dedicated endpoints for reading and writing mining rate changes.
 * It handles both auto mining rates (coinsPerSecond) and referral bonus rates (referralBonusRate).
 */

export class MiningRateService {
  /**
   * Get the total mining rate for a user
   * Combines auto mining rate and referral bonus rate
   * 
   * @param userId - The user ID
   * @returns The total mining rate in EKH/hour
   */
  static async getTotalMiningRate(userId: string): Promise<number> {
    try {
      const userProfile = await this.getUserProfile(userId);
      if (!userProfile) {
        return 0;
      }
      return this.calculateTotalMiningRate(userProfile);
    } catch (error) {
      console.error('Failed to get total mining rate:', error);
      return 0;
    }
  }

  /**
   * Calculate the total mining rate from a user profile
   * 
   * @param profile - The user profile
   * @returns The total mining rate in EKH/hour
   */
  static calculateTotalMiningRate(profile: UserProfile): number {
    // Auto mining rate: autoMiningRate is already in EKH/hour
    const autoMiningRate = profile.autoMiningRate || 0;
    
    // Manual mining rate: dailyMiningRate / 24 (convert from EKH/day to EKH/hour)
    const manualMiningRate = (profile.dailyMiningRate || 2) / 24;
    
    // Referral bonus rate: directly in EKH/hour
    const referralBonusRate = profile.referralBonusRate || 0;
    
    // Total rate is the sum of all three
    return manualMiningRate + autoMiningRate + referralBonusRate;
  }

  /**
   * Update the referral bonus rate for a user
   * 
   * @param userId - The user ID
   * @param increment - The amount to increment the referral bonus rate by
   * @returns The updated user profile
   */
  static async updateReferralBonusRate(userId: string, increment: number): Promise<UserProfile | null> {
    try {
      // Get current user profile
      const userProfile = await this.getUserProfile(userId);
      
      if (!userProfile) {
        throw new Error('User profile not found');
      }
      
      // Calculate new referral bonus rate
      const newReferralBonusRate = (userProfile.referralBonusRate || 0) + increment;
      
      // Update user profile with new referral bonus rate
      const result = await retryWithBackoff(
        async () => {
          const response = await databases.updateDocument(
            appwriteConfig.databaseId,
            appwriteConfig.collections.userProfiles,
            userProfile.id,
            {
              referralBonusRate: newReferralBonusRate,
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

      if (!result.success) {
        throw result.error;
      }

      // Return updated profile
      return {
        ...userProfile,
        referralBonusRate: newReferralBonusRate,
        updatedAt: new Date().toISOString()
      };
    } catch (error) {
      console.error('Failed to update referral bonus rate:', error);
      return null;
    }
  }

  /**
   * Update the auto mining rate for a user
   * 
   * @param userId - The user ID
   * @param newRate - The new auto mining rate in EKH/hour
   * @returns The updated user profile
   */
  static async updateAutoMiningRate(userId: string, newRate: number): Promise<UserProfile | null> {
    try {
      // Get current user profile
      const userProfile = await this.getUserProfile(userId);
      
      if (!userProfile) {
        throw new Error('User profile not found');
      }
      
      // Update user profile with new auto mining rate
      const result = await retryWithBackoff(
        async () => {
          const response = await databases.updateDocument(
            appwriteConfig.databaseId,
            appwriteConfig.collections.userProfiles,
            userProfile.id,
            {
              autoMiningRate: newRate,
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

      if (!result.success) {
        throw result.error;
      }

      // Return updated profile
      return {
        ...userProfile,
        autoMiningRate: newRate,
        updatedAt: new Date().toISOString()
      };
    } catch (error) {
      console.error('Failed to update auto mining rate:', error);
      return null;
    }
  }

  /**
   * Get user profile by user ID
   * 
   * @param userId - The user ID
   * @returns The user profile or null if not found
   */
  private static async getUserProfile(userId: string): Promise<UserProfile | null> {
    try {
      const result = await retryWithBackoff(
        async () => {
          const response = await databases.listDocuments(
            appwriteConfig.databaseId,
            appwriteConfig.collections.userProfiles,
            [Query.equal('userId', [userId])]
          );
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

      if (response.documents.length === 0) {
        return null;
      }

      const doc = response.documents[0];
      const userProfile: UserProfile = {
        id: doc.$id,
        userId: doc.userId?.[0] || doc.userId,
        username: doc.username,
        totalCoins: doc.totalCoins,
        coinsPerSecond: doc.coinsPerSecond,
        autoMiningRate: doc.autoMiningRate || 0,
        miningPower: doc.miningPower,
        referralBonusRate: doc.referralBonusRate || 0,
        currentStreak: doc.currentStreak,
        longestStreak: doc.longestStreak,
        lastLoginDate: doc.lastLoginDate,
        referralCode: doc.referralCode?.[0] || doc.referralCode,
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

      return userProfile;
    } catch (error) {
      console.error('Failed to fetch user profile:', error);
      return null;
    }
  }
}

export default MiningRateService;