#!/usr/bin/env node
// Script to test OAuth flows
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

console.log('🔐 Testing OAuth Flows...\n');

// Function to check Appwrite OAuth configuration
function checkAppwriteOAuthConfig() {
  console.log('📋 Checking Appwrite OAuth Configuration...\n');
  
  // This would normally connect to Appwrite to check OAuth settings
  // For now, we'll just verify the configuration files exist
  const appwriteConfigPath = path.join(__dirname, '..', 'src', 'config', 'appwrite.ts');
  if (fs.existsSync(appwriteConfigPath)) {
    const configContent = fs.readFileSync(appwriteConfigPath, 'utf8');
    
    // Check for OAuth provider configuration
    if (configContent.includes('OAuthProvider')) {
      console.log('✅ OAuthProvider imported in Appwrite config');
    } else {
      console.log('⚠️  OAuthProvider not imported in Appwrite config');
    }
    
    // Check for Google OAuth configuration
    if (configContent.includes('google') && configContent.includes('oauth')) {
      console.log('✅ Google OAuth configuration found');
    } else {
      console.log('⚠️  Google OAuth configuration not found');
    }
  } else {
    console.log('❌ Appwrite configuration file not found');
  }
  
  console.log('');
}

// Function to check OAuth callback implementations
function checkOAuthCallbackImplementations() {
  console.log('🔄 Checking OAuth Callback Implementations...\n');
  
  // Check callback.tsx
  const callbackPath = path.join(__dirname, '..', 'app', 'oauth', 'callback.tsx');
  if (fs.existsSync(callbackPath)) {
    const callbackContent = fs.readFileSync(callbackPath, 'utf8');
    
    if (callbackContent.includes('createSession')) {
      console.log('✅ Session creation logic found in callback.tsx');
    } else {
      console.log('⚠️  Session creation logic missing in callback.tsx');
    }
    
    if (callbackContent.includes('checkAuthStatus')) {
      console.log('✅ Auth status checking logic found in callback.tsx');
    } else {
      console.log('⚠️  Auth status checking logic missing in callback.tsx');
    }
  } else {
    console.log('❌ callback.tsx not found');
  }
  
  // Check return.tsx
  const returnPath = path.join(__dirname, '..', 'app', 'oauth', 'return.tsx');
  if (fs.existsSync(returnPath)) {
    const returnContent = fs.readFileSync(returnPath, 'utf8');
    
    if (returnContent.includes('createSession')) {
      console.log('✅ Session creation logic found in return.tsx');
    } else {
      console.log('⚠️  Session creation logic missing in return.tsx');
    }
    
    if (returnContent.includes('maybeCompleteAuthSession')) {
      console.log('✅ Auth session completion logic found in return.tsx');
    } else {
      console.log('⚠️  Auth session completion logic missing in return.tsx');
    }
  } else {
    console.log('❌ return.tsx not found');
  }
  
  console.log('');
}

// Function to check AuthContext OAuth implementation
function checkAuthContextOAuth() {
  console.log('🔑 Checking AuthContext OAuth Implementation...\n');
  
  const authContextPath = path.join(__dirname, '..', 'src', 'contexts', 'AuthContext.tsx');
  if (fs.existsSync(authContextPath)) {
    const authContent = fs.readFileSync(authContextPath, 'utf8');
    
    if (authContent.includes('createOAuth2Token')) {
      console.log('✅ OAuth2 token creation logic found in AuthContext');
    } else {
      console.log('⚠️  OAuth2 token creation logic missing in AuthContext');
    }
    
    if (authContent.includes('OAuthProvider')) {
      console.log('✅ OAuthProvider usage found in AuthContext');
    } else {
      console.log('⚠️  OAuthProvider usage missing in AuthContext');
    }
    
    if (authContent.includes('signIn')) {
      console.log('✅ signIn method found in AuthContext');
    } else {
      console.log('⚠️  signIn method missing in AuthContext');
    }
    
    // Check for proper URL handling
    if (authContent.includes('successUrl') && authContent.includes('failureUrl')) {
      console.log('✅ Success and failure URLs configured in AuthContext');
    } else {
      console.log('⚠️  Success and failure URLs not properly configured in AuthContext');
    }
  } else {
    console.log('❌ AuthContext.tsx not found');
  }
  
  console.log('');
}

// Function to simulate OAuth flow (without actually executing it)
function simulateOAuthFlow() {
  console.log('🎮 Simulating OAuth Flow...\n');
  
  console.log('Step 1: User initiates Google sign in');
  console.log('  - App calls AuthContext.signIn()');
  console.log('  - AuthContext creates OAuth2 token with Appwrite');
  console.log('  - User is redirected to Google OAuth page\n');
  
  console.log('Step 2: User authenticates with Google');
  console.log('  - User enters credentials on Google page');
  console.log('  - Google redirects back to app with auth code\n');
  
  console.log('Step 3: App handles OAuth callback');
  console.log('  - OAuth callback page receives auth parameters');
  console.log('  - App creates session with Appwrite using auth code');
  console.log('  - App updates auth context with user data\n');
  
  console.log('Step 4: User is redirected to main app');
  console.log('  - User is navigated to main mining screen');
  console.log('  - App checks auth status to ensure session is valid\n');
  
  console.log('✅ OAuth flow simulation completed!\n');
}

// Function to check for common OAuth issues
function checkCommonOAuthIssues() {
  console.log('🔍 Checking for Common OAuth Issues...\n');
  
  // Check redirect URLs in app.json
  const appJsonPath = path.join(__dirname, '..', 'app.json');
  if (fs.existsSync(appJsonPath)) {
    const appJson = JSON.parse(fs.readFileSync(appJsonPath, 'utf8'));
    
    // Check if scheme matches OAuth redirect URLs
    const scheme = appJson.expo && appJson.expo.scheme ? appJson.expo.scheme : null;
    if (scheme) {
      console.log(`✅ App scheme is set to: ${scheme}`);
      
      // Check AuthContext for matching URLs
      const authContextPath = path.join(__dirname, '..', 'src', 'contexts', 'AuthContext.tsx');
      if (fs.existsSync(authContextPath)) {
        const authContent = fs.readFileSync(authContextPath, 'utf8');
        
        if (authContent.includes(`${scheme}://`)) {
          console.log(`✅ OAuth redirect URLs use correct scheme: ${scheme}`);
        } else {
          console.log(`⚠️  OAuth redirect URLs may not use correct scheme: ${scheme}`);
        }
      }
    } else {
      console.log('⚠️  App scheme not configured');
    }
  }
  
  console.log('');
}

// Function to provide testing recommendations
function provideTestingRecommendations() {
  console.log('📋 OAuth Testing Recommendations:\n');
  
  console.log('1. Manual Testing:');
  console.log('   - Test OAuth flow on each platform (Android, iOS, Web)');
  console.log('   - Verify redirect URLs work correctly');
  console.log('   - Check session persistence after app restart');
  console.log('   - Test sign out and sign back in');
  
  console.log('\n2. Appwrite Console Verification:');
  console.log('   - Verify Google OAuth client IDs in Appwrite Auth settings');
  console.log('   - Check that redirect URLs are registered in Appwrite');
  console.log('   - Confirm OAuth providers are enabled');
  
  console.log('\n3. Error Handling:');
  console.log('   - Test with invalid credentials');
  console.log('   - Test network interruption during OAuth flow');
  console.log('   - Verify error messages are user-friendly');
  
  console.log('\n4. Security Considerations:');
  console.log('   - Ensure OAuth client IDs are correct for each platform');
  console.log('   - Verify HTTPS is used in production');
  console.log('   - Check that sessions are properly invalidated on sign out');
  
  console.log('');
}

// Run all checks
checkAppwriteOAuthConfig();
checkOAuthCallbackImplementations();
checkAuthContextOAuth();
simulateOAuthFlow();
checkCommonOAuthIssues();
provideTestingRecommendations();

console.log('✅ OAuth Flow Testing Complete!');
console.log('\nTo test OAuth flows manually:');
console.log('1. Run the app: npm start');
console.log('2. Try signing in with Google');
console.log('3. Verify you are redirected correctly');
console.log('4. Check that you can access protected routes');
console.log('5. Test sign out functionality');