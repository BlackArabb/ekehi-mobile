const { Client, Account } = require('node-appwrite');

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
    // Try to get project info
    const response = await client.call('get', '/health');
    console.log('Appwrite health check:', response);
  } catch (error) {
    console.log('Connection error:', error.message);
    console.log('Error code:', error.code);
    console.log('Error details:', error);
  }
}

testConnection();