@echo off
echo ==========================================
echo KtMobile Production Keystore Generator
echo ==========================================
echo.

REM Check if keystore already exists
if exist "ekehi-release.keystore" (
    echo WARNING: ekehi-release.keystore already exists!
    echo Delete it first if you want to generate a new one.
    pause
    exit /b 1
)

echo Generating production keystore...
echo.
echo NOTE: Remember the passwords you enter! You will need them for:
echo - KEYSTORE_PASSWORD (store password)
echo - KEY_PASSWORD (key password)
echo.
echo Press any key to continue...
pause > nul

REM Generate keystore using full keytool path
"C:\Program Files\Java\jdk-17\bin\keytool" -genkey -v -keystore ekehi-release.keystore -alias ekehi -keyalg RSA -keysize 2048 -validity 10000

if %ERRORLEVEL% neq 0 (
    echo.
    echo ERROR: Keystore generation failed!
    pause
    exit /b 1
)

echo.
echo ==========================================
echo Keystore generated successfully!
echo ==========================================
echo.
echo File: ekehi-release.keystore
echo Alias: ekehi
echo.
echo NEXT STEPS:
echo 1. Set environment variables:
echo    set KEYSTORE_PASSWORD=your_store_password
echo    set KEY_PASSWORD=your_key_password
echo.
echo 2. Build release APK:
echo    gradle assembleRelease
echo.
echo 3. Keep this keystore safe! You need it for all future updates.
echo.
pause
