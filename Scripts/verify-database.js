// Simple script to verify database connectivity
const { Client, Databases } = require('appwrite');

// Initialize Appwrite client
const client = new Client();
client
  .setEndpoint('https://fra.cloud.appwrite.io/v1')
  .setProject('68c2dd6e002112935ed2');

const databases = new Databases(client);

async function verifyDatabase() {
  console.log('🔍 Verifying database connectivity...');
  
  try {
    // Try to get the database
    const database = await databases.get('68c336e7000f87296feb');
    console.log('✅ Database connection successful!');
    console.log(`📊 Database ID: ${database.$id}`);
    console.log(`📝 Database Name: ${database.name}`);
    console.log(`📅 Created: ${database.$createdAt}`);
    
  } catch (error) {
    console.error('❌ Database connection failed:', error.message);
  }
}

// Run the verification
verifyDatabase();