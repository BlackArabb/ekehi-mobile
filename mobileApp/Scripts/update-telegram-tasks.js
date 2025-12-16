// Script to update Telegram tasks with verification data
// This script adds the required verificationData to existing Telegram tasks

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

async function updateTelegramTasks() {
  const collectionId = 'social_tasks';
  
  console.log(`\n🔧 Updating Telegram tasks with verification data...`);
  
  try {
    // Find all Telegram tasks
    const response = await databases.listDocuments(
      APPWRITE_CONFIG.databaseId,
      collectionId,
      [
        Query.equal('platform', 'Telegram')
      ]
    );
    
    console.log(`  Found ${response.documents.length} Telegram tasks`);
    
    // Update each Telegram task with verification data
    for (const task of response.documents) {
      try {
        // Update the task with verification data
        const updatedTask = await databases.updateDocument(
          APPWRITE_CONFIG.databaseId,
          collectionId,
          task.$id,
          {
            verificationData: JSON.stringify({
              channel_username: '@ekehi_network' // Replace with your actual Telegram channel
            })
          }
        );
        
        console.log(`  ✅ Updated task: ${task.title} (${task.$id})`);
      } catch (updateError) {
        console.error(`  ❌ Failed to update task ${task.title}:`, updateError.message);
      }
    }
    
    console.log(`  🎉 All Telegram tasks updated successfully!`);
  } catch (error) {
    console.error(`  ❌ Failed to update Telegram tasks:`, error.message);
    throw error;
  }
}

// Run the update
updateTelegramTasks()
  .then(() => {
    console.log('\n✅ Telegram tasks updated successfully!');
    process.exit(0);
  })
  .catch((error) => {
    console.error('\n❌ Error updating Telegram tasks:', error);
    process.exit(1);
  });