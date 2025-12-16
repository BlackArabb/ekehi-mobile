// Script to check Telegram tasks for missing verificationData
const { Client, Databases, Query } = require('node-appwrite');

const APPWRITE_CONFIG = {
  endpoint: 'https://fra.cloud.appwrite.io/v1',
  projectId: '68c2dd6e002112935ed2',
  databaseId: '68c336e7000f87296feb',
  apiKey: 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d'
};

const client = new Client();
client
  .setEndpoint(APPWRITE_CONFIG.endpoint)
  .setProject(APPWRITE_CONFIG.projectId)
  .setKey(APPWRITE_CONFIG.apiKey);

const databases = new Databases(client);

async function checkTelegramTasks() {
  try {
    console.log('🔍 Checking Telegram tasks for missing verificationData...');
    
    // Find all Telegram tasks
    const response = await databases.listDocuments(
      APPWRITE_CONFIG.databaseId,
      'social_tasks', // social_tasks collection ID
      [
        Query.equal('platform', ['telegram'])
      ]
    );
    
    console.log(`Found ${response.total} Telegram tasks`);
    
    // Check each task for verificationData
    let missingCount = 0;
    response.documents.forEach((doc, index) => {
      console.log(`\nTask ${index + 1}: ${doc.name}`);
      console.log(`  ID: ${doc.$id}`);
      
      if (doc.verificationData) {
        console.log(`  ✅ verificationData:`, doc.verificationData);
      } else {
        console.log(`  ❌ Missing verificationData`);
        missingCount++;
      }
    });
    
    console.log(`\n📊 Summary: ${missingCount} out of ${response.total} Telegram tasks are missing verificationData`);
    
    // If there are tasks missing verificationData, let's see what the structure should be
    if (missingCount > 0) {
      console.log('\n🔧 Sample verificationData structure for Telegram tasks:');
      console.log('  {');
      console.log('    "channel_username": "@channel_name"');
      console.log('  }');
    }
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  }
}

checkTelegramTasks();