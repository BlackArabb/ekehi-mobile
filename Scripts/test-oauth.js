// Simple test script to verify OAuth URL generation
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
  
  // Test with hosted URLs
  const hostedSuccessUrl = 'https://ekehi-oauth.netlify.app/oauth/return';
  const hostedFailureUrl = 'https://ekehi-oauth.netlify.app/auth';
  
  console.log('\nCreating OAuth URL with hosted URLs:');
  console.log('  Provider: Google');
  console.log('  Success URL:', hostedSuccessUrl);
  console.log('  Failure URL:', hostedFailureUrl);
  
  const hostedOauthUrl = account.createOAuth2Token(
    OAuthProvider.Google,
    hostedSuccessUrl,
    hostedFailureUrl
  );
  
  console.log('Generated hosted OAuth URL:', hostedOauthUrl);
  
} catch (error) {
  console.error('Error generating OAuth URL:', error);
}