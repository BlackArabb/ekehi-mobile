// Comprehensive test for auth functionality
const { Client, Account } = require('appwrite');

async function runComprehensiveTest() {
  console.log('=== Comprehensive Auth Test ===');
  
  // Initialize Appwrite client
  const client = new Client();
  client
    .setEndpoint('https://fra.cloud.appwrite.io/v1')
    .setProject('68c2dd6e002112935ed2');

  const account = new Account(client);
  
  console.log('1. Testing client configuration...');
  console.log('   Endpoint:', client.config.endpoint);
  console.log('   Project:', client.config.project);
  
  console.log('2. Testing connection to Appwrite...');
  try {
    const response = await account.get();
    console.log('   ✅ Connection successful');
    console.log('   User ID:', response.$id);
    console.log('   User email:', response.email);
  } catch (error) {
    console.log('   ℹ️  No active session (this is normal if not logged in)');
    console.log('   Error message:', error.message);
    console.log('   Error code:', error.code);
  }
  
  console.log('3. Testing OAuth URL generation...');
  try {
    const successUrl = 'https://ekehi-oauth.netlify.app/oauth/return';
    const failureUrl = 'https://ekehi-oauth.netlify.app/auth';
    
    const oauthUrl = account.createOAuth2Token('google', successUrl, failureUrl);
    console.log('   ✅ OAuth URL generation successful');
    console.log('   OAuth URL:', oauthUrl);
  } catch (error) {
    console.log('   ❌ OAuth URL generation failed:', error.message);
  }
  
  console.log('=== Test Complete ===');
}

runComprehensiveTest().catch(console.error);