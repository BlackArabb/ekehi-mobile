# Appwrite API Key Setup Guide

## Why You Need an API Key

The admin panel requires an Appwrite API key to:
- Fetch social tasks from the database
- Create new social tasks
- Update existing social tasks
- Delete social tasks

Without a valid API key, all operations will fail with "user_unauthorized" errors.

## How to Get Your Appwrite API Key

1. **Log in to your Appwrite Console**
   - Visit your Appwrite instance (e.g., https://cloud.appwrite.io/console)
   - Sign in with your credentials

2. **Navigate to Your Project**
   - Select the project that corresponds to your Ekehi Mobile App
   - The project ID should be `68c2dd6e002112935ed2`

3. **Access API Keys Section**
   - In the left sidebar, click on "Project Settings"
   - Scroll down to the "API Keys" section
   - Click "Add API Key"

4. **Create a New API Key**
   - Give your key a descriptive name (e.g., "Admin Panel Key")
   - Set an expiration date if desired
   - Select the appropriate scopes:
     - `documents.read` - To read social tasks
     - `documents.write` - To create/update/delete social tasks
   - Click "Create"

5. **Copy Your API Key**
   - Once created, copy the full API key
   - **Important**: This key will only be shown once for security reasons

## Configure Your Environment

1. **Update Your .env File**
   - Open the `.env` file in your admin panel project
   - Replace `your_appwrite_api_key_here` with your actual API key
   - Save the file

2. **Restart Your Development Server**
   - Stop your current development server (Ctrl+C)
   - Start it again with `npm run dev`

## Verify Your Setup

After configuring your API key:

1. Visit the Social Tasks page in your admin panel
2. You should see real data from your Appwrite database
3. You should be able to create, update, and delete social tasks
4. Check your browser's developer console for any errors

## Troubleshooting

If you're still experiencing issues:

1. **Verify API Key Permissions**
   - Ensure your API key has the necessary scopes (`documents.read` and `documents.write`)

2. **Check Environment Variables**
   - Make sure there are no extra spaces or characters in your API key
   - Ensure all environment variables in `.env` are correctly formatted

3. **Verify Collection IDs**
   - Confirm that the collection IDs in your `.env` file match your Appwrite database
   - The social tasks collection should be named `social_tasks`

4. **Check Network Requests**
   - Use your browser's developer tools to inspect network requests
   - Look for any failed requests to `/api/social` endpoints