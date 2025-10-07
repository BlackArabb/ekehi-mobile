// Test script to verify OAuth URL generation with deep link URLs
const { Client, Account, OAuthProvider } = require('appwrite');

// Initialize Appwrite client
const client = new Client();
client
  .setEndpoint('https://fra.cloud.appwrite.io/v1')
  .setProject('68c2dd6e002112935ed2');

const account = new Account(client);

console.log('Testing OAuth URL generation with deep link URLs...');

try {
  // Test with deep linking URLs
  const successUrl = 'ekehi://oauth/return';
  const failureUrl = 'ekehi://auth';
  
  console.log('Creating OAuth URL with deep link URLs:');
  console.log('  Provider: Google');
  console.log('  Success URL:', successUrl);
  console.log('  Failure URL:', failureUrl);
  
  const oauthUrl = account.createOAuth2Token(
    OAuthProvider.Google,
    successUrl,
    failureUrl
  );
  
  console.log('Generated OAuth URL:', oauthUrl);
  
  // Validate the URL
  if (oauthUrl && typeof oauthUrl === 'string') {
    const url = new URL(oauthUrl);
    console.log('OAuth URL is valid');
    console.log('  Protocol:', url.protocol);
    console.log('  Hostname:', url.hostname);
    console.log('  Pathname:', url.pathname);
    
    // Check parameters
    const successParam = url.searchParams.get('success');
    const failureParam = url.searchParams.get('failure');
    console.log('  Success param:', successParam ? decodeURIComponent(successParam) : 'None');
    console.log('  Failure param:', failureParam ? decodeURIComponent(failureParam) : 'None');
    
    console.log('✅ OAuth URL generation successful with deep link URLs');
  } else {
    console.error('❌ Failed to generate OAuth URL');
  }
  
} catch (error) {
  console.error('Error generating OAuth URL:', error);
}

console.log('\nTesting hosted OAuth URL generation...');

try {
  // Test with hosted OAuth URLs
  const hostedSuccessUrl = 'https://ekehi-oauth.netlify.app/oauth/return';
  const hostedFailureUrl = 'https://ekehi-oauth.netlify.app/auth';
  
  console.log('Creating OAuth URL with hosted URLs:');
  console.log('  Provider: Google');
  console.log('  Success URL:', hostedSuccessUrl);
  console.log('  Failure URL:', hostedFailureUrl);
  
  const oauthUrl = account.createOAuth2Token(
    OAuthProvider.Google,
    hostedSuccessUrl,
    hostedFailureUrl
  );
  
  console.log('Generated hosted OAuth URL:', oauthUrl);
  
  // Validate the URL
  if (oauthUrl && typeof oauthUrl === 'string') {
    const url = new URL(oauthUrl);
    console.log('Hosted OAuth URL is valid');
    console.log('  Protocol:', url.protocol);
    console.log('  Hostname:', url.hostname);
    console.log('  Pathname:', url.pathname);
    
    console.log('✅ Hosted OAuth URL generation successful');
  } else {
    console.error('❌ Failed to generate hosted OAuth URL');
  }
  
} catch (error) {
  console.error('Error generating hosted OAuth URL:', error);
}