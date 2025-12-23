// Simple test script to verify Appwrite database connection
const { Client, Databases } = require('node-appwrite');

const client = new Client();
client
  .setEndpoint('https://fra.cloud.appwrite.io/v1')
  .setProject('68c2dd6e002112935ed2')
  .setKey('standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d');

const databases = new Databases(client);

async function testConnection() {
  try {
    console.log('Testing Appwrite database connection...');
    
    // Try to list all databases first
    const dbs = await databases.list();
    console.log(`✅ Found ${dbs.total} databases:`);
    
    for (const db of dbs.databases) {
      console.log(`  - ${db.name} (${db.$id})`);
      
      // If this is our target database, list its collections
      if (db.$id === '68c336e7000f87296feb') {
        try {
          const collections = await databases.listCollections(db.$id);
          console.log(`    Collections in ${db.name}:`);
          for (const collection of collections.collections) {
            console.log(`      - ${collection.name} (${collection.$id})`);
          }
        } catch (collectionError) {
          console.log(`    Error listing collections: ${collectionError.message}`);
        }
      }
    }
    
  } catch (error) {
    console.error('❌ Connection failed:', error.message);
    console.error('Error code:', error.code);
    console.error('Full error:', error);
  }
}

testConnection();