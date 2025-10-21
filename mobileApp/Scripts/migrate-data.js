// Data Migration Script
// 
// This script helps migrate data from Cloudflare D1 to Appwrite Database
// 
// Prerequisites:
// 1. Set up Appwrite project and collections
// 2. Replace YOUR_PROJECT_ID and collection IDs below
// 3. Export data from Cloudflare: pnpm run export-cloudflare-data
// 
// Usage:
// pnpm run migrate-data

const { Client, Databases, ID } = require('node-appwrite');
const fs = require('fs');
const path = require('path');

// ⚠️ IMPORTANT: Replace these with your actual Appwrite values
const APPWRITE_CONFIG = {
  endpoint: 'https://fra.cloud.appwrite.io/v1',
  projectId: '68c2dd6e002112935ed2', // Your project ID
  databaseId: '68c336e7000f87296feb', // Your existing database ID
  apiKey: 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d', // API key from setup script
  collections: {
    users: 'users',                    // Collection IDs match their names
    userProfiles: 'user_profiles',     // from the setup script
    miningSessions: 'mining_sessions', 
    socialTasks: 'social_tasks',       
    userSocialTasks: 'user_social_tasks', 
    achievements: 'achievements',      
    userAchievements: 'user_achievements', 
    presalePurchases: 'presale_purchases', 
    adViews: 'ad_views'               
  }
};

// Data directory
const JSON_DATA_DIR = './cloudflare-json-export';

// Appwrite Client Setup
const client = new Client();
client
  .setEndpoint(APPWRITE_CONFIG.endpoint)
  .setProject(APPWRITE_CONFIG.projectId)
  .setKey(APPWRITE_CONFIG.apiKey); // Add API key for server-side operations

const databases = new Databases(client);

// Validation function
function validateConfiguration() {
  if (APPWRITE_CONFIG.projectId === 'YOUR_PROJECT_ID') {
    console.error('❌ Error: Please replace YOUR_PROJECT_ID with your actual Appwrite project ID');
    return false;
  }
  
  const hasPlaceholderCollections = Object.values(APPWRITE_CONFIG.collections)
    .some(id => id.includes('collection_id'));
  
  if (hasPlaceholderCollections) {
    console.error('❌ Error: Please replace collection ID placeholders with actual collection IDs from Appwrite dashboard');
    return false;
  }
  
  if (!fs.existsSync(JSON_DATA_DIR)) {
    console.error(`❌ Error: JSON data directory not found: ${JSON_DATA_DIR}`);
    console.error('Please run "pnpm run export-cloudflare-data" first');
    return false;
  }
  
  return true;
}

// Helper function to safely read JSON file
function readJSONFile(filename) {
  const filePath = path.join(JSON_DATA_DIR, filename);
  
  if (!fs.existsSync(filePath)) {
    console.log(`⚠️ Warning: ${filename} not found, skipping...`);
    return [];
  }
  
  try {
    const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));
    console.log(`📂 Loaded ${data.length} records from ${filename}`);
    return data;
  } catch (error) {
    console.error(`❌ Error reading ${filename}:`, error);
    return [];
  }
}

// Helper function to create document with error handling
async function createDocumentSafely(collectionId, data, itemName) {
  try {
    await databases.createDocument(
      APPWRITE_CONFIG.databaseId,
      collectionId,
      ID.unique(),
      data
    );
    return true;
  } catch (error) {
    console.error(`❌ Failed to create ${itemName}:`, error.message);
    return false;
  }
}

// Migration Functions
async function migrateUsers() {
  console.log('\n🚀 Migrating users...');
  const cloudflareData = readJSONFile('users.json');
  
  if (cloudflareData.length === 0) {
    console.log('ℹ️  No users to migrate (using Appwrite Auth instead)');
    return;
  }
  
  console.log('ℹ️  Note: Users will be managed by Appwrite Auth system');
  console.log('ℹ️  Consider creating accounts via Appwrite dashboard or registration flow');
  
  // If you really need a custom users collection, uncomment below:
  /*
  let successCount = 0;
  
  for (const user of cloudflareData) {
    const userData = {
      email: user.email,  // No unique constraint needed
      name: user.name || user.username || 'Unknown User',
      createdAt: user.created_at || new Date().toISOString(),
      lastLogin: user.last_login || null
    };
    
    const success = await createDocumentSafely(
      APPWRITE_CONFIG.collections.users,
      userData,
      `user ${user.email}`
    );
    
    if (success) successCount++;
  }
  
  console.log(`✅ Users migration completed: ${successCount}/${cloudflareData.length} successful`);
  */
}

async function migrateUserProfiles() {
  console.log('\n🚀 Migrating user profiles...');
  const cloudflareData = readJSONFile('user_profiles.json');
  
  if (cloudflareData.length === 0) return;
  
  let successCount = 0;
  
  for (const profile of cloudflareData) {
    const profileData = {
      userId: profile.user_id,
      username: profile.username || null,
      totalCoins: profile.total_coins || 0,
      coinsPerSecond: profile.coins_per_second || 0,
      autoMiningRate: profile.auto_mining_rate || 0, // New field for auto mining rate
      miningPower: profile.mining_power || 1,
      referralBonusRate: profile.referral_bonus_rate || 0, // New field for referral bonus
      currentStreak: profile.current_streak || 0,
      longestStreak: profile.longest_streak || 0,
      lastLoginDate: profile.last_login_date || null,
      referralCode: profile.referral_code || null,
      referredBy: profile.referred_by || null,
      totalReferrals: profile.total_referrals || 0,
      lifetimeEarnings: profile.lifetime_earnings || 0,
      dailyMiningRate: profile.daily_mining_rate || 2, // Updated to 2 EKH per 24-hour session
      maxDailyEarnings: profile.max_daily_earnings || 10000,
      todayEarnings: profile.today_earnings || 0,
      lastMiningDate: profile.last_mining_date || null,
      streakBonusClaimed: profile.streak_bonus_claimed || 0,
      createdAt: profile.created_at || new Date().toISOString(),
      updatedAt: profile.updated_at || new Date().toISOString()
    };
    
    const success = await createDocumentSafely(
      APPWRITE_CONFIG.collections.userProfiles,
      profileData,
      `profile for user ${profile.user_id}`
    );
    
    if (success) successCount++;
  }
  
  console.log(`✅ User profiles migration completed: ${successCount}/${cloudflareData.length} successful`);
}

async function migrateSocialTasks() {
  console.log('\n🚀 Migrating social tasks...');
  const cloudflareData = readJSONFile('social_tasks.json');
  
  if (cloudflareData.length === 0) return;
  
  let successCount = 0;
  
  for (const task of cloudflareData) {
    const taskData = {
      title: task.title,
      description: task.description,
      platform: task.platform,
      taskType: task.task_type || task.taskType,
      rewardCoins: task.reward_coins || task.rewardCoins || 0,
      actionUrl: task.action_url || task.actionUrl || null,
      verificationMethod: task.verification_method || task.verificationMethod,
      isActive: task.is_active !== undefined ? task.is_active : task.isActive !== undefined ? task.isActive : true,
      sortOrder: task.sort_order || task.sortOrder || 0,
      createdAt: task.created_at || task.createdAt || new Date().toISOString()
    };
    
    const success = await createDocumentSafely(
      APPWRITE_CONFIG.collections.socialTasks,
      taskData,
      `social task ${task.title}`
    );
    
    if (success) successCount++;
  }
  
  console.log(`✅ Social tasks migration completed: ${successCount}/${cloudflareData.length} successful`);
}

async function migrateAchievements() {
  console.log('\n🚀 Migrating achievements...');
  const cloudflareData = readJSONFile('achievements.json');
  
  if (cloudflareData.length === 0) return;
  
  let successCount = 0;
  
  for (const achievement of cloudflareData) {
    const achievementData = {
      achievementId: achievement.achievement_id || achievement.achievementId,
      title: achievement.title,
      description: achievement.description,
      type: achievement.type,
      target: achievement.target || 0,
      reward: achievement.reward || 0,
      rarity: achievement.rarity || 'common',
      isActive: achievement.is_active !== undefined ? achievement.is_active : achievement.isActive !== undefined ? achievement.isActive : true,
      createdAt: achievement.created_at || achievement.createdAt || new Date().toISOString()
    };
    
    const success = await createDocumentSafely(
      APPWRITE_CONFIG.collections.achievements,
      achievementData,
      `achievement ${achievement.title}`
    );
    
    if (success) successCount++;
  }
  
  console.log(`✅ Achievements migration completed: ${successCount}/${cloudflareData.length} successful`);
}

// Additional migration functions for other collections
async function migrateMiningSessions() {
  console.log('\n🚀 Migrating mining sessions...');
  const cloudflareData = readJSONFile('mining_sessions.json');
  
  if (cloudflareData.length === 0) return;
  
  let successCount = 0;
  
  for (const session of cloudflareData) {
    const sessionData = {
      userId: session.user_id || session.userId,
      coinsEarned: session.coins_earned || session.coinsEarned || 0,
      clicksMade: session.clicks_made || session.clicksMade || 0,
      sessionDuration: session.session_duration || session.sessionDuration || 0,
      createdAt: session.created_at || session.createdAt || new Date().toISOString()
    };
    
    const success = await createDocumentSafely(
      APPWRITE_CONFIG.collections.miningSessions,
      sessionData,
      `mining session for user ${session.user_id || session.userId}`
    );
    
    if (success) successCount++;
  }
  
  console.log(`✅ Mining sessions migration completed: ${successCount}/${cloudflareData.length} successful`);
}

async function migratePresalePurchases() {
  console.log('\n🚀 Migrating presale purchases...');
  const cloudflareData = readJSONFile('presale_purchases.json');
  
  if (cloudflareData.length === 0) return;
  
  let successCount = 0;
  
  for (const purchase of cloudflareData) {
    const purchaseData = {
      userId: purchase.user_id || purchase.userId,
      amountUsd: purchase.amount_usd || purchase.amountUsd || 0,
      tokensAmount: purchase.tokens_amount || purchase.tokensAmount || 0,
      transactionHash: purchase.transaction_hash || purchase.transactionHash || null,
      status: purchase.status || 'pending',
      paymentMethod: purchase.payment_method || purchase.paymentMethod || null,
      createdAt: purchase.created_at || purchase.createdAt || new Date().toISOString()
    };
    
    const success = await createDocumentSafely(
      APPWRITE_CONFIG.collections.presalePurchases,
      purchaseData,
      `presale purchase for user ${purchase.user_id || purchase.userId}`
    );
    
    if (success) successCount++;
  }
  
  console.log(`✅ Presale purchases migration completed: ${successCount}/${cloudflareData.length} successful`);
}

// Run migrations
async function runMigrations() {
  console.log('🚀 Starting data migration from Cloudflare D1 to Appwrite...');
  console.log('='.repeat(60));
  
  // Validate configuration
  if (!validateConfiguration()) {
    console.log('\n🛠 Configuration Guide:');
    console.log('1. Replace YOUR_PROJECT_ID in this file with your Appwrite project ID');
    console.log('2. Replace all collection_id placeholders with actual collection IDs');
    console.log('3. Run "pnpm run export-cloudflare-data" to export your Cloudflare data');
    return;
  }
  
  console.log('✅ Configuration validated');
  console.log(`📂 Using data from: ${JSON_DATA_DIR}`);
  console.log(`📡 Appwrite endpoint: ${APPWRITE_CONFIG.endpoint}`);
  console.log(`🎯 Appwrite project: ${APPWRITE_CONFIG.projectId}`);
  
  try {
    // Run all migrations
    await migrateUsers();
    await migrateUserProfiles();
    await migrateMiningSessions();
    await migrateSocialTasks();
    await migrateAchievements();
    await migratePresalePurchases();
    
    console.log('\n' + '='.repeat(60));
    console.log('✅ All migrations completed successfully!');
    console.log('🚀 Your Cloudflare data has been migrated to Appwrite!');
    console.log('\n📝 Next steps:');
    console.log('1. Check your Appwrite dashboard to verify data');
    console.log('2. Test your app to ensure everything works correctly');
    console.log('3. Update your app configuration if needed');
    
  } catch (error) {
    console.error('\n❌ Migration failed:', error);
    console.log('\n🛠 Troubleshooting:');
    console.log('1. Check your Appwrite project ID and collection IDs');
    console.log('2. Verify your Appwrite project permissions');
    console.log('3. Ensure your collections have the correct attributes');
  }
}

// Execute migrations
if (require.main === module) {
  runMigrations().catch(console.error);
}