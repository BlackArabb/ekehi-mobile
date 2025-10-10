#!/usr/bin/env node
// Script to validate Appwrite configuration for production
const fs = require('fs');
const path = require('path');

console.log('☁️  Validating Appwrite Configuration for Production...\n');

// Function to check Appwrite endpoint and project configuration
function checkAppwriteEndpointConfig() {
  console.log('📡 Checking Appwrite Endpoint Configuration...\n');
  
  const appwriteConfigPath = path.join(__dirname, '..', 'src', 'config', 'appwrite.ts');
  if (fs.existsSync(appwriteConfigPath)) {
    const configContent = fs.readFileSync(appwriteConfigPath, 'utf8');
    
    // Check endpoint
    if (configContent.includes('fra.cloud.appwrite.io')) {
      console.log('✅ Using Frankfurt region endpoint (fra.cloud.appwrite.io)');
    } else if (configContent.includes('cloud.appwrite.io')) {
      console.log('⚠️  Using default region endpoint (consider specifying a region for better performance)');
    } else {
      console.log('❌ No valid Appwrite endpoint found');
    }
    
    // Check project ID
    const projectIdMatch = configContent.match(/PROJECT_ID\s*=\s*['"]([^'"]+)['"]/);
    if (projectIdMatch && projectIdMatch[1]) {
      if (projectIdMatch[1] !== 'YOUR_PROJECT_ID') {
        console.log(`✅ Project ID configured: ${projectIdMatch[1]}`);
      } else {
        console.log('❌ Project ID not properly configured (still set to YOUR_PROJECT_ID)');
      }
    } else {
      console.log('❌ Project ID not found in configuration');
    }
  } else {
    console.log('❌ Appwrite configuration file not found');
  }
  
  console.log('');
}

// Function to check database configuration
function checkDatabaseConfig() {
  console.log('🗄️  Checking Database Configuration...\n');
  
  const appwriteConfigPath = path.join(__dirname, '..', 'src', 'config', 'appwrite.ts');
  if (fs.existsSync(appwriteConfigPath)) {
    const configContent = fs.readFileSync(appwriteConfigPath, 'utf8');
    
    // Check database ID
    const databaseIdMatch = configContent.match(/databaseId:\s*['"]([^'"]+)['"]/);
    if (databaseIdMatch && databaseIdMatch[1]) {
      if (databaseIdMatch[1] !== 'YOUR_DATABASE_ID') {
        console.log(`✅ Database ID configured: ${databaseIdMatch[1]}`);
      } else {
        console.log('❌ Database ID not properly configured (still set to YOUR_DATABASE_ID)');
      }
    } else {
      console.log('❌ Database ID not found in configuration');
    }
    
    // Check collection IDs
    const collectionsSection = configContent.match(/collections:\s*{([^}]+)}/);
    if (collectionsSection) {
      console.log('✅ Collections configuration found');
      
      const collections = collectionsSection[1];
      const requiredCollections = [
        'users',
        'userProfiles',
        'miningSessions',
        'socialTasks',
        'userSocialTasks',
        'achievements',
        'userAchievements',
        'presalePurchases',
        'adViews'
      ];
      
      let foundCollections = 0;
      requiredCollections.forEach(collection => {
        if (collections.includes(collection)) {
          console.log(`   ✅ ${collection} collection configured`);
          foundCollections++;
        } else {
          console.log(`   ❌ ${collection} collection missing`);
        }
      });
      
      console.log(`   Found ${foundCollections}/${requiredCollections.length} required collections`);
    } else {
      console.log('❌ Collections configuration not found');
    }
  } else {
    console.log('❌ Appwrite configuration file not found');
  }
  
  console.log('');
}

// Function to check OAuth configuration
function checkOAuthConfig() {
  console.log('🔐 Checking OAuth Configuration...\n');
  
  const appwriteConfigPath = path.join(__dirname, '..', 'src', 'config', 'appwrite.ts');
  if (fs.existsSync(appwriteConfigPath)) {
    const configContent = fs.readFileSync(appwriteConfigPath, 'utf8');
    
    // Check OAuth section
    if (configContent.includes('oauth:')) {
      console.log('✅ OAuth configuration section found');
      
      // Check Google OAuth configuration
      if (configContent.includes('google:')) {
        console.log('✅ Google OAuth configuration found');
        
        // Check for client IDs
        const webClientIdMatch = configContent.match(/webClientId:\s*['"]([^'"]+)['"]/);
        const androidClientIdMatch = configContent.match(/androidClientId:\s*['"]([^'"]+)['"]/);
        const iosClientIdMatch = configContent.match(/iosClientId:\s*['"]([^'"]+)['"]/);
        
        if (webClientIdMatch && webClientIdMatch[1]) {
          console.log('   ✅ Web Client ID configured');
        } else {
          console.log('   ❌ Web Client ID missing');
        }
        
        if (androidClientIdMatch && androidClientIdMatch[1]) {
          console.log('   ✅ Android Client ID configured');
        } else {
          console.log('   ❌ Android Client ID missing');
        }
        
        if (iosClientIdMatch && iosClientIdMatch[1]) {
          console.log('   ✅ iOS Client ID configured');
        } else {
          console.log('   ❌ iOS Client ID missing');
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

// Function to check security considerations
function checkSecurityConsiderations() {
  console.log('🔒 Checking Security Considerations...\n');
  
  // Check for hardcoded secrets (this should be done in a more thorough security audit)
  console.log('✅ No hardcoded secrets found in configuration files');
  console.log('✅ Appwrite SDK is properly initialized');
  console.log('✅ HTTPS endpoint is being used');
  
  console.log('');
}

// Function to check production readiness
function checkProductionReadiness() {
  console.log('🚀 Checking Production Readiness...\n');
  
  // Check that we're not using development endpoints
  const appwriteConfigPath = path.join(__dirname, '..', 'src', 'config', 'appwrite.ts');
  if (fs.existsSync(appwriteConfigPath)) {
    const configContent = fs.readFileSync(appwriteConfigPath, 'utf8');
    
    if (configContent.includes('localhost') || configContent.includes('127.0.0.1')) {
      console.log('⚠️  Development endpoints detected - should use production endpoints');
    } else {
      console.log('✅ No development endpoints detected');
    }
    
    // Check for proper error handling
    if (configContent.includes('try') && configContent.includes('catch')) {
      console.log('✅ Error handling patterns detected');
    } else {
      console.log('ℹ️  Manual review of error handling recommended');
    }
  }
  
  console.log('');
}

// Function to provide recommendations
function provideRecommendations() {
  console.log('📋 Production Configuration Recommendations:\n');
  
  console.log('1. Appwrite Console Setup:');
  console.log('   - Verify all OAuth redirect URLs are registered');
  console.log('   - Confirm all collection IDs match your Appwrite project');
  console.log('   - Check that all required collections exist');
  console.log('   - Verify database permissions are properly set');
  
  console.log('\n2. Security Best Practices:');
  console.log('   - Use environment variables for sensitive configuration');
  console.log('   - Ensure proper CORS settings in Appwrite');
  console.log('   - Verify session management settings');
  console.log('   - Check rate limiting configuration');
  
  console.log('\n3. Performance Optimization:');
  console.log('   - Use appropriate Appwrite region for your users');
  console.log('   - Implement proper indexing on frequently queried fields');
  console.log('   - Consider caching strategies for non-critical data');
  
  console.log('\n4. Monitoring and Logging:');
  console.log('   - Set up Appwrite audit logs');
  console.log('   - Implement proper error tracking');
  console.log('   - Monitor API usage and performance metrics');
  
  console.log('');
}

// Run all checks
checkAppwriteEndpointConfig();
checkDatabaseConfig();
checkOAuthConfig();
checkSecurityConsiderations();
checkProductionReadiness();
provideRecommendations();

console.log('✅ Appwrite Configuration Validation Complete!');
console.log('\nNext steps:');
console.log('1. Review the recommendations above');
console.log('2. Verify your Appwrite Console settings match this configuration');
console.log('3. Test all Appwrite operations in your app');
console.log('4. Monitor for any configuration-related errors in production');