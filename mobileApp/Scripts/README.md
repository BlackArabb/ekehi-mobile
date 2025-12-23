# Ekehi Network Appwrite Database Scripts

This directory contains scripts for managing the Appwrite database collections for the Ekehi Network mobile application.

## Prerequisites

1. Node.js installed
2. Appwrite account with a project created
3. API key with database permissions

## Setup

1. Install dependencies:
```bash
npm install
```

2. Update the configuration in each script with your Appwrite credentials:
   - Replace `YOUR_PROJECT_ID` with your actual Appwrite project ID
   - Replace `YOUR_API_KEY` with your actual Appwrite API key

## Available Scripts

### 1. Setup Appwrite Collections
Creates all required collections with proper attributes and permissions.

```bash
npm run setup-collections
```

### 2. Update User Social Tasks Collection
Updates the user_social_tasks collection to prepare for Telegram ID uniqueness enforcement.

```bash
npm run update-user-social-tasks
```

### 3. Add Telegram ID Unique Index
Adds unique index to prevent duplicate Telegram ID submissions.

```bash
npm run add-telegram-index
```

### 4. Verify Telegram Uniqueness
Verifies that the Telegram ID uniqueness constraint is working properly.

```bash
npm run verify-telegram-uniqueness
```

### 5. Test Connection
Tests the connection to your Appwrite instance.

```bash
npm run test-connection
```

## Manual Steps Required

After running the scripts, you need to manually create the unique index in the Appwrite console:

1. Go to your Appwrite Console
2. Navigate to your database and user_social_tasks collection
3. Go to the Indexes tab
4. Create a new index with these settings:
   - Attributes: taskId (ascending), telegram_user_id (ascending)
   - Type: Unique

## Security Notes

- Keep your API keys secure and never commit them to version control
- The scripts require admin permissions to modify database collections
- Always test scripts in a development environment first

## Troubleshooting

If you encounter issues:

1. Verify your Appwrite endpoint, project ID, and API key are correct
2. Ensure you have network connectivity to the Appwrite instance
3. Check that you have the necessary permissions
4. Make sure the collections exist before trying to modify them