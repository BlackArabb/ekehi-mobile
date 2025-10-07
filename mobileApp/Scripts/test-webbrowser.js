// Simple test script to verify WebBrowser functionality
const { Client, Account, OAuthProvider } = require('appwrite');

// Initialize Appwrite client
const client = new Client();
client
  .setEndpoint('https://fra.cloud.appwrite.io/v1')
  .setProject('68c2dd6e002112935ed2');

const account = new Account(client);

// Test OAuth URL generation
console.log('Testing OAuth URL generation...');

try {
  // Test with a simple URL first
  const testUrl = 'https://www.google.com';
  console.log('Testing with simple URL:', testUrl);
  
  // Test with deep linking URLs
  const successUrl = 'ekehi://oauth/callback';
  const failureUrl = 'ekehi://auth';
  
  console.log('Creating OAuth URL with:');
  console.log('  Provider: Google');
  console.log('  Success URL:', successUrl);
  console.log('  Failure URL:', failureUrl);
  
  const oauthUrl = account.createOAuth2Token(
    OAuthProvider.Google,
    successUrl,
    failureUrl
  );
  
  console.log('Generated OAuth URL:', oauthUrl);
  
  // Parse the URL to check parameters
  const url = new URL(oauthUrl);
  console.log('URL components:');
  console.log('  Protocol:', url.protocol);
  console.log('  Hostname:', url.hostname);
  console.log('  Pathname:', url.pathname);
  
  // Check search parameters
  for (const [key, value] of url.searchParams) {
    console.log(`  ${key}: ${value}`);
    if (key === 'success' || key === 'failure') {
      console.log(`    Decoded ${key}:`, decodeURIComponent(value));
    }
  }
  
} catch (error) {
  console.error('Error:', error);
}