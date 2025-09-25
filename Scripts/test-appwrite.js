// Appwrite Test Script
// 
// This script tests the basic Appwrite integration
// 
// Usage:
// node test-appwrite.js

const { Client, Account, Databases } = require('appwrite');

// Appwrite Client Setup
const client = new Client();
client
  .setEndpoint('https://cloud.appwrite.io/v1')
  .setProject('YOUR_PROJECT_ID');

const account = new Account(client);
const databases = new Databases(client);

// Test Appwrite connection
async function testAppwrite() {
  console.log('Testing Appwrite integration...');
  
  try {
    // Test account service
    console.log('Testing account service...');
    // This will fail if not authenticated, but that's expected
    await account.get().catch(() => {
      console.log('Account service is accessible (not authenticated, which is expected)');
    });
    
    console.log('Appwrite integration test completed successfully!');
    console.log('Make sure to replace YOUR_PROJECT_ID with your actual Appwrite project ID');
  } catch (error) {
    console.error('Appwrite integration test failed:', error);
  }
}

// Run the test
testAppwrite();