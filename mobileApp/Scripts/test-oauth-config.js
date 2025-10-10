#!/usr/bin/env node
// Script to test OAuth configuration
const fs = require('fs');
const path = require('path');

console.log('🔍 Testing OAuth Configuration...\n');

// Function to check app.json configuration
function checkAppJsonConfig() {
  console.log('📱 Checking app.json Configuration...\n');
  
  const appJsonPath = path.join(__dirname, '..', 'app.json');
  if (fs.existsSync(appJsonPath)) {
    const appJson = JSON.parse(fs.readFileSync(appJsonPath, 'utf8'));
    
    // Check scheme
    if (appJson.expo && appJson.expo.scheme) {
      console.log(`✅ App scheme: ${appJson.expo.scheme}`);
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
      }
    } else {
      console.log('❌ iOS configuration not found');
    }
  } else {
    console.log('❌ app.json not found');
  }
  
  console.log('');
}

// Function to check Appwrite configuration
function checkAppwriteConfig() {
  console.log('☁️  Checking Appwrite Configuration...\n');
  
  const appwriteConfigPath = path.join(__dirname, '..', 'src', 'config', 'appwrite.ts');
  if (fs.existsSync(appwriteConfigPath)) {
    const appwriteConfig = fs.readFileSync(appwriteConfigPath, 'utf8');
    
    // Check project ID
    const projectIdMatch = appwriteConfig.match(/PROJECT_ID\s*=\s*['"]([^'"]+)['"]/);
    if (projectIdMatch && projectIdMatch[1]) {
      console.log(`✅ Project ID: ${projectIdMatch[1]}`);
    } else {
      console.log('❌ Project ID not found');
    }
    
    // Check endpoint
    if (appwriteConfig.includes('fra.cloud.appwrite.io')) {
      console.log('✅ Using Frankfurt region endpoint');
    } else {
      console.log('❌ Frankfurt region endpoint not found');
    }
    
    // Check OAuth configuration
    if (appwriteConfig.includes('oauth:')) {
      console.log('✅ OAuth configuration section found');
      
      // Check Google OAuth
      if (appwriteConfig.includes('google:')) {
        console.log('✅ Google OAuth configuration found');
        
        // Check Client IDs
        const webClientIdMatch = appwriteConfig.match(/webClientId:\s*['"]([^'"]+)['"]/);
        const androidClientIdMatch = appwriteConfig.match(/androidClientId:\s*['"]([^'"]+)['"]/);
        const iosClientIdMatch = appwriteConfig.match(/iosClientId:\s*['"]([^'"]+)['"]/);
        
        if (webClientIdMatch && webClientIdMatch[1]) {
          console.log('   - Web Client ID: Present');
        } else {
          console.log('   - Web Client ID: Missing');
        }
        
        if (androidClientIdMatch && androidClientIdMatch[1]) {
          console.log('   - Android Client ID: Present');
        } else {
          console.log('   - Android Client ID: Missing');
        }
        
        if (iosClientIdMatch && iosClientIdMatch[1]) {
          console.log('   - iOS Client ID: Present');
        } else {
          console.log('   - iOS Client ID: Missing');
        }
      } else {
        console.log('❌ Google OAuth configuration missing');
      }
    } else {
      console.log('❌ OAuth configuration section missing');
    }
  } else {
    console.log('❌ Appwrite configuration file not found');
  }
  
  console.log('');
}

// Function to check AuthContext implementation
function checkAuthContext() {
  console.log('🔐 Checking AuthContext Implementation...\n');
  
  const authContextPath = path.join(__dirname, '..', 'src', 'contexts', 'AuthContext.tsx');
  if (fs.existsSync(authContextPath)) {
    const authContext = fs.readFileSync(authContextPath, 'utf8');
    
    // Check OAuth imports
    if (authContext.includes('OAuthProvider')) {
      console.log('✅ OAuthProvider import found');
    } else {
      console.log('❌ OAuthProvider import missing');
    }
    
    // Check signIn function
    if (authContext.includes('signIn')) {
      console.log('✅ signIn function found');
    } else {
      console.log('❌ signIn function missing');
    }
    
    // Check OAuth URLs
    if (authContext.includes('ekehi://oauth/return')) {
      console.log('✅ OAuth return URL found');
    } else {
      console.log('❌ OAuth return URL missing');
    }
    
    if (authContext.includes('ekehi://auth')) {
      console.log('✅ OAuth auth URL found');
    } else {
      console.log('❌ OAuth auth URL missing');
    }
    
    // Check WebBrowser usage
    if (authContext.includes('WebBrowser.openAuthSessionAsync')) {
      console.log('✅ WebBrowser OAuth session implementation found');
    } else {
      console.log('❌ WebBrowser OAuth session implementation missing');
    }
  } else {
    console.log('❌ AuthContext.tsx not found');
  }
  
  console.log('');
}

// Function to provide recommendations
function provideRecommendations() {
  console.log('📋 Recommendations:\n');
  
  console.log('1. Register Platforms in Appwrite Console:');
  console.log('   - Go to Appwrite Console → Auth → Settings → Platforms');
  console.log('   - Add Android, iOS, and Web platforms with correct configurations');
  console.log('   - Add redirect URLs: ekehi://oauth/return and ekehi://auth\n');
  
  console.log('2. Verify OAuth Provider Configuration:');
  console.log('   - In Appwrite Console → Auth → Settings → OAuth Providers');
  console.log('   - Enable Google OAuth provider');
  console.log('   - Verify Client IDs match those in appwrite.ts\n');
  
  console.log('3. Test Deep Linking:');
  console.log('   - Try opening these URLs in your mobile browser:');
  console.log('     * ekehi://oauth/return');
  console.log('     * ekehi://auth');
  console.log('     * ekehi://referral/ABC123');
  console.log('   - These should open your app if configured correctly\n');
  
  console.log('4. Clear Cache and Restart:');
  console.log('   - Run: npm start --reset-cache');
  console.log('   - This ensures configuration changes take effect\n');
}

// Run all checks
checkAppJsonConfig();
checkAppwriteConfig();
checkAuthContext();
provideRecommendations();

console.log('✅ OAuth Configuration Test Complete!');
console.log('\n📝 For detailed instructions, see: Documentations/APPWRITE_OAUTH_FIX.md');