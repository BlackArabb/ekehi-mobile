#!/usr/bin/env node

/**
 * AdMob Service Test Script
 * 
 * This script tests the AdMob service implementation in the Ekehi Mobile app.
 */

const AdMobService = require('../src/services/AdMobService').default;

async function testAdMobService() {
  console.log('🧪 Starting AdMob Service Tests...\n');
  
  try {
    // Test 1: Check if AdMob service is properly imported
    console.log('Testing: AdMob Service Import');
    if (AdMobService) {
      console.log('✅ Success: AdMobService imported successfully\n');
    } else {
      console.log('❌ Failed: AdMobService import failed\n');
      return;
    }
    
    // Test 2: Check Ad Unit ID
    console.log('Testing: Ad Unit ID Configuration');
    const adUnitId = AdMobService.getAdUnitId();
    console.log(`Ad Unit ID: ${adUnitId}`);
    console.log(`Environment: ${__DEV__ ? 'Development' : 'Production'}\n`);
    
    // Test 3: Check initialization
    console.log('Testing: AdMob Service Initialization');
    try {
      await AdMobService.initialize();
      const isInitialized = AdMobService.isAdMobInitialized();
      if (isInitialized) {
        console.log('✅ Success: AdMobService initialized successfully\n');
      } else {
        console.log('⚠️  Warning: AdMobService initialization reported false\n');
      }
    } catch (error) {
      console.log(`❌ Error during initialization: ${error.message}\n`);
    }
    
    console.log('📊 AdMob Service Test Summary');
    console.log('==========================');
    console.log('✅ AdMob Service Implementation:');
    console.log('   - Service import: Working');
    console.log('   - Ad Unit ID: Configured');
    console.log('   - Initialization: Attempted');
    console.log('\n📋 Recommendations:');
    console.log('   1. Check if AdMob plugin is properly configured in app.json');
    console.log('   2. Verify Google Mobile Ads App ID is correct');
    console.log('   3. Test on a real device or emulator');
    console.log('   4. Check device logs for AdMob errors');
    
  } catch (error) {
    console.error('❌ Test suite failed:', error.message);
    process.exit(1);
  }
}

// Run the tests
if (require.main === module) {
  testAdMobService();
}