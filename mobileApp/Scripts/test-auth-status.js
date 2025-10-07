// Test script to verify auth status checking
const { Client, Account } = require('appwrite');

async function testAuthStatus() {
  console.log('=== Auth Status Test ===');
  
  // Initialize Appwrite client
  const client = new Client();
  client
    .setEndpoint('https://fra.cloud.appwrite.io/v1')
    .setProject('68c2dd6e002112935ed2');

  const account = new Account(client);
  
  console.log('1. Testing auth status check...');
  try {
    const accountData = await account.get();
    console.log('   ✅ Auth check successful');
    console.log('   User ID:', accountData.$id);
    console.log('   User email:', accountData.email);
    console.log('   User name:', accountData.name);
  } catch (error) {
    console.log('   ℹ️ No active session (this is normal if not logged in)');
    console.log('   Error message:', error.message);
    console.log('   Error code:', error.code);
  }
  
  console.log('=== Test Complete ===');
}

testAuthStatus().catch(console.error);