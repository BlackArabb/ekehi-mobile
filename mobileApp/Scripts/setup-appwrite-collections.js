// Appwrite Collections Auto-Setup Script
// 
// This script automatically creates all required collections in your Appwrite database
// with the correct attributes and permissions - just like Cloudflare D1 schema generation
// 
// Usage:
// 1. Replace YOUR_PROJECT_ID with your actual Appwrite project ID
// 2. Run: pnpm run setup-collections

const { Client, Databases, Permission, Role, ID } = require('node-appwrite');

// ⚠️ IMPORTANT: Replace with your actual Appwrite project ID and API Key
const APPWRITE_CONFIG = {
  endpoint: 'https://fra.cloud.appwrite.io/v1', // Frankfurt region endpoint
  projectId: '68c2dd6e002112935ed2', // Your actual project ID
  databaseId: '68c336e7000f87296feb', // Using your existing database ID
  apiKey: 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d' // Replace with your actual API key from Appwrite Console
};

// Appwrite Client Setup for server-side operations
const client = new Client();
client
  .setEndpoint(APPWRITE_CONFIG.endpoint)
  .setProject(APPWRITE_CONFIG.projectId)
  .setKey(APPWRITE_CONFIG.apiKey); // Using API key from config

const databases = new Databases(client);

// Collection Schema Definitions
const COLLECTIONS_SCHEMA = {
  users: {
    name: 'users',
    documentSecurity: true,
    permissions: [
      Permission.create(Role.users()),
      Permission.read(Role.users()),
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
    documentSecurity: true,
    permissions: [
      Permission.create(Role.users()),
      Permission.read(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true, unique: true },
      { key: 'username', type: 'string', size: 255, required: false },
      { key: 'totalCoins', type: 'double', required: true, default: 0 },
      { key: 'coinsPerSecond', type: 'double', required: true, default: 0 },
      { key: 'miningPower', type: 'double', required: true, default: 1 },
      { key: 'currentStreak', type: 'integer', required: true, default: 0 },
      { key: 'longestStreak', type: 'integer', required: true, default: 0 },
      { key: 'lastLoginDate', type: 'datetime', required: false },
      { key: 'referralCode', type: 'string', size: 255, required: false, unique: true },
      { key: 'referredBy', type: 'string', size: 255, required: false },
      { key: 'totalReferrals', type: 'integer', required: true, default: 0 },
      { key: 'lifetimeEarnings', type: 'double', required: true, default: 0 },
      { key: 'dailyMiningRate', type: 'double', required: true, default: 2 }, // UPDATED: Now represents 2 EKH per 24-hour session
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
    documentSecurity: true,
    permissions: [
      Permission.create(Role.users()),
      Permission.read(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true },
      { key: 'coinsEarned', type: 'double', required: true },
      { key: 'clicksMade', type: 'integer', required: true },
      { key: 'sessionDuration', type: 'integer', required: true },
      { key: 'createdAt', type: 'datetime', required: true }
    ]
  },

  social_tasks: {
    name: 'social_tasks',
    documentSecurity: true,
    permissions: [
      Permission.create(Role.users()),
      Permission.read(Role.users()),
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
      { key: 'createdAt', type: 'datetime', required: true }
    ]
  },

  user_social_tasks: {
    name: 'user_social_tasks',
    documentSecurity: true,
    permissions: [
      Permission.create(Role.users()),
      Permission.read(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true },
      { key: 'taskId', type: 'string', size: 255, required: true },
      { key: 'completedAt', type: 'datetime', required: true },
      { key: 'username', type: 'string', size: 255, required: false }
    ]
  },

  achievements: {
    name: 'achievements',
    documentSecurity: true,
    permissions: [
      Permission.create(Role.users()),
      Permission.read(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'achievementId', type: 'string', size: 255, required: true, unique: true },
      { key: 'title', type: 'string', size: 255, required: true },
      { key: 'description', type: 'string', size: 1000, required: true },
      { key: 'type', type: 'string', size: 255, required: true },
      { key: 'target', type: 'double', required: true },
      { key: 'reward', type: 'double', required: true },
      { key: 'rarity', type: 'string', size: 255, required: true },
      { key: 'isActive', type: 'boolean', required: true, default: true },
      { key: 'createdAt', type: 'datetime', required: true }
    ]
  },

  user_achievements: {
    name: 'user_achievements',
    documentSecurity: true,
    permissions: [
      Permission.create(Role.users()),
      Permission.read(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true },
      { key: 'achievementId', type: 'string', size: 255, required: true },
      { key: 'claimedAt', type: 'datetime', required: true }
    ]
  },

  presale_purchases: {
    name: 'presale_purchases',
    documentSecurity: true,
    permissions: [
      Permission.create(Role.users()),
      Permission.read(Role.users()),
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
      { key: 'createdAt', type: 'datetime', required: true }
    ]
  },

  ad_views: {
    name: 'ad_views',
    documentSecurity: true,
    permissions: [
      Permission.create(Role.users()),
      Permission.read(Role.users()),
      Permission.update(Role.users()),
      Permission.delete(Role.users())
    ],
    attributes: [
      { key: 'userId', type: 'string', size: 255, required: true },
      { key: 'adType', type: 'string', size: 255, required: true },
      { key: 'reward', type: 'double', required: true },
      { key: 'createdAt', type: 'datetime', required: true }
    ]
  }
};

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
          attribute.default,
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
          attribute.default
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
          attribute.default
        );
        break;
        
      case 'boolean':
        result = await databases.createBooleanAttribute(
          databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.default
        );
        break;
        
      case 'datetime':
        result = await databases.createDatetimeAttribute(
          databaseId,
          collectionId,
          attribute.key,
          attribute.required,
          attribute.default
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

// Function to create a collection with all its attributes
async function createCollection(collectionSchema) {
  const collectionId = collectionSchema.name;
  
  try {
    console.log(`\\n🚀 Creating collection: ${collectionId}`);
    
    // Create the collection
    try {
      await databases.createCollection(
        APPWRITE_CONFIG.databaseId,
        collectionId,
        collectionSchema.name,
        collectionSchema.permissions,
        collectionSchema.documentSecurity
      );
      console.log(`  ✅ Collection created: ${collectionId}`);
    } catch (error) {
      if (error.message.includes('already exists')) {
        console.log(`  ⚠️  Collection ${collectionId} already exists, updating attributes...`);
      } else {
        throw error;
      }
    }
    
    // Add a small delay to ensure collection is ready
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Create all attributes
    console.log(`  📝 Creating ${collectionSchema.attributes.length} attributes...`);
    for (const attribute of collectionSchema.attributes) {
      await createAttribute(APPWRITE_CONFIG.databaseId, collectionId, attribute);
      // Small delay between attributes to avoid rate limits
      await new Promise(resolve => setTimeout(resolve, 200));
    }
    
    console.log(`  🎉 Collection ${collectionId} setup complete!`);
    return collectionId;
    
  } catch (error) {
    console.error(`❌ Failed to create collection ${collectionId}:`, error.message);
    throw error;
  }
}

// Main setup function
async function setupAppwriteCollections() {
  console.log('🚀 Starting Appwrite Collections Auto-Setup...');
  console.log('=' .repeat(60));
  
  // Validate configuration
  if (APPWRITE_CONFIG.apiKey === 'YOUR_API_KEY_HERE') {
    console.error('❌ Error: Please replace YOUR_API_KEY_HERE with your actual Appwrite API key');
    console.log('🔑 To get your API key:');
    console.log('1. Go to https://cloud.appwrite.io/console');
    console.log('2. Select your project');
    console.log('3. Go to Settings → API Keys');
    console.log('4. Create a new API key with Database permissions');
    console.log('5. Copy the key and replace YOUR_API_KEY_HERE in this script');
    return;
  }
  
  // Validate configuration
  if (APPWRITE_CONFIG.projectId === 'YOUR_PROJECT_ID') {
    console.error('❌ Error: Please replace YOUR_PROJECT_ID with your actual Appwrite project ID');
    console.log('\\n🛠 Setup Instructions:');
    console.log('1. Edit setup-appwrite-collections.js');
    console.log('2. Replace YOUR_PROJECT_ID with your actual project ID');
    console.log('3. Run the script again');
    return;
  }
  
  console.log(`📡 Appwrite endpoint: ${APPWRITE_CONFIG.endpoint}`);
  console.log(`🎯 Project ID: ${APPWRITE_CONFIG.projectId}`);
  console.log(`🗄️  Database ID: ${APPWRITE_CONFIG.databaseId}`);
  
  try {
    // Create/verify database exists
    try {
      await databases.get(APPWRITE_CONFIG.databaseId);
      console.log(`\\n✅ Database '${APPWRITE_CONFIG.databaseId}' found`);
    } catch (error) {
      if (error.code === 404) {
        console.log(`\\n🏗️  Creating database: ${APPWRITE_CONFIG.databaseId}`);
        try {
          await databases.create(APPWRITE_CONFIG.databaseId, APPWRITE_CONFIG.databaseId);
          console.log(`✅ Database created successfully`);
        } catch (createError) {
          if (createError.message.includes('maximum number of databases')) {
            console.log(`⚠️ Database limit reached. Let's use your existing database.`);
            console.log(`📋 Please check your Appwrite console for existing databases.`);
            // Try to list existing databases
            try {
              const existingDbs = await databases.list();
              console.log(`📊 Found ${existingDbs.total} existing databases:`);
              existingDbs.databases.forEach(db => {
                console.log(`  • ${db.name} (ID: ${db.$id})`);
              });
              console.log(`💡 Consider updating APPWRITE_CONFIG.databaseId to use one of these existing databases.`);
            } catch (listError) {
              console.log(`⚠️ Could not list existing databases: ${listError.message}`);
            }
            // Continue with collection creation - the specified database might exist
          } else {
            throw createError;
          }
        }
      } else {
        throw error;
      }
    }
    
    // Create all collections
    const collectionIds = [];
    const collectionNames = Object.keys(COLLECTIONS_SCHEMA);
    
    console.log(`\\n📋 Creating ${collectionNames.length} collections...`);
    
    for (const collectionName of collectionNames) {
      try {
        const collectionId = await createCollection(COLLECTIONS_SCHEMA[collectionName]);
        collectionIds.push(collectionId);
      } catch (error) {
        console.error(`Failed to create collection ${collectionName}:`, error.message);
        // Continue with other collections
      }
    }
    
    console.log('\\n' + '='.repeat(60));
    console.log('🎉 Appwrite Collections Setup Complete!');
    console.log(`✅ Successfully created/updated ${collectionIds.length} collections`);
    
    console.log('\\n📋 Created Collections:');
    collectionIds.forEach(id => console.log(`  • ${id}`));
    
    console.log('\\n🔧 Next Steps:');
    console.log('1. Update migrate-data.js with these collection IDs:');
    collectionIds.forEach(id => {
      console.log(`   ${id}: '${id}',`);
    });
    console.log('2. Run migration: pnpm run migrate-data');
    console.log('3. Test your app: pnpm start');
    
  } catch (error) {
    console.error('\\n❌ Setup failed:', error.message);
    console.log('\\n🛠 Troubleshooting:');
    console.log('1. Check your Appwrite project ID');
    console.log('2. Verify you have admin permissions');
    console.log('3. Ensure your internet connection is stable');
  }
}

// Execute the setup
if (require.main === module) {
  setupAppwriteCollections().catch(console.error);
}