// Script to check the detailed data of Telegram tasks
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

async function checkTelegramTaskDetails() {
  try {
    console.log('🔍 Checking detailed data of Telegram tasks...');
    
    // Find all Telegram tasks
    const response = await databases.listDocuments(
      APPWRITE_CONFIG.databaseId,
      'social_tasks', // social_tasks collection ID
      [
        Query.equal('platform', ['telegram'])
      ]
    );
    
    console.log(`Found ${response.total} Telegram tasks`);
    
    // Display detailed information for each task
    response.documents.forEach((doc, index) => {
      console.log(`\n=== Task ${index + 1} ===`);
      console.log(`ID: ${doc.$id}`);
      console.log(`Name: ${doc.name || 'N/A'}`);
      console.log(`Title: ${doc.title || 'N/A'}`);
      console.log(`Platform: ${doc.platform}`);
      console.log(`Verification Data Type: ${typeof doc.verificationData}`);
      console.log(`Verification Data:`, doc.verificationData);
      
      // Try to parse if it's a string
      if (typeof doc.verificationData === 'string') {
        try {
          const parsed = JSON.parse(doc.verificationData);
          console.log(`Parsed Verification Data:`, parsed);
        } catch (e) {
          console.log(`Could not parse verificationData as JSON: ${e.message}`);
        }
      }
      
      // Check if verificationData has channel_username
      if (doc.verificationData) {
        let verificationDataObj = doc.verificationData;
        if (typeof doc.verificationData === 'string') {
          try {
            verificationDataObj = JSON.parse(doc.verificationData);
          } catch (e) {
            console.log(`Invalid JSON in verificationData`);
          }
        }
        
        if (verificationDataObj && typeof verificationDataObj === 'object') {
          if (verificationDataObj.channel_username) {
            console.log(`✅ Channel Username: ${verificationDataObj.channel_username}`);
          } else {
            console.log(`❌ Missing channel_username in verificationData`);
          }
        }
      } else {
        console.log(`❌ No verificationData found`);
      }
    });
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  }
}

checkTelegramTaskDetails();