// Script to fix missing attributes in user_social_tasks collection
// This script adds the missing attributes that are required by the Android app

const { Client, Databases, Permission, Role } = require('node-appwrite');

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

// Missing attributes for user_social_tasks collection
const MISSING_ATTRIBUTES = [
  { key: 'status', type: 'string', size: 255, required: true }, // Removed default value
  { key: 'verifiedAt', type: 'datetime', required: false },
  { key: 'proofUrl', type: 'string', size: 1000, required: false },
  { key: 'proofData', type: 'string', size: 5000, required: false }, // Store as JSON string
  { key: 'verificationAttempts', type: 'integer', required: true }, // Removed default value
  { key: 'rejectionReason', type: 'string', size: 1000, required: false }
];

// Helper function to create an attribute
async function createAttribute(databaseId, collectionId, attribute) {
  try {
    let result;
    
    switch (attribute.type) {
      case 'string':
        result = await databases.createStringAttribute(
          databaseId,
          collectionId,
          attribute.key,
          attribute.size,
          attribute.required,
          attribute.default, // This will be undefined for required attributes
          attribute.unique || false
        );
        break;
        
      case 'integer':
        result = await databases.createIntegerAttribute(
          databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.min,
          attribute.max,
          attribute.default // This will be undefined for required attributes
        );
        break;
        
      case 'double':
        result = await databases.createFloatAttribute(
          databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.min,
          attribute.max,
          attribute.default // This will be undefined for required attributes
        );
        break;
        
      case 'boolean':
        result = await databases.createBooleanAttribute(
          databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.default // This will be undefined for required attributes
        );
        break;
        
      case 'datetime':
        result = await databases.createDatetimeAttribute(
          databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.default // This will be undefined for required attributes
        );
        break;
        
      default:
        throw new Error(`Unknown attribute type: ${attribute.type}`);
    }
    
    const uniqueText = attribute.unique ? ' (unique)' : '';
    console.log(`  ✅ Created attribute: ${attribute.key} (${attribute.type})${uniqueText}`);
    return result;
    
  } catch (error) {
    if (error.message.includes('already exists')) {
      console.log(`  ⚠️  Attribute ${attribute.key} already exists, skipping...`);
    } else {
      console.error(`  ❌ Failed to create attribute ${attribute.key}:`, error.message);
      throw error;
    }
  }
}

async function fixUserSocialTasksCollection() {
  const collectionId = 'user_social_tasks';
  
  console.log(`\n🔧 Fixing collection: ${collectionId}`);
  
  // Add missing attributes
  console.log(`  📝 Adding ${MISSING_ATTRIBUTES.length} missing attributes...`);
  for (const attribute of MISSING_ATTRIBUTES) {
    await createAttribute(APPWRITE_CONFIG.databaseId, collectionId, attribute);
    // Small delay between attributes to avoid rate limits
    await new Promise(resolve => setTimeout(resolve, 200));
  }
  
  console.log(`  🎉 Collection ${collectionId} fix complete!`);
}

// Run the fix
fixUserSocialTasksCollection()
  .then(() => {
    console.log('\n✅ All fixes applied successfully!');
    process.exit(0);
  })
  .catch((error) => {
    console.error('\n❌ Error applying fixes:', error);
    process.exit(1);
  });