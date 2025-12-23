// Script to create user_social_tasks collection with correct attributes
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

async function createUserSocialTasksCollection() {
  console.log('🚀 Creating user_social_tasks collection...');
  
  try {
    // Create the collection
    console.log('Creating collection...');
    await databases.createCollection(
      APPWRITE_CONFIG.databaseId,
      'user_social_tasks', // collectionId
      'user_social_tasks', // name
      [
        Permission.read(Role.users()),
        Permission.create(Role.users()),
        Permission.update(Role.users()),
        Permission.delete(Role.users())
      ]
    );
    console.log('✅ Collection created successfully');
  } catch (error) {
    if (error.message.includes('already exists')) {
      console.log('⚠️ Collection already exists, continuing...');
    } else {
      console.error('❌ Failed to create collection:', error.message);
      return;
    }
  }
  
  // Add attributes one by one
  try {
    console.log('Adding attributes...');
    
    // userId attribute
    try {
      await databases.createStringAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'userId',
        255,
        true // required
      );
      console.log('✅ Added userId attribute');
    } catch (error) {
      if (!error.message.includes('already exists')) {
        console.error('❌ Failed to add userId attribute:', error.message);
      }
    }
    
    // taskId attribute
    try {
      await databases.createStringAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'taskId',
        255,
        true // required
      );
      console.log('✅ Added taskId attribute');
    } catch (error) {
      if (!error.message.includes('already exists')) {
        console.error('❌ Failed to add taskId attribute:', error.message);
      }
    }
    
    // completedAt attribute
    try {
      await databases.createDatetimeAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'completedAt',
        true // required
      );
      console.log('✅ Added completedAt attribute');
    } catch (error) {
      if (!error.message.includes('already exists')) {
        console.error('❌ Failed to add completedAt attribute:', error.message);
      }
    }
    
    // username attribute
    try {
      await databases.createStringAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'username',
        255,
        false // not required
      );
      console.log('✅ Added username attribute');
    } catch (error) {
      if (!error.message.includes('already exists')) {
        console.error('❌ Failed to add username attribute:', error.message);
      }
    }
    
    // telegram_user_id attribute
    try {
      await databases.createIntegerAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'telegram_user_id',
        false // not required
      );
      console.log('✅ Added telegram_user_id attribute');
    } catch (error) {
      if (!error.message.includes('already exists')) {
        console.error('❌ Failed to add telegram_user_id attribute:', error.message);
      }
    }
    
    // status attribute
    try {
      await databases.createStringAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'status',
        50,
        false // not required
      );
      console.log('✅ Added status attribute');
    } catch (error) {
      if (!error.message.includes('already exists')) {
        console.error('❌ Failed to add status attribute:', error.message);
      }
    }
    
    // verificationAttempts attribute
    try {
      await databases.createIntegerAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'verificationAttempts',
        false, // not required
        0, // min
        null, // no max
        0 // default
      );
      console.log('✅ Added verificationAttempts attribute');
    } catch (error) {
      if (!error.message.includes('already exists')) {
        console.error('❌ Failed to add verificationAttempts attribute:', error.message);
      }
    }
    
    // verifiedAt attribute
    try {
      await databases.createDatetimeAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'verifiedAt',
        false // not required
      );
      console.log('✅ Added verifiedAt attribute');
    } catch (error) {
      if (!error.message.includes('already exists')) {
        console.error('❌ Failed to add verifiedAt attribute:', error.message);
      }
    }
    
    // rejectionReason attribute
    try {
      await databases.createStringAttribute(
        APPWRITE_CONFIG.databaseId,
        'user_social_tasks',
        'rejectionReason',
        1000,
        false // not required
      );
      console.log('✅ Added rejectionReason attribute');
    } catch (error) {
      if (!error.message.includes('already exists')) {
        console.error('❌ Failed to add rejectionReason attribute:', error.message);
      }
    }
    
    console.log('🎉 All attributes added successfully!');
    
  } catch (error) {
    console.error('❌ Failed to add attributes:', error.message);
    return;
  }
  
  console.log('\n✅ user_social_tasks collection setup complete!');
  console.log('\n📝 Next steps:');
  console.log('1. Run the quick-update-telegram-field.js script to migrate existing data');
  console.log('2. Create a unique index in Appwrite Console:');
  console.log('   - Go to Database → user_social_tasks → Indexes');
  console.log('   - Add Index with attributes: taskId (asc), telegram_user_id (asc)');
  console.log('   - Set type to Unique');
}

createUserSocialTasksCollection().catch(console.error);