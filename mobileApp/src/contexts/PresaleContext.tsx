import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { PresalePurchase } from '@/types';
import { useAuth } from './AuthContext';
import { databases, appwriteConfig } from '@/config/appwrite';
import { Query, ID } from 'appwrite';
import API_CONFIG from '@/config/api';
import { retryWithBackoff, isNetworkError } from '@/utils/retry';
import LoggingService from '@/services/LoggingService';

interface PresaleContextType {
  isActive: boolean;
  tokenPrice: number;
  minPurchase: number;
  purchases: PresalePurchase[];
  isLoading: boolean;
  purchaseTokens: (amountUSD: number, paymentMethod: string) => Promise<{ success: boolean; message: string }>;
  fetchPurchases: () => Promise<void>;
  // Add new functions for auto mining calculations
  calculateAutoMiningRate: () => number;
  isAutoMiningEligible: () => boolean;
  autoMiningMinPurchase: number;
  // Add maximum limits
  maxMiningRatePurchaseAmount: number; // Maximum purchase amount for mining rate calculation
  maxGeneralPurchaseAmount: number; // Maximum general purchase amount
  maxMiningRate: number; // Maximum mining rate cap
}

const PresaleContext = createContext<PresaleContextType | undefined>(undefined);

export function PresaleProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [isActive] = useState(true);
  const [tokenPrice] = useState(0.1);
  const [minPurchase] = useState(10);
  const [purchases, setPurchases] = useState<PresalePurchase[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // Define the minimum purchase amount for auto mining eligibility
  const autoMiningMinPurchase = 50; // $50 minimum for auto mining eligibility
  
  // Define the rate calculation: 1 EKH/second per $1000 purchased
  const autoMiningRatePerDollar = 0.001; // 0.001 EKH/second per dollar
  
  // Define maximum limits
  const maxMiningRatePurchaseAmount = 10000; // $10,000 maximum for mining rate calculation
  const maxGeneralPurchaseAmount = 50000; // $50,000 maximum general purchase amount
  const maxMiningRate = 10; // 10 EKH/second maximum mining rate

  useEffect(() => {
    if (user) {
      console.log('PresaleContext: User detected, fetching purchases');
      fetchPurchases();
    }
    // Return undefined to satisfy TypeScript
    return undefined;
  }, [user]);

  // Function to process payment through Ekehi Network API
  const processPayment = async (amountUSD: number, paymentMethod: string): Promise<{ success: boolean; transactionHash?: string; message: string }> => {
    try {
      // In a real implementation, this would integrate with a payment processor
      // and then call the Ekehi Network API to mint tokens
      // For now, we'll simulate a successful payment and generate a mock transaction hash
      
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Generate a mock transaction hash
      const transactionHash = '0x' + Array.from({length: 64}, () => 
        Math.floor(Math.random() * 16).toString(16)
      ).join('').toLowerCase();
      
      return { 
        success: true, 
        transactionHash,
        message: 'Payment processed successfully' 
      };
    } catch (error: any) {
      console.error('Failed to process payment:', error);
      return { 
        success: false, 
        message: `Payment failed: ${error.message || 'Please try again'}` 
      };
    }
  };

  // Function to mint tokens through Ekehi Network API
  const mintTokens = async (userId: string, tokensAmount: number, transactionHash: string): Promise<{ success: boolean; message: string }> => {
    try {
      // Use retry mechanism for network operations
      const result = await retryWithBackoff(
        async () => {
          // Call Ekehi Network API to mint tokens
          const response = await fetch(`${API_CONFIG.EKEHI_NETWORK.BASE_URL}/mint`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'X-API-Key': API_CONFIG.EKEHI_NETWORK.API_KEY,
            },
            body: JSON.stringify({
              userId: userId,
              amount: tokensAmount,
              transactionHash: transactionHash,
              chainId: API_CONFIG.EKEHI_NETWORK.CHAIN_ID,
              token: 'EKH'
            }),
          });
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

      if (response.ok) {
        return { success: true, message: 'Tokens minted successfully' };
      } else {
        const errorData = await response.json();
        return { success: false, message: `Minting failed: ${errorData.message || 'Please try again'}` };
      }
    } catch (error: any) {
      console.error('Failed to mint tokens:', error);
      return { success: false, message: `Minting failed: ${error.message || 'Please try again'}` };
    }
  };

  const purchaseTokens = async (amountUSD: number, paymentMethod: string): Promise<{ success: boolean; message: string }> => {
    if (!user) {
      LoggingService.warn('User not authenticated for token purchase', 'PresaleContext');
      return { success: false, message: 'User not authenticated' };
    }

    if (amountUSD < minPurchase) {
      LoggingService.warn(`Purchase amount ${amountUSD} is below minimum ${minPurchase}`, 'PresaleContext');
      return { success: false, message: `Minimum purchase amount is $${minPurchase}` };
    }

    LoggingService.info(`Starting token purchase: $${amountUSD} via ${paymentMethod}`, 'PresaleContext', { userId: user.id });
    setIsLoading(true);
    try {
      // Calculate tokens amount based on price
      const tokensAmount = amountUSD / tokenPrice;
      
      // Process payment
      const paymentResult = await processPayment(amountUSD, paymentMethod);
      
      if (!paymentResult.success) {
        LoggingService.error('Payment processing failed', 'PresaleContext', { userId: user.id, amountUSD, paymentMethod });
        return { success: false, message: paymentResult.message };
      }
      
      // Mint tokens through Ekehi Network API
      const mintResult = await mintTokens(user.id, tokensAmount, paymentResult.transactionHash!);
      
      if (!mintResult.success) {
        LoggingService.error('Token minting failed', 'PresaleContext', { userId: user.id, tokensAmount });
        return { success: false, message: mintResult.message };
      }
      
      // Use retry mechanism for database operations
      const dbResult = await retryWithBackoff(
        async () => {
          // Create a new presale purchase document with transaction hash
          const newPurchase = {
            userId: user.id,
            amountUsd: amountUSD,
            tokensAmount: tokensAmount,
            transactionHash: paymentResult.transactionHash,
            status: 'completed',
            paymentMethod: paymentMethod,
            createdAt: new Date().toISOString()
          };

          // Save to Appwrite database
          const response = await databases.createDocument(
            appwriteConfig.databaseId,
            appwriteConfig.collections.presalePurchases,
            ID.unique(),
            newPurchase
          );
          return response;
        },
        {
          maxRetries: 3,
          delay: 1000,
          shouldRetry: isNetworkError
        }
      );

      if (!dbResult.success) {
        throw dbResult.error;
      }

      // Update user profile with purchase info
      const userProfileResult = await retryWithBackoff(
        async () => {
          const userProfileResponse = await databases.listDocuments(
            appwriteConfig.databaseId,
            appwriteConfig.collections.userProfiles,
            [Query.equal('userId', [user.id])]
          );
          return userProfileResponse;
        },
        {
          maxRetries: 3,
          delay: 1000,
          shouldRetry: isNetworkError
        }
      );

      if (!userProfileResult.success) {
        throw userProfileResult.error;
      }

      const userProfileResponse = userProfileResult.data!;

      if (userProfileResponse.documents.length > 0) {
        const userProfile = userProfileResponse.documents[0];
        
        const updateResult = await retryWithBackoff(
          async () => {
            const response = await databases.updateDocument(
              appwriteConfig.databaseId,
              appwriteConfig.collections.userProfiles,
              userProfile.$id,
              {
                ...userProfile,
                totalCoins: userProfile.totalCoins + tokensAmount,
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
        
        await fetchPurchases();
        const successMessage = `Successfully purchased ${tokensAmount.toLocaleString()} EKH tokens! Transaction hash: ${paymentResult.transactionHash!.substring(0, 10)}...`;
        LoggingService.info('Token purchase successful', 'PresaleContext', { userId: user.id, tokensAmount, transactionHash: paymentResult.transactionHash });
        return { success: true, message: successMessage };
      } else {
        // If user profile doesn't exist, we should create one
        LoggingService.error('User profile not found during purchase', 'PresaleContext', { userId: user.id });
        return { success: false, message: 'User profile not found. Please contact support.' };
      }
    } catch (error: any) {
      LoggingService.error('Failed to purchase tokens', 'PresaleContext', { userId: user?.id, amountUSD, paymentMethod }, error);
      console.error('Failed to purchase tokens:', error);
      return { success: false, message: `Failed to process purchase: ${error.message || 'Please try again'}` };
    } finally {
      setIsLoading(false);
    }
  };

  const fetchPurchases = async () => {
    if (!user) return;

    try {
      // Use retry mechanism for database operations
      const result = await retryWithBackoff(
        async () => {
          const response = await databases.listDocuments(
            appwriteConfig.databaseId,
            appwriteConfig.collections.presalePurchases,
            [Query.equal('userId', [user.id]), Query.orderDesc('createdAt')]
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

      const presalePurchases: PresalePurchase[] = response.documents.map(doc => ({
        id: doc.$id,
        userId: doc.userId,
        amountUsd: doc.amountUsd,
        tokensAmount: doc.tokensAmount,
        transactionHash: doc.transactionHash,
        status: doc.status,
        paymentMethod: doc.paymentMethod,
        createdAt: doc.createdAt
      }));

      setPurchases(presalePurchases);
    } catch (error) {
      console.error('Failed to fetch purchases:', error);
    }
  };

  // Function to calculate auto mining rate based on purchases with maximum limits
  const calculateAutoMiningRate = useCallback((): number => {
    if (!user || purchases.length === 0) {
      return 0;
    }

    // Calculate total amount spent in completed purchases
    const totalSpent = purchases.reduce((sum, purchase) => 
      purchase.status === 'completed' ? sum + purchase.amountUsd : sum, 0
    );

    // Check if user meets minimum purchase requirement for auto mining
    if (totalSpent < autoMiningMinPurchase) {
      return 0; // Not eligible for auto mining
    }

    // Calculate mining rate based on total spent, but cap at maxMiningRatePurchaseAmount
    const effectiveSpent = Math.min(totalSpent, maxMiningRatePurchaseAmount);
    const miningRate = effectiveSpent * autoMiningRatePerDollar;
    
    // Cap the mining rate at maxMiningRate
    const finalRate = Math.min(miningRate, maxMiningRate);
    
    // Round to 4 decimal places for precision
    return Math.round(finalRate * 10000) / 10000;
  }, [purchases, user, autoMiningMinPurchase, autoMiningRatePerDollar, maxMiningRatePurchaseAmount, maxMiningRate]);

  // Function to check if user is eligible for auto mining
  const isAutoMiningEligible = useCallback((): boolean => {
    if (!user || purchases.length === 0) {
      return false;
    }

    // Calculate total amount spent in completed purchases
    const totalSpent = purchases.reduce((sum, purchase) => 
      purchase.status === 'completed' ? sum + purchase.amountUsd : sum, 0
    );

    return totalSpent >= autoMiningMinPurchase;
  }, [purchases, user, autoMiningMinPurchase]);

  // Function to check if user has reached maximum general purchase amount
  const hasReachedMaxGeneralPurchase = useCallback((): boolean => {
    if (!user || purchases.length === 0) {
      return false;
    }

    // Calculate total amount spent in completed purchases
    const totalSpent = purchases.reduce((sum, purchase) => 
      purchase.status === 'completed' ? sum + purchase.amountUsd : sum, 0
    );

    return totalSpent >= maxGeneralPurchaseAmount;
  }, [purchases, user, maxGeneralPurchaseAmount]);

  // Function to get remaining amount until max general purchase limit
  const getRemainingToMaxGeneralPurchase = useCallback((): number => {
    if (!user || purchases.length === 0) {
      return maxGeneralPurchaseAmount;
    }

    // Calculate total amount spent in completed purchases
    const totalSpent = purchases.reduce((sum, purchase) => 
      purchase.status === 'completed' ? sum + purchase.amountUsd : sum, 0
    );

    return Math.max(0, maxGeneralPurchaseAmount - totalSpent);
  }, [purchases, user, maxGeneralPurchaseAmount]);

  return (
    <PresaleContext.Provider value={{
      isActive,
      tokenPrice,
      minPurchase,
      purchases,
      isLoading,
      purchaseTokens,
      fetchPurchases,
      // Add the new functions to the context value
      calculateAutoMiningRate,
      isAutoMiningEligible,
      autoMiningMinPurchase,
      // Add maximum limits
      maxMiningRatePurchaseAmount,
      maxGeneralPurchaseAmount,
      maxMiningRate
    }}>
      {children}
    </PresaleContext.Provider>
  );
}

export function usePresale() {
  const context = useContext(PresaleContext);
  if (context === undefined) {
    throw new Error('usePresale must be used within a PresaleProvider');
  }
  return context;
}
