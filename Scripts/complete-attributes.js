// Complete Missing Attributes Script
// This script adds all missing attributes to your Appwrite collections

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

// Missing attributes for each collection
const MISSING_ATTRIBUTES = {
  user_profiles: [
    { key: 'totalCoins', type: 'double', required: true, default: 0 },
    { key: 'coinsPerClick', type: 'integer', required: true, default: 1 },
    { key: 'coinsPerSecond', type: 'double', required: true, default: 0 },
    { key: 'miningPower', type: 'double', required: true, default: 1 },
    { key: 'currentStreak', type: 'integer', required: true, default: 0 },
    { key: 'longestStreak', type: 'integer', required: true, default: 0 },
    { key: 'lastLoginDate', type: 'datetime', required: false },
    { key: 'referralCode', type: 'string', size: 255, required: false, unique: true },
    { key: 'referredBy', type: 'string', size: 255, required: false },
    { key: 'totalReferrals', type: 'integer', required: true, default: 0 },
    { key: 'lifetimeEarnings', type: 'double', required: true, default: 0 },
    { key: 'dailyMiningRate', type: 'double', required: true, default: 1000 },
    { key: 'maxDailyEarnings', type: 'double', required: true, default: 10000 },
    { key: 'todayEarnings', type: 'double', required: true, default: 0 },
    { key: 'lastMiningDate', type: 'datetime', required: false },
    { key: 'streakBonusClaimed', type: 'integer', required: true, default: 0 },
    { key: 'createdAt', type: 'datetime', required: true },
    { key: 'updatedAt', type: 'datetime', required: true }
  ],
  
  social_tasks: [
    { key: 'isActive', type: 'boolean', required: true, default: true },
    { key: 'sortOrder', type: 'integer', required: true, default: 0 },
    { key: 'createdAt', type: 'datetime', required: true }
  ],
  
  achievements: [
    { key: 'isActive', type: 'boolean', required: true, default: true },
    { key: 'createdAt', type: 'datetime', required: true }
  ]
};

// Helper function to create an attribute
async function createAttribute(collectionId, attribute) {
  try {
    let result;
    
    console.log(`  📝 Creating ${attribute.key} (${attribute.type})...`);
    
    switch (attribute.type) {
      case 'string':
        result = await databases.createStringAttribute(
          APPWRITE_CONFIG.databaseId,
          collectionId,
          attribute.key,
          attribute.size,
          attribute.required,
          attribute.default || null,
          attribute.unique || false
        );
        break;
        
      case 'integer':
        result = await databases.createIntegerAttribute(
          APPWRITE_CONFIG.databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.min,
          attribute.max,
          attribute.default || null
        );
        break;
        
      case 'double':
        result = await databases.createFloatAttribute(
          APPWRITE_CONFIG.databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.min,
          attribute.max,
          attribute.default || null
        );
        break;
        
      case 'boolean':
        result = await databases.createBooleanAttribute(
          APPWRITE_CONFIG.databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.default || null
        );
        break;
        
      case 'datetime':
        result = await databases.createDatetimeAttribute(
          APPWRITE_CONFIG.databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.default || null
        );
        break;
        
      default:
        throw new Error(`Unsupported attribute type: ${attribute.type}`);
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

async function addMissingAttributes() {
  console.log('🔧 Adding missing attributes to Appwrite collections...');
  console.log('============================================================');
  
  let totalAdded = 0;
  
  for (const [collectionId, attributes] of Object.entries(MISSING_ATTRIBUTES)) {
    console.log(`\\n📦 Processing collection: ${collectionId}`);
    console.log(`🎯 Adding ${attributes.length} missing attributes...`);
    
    let addedCount = 0;
    
    for (const attribute of attributes) {
      const result = await createAttribute(collectionId, attribute);
      if (result) {
        addedCount++;
        totalAdded++;
      }
      
      // Add small delay to avoid rate limits
      await new Promise(resolve => setTimeout(resolve, 500));
    }
    
    console.log(`✅ ${collectionId}: ${addedCount}/${attributes.length} attributes added`);
  }
  
  console.log('\\n' + '='.repeat(60));
  console.log('🎉 Attribute completion finished!');
  console.log(`📊 Total attributes added: ${totalAdded}`);
  console.log('\\n📋 Next steps:');
  console.log('1. Run "node check-collections.js" to verify');
  console.log('2. Run "node simple-migrate.js" to migrate remaining data');
  console.log('3. Test your app: pnpm start');
}

// Add delay helper
function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

addMissingAttributes().catch(console.error);