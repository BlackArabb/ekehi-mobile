// Appwrite Configuration
// 
// This file contains all Appwrite-specific configuration
// and SDK initialization

import { Client, Account, Databases } from 'appwrite';

// Use environment variables for production, with fallbacks for development
const PROJECT_ID = process.env.EXPO_PUBLIC_APPWRITE_PROJECT_ID || '68c2dd6e002112935ed2';
const ENDPOINT = process.env.EXPO_PUBLIC_APPWRITE_ENDPOINT || 'https://fra.cloud.appwrite.io/v1';

const client = new Client();

client
  .setEndpoint(ENDPOINT)     // Configurable endpoint for different environments
  .setProject(PROJECT_ID);   // Configurable project ID

export const account = new Account(client);
export const databases = new Databases(client);

export const appwriteConfig = {
  endpoint: ENDPOINT,
  projectId: PROJECT_ID,
  databaseId: process.env.EXPO_PUBLIC_APPWRITE_DATABASE_ID || '68c336e7000f87296feb',
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
      webClientId: process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID || '842046112756-rk2jcdf9l7f4cbh136u4tm591qmtl6bq.apps.googleusercontent.com',
      androidClientId: process.env.EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID || '842046112756-noh6rsvng9q3plh8snivdcqlmkmg3osk.apps.googleusercontent.com', 
      iosClientId: process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID || '842046112756-qsd86lg9s040qg0g7m9rtfk65kugijcu.apps.googleusercontent.com'
    },
    // OAuth redirect URLs
    redirectUrls: {
      success: process.env.EXPO_PUBLIC_OAUTH_SUCCESS_URL || 'ekehi://oauth/return',
      failure: process.env.EXPO_PUBLIC_OAUTH_FAILURE_URL || 'ekehi://auth'
    }
  }
};