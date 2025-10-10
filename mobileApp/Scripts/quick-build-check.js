#!/usr/bin/env node

// Quick build configuration check
const fs = require('fs');
const path = require('path');

console.log('🚀 Quick Production Build Configuration Check\n');

// Check EAS config
const easConfigPath = path.join(__dirname, '..', 'eas.json');
if (fs.existsSync(easConfigPath)) {
  const easConfig = JSON.parse(fs.readFileSync(easConfigPath, 'utf8'));
  if (easConfig.build && easConfig.build.production) {
    console.log('✅ EAS production configuration: OK');
  } else {
    console.log('❌ EAS production configuration: Missing');
  }
} else {
  console.log('❌ EAS configuration file not found');
}

// Check app config
const appConfigPath = path.join(__dirname, '..', 'app.json');
if (fs.existsSync(appConfigPath)) {
  const appConfig = JSON.parse(fs.readFileSync(appConfigPath, 'utf8'));
  if (appConfig.expo) {
    console.log('✅ App configuration: OK');
  } else {
    console.log('❌ App configuration: Invalid');
  }
} else {
  console.log('❌ App configuration file not found');
}

// Check package.json scripts
const packageJsonPath = path.join(__dirname, '..', 'package.json');
if (fs.existsSync(packageJsonPath)) {
  const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
  if (packageJson.scripts && packageJson.scripts['build:android'] && packageJson.scripts['build:ios']) {
    console.log('✅ Build scripts: OK');
  } else {
    console.log('❌ Build scripts: Missing');
  }
} else {
  console.log('❌ package.json not found');
}

// Check Appwrite config
const appwriteConfigPath = path.join(__dirname, '..', 'src', 'config', 'appwrite.ts');
if (fs.existsSync(appwriteConfigPath)) {
  const appwriteConfig = fs.readFileSync(appwriteConfigPath, 'utf8');
  if (appwriteConfig.includes('PROJECT_ID') && appwriteConfig.includes('endpoint')) {
    console.log('✅ Appwrite configuration: OK');
  } else {
    console.log('❌ Appwrite configuration: Incomplete');
  }
} else {
  console.log('❌ Appwrite configuration file not found');
}

console.log('\n✅ Quick build check completed!');