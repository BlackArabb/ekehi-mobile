// Script to add missing attributes to existing user_social_tasks collection
const { Client, Databases } = require('node-appwrite');

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

async function addMissingAttributes() {
  console.log('🚀 Adding missing attributes to user_social_tasks collection...');
  
  try {
    // Check if collection exists
    try {
      await databases.getCollection(APPWRITE_CONFIG.databaseId, 'user_social_tasks');
      console.log('✅ Found user_social_tasks collection');
    } catch (error) {
      console.error('❌ Collection user_social_tasks not found');
      return;
    }
    
    // Add missing attributes one by one
    console.log('Adding attributes...');
    
    // telegram_user_id attribute (the main one we need)
    try {
      await databases.createIntegerAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'telegram_user_id',
        false // not required
      );
      console.log('✅ Added telegram_user_id attribute');
    } catch (error) {
      if (error.message.includes('already exists')) {
        console.log('⚠️ telegram_user_id attribute already exists');
      } else {
        console.error('❌ Failed to add telegram_user_id attribute:', error.message);
      }
    }
    
    // status attribute (if not exists)
    try {
      await databases.createStringAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'status',
        50,
        false // not required
      );
      console.log('✅ Added status attribute');
    } catch (error) {
      if (error.message.includes('already exists')) {
        console.log('⚠️ status attribute already exists');
      } else {
        console.error('❌ Failed to add status attribute:', error.message);
      }
    }
    
    // verificationAttempts attribute (if not exists)
    try {
      await databases.createIntegerAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'verificationAttempts',
        false, // not required
        0, // min
        null, // no max
        0 // default
      );
      console.log('✅ Added verificationAttempts attribute');
    } catch (error) {
      if (error.message.includes('already exists')) {
        console.log('⚠️ verificationAttempts attribute already exists');
      } else {
        console.error('❌ Failed to add verificationAttempts attribute:', error.message);
      }
    }
    
    // verifiedAt attribute (if not exists)
    try {
      await databases.createDatetimeAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'verifiedAt',
        false // not required
      );
      console.log('✅ Added verifiedAt attribute');
    } catch (error) {
      if (error.message.includes('already exists')) {
        console.log('⚠️ verifiedAt attribute already exists');
      } else {
        console.error('❌ Failed to add verifiedAt attribute:', error.message);
      }
    }
    
    // rejectionReason attribute (if not exists)
    try {
      await databases.createStringAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'rejectionReason',
        1000,
        false // not required
      );
      console.log('✅ Added rejectionReason attribute');
    } catch (error) {
      if (error.message.includes('already exists')) {
        console.log('⚠️ rejectionReason attribute already exists');
      } else {
        console.error('❌ Failed to add rejectionReason attribute:', error.message);
      }
    }
    
    console.log('\n🎉 All missing attributes processed!');
    
    // Now run the data migration
    console.log('\n🔄 Starting data migration for existing documents...');
    await migrateExistingData();
    
  } catch (error) {
    console.error('❌ Failed to add attributes:', error.message);
  }
}

async function migrateExistingData() {
  try {
    console.log('🔍 Fetching documents that need migration...');
    
    // Get all documents
    const response = await databases.listDocuments(
      APPWRITE_CONFIG.databaseId,
      'user_social_tasks'
    );
    
    console.log(`📊 Found ${response.total} documents to process`);
    
    let migratedCount = 0;
    let errorCount = 0;
    
    for (const doc of response.documents) {
      try {
        // Check if document needs migration (missing telegram_user_id but has proofData with telegram_user_id)
        if (!doc.telegram_user_id && doc.proofData) {
          let proofDataObj = {};
          
          // Parse proofData if it's a string
          if (typeof doc.proofData === 'string') {
            try {
              proofDataObj = JSON.parse(doc.proofData);
            } catch (parseError) {
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
              migratedCount++;
              
              // Show progress every 10 updates
              if (migratedCount % 10 === 0) {
                console.log(`   Migrated ${migratedCount} documents...`);
              }
            }
          }
        }
      } catch (docError) {
        errorCount++;
        if (errorCount <= 5) { // Only show first 5 errors to avoid spam
          console.log(`   Error migrating document ${doc.$id}:`, docError.message);
        }
      }
    }
    
    console.log(`\n🎉 Data migration complete!`);
    console.log(`   Successfully migrated: ${migratedCount} documents`);
    if (errorCount > 0) {
      console.log(`   Errors encountered: ${errorCount} documents`);
      if (errorCount > 5) {
        console.log(`   (Only first 5 errors shown)`);
      }
    }
    
    console.log('\n✅ Setup complete!');
    console.log('\n📝 Next steps:');
    console.log('1. Create a unique index in Appwrite Console:');
    console.log('   - Go to Database → user_social_tasks → Indexes');
    console.log('   - Add Index with attributes: taskId (asc), telegram_user_id (asc)');
    console.log('   - Set type to Unique');
    
  } catch (error) {
    console.error('❌ Data migration failed:', error.message);
  }
}

addMissingAttributes().catch(console.error);