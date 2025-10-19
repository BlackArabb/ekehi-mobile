const { exec } = require('child_process');

console.log('Testing Start.io integration...');

// First, make sure the package is installed
console.log('1. Checking if @kastorcode/expo-startio is installed...');
exec('npm list @kastorcode/expo-startio', (error, stdout, stderr) => {
  if (error) {
    console.error('Error checking package:', error);
    console.log('Please run: npm install @kastorcode/expo-startio');
    return;
  }
  
  console.log('Package is installed correctly');
  console.log(stdout);
  
  // Now test the app
  console.log('2. Starting the React Native packager...');
  console.log('Please run the app on an Android device to test Start.io integration');
  console.log('Check the console logs for Start.io initialization messages');
  console.log('\nTo test:');
  console.log('1. Connect an Android device or start an emulator');
  console.log('2. Run: npm run android');
  console.log('3. Watch the console logs for Start.io messages');
});