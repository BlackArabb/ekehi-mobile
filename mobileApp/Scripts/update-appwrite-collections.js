// Appwrite Collection Update Script
//
// This script creates or updates Appwrite collections with the correct structure
// based on the current data model in the mobile app
//
// Usage:
// node Scripts/update-appwrite-collections.js

const { Client, Databases, Permission, Role, ID } = require('node-appwrite');

// Configuration - Update these with your actual values
const APPWRITE_CONFIG = {
  endpoint: process.env.APPWRITE_ENDPOINT || 'https://fra.cloud.appwrite.io/v1',
  projectId: process.env.APPWRITE_PROJECT_ID || '68c2dd6e002112935ed2',
  databaseId: process.env.APPWRITE_DATABASE_ID || '68c336e7000f87296feb',
  apiKey: process.env.APPWRITE_API_KEY || 'YOUR_API_KEY_HERE' // You'll need to provide this
};

// Appwrite Client Setup
const client = new Client();
client
  .setEndpoint(APPWRITE_CONFIG.endpoint)
  .setProject(APPWRITE_CONFIG.projectId)
  .setKey(APPWRITE_CONFIG.apiKey);

const databases = new Databases(client);

// Collection definitions matching the current mobile app structure
const COLLECTIONS = {
  users: {
    name: 'users',
    permissions: [
      Permission.read(Role.users()),
      Permission.create(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'email', type: 'string', size: 255, required: true },
      { key: 'name', type: 'string', size: 255, required: true },
      { key: 'createdAt', type: 'datetime', required: true },
      { key: 'lastLogin', type: 'datetime', required: false }
    ]
  },
  user_profiles: {
    name: 'user_profiles',
    permissions: [
      Permission.read(Role.users()),
      Permission.create(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true, array: false },
      { key: 'username', type: 'string', size: 255, required: false },
      { key: 'totalCoins', type: 'double', required: true, default: 0 },
      { key: 'coinsPerSecond', type: 'double', required: true, default: 0 }, // Deprecated but still in use
      { key: 'autoMiningRate', type: 'double', required: true, default: 0 }, // New field for auto mining rate
      { key: 'miningPower', type: 'double', required: true, default: 1 },
      { key: 'referralBonusRate', type: 'double', required: true, default: 0 }, // New field for referral bonus
      { key: 'currentStreak', type: 'integer', required: true, default: 0 },
      { key: 'longestStreak', type: 'integer', required: true, default: 0 },
      { key: 'lastLoginDate', type: 'datetime', required: false },
      { key: 'referralCode', type: 'string', size: 255, required: false },
      { key: 'referredBy', type: 'string', size: 255, required: false },
      { key: 'totalReferrals', type: 'integer', required: true, default: 0 },
      { key: 'lifetimeEarnings', type: 'double', required: true, default: 0 },
      { key: 'dailyMiningRate', type: 'double', required: true, default: 2 }, // Updated to 2 EKH per 24-hour session
      { key: 'maxDailyEarnings', type: 'double', required: true, default: 10000 },
      { key: 'todayEarnings', type: 'double', required: true, default: 0 },
      { key: 'lastMiningDate', type: 'datetime', required: false },
      { key: 'streakBonusClaimed', type: 'integer', required: true, default: 0 },
      { key: 'createdAt', type: 'datetime', required: true },
      { key: 'updatedAt', type: 'datetime', required: true }
    ]
  },
  mining_sessions: {
    name: 'mining_sessions',
    permissions: [
      Permission.read(Role.users()),
      Permission.create(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true },
      { key: 'coinsEarned', type: 'double', required: true },
      { key: 'clicksMade', type: 'integer', required: true },
      { key: 'sessionDuration', type: 'integer', required: true },
      { key: 'createdAt', type: 'datetime', required: true },
      { key: 'updatedAt', type: 'datetime', required: true }
    ]
  },
  social_tasks: {
    name: 'social_tasks',
    permissions: [
      Permission.read(Role.users()),
      Permission.create(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'title', type: 'string', size: 255, required: true },
      { key: 'description', type: 'string', size: 1000, required: true },
      { key: 'platform', type: 'string', size: 255, required: true },
      { key: 'taskType', type: 'string', size: 255, required: true },
      { key: 'rewardCoins', type: 'double', required: true },
      { key: 'actionUrl', type: 'string', size: 500, required: false },
      { key: 'verificationMethod', type: 'string', size: 255, required: true },
      { key: 'isActive', type: 'boolean', required: true, default: true },
      { key: 'sortOrder', type: 'integer', required: true, default: 0 },
      { key: 'createdAt', type: 'datetime', required: true },
      { key: 'updatedAt', type: 'datetime', required: true }
    ]
  },
  user_social_tasks: {
    name: 'user_social_tasks',
    permissions: [
      Permission.read(Role.users()),
      Permission.create(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true },
      { key: 'taskId', type: 'string', size: 255, required: true },
      { key: 'completedAt', type: 'datetime', required: true },
      { key: 'createdAt', type: 'datetime', required: true },
      { key: 'updatedAt', type: 'datetime', required: true }
    ]
  },
  achievements: {
    name: 'achievements',
    permissions: [
      Permission.read(Role.users()),
      Permission.create(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'achievementId', type: 'string', size: 255, required: true },
      { key: 'title', type: 'string', size: 255, required: true },
      { key: 'description', type: 'string', size: 1000, required: true },
      { key: 'type', type: 'string', size: 255, required: true },
      { key: 'target', type: 'double', required: true },
      { key: 'reward', type: 'double', required: true },
      { key: 'rarity', type: 'string', size: 255, required: true },
      { key: 'isActive', type: 'boolean', required: true, default: true },
      { key: 'createdAt', type: 'datetime', required: true },
      { key: 'updatedAt', type: 'datetime', required: true }
    ]
  },
  user_achievements: {
    name: 'user_achievements',
    permissions: [
      Permission.read(Role.users()),
      Permission.create(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true },
      { key: 'achievementId', type: 'string', size: 255, required: true },
      { key: 'claimedAt', type: 'datetime', required: true },
      { key: 'createdAt', type: 'datetime', required: true },
      { key: 'updatedAt', type: 'datetime', required: true }
    ]
  },
  presale_purchases: {
    name: 'presale_purchases',
    permissions: [
      Permission.read(Role.users()),
      Permission.create(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true },
      { key: 'amountUsd', type: 'double', required: true },
      { key: 'tokensAmount', type: 'double', required: true },
      { key: 'transactionHash', type: 'string', size: 255, required: false },
      { key: 'status', type: 'string', size: 255, required: true },
      { key: 'paymentMethod', type: 'string', size: 255, required: false },
      { key: 'createdAt', type: 'datetime', required: true },
      { key: 'updatedAt', type: 'datetime', required: true }
    ]
  },
  ad_views: {
    name: 'ad_views',
    permissions: [
      Permission.read(Role.users()),
      Permission.create(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true },
      { key: 'adType', type: 'string', size: 255, required: true },
      { key: 'reward', type: 'double', required: true },
      { key: 'createdAt', type: 'datetime', required: true },
      { key: 'updatedAt', type: 'datetime', required: true }
    ]
  }
};

// Function to create or update a collection
async function createOrUpdateCollection(collectionId, collectionConfig) {
  try {
    console.log(`\n🔧 Creating/updating collection: ${collectionConfig.name}`);
    
    // Try to get the collection first
    try {
      const existingCollection = await databases.getCollection(
        APPWRITE_CONFIG.databaseId,
        collectionId
      );
      console.log(`  ℹ️  Collection ${collectionConfig.name} already exists`);
      return existingCollection;
    } catch (error) {
      // Collection doesn't exist, create it
      console.log(`  ➕ Creating new collection: ${collectionConfig.name}`);
      const collection = await databases.createCollection(
        APPWRITE_CONFIG.databaseId,
        collectionId,
        collectionConfig.name,
        collectionConfig.permissions
      );
      
      // Add attributes
      console.log(`  📝 Adding attributes to ${collectionConfig.name}`);
      for (const attribute of collectionConfig.attributes) {
        try {
          switch (attribute.type) {
            case 'string':
              await databases.createStringAttribute(
                APPWRITE_CONFIG.databaseId,
                collectionId,
                attribute.key,
                attribute.size,
                attribute.required,
                attribute.default,
                attribute.array
              );
              break;
            case 'integer':
              await databases.createIntegerAttribute(
                APPWRITE_CONFIG.databaseId,
                collectionId,
                attribute.key,
                attribute.required,
                attribute.default,
                attribute.array
              );
              break;
            case 'double':
              await databases.createFloatAttribute(
                APPWRITE_CONFIG.databaseId,
                collectionId,
                attribute.key,
                attribute.required,
                attribute.default,
                attribute.array
              );
              break;
            case 'boolean':
              await databases.createBooleanAttribute(
                APPWRITE_CONFIG.databaseId,
                collectionId,
                attribute.key,
                attribute.required,
                attribute.default,
                attribute.array
              );
              break;
            case 'datetime':
              await databases.createDatetimeAttribute(
                APPWRITE_CONFIG.databaseId,
                collectionId,
                attribute.key,
                attribute.required,
                attribute.default,
                attribute.array
              );
              break;
          }
          console.log(`    ✅ Added attribute: ${attribute.key}`);
        } catch (attrError) {
          if (attrError.message.includes('already exists')) {
            console.log(`    ℹ️  Attribute ${attribute.key} already exists`);
          } else {
            console.error(`    ❌ Failed to add attribute ${attribute.key}:`, attrError.message);
          }
        }
      }
      
      return collection;
    }
  } catch (error) {
    console.error(`❌ Error creating/updating collection ${collectionConfig.name}:`, error.message);
    throw error;
  }
}

// Main function to update all collections
async function updateAllCollections() {
  console.log('🚀 Starting Appwrite collection update...');
  console.log('='.repeat(50));
  
  // Validate configuration
  if (APPWRITE_CONFIG.apiKey === 'YOUR_API_KEY_HERE') {
    console.error('❌ Error: Please set your Appwrite API key in the APPWRITE_CONFIG');
    console.log('\n🔧 Setup instructions:');
    console.log('1. Create an API key in your Appwrite console with appropriate permissions');
    console.log('2. Set the APPWRITE_API_KEY environment variable');
    console.log('3. Or update the apiKey value directly in this script');
    return;
  }
  
  console.log(`📡 Connecting to Appwrite at: ${APPWRITE_CONFIG.endpoint}`);
  console.log(`📦 Using project: ${APPWRITE_CONFIG.projectId}`);
  console.log(`📂 Using database: ${APPWRITE_CONFIG.databaseId}`);
  
  try {
    // Create/update all collections
    for (const [collectionId, collectionConfig] of Object.entries(COLLECTIONS)) {
      await createOrUpdateCollection(collectionId, collectionConfig);
    }
    
    console.log('\n' + '='.repeat(50));
    console.log('✅ All collections updated successfully!');
    console.log('\n📝 Next steps:');
    console.log('1. Verify collections in your Appwrite dashboard');
    console.log('2. Update your app configuration if needed');
    console.log('3. Test your app to ensure everything works correctly');
  } catch (error) {
    console.error('\n❌ Collection update failed:', error);
    console.log('\n🛠 Troubleshooting:');
    console.log('1. Check your Appwrite project ID and API key');
    console.log('2. Verify your Appwrite project permissions');
    console.log('3. Ensure you have the correct database ID');
  }
}

// Execute the script
if (require.main === module) {
  updateAllCollections().catch(console.error);
}