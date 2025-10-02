const { exec } = require('child_process');

console.log('Testing dependency installation...');

exec('npm install lodash', (error, stdout, stderr) => {
  if (error) {
    console.error(`Error: ${error}`);
    return;
  }
  if (stderr) {
    console.error(`Stderr: ${stderr}`);
    return;
  }
  console.log(`Stdout: ${stdout}`);
  console.log('Installation test completed');
});