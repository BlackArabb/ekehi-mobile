// Update User Social Tasks Collection Script
//
// This script updates the user_social_tasks collection to add the telegram_user_id attribute
// and prepares it for unique index creation.
//
// Usage:
// 1. Replace YOUR_PROJECT_ID and YOUR_API_KEY with your actual Appwrite credentials
// 2. Run: node update-user-social-tasks-collection.js

const { Client, Databases, Query } = require('node-appwrite');

// ⚠️ IMPORTANT: Replace with your actual Appwrite project ID and API Key
const APPWRITE_CONFIG = {
  endpoint: 'https://fra.cloud.appwrite.io/v1', // Frankfurt region endpoint
  projectId: '68c2dd6e002112935ed2', // Your actual project ID
  databaseId: '68c336e7000f87296feb', // Using your existing database ID
  apiKey: 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d' // Replace with your actual API key from Appwrite Console
};

// Appwrite Client Setup for server-side operations
const client = new Client();
client
  .setEndpoint(APPWRITE_CONFIG.endpoint)
  .setProject(APPWRITE_CONFIG.projectId)
  .setKey(APPWRITE_CONFIG.apiKey);

const databases = new Databases(client);

// Function to update the user_social_tasks collection
async function updateUserSocialTasksCollection() {
  console.log('🚀 Updating user_social_tasks collection...');
  console.log('=' .repeat(60));
  
  try {
    // Validate configuration
    if (APPWRITE_CONFIG.apiKey === 'YOUR_API_KEY_HERE') {
      console.error('❌ Error: Please replace YOUR_API_KEY_HERE with your actual Appwrite API key');
      console.log('🔑 To get your API key:');
      console.log('1. Go to https://cloud.appwrite.io/console');
      console.log('2. Select your project');
      console.log('3. Go to Settings → API Keys');
      console.log('4. Create a new API key with Database permissions');
      console.log('5. Copy the key and replace YOUR_API_KEY_HERE in this script');
      return;
    }
    
    console.log(`📡 Appwrite endpoint: ${APPWRITE_CONFIG.endpoint}`);
    console.log(`🎯 Project ID: ${APPWRITE_CONFIG.projectId}`);
    console.log(`🗄️  Database ID: ${APPWRITE_CONFIG.databaseId}`);
    
    // Check if the collection exists
    try {
      const collection = await databases.getCollection(APPWRITE_CONFIG.databaseId, 'user_social_tasks');
      console.log(`✅ Found collection: user_social_tasks`);
    } catch (error) {
      console.error('❌ Collection user_social_tasks not found:', error.message);
      console.log('💡 Make sure the collection exists before updating.');
      return;
    }
    
    // Add the telegram_user_id attribute
    console.log('\\n➕ Adding telegram_user_id attribute...');
    
    try {
      // Check if the attribute already exists
      try {
        await databases.getAttribute(APPWRITE_CONFIG.databaseId, 'user_social_tasks', 'telegram_user_id');
        console.log('  ⚠️  telegram_user_id attribute already exists, skipping creation...');
      } catch (attrError) {
        if (attrError.code === 404) {
          // Attribute doesn't exist, create it
          await databases.createIntegerAttribute(
            APPWRITE_CONFIG.databaseId,
            'user_social_tasks',
            'telegram_user_id',
            false, // not required
            null, // no min
            null, // no max
            null  // no default
          );
          console.log('  ✅ Created telegram_user_id attribute');
        } else {
          throw attrError;
        }
      }
    } catch (attrError) {
      console.log('  ⚠️  Could not create telegram_user_id attribute:', attrError.message);
      console.log('  💡 This might be because the attribute already exists or there was a network error.');
    }
    
    // Update existing documents to extract telegram_user_id from proofData
    console.log('\\n🔄 Updating existing documents to extract telegram_user_id...');
    
    try {
      // Get all documents that have proofData containing telegram_user_id
      const response = await databases.listDocuments(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        [
          Query.orderAsc('$createdAt')
        ]
      );
      
      console.log(`  📊 Found ${response.total} documents to process`);
      
      let updatedCount = 0;
      let skippedCount = 0;
      
      for (const doc of response.documents) {
        try {
          // Skip if telegram_user_id is already set
          if (doc.telegram_user_id) {
            skippedCount++;
            continue;
          }
          
          // Check if proofData exists and contains telegram_user_id
          if (doc.proofData) {
            let proofDataObj = {};
            
            // Parse proofData if it's a string
            if (typeof doc.proofData === 'string') {
              try {
                proofDataObj = JSON.parse(doc.proofData);
              } catch (parseError) {
                console.log(`    ⚠️  Could not parse proofData for document ${doc.$id}`);
                continue;
              }
            } else if (typeof doc.proofData === 'object') {
              proofDataObj = doc.proofData;
            }
            
            // If we found a telegram_user_id, update the document
            if (proofDataObj.telegram_user_id) {
              const telegramUserId = typeof proofDataObj.telegram_user_id === 'string' 
                ? parseInt(proofDataObj.telegram_user_id, 10) 
                : proofDataObj.telegram_user_id;
              
              if (!isNaN(telegramUserId)) {
                await databases.updateDocument(
                  APPWRITE_CONFIG.databaseId,
                  'user_social_tasks',
                  doc.$id,
                  {
                    ...doc.data,
                    telegram_user_id: telegramUserId
                  }
                );
                updatedCount++;
                console.log(`    ✅ Updated document ${doc.$id} with telegram_user_id: ${telegramUserId}`);
              }
            }
          }
        } catch (docError) {
          console.log(`    ❌ Error updating document ${doc.$id}:`, docError.message);
        }
        
        // Rate limiting to avoid hitting API limits
        await new Promise(resolve => setTimeout(resolve, 50));
      }
      
      console.log(`  🎉 Updated ${updatedCount} documents with extracted telegram_user_id`);
      console.log(`  ⏭️  Skipped ${skippedCount} documents (already had telegram_user_id)`);
      
    } catch (updateError) {
      console.error('❌ Error updating documents:', updateError.message);
    }
    
    console.log('\\n📝 Next Steps:');
    console.log('1. To enforce uniqueness, manually create a unique index in the Appwrite console:');
    console.log('   - Go to your Appwrite Console');
    console.log('   - Navigate to your database and user_social_tasks collection');
    console.log('   - Go to the Indexes tab');
    console.log('   - Create a new index with these settings:');
    console.log('     * Attributes: taskId (ascending), telegram_user_id (ascending)');
    console.log('     * Type: Unique');
    console.log('2. Update your application code to populate telegram_user_id directly');
    
    console.log('\\n' + '='.repeat(60));
    console.log('🎉 Collection Update Complete!');
    console.log('✅ The user_social_tasks collection has been prepared for Telegram ID uniqueness enforcement');
    
  } catch (error) {
    console.error('\\n❌ Update failed:', error.message);
    console.log('\\n🛠 Troubleshooting:');
    console.log('1. Check your Appwrite project ID and API key');
    console.log('2. Verify you have admin permissions');
    console.log('3. Ensure your internet connection is stable');
    console.log('4. Make sure the user_social_tasks collection exists');
  }
}

// Execute the update
if (require.main === module) {
  updateUserSocialTasksCollection().catch(console.error);
}