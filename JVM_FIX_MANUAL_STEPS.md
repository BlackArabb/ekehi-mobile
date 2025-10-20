# Manual Steps to Fix JVM Compatibility Issue

## Problem
You're encountering the error: "BUG! exception in phase 'semantic analysis' in source unit '_BuildScript_' Unsupported class file major version 68"

This indicates that Gradle is trying to use Java 24 (class file version 68), which may not be fully compatible with Gradle 8.10.2.

## Solution

### Step 1: Install JDK 17 (if not already installed)
1. Download JDK 17 from https://adoptium.net/temurin/releases/?version=17
2. Install it to the default location (typically C:\Program Files\Java\jdk-17)

### Step 2: Update gradle.properties
1. Open `c:\ekehi-mobile\ktMobileApp\gradle.properties` in a text editor
2. Ensure it contains:
   ```
   // Project-wide Gradle settings.
   org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
   android.useAndroidX=true
   android.enableJetifier=true
   kotlin.code.style=official
   android.nonTransitiveRClass=true
   // Use JDK 17 which is more compatible with Gradle 8.10.2
   org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
   ```

### Step 3: Stop Gradle Daemons
1. Open a command prompt
2. Navigate to the project directory:
   ```
   cd c:\ekehi-mobile\ktMobileApp
   ```
3. Stop any existing Gradle daemons:
   ```
   gradlew.bat --stop
   ```

### Step 4: Clean the Project
1. In the same command prompt, run:
   ```
   gradlew.bat clean
   ```

### Step 5: Try Building Again
1. Attempt to build the project:
   ```
   gradlew.bat assembleDebug
   ```

## Alternative Solutions

### If JDK 17 is not available:
1. Try JDK 11 instead (update the path in gradle.properties accordingly)
2. You can download JDK 11 from https://adoptium.net/temurin/releases/?version=11

### If you must use JDK 24:
1. Update to a newer version of Gradle that fully supports JDK 24
2. This would require updating the Gradle wrapper version in gradle/wrapper/gradle-wrapper.properties

## Verification
After applying the fix, verify the configuration by running:
```
cd c:\ekehi-mobile\ktMobileApp
gradlew.bat --version
```

You should see output indicating that Gradle is using JDK 17 rather than JDK 24.

## Compatibility Reference
- Gradle 8.10.2: Compatible with Java 8-21 (JDK 17 recommended)
- Android Gradle Plugin 8.2.0: Compatible with Java 11-17 (JDK 17 recommended)
- Kotlin 1.9.0: Compatible with Java 8-21