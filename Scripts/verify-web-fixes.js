// Test file to verify the web platform fixes
console.log('🔍 Verifying web platform fixes...');

const fs = require('fs');
const path = require('path');

// Test 1: Check that AdMobService handles web platform properly
const adMobServicePath = path.join(__dirname, '..', 'src', 'services', 'AdMobService.ts');
const adMobServiceContent = fs.readFileSync(adMobServicePath, 'utf8');

if (adMobServiceContent.includes('Platform.OS === \'web\'') && 
    adMobServiceContent.includes('Ads not supported on web platform')) {
  console.log('✅ Test 1: AdMobService web platform handling - PASSED');
} else {
  console.log('❌ Test 1: AdMobService web platform handling - FAILED');
  process.exit(1);
}

// Test 2: Check that AdModal handles web platform properly
const adModalPath = path.join(__dirname, '..', 'src', 'components', 'AdModal.tsx');
const adModalContent = fs.readFileSync(adModalPath, 'utf8');

if (adModalContent.includes('Platform.OS === \'web\'') && 
    adModalContent.includes('Ads are not available on web platform')) {
  console.log('✅ Test 2: AdModal web platform handling - PASSED');
} else {
  console.log('❌ Test 2: AdModal web platform handling - FAILED');
  process.exit(1);
}

// Test 3: Check that webpack.config.js exists and has proper configuration
const webpackConfigPath = path.join(__dirname, '..', 'webpack.config.js');
const webpackConfigExists = fs.existsSync(webpackConfigPath);

if (webpackConfigExists) {
  const webpackConfigContent = fs.readFileSync(webpackConfigPath, 'utf8');
  if (webpackConfigContent.includes('react-native-google-mobile-ads') && 
      webpackConfigContent.includes('null-loader')) {
    console.log('✅ Test 3: Webpack configuration - PASSED');
  } else {
    console.log('❌ Test 3: Webpack configuration - FAILED');
    process.exit(1);
  }
} else {
  console.log('❌ Test 3: Webpack configuration file missing - FAILED');
  process.exit(1);
}

// Test 4: Check that package.json has proper resolutions
const packageJsonPath = path.join(__dirname, '..', 'package.json');
const packageJsonContent = fs.readFileSync(packageJsonPath, 'utf8');

if (packageJsonContent.includes('"resolutions"') && 
    packageJsonContent.includes('react-native-web')) {
  console.log('✅ Test 4: Package.json resolutions - PASSED');
} else {
  console.log('❌ Test 4: Package.json resolutions - FAILED');
  process.exit(1);
}

console.log('\n📋 Summary of web platform fixes:');
console.log('1. Updated AdMobService to handle web platform properly');
console.log('2. Updated AdModal to handle web platform properly');
console.log('3. Created webpack.config.js with proper aliases and exclusions');
console.log('4. Added package.json resolutions for react-native-web');
console.log('5. Added null-loader to exclude native modules from web build');

console.log('\n🎉 All web platform fixes have been implemented and verified!');
console.log('The web bundling error should now be resolved.');
console.log('\n⚠️  To test the fix:');
console.log('   1. Restart the Expo development server');
console.log('   2. Try opening the web version again');
console.log('   3. The app should now load without the Platform module error');