// Appwrite Configuration
// 
// This file contains all Appwrite-specific configuration
// and SDK initialization

import { Client, Account, Databases } from 'appwrite';

// ⚠️ IMPORTANT: Replace 'YOUR_PROJECT_ID' with your actual Appwrite project ID
const PROJECT_ID = '68c2dd6e002112935ed2';

// Project ID is configured correctly

const client = new Client();

client
  .setEndpoint('https://fra.cloud.appwrite.io/v1') // Frankfurt region endpoint
  .setProject(PROJECT_ID);                        // Your project ID

export const account = new Account(client);
export const databases = new Databases(client);

export const appwriteConfig = {
  endpoint: 'https://fra.cloud.appwrite.io/v1',
  projectId: PROJECT_ID,
  databaseId: '68c336e7000f87296feb', // Your existing database ID
  collections: {
    // Collection IDs from automated setup
    users: 'users',
    userProfiles: 'user_profiles',
    miningSessions: 'mining_sessions',
    socialTasks: 'social_tasks',
    userSocialTasks: 'user_social_tasks',
    achievements: 'achievements',
    userAchievements: 'user_achievements',
    presalePurchases: 'presale_purchases',
    adViews: 'ad_views'
  },
  // OAuth configuration - your actual Google Client IDs
  oauth: {
    google: {
      // Your actual Google OAuth Client IDs from Google Cloud Console
      webClientId: '842046112756-rk2jcdf9l7f4cbh136u4tm591qmtl6bq.apps.googleusercontent.com',
      androidClientId: '842046112756-noh6rsvng9q3plh8snivdcqlmkmg3osk.apps.googleusercontent.com', 
      iosClientId: '842046112756-qsd86lg9s040qg0g7m9rtfk65kugijcu.apps.googleusercontent.com'
    }
  }
};