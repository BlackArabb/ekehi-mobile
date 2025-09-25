// Add Wallet Address Attribute Script
// This script adds the walletAddress attribute to the user_profiles collection

const { Client, Databases } = require('node-appwrite');

const APPWRITE_CONFIG = {
  endpoint: 'https://fra.cloud.appwrite.io/v1',
  projectId: '68c2dd6e002112935ed2',
  databaseId: '68c336e7000f87296feb',
  apiKey: 'standard_10978d04f108cd8ff60bd7da34905ecfea1e28dbb311b791230d00e6f210499ae2a46ae1916cdf277db6037f7d5062efcd77b8d953b8ec890a28d0b7681dd1dddc11a1d6a0e7cc30b36afbcfe43c561465fb078b450e00eef113fadc3fe267beab0c2760c447c44427b099a8742711412d5f8fc18f2c6f5d3add2f4a8b0af19d'
};

const client = new Client();
client
  .setEndpoint(APPWRITE_CONFIG.endpoint)
  .setProject(APPWRITE_CONFIG.projectId)
  .setKey(APPWRITE_CONFIG.apiKey);

const databases = new Databases(client);

async function addWalletAddressAttribute() {
  console.log('🔧 Adding walletAddress attribute to user_profiles collection...');
  console.log('============================================================');
  
  try {
    console.log('  📝 Creating walletAddress (string) attribute...');
    
    const result = await databases.createStringAttribute(
      APPWRITE_CONFIG.databaseId,
      'user_profiles',  // collectionId
      'walletAddress',  // key
      255,              // size
      false             // required (set to false since it's optional)
    );
    
    console.log('  ✅ Successfully created walletAddress attribute');
    console.log('\\n📋 Next steps:');
    console.log('1. Run "node check-collections.js" to verify');
    console.log('2. Test your app: pnpm start');
    
  } catch (error) {
    if (error.message.includes('already exists')) {
      console.log('  ⚠️  walletAddress attribute already exists, skipping...');
      console.log('\\n📋 Next steps:');
      console.log('1. Run "node check-collections.js" to verify');
      console.log('2. Test your app: pnpm start');
    } else {
      console.error('  ❌ Failed to create walletAddress attribute:', error.message);
      process.exit(1);
    }
  }
}

addWalletAddressAttribute().catch(console.error);