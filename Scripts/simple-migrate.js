// Simple Data Migration - Only Using Existing Attributes
const { Client, Databases, ID } = require('node-appwrite');

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

// Simplified data using only existing attributes
const migrationData = {
  userProfiles: [
    {
      userId: '111503823123987272638',
      username: 'user1'
    },
    {
      userId: '100918368155980132753', 
      username: 'ekehi_official'
    }
  ],
  
  socialTasks: [
    {
      title: 'Follow on Twitter',
      description: 'Follow our official Twitter account',
      platform: 'Twitter',
      taskType: 'follow',
      rewardCoins: 50,
      actionUrl: 'https://twitter.com/ekehi_network',
      verificationMethod: 'manual'
    },
    {
      title: 'Join Telegram',
      description: 'Join our Telegram community',
      platform: 'Telegram',
      taskType: 'join',
      rewardCoins: 75,
      actionUrl: 'https://t.me/ekehi_network',
      verificationMethod: 'manual'
    },
    {
      title: 'Subscribe on YouTube',
      description: 'Subscribe to our YouTube channel',
      platform: 'YouTube',
      taskType: 'subscribe',
      rewardCoins: 100,
      actionUrl: 'https://youtube.com/@ekehi_network',
      verificationMethod: 'manual'
    },
    {
      title: 'Share on Facebook',
      description: 'Share our latest post on Facebook',
      platform: 'Facebook',
      taskType: 'share',
      rewardCoins: 150,
      actionUrl: 'https://facebook.com/ekehi_network',
      verificationMethod: 'manual'
    }
  ],
  
  achievements: [
    {
      achievementId: 'first_mine',
      title: 'First Mine',
      description: 'Complete your first mining action',
      type: 'total_coins',
      target: 1,
      reward: 10,
      rarity: 'common'
    },
    {
      achievementId: 'hundred_coins',
      title: 'Hundred Club',
      description: 'Mine 100 EKH tokens',
      type: 'total_coins',
      target: 100,
      reward: 25,
      rarity: 'common'
    },
    {
      achievementId: 'thousand_coins',
      title: 'Thousand Club',
      description: 'Mine 1,000 EKH tokens',
      type: 'total_coins',
      target: 1000,
      reward: 100,
      rarity: 'common'
    },
    {
      achievementId: 'ten_thousand_coins',
      title: 'Ten Thousand Club',
      description: 'Mine 10,000 EKH tokens',
      type: 'total_coins',
      target: 10000,
      reward: 500,
      rarity: 'rare'
    },
    {
      achievementId: 'first_streak',
      title: 'First Streak',
      description: 'Maintain a 1-day mining streak',
      type: 'mining_streak',
      target: 1,
      reward: 20,
      rarity: 'common'
    }
  ]
};

async function simpleMigration() {
  console.log('🚀 Starting simple data migration...');
  console.log('============================================================');
  
  try {
    // Migrate User Profiles
    console.log('\\n📝 Migrating user profiles (basic data)...');
    let profileCount = 0;
    for (const profile of migrationData.userProfiles) {
      try {
        await databases.createDocument(
          APPWRITE_CONFIG.databaseId,
          'user_profiles',
          ID.unique(),
          profile
        );
        profileCount++;
        console.log(`✅ Created profile: ${profile.username}`);
      } catch (error) {
        console.error(`❌ Failed to create profile ${profile.username}:`, error.message);
      }
    }
    
    // Migrate Social Tasks
    console.log('\\n📝 Migrating social tasks (basic data)...');
    let taskCount = 0;
    for (const task of migrationData.socialTasks) {
      try {
        await databases.createDocument(
          APPWRITE_CONFIG.databaseId,
          'social_tasks',
          ID.unique(),
          task
        );
        taskCount++;
        console.log(`✅ Created task: ${task.title}`);
      } catch (error) {
        console.error(`❌ Failed to create task ${task.title}:`, error.message);
      }
    }
    
    // Migrate Achievements
    console.log('\\n📝 Migrating achievements (basic data)...');
    let achievementCount = 0;
    for (const achievement of migrationData.achievements) {
      try {
        await databases.createDocument(
          APPWRITE_CONFIG.databaseId,
          'achievements',
          ID.unique(),
          achievement
        );
        achievementCount++;
        console.log(`✅ Created achievement: ${achievement.title}`);
      } catch (error) {
        console.error(`❌ Failed to create achievement ${achievement.title}:`, error.message);
      }
    }
    
    console.log('\\n' + '='.repeat(60));
    console.log('🎉 Basic migration completed!');
    console.log(`📊 Migrated: ${profileCount} profiles, ${taskCount} tasks, ${achievementCount} achievements`);
    console.log('\\n📋 Next steps:');
    console.log('1. Check your Appwrite dashboard');
    console.log('2. Test your app: pnpm start');
    console.log('3. Add missing attributes later as needed');
    
  } catch (error) {
    console.error('❌ Migration failed:', error.message);
  }
}

simpleMigration().catch(console.error);