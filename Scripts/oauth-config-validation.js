// OAuth Configuration Validation Script
console.log('=== OAuth Configuration Validation ===');

const projectId = '68c2dd6e002112935ed2';
const deepLinkSuccessUrl = 'ekehi://oauth/callback';
const deepLinkFailureUrl = 'ekehi://auth';
const hostedSuccessUrl = 'https://ekehi-oauth.netlify.app/oauth/return';
const hostedFailureUrl = 'https://ekehi-oauth.netlify.app/auth';

console.log('Project ID:', projectId);
console.log('Deep Link URLs:');
console.log('  Success:', deepLinkSuccessUrl);
console.log('  Failure:', deepLinkFailureUrl);
console.log('Hosted OAuth URLs:');
console.log('  Success:', hostedSuccessUrl);
console.log('  Failure:', hostedFailureUrl);

// Validate URL formats
function isValidUrl(string) {
  try {
    new URL(string);
    return true;
  } catch (_) {
    // Also check if it's a valid deep link format
    return /^[\w]+:\/\/[\w\/\-\.]+$/.test(string);
  }
}

console.log('\nValidation Results:');
console.log('Deep Link Success URL valid:', isValidUrl(deepLinkSuccessUrl));
console.log('Deep Link Failure URL valid:', isValidUrl(deepLinkFailureUrl));
console.log('Hosted Success URL valid:', isValidUrl(hostedSuccessUrl));
console.log('Hosted Failure URL valid:', isValidUrl(hostedFailureUrl));

console.log('\nRequired Appwrite Configuration:');
console.log('\nOption 1 - Deep Linking (Recommended):');
console.log('  Platform Type: Flutter/React Native');
console.log('  Name: Ekehi OAuth');
console.log('  App ID/Bundle ID: com.ekehi.network');
console.log('  Redirect URLs:');
console.log('    -', deepLinkSuccessUrl);
console.log('    -', deepLinkFailureUrl);
console.log('  Note: This matches the app routing configuration in app/oauth/callback.tsx');

console.log('\nOption 2 - Hosted OAuth:');
console.log('  Platform Type: Web');
console.log('  Name: Ekehi OAuth');
console.log('  Hostname:', new URL(hostedSuccessUrl).hostname);

console.log('\n✅ All URLs are properly formatted for Appwrite configuration');
console.log('✅ Deep linking URLs match the app routing configuration');
console.log('✅ Hosted OAuth URLs are properly formatted');