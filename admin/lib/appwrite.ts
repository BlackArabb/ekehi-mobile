import { Client, Databases, Account } from 'node-appwrite';
import { API_CONFIG } from '@/src/config/api';

// Appwrite server configuration
const client = new Client()
  .setEndpoint(API_CONFIG.APPWRITE_ENDPOINT)
  .setProject(API_CONFIG.APPWRITE_PROJECT_ID);

// Set API key if available
if (API_CONFIG.APPWRITE_API_KEY) {
  client.setKey(API_CONFIG.APPWRITE_API_KEY);
}

console.log('[Appwrite] Client configured with:', {
  endpoint: API_CONFIG.APPWRITE_ENDPOINT,
  project: API_CONFIG.APPWRITE_PROJECT_ID,
  hasApiKey: !!API_CONFIG.APPWRITE_API_KEY,
  databaseId: API_CONFIG.DATABASE_ID
});

// Initialize services
export const account = new Account(client);
export const databases = new Databases(client);

// Collection IDs from the main app
export const collections = {
  users: API_CONFIG.COLLECTIONS.USERS,
  userProfiles: API_CONFIG.COLLECTIONS.USER_PROFILES,
  miningSessions: API_CONFIG.COLLECTIONS.MINING_SESSIONS,
  socialTasks: API_CONFIG.COLLECTIONS.SOCIAL_TASKS,
  userSocialTasks: API_CONFIG.COLLECTIONS.USER_SOCIAL_TASKS,
  achievements: API_CONFIG.COLLECTIONS.ACHIEVEMENTS,
  userAchievements: API_CONFIG.COLLECTIONS.USER_ACHIEVEMENTS,
  presalePurchases: API_CONFIG.COLLECTIONS.PRESALE_PURCHASES,
  adViews: API_CONFIG.COLLECTIONS.AD_VIEWS
};

export default { client, account, databases, collections };