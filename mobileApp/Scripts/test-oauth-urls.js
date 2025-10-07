// Test OAuth URL generation
const { Client, Account, OAuthProvider } = require('appwrite');

// Initialize Appwrite client
const client = new Client();
client
  .setEndpoint('https://fra.cloud.appwrite.io/v1')
  .setProject('68c2dd6e002112935ed2');

const account = new Account(client);

// Test OAuth URL generation
function testOAuthURLs() {
  try {
    console.log('Testing OAuth URL generation...');
    
    const successUrl = 'https://ekehi-oauth.netlify.app/oauth/return';
    const failureUrl = 'https://ekehi-oauth.netlify.app/auth';
    
    console.log('Success URL:', successUrl);
    console.log('Failure URL:', failureUrl);
    
    // Generate OAuth URL
    const oauthUrl = account.createOAuth2Token(
      OAuthProvider.Google,
      successUrl,
      failureUrl
    );
    
    console.log('Generated OAuth URL:', oauthUrl);
    console.log('Type of OAuth URL:', typeof oauthUrl);
    
    if (oauthUrl && typeof oauthUrl === 'string') {
      console.log('✅ OAuth URL generation successful');
    } else {
      console.log('❌ OAuth URL generation failed');
    }
  } catch (error) {
    console.error('Error generating OAuth URL:', error);
  }
}

testOAuthURLs();