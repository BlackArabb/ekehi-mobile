// Test script to validate OAuth URLs
console.log('=== OAuth URLs Validation Test ===');

const successUrl = 'https://ekehi-oauth.netlify.app/oauth/return';
const failureUrl = 'https://ekehi-oauth.netlify.app/auth';

console.log('Testing OAuth URLs for Appwrite configuration:');
console.log('Success URL:', successUrl);
console.log('Failure URL:', failureUrl);

// Validate URL format
function isValidUrl(string) {
  try {
    new URL(string);
    return true;
  } catch (_) {
    return false;
  }
}

console.log('\nValidation Results:');
console.log('Success URL valid:', isValidUrl(successUrl));
console.log('Failure URL valid:', isValidUrl(failureUrl));

console.log('\nRequired Appwrite Configuration:');
console.log('- Platform Type: Web');
console.log('- Platform Name: Ekehi OAuth');
console.log('- Hostname:', new URL(successUrl).hostname);

console.log('\n✅ URLs are properly formatted for Appwrite Web Platform configuration');
console.log('Make sure to add this hostname in your Appwrite Console under Auth > Settings > Platforms');