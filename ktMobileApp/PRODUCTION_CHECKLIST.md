# KtMobile App Production Checklist

## 1. Build and Compilation Requirements

### Version Configuration
- [ ] Update `versionCode` in `app/build.gradle` (increment from current 14)
- [ ] Update `versionName` in `app/build.gradle` (update from current "1.4.0")
- [ ] Verify `compileSdk` is set to 34 (Android 14)
- [ ] Verify `targetSdk` is set to 34
- [ ] Verify `minSdk` is set to 24 (Android 7.0)

### Build Environment
- [ ] Verify JDK 17 is installed and configured
- [ ] Verify Android SDK API level 34 is installed
- [ ] Verify Gradle wrapper is functioning (`./gradlew --version`)
- [ ] Clean build cache: `./gradlew clean`
- [ ] Verify debug build compiles: `./gradlew assembleDebug`
- [ ] Verify release build compiles: `./gradlew assembleRelease`

### Build Configuration Validation
```gradle
// Verify in app/build.gradle:
android {
    compileSdk 34
    defaultConfig {
        applicationId "com.ekehi.network"
        minSdk 24
        targetSdk 34
        versionCode [INCREMENTED]
        versionName "[NEW_VERSION]"
    }
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.release // NOT debug
        }
    }
}
```

---

## 2. Security Configurations and Validations

### Keystore Configuration
- [ ] Generate production keystore (if not exists):
  ```bash
  keytool -genkey -v -keystore ekehi-release.keystore -alias ekehi -keyalg RSA -keysize 2048 -validity 10000
  ```
- [ ] Store keystore file in secure location (NOT in version control)
- [ ] Configure signing config in `app/build.gradle`:
  ```gradle
  signingConfigs {
      release {
          storeFile file('../ekehi-release.keystore')
          storePassword System.getenv('KEYSTORE_PASSWORD')
          keyAlias 'ekehi'
          keyPassword System.getenv('KEY_PASSWORD')
      }
  }
  ```
- [ ] Set environment variables for keystore passwords:
  ```bash
  export KEYSTORE_PASSWORD=your_secure_password
  export KEY_PASSWORD=your_secure_password
  ```

### API Keys and Secrets
- [ ] Verify Telegram Bot Token is production-ready (line 27 in build.gradle)
- [ ] Verify YouTube API Key is production-ready (line 28 in build.gradle)
- [ ] Verify Google Web Client ID is correct (line 29 in build.gradle)
- [ ] Verify YouTube Client ID is correct (line 30 in build.gradle)
- [ ] Verify Start.io App ID in AndroidManifest.xml (line 177): `210617452`
- [ ] Verify Facebook App ID in `res/values/strings.xml`
- [ ] Verify Facebook Client Token is configured

### Security Implementations (from SECURITY_IMPLEMENTATION.md)
- [ ] Input validation enabled (InputValidator.kt)
- [ ] Output encoding enabled (OutputEncoder.kt)
- [ ] Authentication and password management (AuthManager.kt)
- [ ] Session management enabled (SessionManager.kt)
- [ ] Access control configured (AccessControlManager.kt)
- [ ] Cryptographic controls active (CryptoManager.kt)
- [ ] Secure data storage verified (SecurePreferences.kt)
- [ ] Error handling and logging configured (ErrorHandler.kt)
- [ ] Secure communication enabled (SecurityInterceptor.kt)
- [ ] Security logging and monitoring active (SecurityLogger.kt)
- [ ] Security configuration validated (SecurityConfig.kt)
- [ ] Malicious code protection enabled (MaliciousCodeProtection.kt)

### Network Security
- [ ] Verify `network_security_config.xml` allows only HTTPS
- [ ] Certificate pinning configured (if applicable)
- [ ] Verify `android:usesCleartextTraffic="false"` in manifest

---

## 3. Backend Integration Verification

### Appwrite Configuration
- [ ] Verify Appwrite endpoint: `https://fra.cloud.appwrite.io/v1`
- [ ] Verify Appwrite Project ID: `68c2dd6e002112935ed2`
- [ ] Verify Appwrite Database ID: `68c336e7000f87296feb`
- [ ] Verify all collection IDs in AppwriteService.kt:
  - USERS_COLLECTION = "users"
  - USER_PROFILES_COLLECTION = "user_profiles"
  - MINING_SESSIONS_COLLECTION = "mining_sessions"
  - SOCIAL_TASKS_COLLECTION = "social_tasks"
  - USER_SOCIAL_TASKS_COLLECTION = "user_social_tasks"
  - ACHIEVEMENTS_COLLECTION = "achievements"
  - USER_ACHIEVEMENTS_COLLECTION = "user_achievements"
  - PRESALE_PURCHASES_COLLECTION = "presale_purchases"
  - AD_VIEWS_COLLECTION = "ad_views"
  - REFERRALS_COLLECTION = "referrals"
  - ADS_COLLECTION = "ads"
  - ADS_BANNERS_BUCKET = "694fe7980019d4fde1dd"

### Firebase Configuration
- [ ] Verify `google-services.json` is in `app/` directory
- [ ] Verify package name matches: `com.ekehi.network`
- [ ] Verify Firebase project: `time-machine-50e5b`
- [ ] Verify Firebase Analytics is enabled
- [ ] Verify Firebase Cloud Messaging is configured

### API Endpoints Validation
- [ ] Test Appwrite connectivity
- [ ] Test user authentication flow
- [ ] Test mining session creation/retrieval
- [ ] Test social tasks fetching
- [ ] Test leaderboard data retrieval
- [ ] Test user profile updates

---

## 4. Ad Network Setup (Start.io) Configurations

### Start.io SDK Configuration
- [ ] Verify Start.io SDK version: `5.0.0` in build.gradle
- [ ] Verify Start.io App ID in AndroidManifest.xml: `210617452`
- [ ] Verify Start.io permissions in manifest:
  - `ACCESS_WIFI_STATE`
  - `CHANGE_WIFI_STATE`
  - `ACCESS_COARSE_LOCATION`
  - `ACCESS_FINE_LOCATION`
  - `RECEIVE_BOOT_COMPLETED`
  - `AD_ID`
  - `BLUETOOTH`

### Ad Settings
- [ ] Splash ads: DISABLED (`SPLASH_ENABLED = false`)
- [ ] Return ads: ENABLED (`RETURN_ADS_ENABLED = true`)
- [ ] Verify Start.io components registered in manifest:
  - List3DActivity
  - OverlayActivity
  - FullScreenActivity
  - BannerActivity
  - StartAppAdService
  - SchedulerService
  - StartAppAdReceiver

### Ad Testing
- [ ] Test interstitial ad display
- [ ] Test rewarded video ad display
- [ ] Test banner ad display
- [ ] Verify ad callbacks work correctly

---

## 5. Permissions and Manifest Settings

### Required Permissions Check
```xml
<!-- Verify in AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.AD_ID" />
```

### Application Configuration
- [ ] Verify `android:allowBackup="true"` (or false for security)
- [ ] Verify `android:networkSecurityConfig` is set
- [ ] Verify FileProvider is configured for APK updates
- [ ] Verify MainApplication is set as application name

### Facebook SDK Configuration
- [ ] Verify Facebook ApplicationId meta-data
- [ ] Verify Facebook ClientToken meta-data
- [ ] Verify FacebookActivity is registered
- [ ] Verify CustomTabActivity is registered with intent filter

### OAuth Configuration
- [ ] Verify OAuthCallbackActivity is registered
- [ ] Verify OAuth success callback intent filter
- [ ] Verify OAuth failure callback intent filter
- [ ] Verify callback scheme: `appwrite-callback-68c2dd6e002112935ed2`

---

## 6. Performance Optimizations

### ProGuard/R8 Configuration
- [ ] Verify ProGuard rules in `proguard-rules.pro`:
  - Appwrite SDK rules
  - Hilt/Dagger rules
  - Room database rules
  - Firebase rules
  - Start.io rules
  - Data classes rules
  - ViewModel rules

### Build Optimizations
- [ ] Verify `minifyEnabled true` for release builds
- [ ] Verify `shrinkResources` is enabled (add if not present)
- [ ] Verify `zipAlignEnabled` is enabled
- [ ] Verify `debuggable false` for release builds

### Code Optimization
- [ ] Remove all debug logging statements
- [ ] Remove test code and mock data
- [ ] Verify no hardcoded API keys in source code
- [ ] Verify no test endpoints in production

### Resource Optimization
- [ ] Optimize image assets (WebP format recommended)
- [ ] Verify no unused resources
- [ ] Check APK size is reasonable (< 50MB target)

---

## 7. Testing Requirements

### Unit Tests
- [ ] Run all unit tests: `./gradlew test`
- [ ] Verify security component tests pass
- [ ] Verify repository tests pass
- [ ] Verify use case tests pass

### Integration Tests
- [ ] Run instrumentation tests: `./gradlew connectedAndroidTest`
- [ ] Test authentication flow end-to-end
- [ ] Test mining session flow
- [ ] Test social tasks completion flow
- [ ] Test leaderboard functionality

### Manual Testing Checklist
- [ ] App launches without crashes
- [ ] User registration works
- [ ] User login works
- [ ] Social login (Google, Facebook) works
- [ ] Mining session starts/stops correctly
- [ ] Social tasks display and complete
- [ ] Notifications appear correctly
- [ ] Ads display correctly
- [ ] Offline mode works
- [ ] App update mechanism works

### Device Testing
- [ ] Test on Android 7.0 (API 24) - minimum supported
- [ ] Test on Android 10 (API 29)
- [ ] Test on Android 12 (API 31)
- [ ] Test on Android 14 (API 34) - target
- [ ] Test on various screen sizes
- [ ] Test on tablet devices

---

## 8. Production-Specific Configurations

### Environment Configuration
- [ ] Verify production Appwrite endpoint
- [ ] Verify production Firebase project
- [ ] Verify production Start.io App ID
- [ ] Verify production Facebook App ID
- [ ] Verify production API keys

### Feature Flags
- [ ] Disable debug features
- [ ] Enable analytics tracking
- [ ] Enable crash reporting
- [ ] Verify feature flags are set for production

### Notification Configuration
- [ ] Verify notification channels are created
- [ ] Verify notification icons are set
- [ ] Test notification delivery
- [ ] Verify notification permissions handling

### Background Services
- [ ] Verify MiningService is configured correctly
- [ ] Verify DownloadService is configured correctly
- [ ] Verify FirebaseMessagingService is registered
- [ ] Test background task scheduling

---

## 9. Deployment Preparations

### Pre-Deployment Checklist
- [ ] All tests passing
- [ ] Release build generates successfully
- [ ] APK is signed with release keystore
- [ ] APK size is optimized
- [ ] No debug information in release build
- [ ] ProGuard mapping file is saved

### Google Play Store Preparation
- [ ] Prepare store listing:
  - App title
  - Short description
  - Full description
  - Screenshots (phone, tablet)
  - Feature graphic
  - App icon (512x512)
- [ ] Prepare privacy policy URL
- [ ] Set content rating
- [ ] Configure app pricing (free/paid)
- [ ] Set up in-app products (if applicable)

### Release Artifacts
- [ ] Generate signed APK: `./gradlew assembleRelease`
- [ ] Locate release APK: `app/build/outputs/apk/release/`
- [ ] Generate AAB (Android App Bundle): `./gradlew bundleRelease`
- [ ] Save ProGuard mapping file: `app/build/outputs/mapping/release/`
- [ ] Document version changes

---

## 10. Post-Deployment Verification Steps

### Immediate Verification
- [ ] Upload APK/AAB to Google Play Console
- [ ] Verify app passes Google Play pre-launch report
- [ ] Test internal testing track
- [ ] Verify app is downloadable
- [ ] Verify app installs correctly
- [ ] Verify app launches without crashes

### Functional Verification
- [ ] Test user registration on production
- [ ] Test user login on production
- [ ] Test mining functionality
- [ ] Test social tasks
- [ ] Test notifications
- [ ] Test ads display
- [ ] Verify analytics events are received
- [ ] Verify crash reports are working

### Monitoring Setup
- [ ] Set up Firebase Crashlytics monitoring
- [ ] Set up Firebase Analytics dashboards
- [ ] Set up Google Play Console alerts
- [ ] Configure error rate monitoring
- [ ] Set up performance monitoring

### Rollback Plan
- [ ] Document rollback procedure
- [ ] Keep previous version APK available
- [ ] Prepare emergency contact list
- [ ] Set up rapid response team

---

## Critical Production Changes Required

### 1. Fix Signing Configuration
**File**: `app/build.gradle` (Line 37)
```gradle
// CHANGE FROM:
signingConfig signingConfigs.debug

// CHANGE TO:
signingConfig signingConfigs.release
```

### 2. Update Version
**File**: `app/build.gradle` (Lines 18-19)
```gradle
versionCode 15  // Increment
versionName "1.5.0"  // Update version
```

### 3. Enable Resource Shrinking
**File**: `app/build.gradle`
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true  // Add this
        zipAlignEnabled true  // Add this
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release
    }
}
```

### 4. Secure API Keys
Move API keys from build.gradle to environment variables or secure storage:
```gradle
buildConfigField "String", "TELEGRAM_BOT_TOKEN", "\"${System.getenv('TELEGRAM_BOT_TOKEN')}\""
```

---

## Emergency Contacts

- **Development Team**: [Add contact]
- **DevOps Team**: [Add contact]
- **Product Manager**: [Add contact]
- **Google Play Console Admin**: [Add contact]
- **Appwrite Admin**: [Add contact]
- **Firebase Admin**: [Add contact]

---

## Notes

- Keep keystore file secure and backed up
- Never commit keystore or passwords to version control
- Test release build thoroughly before deployment
- Monitor app performance after release
- Be prepared for rapid rollback if critical issues arise
