// Script to add verificationData attribute to social_tasks collection
// This attribute is required for Telegram and other platform-specific verification

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

async function addVerificationDataAttribute() {
  const collectionId = 'social_tasks';
  
  console.log(`\n🔧 Adding verificationData attribute to collection: ${collectionId}`);
  
  try {
    // Add verificationData attribute as a string (will store JSON)
    await databases.createStringAttribute(
      APPWRITE_CONFIG.databaseId,
      collectionId,
      'verificationData', // attribute key
      5000, // size
      false // not required (some tasks might not need verification data)
    );
    
    console.log(`  ✅ Created attribute: verificationData (string)`);
    console.log(`  🎉 Collection ${collectionId} updated successfully!`);
  } catch (error) {
    if (error.message.includes('already exists')) {
      console.log(`  ⚠️  Attribute verificationData already exists, skipping...`);
    } else {
      console.error(`  ❌ Failed to create attribute verificationData:`, error.message);
      throw error;
    }
  }
}

// Run the fix
addVerificationDataAttribute()
  .then(() => {
    console.log('\n✅ VerificationData attribute added successfully!');
    process.exit(0);
  })
  .catch((error) => {
    console.error('\n❌ Error adding verificationData attribute:', error);
    process.exit(1);
  });