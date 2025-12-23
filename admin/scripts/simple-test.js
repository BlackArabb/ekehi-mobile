const { Client, Databases } = require('appwrite');

async function simpleTest() {
  console.log('Simple Test...');
  
  // Initialize Appwrite client
  const client = new Client();
  client
    .setEndpoint('https://fra.cloud.appwrite.io/v1')
    .setProject('68c2dd6e002112935ed2');
    
  const databases = new Databases(client);
  
  try {
    console.log('Fetching social tasks...');
    
    // Try to list documents from the social tasks collection
    const response = await databases.listDocuments(
      '68c336e7000f87296feb',
      'social_tasks'
    );
    
    console.log(`Found ${response.total} tasks.`);
    
  } catch (error) {
    console.error('Error:', error.message);
  }
}

simpleTest();