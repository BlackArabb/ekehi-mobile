// Create Test User Script
// Creates a test user account in Appwrite for testing

const { Client, Account, ID } = require('node-appwrite');

const APPWRITE_CONFIG = {
  endpoint: 'https://fra.cloud.appwrite.io/v1',
  projectId: '68c2dd6e002112935ed2',
  apiKey: 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d'
};

const client = new Client();
client
  .setEndpoint(APPWRITE_CONFIG.endpoint)
  .setProject(APPWRITE_CONFIG.projectId)
  .setKey(APPWRITE_CONFIG.apiKey);

const account = new Account(client);

async function createTestUser() {
  console.log('🚀 Creating test user account...');
  
  try {
    // Create a test user
    const testUser = await account.create(
      ID.unique(),
      'test@ekehi.network',
      'testpassword123',
      'Test User'
    );
    
    console.log('✅ Test user created successfully!');
    console.log(`📧 Email: test@ekehi.network`);
    console.log(`🔑 Password: testpassword123`);
    console.log(`👤 User ID: ${testUser.$id}`);
    
    console.log('\\n📋 You can now:');
    console.log('1. Use these credentials to sign in to your app');
    console.log('2. Test the authentication flow');
    console.log('3. Verify all app features work correctly');
    
  } catch (error) {
    if (error.message.includes('already exists')) {
      console.log('⚠️  Test user already exists!');
      console.log('📧 Email: test@ekehi.network');
      console.log('🔑 Password: testpassword123');
    } else {
      console.error('❌ Failed to create test user:', error.message);
    }
  }
}

createTestUser().catch(console.error);