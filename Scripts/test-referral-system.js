// Test script for the referral system
const { Client, Account, Databases, ID, Query } = require('appwrite');

// Initialize the client
const client = new Client();
client
  .setEndpoint('https://fra.cloud.appwrite.io/v1')
  .setProject('68c2dd6e002112935ed2');

// Initialize services
const account = new Account(client);
const databases = new Databases(client);

// Test configuration
const config = {
  databaseId: '68c336e7000f87296feb',
  collections: {
    userProfiles: 'user_profiles'
  }
};

async function testReferralSystem() {
  console.log('Testing referral system...');
  
  try {
    // Create two test users
    const user1Id = 'test-user-1-' + Date.now();
    const user2Id = 'test-user-2-' + Date.now();
    
    // Generate referral codes
    const referralCode1 = Math.random().toString(36).substring(2, 10).toUpperCase();
    
    // Create user profiles
    const user1Profile = {
      userId: [user1Id],
      username: 'Test User 1',
      totalCoins: 0,
      coinsPerClick: 1,
      coinsPerSecond: 0, // Starting mining rate
      miningPower: 1,
      currentStreak: 0,
      longestStreak: 0,
      lastLoginDate: new Date().toISOString(),
      referralCode: [referralCode1],
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

    const user2Profile = {
      userId: [user2Id],
      username: 'Test User 2',
      totalCoins: 0,
      coinsPerClick: 1,
      coinsPerSecond: 0, // Starting mining rate
      miningPower: 1,
      currentStreak: 0,
      longestStreak: 0,
      lastLoginDate: new Date().toISOString(),
      referralCode: [''],
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

    console.log('Creating user profiles...');
    
    // Create user 1
    const user1Result = await databases.createDocument(
      config.databaseId,
      config.collections.userProfiles,
      ID.unique(),
      user1Profile
    );
    
    console.log('✅ User 1 created:', user1Result.$id);
    
    // Create user 2
    const user2Result = await databases.createDocument(
      config.databaseId,
      config.collections.userProfiles,
      ID.unique(),
      user2Profile
    );
    
    console.log('✅ User 2 created:', user2Result.$id);
    
    // Test referral claiming
    console.log('Testing referral claiming...');
    
    // User 2 claims User 1's referral code
    const referrerResponse = await databases.listDocuments(
      config.databaseId,
      config.collections.userProfiles,
      [Query.equal('referralCode', referralCode1)]
    );
    
    if (referrerResponse.documents.length > 0) {
      const referrerDoc = referrerResponse.documents[0];
      
      // Update referrer's total referrals and mining rate
      await databases.updateDocument(
        config.databaseId,
        config.collections.userProfiles,
        referrerDoc.$id,
        {
          totalReferrals: referrerDoc.totalReferrals + 1,
          coinsPerSecond: (referrerDoc.coinsPerSecond || 0) + 0.2 // Increase mining rate by 0.2 EKH/second
        }
      );
      
      // Update referee's referredBy field and give reward
      await databases.updateDocument(
        config.databaseId,
        config.collections.userProfiles,
        user2Result.$id,
        {
          referredBy: referrerDoc.userId,
          totalCoins: (user2Result.totalCoins || 0) + 2.0 // Referee reward: 2 EKH
        }
      );
      
      console.log('✅ Referral claimed successfully!');
      console.log('  Referrer mining rate increased by 0.2 EKH/second');
      console.log('  Referee received 2 EKH');
    } else {
      console.log('❌ Referrer not found');
    }
    
    // Verify results
    console.log('Verifying results...');
    
    const updatedUser1 = await databases.getDocument(
      config.databaseId,
      config.collections.userProfiles,
      user1Result.$id
    );
    
    const updatedUser2 = await databases.getDocument(
      config.databaseId,
      config.collections.userProfiles,
      user2Result.$id
    );
    
    console.log('User 1 (referrer):');
    console.log('  Total coins:', updatedUser1.totalCoins);
    console.log('  Mining rate:', updatedUser1.coinsPerSecond, 'EKH/second');
    console.log('  Total referrals:', updatedUser1.totalReferrals);
    
    console.log('User 2 (referee):');
    console.log('  Total coins:', updatedUser2.totalCoins);
    console.log('  Mining rate:', updatedUser2.coinsPerSecond, 'EKH/second');
    console.log('  Referred by:', updatedUser2.referredBy);
    
    // Clean up - delete test profiles
    console.log('Cleaning up test profiles...');
    await databases.deleteDocument(
      config.databaseId,
      config.collections.userProfiles,
      user1Result.$id
    );
    
    await databases.deleteDocument(
      config.databaseId,
      config.collections.userProfiles,
      user2Result.$id
    );
    
    console.log('✅ Test completed successfully!');
  } catch (error) {
    console.error('Test failed:', error);
  }
}

// Run the test
testReferralSystem();