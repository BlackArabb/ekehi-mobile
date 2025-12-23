const { Client, Databases } = require('appwrite');

async function testAppwriteConnection() {
  console.log('Testing Appwrite Connection...');
  
  // Initialize Appwrite client
  const client = new Client();
  client
    .setEndpoint('https://fra.cloud.appwrite.io/v1')
    .setProject('68c2dd6e002112935ed2');
    
  const databases = new Databases(client);
  
  try {
    // Test connection by listing collections
    console.log('Attempting to connect to Appwrite...');
    
    // Just test if we can initialize the client
    console.log('✅ Appwrite client initialized successfully');
    console.log('Project: 68c2dd6e002112935ed2');
    
    // Try to list documents from the social tasks collection
    const response = await databases.listDocuments(
      '68c336e7000f87296feb',
      'social_tasks'
    );
    
    console.log(`✅ Successfully connected to database. Found ${response.total} social tasks.`);
    
    if (response.documents.length > 0) {
      const sampleTask = response.documents[0];
      console.log('\nSample Task:');
      console.log('- ID:', sampleTask.$id);
      console.log('- Title:', sampleTask.title);
      console.log('- Platform:', sampleTask.platform);
      console.log('- Reward:', sampleTask.rewardCoins);
    }
    
  } catch (error) {
    console.error('❌ Error connecting to Appwrite:', error.message);
  }
}

testAppwriteConnection();