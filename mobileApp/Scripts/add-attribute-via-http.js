// Script to add telegram_user_id attribute via direct HTTP requests
const https = require('https');

const PROJECT_ID = '68c2dd6e002112935ed2';
const DATABASE_ID = '68c336e7000f87296feb';
const API_KEY = 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d';

// Function to make HTTP request to Appwrite
function appwriteRequest(path, method, data = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'fra.cloud.appwrite.io',
      port: 443,
      path: `/v1${path}`,
      method: method,
      headers: {
        'X-Appwrite-Response-Format': '1.0.0',
        'X-Appwrite-Project': PROJECT_ID,
        'X-Appwrite-Key': API_KEY,
        'Content-Type': 'application/json'
      }
    };

    if (data) {
      options.headers['Content-Length'] = JSON.stringify(data).length;
    }

    const req = https.request(options, (res) => {
      let responseData = '';
      
      res.on('data', (chunk) => {
        responseData += chunk;
      });
      
      res.on('end', () => {
        try {
          const json = JSON.parse(responseData);
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(json);
          } else {
            reject(new Error(`HTTP ${res.statusCode}: ${json.message || responseData}`));
          }
        } catch (error) {
          reject(new Error(`Failed to parse response: ${responseData}`));
        }
      });
    });

    req.on('error', (error) => {
      reject(error);
    });

    if (data) {
      req.write(JSON.stringify(data));
    }
    
    req.end();
  });
}

async function addTelegramUserIdAttribute() {
  console.log('🚀 Adding telegram_user_id attribute to user_social_tasks collection...');
  
  try {
    // First, check if the collection exists
    console.log('Checking if user_social_tasks collection exists...');
    const collections = await appwriteRequest(`/databases/${DATABASE_ID}/collections`, 'GET');
    
    const userSocialTasksCollection = collections.collections.find(c => c.$id === 'user_social_tasks');
    
    if (!userSocialTasksCollection) {
      console.error('❌ user_social_tasks collection not found');
      return;
    }
    
    console.log('✅ Found user_social_tasks collection');
    
    // Try to add the telegram_user_id attribute
    console.log('Adding telegram_user_id attribute...');
    
    const attributeData = {
      key: 'telegram_user_id',
      type: 'integer',
      required: false,
      min: null,
      max: null,
      default: null
    };
    
    try {
      const result = await appwriteRequest(
        `/databases/${DATABASE_ID}/collections/user_social_tasks/attributes/integer`,
        'POST',
        attributeData
      );
      
      console.log('✅ Successfully added telegram_user_id attribute');
      console.log('Attribute details:', JSON.stringify(result, null, 2));
      
    } catch (error) {
      if (error.message.includes('already exists')) {
        console.log('⚠️ telegram_user_id attribute already exists');
      } else {
        console.error('❌ Failed to add telegram_user_id attribute:', error.message);
        return;
      }
    }
    
    // Also add the status attribute if it doesn't exist
    console.log('Adding status attribute...');
    
    const statusAttributeData = {
      key: 'status',
      type: 'string',
      size: 50,
      required: false,
      default: null
    };
    
    try {
      const result = await appwriteRequest(
        `/databases/${DATABASE_ID}/collections/user_social_tasks/attributes/string`,
        'POST',
        statusAttributeData
      );
      
      console.log('✅ Successfully added status attribute');
      
    } catch (error) {
      if (error.message.includes('already exists')) {
        console.log('⚠️ status attribute already exists');
      } else {
        console.error('❌ Failed to add status attribute:', error.message);
      }
    }
    
    console.log('\n🎉 Attribute addition complete!');
    console.log('\n📝 Next steps:');
    console.log('1. Create a unique index in Appwrite Console:');
    console.log('   - Go to Database → user_social_tasks → Indexes');
    console.log('   - Add Index with attributes: taskId (asc), telegram_user_id (asc)');
    console.log('   - Set type to Unique');
    
  } catch (error) {
    console.error('❌ Failed to add attributes:', error.message);
  }
}

addTelegramUserIdAttribute().catch(console.error);