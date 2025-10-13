// Simple Data Migration Script
// Direct migration from your Cloudflare data to Appwrite
// This manually maps the known data from your SQL export

const { Client, Databases, ID } = require('node-appwrite');

const APPWRITE_CONFIG = {
  endpoint: 'https://fra.cloud.appwrite.io/v1',
  projectId: '68c2dd6e002112935ed2',
  databaseId: '68c336e7000f87296feb',
  apiKey: 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d',
  collections: {
    userProfiles: 'user_profiles',
    socialTasks: 'social_tasks',
    achievements: 'achievements'
  }
};

// Appwrite Client Setup
const client = new Client();
client
  .setEndpoint(APPWRITE_CONFIG.endpoint)
  .setProject(APPWRITE_CONFIG.projectId)
  .setKey(APPWRITE_CONFIG.apiKey);

const databases = new Databases(client);

// Your actual data from the SQL file
const userData = {
  userProfiles: [
    {
      userId: '111503823123987272638',
      username: null,
      totalCoins: 0,
      coinsPerSecond: 0,
      miningPower: 1,
      currentStreak: 0,
      longestStreak: 0,
      lastLoginDate: null,
      referralCode: null,
      referredBy: null,
      totalReferrals: 0,
      lifetimeEarnings: 0,
      dailyMiningRate: 2, // UPDATED: Now represents 2 EKH per 24-hour session
      maxDailyEarnings: 10000,
      todayEarnings: 0,
      lastMiningDate: null,
      streakBonusClaimed: 0,
      createdAt: '2025-09-08 11:58:26',
      updatedAt: '2025-09-08 11:58:26'
    },
    {
      userId: '100918368155980132753',
      username: null,
      totalCoins: 0,
      coinsPerSecond: 0,
      miningPower: 1,
      currentStreak: 0,
      longestStreak: 0,
      lastLoginDate: null,
      referralCode: null,
      referredBy: null,
      totalReferrals: 0,
      lifetimeEarnings: 0,
      dailyMiningRate: 2, // UPDATED: Now represents 2 EKH per 24-hour session
      maxDailyEarnings: 10000,
      todayEarnings: 0,
      lastMiningDate: null,
      streakBonusClaimed: 0,
      createdAt: '2025-09-08 22:13:18',
      updatedAt: '2025-09-08 22:13:18'
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
      verificationMethod: 'manual',
      isActive: true,
      sortOrder: 1,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      title: 'Join Telegram',
      description: 'Join our Telegram community',
      platform: 'Telegram',
      taskType: 'join',
      rewardCoins: 75,
      actionUrl: 'https://t.me/ekehi_network',
      verificationMethod: 'manual',
      isActive: true,
      sortOrder: 2,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      title: 'Subscribe on YouTube',
      description: 'Subscribe to our YouTube channel',
      platform: 'YouTube',
      taskType: 'subscribe',
      rewardCoins: 100,
      actionUrl: 'https://youtube.com/@ekehi_network',
      verificationMethod: 'manual',
      isActive: true,
      sortOrder: 3,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      title: 'Share on Facebook',
      description: 'Share our latest post on Facebook',
      platform: 'Facebook',
      taskType: 'share',
      rewardCoins: 150,
      actionUrl: 'https://facebook.com/ekehi_network',
      verificationMethod: 'manual',
      isActive: true,
      sortOrder: 4,
      createdAt: '2025-09-07 21:54:57'
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
      rarity: 'common',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      achievementId: 'hundred_coins',
      title: 'Hundred Club',
      description: 'Mine 100 EKH tokens',
      type: 'total_coins',
      target: 100,
      reward: 25,
      rarity: 'common',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      achievementId: 'thousand_coins',
      title: 'Thousand Club',
      description: 'Mine 1,000 EKH tokens',
      type: 'total_coins',
      target: 1000,
      reward: 100,
      rarity: 'common',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      achievementId: 'ten_thousand_coins',
      title: 'Ten Thousand Club',
      description: 'Mine 10,000 EKH tokens',
      type: 'total_coins',
      target: 10000,
      reward: 500,
      rarity: 'rare',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      achievementId: 'first_streak',
      title: 'First Streak',
      description: 'Maintain a 1-day mining streak',
      type: 'mining_streak',
      target: 1,
      reward: 20,
      rarity: 'common',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      achievementId: 'week_streak',
      title: 'Week Streak',
      description: 'Maintain a 7-day mining streak',
      type: 'mining_streak',
      target: 7,
      reward: 150,
      rarity: 'rare',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      achievementId: 'month_streak',
      title: 'Month Streak',
      description: 'Maintain a 30-day mining streak',
      type: 'mining_streak',
      target: 30,
      reward: 1000,
      rarity: 'epic',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      achievementId: 'first_referral',
      title: 'First Referral',
      description: 'Refer your first friend',
      type: 'referrals',
      target: 1,
      reward: 50,
      rarity: 'common',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      achievementId: 'five_referrals',
      title: 'Social Butterfly',
      description: 'Refer 5 friends',
      type: 'referrals',
      target: 5,
      reward: 300,
      rarity: 'rare',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    },
    {
      achievementId: 'ten_referrals',
      title: 'Community Builder',
      description: 'Refer 10 friends',
      type: 'referrals',
      target: 10,
      reward: 1000,
      rarity: 'epic',
      isActive: true,
      createdAt: '2025-09-07 21:54:57'
    }
  ]
};

async function migrateData() {
  console.log('🚀 Starting direct data migration to Appwrite...');
  console.log('============================================================');
  
  try {
    // Migrate User Profiles
    console.log('\\n🚀 Migrating user profiles...');
    let profileCount = 0;
    for (const profile of userData.userProfiles) {
      try {
        await databases.createDocument(
          APPWRITE_CONFIG.databaseId,
          APPWRITE_CONFIG.collections.userProfiles,
          ID.unique(),
          profile
        );
        profileCount++;
        console.log(`✅ Created profile for user: ${profile.userId}`);
      } catch (error) {
        console.error(`❌ Failed to create profile for ${profile.userId}:`, error.message);
      }
    }
    console.log(`✅ User profiles migration completed: ${profileCount}/${userData.userProfiles.length} successful`);
    
    // Migrate Social Tasks
    console.log('\\n🚀 Migrating social tasks...');
    let taskCount = 0;
    for (const task of userData.socialTasks) {
      try {
        await databases.createDocument(
          APPWRITE_CONFIG.databaseId,
          APPWRITE_CONFIG.collections.socialTasks,
          ID.unique(),
          task
        );
        taskCount++;
        console.log(`✅ Created social task: ${task.title}`);
      } catch (error) {
        console.error(`❌ Failed to create task ${task.title}:`, error.message);
      }
    }
    console.log(`✅ Social tasks migration completed: ${taskCount}/${userData.socialTasks.length} successful`);
    
    // Migrate Achievements
    console.log('\\n🚀 Migrating achievements...');
    let achievementCount = 0;
    for (const achievement of userData.achievements) {
      try {
        await databases.createDocument(
          APPWRITE_CONFIG.databaseId,
          APPWRITE_CONFIG.collections.achievements,
          ID.unique(),
          achievement
        );
        achievementCount++;
        console.log(`✅ Created achievement: ${achievement.title}`);
      } catch (error) {
        console.error(`❌ Failed to create achievement ${achievement.title}:`, error.message);
      }
    }
    console.log(`✅ Achievements migration completed: ${achievementCount}/${userData.achievements.length} successful`);
    
    console.log('\\n' + '='.repeat(60));
    console.log('🎉 Data migration completed!');
    console.log(`📊 Total records migrated: ${profileCount + taskCount + achievementCount}`);
    console.log('\\n📋 Next steps:');
    console.log('1. Check your Appwrite dashboard to verify data');
    console.log('2. Test your app: pnpm start');
    
  } catch (error) {
    console.error('❌ Migration failed:', error.message);
  }
}

// Execute migration
migrateData().catch(console.error);