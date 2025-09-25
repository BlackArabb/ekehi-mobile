# Collection ID Mapping Template

After creating your collections in Appwrite Dashboard, copy the Collection IDs and update your appwrite.ts file:

## Your Collection IDs (replace with actual values):

```typescript
collections: {
  users: 'REPLACE_WITH_USERS_COLLECTION_ID',
  userProfiles: 'REPLACE_WITH_USER_PROFILES_COLLECTION_ID', 
  miningSessions: 'REPLACE_WITH_MINING_SESSIONS_COLLECTION_ID',
  socialTasks: 'REPLACE_WITH_SOCIAL_TASKS_COLLECTION_ID',
  userSocialTasks: 'REPLACE_WITH_USER_SOCIAL_TASKS_COLLECTION_ID',
  achievements: 'REPLACE_WITH_ACHIEVEMENTS_COLLECTION_ID',
  userAchievements: 'REPLACE_WITH_USER_ACHIEVEMENTS_COLLECTION_ID',
  presalePurchases: 'REPLACE_WITH_PRESALE_PURCHASES_COLLECTION_ID',
  adViews: 'REPLACE_WITH_AD_VIEWS_COLLECTION_ID'
}
```

## Example (with fake IDs):
```typescript
collections: {
  users: '64f7b2c8e1234567',
  userProfiles: '64f7b2c8e1234568',
  miningSessions: '64f7b2c8e1234569',
  socialTasks: '64f7b2c8e123456a',
  userSocialTasks: '64f7b2c8e123456b',
  achievements: '64f7b2c8e123456c',
  userAchievements: '64f7b2c8e123456d',
  presalePurchases: '64f7b2c8e123456e',
  adViews: '64f7b2c8e123456f'
}
```

## How to find Collection IDs:
1. Go to Appwrite Dashboard → Databases
2. Click on "ekehi-network-db" 
3. Each collection will show its ID in the collection list
4. Copy each ID and replace the placeholders above