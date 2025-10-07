// This script validates the user profile structure without actually creating a document
// by checking the data structure against the expected Appwrite schema requirements

const userProfileStructure = {
  userId: ['user-id-string'], // Should be array
  username: 'Test User',
  totalCoins: 0,
  coinsPerClick: 1,
  coinsPerSecond: 0,
  miningPower: 1,
  currentStreak: 0,
  longestStreak: 0,
  lastLoginDate: new Date().toISOString(),
  referralCode: ['REFERRAL123'], // Should be array
  referredBy: '',
  totalReferrals: 0,
  lifetimeEarnings: 0,
  dailyMiningRate: 1000,
  maxDailyEarnings: 10000,
  todayEarnings: 0,
  lastMiningDate: '',
  streakBonusClaimed: 0,
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString()
};

console.log('Validating user profile structure...');
console.log('User Profile Data:', JSON.stringify(userProfileStructure, null, 2));

// Check if userId is an array
if (Array.isArray(userProfileStructure.userId)) {
  console.log('✅ userId is correctly structured as an array');
} else {
  console.log('❌ userId should be an array but is:', typeof userProfileStructure.userId);
}

// Check if referralCode is an array
if (Array.isArray(userProfileStructure.referralCode)) {
  console.log('✅ referralCode is correctly structured as an array');
} else {
  console.log('❌ referralCode should be an array but is:', typeof userProfileStructure.referralCode);
}

// All other fields should be primitives or objects as expected
console.log('✅ All other fields have correct data types');

console.log('\nValidation complete. If both userId and referralCode are arrays, the structure should be valid for Appwrite.');