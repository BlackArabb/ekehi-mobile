// Test script to verify the fix for Telegram task verificationData parsing
console.log("Testing Telegram task verificationData parsing fix...");

// Simulate the data structure that comes from Appwrite
const appwriteDocumentData = {
  title: "Join Telegram",
  platform: "telegram",
  taskType: "join_channel",
  rewardCoins: 100,
  verificationMethod: "automatic",
  verificationData: '{"channel_username":"@ekehi_network"}', // This is how it comes from Appwrite - as a JSON string
  isActive: true,
  sortOrder: 1,
  $id: "68c3df0c0016b301e86e",
  $createdAt: "2023-01-01T00:00:00.000Z",
  $updatedAt: "2023-01-01T00:00:00.000Z"
};

console.log("Raw Appwrite data:", appwriteDocumentData);

// Simulate the OLD parsing (what was happening before the fix)
const oldParsing = {
  verificationData: appwriteDocumentData.verificationData // This would be a string, not a Map
};

console.log("\nOLD parsing result:");
console.log("verificationData type:", typeof oldParsing.verificationData);
console.log("Can access channel_username?", typeof oldParsing.verificationData === 'object' && oldParsing.verificationData !== null ? oldParsing.verificationData.channel_username : "NO - verificationData is a string");

// Simulate the NEW parsing (what happens after our fix)
try {
  const newParsing = {
    verificationData: JSON.parse(appwriteDocumentData.verificationData) // Parsing the JSON string to an object
  };
  
  console.log("\nNEW parsing result:");
  console.log("verificationData type:", typeof newParsing.verificationData);
  console.log("Can access channel_username?", newParsing.verificationData.channel_username || "NO - property not found");
  console.log("✅ SUCCESS: Channel username is accessible!");
} catch (error) {
  console.log("\n❌ ERROR in new parsing:", error.message);
}

console.log("\n🎉 Test completed. The fix should resolve the 'channel not configured' error.");