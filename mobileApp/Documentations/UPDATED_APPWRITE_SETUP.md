# Updated Appwrite Setup Guide

This document provides updated instructions for setting up Appwrite collections for the Ekehi Network mobile app, reflecting the current data structure and recent changes.

## Current Collection Structure

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
- `userId` (String, 255, Required)
- `username` (String, 255, Optional)
- `totalCoins` (Float, Required, Default: 0)
- `coinsPerSecond` (Float, Required, Default: 0) - Deprecated but still in use
- `autoMiningRate` (Float, Required, Default: 0) - New field for auto mining rate
- `miningPower` (Float, Required, Default: 1)
- `referralBonusRate` (Float, Required, Default: 0) - New field for referral bonus
- `currentStreak` (Integer, Required, Default: 0)
- `longestStreak` (Integer, Required, Default: 0)
- `lastLoginDate` (DateTime, Optional)
- `referralCode` (String, 255, Optional)
- `referredBy` (String, 255, Optional)
- `totalReferrals` (Integer, Required, Default: 0)
- `lifetimeEarnings` (Float, Required, Default: 0)
- `dailyMiningRate` (Float, Required, Default: 2) - Updated to 2 EKH per 24-hour session
- `maxDailyEarnings` (Float, Required, Default: 10000)
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
- `clicksMade` (Integer, Required)
- `sessionDuration` (Integer, Required)
- `createdAt` (DateTime, Required)
- `updatedAt` (DateTime, Required)

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
- `updatedAt` (DateTime, Required)

### 5. user_social_tasks
- **Collection ID**: `user_social_tasks`
- **Name**: `User Social Tasks`
- **Permissions**: Same as above

**Attributes:**
- `userId` (String, 255, Required)
- `taskId` (String, 255, Required)
- `completedAt` (DateTime, Required)
- `createdAt` (DateTime, Required)
- `updatedAt` (DateTime, Required)

### 6. achievements
- **Collection ID**: `achievements`
- **Name**: `Achievements`
- **Permissions**: Same as above

**Attributes:**
- `achievementId` (String, 255, Required)
- `title` (String, 255, Required)
- `description` (String, 1000, Required)
- `type` (String, 255, Required)
- `target` (Float, Required)
- `reward` (Float, Required)
- `rarity` (String, 255, Required)
- `isActive` (Boolean, Required, Default: true)
- `createdAt` (DateTime, Required)
- `updatedAt` (DateTime, Required)

### 7. user_achievements
- **Collection ID**: `user_achievements`
- **Name**: `User Achievements`
- **Permissions**: Same as above

**Attributes:**
- `userId` (String, 255, Required)
- `achievementId` (String, 255, Required)
- `claimedAt` (DateTime, Required)
- `createdAt` (DateTime, Required)
- `updatedAt` (DateTime, Required)

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
- `updatedAt` (DateTime, Required)

### 9. ad_views
- **Collection ID**: `ad_views`
- **Name**: `Ad Views`
- **Permissions**: Same as above

**Attributes:**
- `userId` (String, 255, Required)
- `adType` (String, 255, Required)
- `reward` (Float, Required)
- `createdAt` (DateTime, Required)
- `updatedAt` (DateTime, Required)

## Setup Process

### Step 1: Create Database
1. Go to your Appwrite Console
2. Select your project
3. Go to **Databases** tab
4. Click **Create Database**
5. Set Database ID: `68c336e7000f87296feb` (or your preferred ID)
6. Set Name: `Ekehi Network Database`
7. Click **Create**

### Step 2: Create Collections
Use the automated script or create collections manually:

```bash
# Generate an API key in your Appwrite console first
# Then run:
node Scripts/update-appwrite-collections.js
```

### Step 3: Generate Sample Data (Optional)
For testing purposes:

```bash
node Scripts/generate-sample-data.js
```

### Step 4: Migrate Data
```bash
pnpm run migrate-data
```

## Recent Changes

### Manual Mining System Update
- Removed `coinsPerClick` from UserProfile (deprecated)
- Updated `dailyMiningRate` default to 2 EKH per 24-hour session
- Added `autoMiningRate` for auto mining feature
- Added `referralBonusRate` for referral bonus feature

### New Fields
- `autoMiningRate` - Tracks user's auto mining rate
- `referralBonusRate` - Tracks user's referral bonus rate
- `clicksMade` - Tracks clicks in mining sessions
- `updatedAt` - Added to all collections for better data tracking

## Testing
After setup, verify:
1. All collections exist with correct attributes
2. Permissions are set correctly
3. Sample data migrates successfully
4. Mobile app connects and functions properly

## Troubleshooting
1. **Collection creation fails**: Check API key permissions
2. **Migration fails**: Verify collection IDs match
3. **Connection issues**: Check endpoint and project ID
4. **Permission errors**: Ensure user roles are correctly set