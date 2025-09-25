// Simple test script to verify browser opening
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
  console.log('Decoded URL:');
  console.log('  Base:', 'https://fra.cloud.appwrite.io/v1/account/tokens/oauth2/google');
  console.log('  Success param:', decodeURIComponent(oauthUrl.split('success=')[1].split('&')[0]));
  console.log('  Failure param:', decodeURIComponent(oauthUrl.split('failure=')[1].split('&')[0]));
  
  // Test URL validation
  try {
    const url = new URL(oauthUrl);
    console.log('URL is valid');
    console.log('  Protocol:', url.protocol);
    console.log('  Hostname:', url.hostname);
    console.log('  Pathname:', url.pathname);
    console.log('  Search params:', url.search);
  } catch (urlError) {
    console.error('Invalid URL:', urlError);
  }
  
} catch (error) {
  console.error('Error generating OAuth URL:', error);
}