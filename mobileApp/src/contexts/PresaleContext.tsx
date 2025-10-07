import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { PresalePurchase } from '@/types';
import { useAuth } from './AuthContext';
import { databases, appwriteConfig } from '@/config/appwrite';
import { Query, ID } from 'appwrite';
import API_CONFIG from '@/config/api';

interface PresaleContextType {
  isActive: boolean;
  tokenPrice: number;
  minPurchase: number;
  purchases: PresalePurchase[];
  isLoading: boolean;
  purchaseTokens: (amountUSD: number, paymentMethod: string) => Promise<{ success: boolean; message: string }>;
  fetchPurchases: () => Promise<void>;
}

const PresaleContext = createContext<PresaleContextType | undefined>(undefined);

export function PresaleProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [isActive, setIsActive] = useState(true);
  const [tokenPrice, setTokenPrice] = useState(0.1);
  const [minPurchase, setMinPurchase] = useState(10);
  const [purchases, setPurchases] = useState<PresalePurchase[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (user) {
      console.log('PresaleContext: User detected, fetching purchases');
      fetchPurchases();
    }
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
      return { success: false, message: 'User not authenticated' };
    }

    if (amountUSD < minPurchase) {
      return { success: false, message: `Minimum purchase amount is $${minPurchase}` };
    }

    setIsLoading(true);
    try {
      // Calculate tokens amount based on price
      const tokensAmount = amountUSD / tokenPrice;
      
      // Process payment
      const paymentResult = await processPayment(amountUSD, paymentMethod);
      
      if (!paymentResult.success) {
        return { success: false, message: paymentResult.message };
      }
      
      // Mint tokens through Ekehi Network API
      const mintResult = await mintTokens(user.id, tokensAmount, paymentResult.transactionHash!);
      
      if (!mintResult.success) {
        return { success: false, message: mintResult.message };
      }
      
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

      // Update user profile with purchase info
      const userProfileResponse = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [Query.equal('userId', [user.id])]
      );

      if (userProfileResponse.documents.length > 0) {
        const userProfile = userProfileResponse.documents[0];
        await databases.updateDocument(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          userProfile.$id,
          {
            ...userProfile,
            totalCoins: userProfile.totalCoins + tokensAmount,
            updatedAt: new Date().toISOString()
          }
        );
        
        await fetchPurchases();
        return { success: true, message: `Successfully purchased ${tokensAmount.toLocaleString()} EKH tokens! Transaction hash: ${paymentResult.transactionHash!.substring(0, 10)}...` };
      } else {
        // If user profile doesn't exist, we should create one
        return { success: false, message: 'User profile not found. Please contact support.' };
      }
    } catch (error: any) {
      console.error('Failed to purchase tokens:', error);
      return { success: false, message: `Failed to process purchase: ${error.message || 'Please try again'}` };
    } finally {
      setIsLoading(false);
    }
  };

  const fetchPurchases = async () => {
    if (!user) return;

    try {
      const response = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.presalePurchases,
        [Query.equal('userId', [user.id]), Query.orderDesc('createdAt')]
      );

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

  return (
    <PresaleContext.Provider value={{
      isActive,
      tokenPrice,
      minPurchase,
      purchases,
      isLoading,
      purchaseTokens,
      fetchPurchases,
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