// API Configuration for Admin Dashboard
// 
// This file contains all API configuration for the admin dashboard
// It connects to the same Appwrite instance as the mobile app

export const API_CONFIG = {
  // Appwrite Configuration
  APPWRITE_ENDPOINT: process.env.APPWRITE_ENDPOINT || 'https://fra.cloud.appwrite.io/v1',
  APPWRITE_PROJECT_ID: process.env.APPWRITE_PROJECT_ID || '68c2dd6e002112935ed2',
  APPWRITE_API_KEY: process.env.APPWRITE_API_KEY,
  
  // Database Configuration
  DATABASE_ID: process.env.DATABASE_ID || '68c336e7000f87296feb',
  
  // Collection IDs (matching the mobile app)
  COLLECTIONS: {
    USERS: 'users',
    USER_PROFILES: 'user_profiles',
    MINING_SESSIONS: 'mining_sessions',
    SOCIAL_TASKS: 'social_tasks',
    USER_SOCIAL_TASKS: 'user_social_tasks',
    ACHIEVEMENTS: 'achievements',
    USER_ACHIEVEMENTS: 'user_achievements',
    PRESALE_PURCHASES: 'presale_purchases',
    AD_VIEWS: 'ad_views'
  },
  
  // Authentication Methods
  AUTH_METHODS: {
    GOOGLE: 'google',
    EMAIL: 'email'
  }
};

export default API_CONFIG;