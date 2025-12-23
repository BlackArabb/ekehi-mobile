// Quick Update Script for Telegram ID Field
// 
// This script adds the telegram_user_id field to user_social_tasks collection
// and updates existing documents to extract Telegram IDs from proofData.

// UPDATE THESE VALUES WITH YOUR ACTUAL APPWRITE CREDENTIALS
const YOUR_ENDPOINT = 'https://fra.cloud.appwrite.io/v1';      // Appwrite endpoint
const YOUR_PROJECT_ID = '68c2dd6e002112935ed2';               // Project ID
const YOUR_DATABASE_ID = '68c336e7000f87296feb';              // Database ID
const YOUR_API_KEY = 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d';                 // API key with database permissions

const { Client, Databases, Query } = require('node-appwrite');

async function updateTelegramField() {
    console.log('🚀 Starting Telegram ID field update...');
    
    // Initialize Appwrite client
    const client = new Client();
    client
        .setEndpoint(YOUR_ENDPOINT)
        .setProject(YOUR_PROJECT_ID)
        .setKey(YOUR_API_KEY);

    const databases = new Databases(client);

    try {
        // Check if collection exists
        try {
            await databases.getCollection(YOUR_DATABASE_ID, 'user_social_tasks');
            console.log('✅ Found user_social_tasks collection');
        } catch (error) {
            console.error('❌ Collection user_social_tasks not found');
            console.error('   Please make sure the collection exists in your database');
            return;
        }

        // Try to add telegram_user_id attribute
        try {
            await databases.createIntegerAttribute(
                YOUR_DATABASE_ID,
                'user_social_tasks',
                'telegram_user_id',
                false, // not required
                null, // no min
                null, // no max
                null  // no default
            );
            console.log('✅ Created telegram_user_id attribute');
        } catch (error) {
            if (error.message.includes('already exists')) {
                console.log('⚠️  telegram_user_id attribute already exists, continuing...');
            } else {
                console.log('⚠️  Could not create telegram_user_id attribute:', error.message);
            }
        }

        // Update existing documents
        console.log('🔄 Updating existing documents...');
        let updatedCount = 0;
        let errorCount = 0;

        // Get documents with Telegram proofData
        const response = await databases.listDocuments(
            YOUR_DATABASE_ID,
            'user_social_tasks',
            [
                Query.orderAsc('$createdAt')
            ]
        );

        console.log(`📊 Processing ${response.total} documents...`);

        for (const doc of response.documents) {
            try {
                // Skip if already has telegram_user_id
                if (doc.telegram_user_id) {
                    continue;
                }

                // Check if proofData exists and contains telegram_user_id
                if (doc.proofData) {
                    let proofDataObj = {};

                    // Parse proofData if it's a string
                    if (typeof doc.proofData === 'string') {
                        try {
                            proofDataObj = JSON.parse(doc.proofData);
                        } catch (parseError) {
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
                            await databases.updateDocument(
                                YOUR_DATABASE_ID,
                                'user_social_tasks',
                                doc.$id,
                                {
                                    ...doc.data,
                                    telegram_user_id: telegramUserId
                                }
                            );
                            updatedCount++;
                            
                            // Show progress every 10 updates
                            if (updatedCount % 10 === 0) {
                                console.log(`   Updated ${updatedCount} documents...`);
                            }
                        }
                    }
                }
            } catch (docError) {
                errorCount++;
                if (errorCount <= 5) { // Only show first 5 errors to avoid spam
                    console.log(`   Error updating document ${doc.$id}:`, docError.message);
                }
            }
        }

        console.log(`🎉 Update complete!`);
        console.log(`   Successfully updated: ${updatedCount} documents`);
        if (errorCount > 0) {
            console.log(`   Errors encountered: ${errorCount} documents`);
            if (errorCount > 5) {
                console.log(`   (Only first 5 errors shown)`);
            }
        }

        console.log('\n📝 Next Steps:');
        console.log('1. Create a unique index in Appwrite Console:');
        console.log('   - Go to Database → user_social_tasks → Indexes');
        console.log('   - Add Index with attributes: taskId (asc), telegram_user_id (asc)');
        console.log('   - Set type to Unique');

    } catch (error) {
        console.error('❌ Update failed:', error.message);
    }
}

// Run the update
updateTelegramField().catch(console.error);