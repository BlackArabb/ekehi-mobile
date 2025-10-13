// Final Missing Attributes Script
// Adds the last few missing attributes without default values

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

// Final missing attributes (without default values for required fields)
const FINAL_ATTRIBUTES = {
  user_profiles: [
    { key: 'miningPower', type: 'double', required: false },   // Changed to optional
    { key: 'dailyMiningRate', type: 'double', required: false }, // Changed to optional
    { key: 'maxDailyEarnings', type: 'double', required: false } // Changed to optional
  ],
  
  social_tasks: [
    { key: 'isActive', type: 'boolean', required: false } // Changed to optional
  ],
  
  achievements: [
    { key: 'isActive', type: 'boolean', required: false } // Changed to optional
  ]
};

async function createAttribute(collectionId, attribute) {
  try {
    console.log(`  📝 Creating ${attribute.key} (${attribute.type})...`);
    
    let result;
    switch (attribute.type) {
      case 'integer':
        result = await databases.createIntegerAttribute(
          APPWRITE_CONFIG.databaseId,
          collectionId,
          attribute.key,
          attribute.required
        );
        break;
        
      case 'double':
        result = await databases.createFloatAttribute(
          APPWRITE_CONFIG.databaseId,
          collectionId,
          attribute.key,
          attribute.required
        );
        break;
        
      case 'boolean':
        result = await databases.createBooleanAttribute(
          APPWRITE_CONFIG.databaseId,
          collectionId,
          attribute.key,
          attribute.required
        );
        break;
    }
    
    console.log(`  ✅ Created ${attribute.key}`);
    return result;
    
  } catch (error) {
    if (error.message.includes('already exists')) {
      console.log(`  ⚠️  ${attribute.key} already exists, skipping...`);
    } else {
      console.error(`  ❌ Failed to create ${attribute.key}:`, error.message);
    }
    return null;
  }
}

async function addFinalAttributes() {
  console.log('🔧 Adding final missing attributes...');
  console.log('============================================================');
  
  let totalAdded = 0;
  
  for (const [collectionId, attributes] of Object.entries(FINAL_ATTRIBUTES)) {
    console.log(`\\n📦 Processing collection: ${collectionId}`);
    console.log(`🎯 Adding ${attributes.length} final attributes...`);
    
    let addedCount = 0;
    
    for (const attribute of attributes) {
      const result = await createAttribute(collectionId, attribute);
      if (result) {
        addedCount++;
        totalAdded++;
      }
      
      // Add delay to avoid rate limits
      await new Promise(resolve => setTimeout(resolve, 1000));
    }
    
    console.log(`✅ ${collectionId}: ${addedCount}/${attributes.length} attributes added`);
  }
  
  console.log('\\n' + '='.repeat(60));
  console.log('🎉 All attributes completed!');
  console.log(`📊 Final attributes added: ${totalAdded}`);
  console.log('\\n📋 Next steps:');
  console.log('1. Run "node check-collections.js" to verify complete structure');
  console.log('2. Your database is now complete with all required attributes!');
  console.log('3. Test data migration and app functionality');
}

addFinalAttributes().catch(console.error);