const { Client, Account } = require('appwrite');

// Initialize Appwrite client
const client = new Client();
client
  .setEndpoint('https://fra.cloud.appwrite.io/v1')
  .setProject('68c2dd6e002112935ed2');

const account = new Account(client);

// Test connection
async function testConnection() {
  try {
    console.log('Testing Appwrite connection...');
    const session = await account.get();
    console.log('Current session:', session);
  } catch (error) {
    console.log('No active session or connection error:', error.message);
  }
}

testConnection();