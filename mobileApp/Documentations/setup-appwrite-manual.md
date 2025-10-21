# Manual Appwrite Database Setup Guide

Since the automated setup script requires server-side API keys, here's a step-by-step manual setup guide for your Appwrite database:

## Step 1: Create Database

1. Go to your Appwrite Console: [https://cloud.appwrite.io/console](https://cloud.appwrite.io/console)
2. Select your project: `68c2dd6e002112935ed2`
3. Go to **Databases** tab
4. Click **Create Database**
5. Set Database ID: `ekehi-network-db`
6. Set Name: `Ekehi Network Database`
7. Click **Create**

## Step 2: Create Collections

For each collection below, click **Create Collection** and follow the settings:

### 1. users
- **Collection ID**: `users`
- **Name**: `Users`
- **Permissions**: 
  - Read: `users`
  - Create: `users`
  - Update: `users`
  - Delete: `users`

**Attributes:**
- `email` (String, 255, Required)
- `name` (String, 255, Required)
- `createdAt` (DateTime, Required)
- `lastLogin` (DateTime, Optional)

### 2. user_profiles
- **Collection ID**: `user_profiles`
- **Name**: `User Profiles`
- **Permissions**: Same as above

**Attributes:**
- `userId` (String, 255, Required, Unique)
- `username` (String, 255, Optional)
- `totalCoins` (Float, Required, Default: 0)
- `coinsPerSecond` (Float, Required, Default: 0)
- `totalMiningRate` (Float, Required, Default: 0.8333)
- `defaultMiningRate` (Float, Required, Default: 0.8333)
- `referralMiningRate` (Float, Required, Default: 0)
- `presaleMiningRate` (Float, Required, Default: 0)
- `currentStreak` (Integer, Required, Default: 0)
- `longestStreak` (Integer, Required, Default: 0)
- `lastLoginDate` (DateTime, Optional)
- `referralCode` (String, 255, Optional, Unique)
- `referredBy` (String, 255, Optional)
- `totalReferrals` (Integer, Required, Default: 0)
- `lifetimeEarnings` (Float, Required, Default: 0)
- `todayEarnings` (Float, Required, Default: 0)
- `lastMiningDate` (DateTime, Optional)
- `streakBonusClaimed` (Integer, Required, Default: 0)
- `createdAt` (DateTime, Required)
- `updatedAt` (DateTime, Required)

### 3. mining_sessions
- **Collection ID**: `mining_sessions`
- **Name**: `Mining Sessions`
- **Permissions**: Same as above

**Attributes:**
- `userId` (String, 255, Required)
- `coinsEarned` (Float, Required)
- `sessionDuration` (Integer, Required)
- `createdAt` (DateTime, Required)

### 4. social_tasks
- **Collection ID**: `social_tasks`
- **Name**: `Social Tasks`
- **Permissions**: Same as above

**Attributes:**
- `title` (String, 255, Required)
- `description` (String, 1000, Required)
- `platform` (String, 255, Required)
- `taskType` (String, 255, Required)
- `rewardCoins` (Float, Required)
- `actionUrl` (String, 500, Optional)
- `verificationMethod` (String, 255, Required)
- `isActive` (Boolean, Required, Default: true)
- `sortOrder` (Integer, Required, Default: 0)
- `createdAt` (DateTime, Required)

### 5. user_social_tasks
- **Collection ID**: `user_social_tasks`
- **Name**: `User Social Tasks`
- **Permissions**: Same as above

**Attributes:**
- `userId` (String, 255, Required)
- `taskId` (String, 255, Required)
- `completedAt` (DateTime, Required)

### 6. achievements
- **Collection ID**: `achievements`
- **Name**: `Achievements`
- **Permissions**: Same as above

**Attributes:**
- `achievementId` (String, 255, Required, Unique)
- `title` (String, 255, Required)
- `description` (String, 1000, Required)
- `type` (String, 255, Required)
- `target` (Float, Required)
- `reward` (Float, Required)
- `rarity` (String, 255, Required)
- `isActive` (Boolean, Required, Default: true)
- `createdAt` (DateTime, Required)

### 7. user_achievements
- **Collection ID**: `user_achievements`
- **Name**: `User Achievements`
- **Permissions**: Same as above

**Attributes:**
- `userId` (String, 255, Required)
- `achievementId` (String, 255, Required)
- `claimedAt` (DateTime, Required)

### 8. presale_purchases
- **Collection ID**: `presale_purchases`
- **Name**: `Presale Purchases`
- **Permissions**: Same as above

**Attributes:**
- `userId` (String, 255, Required)
- `amountUsd` (Float, Required)
- `tokensAmount` (Float, Required)
- `transactionHash` (String, 255, Optional)
- `status` (String, 255, Required)
- `paymentMethod` (String, 255, Optional)
- `createdAt` (DateTime, Required)

### 9. ad_views
- **Collection ID**: `ad_views`
- **Name**: `Ad Views`
- **Permissions**: Same as above

**Attributes:**
- `userId` (String, 255, Required)
- `adType` (String, 255, Required)
- `reward` (Float, Required)
- `createdAt` (DateTime, Required)

## Step 3: Update Collection IDs

After creating all collections, update your `migrate-data.js` file with the actual collection IDs from the Appwrite dashboard:

```javascript
collections: {
  users: 'users',
  userProfiles: 'user_profiles',
  miningSessions: 'mining_sessions',
  socialTasks: 'social_tasks',
  userSocialTasks: 'user_social_tasks',
  achievements: 'achievements',
  userAchievements: 'user_achievements',
  presalePurchases: 'presale_purchases',
  adViews: 'ad_views'
}
```

## Step 4: Test Connection

Run the test command to verify everything is working:

```bash
pnpm run test-appwrite
```

## Step 5: Migrate Data

Once the collections are created, you can migrate your data:

```bash
pnpm run migrate-data
```

## Next Steps

1. Create all 9 collections manually using the guide above
2. Update collection IDs in your code
3. Test the connection
4. Migrate your Cloudflare data
5. Start your app and test functionality

This manual approach ensures everything is set up correctly without needing server-side API keys.