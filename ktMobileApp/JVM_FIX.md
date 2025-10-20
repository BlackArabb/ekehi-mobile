# JVM Configuration Fix for Ekehi Mobile App

## Issue Identified
The project was showing the error: "Dependency requires at least JVM runtime version 11. This build uses a Java 8 JVM."

## Root Cause
The project was configured to use Java 8 (1.8) in the Gradle build configuration, but some dependencies require JVM 11 or higher.

## Solution Applied

### 1. Updated Gradle Build Configuration
Modified `app/build.gradle` to use JVM 11 instead of JVM 1.8:

```gradle
compileOptions {
    sourceCompatibility JavaVersion.VERSION_11
    targetCompatibility JavaVersion.VERSION_11
}
kotlinOptions {
    jvmTarget = '11'
}
```

### 2. Fixed Missing Gradle Wrapper JAR
Copied `gradle-wrapper.jar` from the React Native project to the Kotlin project:
- From: `mobileApp/android/gradle/wrapper/gradle-wrapper.jar`
- To: `ktMobileApp/gradle/wrapper/gradle-wrapper.jar`

### 3. Configured Gradle to Use Correct JDK
Updated `gradle.properties` to explicitly point to JDK 17:

```properties
// Use JDK 17 which is more compatible with Gradle 8.10.2
org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
```

## New Issue Identified
After the initial fix, you're now seeing the error: "BUG! exception in phase 'semantic analysis' in source unit '_BuildScript_' Unsupported class file major version 68"

This error indicates that:
- Class file major version 68 corresponds to Java 24
- Your Gradle 8.10.2 is trying to use Java 24, which may not be fully compatible
- Gradle 8.10.2 has better compatibility with JDK 17

## Updated Solution

### 1. Use JDK 17 Instead of JDK 24
JDK 17 is a Long Term Support (LTS) version and is more stable with Gradle 8.10.2.

### 2. Install JDK 17 (if not already installed)
If you don't have JDK 17 installed, you can download it from:
- [Eclipse Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17)
- [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

### 3. Verify JDK 17 Installation
After installing JDK 17, verify it's correctly installed:
```bash
# Check Java version
"C:\Program Files\Java\jdk-17\bin\java" -version

# Check Java compiler version
"C:\Program Files\Java\jdk-17\bin\javac" -version
```

### 4. Update Environment Variables (Optional)
You can also set your JAVA_HOME environment variable to point to JDK 17:
```bash
# Windows Command Prompt
set JAVA_HOME=C:\Program Files\Java\jdk-17

# Windows PowerShell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
```

## Verification
After making these changes, verify that Gradle is correctly configured:

```bash
cd ktMobileApp
gradlew.bat --version
```

You should see output similar to:
```
Launcher JVM:  17.x.x (Eclipse Adoptium 17.x.x+xx)
Daemon JVM:    'C:\Program Files\Java\jdk-17' (from org.gradle.java.home)
```

## Next Steps
The project should now build successfully with JVM 11+ requirements satisfied. If you encounter any issues:

1. Ensure JDK 17 is installed at `C:\Program Files\Java\jdk-17`
2. Verify that `gradle-wrapper.jar` exists in `ktMobileApp/gradle/wrapper/`
3. Try cleaning the Gradle cache: `gradlew.bat --stop` then `gradlew.bat clean`
4. If problems persist, try importing the project fresh in Android Studio

## Build Commands
Once the configuration is working, you can build the project with:

```bash
# Navigate to the project directory
cd ktMobileApp

# Build debug APK
gradlew.bat assembleDebug

# Install debug APK to connected device
gradlew.bat installDebug
```

## Compatibility Reference
- Gradle 8.10.2: Compatible with Java 8-21 (JDK 17 recommended)
- Android Gradle Plugin 8.2.0: Compatible with Java 11-17 (JDK 17 recommended)
- Kotlin 1.9.0: Compatible with Java 8-21
```