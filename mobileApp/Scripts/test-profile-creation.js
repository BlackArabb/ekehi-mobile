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

// Test user data (this would normally come from OAuth)
const testUserData = {
  $id: 'test-user-id-' + Date.now(),
  name: 'Test User',
  email: 'test@example.com'
};

async function testProfileCreation() {
  console.log('Testing user profile creation...');
  
  try {
    // Generate a referral code
    const referralCode = Math.random().toString(36).substring(2, 10).toUpperCase();
    
    // Create the user profile object with arrays for userId and referralCode
    const userProfile = {
      userId: [testUserData.$id], // Array as required by Appwrite schema
      username: testUserData.name || `user_${testUserData.$id.substring(0, 8)}`,
      totalCoins: 0,
      coinsPerSecond: 0,
      miningPower: 1,
      currentStreak: 0,
      longestStreak: 0,
      lastLoginDate: new Date().toISOString(),
      referralCode: [referralCode], // Array as required by Appwrite schema
      referredBy: '',
      totalReferrals: 0,
      lifetimeEarnings: 0,
      dailyMiningRate: 2, // UPDATED: Now represents 2 EKH per 24-hour session
      maxDailyEarnings: 10000,
      todayEarnings: 0,
      lastMiningDate: '',
      streakBonusClaimed: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };

    console.log('Creating user profile with data:', userProfile);
    
    // Create the document
    const result = await databases.createDocument(
      config.databaseId,
      config.collections.userProfiles,
      ID.unique(),
      userProfile
    );
    
    console.log('✅ User profile created successfully:', result.$id);
    
    // Test querying by userId array
    console.log('Testing query by userId array...');
    const queryResult = await databases.listDocuments(
      config.databaseId,
      config.collections.userProfiles,
      [Query.equal('userId', [testUserData.$id])]
    );
    
    console.log('Query result:', queryResult.total, 'profiles found');
    
    // Clean up - delete the test profile
    if (result.$id) {
      console.log('Cleaning up test profile...');
      await databases.deleteDocument(
        config.databaseId,
        config.collections.userProfiles,
        result.$id
      );
      console.log('✅ Test profile cleaned up');
    }
    
    return true;
  } catch (error) {
    console.error('❌ Failed to create user profile:', error);
    console.error('Error details:', {
      message: error.message,
      code: error.code,
      type: error.type
    });
    return false;
  }
}

// Run the test
testProfileCreation().then(success => {
  process.exit(success ? 0 : 1);
});