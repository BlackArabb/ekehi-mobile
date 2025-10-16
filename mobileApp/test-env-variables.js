const fs = require('fs');
const path = require('path');

console.log('Testing Environment Variable Configuration...\n');

// Check if .env.production file exists
const envProdPath = path.join(__dirname, '.env.production');
if (fs.existsSync(envProdPath)) {
  console.log('✅ .env.production file exists');
  const envContent = fs.readFileSync(envProdPath, 'utf8');
  console.log('Contents:');
  console.log(envContent);
} else {
  console.log('❌ .env.production file not found');
}

// Check if .env file exists
const envPath = path.join(__dirname, '.env');
if (fs.existsSync(envPath)) {
  console.log('\n✅ .env file exists');
  const envContent = fs.readFileSync(envPath, 'utf8');
  console.log('Contents:');
  console.log(envContent);
} else {
  console.log('\nℹ️  .env file not found');
}

console.log('\n✅ Environment variable configuration check complete!');