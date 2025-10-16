#!/usr/bin/env node
// Script to help identify Appwrite version and provide specific instructions
const fs = require('fs');
const path = require('path');

console.log('🔍 Appwrite Version Identification Script\n');

console.log('This script will help you identify which version of Appwrite you\'re using');
console.log('and provide specific instructions for configuring redirect URLs.\n');

console.log('Please follow these steps:\n');

console.log('1. Log in to your Appwrite Console at https://cloud.appwrite.io/console');
console.log('2. Look at the bottom of the page for the version number');
console.log('3. The version will look something like "v1.2.3" or "0.15.4"');
console.log('4. Note whether it\'s a v1.x version or v0.x version\n');

console.log('Common version patterns:');
console.log('  - v1.x.x (newer versions)');
console.log('  - v0.x.x (older versions)');
console.log('  - 1.x.x (without "v" prefix)');
console.log('  - 0.x.x (without "v" prefix)\n');

console.log('Based on your version, follow these instructions:\n');

console.log('=== FOR APPWRITE v1.x (Newer Versions) ===');
console.log('1. Left sidebar → Authentication → Platforms');
console.log('2. Click "Add Platform"');
console.log('3. Select "Flutter/React Native"');
console.log('4. Fill in details:');
console.log('   - Platform Name: Ekehi Mobile App');
console.log('   - App ID/Bundle ID: com.ekehi.network');
console.log('5. Redirect URLs section should appear automatically');
console.log('6. Add:');
console.log('   ekehi://oauth/return');
console.log('   ekehi://auth\n');

console.log('=== FOR APPWRITE v0.x (Older Versions) ===');
console.log('1. Left sidebar → Settings → Platforms');
console.log('2. Click "+ New Platform"');
console.log('3. Select "Flutter/React Native" or "Mobile"');
console.log('4. Fill in details:');
console.log('   - Platform Name: Ekehi Mobile App');
console.log('   - App ID/Bundle ID: com.ekehi.network');
console.log('5. Scroll down to find Redirect URLs section');
console.log('6. Add:');
console.log('   ekehi://oauth/return');
console.log('   ekehi://auth\n');

console.log('=== IF YOU STILL CAN\'T FIND IT ===');
console.log('1. Check all menu items in the left sidebar:');
console.log('   - Users & Teams → Platforms');
console.log('   - Applications → Platforms');
console.log('   - Clients → Platforms');
console.log('   - Project Settings → Platforms');
console.log('2. Use the search function in Appwrite Console');
console.log('3. Search for "platform", "redirect", or "oauth"');
console.log('4. Try selecting different platform types if "Flutter/React Native" is not available\n');

console.log('=== ALTERNATIVE APPROACHES ===');
console.log('If standard platform configuration doesn\'t work:');
console.log('1. Check OAuth Providers section for redirect URL settings');
console.log('2. Look in Project Settings for global redirect configurations');
console.log('3. Try creating the platform without URLs first, then editing it\n');

console.log('For more detailed troubleshooting, see:');
console.log('- APPWRITE_REDIRECT_URLS_TROUBLESHOOTING.md');
console.log('- APPWRITE_ALTERNATIVE_REDIRECT_URL_CONFIG.md\n');

console.log('After configuring the redirect URLs, test with:');
console.log('npm run verify-redirect-urls');