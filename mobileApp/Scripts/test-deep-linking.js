#!/usr/bin/env node
// Script to test deep linking and OAuth flows
const fs = require('fs');
const path = require('path');

console.log('🔍 Testing Deep Linking and OAuth Flows...\n');

// Function to check deep linking configuration
function checkDeepLinkingConfig() {
  console.log('🔗 Checking Deep Linking Configuration...\n');
  
  // Check app.json for scheme configuration
  const appJsonPath = path.join(__dirname, '..', 'app.json');
  if (fs.existsSync(appJsonPath)) {
    const appJson = JSON.parse(fs.readFileSync(appJsonPath, 'utf8'));
    
    if (appJson.expo && appJson.expo.scheme) {
      console.log(`✅ App scheme configured: ${appJson.expo.scheme}`);
      
      // Check Android intent filters
      if (appJson.expo.android && appJson.expo.android.intentFilters) {
        console.log('✅ Android intent filters configured');
        appJson.expo.android.intentFilters.forEach((filter, index) => {
          console.log(`   Filter ${index + 1}:`);
          if (filter.data) {
            filter.data.forEach(data => {
              console.log(`     - Scheme: ${data.scheme || 'N/A'}`);
              if (data.host) console.log(`     - Host: ${data.host}`);
              if (data.pathPrefix) console.log(`     - Path Prefix: ${data.pathPrefix}`);
            });
          }
        });
      } else {
        console.log('⚠️  Android intent filters not configured');
      }
      
      // Check iOS configuration
      if (appJson.expo.ios) {
        console.log('✅ iOS configuration present');
      } else {
        console.log('⚠️  iOS configuration missing');
      }
    } else {
      console.log('❌ App scheme not configured');
    }
  } else {
    console.log('❌ app.json not found');
  }
  
  console.log('');
}

// Function to check OAuth configuration
function checkOAuthConfig() {
  console.log('🔐 Checking OAuth Configuration...\n');
  
  // Check Appwrite config for OAuth settings
  const appwriteConfigPath = path.join(__dirname, '..', 'src', 'config', 'appwrite.ts');
  if (fs.existsSync(appwriteConfigPath)) {
    const appwriteConfig = fs.readFileSync(appwriteConfigPath, 'utf8');
    
    // Check for Google OAuth client IDs
    if (appwriteConfig.includes('webClientId') && 
        appwriteConfig.includes('androidClientId') && 
        appwriteConfig.includes('iosClientId')) {
      console.log('✅ Google OAuth client IDs configured for all platforms');
      
      // Check for actual client ID values (basic validation)
      const clientIdRegex = /[a-zA-Z0-9\-_]+\.apps\.googleusercontent\.com/g;
      const matches = appwriteConfig.match(clientIdRegex);
      if (matches && matches.length >= 3) {
        console.log(`✅ Found ${matches.length} Google OAuth client IDs`);
        matches.forEach((clientId, index) => {
          console.log(`   ${index + 1}. ${clientId}`);
        });
      } else {
        console.log('⚠️  Google OAuth client IDs may be incomplete');
      }
    } else {
      console.log('❌ Google OAuth client IDs not properly configured');
    }
    
    // Check for PROJECT_ID
    if (appwriteConfig.includes('PROJECT_ID')) {
      const projectIdMatch = appwriteConfig.match(/PROJECT_ID\s*=\s*['"]([^'"]+)['"]/);
      if (projectIdMatch && projectIdMatch[1] !== 'YOUR_PROJECT_ID') {
        console.log(`✅ Appwrite Project ID configured: ${projectIdMatch[1].substring(0, 8)}...`);
      } else {
        console.log('⚠️  Appwrite Project ID not properly configured');
      }
    } else {
      console.log('❌ Appwrite Project ID not found');
    }
  } else {
    console.log('❌ Appwrite configuration file not found');
  }
  
  console.log('');
}

// Function to check OAuth callback files
function checkOAuthCallbacks() {
  console.log('🔄 Checking OAuth Callback Files...\n');
  
  const oauthDir = path.join(__dirname, '..', 'app', 'oauth');
  if (fs.existsSync(oauthDir)) {
    const files = fs.readdirSync(oauthDir);
    console.log(`✅ OAuth directory found with ${files.length} files:`);
    files.forEach(file => {
      console.log(`   - ${file}`);
    });
  } else {
    console.log('❌ OAuth directory not found');
  }
  
  console.log('');
}

// Function to check referral handling
function checkReferralHandling() {
  console.log('👥 Checking Referral Handling...\n');
  
  const referralDir = path.join(__dirname, '..', 'app', 'referral');
  if (fs.existsSync(referralDir)) {
    const files = fs.readdirSync(referralDir);
    console.log(`✅ Referral directory found with ${files.length} files:`);
    files.forEach(file => {
      console.log(`   - ${file}`);
    });
  } else {
    console.log('❌ Referral directory not found');
  }
  
  console.log('');
}

// Function to generate test URLs
function generateTestUrls() {
  console.log('🧪 Generating Test URLs...\n');
  
  const appJsonPath = path.join(__dirname, '..', 'app.json');
  if (fs.existsSync(appJsonPath)) {
    const appJson = JSON.parse(fs.readFileSync(appJsonPath, 'utf8'));
    const scheme = appJson.expo && appJson.expo.scheme ? appJson.expo.scheme : 'yourapp';
    
    console.log('Use these URLs to test deep linking:');
    console.log(`   1. Main app: ${scheme}://`);
    console.log(`   2. Referral link: ${scheme}://referral/ABC123`);
    console.log(`   3. OAuth return: ${scheme}://oauth/return`);
    console.log(`   4. OAuth callback: ${scheme}://oauth/callback`);
    console.log('');
    console.log('For web testing, use:');
    console.log(`   1. Main app: http://localhost:8081/`);
    console.log(`   2. Referral link: http://localhost:8081/referral/ABC123`);
    console.log(`   3. OAuth return: http://localhost:8081/oauth/return`);
    console.log('');
  }
}

// Run all checks
checkDeepLinkingConfig();
checkOAuthConfig();
checkOAuthCallbacks();
checkReferralHandling();
generateTestUrls();

console.log('✅ Deep Linking and OAuth Flow Verification Complete!');
console.log('\nNext steps:');
console.log('1. Test the URLs generated above on your device');
console.log('2. Verify OAuth redirects work in Appwrite Console');
console.log('3. Test referral code handling');
console.log('4. Validate sign in/out flows across platforms');