// Script to check attributes of user_social_tasks collection
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

async function checkUserSocialTasksAttributes() {
  try {
    console.log('🔍 Checking user_social_tasks collection attributes...');
    
    // Get the collection details
    const collection = await databases.getCollection(
      APPWRITE_CONFIG.databaseId,
      'user_social_tasks'  // collection ID
    );
    
    console.log(`Collection Name: ${collection.name}`);
    console.log(`Collection ID: ${collection.$id}`);
    console.log('\nAttributes:');
    
    // Display all attributes
    collection.attributes.forEach(attr => {
      console.log(`- ${attr.key} (${attr.type})`);
      if (attr.required) {
        console.log('  Required: Yes');
      }
    });
    
    console.log('\n✅ Check completed successfully');
  } catch (error) {
    console.error('❌ Error checking collection attributes:', error.message);
  }
}

checkUserSocialTasksAttributes();