// Appwrite Configuration Validation Script
console.log('=== Appwrite OAuth Configuration Validation ===');

const projectId = '68c2dd6e002112935ed2';
const region = 'fra'; // Frankfurt region
const platformType = 'Web';
const hostname = 'ekehi-oauth.netlify.app';
const oauthProvider = 'Google';
const clientId = '842046112756-rk2jcdf9l7f4cbh136u4tm591qmtl6bq.apps.googleusercontent.com';

console.log('Required Appwrite Configuration:');
console.log('  Project ID:', projectId);
console.log('  Region:', region);
console.log('  Platform Type:', platformType);
console.log('  Hostname:', hostname);
console.log('  OAuth Provider:', oauthProvider);
console.log('  Client ID:', clientId);

// Calculate the required redirect URI for Google Cloud Console
const appwriteRedirectUri = `https://${region}.cloud.appwrite.io/v1/account/sessions/oauth2/callback/${oauthProvider.toLowerCase()}/${projectId}`;
console.log('\nRequired Redirect URI for Google Cloud Console:');
console.log('  Redirect URI:', appwriteRedirectUri);

console.log('\nConfiguration Steps:');
console.log('1. In Appwrite Console:');
console.log('   - Go to Auth > Settings');
console.log('   - Add Platform > Web');
console.log('   - Name: Ekehi OAuth');
console.log('   - Hostname:', hostname);
console.log('   - Click Register');

console.log('\n2. In Google Cloud Console:');
console.log('   - Go to APIs & Services > Credentials');
console.log('   - Edit your OAuth 2.0 Client ID');
console.log('   - Add the following Authorized Redirect URI:');
console.log('   -', appwriteRedirectUri);

console.log('\n3. In Appwrite Console OAuth Provider Settings:');
console.log('   - Go to Auth > Google');
console.log('   - Enable the provider');
console.log('   - Enter Client ID:', clientId);
console.log('   - Enter Client Secret: [YOUR_SECRET]');
console.log('   - Click Update');

console.log('\n✅ After completing these steps, restart your app');
console.log('✅ The "invalid URL" error should be resolved');
console.log('✅ OAuth flow should work correctly');