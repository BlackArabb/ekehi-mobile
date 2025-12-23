import { Client, Databases } from 'appwrite';
import { API_CONFIG } from '../src/config/api';

async function verifySocialTasksIntegration() {
  console.log('Verifying Social Tasks Integration between Admin Panel and KtMobileApp...');
  
  // Initialize Appwrite client
  const client = new Client();
  client
    .setEndpoint(API_CONFIG.APPWRITE_ENDPOINT)
    .setProject(API_CONFIG.APPWRITE_PROJECT_ID);
    
  const databases = new Databases(client);
  
  try {
    // Fetch social tasks from Appwrite
    const response = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      API_CONFIG.COLLECTIONS.SOCIAL_TASKS
    );
    
    console.log(`Found ${response.total} social tasks in the database.`);
    
    // Verify the structure of the tasks
    if (response.documents.length > 0) {
      const sampleTask = response.documents[0];
      
      console.log('\nSample Task Structure:');
      console.log('- ID:', sampleTask.$id);
      console.log('- Title:', sampleTask.title);
      console.log('- Description:', sampleTask.description);
      console.log('- Platform:', sampleTask.platform);
      console.log('- Task Type:', sampleTask.taskType || 'Not set');
      console.log('- Reward Coins:', sampleTask.rewardCoins || 'Not set');
      console.log('- Action URL:', sampleTask.actionUrl || 'Not set');
      console.log('- Verification Method:', sampleTask.verificationMethod || 'Not set');
      console.log('- Is Active:', sampleTask.isActive !== undefined ? sampleTask.isActive : 'Not set');
      console.log('- Sort Order:', sampleTask.sortOrder || 'Not set');
      console.log('- Created At:', sampleTask.$createdAt);
      console.log('- Updated At:', sampleTask.$updatedAt);
      
      // Check if all required fields for KtMobileApp are present
      const requiredFields = ['title', 'description', 'platform', 'rewardCoins', 'isActive'];
      const missingFields = requiredFields.filter(field => sampleTask[field] === undefined);
      
      if (missingFields.length === 0) {
        console.log('\n✅ All required fields for KtMobileApp are present.');
      } else {
        console.log('\n❌ Missing required fields for KtMobileApp:', missingFields);
      }
      
      // Check if extended fields for advanced features are present
      const extendedFields = ['taskType', 'actionUrl', 'verificationMethod', 'sortOrder'];
      const presentExtendedFields = extendedFields.filter(field => sampleTask[field] !== undefined);
      
      if (presentExtendedFields.length > 0) {
        console.log('\n✅ Extended fields available:', presentExtendedFields);
      }
    }
    
    console.log('\n✅ Social Tasks Integration Verification Complete.');
    
  } catch (error) {
    console.error('❌ Error verifying social tasks integration:', error);
  }
}

// Run the verification
verifySocialTasksIntegration();