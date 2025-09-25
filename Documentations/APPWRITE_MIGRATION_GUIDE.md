# Complete Guide: Migrating from Cloudflare to Appwrite for Ekehi Network

This document explains how to migrate your Ekehi Network mobile app backend from Cloudflare Workers to Appwrite while keeping your data intact.

## Why Migrate to Appwrite?

1. **Built-in Services**: Authentication, database, storage, and functions out of the box
2. **Developer Experience**: Comprehensive SDKs and documentation
3. **Scalability**: Automatic scaling with generous free tier
4. **Data Management**: Visual database management interface
5. **Cross-platform**: Works with mobile, web, and server applications

## Migration Overview

The migration involves:
1. Setting up Appwrite project and services
2. Migrating data from Cloudflare D1 to Appwrite Database
3. Updating mobile app to use Appwrite SDK instead of custom API calls
4. Removing Cloudflare backend dependencies
5. Testing everything works correctly

## Step-by-Step Migration Guide

### 1. Prerequisites

- An Appwrite account (Cloud or Self-hosted)
- Node.js 18+ installed
- npm or yarn package manager
- Existing Cloudflare project (for data migration)

### 2. Set Up Appwrite Project

1. **Create Appwrite Account**:
   - Visit https://cloud.appwrite.io/
   - Sign up for a free account
   - Create a new project called "Ekehi Network"

2. **Configure Platforms**:
   - In Appwrite Dashboard, go to "Platforms"
   - Add your mobile app as a platform (iOS/Android)
   - Add your web app as a platform (if needed)
   - Note your Project ID for later use

3. **Set Up Authentication**:
   - In Appwrite Dashboard, go to Authentication > Settings
   - Enable "Users" and "Sessions"
   - Configure Google OAuth provider with your Google Client ID/Secret

### 3. Create Appwrite Database Structure

1. **Create Database**:
   - In Appwrite Dashboard, go to Databases
   - Create a new database called "ekehi-network-db"

2. **Create Collections**:
   Based on your Cloudflare D1 schema, create these collections:
   
   a. **Users Collection**:
      - Name: `users`
      - Permissions: Read/Write for Any, Create for Any
      - Attributes:
        - email (string, 255, required, unique)
        - name (string, 255, required)
        - createdAt (datetime, required)
        - lastLogin (datetime)
      
   b. **User Profiles Collection**:
      - Name: `user_profiles`
      - Permissions: Read/Write for Any
      - Attributes:
        - userId (string, 255, required, unique)
        - username (string, 255)
        - totalCoins (double, default: 0)
        - coinsPerClick (integer, default: 1)
        - coinsPerSecond (double, default: 0)
        - miningPower (double, default: 1)
        - currentStreak (integer, default: 0)
        - longestStreak (integer, default: 0)
        - lastLoginDate (datetime)
        - referralCode (string, 255, unique)
        - referredBy (string, 255)
        - totalReferrals (integer, default: 0)
        - lifetimeEarnings (double, default: 0)
        - dailyMiningRate (double, default: 1000)
        - maxDailyEarnings (double, default: 10000)
        - todayEarnings (double, default: 0)
        - lastMiningDate (datetime)
        - streakBonusClaimed (integer, default: 0)
        - createdAt (datetime, required)
        - updatedAt (datetime, required)
      
   c. **Mining Sessions Collection**:
      - Name: `mining_sessions`
      - Permissions: Read/Write for Any
      - Attributes:
        - userId (string, 255, required)
        - coinsEarned (double, required)
        - clicksMade (integer, required)
        - sessionDuration (integer, required)
        - createdAt (datetime, required)
      
   d. **Social Tasks Collection**:
      - Name: `social_tasks`
      - Permissions: Read for Any, Write for Any
      - Attributes:
        - title (string, 255, required)
        - description (string, required)
        - platform (string, 255, required)
        - taskType (string, 255, required)
        - rewardCoins (double, required)
        - actionUrl (string, 255)
        - verificationMethod (string, 255, required)
        - isActive (integer, default: 1)
        - sortOrder (integer, default: 0)
        - createdAt (datetime, required)
      
   e. **User Social Tasks Collection**:
      - Name: `user_social_tasks`
      - Permissions: Read/Write for Any
      - Attributes:
        - userId (string, 255, required)
        - taskId (string, 255, required)
        - completedAt (datetime, required)
      
   f. **Achievements Collection**:
      - Name: `achievements`
      - Permissions: Read for Any, Write for Any
      - Attributes:
        - achievementId (string, 255, required, unique)
        - title (string, 255, required)
        - description (string, required)
        - type (string, 255, required)
        - target (double, required)
        - reward (double, required)
        - rarity (string, 255, required)
        - isActive (integer, default: 1)
        - createdAt (datetime, required)
      
   g. **User Achievements Collection**:
      - Name: `user_achievements`
      - Permissions: Read/Write for Any
      - Attributes:
        - userId (string, 255, required)
        - achievementId (string, 255, required)
        - claimedAt (datetime, required)
      
   h. **Presale Purchases Collection**:
      - Name: `presale_purchases`
      - Permissions: Read/Write for Any
      - Attributes:
        - userId (string, 255, required)
        - amountUsd (double, required)
        - tokensAmount (double, required)
        - transactionHash (string, 255)
        - status (string, 255, required)
        - paymentMethod (string, 255)
        - createdAt (datetime, required)
      
   i. **Ad Views Collection**:
      - Name: `ad_views`
      - Permissions: Read/Write for Any
      - Attributes:
        - userId (string, 255, required)
        - adType (string, 255, required)
        - reward (double, required)
        - createdAt (datetime, required)

### 4. Migrate Data from Cloudflare to Appwrite

1. **Export Data from Cloudflare D1**:
   ```bash
   # Use wrangler to export data
   wrangler d1 export ekehi-network-db --output-dir ./cloudflare-data
   ```

2. **Transform Data Format**:
   Create a script to convert SQLite export to Appwrite JSON format:
   ```javascript
   // transform-data.js
   const fs = require('fs');
   
   // Read Cloudflare export and convert to Appwrite format
   // This would require custom code based on your exact data structure
   ```

3. **Import Data to Appwrite**:
   Use Appwrite's import API or dashboard to import transformed data.

### 5. Update Mobile App Dependencies

1. **Install Appwrite SDK**:
   ```bash
   npm install appwrite
   ```

2. **Update package.json**:
   ```json
   {
     "dependencies": {
       // ... existing dependencies
       "appwrite": "^13.0.0"
     }
   }
   ```

### 6. Update Mobile App Configuration

1. **Create Appwrite Configuration** (`src/config/appwrite.ts`):
   ```typescript
   import { Client, Account, Databases } from 'appwrite';
   
   const client = new Client();
   
   client
     .setEndpoint('https://cloud.appwrite.io/v1') // Your Appwrite endpoint
     .setProject('YOUR_PROJECT_ID');              // Your project ID
   
   export const account = new Account(client);
   export const databases = new Databases(client);
   
   export const appwriteConfig = {
     endpoint: 'https://cloud.appwrite.io/v1',
     projectId: 'YOUR_PROJECT_ID',
     databaseId: 'ekehi-network-db',
     collections: {
       users: 'users_collection_id',
       userProfiles: 'user_profiles_collection_id',
       miningSessions: 'mining_sessions_collection_id',
       socialTasks: 'social_tasks_collection_id',
       userSocialTasks: 'user_social_tasks_collection_id',
       achievements: 'achievements_collection_id',
       userAchievements: 'user_achievements_collection_id',
       presalePurchases: 'presale_purchases_collection_id',
       adViews: 'ad_views_collection_id'
     }
   };
   ```

### 7. Update Authentication Context

Replace `src/contexts/AuthContext.tsx` with Appwrite-based authentication:

```typescript
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as WebBrowser from 'expo-web-browser';
import { Platform } from 'react-native';
import { User } from '@/types';
import { account } from '@/config/appwrite';
import { ID } from 'appwrite';

// Polyfill for web browser authentication
if (Platform.OS === 'web') {
  WebBrowser.maybeCompleteAuthSession();
}

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  signIn: () => Promise<void>;
  signInWithEmail: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  signUp: (email: string, password: string, name: string) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = async () => {
    try {
      console.log('Checking auth status with Appwrite...');
      // Get current user from Appwrite
      const accountData = await account.get();
      if (accountData) {
        // Transform Appwrite user to your User type
        const userData: User = {
          id: accountData.$id,
          email: accountData.email,
          name: accountData.name
        };
        setUser(userData);
      }
    } catch (error) {
      console.error('Auth check failed:', error);
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  const signIn = async () => {
    try {
      console.log('Starting Google OAuth sign in...');
      // For web, redirect the entire page to the OAuth URL
      if (Platform.OS === 'web') {
        await account.createOAuth2Token(
          'google',
          `${window.location.origin}/auth`, // Success URL
          `${window.location.origin}/auth`  // Failure URL
        );
      } else {
        // For native platforms, use WebBrowser
        const successUrl = 'ekehi://oauth/callback';
        const result = await account.createOAuth2Token(
          'google',
          successUrl, // Success URL
          successUrl  // Failure URL
        );
        
        // Open the OAuth URL in browser
        const response = await WebBrowser.openAuthSessionAsync(result, successUrl);
        
        if (response.type === 'success') {
          // Handle the redirect and extract session
          await checkAuthStatus();
        } else if (response.type === 'dismiss') {
          throw new Error('Authentication cancelled');
        } else {
          throw new Error('Authentication failed');
        }
      }
    } catch (error) {
      console.error('Sign in failed:', error);
      throw error;
    }
  };

  const signInWithEmail = async (email: string, password: string) => {
    try {
      await account.createEmailPasswordSession(email, password);
      await checkAuthStatus();
    } catch (error) {
      console.error('Email sign in failed:', error);
      throw error;
    }
  };

  const signUp = async (email: string, password: string, name: string) => {
    try {
      // Create new user account
      await account.create(ID.unique(), email, password, name);
      // Create session
      await account.createEmailPasswordSession(email, password);
      await checkAuthStatus();
    } catch (error) {
      console.error('Sign up failed:', error);
      throw error;
    }
  };

  const signOut = async () => {
    try {
      await account.deleteSession('current');
      setUser(null);
    } catch (error) {
      console.error('Sign out failed:', error);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, signIn, signInWithEmail, signOut, signUp }}>
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
```

### 8. Update Other Context Files

Update other context files to use Appwrite instead of custom API calls. For example, `src/contexts/MiningContext.tsx`:

```typescript
// Example update for MiningContext.tsx
import { databases, appwriteConfig } from '@/config/appwrite';
import { Query } from 'appwrite';

// Replace API calls with Appwrite database calls
const getUserProfile = async (userId: string) => {
  try {
    const response = await databases.listDocuments(
      appwriteConfig.databaseId,
      appwriteConfig.collections.userProfiles,
      [Query.equal('userId', userId)]
    );
    return response.documents[0];
  } catch (error) {
    console.error('Failed to get user profile:', error);
    throw error;
  }
};
```

### 9. Remove Cloudflare Backend

1. **Delete Cloudflare Backend Directory**:
   ```bash
   rm -rf cloudflare-backend/
   ```

2. **Remove Cloudflare-specific configurations**:
   - Remove `src/config/api.ts` or update it to remove Cloudflare endpoints
   - Update any remaining references to Cloudflare URLs

### 10. Update Documentation

1. **Update README.md**:
   Replace Cloudflare-specific information with Appwrite information

2. **Create New Migration Status Document**:
   Document the current status of the Appwrite migration

### 11. Testing

1. **Authentication Testing**:
   - Test Google OAuth sign in
   - Test email/password sign in
   - Test sign up flow
   - Test sign out functionality

2. **Data Operations Testing**:
   - Test profile retrieval and updates
   - Test mining operations
   - Test social tasks completion
   - Test achievement tracking

3. **Cross-platform Testing**:
   - Test on iOS
   - Test on Android
   - Test on web (if applicable)

## Benefits After Migration

1. **Simplified Architecture**: No need to maintain custom backend
2. **Built-in Features**: Authentication, database, storage all provided
3. **Reduced Complexity**: Less custom code to maintain
4. **Better Developer Experience**: Comprehensive dashboard and tools
5. **Scalability**: Automatic scaling handled by Appwrite

## Next Steps

1. Complete the data migration process
2. Update all context files to use Appwrite
3. Remove Cloudflare backend code
4. Thoroughly test all app features
5. Update documentation with new endpoints and processes
6. Monitor performance and usage metrics

## Getting Help

- Appwrite documentation: https://appwrite.io/docs
- Appwrite GitHub: https://github.com/appwrite/appwrite
- Appwrite Discord community: https://appwrite.io/discord