const { Client, Databases, Query } = require('appwrite');

async function debugSocialTasks() {
  console.log('Debugging Social Tasks Fetching...');
  
  // Initialize Appwrite client
  const client = new Client();
  client
    .setEndpoint('https://fra.cloud.appwrite.io/v1')
    .setProject('68c2dd6e002112935ed2');
    
  const databases = new Databases(client);
  
  try {
    console.log('Attempting to fetch social tasks...');
    console.log('Database ID: 68c336e7000f87296feb');
    console.log('Collection ID: social_tasks');
    
    // Try to list documents from the social tasks collection
    const response = await databases.listDocuments(
      '68c336e7000f87296feb',
      'social_tasks',
      [Query.orderAsc('sortOrder')]
    );
    
    console.log(`✅ Successfully fetched social tasks. Found ${response.total} tasks.`);
    console.log('Response structure:', Object.keys(response));
    console.log('Documents count:', response.documents.length);
    
    if (response.documents.length > 0) {
      console.log('\nFirst task details:');
      const firstTask = response.documents[0];
      console.log('- ID:', firstTask.$id);
      console.log('- Title:', firstTask.title);
      console.log('- Platform:', firstTask.platform);
      console.log('- Reward Coins:', firstTask.rewardCoins);
      console.log('- Is Active:', firstTask.isActive);
      console.log('- All keys:', Object.keys(firstTask));
    } else {
      console.log('\nNo tasks found in the collection.');
      
      // Let's try to create a test task
      console.log('\nCreating a test task...');
      const testTask = await databases.createDocument(
        '68c336e7000f87296feb',
        'social_tasks',
        'unique()',
        {
          title: 'Test Social Task',
          description: 'This is a test task for debugging',
          platform: 'Twitter',
          taskType: 'follow',
          rewardCoins: 50,
          isActive: true,
          sortOrder: 1
        }
      );
      
      console.log('✅ Test task created successfully:', testTask.$id);
      
      // Try fetching again
      const response2 = await databases.listDocuments(
        '68c336e7000f87296feb',
        'social_tasks',
        [Query.orderAsc('sortOrder')]
      );
      
      console.log(`✅ After creating test task, found ${response2.total} tasks.`);
    }
    
  } catch (error) {
    console.error('❌ Error fetching social tasks:', error.message);
    console.error('Error details:', error);
  }
}

debugSocialTasks();