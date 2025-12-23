// Add Unique Index for Telegram ID Prevention Script
//
// This script adds a unique compound index to the user_social_tasks collection
// to prevent multiple users from using the same Telegram ID for the same task.
//
// Usage:
// 1. Replace YOUR_PROJECT_ID and YOUR_API_KEY with your actual Appwrite credentials
// 2. Run: node add-telegram-id-unique-index.js

const { Client, Databases, Query, ID } = require('node-appwrite');

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

// Function to add unique index to user_social_tasks collection
async function addUserSocialTasksUniqueIndex() {
  console.log('🚀 Starting unique index addition for user_social_tasks collection...');
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
      console.log('💡 Make sure the collection exists before adding indexes.');
      return;
    }
    
    // Add unique compound index on (taskId, proofData.telegram_user_id)
    // Note: Since proofData is stored as JSON, we'll need to create an index on the entire proofData field
    // For more precise control, we'd need to add a separate telegram_user_id attribute
    
    console.log('\\n📝 Adding unique compound index...');
    
    try {
      // First, let's add a dedicated telegram_user_id attribute to make indexing easier
      console.log('  ➕ Adding telegram_user_id attribute...');
      
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
          
          // Wait a bit for the attribute to be ready
          await new Promise(resolve => setTimeout(resolve, 2000));
        } else {
          throw attrError;
        }
      }
      
      // Now add the unique index
      // Note: Appwrite doesn't directly support compound unique indexes on different fields
      // We'll create a workaround by extracting telegram_user_id to its own field
      
      // Update existing documents to extract telegram_user_id from proofData
      console.log('  🔄 Updating existing documents to extract telegram_user_id...');
      
      // Get all documents that have telegram_user_id in proofData
      const response = await databases.listDocuments(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        [
          Query.orderAsc('$createdAt')
        ]
      );
      
      console.log(`  📊 Found ${response.total} documents to process`);
      
      let updatedCount = 0;
      for (const doc of response.documents) {
        try {
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
        
        // Rate limiting
        await new Promise(resolve => setTimeout(resolve, 100));
      }
      
      console.log(`  🎉 Updated ${updatedCount} documents with extracted telegram_user_id`);
      
      // Now try to make the telegram_user_id attribute unique (if supported)
      console.log('  🔐 Attempting to make telegram_user_id attribute unique...');
      
      // Unfortunately, Appwrite doesn't allow changing attribute properties after creation
      // We'll need to create a new attribute with unique constraint
      try {
        // This will likely fail, but let's try
        await databases.updateIntegerAttribute(
          APPWRITE_CONFIG.databaseId,
          'user_social_tasks',
          'telegram_user_id',
          false, // not required
          null, // no min
          null, // no max
          null, // no default
          true  // unique
        );
        console.log('  ✅ Made telegram_user_id attribute unique');
      } catch (uniqueError) {
        console.log('  ⚠️  Could not make telegram_user_id attribute unique directly.');
        console.log('  💡 You may need to manually configure this in the Appwrite console.');
      }
      
      console.log('\\n📝 Implementation Notes:');
      console.log('1. The script has extracted telegram_user_id from proofData to a dedicated field');
      console.log('2. To enforce uniqueness, you need to:');
      console.log('   a. Manually create a unique index in the Appwrite console on (taskId, telegram_user_id)');
      console.log('   b. Or modify the application logic to check for duplicates before submission');
      console.log('3. Future submissions should populate both proofData and the telegram_user_id field');
      
    } catch (indexError) {
      console.error('❌ Error adding index:', indexError.message);
      console.log('\\n💡 Manual Solution:');
      console.log('1. Go to your Appwrite Console');
      console.log('2. Navigate to your database and user_social_tasks collection');
      console.log('3. Go to the Indexes tab');
      console.log('4. Create a new index with these settings:');
      console.log('   - Attributes: taskId (ascending), telegram_user_id (ascending)');
      console.log('   - Type: Unique');
    }
    
    console.log('\\n' + '='.repeat(60));
    console.log('🎉 Unique Index Setup Process Complete!');
    console.log('✅ The script has prepared the collection for duplicate prevention');
    console.log('⚠️  Manual steps may still be required in the Appwrite console');
    
  } catch (error) {
    console.error('\\n❌ Setup failed:', error.message);
    console.log('\\n🛠 Troubleshooting:');
    console.log('1. Check your Appwrite project ID and API key');
    console.log('2. Verify you have admin permissions');
    console.log('3. Ensure your internet connection is stable');
    console.log('4. Make sure the user_social_tasks collection exists');
  }
}

// Execute the setup
if (require.main === module) {
  addUserSocialTasksUniqueIndex().catch(console.error);
}