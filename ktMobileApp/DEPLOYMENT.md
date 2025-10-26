# Ekehi Mobile App Deployment Guide

## Prerequisites

1. Android Studio (latest stable version)
2. JDK 8 or higher
3. Android SDK API level 34
4. Google Services JSON file (for Firebase integration)
5. Appwrite server instance
6. Keystore file for signing release builds

## Build Configuration

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

## Signing Configuration

For production deployment, you need to configure a proper signing configuration in `app/build.gradle`:

```gradle
android {
    signingConfigs {
        release {
            storeFile file('path/to/your/keystore.jks')
            storePassword 'your_store_password'
            keyAlias 'your_key_alias'
            keyPassword 'your_key_password'
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## Firebase Configuration

1. Download `google-services.json` from Firebase Console
2. Place it in `app/` directory
3. Ensure the package name matches `com.ekehi.network`

## Appwrite Configuration

The Appwrite configuration is hardcoded in the application:
- Endpoint: `https://fra.cloud.appwrite.io/v1`
- Project ID: `68c2dd6e002112935ed2`

To change these values, update `AppwriteService.kt`:

```kotlin
Client()
    .setEndpoint("YOUR_APPWRITE_ENDPOINT")
    .setProject("YOUR_PROJECT_ID")
```

## Environment Variables

The application uses the following environment-specific values:

1. Appwrite Endpoint
2. Appwrite Project ID
3. Firebase configuration (via google-services.json)

## Testing Before Deployment

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

## Performance Considerations

1. Ensure ProGuard rules are properly configured
2. Test app performance on various device configurations
3. Verify memory usage is within acceptable limits
4. Check startup time and frame rates

## Release Checklist

- [ ] Update version code and version name in `build.gradle`
- [ ] Configure proper signing configuration
- [ ] Run all tests (unit and instrumentation)
- [ ] Perform performance testing
- [ ] Verify Firebase integration
- [ ] Test Appwrite connectivity
- [ ] Check offline functionality
- [ ] Verify push notifications
- [ ] Test analytics tracking
- [ ] Review ProGuard rules
- [ ] Update documentation if needed

## Troubleshooting

### Common Issues

1. **Signing Errors**: Ensure keystore path and passwords are correct
2. **Firebase Integration**: Verify `google-services.json` is in the correct location
3. **Appwrite Connectivity**: Check endpoint and project ID configuration
4. **ProGuard Issues**: Add necessary keep rules for used libraries

### Debugging Release Builds

To debug release builds, temporarily enable debuggable mode:
```gradle
buildTypes {
    release {
        debuggable true
        // ... other configurations
    }
}
```

## Support

For deployment issues, contact the development team or refer to:
- Android Developer Documentation
- Appwrite Documentation
- Firebase Documentation
- Hilt Documentation