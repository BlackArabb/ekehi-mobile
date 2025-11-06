# Ekehi Mobile - Build Instructions

This document provides instructions for building the Ekehi Mobile Kotlin Android application.

## Prerequisites

Before building the application, ensure you have the following installed:

1. Android Studio Jellyfish (2023.3.1) or later
2. Android SDK API level 34
3. JDK 11 or later (JDK 17 recommended)
4. Kotlin 1.9.0 or later

### Android SDK Installation

If you don't have the Android SDK installed:

#### Option 1: Install Android Studio (Recommended)
1. Download Android Studio from [https://developer.android.com/studio](https://developer.android.com/studio)
2. Install Android Studio with default settings
3. During installation, make sure to select "Android SDK" components
4. After installation, open Android Studio and go to Settings > Appearance & Behavior > System Settings > Android SDK
5. Install the required SDK platforms (API level 34) and tools

#### Option 2: Install Command-line Tools Only
1. Download "Command line tools only" from [https://developer.android.com/studio#command-tools](https://developer.android.com/studio#command-tools)
2. Extract the ZIP file to a directory (e.g., `C:\Android\Sdk`)
3. Navigate to the `cmdline-tools\latest\bin` directory in your terminal
4. Run the following command to install required packages:
   ```
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```

### SDK Path Configuration

After installing the Android SDK, you need to configure the SDK path:

1. Open the `local.properties` file in the project root directory
2. Add the following line with your actual SDK path:
   ```
   sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   ```
   
   Or if you installed to a custom location:
   ```
   sdk.dir=C:\\Android\\Sdk
   ```

If the `local.properties` file doesn't exist, create it in the project root directory (`ktMobileApp/`).

## Project Setup

1. Open Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to the `ktMobileApp` directory and select it
4. Wait for Android Studio to sync the project and download dependencies

## Building the Application

### From Android Studio

1. In Android Studio, select "Build" from the menu
2. Choose one of the following options:
   - "Build Bundle(s) / APK(s)" > "Build APK(s)" to build a debug APK
   - "Generate Signed Bundle / APK" to build a release APK

### From Command Line

If you have Gradle installed:

```bash
# Navigate to the project directory
cd ktMobileApp

# Build debug APK
gradle assembleDebug

# Build release APK
gradle assembleRelease

# Install debug APK to connected device
gradle installDebug

# Run unit tests
gradle test
```

If you're having issues with the Gradle wrapper, you can also try:

```bash
# On Windows
gradlew.bat assembleDebug

# On macOS/Linux
./gradlew assembleDebug
```

## JVM Version Configuration

The project has been configured to use JVM 11 or higher. If you encounter JVM version errors, ensure that:

1. You have JDK 11 or higher installed on your system (JDK 17 recommended)
2. The `gradle.properties` file in the project root contains the correct path to your JDK:

```properties
// Use JDK 17 which is more compatible with Gradle 8.10.2
org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
```

Update this path to match your actual JDK installation directory.

## Troubleshooting JVM Compatibility Issues

### Error: "Unsupported class file major version 68"
This error indicates that Gradle is trying to use Java 24, which may not be fully compatible with Gradle 8.10.2.

**Solution:**
1. Install JDK 17 if not already installed
2. Update `gradle.properties` to point to JDK 17:
   ```
   org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
   ```
3. Stop any existing Gradle daemons:
   ```bash
   gradlew.bat --stop
   ```
4. Clean the project:
   ```bash
   gradlew.bat clean
   ```

### Checking Your Java Installation
To verify which Java versions you have installed:

```bash
# Check current Java version
java -version

# Check Java compiler version
javac -version

# List Java installations (Windows)
dir "C:\Program Files\Java"

# Check JAVA_HOME environment variable
echo %JAVA_HOME%
```

### JDK Compatibility Reference
- Gradle 8.10.2: Compatible with Java 8-21 (JDK 17 recommended)
- Android Gradle Plugin 8.2.0: Compatible with Java 11-17 (JDK 17 recommended)
- Kotlin 1.9.0: Compatible with Java 8-21

## Security Implementation

The application implements all 13 OWASP Secure Coding Practices:

1. Input Validation
2. Output Encoding
3. Authentication and Password Management
4. Session Management
5. Access Control
6. Cryptographic Controls
7. Secure Data Storage
8. Error Handling and Logging
9. Secure Communication
10. Security Logging and Monitoring
11. Security Configuration
12. Malicious Code Protection
13. Security Testing

Each security component has corresponding unit tests in the `app/src/test/java/com/ekehi/mobile/security/` directory.

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/ekehi/mobile/
│   │   │   ├── security/       # Security components (13 OWASP rules)
│   │   │   ├── presentation/   # UI layer with ViewModels using security
│   │   │   ├── di/             # Dependency injection (SecurityModule)
│   │   │   └── ...             # Other components
│   │   └── res/                # Resources
│   └── test/                   # Unit tests
│       └── java/com/ekehi/mobile/security/  # Security tests
├── build.gradle               # App-level build configuration
└── src/main/AndroidManifest.xml
```

## Security Integration Points

1. ViewModels (RegistrationViewModel, LoginViewModel) use InputValidator for form validation
2. Dependency Injection is handled through SecurityModule which provides secure components via Hilt
3. ErrorHandler is used in LoginViewModel for secure exception handling
4. SecurityLogger is used for secure logging throughout the application

## Troubleshooting

If you encounter issues building the project:

1. Ensure all prerequisites are installed, especially the Android SDK
2. Verify that the Android SDK path is correctly configured in the `local.properties` file
3. Try "File" > "Sync Project with Gradle Files" in Android Studio
4. Clean and rebuild the project: "Build" > "Clean Project" then "Build" > "Rebuild Project"
5. Ensure you have a stable internet connection for downloading dependencies
6. Verify that the JDK path in `gradle.properties` matches your installation
7. If Gradle wrapper issues occur, ensure `gradle-wrapper.jar` exists in `gradle/wrapper/` directory
8. Stop Gradle daemons: `gradlew.bat --stop`
9. Check for JVM compatibility issues (see above)

## Testing

All security components have corresponding unit tests. To run tests in Android Studio:

1. Right-click on the `security` package in `app/src/test/java/com/ekehi/mobile/`
2. Select "Run Tests"

To run specific test classes, right-click on the individual test file and select "Run".