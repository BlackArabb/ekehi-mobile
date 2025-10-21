// Sample Data Generation Script
//
// This script generates sample data for testing the Appwrite collections
//
// Usage:
// node Scripts/generate-sample-data.js

const fs = require('fs');
const path = require('path');

// Create the data directory if it doesn't exist
const DATA_DIR = './cloudflare-json-export';
if (!fs.existsSync(DATA_DIR)) {
  fs.mkdirSync(DATA_DIR, { recursive: true });
}

// Generate sample users
function generateUsers(count = 5) {
  const users = [];
  const names = ['Alice Johnson', 'Bob Smith', 'Charlie Brown', 'Diana Prince', 'Edward Norton'];
  const emails = ['alice@example.com', 'bob@example.com', 'charlie@example.com', 'diana@example.com', 'edward@example.com'];
  
  for (let i = 0; i < count; i++) {
    users.push({
      id: `user_${i + 1}`,
      email: emails[i],
      name: names[i],
      created_at: new Date(Date.now() - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000)).toISOString(),
      last_login: new Date(Date.now() - Math.floor(Math.random() * 7 * 24 * 60 * 60 * 1000)).toISOString()
    });
  }
  
  return users;
}

// Generate sample user profiles
function generateUserProfiles(users, count = 5) {
  const profiles = [];
  
  for (let i = 0; i < count; i++) {
    const userId = users[i].id;
    profiles.push({
      id: `profile_${i + 1}`,
      user_id: userId,
      username: users[i].name.split(' ')[0].toLowerCase(),
      total_coins: Math.floor(Math.random() * 10000) / 100,
      coins_per_second: 0, // Deprecated
      auto_mining_rate: Math.random() * 0.5, // New field
      mining_power: 1 + Math.floor(Math.random() * 5),
      referral_bonus_rate: Math.random() * 0.1, // New field
      current_streak: Math.floor(Math.random() * 30),
      longest_streak: Math.floor(Math.random() * 60),
      last_login_date: new Date(Date.now() - Math.floor(Math.random() * 7 * 24 * 60 * 60 * 1000)).toISOString(),
      referral_code: `REF${Math.floor(1000 + Math.random() * 9000)}`,
      referred_by: i > 0 ? users[Math.floor(Math.random() * i)].id : null,
      total_referrals: Math.floor(Math.random() * 20),
      lifetime_earnings: Math.floor(Math.random() * 5000) / 100,
      daily_mining_rate: 2, // Updated to 2 EKH per 24-hour session
      max_daily_earnings: 10000,
      today_earnings: Math.floor(Math.random() * 100) / 100,
      last_mining_date: new Date(Date.now() - Math.floor(Math.random() * 24 * 60 * 60 * 1000)).toISOString(),
      streak_bonus_claimed: Math.floor(Math.random() * 2),
      created_at: new Date(Date.now() - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000)).toISOString(),
      updated_at: new Date().toISOString()
    });
  }
  
  return profiles;
}

// Generate sample social tasks
function generateSocialTasks(count = 10) {
  const tasks = [];
  const platforms = ['Twitter', 'Facebook', 'Instagram', 'YouTube', 'TikTok'];
  const taskTypes = ['Follow', 'Like', 'Share', 'Comment', 'Subscribe'];
  const descriptions = [
    'Follow our official account',
    'Like our latest post',
    'Share our content with your friends',
    'Comment on our latest post',
    'Subscribe to our channel'
  ];
  
  for (let i = 0; i < count; i++) {
    tasks.push({
      id: `task_${i + 1}`,
      title: `${taskTypes[i % taskTypes.length]} on ${platforms[i % platforms.length]}`,
      description: descriptions[i % descriptions.length],
      platform: platforms[i % platforms.length],
      task_type: taskTypes[i % taskTypes.length],
      reward_coins: 0.5 + Math.floor(Math.random() * 10) / 10,
      action_url: `https://${platforms[i % platforms.length].toLowerCase()}.com/ekehi`,
      verification_method: 'manual',
      is_active: Math.random() > 0.2,
      sort_order: i,
      created_at: new Date(Date.now() - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000)).toISOString(),
      updated_at: new Date().toISOString()
    });
  }
  
  return tasks;
}

// Generate sample achievements
function generateAchievements(count = 8) {
  const achievements = [];
  const types = ['mining', 'referral', 'social', 'streak'];
  const rarities = ['common', 'uncommon', 'rare', 'epic', 'legendary'];
  const titles = [
    'First Mining Session',
    'Streak Master',
    'Referral King',
    'Social Butterfly',
    'Daily Miner',
    'Weekend Warrior',
    'Month Master',
    'Achievement Hunter'
  ];
  const descriptions = [
    'Complete your first mining session',
    'Maintain a 30-day streak',
    'Refer 10 friends',
    'Complete 5 social tasks',
    'Mine for 7 consecutive days',
    'Mine on weekends for a month',
    'Mine every day for a month',
    'Unlock 10 achievements'
  ];
  
  for (let i = 0; i < count; i++) {
    achievements.push({
      id: `achievement_${i + 1}`,
      achievement_id: `ach_${i + 1}`,
      title: titles[i],
      description: descriptions[i],
      type: types[i % types.length],
      target: 1 + Math.floor(Math.random() * 100),
      reward: 1 + Math.floor(Math.random() * 10),
      rarity: rarities[Math.floor(Math.random() * rarities.length)],
      is_active: true,
      created_at: new Date(Date.now() - Math.floor(Math.random() * 30 * 24 * 60 * 60 * 1000)).toISOString(),
      updated_at: new Date().toISOString()
    });
  }
  
  return achievements;
}

// Generate sample mining sessions
function generateMiningSessions(users, count = 20) {
  const sessions = [];
  
  for (let i = 0; i < count; i++) {
    const user = users[Math.floor(Math.random() * users.length)];
    sessions.push({
      id: `session_${i + 1}`,
      user_id: user.id,
      coins_earned: Math.floor(Math.random() * 100) / 100,
      clicks_made: 10 + Math.floor(Math.random() * 90),
      session_duration: 300 + Math.floor(Math.random() * 3300), // 5-60 minutes
      created_at: new Date(Date.now() - Math.floor(Math.random() * 7 * 24 * 60 * 60 * 1000)).toISOString(),
      updated_at: new Date().toISOString()
    });
  }
  
  return sessions;
}

// Write data to JSON files
function writeDataToFile(filename, data) {
  const filePath = path.join(DATA_DIR, filename);
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2));
  console.log(`📄 Generated ${data.length} records in ${filename}`);
}

// Main function to generate all sample data
function generateAllSampleData() {
  console.log('🚀 Generating sample data...');
  console.log('='.repeat(40));
  
  try {
    // Generate users
    const users = generateUsers(5);
    writeDataToFile('users.json', users);
    
    // Generate user profiles
    const profiles = generateUserProfiles(users, 5);
    writeDataToFile('user_profiles.json', profiles);
    
    // Generate social tasks
    const socialTasks = generateSocialTasks(10);
    writeDataToFile('social_tasks.json', socialTasks);
    
    // Generate achievements
    const achievements = generateAchievements(8);
    writeDataToFile('achievements.json', achievements);
    
    // Generate mining sessions
    const miningSessions = generateMiningSessions(users, 20);
    writeDataToFile('mining_sessions.json', miningSessions);
    
    // Generate empty files for other collections
    writeDataToFile('ad_views.json', []);
    writeDataToFile('presale_purchases.json', []);
    writeDataToFile('user_achievements.json', []);
    writeDataToFile('user_social_tasks.json', []);
    
    console.log('\n' + '='.repeat(40));
    console.log('✅ Sample data generation completed!');
    console.log(`📂 Data saved to: ${DATA_DIR}`);
    console.log('\n📝 Next steps:');
    console.log('1. Run the collection update script: node Scripts/update-appwrite-collections.js');
    console.log('2. Run the migration script: pnpm run migrate-data');
    console.log('3. Verify data in your Appwrite dashboard');
  } catch (error) {
    console.error('❌ Sample data generation failed:', error);
  }
}

// Execute the script
if (require.main === module) {
  generateAllSampleData();
}