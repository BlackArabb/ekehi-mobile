@echo off
echo Java Installation Script for ktMobileApp
echo ========================================
echo.
echo This script will help you install JDK 17 for building ktMobileApp
echo.
echo Option 1: Install using winget (if available)
echo winget install EclipseAdoptium.Temurin.17.JDK
echo.
echo Option 2: Download manually from Adoptium
echo 1. Visit: https://adoptium.net/temurin/releases/?version=17
echo 2. Download JDK 17 for Windows x64
echo 3. Run the installer
echo 4. Make sure to select "Set JAVA_HOME variable" during installation
echo.
echo Option 3: Install using the MSI installer
echo If you have the MSI installer, run:
echo msiexec /i "jdk-17-installer.msi" /quiet ADDLOCAL=FeatureMain,FeatureEnvironment,FeatureJarFileRunWith
echo.
echo After installation, update gradle.properties with the correct path:
echo org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
echo.
echo Press any key to continue...
pause > nul