// Script to migrate existing data to populate telegram_user_id field
const https = require('https');

const PROJECT_ID = '68c2dd6e002112935ed2';
const DATABASE_ID = '68c336e7000f87296feb';
const API_KEY = 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d';

// Function to make HTTP request to Appwrite
function appwriteRequest(path, method, data = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'fra.cloud.appwrite.io',
      port: 443,
      path: `/v1${path}`,
      method: method,
      headers: {
        'X-Appwrite-Response-Format': '1.0.0',
        'X-Appwrite-Project': PROJECT_ID,
        'X-Appwrite-Key': API_KEY,
        'Content-Type': 'application/json'
      }
    };

    if (data) {
      options.headers['Content-Length'] = JSON.stringify(data).length;
    }

    const req = https.request(options, (res) => {
      let responseData = '';
      
      res.on('data', (chunk) => {
        responseData += chunk;
      });
      
      res.on('end', () => {
        try {
          const json = JSON.parse(responseData);
          if (res.statusCode >= 200 && res.statusCode < 300) {
            resolve(json);
          } else {
            reject(new Error(`HTTP ${res.statusCode}: ${json.message || responseData}`));
          }
        } catch (error) {
          // If we can't parse JSON, return raw data for error responses
          if (res.statusCode >= 200 && res.statusCode < 300) {
            reject(new Error(`Failed to parse response: ${responseData}`));
          } else {
            reject(new Error(`HTTP ${res.statusCode}: ${responseData}`));
          }
        }
      });
    });

    req.on('error', (error) => {
      reject(error);
    });

    if (data) {
      req.write(JSON.stringify(data));
    }
    
    req.end();
  });
}

async function migrateTelegramData() {
  console.log('🚀 Migrating existing data to populate telegram_user_id field...');
  
  try {
    // Get all documents from user_social_tasks collection
    console.log('Fetching documents from user_social_tasks collection...');
    
    let offset = 0;
    const limit = 100;
    let totalMigrated = 0;
    let totalErrors = 0;
    
    do {
      const response = await appwriteRequest(
        `/databases/${DATABASE_ID}/collections/user_social_tasks/documents?limit=${limit}&offset=${offset}`,
        'GET'
      );
      
      const { documents, total } = response;
      
      if (documents.length === 0) {
        break;
      }
      
      console.log(`Processing batch of ${documents.length} documents...`);
      
      // Process each document
      for (const doc of documents) {
        try {
          // Check if document needs migration
          // (telegram_user_id is null but proofData contains telegram_user_id)
          if ((doc.telegram_user_id === null || doc.telegram_user_id === undefined) && doc.proofData) {
            let proofDataObj = {};
            
            // Parse proofData if it's a string
            if (typeof doc.proofData === 'string') {
              try {
                proofDataObj = JSON.parse(doc.proofData);
              } catch (parseError) {
                console.log(`   Could not parse proofData for document ${doc.$id}`);
                continue;
              }
            } else if (typeof doc.proofData === 'object') {
              proofDataObj = doc.proofData;
            }
            
            // If we found a telegram_user_id, update the document
            if (proofDataObj.telegram_user_id) {
              const telegramUserId = typeof proofDataObj.telegram_user_id === 'string' 
                ? parseInt(proofDataObj.telegram_user_id, 10) 
                : proofDataObj.telegram_user_id;
              
              if (!isNaN(telegramUserId)) {
                // Prepare update data - include all required fields plus the new telegram_user_id
                const updateData = {
                  data: {
                    userId: doc.userId,
                    taskId: doc.taskId,
                    completedAt: doc.completedAt,
                    telegram_user_id: telegramUserId
                  }
                };
                
                // Add optional fields if they exist
                if (doc.status !== undefined) updateData.data.status = doc.status;
                if (doc.verifiedAt !== undefined) updateData.data.verifiedAt = doc.verifiedAt;
                if (doc.proofUrl !== undefined) updateData.data.proofUrl = doc.proofUrl;
                if (doc.proofData !== undefined) updateData.data.proofData = doc.proofData;
                if (doc.verificationAttempts !== undefined) updateData.data.verificationAttempts = doc.verificationAttempts;
                if (doc.rejectionReason !== undefined) updateData.data.rejectionReason = doc.rejectionReason;
                if (doc.username !== undefined) updateData.data.username = doc.username;
                
                // Update the document
                try {
                  await appwriteRequest(
                    `/databases/${DATABASE_ID}/collections/user_social_tasks/documents/${doc.$id}`,
                    'PUT',
                    updateData
                  );
                  
                  totalMigrated++;
                  
                  // Show progress every 5 updates
                  if (totalMigrated % 5 === 0) {
                    console.log(`   Migrated ${totalMigrated} documents...`);
                  }
                  
                  console.log(`   ✓ Migrated document ${doc.$id}: Set telegram_user_id to ${telegramUserId}`);
                } catch (updateError) {
                  console.log(`   Error updating document ${doc.$id}:`, updateError.message);
                  totalErrors++;
                }
              } else {
                console.log(`   Invalid telegram_user_id in document ${doc.$id}: ${proofDataObj.telegram_user_id}`);
              }
            }
          } else if (doc.telegram_user_id) {
            // telegram_user_id already set
            // console.log(`   Skipping document ${doc.$id}: telegram_user_id already set`);
          } else {
            // No proofData or no telegram_user_id in proofData
            // console.log(`   Skipping document ${doc.$id}: No telegram_user_id in proofData`);
          }
        } catch (docError) {
          totalErrors++;
          if (totalErrors <= 5) { // Only show first 5 errors to avoid spam
            console.log(`   Error processing document ${doc.$id}:`, docError.message);
          }
        }
      }
      
      offset += limit;
      
      // If we've processed all documents, break
      if (offset >= total) {
        break;
      }
      
    } while (true);
    
    console.log(`\n🎉 Data migration complete!`);
    console.log(`   Successfully migrated: ${totalMigrated} documents`);
    if (totalErrors > 0) {
      console.log(`   Errors encountered: ${totalErrors} documents`);
      if (totalErrors > 5) {
        console.log(`   (Only first 5 errors shown)`);
      }
    }
    
    console.log('\n✅ Migration complete!');
    console.log('\n📝 Next step:');
    console.log('Create a unique index in Appwrite Console:');
    console.log('   - Go to Database → user_social_tasks → Indexes');
    console.log('   - Add Index with attributes: taskId (asc), telegram_user_id (asc)');
    console.log('   - Set type to Unique');
    
  } catch (error) {
    console.error('❌ Data migration failed:', error.message);
  }
}

migrateTelegramData().catch(console.error);