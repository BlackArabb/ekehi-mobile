const { exec } = require('child_process');

console.log('Testing Start.io integration...');

// Run the React Native app in development mode to test Start.io
exec('npx react-native start', (error, stdout, stderr) => {
  if (error) {
    console.error(`Error starting React Native: ${error}`);
    return;
  }
  
  console.log('React Native started successfully');
  console.log('Please run the app on an Android device to test Start.io integration');
  console.log('Check the console logs for Start.io initialization messages');
});