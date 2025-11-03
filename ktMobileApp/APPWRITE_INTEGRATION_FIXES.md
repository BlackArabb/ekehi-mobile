# Appwrite Integration Fixes for Mining and Profile Pages

This document summarizes the fixes made to resolve issues with the mining and profile pages not displaying actual data from the database.

## Issues Identified

1. **Incorrect Appwrite Collection IDs**: The AppwriteService was using collection names instead of actual collection IDs
2. **Wrong Appwrite Endpoint**: Using default region instead of Frankfurt region endpoint
3. **Incorrect User Profile Querying**: Using document ID directly instead of querying by userId field
4. **Inconsistent Document Creation**: Not using "unique()" for document IDs

## Fixes Applied

### 1. Updated Appwrite Collection IDs

**File**: `service/AppwriteService.kt`

Changed from collection names to actual collection IDs:
```kotlin
// Before
const val USERS_COLLECTION = "users"
const val USER_PROFILES_COLLECTION = "user_profiles"
// ... other collections

// After
const val USERS_COLLECTION = "68c338510019911224b3"
const val USER_PROFILES_COLLECTION = "68c339e300263bb8050e"
// ... other collections with actual IDs
```

### 2. Updated Appwrite Endpoint

**File**: `di/AppModule.kt`

Changed to Frankfurt region endpoint to match React Native app:
```kotlin
// Before
.setEndpoint("https://cloud.appwrite.io/v1")

// After
.setEndpoint("https://fra.cloud.appwrite.io/v1") // Frankfurt region endpoint
```

### 3. Fixed User Profile Repository Methods

**File**: `data/repository/UserRepository.kt`

Updated all methods to query by userId field instead of using document ID directly:

#### getUserProfile Method
```kotlin
// Before
val document = appwriteService.databases.getDocument(
    databaseId = AppwriteService.DATABASE_ID,
    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
    documentId = userId
)

// After
val response = appwriteService.databases.listDocuments(
    databaseId = AppwriteService.DATABASE_ID,
    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
    queries = listOf(
        io.appwrite.Query.equal("userId", userId)
    )
)
```

#### createUserProfile Method
```kotlin
// Before
documentId = userId

// After
documentId = "unique()"
```

#### updateUserProfile Method
```kotlin
// Before
val document = appwriteService.databases.updateDocument(
    databaseId = AppwriteService.DATABASE_ID,
    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
    documentId = userId,
    data = updates
)

// After
// First find document by userId
val response = appwriteService.databases.listDocuments(
    databaseId = AppwriteService.DATABASE_ID,
    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
    queries = listOf(
        io.appwrite.Query.equal("userId", userId)
    )
)

// Then update using actual document ID
val document = appwriteService.databases.updateDocument(
    databaseId = AppwriteService.DATABASE_ID,
    collectionId = AppwriteService.USER_PROFILES_COLLECTION,
    documentId = documentId, // Actual document ID from query result
    data = updates
)
```

### 4. Fixed Mining Repository

**File**: `data/repository/MiningRepository.kt`

Updated claimFinalReward method to properly handle unused variable and made documentToMiningSession method more robust:
```kotlin
// Before
val updatedProfile = appwriteService.databases.updateDocument(...) // Variable never used

// After
appwriteService.databases.updateDocument(...) // Direct call without unused variable

// Also fixed documentToMiningSession to handle null values
return MiningSession(
    id = document.id ?: "",
    userId = data["userId"] as? String ?: "",
    // ... other fields with proper null handling
)
```

## Root Cause Analysis

The main issue was that the Kotlin Android app was not aligned with the React Native app's data structure and querying patterns:

1. The React Native app stores user profiles with the userId field matching the user's account ID
2. The Kotlin app was trying to fetch user profiles by document ID, which didn't match the actual data structure
3. Collection IDs and endpoint configuration didn't match the React Native app setup

## Testing Verification

After applying these fixes:
1. Profile page should now correctly display user data from the database
2. Mining page should now correctly display mining session data
3. Leaderboard and social pages should continue to work as before

## Additional Fixes for Null Safety

Made all document conversion methods more robust by adding null checks:

1. **UserRepository.documentToUserProfile**: Added null checks for all fields including document.id, document.createdAt, and document.updatedAt
2. **MiningRepository.documentToMiningSession**: Added null checks for document.id, document.createdAt, and document.updatedAt
3. **SocialTaskRepository.documentToSocialTask**: Added null checks for all fields
4. **SocialTaskRepository.documentToUserSocialTask**: Added null checks for userId and taskId

## Future Considerations

1. Ensure all Appwrite queries follow the pattern of querying by fields rather than document IDs when the document ID isn't known
2. Keep collection IDs synchronized between backend and frontend configurations
3. Maintain consistency in endpoint configuration across all platforms