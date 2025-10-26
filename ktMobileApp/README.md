# Ekehi Mobile - Kotlin Android App

This is the native Kotlin Android implementation of the Ekehi Mobile application, migrated from the original React Native codebase.

## Project Overview

The Ekehi Mobile app is a cryptocurrency mining application that allows users to earn rewards through various activities. This Kotlin version maintains all existing backend integrations with Appwrite while providing improved performance and a native Android experience.

## Features

- User authentication (Email/Google OAuth)
- Mining session tracking
- Social task completion
- Achievement system
- Leaderboard
- Profile management
- Start.io ad integration
- Real-time updates
- Offline support

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture with MVVM
- **Dependency Injection**: Hilt
- **Networking**: Appwrite Kotlin SDK
- **Ads**: Start.io Android SDK
- **Build System**: Gradle with Kotlin DSL

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/ekehi/mobile/
│   │   │   ├── data/           # Data layer (models, repositories)
│   │   │   ├── di/             # Dependency injection modules
│   │   │   ├── domain/         # Domain layer (use cases, models)
│   │   │   ├── network/        # Network layer (services)
│   │   │   ├── presentation/   # Presentation layer (UI, ViewModels)
│   │   │   ├── security/       # Security components (13 OWASP rules)
│   │   │   ├── utils/          # Utility classes
│   │   │   ├── MainActivity.kt
│   │   │   └── MainApplication.kt
│   │   └── res/                # Resources (drawables, values, etc.)
│   └── test/                   # Test files
├── build.gradle               # App-level build configuration
└── src/main/AndroidManifest.xml
```

## Getting Started

### Prerequisites

- Android Studio Jellyfish (2023.3.1) or later
- Kotlin 1.9.0 or later
- Android SDK API level 34
- JDK 11 or later (JDK 17 recommended)

### Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Build and run the project

### JVM Configuration

**Important**: This project requires JVM 11 or higher. If you encounter JVM version errors:

1. Ensure you have JDK 11 or higher installed (JDK 17 recommended)
2. Check that `gradle.properties` contains the correct JDK path:
   ```
   org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
   ```
3. Verify that `gradle-wrapper.jar` exists in `gradle/wrapper/` directory

See [JVM_FIX.md](JVM_FIX.md) for detailed instructions on resolving JVM version issues.

### Troubleshooting JVM Issues

If you encounter the error "Unsupported class file major version 68":
1. This indicates you're using Java 24 which may not be fully compatible with Gradle 8.10.2
2. Switch to JDK 17 which has better compatibility
3. Update the `org.gradle.java.home` property in `gradle.properties`
4. Stop any existing Gradle daemons: `gradlew.bat --stop`
5. Clean the project: `gradlew.bat clean`

### Building

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug
```

## Backend Integration

This Kotlin implementation now has full Appwrite backend integration matching the React Native version:

- **Authentication**: Email/Password and Google OAuth
- **User Management**: Profile creation and updates
- **Mining**: Session tracking and earnings
- **Social Tasks**: Task completion and rewards
- **Leaderboard**: Real-time ranking system
- **Achievements**: Progress tracking and rewards
- **Offline Support**: Local caching with sync capabilities

See [BACKEND_INTEGRATION_FIX.md](BACKEND_INTEGRATION_FIX.md) for details on the backend integration implementation.

## Common Build Issues

### Compilation Error: "No value passed for parameter 'context'"
This error typically occurs due to:
1. Caching issues - Try cleaning the build environment
2. Incorrect dependency injection setup - Check DI modules
3. Corrupted compiled files - Delete build directories

### Resource Merge Error: "java.io.EOFException: End of input at line 1 column 1"
This error typically occurs due to:
1. Corrupted or empty resource files - Check all XML files in res/ directory
2. Line ending issues - Ensure consistent line endings
3. Hidden characters in files - Check for invisible characters

See [BUILD_ISSUES.md](BUILD_ISSUES.md) for detailed troubleshooting steps.

## Security Implementation

This project implements all 13 OWASP Secure Coding Practices. See [SECURITY_IMPLEMENTATION.md](SECURITY_IMPLEMENTATION.md) for details.

## Configuration

The app uses the following Appwrite configuration:

- **Endpoint**: `https://fra.cloud.appwrite.io/v1`
- **Project ID**: `68c2dd6e002112935ed2`
- **Database ID**: `68c336e7000f87296feb`

## Migration Status

This Kotlin implementation is a work in progress, migrated from the React Native version. See [KT_MIGRATION_GUIDE.md](KT_MIGRATION_GUIDE.md) for details on the migration process.

## Documentation

- [Migration Guide](KT_MIGRATION_GUIDE.md)
- [Project Structure](MIGRATION_PLAN.md)
- [Security Implementation](SECURITY_IMPLEMENTATION.md)
- [Build Instructions](BUILD_INSTRUCTIONS.md)
- [JVM Configuration Fix](JVM_FIX.md)
- [Build Issues Troubleshooting](BUILD_ISSUES.md)
- [Backend Integration Fix](BACKEND_INTEGRATION_FIX.md)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## License

This project is proprietary and confidential. All rights reserved.