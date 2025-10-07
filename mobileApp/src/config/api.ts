// API Configuration
// 
// Migrated to Appwrite - Hybrid Approach
// 1. Appwrite handles authentication and database operations
// 2. Complex business logic consolidated in minimal functions
// 3. See APPWRITE_MIGRATION_GUIDE.md for details

export const API_CONFIG = {
  // Appwrite Configuration
  APPWRITE_ENDPOINT: 'https://fra.cloud.appwrite.io/v1',
  APPWRITE_PROJECT_ID: '68c2dd6e002112935ed2',
  
  // Ekehi Network Blockchain API Configuration
  EKEHI_NETWORK: {
    BASE_URL: 'https://api.ekehi.network/v1',
    API_KEY: 'YOUR_PRODUCTION_API_KEY_HERE',
    CHAIN_ID: 'ekehi-mainnet',
    TOKEN_CONTRACT_ADDRESS: '0x0000000000000000000000000000000000000000' // Replace with actual contract address
  },
  
  // Database Configuration
  DATABASE_ID: '68c336e7000f87296feb',
  
  // Collection IDs (these will be actual collection IDs from Appwrite)
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