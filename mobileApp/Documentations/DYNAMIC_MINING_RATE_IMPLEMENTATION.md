# Dynamic Mining Rate Implementation

## Overview

This document explains the implementation of dynamic mining rate calculations in the Ekehi Network mobile app. Previously, the mining rate was displayed using a fixed calculation, but it has now been updated to use dynamic values based on each user's profile data.

**Note**: This documentation refers to the auto mining rate display. The manual mining rate is now fixed at 0.0833 EKH/hour (2 EKH per 24-hour session).

## Problem Statement

The previous implementation used a fixed calculation `(2 / 24).toFixed(4)` which always displayed 0.0833 EKH/hour regardless of the user's actual mining rate. This approach didn't account for:

1. Users with different mining powers
2. Mining rate bonuses from referrals or achievements
3. Future mining rate adjustments based on user progression

**Note**: This issue was specifically for the auto mining rate display. The manual mining rate is intentionally fixed.

## Solution

The auto mining rate calculation has been updated to use dynamic values from the user's profile data. The new calculation divides the user's `coinsPerSecond` rate by 3600 to get the hourly rate for auto mining:

```typescript
// Dynamic calculation based on user's profile for auto mining
const hourlyAutoMiningRate = profile.coinsPerSecond * 3600;
```

For manual mining, the rate is fixed at 0.0833 EKH/hour:
```typescript
// Fixed rate for manual mining (2 EKH per 24-hour session)
const hourlyManualMiningRate = 0.0833;
```

## Implementation Details

### 1. Mine Page ([mine.tsx](file:///c:/ekehi-mobile/app/(tabs)/mine.tsx))

**Manual Mining Rate Display (Fixed):**
```typescript
<Text style={styles.statValue}>
  0.0833
</Text>
```

**Auto Mining Status Component ([AutoMiningStatus.tsx](file:///c:/ekehi-mobile/mobileApp/src/components/AutoMiningStatus.tsx))**
```typescript
<Text style={styles.statValue}>
  {profile?.coinsPerSecond ? (profile.coinsPerSecond * 3600).toFixed(4) : '0.0000'}
</Text>
```

### 2. Profile Page ([profile.tsx](file:///c:/ekehi-mobile/app/(tabs)/profile.tsx))

**Auto Mining Rate Display (Dynamic):**
```typescript
<Text style={styles.statValue}>
  {profile?.coinsPerSecond ? (profile.coinsPerSecond * 3600).toFixed(4) : '0.0000'}
</Text>
```

## How It Works

### Auto Mining Rates (Dynamic):
- **Standard User**: A user with a `coinsPerSecond` of 0.05 EKH will see 180.0000 EKH/hour (0.05 × 3600 = 180)
- **Power User**: A user with a `coinsPerSecond` of 0.1 EKH will see 360.0000 EKH/hour (0.1 × 3600 = 360)
- **No Profile**: If no profile data is available, it defaults to 0.0000 EKH/hour

### Manual Mining Rate (Fixed):
- **All Users**: All users see a fixed 0.0833 EKH/hour rate (2 EKH ÷ 24 hours)

## Benefits

1. **Personalization**: Auto mining rate displays each user's actual mining rate based on their profile
2. **Accuracy**: The displayed auto mining rate accurately reflects what the user will earn per hour
3. **Scalability**: Future auto mining rate adjustments will automatically be reflected
4. **Transparency**: Users can see exactly how their auto mining power affects their earnings
5. **Simplicity**: Manual mining rate is fixed and easy to understand

## Verification

To verify that the dynamic mining rate is working correctly:

1. Check different user accounts with varying auto mining powers
2. Verify that the auto mining displayed rate matches `coinsPerSecond * 3600`
3. Confirm that referral bonuses properly affect the auto mining displayed rate
4. Test with accounts that have no profile data to ensure proper fallback
5. Verify that manual mining rate displays 0.0833 EKH/hour for all users

## Related Systems

This change affects:
- Auto mining rate display on the Mine page
- Auto mining rate display on the Profile page
- Any future calculations based on auto hourly mining rates
- Manual mining rate display (now fixed)

## Future Considerations

1. **Real-time Updates**: The auto mining rate will automatically update when profile data changes
2. **Achievement Bonuses**: Future achievement bonuses will be immediately reflected in auto mining rate
3. **Seasonal Events**: Special event auto mining rate multipliers will be automatically displayed
4. **Manual Mining**: The fixed manual mining rate provides a consistent user experience