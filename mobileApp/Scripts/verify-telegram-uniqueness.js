// Verify Telegram ID Uniqueness Script
//
// This script verifies that the Telegram ID uniqueness constraint is working properly
// by attempting to create duplicate entries and checking for errors.
//
// Usage:
// 1. Replace YOUR_PROJECT_ID and YOUR_API_KEY with your actual Appwrite credentials
// 2. Run: node verify-telegram-uniqueness.js

const { Client, Databases, ID } = require('node-appwrite');

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

// Test function to verify uniqueness
async function verifyTelegramUniqueness() {
  console.log('🔍 Verifying Telegram ID uniqueness constraint...');
  console.log('=' .repeat(60));
  
  try {
    // Validate configuration
    if (APPWRITE_CONFIG.apiKey === 'YOUR_API_KEY_HERE') {
      console.error('❌ Error: Please replace YOUR_API_KEY_HERE with your actual Appwrite API key');
      return;
    }
    
    console.log(`📡 Appwrite endpoint: ${APPWRITE_CONFIG.endpoint}`);
    console.log(`🎯 Project ID: ${APPWRITE_CONFIG.projectId}`);
    console.log(`🗄️  Database ID: ${APPWRITE_CONFIG.databaseId}`);
    
    // Create a test document
    console.log('\\n🧪 Creating test document...');
    
    const testTaskId = 'test_telegram_task_' + Date.now();
    const testTelegramId = 1234567890;
    const testUserId = 'test_user_' + Date.now();
    
    try {
      const testDoc1 = await databases.createDocument(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        ID.unique(),
        {
          userId: testUserId + '_1',
          taskId: testTaskId,
          completedAt: new Date().toISOString(),
          telegram_user_id: testTelegramId,
          proofData: JSON.stringify({
            platform: 'telegram',
            telegram_user_id: testTelegramId,
            submitted_at: Date.now()
          })
        }
      );
      
      console.log(`  ✅ Created first test document: ${testDoc1.$id}`);
      
      // Try to create a duplicate document with the same taskId and telegram_user_id
      console.log('\\n🧪 Attempting to create duplicate document...');
      
      try {
        const testDoc2 = await databases.createDocument(
          APPWRITE_CONFIG.databaseId,
          'user_social_tasks',
          ID.unique(),
          {
            userId: testUserId + '_2', // Different user
            taskId: testTaskId, // Same task
            completedAt: new Date().toISOString(),
            telegram_user_id: testTelegramId, // Same Telegram ID
            proofData: JSON.stringify({
              platform: 'telegram',
              telegram_user_id: testTelegramId,
              submitted_at: Date.now()
            })
          }
        );
        
        console.log(`  ⚠️  Created duplicate document: ${testDoc2.$id}`);
        console.log('  ❌ Uniqueness constraint is NOT working!');
        
        // Clean up
        try {
          await databases.deleteDocument(APPWRITE_CONFIG.databaseId, 'user_social_tasks', testDoc1.$id);
          await databases.deleteDocument(APPWRITE_CONFIG.databaseId, 'user_social_tasks', testDoc2.$id);
        } catch (cleanupError) {
          console.log('  ⚠️  Cleanup failed:', cleanupError.message);
        }
        
      } catch (duplicateError) {
        console.log('  ✅ Duplicate creation failed as expected');
        console.log('  🎉 Uniqueness constraint is working!');
        
        // Check if it's the right kind of error
        if (duplicateError.message.includes('unique') || duplicateError.message.includes('duplicate')) {
          console.log('  🔍 Error message confirms uniqueness constraint: ', duplicateError.message);
        }
        
        // Clean up
        try {
          await databases.deleteDocument(APPWRITE_CONFIG.databaseId, 'user_social_tasks', testDoc1.$id);
        } catch (cleanupError) {
          console.log('  ⚠️  Cleanup failed:', cleanupError.message);
        }
      }
      
    } catch (error) {
      console.error('❌ Test failed:', error.message);
      
      // If it's a unique constraint error, that's actually good!
      if (error.message.includes('unique') || error.message.includes('duplicate')) {
        console.log('  🎉 Uniqueness constraint is working! The error is expected.');
      }
    }
    
    console.log('\\n' + '='.repeat(60));
    console.log('✅ Verification complete!');
    console.log('💡 If you saw a uniqueness constraint error, that means the system is working correctly.');
    console.log('💡 If you did not see an error, you may need to set up the unique index in the Appwrite console.');
    
  } catch (error) {
    console.error('\\n❌ Verification failed:', error.message);
    console.log('\\n🛠 Troubleshooting:');
    console.log('1. Check your Appwrite project ID and API key');
    console.log('2. Verify you have admin permissions');
    console.log('3. Ensure your internet connection is stable');
    console.log('4. Make sure the user_social_tasks collection exists');
    console.log('5. Verify the unique index has been created in the Appwrite console');
  }
}

// Execute the verification
if (require.main === module) {
  verifyTelegramUniqueness().catch(console.error);
}