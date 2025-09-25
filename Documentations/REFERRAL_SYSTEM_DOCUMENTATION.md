# Ekehi Network Referral System Implementation

## Overview
This document describes the implementation of the referral system for the Ekehi Network mobile app. The system allows users to invite friends and earn rewards for successful referrals.

## Components

### 1. ReferralContext
The core of the referral system is implemented in `src/contexts/ReferralContext.tsx`. This context provides:

- **Referral Code Generation**: Unique referral codes for each user
- **Referral Claiming**: Ability for users to claim referral codes from friends
- **Reward Distribution**: Automatic distribution of rewards to both referrer and referee
- **Referral Tracking**: Tracking of total referrals and referral history
- **Deep Linking Support**: Handling of referral links (`ekehi://referral/CODE`)

### 2. Profile Integration
The referral system is integrated into the user profile screen (`app/(tabs)/profile.tsx`) with:

- Display of user's referral code
- Copy and share functionality for referral codes
- Link to detailed referral screen

### 3. Dedicated Referral Screen
A comprehensive referral screen (`src/components/ReferralScreen.tsx`) provides:

- Referral code display with copy/share options
- Referral code claiming form
- Referral statistics (total referrals, rewards per referral)
- Referral history tracking
- Visual feedback for all actions

### 4. Deep Linking
The system supports deep linking through:
- Dynamic route handler (`app/referral/[code].tsx`)
- App scheme configuration in `app.json`

## Features

### Referral Code Generation
- Each user automatically receives a unique referral code
- Codes are generated with high uniqueness to prevent collisions
- Fallback generation method ensures codes are always created

### Referral Rewards
- **Referrer Reward**: 100 EKH coins for each successful referral
- **Referee Reward**: 50 EKH coins for using a referral code
- Automatic reward distribution when referrals are claimed

### Referral Tracking
- Total referral count tracking
- Referral history with referred user information
- Prevention of self-referrals
- Prevention of multiple referrals for the same user

### Deep Linking
- Users can open referral links directly in the app
- Automatic processing of referral codes from links
- Proper error handling for invalid links

## Implementation Details

### Data Structure
The referral system uses the existing user profile structure with these fields:
- `referralCode`: Unique code for each user
- `referredBy`: ID of the user who referred this user
- `totalReferrals`: Count of successful referrals

### Reward Distribution
Rewards are distributed through direct database updates:
1. Referrer receives 100 coins
2. Referee receives 50 coins
3. Both user profiles are updated in real-time

### Security Measures
- Users cannot refer themselves
- Users cannot claim multiple referral codes
- Proper error handling for invalid codes
- Authentication required for all operations

## User Flow

1. **Existing User**:
   - Opens profile screen
   - Sees their referral code
   - Shares referral code with friends
   - Receives rewards when friends use the code

2. **New User**:
   - Receives referral link from friend
   - Opens link in app or browser
   - Automatically navigates to referral processing
   - Receives reward for using referral code

## API Endpoints

The system uses Appwrite database operations:
- `listDocuments`: Query user profiles by referral code
- `updateDocument`: Update user profiles with referral information and rewards

## Error Handling

The system provides comprehensive error handling:
- Invalid referral codes
- Self-referral attempts
- Duplicate referral attempts
- Network errors
- Authentication errors

## Testing

The system includes a test script (`test-referral-system.js`) for verifying functionality, though it requires proper authentication to run successfully.

## Future Enhancements

Potential improvements for future versions:
- Tiered referral rewards
- Referral leaderboard
- Social sharing integration
- Analytics dashboard
- Referral expiration dates