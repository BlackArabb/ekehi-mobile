# Ekehi Network Auto Mining Feature Documentation

## Overview

The Auto Mining feature is a passive income generation system that allows users who participate in the Ekehi Network token presale to earn EKH tokens automatically based on their purchase amount. This feature provides continuous earnings without requiring user interaction, creating a compelling incentive for early adopters.

## Business Requirements

### Eligibility Criteria
1. **PARTICIPANT IN PRESALE**: User must have made at least one presale purchase
2. **MINIMUM PURCHASE AMOUNT**: Total completed purchases must be $50 or more
3. **RATE CALCULATION**: Mining rate is proportional to total purchase amount

### Maximum Limits
1. **MAXIMUM MINING RATE PURCHASE AMOUNT (mmPA)**: $10,000 - Purchase amount used for mining rate calculation
2. **MAXIMUM GENERAL PURCHASE AMOUNT (mGPA)**: $50,000 - Total purchase amount limit
3. **MAXIMUM MINING RATE (mMR)**: 10 EKH/second - Cap on mining rate regardless of purchase amount

### Important Notes
- mmPA is not equivalent to mGPA
- Even if a user reaches mGPA, the mining rate is still capped at mmPA
- Users can purchase up to mGPA but mining rate calculation stops at mmPA

### Rate Calculation Formula
```
Effective Purchase Amount = min(Total Completed Purchases, mmPA)
Auto Mining Rate = Effective Purchase Amount × 0.001 EKH/second
Final Mining Rate = min(Auto Mining Rate, mMR)
```

### Examples
- $50 purchase = 0.05 EKH/second
- $100 purchase = 0.1 EKH/second
- $500 purchase = 0.5 EKH/second
- $1000 purchase = 1.0 EKH/second
- $5000 purchase = 5.0 EKH/second
- $10000 purchase = 10.0 EKH/second (mmPA reached)
- $15000 purchase = 10.0 EKH/second (capped at mMR)
- $50000 purchase = 10.0 EKH/second (capped at mMR, mGPA reached)

### Key Features
- **Maximum Mining Rate Cap**: Mining rate capped at 10 EKH/second
- **Maximum Purchase Limits**: Two-tier purchase limits (mmPA and mGPA)
- **Real-time Updates**: Rate updates automatically when new purchases are made
- **Profile Integration**: Status clearly displayed in user profile
- **Progress Tracking**: Ineligible users can see progress toward minimum requirement

## Technical Implementation

### Architecture Overview
The auto mining feature is implemented through several key components:

1. **PresaleContext**: Manages presale purchase data and rate calculations
2. **MiningContext**: Integrates auto mining rates with existing mining system
3. **AutoMiningManager**: Handles automatic rate updates when purchases change
4. **AutoMiningInfo**: Displays auto mining status in user profile
5. **Profile Page**: Integrates auto mining information display

### Core Components

#### PresaleContext Enhancements
The PresaleContext was enhanced with new functions and limits:

```typescript
interface PresaleContextType {
  // Existing properties...
  calculateAutoMiningRate: () => number;
  isAutoMiningEligible: () => boolean;
  autoMiningMinPurchase: number;
  // New maximum limits
  maxMiningRatePurchaseAmount: number; // mmPA: $10,000
  maxGeneralPurchaseAmount: number; // mGPA: $50,000
  maxMiningRate: number; // mMR: 10 EKH/second
}
```

**Key Functions:**
- `calculateAutoMiningRate()`: Calculates current EKH/second rate based on purchases with maximum limits
- `isAutoMiningEligible()`: Determines if user meets minimum purchase requirement
- `autoMiningMinPurchase`: Configurable minimum purchase amount ($50)
- `maxMiningRatePurchaseAmount`: Maximum purchase amount for mining rate calculation ($10,000)
- `maxGeneralPurchaseAmount`: Maximum general purchase amount ($50,000)
- `maxMiningRate`: Maximum mining rate cap (10 EKH/second)

#### AutoMiningManager Component
A background component that automatically updates user mining rates:

```typescript
const AutoMiningManager: React.FC = () => {
  const { user } = useAuth();
  const { profile, refreshProfile } = useMining();
  const { purchases, calculateAutoMiningRate } = usePresale();

  useEffect(() => {
    updateAutoMiningRate();
  }, [purchases, updateAutoMiningRate]);
  
  // ... implementation details
};
```

#### AutoMiningInfo Component
A UI component that displays auto mining status in the profile:

```typescript
interface AutoMiningInfoProps {
  profile: UserProfile | null;
}

const AutoMiningInfo: React.FC<AutoMiningInfoProps> = ({ profile }) => {
  // ... implementation details
};
```

### Data Flow

1. **Purchase Tracking**: PresaleContext monitors user purchases
2. **Rate Calculation**: When purchases change, auto mining rate is recalculated with maximum limits
3. **Database Update**: User profile is updated with new coinsPerSecond value
4. **UI Refresh**: Profile page displays updated auto mining status
5. **Mining Integration**: AutoMiningStatus component uses coinsPerSecond for passive earnings

### Performance Optimizations

#### Silent Profile Updates
To prevent visual disruption during frequent updates:
```typescript
const silentRefreshProfile = useCallback(async () => {
  // Update profile without showing loading indicators
  // Only update when data actually changes
});
```

#### Memoized Components
Profile stats and auto mining information use memoization:
```typescript
const AutoMiningInfo = memo(({ profile }) => {
  // Component only re-renders when props change
});
```

#### Efficient Calculations
Rate calculations are optimized to avoid unnecessary computations:
```typescript
const calculateAutoMiningRate = useCallback((): number => {
  // Only calculate when purchases or user data changes
  // Apply maximum limits in calculation
}, [purchases, user]);
```

## Integration Points

### Profile Page Integration
The AutoMiningInfo component is integrated into the profile page:

```typescript
// In profile.tsx
return (
  <LinearGradient>
    <ScrollView>
      {/* Existing profile content */}
      
      {/* Auto Mining Information */}
      <AutoMiningInfo profile={profile} />
      
      {/* Remaining profile content */}
    </ScrollView>
  </LinearGradient>
);
```

### Mining Context Integration
The MiningContext automatically updates auto mining rates:

```typescript
// In MiningContext.tsx
useEffect(() => {
  if (user && profile) {
    updateAutoMiningRate();
  }
}, [purchases, user, profile]);
```

### App Layout Integration
The AutoMiningManager is included in the root layout:

```typescript
// In _layout.tsx
export default function RootLayout() {
  return (
    <AuthProvider>
      <PresaleProvider>
        <MiningProvider>
          {/* Other providers */}
          <AutoMiningManager />
          {/* App navigation */}
        </MiningProvider>
      </PresaleProvider>
    </AuthProvider>
  );
}
```

## Configuration

### Rate Parameters
The auto mining system uses configurable parameters:

```typescript
const autoMiningMinPurchase = 50; // $50 minimum for eligibility
const autoMiningRatePerDollar = 0.001; // 0.001 EKH/second per dollar spent
const maxMiningRatePurchaseAmount = 10000; // $10,000 mmPA
const maxGeneralPurchaseAmount = 50000; // $50,000 mGPA
const maxMiningRate = 10; // 10 EKH/second mMR
```

### Database Integration
Auto mining rates are stored in the user profile:

```typescript
interface UserProfile {
  // Existing properties...
  coinsPerSecond: number; // Auto mining rate in EKH/second
}
```

## User Experience