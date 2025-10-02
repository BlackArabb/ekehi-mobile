// Test script to verify self-referral prevention logic
console.log('Testing self-referral prevention...');

// Simulate user trying to refer themselves
const userProfile = {
  userId: 'user-1',
  totalCoins: 100,
  coinsPerSecond: 0,
  totalReferrals: 5,
  referralCode: 'ABC123'
};

const referralCode = 'ABC123'; // Same as user's own code

console.log('User ID:', userProfile.userId);
console.log('User referral code:', userProfile.referralCode);
console.log('Attempting to use referral code:', referralCode);

// Check if user is trying to refer themselves
if (userProfile.referralCode === referralCode) {
  console.log('❌ Self-referral detected! Preventing referral.');
  console.log('Users cannot refer themselves.');
} else {
  console.log('✅ Valid referral code. Processing...');
  // Process referral logic here
}

// Test with different referral code
console.log('\nTesting with different referral code...');
const differentCode = 'XYZ789';

if (userProfile.referralCode === differentCode) {
  console.log('❌ Self-referral detected! Preventing referral.');
} else {
  console.log('✅ Valid referral code. Can process referral.');
}