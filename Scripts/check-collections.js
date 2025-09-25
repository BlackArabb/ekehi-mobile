// Test Appwrite Collections - Check what was actually created
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

async function checkCollections() {
  try {
    console.log('🔍 Checking Appwrite collections...');
    
    // List all collections
    const collections = await databases.listCollections(APPWRITE_CONFIG.databaseId);
    console.log(`\\n📋 Found ${collections.total} collections:`);
    
    for (const collection of collections.collections) {
      console.log(`\\n📦 Collection: ${collection.name} (ID: ${collection.$id})`);
      console.log(`📊 Document count: ${collection.documentsCount}`);
      
      // List attributes for this collection
      try {
        const attributes = await databases.listAttributes(APPWRITE_CONFIG.databaseId, collection.$id);
        console.log(`🏷️  Attributes (${attributes.total}):`);
        
        attributes.attributes.forEach(attr => {
          console.log(`   • ${attr.key} (${attr.type}${attr.size ? ':' + attr.size : ''}) - Required: ${attr.required}, Unique: ${attr.xunique || false}`);
        });
      } catch (error) {
        console.log(`   ❌ Could not list attributes: ${error.message}`);
      }
    }
    
  } catch (error) {
    console.error('❌ Error checking collections:', error.message);
  }
}

checkCollections().catch(console.error);