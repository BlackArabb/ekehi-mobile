// Import the configuration
const { API_CONFIG } = require('../src/config/api');

// Test the configuration
console.log('API Configuration:');
console.log('- Endpoint:', API_CONFIG.APPWRITE_ENDPOINT);
console.log('- Project ID:', API_CONFIG.APPWRITE_PROJECT_ID);
console.log('- Database ID:', API_CONFIG.DATABASE_ID);
console.log('- Social Tasks Collection:', API_CONFIG.COLLECTIONS.SOCIAL_TASKS);

// Test environment variables
console.log('\nEnvironment Variables:');
console.log('- APPWRITE_ENDPOINT:', process.env.APPWRITE_ENDPOINT);
console.log('- APPWRITE_PROJECT_ID:', process.env.APPWRITE_PROJECT_ID);
console.log('- DATABASE_ID:', process.env.DATABASE_ID);
console.log('- COLLECTION_SOCIAL_TASKS:', process.env.COLLECTION_SOCIAL_TASKS);