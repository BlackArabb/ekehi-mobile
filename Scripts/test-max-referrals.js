// Test script to verify maximum referral limit logic
console.log('Testing maximum referral limit...');

// Simulate user profile with near-maximum referrals
const referrerProfile = {
  userId: 'user-1',
  totalCoins: 100,
  coinsPerSecond: 0,
  totalReferrals: 49, // Close to limit
  referralCode: 'ABC123'
};

const refereeProfile = {
  userId: 'user-2',
  totalCoins: 50,
  coinsPerSecond: 0,
  referredBy: '',
  totalReferrals: 0
};

console.log('Before referral:');
console.log('Referrer coins:', referrerProfile.totalCoins, 'EKH');
console.log('Referrer mining rate:', referrerProfile.coinsPerSecond, 'EKH/second');
console.log('Referrer referrals:', referrerProfile.totalReferrals);
console.log('Referee coins:', refereeProfile.totalCoins, 'EKH');

// Simulate referral claim process
console.log('\nProcessing referral...');

// Check if referrer has reached max referrals (50)
if (referrerProfile.totalReferrals >= 50) {
  console.log('❌ Referrer has reached maximum referrals (50)');
} else {
  console.log('✅ Referrer can accept more referrals');
  console.log('Processing referral...');
  
  // Update referrer
  referrerProfile.totalReferrals += 1;
  referrerProfile.coinsPerSecond += 0.2; // Increase mining rate by 0.2 EKH/second
  
  // Update referee
  refereeProfile.referredBy = referrerProfile.userId;
  refereeProfile.totalCoins += 2.0; // Give 2 EKH to referee
  
  console.log('\nAfter referral:');
  console.log('Referrer coins:', referrerProfile.totalCoins, 'EKH');
  console.log('Referrer mining rate:', referrerProfile.coinsPerSecond, 'EKH/second');
  console.log('Referrer referrals:', referrerProfile.totalReferrals);
  console.log('Referee coins:', refereeProfile.totalCoins, 'EKH');
  
  // Test if referrer can accept another referral
  console.log('\nTesting if referrer can accept another referral...');
  console.log('Current referrals:', referrerProfile.totalReferrals);
  console.log('Max referrals allowed: 50');
  console.log('Can accept more referrals:', referrerProfile.totalReferrals < 50);
  
  if (referrerProfile.totalReferrals >= 50) {
    console.log('❌ Referrer has now reached maximum referrals (50)');
  }
}