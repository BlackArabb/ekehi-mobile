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
- **Referee Reward**: 2.0 EKH coins for using a referral code
- **Referrer Reward**: No direct coins but increased mining rate by 0.2 EKH/second for each referral
- **Maximum Referrals**: Each user can refer up to 50 new users
- Automatic reward distribution when referrals are claimed

### Referral Tracking
- Total referral count tracking
- Referral history with referred user information
- Prevention of self-referrals
- Prevention of multiple referrals for the same user
- Maximum referral limit enforcement (50 referrals per user)

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
- `coinsPerSecond`: Mining rate that increases with referrals

### Reward Distribution
Rewards are distributed through direct database updates:
1. Referrer receives increased mining rate (0.2 EKH/second per referral)
2. Referee receives 2.0 EKH coins
3. Both user profiles are updated in real-time

### Security Measures
- Users cannot refer themselves
- Users cannot claim multiple referral codes
- Users cannot exceed 50 referrals
- Proper error handling for invalid codes
- Authentication required for all operations

## User Flow

1. **Existing User**:
   - Opens profile screen
   - Sees their referral code
   - Shares referral code with friends
   - Receives increased mining rate when friends use the code

2. **New User**:
   - Receives referral link from friend
   - Opens link in app or browser
   - Claims referral code and receives 2 EKH
   - Friend's mining rate increases by 0.2 EKH/second

## Future Enhancements

Potential improvements for future versions:
- Tiered referral rewards
- Referral leaderboard
- Social sharing integration
- Analytics dashboard
- Referral expiration dates