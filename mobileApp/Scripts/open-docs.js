#!/usr/bin/env node

/**
 * Script to open OAuth documentation files
 * Helps developers quickly access the documentation needed to fix OAuth configuration issues
 */

const { exec } = require('child_process');
const path = require('path');
const fs = require('fs');

// Get the platform (Windows, macOS, Linux)
const platform = process.platform;

// Define documentation files to open
const docs = [
  'Documentations/APPWRITE_OAUTH_FIX.md',
  'Documentations/APPWRITE_MOBILE_PLATFORM_SETUP.md',
  'Documentations/OAUTH_SETUP_GUIDE.md'
];

console.log('🚀 Opening OAuth Documentation Files...\n');

// Function to open a file based on the platform
function openFile(filePath) {
  const fullPath = path.join(__dirname, '..', filePath);
  
  // Check if file exists
  if (!fs.existsSync(fullPath)) {
    console.error(`❌ File not found: ${fullPath}`);
    return;
  }
  
  let command;
  
  switch (platform) {
    case 'win32': // Windows
      command = `start "" "${fullPath}"`;
      break;
    case 'darwin': // macOS
      command = `open "${fullPath}"`;
      break;
    case 'linux': // Linux
      command = `xdg-open "${fullPath}"`;
      break;
    default:
      console.error(`Unsupported platform: ${platform}`);
      return;
  }
  
  exec(command, (error) => {
    if (error) {
      console.error(`❌ Failed to open ${filePath}:`, error.message);
    } else {
      console.log(`✅ Opened ${filePath}`);
    }
  });
}

// Open all documentation files
docs.forEach(doc => {
  openFile(doc);
});

console.log('\n📖 Documentation files are opening in your default markdown viewer...');
console.log('If they don\'t open, you can manually open these files:');
docs.forEach(doc => {
  console.log(`  - ${doc}`);
});