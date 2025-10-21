# Appwrite Data Migration Guide

This guide explains how to set up Appwrite collections and migrate data for the Ekehi Network mobile app.

## Overview

The migration process involves:
1. Setting up Appwrite collections with the correct structure
2. Generating or exporting data
3. Migrating data to Appwrite

## Prerequisites

1. Appwrite account and project
2. API key with appropriate permissions
3. Node.js installed
4. pnpm package manager

## Step-by-Step Migration Process

### 1. Set Up Appwrite Collections

Use the automated script to create collections with the correct structure:

```bash
# First, set your Appwrite API key as an environment variable
export APPWRITE_API_KEY=your_api_key_here

# Then run the collection update script
pnpm run update-collections
```

Alternatively, you can manually update the script with your API key and run it:

```bash
# Edit Scripts/update-appwrite-collections.js and set your API key
# Then run:
node Scripts/update-appwrite-collections.js
```

### 2. Generate Sample Data (Optional)

For testing purposes, you can generate sample data:

```bash
pnpm run generate-sample-data
```

This will create JSON files in the `cloudflare-json-export` directory.

### 3. Export Cloudflare Data (If migrating from Cloudflare)

If you have data in Cloudflare D1, export it first:

```bash
pnpm run export-cloudflare-data
```

### 4. Migrate Data to Appwrite

Run the migration script:

```bash
pnpm run migrate-data
```

## Scripts Overview

### update-collections
Creates or updates Appwrite collections with the correct structure based on the current mobile app data model.

### generate-sample-data
Generates sample data for testing purposes.

### export-cloudflare-data
Exports data from Cloudflare D1 to JSON files.

### migrate-data
Migrates data from JSON files to Appwrite collections.

## Environment Variables

The scripts use the following environment variables:

- `APPWRITE_ENDPOINT` - Appwrite endpoint (default: https://fra.cloud.appwrite.io/v1)
- `APPWRITE_PROJECT_ID` - Appwrite project ID (default: 68c2dd6e002112935ed2)
- `APPWRITE_DATABASE_ID` - Appwrite database ID (default: 68c336e7000f87296feb)
- `APPWRITE_API_KEY` - Appwrite API key (required for update-collections)

## Collection Structure

The current collection structure includes:

1. **users** - User authentication data
2. **user_profiles** - User profile information including mining stats
3. **mining_sessions** - Mining session tracking
4. **social_tasks** - Social media tasks
5. **user_social_tasks** - User's completed social tasks
6. **achievements** - Available achievements
7. **user_achievements** - User's unlocked achievements
8. **presale_purchases** - Presale purchase records
9. **ad_views** - Ad view tracking

## Recent Updates

### Manual Mining System
- Removed deprecated `coinsPerClick` field
- Updated `dailyMiningRate` to 2 EKH per 24-hour session
- Added `autoMiningRate` for auto mining feature
- Added `referralBonusRate` for referral bonuses

### Data Structure Improvements
- Added `updatedAt` field to all collections
- Added `clicksMade` field to mining sessions
- Improved field naming consistency

## Troubleshooting

### Collection Creation Issues
1. Verify your API key has appropriate permissions
2. Check that the database ID exists
3. Ensure you're using the correct project ID

### Migration Issues
1. Verify collection IDs match between configuration and Appwrite
2. Check that JSON data structure matches collection attributes
3. Ensure sufficient permissions for data creation

### Connection Issues
1. Verify endpoint URL is correct
2. Check network connectivity
3. Confirm project ID is correct

## Testing

After migration, verify:
1. All collections exist with correct attributes
2. Data has been migrated successfully
3. Mobile app can connect and retrieve data
4. All features work as expected

## Additional Resources

- [UPDATED_APPWRITE_SETUP.md](UPDATED_APPWRITE_SETUP.md) - Detailed collection structure
- [MANUAL_MINING_SYSTEM_UPDATE.md](MANUAL_MINING_SYSTEM_UPDATE.md) - Manual mining system changes
- [setup-appwrite-manual.md](setup-appwrite-manual.md) - Manual setup guide