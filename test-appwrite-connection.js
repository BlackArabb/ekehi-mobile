const { Client, Databases } = require('node-appwrite');

// Initialize Appwrite client
const client = new Client();
client
  .setEndpoint('https://fra.cloud.appwrite.io/v1')
  .setProject('68c2dd6e002112935ed2')
  .setKey('standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d');

const databases = new Databases(client);

// Test connection
async function testConnection() {
  try {
    console.log('Testing Appwrite connection...');
    // Try to list databases to test connection
    const response = await databases.list();
    console.log('Appwrite connection successful!');
    console.log('Databases found:', response.total);
  } catch (error) {
    console.log('Connection error:', error.message);
    console.log('Error code:', error.code);
  }
}

testConnection();