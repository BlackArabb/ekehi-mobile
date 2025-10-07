# Mining Session Recording Implementation

## Overview

This document explains how mining sessions are recorded in the Ekehi Network mobile app. Mining sessions track user mining activity and store this data in the Appwrite database for analytics and rewards purposes.

## Implementation Details

### 1. Mining Context Updates

The [MiningContext.tsx](file:///c:/Users/ARQAM%20TV/Downloads/mobile/src/contexts/MiningContext.tsx) file has been updated to include session recording functionality:

1. **Session Tracking State**: Added `sessionStartTime` to track when mining sessions begin
2. **Enhanced endMiningSession**: Modified to be async and call `recordMiningSession()`
3. **New recordMiningSession Function**: Creates records in the `mining_sessions` collection

### 2. Session Recording Logic

When a mining session ends, the app:

1. Calculates the session duration (in seconds)
2. Filters out sessions shorter than 5 seconds (to avoid spam)
3. Creates a new document in the `mining_sessions` collection with:
   - `userId`: The ID of the user who mined
   - `coinsEarned`: Total coins earned during the session
   - `clicksMade`: Number of mining clicks performed
   - `sessionDuration`: Duration of the session in seconds
   - `createdAt`: Timestamp when the session was recorded

### 3. When Sessions Are Recorded

Mining sessions are recorded in the following scenarios:

1. **Page Navigation**: When users navigate away from the mining page
2. **App Backgrounding**: When the app is sent to the background
3. **Auto Mining Toggle**: When users stop auto mining
4. **Component Cleanup**: When mining-related components are unmounted

## Data Structure

### MiningSession Interface

```typescript
interface MiningSession {
  id: string;
  userId: string;
  coinsEarned: number;
  clicksMade: number;
  sessionDuration: number; // in seconds
  createdAt: string;
  updatedAt: string;
}
```

### Database Collection

The `mining_sessions` collection in Appwrite has the following attributes:
- `userId` (string, required): ID of the user
- `coinsEarned` (double, required): Total coins earned
- `clicksMade` (integer, required): Number of clicks performed
- `sessionDuration` (integer, required): Duration in seconds
- `createdAt` (datetime, required): Creation timestamp
- `updatedAt` (datetime, required): Last update timestamp

## Verification

To verify that mining sessions are being recorded:

1. Perform mining activities in the app
2. Navigate away from the mining page or background the app
3. Check the Appwrite dashboard for new documents in the `mining_sessions` collection
4. Verify that the session data matches the user's mining activity

## Benefits

1. **Analytics**: Track user engagement and mining patterns
2. **Rewards**: Enable time-based rewards and achievements
3. **Fraud Prevention**: Identify and filter out suspicious activity
4. **User Insights**: Provide users with detailed mining statistics