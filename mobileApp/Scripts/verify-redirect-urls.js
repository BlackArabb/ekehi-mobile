#!/usr/bin/env node
// Script to verify redirect URLs configuration
const fs = require('fs');
const path = require('path');

console.log('🔍 Verifying Redirect URLs Configuration...\n');

// Function to check app.json for correct scheme and intent filters
function checkAppJson() {
  console.log('📱 Checking app.json Configuration...\n');
  
  const appJsonPath = path.join(__dirname, '..', 'app.json');
  if (fs.existsSync(appJsonPath)) {
    const appJson = JSON.parse(fs.readFileSync(appJsonPath, 'utf8'));
    
    // Check scheme
    if (appJson.expo && appJson.expo.scheme) {
      console.log(`✅ App scheme: ${appJson.expo.scheme}`);
      if (appJson.expo.scheme === 'ekehi') {
        console.log('✅ App scheme is correctly configured as "ekehi"');
      } else {
        console.log('⚠️  App scheme is not "ekehi" - this may cause issues');
      }
    } else {
      console.log('❌ App scheme not found');
    }
    
    // Check Android intent filters
    if (appJson.expo && appJson.expo.android && appJson.expo.android.intentFilters) {
      console.log('✅ Android intent filters found');
      const intentFilters = appJson.expo.android.intentFilters;
      
      let hasBaseScheme = false;
      let hasReferralScheme = false;
      let hasOAuthReturnScheme = false;
      
      intentFilters.forEach((filter, index) => {
        if (filter.data) {
          filter.data.forEach(data => {
            if (data.scheme === 'ekehi' && !data.host) {
              hasBaseScheme = true;
              console.log('   - Base scheme: ekehi://');
            }
            if (data.scheme === 'ekehi' && data.host === 'referral') {
              hasReferralScheme = true;
              console.log('   - Referral scheme: ekehi://referral/*');
            }
            if (data.scheme === 'ekehi' && data.host === 'oauth' && data.pathPrefix === '/return') {
              hasOAuthReturnScheme = true;
              console.log('   - OAuth return scheme: ekehi://oauth/return');
            }
          });
        }
      });
      
      if (hasBaseScheme && hasReferralScheme && hasOAuthReturnScheme) {
        console.log('✅ All required intent filters are present');
      } else {
        console.log('⚠️  Some intent filters may be missing');
        if (!hasBaseScheme) console.log('   - Missing base scheme filter');
        if (!hasReferralScheme) console.log('   - Missing referral scheme filter');
        if (!hasOAuthReturnScheme) console.log('   - Missing OAuth return scheme filter');
      }
    } else {
      console.log('❌ Android intent filters not found');
    }
    
    // Check iOS configuration
    if (appJson.expo && appJson.expo.ios) {
      console.log('✅ iOS configuration present');
      if (appJson.expo.ios.bundleIdentifier) {
        console.log(`   - Bundle ID: ${appJson.expo.ios.bundleIdentifier}`);
        if (appJson.expo.ios.bundleIdentifier === 'com.ekehi.network') {
          console.log('   ✅ Bundle ID is correctly configured');
        } else {
          console.log('   ⚠️  Bundle ID is not "com.ekehi.network" - this may cause issues');
        }
      }
    } else {
      console.log('❌ iOS configuration not found');
    }
  } else {
    console.log('❌ app.json not found');
  }
  
  console.log('');
}

// Function to check AuthContext for correct OAuth URLs
function checkAuthContext() {
  console.log('🔐 Checking AuthContext Implementation...\n');
  
  const authContextPath = path.join(__dirname, '..', 'src', 'contexts', 'AuthContext.tsx');
  if (fs.existsSync(authContextPath)) {
    const authContext = fs.readFileSync(authContextPath, 'utf8');
    
    // Check OAuth URLs
    if (authContext.includes('ekehi://oauth/return')) {
      console.log('✅ OAuth return URL found: ekehi://oauth/return');
    } else {
      console.log('❌ OAuth return URL missing');
    }
    
    if (authContext.includes('ekehi://auth')) {
      console.log('✅ OAuth auth URL found: ekehi://auth');
    } else {
      console.log('❌ OAuth auth URL missing');
    }
  } else {
    console.log('❌ AuthContext.tsx not found');
  }
  
  console.log('');
}

// Function to provide Appwrite Console instructions
function provideAppwriteInstructions() {
  console.log('📋 Appwrite Console Configuration Instructions:\n');
  
  console.log('1. Log in to your Appwrite Console at https://cloud.appwrite.io/console');
  console.log('2. Select your project (ID: 68c2dd6e002112935ed2)');
  console.log('3. Navigate to one of these locations:');
  console.log('   - Left sidebar → "Authentication" → "Platforms"');
  console.log('   - Left sidebar → "Settings" → "Platforms"');
  console.log('   - Left sidebar → "Project Settings" → "Platforms"');
  console.log('4. Click "Add Platform" or "+ New Platform"');
  console.log('5. Select "Flutter/React Native" as the platform type');
  console.log('   ⚠️  IMPORTANT: Do NOT select separate Android and iOS platforms');
  console.log('6. Fill in the details:');
  console.log('   - Platform Name: Ekehi Mobile App');
  console.log('   - App ID/Bundle ID: com.ekehi.network');
  console.log('7. Look for the "Redirect URLs" section (this is what you\'re looking for!)');
  console.log('8. Add the following redirect URLs:');
  console.log('   ekehi://oauth/return');
  console.log('   ekehi://auth');
  console.log('9. Click "Register"');
  console.log('');
  console.log('🔍 TIP: The "Redirect URLs" section is usually a text area or list where you');
  console.log('        can add multiple URLs. Look for labels like:');
  console.log('        - "Redirect URLs"');
  console.log('        - "Allowed Redirect URLs"');
  console.log('        - "Valid Redirect URLs"');
  console.log('');
}

// Function to provide troubleshooting
function provideTroubleshooting() {
  console.log('❓ Troubleshooting: If You Can\'t Find the Redirect URLs Section\n');
  
  console.log('Common solutions:');
  console.log('1. Make sure you selected "Flutter/React Native" platform type first');
  console.log('2. Look for alternative navigation paths:');
  console.log('   - Search for "Platforms" in the Appwrite Console search bar');
  console.log('   - Check under "Users & Teams" → "Platforms"');
  console.log('   - Look for "Applications" or "Clients" sections');
  console.log('3. If only Android/iOS options are available, choose one and add the URLs');
  console.log('4. The redirect URLs field is typically below the platform details');
  console.log('');
  console.log('If you still can\'t find it:');
  console.log('1. Take a screenshot of what you see in the Appwrite Console');
  console.log('2. Check your Appwrite version in the console footer');
  console.log('3. Refer to the detailed navigation guide:');
  console.log('   - APPWRITE_CONSOLE_NAVIGATION_GUIDE.md');
  console.log('');
}

// Run all checks
checkAppJson();
checkAuthContext();
provideAppwriteInstructions();
provideTroubleshooting();

console.log('✅ Redirect URLs Verification Complete!');
console.log('\n📝 Next steps:');
console.log('1. Follow the Appwrite Console configuration instructions above');
console.log('2. Test the OAuth flow in your app');
console.log('3. If issues persist, check the detailed documentation:');
console.log('   - APPWRITE_CONSOLE_NAVIGATION_GUIDE.md');
console.log('   - APPWRITE_OAUTH_FIX.md');
console.log('   - APPWRITE_MOBILE_PLATFORM_SETUP.md');