@echo off
echo ==========================================
echo KtMobile Production Environment Setup
echo ==========================================
echo.

REM Check if running as administrator (optional but recommended)
echo Checking permissions...
echo.

REM Set environment variables for current session
echo Setting up environment variables for this session...
echo.

REM API Keys - Replace with your production values
echo Enter your production API keys:
echo.

set /p TELEGRAM_BOT_TOKEN="Telegram Bot Token: "
set /p YOUTUBE_API_KEY="YouTube API Key: "
set /p GOOGLE_WEB_CLIENT_ID="Google Web Client ID: "
set /p YOUTUBE_CLIENT_ID="YouTube Client ID: "
set /p KEYSTORE_PASSWORD="Keystore Password: "
set /p KEY_PASSWORD="Key Password: "

echo.
echo ==========================================
echo Environment variables set for this session
echo ==========================================
echo.

REM Optionally save to a local file (not committed to git)
echo Saving to .env.local file (DO NOT COMMIT THIS FILE)...
echo # Production Environment Variables > .env.local
echo # DO NOT COMMIT THIS FILE TO VERSION CONTROL >> .env.local
echo TELEGRAM_BOT_TOKEN=%TELEGRAM_BOT_TOKEN% >> .env.local
echo YOUTUBE_API_KEY=%YOUTUBE_API_KEY% >> .env.local
echo GOOGLE_WEB_CLIENT_ID=%GOOGLE_WEB_CLIENT_ID% >> .env.local
echo YOUTUBE_CLIENT_ID=%YOUTUBE_CLIENT_ID% >> .env.local
echo KEYSTORE_PASSWORD=%KEYSTORE_PASSWORD% >> .env.local
echo KEY_PASSWORD=%KEY_PASSWORD% >> .env.local

echo.
echo Environment setup complete!
echo.
echo NEXT STEPS:
echo 1. Build release APK: gradle assembleRelease
echo 2. Or run: gradlew assembleRelease
echo.
echo NOTE: To make these variables permanent, add them to your system
echo environment variables or run this script before each build.
echo.
pause
