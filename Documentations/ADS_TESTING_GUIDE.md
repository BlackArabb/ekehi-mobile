# Ekehi Mobile Ad System Testing Guide

This guide explains how to test the advertisement system implemented in the Ekehi Mobile application.

## Overview

The Ekehi Mobile app includes a comprehensive ad system that allows users to earn EKH tokens by watching advertisements. The system consists of:

1. **AdModal Component** - UI component for displaying ad interactions
2. **Reward System** - Mechanism for distributing EKH tokens
3. **Cooldown Management** - Time-based restrictions between ad views
4. **Database Integration** - Recording of ad views and rewards
5. **Admin Dashboard** - Management interface for ad campaigns

## Current Implementation

### User Flow
1. User navigates to the "Mine" tab
2. User clicks "Watch Ad for +0.5 EKH" button
3. AdModal appears with reward information
4. User watches a real AdMob test ad
5. User receives EKH reward upon ad completion
6. 5-minute cooldown begins

### Technical Components

#### AdModal Component (`src/components/AdModal.tsx`)
- Displays ad information and rewards
- Shows countdown timer during ad viewing
- Handles real AdMob ad integration
- Provides skip functionality

#### Mine Screen Integration (`app/(tabs)/mine.tsx`)
- Ad button with cooldown timer
- Reward processing logic
- Database recording of ad views
- User notification system

#### Admin Dashboard (`admin/app/dashboard/ads/page.tsx`)
- Campaign management interface
- Statistics and analytics
- Campaign creation/editing

## Testing Procedures

### 1. Manual UI Testing

#### Test Ad Modal Display
1. Navigate to the Mine tab
2. Click the "Watch Ad for +0.5 EKH" button
3. Verify the AdModal appears with correct information:
   - Title: "Watch Ad for Bonus"
   - Description: "Watch a short advertisement to earn 0.5 EKH bonus tokens!"
   - Reward: "+0.5 EKH"
   - TEST MODE indicator visible in development

#### Test Ad Viewing Flow
1. In the AdModal, click "Watch Ad"
2. A real AdMob test ad will load and play
3. Watch the ad to completion (or close it)
4. Verify the reward is added to the user's balance
5. Verify the database record is created

#### Test Cooldown System
1. Watch an ad and receive a reward
2. Immediately try to watch another ad
3. Verify the button is disabled with countdown timer
4. Wait 5 minutes
5. Verify the button becomes enabled again

### 2. Automated Testing

#### Using the Test Script
Run the ad testing script:
```bash
node Scripts/test-ads.js
```

This script tests:
- Different reward scenarios
- Cooldown functionality
- Database integration
- Error handling

### 3. Database Verification

#### Check Ad Views Collection
1. Access the Appwrite console
2. Navigate to the `ad_views` collection
3. Verify ad view records are created with:
   - userId
   - adType
   - reward
   - createdAt timestamp

### 4. Admin Dashboard Testing

#### Campaign Management
1. Access the admin dashboard at `/admin/dashboard/ads`
2. Create a new ad campaign
3. Edit an existing campaign
4. Delete a campaign
5. Verify statistics update correctly

## Test Scenarios

### Scenario 1: Standard Ad View
- User watches ad successfully
- Receives 0.5 EKH reward
- Cooldown activates for 5 minutes
- Database record created

### Scenario 2: Ad Skip
- User starts watching ad
- User skips ad before completion
- No reward given (depending on AdMob policy)
- No cooldown (in current implementation)

### Scenario 3: Multiple Users
- Multiple users watch ads simultaneously
- Each user receives correct rewards
- Individual cooldowns maintained

### Scenario 4: Error Handling
- Simulate network errors
- Verify error messages display correctly
- Verify no rewards given on error
- Verify system recovers gracefully

## Debugging Tips

### Console Logging
The ad system includes console logging for debugging:
- Ad completion events
- Reward processing
- Error conditions
- AdMob event tracking

### Test User Creation
For comprehensive testing, create test users with different profiles:
```javascript
// Example test user data
{
  username: "testuser001",
  totalCoins: 100.0,
  todayEarnings: 10.0,
  maxDailyEarnings: 1000.0
}
```

## Enhancement Recommendations

### 1. Real Ad Network Integration
- Integrate with actual ad networks (Google AdMob, etc.)
- Implement real ad content delivery
- Add ad targeting capabilities

### 2. Advanced Reward System
- Variable rewards based on ad length
- Bonus rewards for consecutive ad views
- Special event ads with higher rewards

### 3. Analytics Improvements
- Detailed user engagement metrics
- Ad performance tracking
- Conversion rate optimization

### 4. User Experience
- Ad preview functionality
- Reward history tracking
- Achievement system for ad viewing

## Troubleshooting

### Common Issues

#### Ad Not Recording
- Check database connection
- Verify user authentication
- Confirm Appwrite permissions

#### Cooldown Not Working
- Check AsyncStorage implementation
- Verify timestamp calculations
- Test on different devices

#### Rewards Not Updating
- Check profile refresh mechanism
- Verify database update permissions
- Confirm network connectivity

### Logs and Monitoring
Check the following for debugging information:
- Browser console (web)
- React Native logs (mobile)
- Appwrite function logs
- Database query logs

## Conclusion

The Ekehi Mobile ad system provides a solid foundation for monetization through user engagement. The testing procedures outlined in this guide will help ensure the system functions correctly and provides a positive user experience.