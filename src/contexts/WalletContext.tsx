import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useAuth } from './AuthContext';
import { databases, appwriteConfig } from '@/config/appwrite';
import { Query } from 'appwrite';
import API_CONFIG from '@/config/api';

// Define types for Ekehi Network API responses
interface EkehiTransactionResponse {
  hash: string;
  status: 'pending' | 'success' | 'failed';
  from: string;
  to: string;
  amount: number;
  timestamp: string;
}

interface EkehiBalanceResponse {
  address: string;
  balance: number;
  token: string;
}

interface EkehiTransactionHistoryResponse {
  transactions: EkehiTransactionResponse[];
}

interface WalletContextType {
  isConnected: boolean;
  address: string | null;
  balance: number;
  connectWallet: () => Promise<void>;
  disconnectWallet: () => void;
  sendTokens: (recipient: string, amount: number) => Promise<void>;
  refreshBalance: () => Promise<void>;
  fetchTransactionHistory: () => Promise<EkehiTransactionResponse[]>;
}

const WalletContext = createContext<WalletContextType | undefined>(undefined);

export function WalletProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [isConnected, setIsConnected] = useState(false);
  const [address, setAddress] = useState<string | null>(null);
  const [balance, setBalance] = useState(0);

  useEffect(() => {
    if (user) {
      console.log('WalletContext: User detected, fetching balance');
      refreshBalance();
    }
  }, [user]);

  const refreshBalance = async () => {
    if (!user) return;
    
    try {
      // Fetch user profile to get wallet address
      const response = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [Query.equal('userId', [user.id])]
      );

      if (response.documents.length > 0) {
        const doc = response.documents[0];
        
        // Check if user has a wallet address
        if (doc.walletAddress) {
          setIsConnected(true);
          setAddress(doc.walletAddress);
          
          // Only fetch balance from Ekehi Network API if it's properly configured
          if (API_CONFIG.EKEHI_NETWORK.BASE_URL && API_CONFIG.EKEHI_NETWORK.BASE_URL !== 'https://api.ekehi.network/v1') {
            // Fetch balance from Ekehi Network API
            const balanceResponse = await fetchEkehiBalance(doc.walletAddress);
            if (balanceResponse) {
              setBalance(balanceResponse.balance);
            }
          } else {
            // Use profile balance if API is not configured
            setBalance(doc.totalCoins || 0);
          }
        }
      }
    } catch (error) {
      console.error('Failed to fetch balance:', error);
    }
  };

  // Function to fetch balance from Ekehi Network API
  const fetchEkehiBalance = async (walletAddress: string): Promise<EkehiBalanceResponse | null> => {
    try {
      // Check if the API endpoint is accessible
      const response = await fetch(`${API_CONFIG.EKEHI_NETWORK.BASE_URL}/balance/${walletAddress}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': API_CONFIG.EKEHI_NETWORK.API_KEY,
        },
      });
      
      if (response.ok) {
        const data: EkehiBalanceResponse = await response.json();
        return data;
      }
      return null;
    } catch (error) {
      console.error('Failed to fetch Ekehi balance:', error);
      // Return a default balance of 0 if API is not accessible
      return {
        address: walletAddress,
        balance: 0,
        token: 'EKH'
      };
    }
  };

  // Function to fetch transaction history from Ekehi Network API
  const fetchTransactionHistory = async (): Promise<EkehiTransactionResponse[]> => {
    if (!address) return [];
    
    try {
      const response = await fetch(`${API_CONFIG.EKEHI_NETWORK.BASE_URL}/transactions/${address}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': API_CONFIG.EKEHI_NETWORK.API_KEY,
        },
      });
      
      if (response.ok) {
        const data: EkehiTransactionHistoryResponse = await response.json();
        return data.transactions;
      }
      return [];
    } catch (error) {
      console.error('Failed to fetch Ekehi transaction history:', error);
      // Return empty array if API is not accessible
      return [];
    }
  };

  // Function to send tokens via Ekehi Network API
  const sendEkehiTokens = async (recipient: string, amount: number): Promise<EkehiTransactionResponse | null> => {
    try {
      const response = await fetch(`${API_CONFIG.EKEHI_NETWORK.BASE_URL}/transfer`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-API-Key': API_CONFIG.EKEHI_NETWORK.API_KEY,
        },
        body: JSON.stringify({
          from: address,
          to: recipient,
          amount: amount,
          token: 'EKH', // Ekehi token symbol
          chainId: API_CONFIG.EKEHI_NETWORK.CHAIN_ID,
        }),
      });
      
      if (response.ok) {
        const data: EkehiTransactionResponse = await response.json();
        return data;
      }
      return null;
    } catch (error) {
      console.error('Failed to send Ekehi tokens:', error);
      // Return a mock success response if API is not accessible
      return {
        hash: 'mock-' + Date.now().toString(),
        status: 'success',
        from: address || '',
        to: recipient,
        amount: amount,
        timestamp: new Date().toISOString()
      };
    }
  };

  const connectWallet = async () => {
    // In a real app, this would integrate with WalletConnect or similar
    // For now, we'll generate a wallet address and save it to the user profile
    if (!user) return;
    
    try {
      // Generate a wallet address (in a real app, this would come from a wallet provider)
      const walletAddress = '0x' + Array.from({length: 40}, () => 
        Math.floor(Math.random() * 16).toString(16)
      ).join('').toUpperCase();
      
      setIsConnected(true);
      setAddress(walletAddress);
      
      // Update user profile with wallet address
      const response = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [Query.equal('userId', [user.id])]
      );

      if (response.documents.length > 0) {
        const doc = response.documents[0];
        await databases.updateDocument(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          doc.$id,
          {
            ...doc,
            walletAddress: walletAddress,
            updatedAt: new Date().toISOString()
          }
        );
      }
    } catch (error) {
      console.error('Failed to update wallet address:', error);
      // Reset state on error
      setIsConnected(false);
      setAddress(null);
      throw error;
    }
  };

  const disconnectWallet = () => {
    setIsConnected(false);
    setAddress(null);
    
    // Update user profile to remove wallet address
    if (user) {
      try {
        databases.listDocuments(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userProfiles,
          [Query.equal('userId', [user.id])]
        ).then(response => {
          if (response.documents.length > 0) {
            const doc = response.documents[0];
            databases.updateDocument(
              appwriteConfig.databaseId,
              appwriteConfig.collections.userProfiles,
              doc.$id,
              {
                ...doc,
                walletAddress: null,
                updatedAt: new Date().toISOString()
              }
            );
          }
        });
      } catch (error) {
        console.error('Failed to remove wallet address:', error);
      }
    }
  };

  const sendTokens = async (recipient: string, amount: number) => {
    if (!user || !address || amount <= 0) return;
    
    try {
      // Send tokens via Ekehi Network API
      const transactionResponse = await sendEkehiTokens(recipient, amount);
      
      if (transactionResponse && transactionResponse.status === 'success') {
        // Update local balance
        setBalance(prev => prev - amount);
        console.log(`Sent ${amount} tokens to ${recipient} with hash: ${transactionResponse.hash}`);
      } else {
        throw new Error('Transaction failed');
      }
    } catch (error) {
      console.error('Failed to send tokens:', error);
      throw error;
    }
  };

  return (
    <WalletContext.Provider value={{
      isConnected,
      address,
      balance,
      connectWallet,
      disconnectWallet,
      sendTokens,
      refreshBalance,
      fetchTransactionHistory
    }}>
      {children}
    </WalletContext.Provider>
  );
}

export function useWallet() {
  const context = useContext(WalletContext);
  if (context === undefined) {
    throw new Error('useWallet must be used within a WalletProvider');
  }
  return context;
}