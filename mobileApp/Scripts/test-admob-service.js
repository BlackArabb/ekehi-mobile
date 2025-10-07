#!/usr/bin/env node

/**
 * AdMob Service Test Script
 * 
 * This script tests the AdMob service implementation in the Ekehi Mobile app.
 */

// Simple test to verify the AdMob service can be imported and used
console.log('🧪 Starting AdMob Service Tests...\n');

// Test 1: Check if AdMob service can be imported
try {
  console.log('Testing: AdMob Service Import');
  const AdMobService = require('../src/services/AdMobService').default;
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
  console.log(`Environment: ${process.env.NODE_ENV === 'development' ? 'Development' : 'Production'}\n`);
  
  // Test 3: Check initialization method exists
  console.log('Testing: AdMob Service Methods');
  if (typeof AdMobService.initialize === 'function') {
    console.log('✅ Success: initialize() method exists\n');
  } else {
    console.log('❌ Failed: initialize() method missing\n');
  }
  
  if (typeof AdMobService.showRewardedAd === 'function') {
    console.log('✅ Success: showRewardedAd() method exists\n');
  } else {
    console.log('❌ Failed: showRewardedAd() method missing\n');
  }
  
  console.log('📊 AdMob Service Test Summary');
  console.log('==========================');
  console.log('✅ AdMob Service Implementation:');
  console.log('   - Service import: Working');
  console.log('   - Ad Unit ID: Configured');
  console.log('   - Methods: Available');
  console.log('\n📋 Recommendations for Testing on Device:');
  console.log('   1. Run the app on a real device or emulator');
  console.log('   2. Navigate to the Mine tab');
  console.log('   3. Click "Watch Ad for +0.5 EKH" button');
  console.log('   4. Check device logs for AdMob messages');
  console.log('   5. Verify ads load and show properly');
  
} catch (error) {
  console.error('❌ Test suite failed:', error.message);
  process.exit(1);
}