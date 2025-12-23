// Simple HTTP test to verify Appwrite connection
const https = require('https');

const options = {
  hostname: 'fra.cloud.appwrite.io',
  port: 443,
  path: '/v1/databases',
  method: 'GET',
  headers: {
    'X-Appwrite-Response-Format': '1.0.0',
    'X-Appwrite-Project': '68c2dd6e002112935ed2',
    'X-Appwrite-Key': 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d'
  }
};

console.log('Testing Appwrite connection via HTTP...');

const req = https.request(options, (res) => {
  console.log(`Status Code: ${res.statusCode}`);
  
  let data = '';
  
  res.on('data', (chunk) => {
    data += chunk;
  });
  
  res.on('end', () => {
    try {
      const jsonData = JSON.parse(data);
      console.log('Response:', JSON.stringify(jsonData, null, 2));
    } catch (error) {
      console.log('Raw response:', data);
    }
  });
});

req.on('error', (error) => {
  console.error('Request error:', error.message);
});

req.end();