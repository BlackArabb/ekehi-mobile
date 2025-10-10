#!/usr/bin/env node

// Script to test production build process
const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('🧪 Testing Production Build Process...\n');

// Function to run a command and capture output
function runCommand(command, description) {
  console.log(`🚀 ${description}`);
  try {
    const output = execSync(command, { cwd: path.join(__dirname, '..'), stdio: 'pipe' });
    console.log('  ✅ Success');
    return { success: true, output: output.toString() };
  } catch (error) {
    console.log('  ❌ Failed');
    console.log(`  Error: ${error.message}`);
    return { success: false, error: error.message };
  }
}

// Check if EAS CLI is installed
console.log('🔍 Checking EAS CLI Installation...');
const easCheck = runCommand('eas --version', 'Checking EAS CLI version');
if (!easCheck.success) {
  console.log('  ⚠️  EAS CLI not found. Installing...');
  const installEas = runCommand('npm install -g eas-cli', 'Installing EAS CLI globally');
  if (!installEas.success) {
    console.log('  ❌ Failed to install EAS CLI. Please install manually with: npm install -g eas-cli');
    process.exit(1);
  }
}

// Check if logged into EAS
console.log('\n🔐 Checking EAS Authentication...');
const authCheck = runCommand('eas whoami', 'Checking EAS authentication status');
if (!authCheck.success) {
  console.log('  ⚠️  Not logged into EAS. Please run: eas login');
  console.log('  For testing purposes, you can continue, but builds will fail without authentication.');
}

// Check project configuration
console.log('\n📋 Checking Project Configuration...');
const projectCheck = runCommand('eas project:info', 'Checking EAS project information');
if (!projectCheck.success) {
  console.log('  ⚠️  Project not configured with EAS. Run: eas build:configure');
}

// Test TypeScript compilation
console.log('\n📝 Testing TypeScript Compilation...');
const tsCheck = runCommand('npx tsc --noEmit', 'Checking TypeScript compilation');
if (tsCheck.success) {
  console.log('  ✅ TypeScript compilation successful');
} else {
  console.log('  ❌ TypeScript compilation failed');
  console.log('  Details:', tsCheck.error);
}

// Test bundle analysis (without actually building)
console.log('\n📦 Testing Bundle Analysis...');
const bundleCheck = runCommand('npx react-native bundle --platform android --dev false --entry-file node_modules/expo/AppEntry.js --bundle-output /tmp/bundle.js --sourcemap-output /tmp/bundle.map', 'Creating test bundle');
if (bundleCheck.success) {
  console.log('  ✅ Bundle creation successful');
  
  // Check bundle size
  try {
    const stats = fs.statSync('/tmp/bundle.js');
    console.log(`  📊 Bundle size: ${(stats.size / 1024 / 1024).toFixed(2)} MB`);
    
    if (stats.size > 10 * 1024 * 1024) {
      console.log('  ⚠️  Bundle is quite large (>10MB). Consider code splitting.');
    } else if (stats.size > 5 * 1024 * 1024) {
      console.log('  ℹ️  Bundle size is moderate (5-10MB).');
    } else {
      console.log('  ✅ Bundle size is good (<5MB).');
    }
  } catch (error) {
    console.log('  ⚠️  Could not check bundle size');
  }
} else {
  console.log('  ❌ Bundle creation failed');
  console.log('  Details:', bundleCheck.error);
}

// Test asset optimization
console.log('\n🖼️  Testing Asset Optimization...');
try {
  const assetsDir = path.join(__dirname, '..', 'assets');
  if (fs.existsSync(assetsDir)) {
    const files = fs.readdirSync(assetsDir);
    console.log(`  📁 Found ${files.length} assets in assets directory`);
    
    // Check for common image formats
    const imageFiles = files.filter(file => 
      file.endsWith('.png') || file.endsWith('.jpg') || file.endsWith('.jpeg') || file.endsWith('.gif')
    );
    
    if (imageFiles.length > 0) {
      console.log(`  🖼️  Found ${imageFiles.length} image assets`);
      
      // Check if sharp is available for image optimization
      try {
        require('sharp');
        console.log('  ✅ Sharp library available for image optimization');
      } catch (error) {
        console.log('  ⚠️  Sharp library not available. Image optimization may be limited.');
      }
    } else {
      console.log('  ⚠️  No image assets found');
    }
  } else {
    console.log('  ⚠️  Assets directory not found');
  }
} catch (error) {
  console.log('  ❌ Failed to check assets');
  console.log('  Error:', error.message);
}

// Test environment variables
console.log('\n🔑 Testing Environment Variables...');
const requiredEnvVars = [
  'APPWRITE_ENDPOINT',
  'APPWRITE_PROJECT_ID',
  'APPWRITE_DATABASE_ID'
];

let missingEnvVars = [];
requiredEnvVars.forEach(envVar => {
  if (!process.env[envVar]) {
    missingEnvVars.push(envVar);
  }
});

if (missingEnvVars.length === 0) {
  console.log('  ✅ All required environment variables are set');
} else {
  console.log('  ⚠️  Missing environment variables:');
  missingEnvVars.forEach(envVar => {
    console.log(`    - ${envVar}`);
  });
  console.log('  ℹ️  These are needed for Appwrite integration');
}

// Test Appwrite connection
console.log('\n🔗 Testing Appwrite Connection...');
try {
  // This would normally test the actual connection, but we'll just check if config exists
  const appwriteConfigPath = path.join(__dirname, '..', 'src', 'config', 'appwrite.ts');
  if (fs.existsSync(appwriteConfigPath)) {
    const appwriteConfig = fs.readFileSync(appwriteConfigPath, 'utf8');
    
    if (appwriteConfig.includes('PROJECT_ID') && appwriteConfig.includes('endpoint')) {
      console.log('  ✅ Appwrite configuration found');
      
      // Check if project ID looks valid (not the placeholder)
      if (appwriteConfig.includes('68c2dd6e002112935ed2')) {
        console.log('  ✅ Valid Appwrite project ID configured');
      } else {
        console.log('  ⚠️  Appwrite project ID may be placeholder value');
      }
    } else {
      console.log('  ❌ Incomplete Appwrite configuration');
    }
  } else {
    console.log('  ❌ Appwrite configuration file not found');
  }
} catch (error) {
  console.log('  ❌ Failed to check Appwrite configuration');
  console.log('  Error:', error.message);
}

// Summary
console.log('\n✅ Production Build Test Complete!');
console.log('\n📋 Next Steps:');
console.log('  1. If all checks passed, you can proceed with: eas build --profile production --platform android');
console.log('  2. For iOS builds: eas build --profile production --platform ios');
console.log('  3. For local testing: eas build --profile production --platform android --local');
console.log('  4. Ensure you have proper credentials configured in EAS');screenLeft