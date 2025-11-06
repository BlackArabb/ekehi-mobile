@echo off
echo ktMobileApp Build Script
echo ========================
echo.

REM Check if Java is available
where java >nul 2>nul
if %errorlevel% equ 0 (
    echo Java found in PATH
    java -version
    echo.
    echo Building ktMobileApp...
    echo.
    gradlew.bat assembleDebug
    if %errorlevel% equ 0 (
        echo.
        echo Build completed successfully!
        echo APK location: app\build\outputs\apk\debug\app-debug.apk
    ) else (
        echo.
        echo Build failed. Check the error messages above.
    )
) else (
    echo ERROR: Java is not installed or not in PATH
    echo.
    echo To build ktMobileApp, you need JDK 17 installed.
    echo.
    echo Please run INSTALL_JAVA.bat for installation instructions.
    echo.
    echo Or manually install JDK 17 from:
    echo https://adoptium.net/temurin/releases/?version=17
    echo.
    echo After installation, make sure to:
    echo 1. Set JAVA_HOME environment variable
    echo 2. Add Java to your PATH
    echo 3. Update gradle.properties with the correct JDK path
)

echo.
echo Press any key to exit...
pause > nul