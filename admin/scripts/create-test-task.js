const { Client, Databases } = require('appwrite');

async function createTestTask() {
  console.log('Creating test task...');
  
  // Initialize Appwrite client
  const client = new Client();
  client
    .setEndpoint('https://fra.cloud.appwrite.io/v1')
    .setProject('68c2dd6e002112935ed2');
    
  const databases = new Databases(client);
  
  try {
    console.log('Creating document...');
    
    // Create a test document
    const response = await databases.createDocument(
      '68c336e7000f87296feb',
      'social_tasks',
      'unique()',
      {
        title: 'Test Task',
        description: 'This is a test task',
        platform: 'Twitter',
        taskType: 'follow',
        rewardCoins: 50,
        isActive: true,
        sortOrder: 1
      }
    );
    
    console.log('✅ Task created successfully:', response.$id);
    
  } catch (error) {
    console.error('❌ Error creating task:', error.message);
  }
}

createTestTask();