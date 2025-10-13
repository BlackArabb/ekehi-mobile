# Ekehi Network Mobile App

## OAuth Configuration Instructions

To properly configure OAuth for Android and iOS, follow these steps:

### 1. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project
3. Navigate to "APIs & Services" > "Credentials"
4. Find your OAuth 2.0 Client IDs

#### For Android OAuth Client:
- Add the following redirect URI:
  ```
  ekehi://oauth/return
  ```

#### For iOS OAuth Client:
- Add the following redirect URI:
  ```
  ekehi://oauth/return
  ```

### 2. Appwrite Console Setup

1. Go to your Appwrite Console
2. Navigate to your project
3. Go to "Authentication" > "Platforms"
4. Add a new platform with the following settings:
   - Platform: "Custom URL Scheme"
   - Hostname: (leave empty)
   - Redirect URLs:
     ```
     ekehi://oauth/return
     ```

### 3. Deep Link Verification

To verify that deep linking is working correctly:

1. On Android, you can test with:
   ```
   adb shell am start -W -a android.intent.action.VIEW -d "ekehi://oauth/return" com.ekehi.network
   ```

2. On iOS, you can test with:
   ```
   xcrun simctl openurl booted ekehi://oauth/return
   ```

### 4. Testing OAuth Configuration

You can test your OAuth configuration by navigating to the test page:
- Run the app and go to `/test-deeplink`

### 5. Troubleshooting

If OAuth is still not working:

1. Check that your package name matches: `com.ekehi.network`
2. Verify that your OAuth client IDs in `appwrite.ts` match those in Google Cloud Console
3. Make sure the redirect URIs exactly match `ekehi://oauth/return`
4. Ensure that the deep link scheme in `app.json` is set to `ekehi`

### 6. Common Issues

- **Redirect URI mismatch**: Make sure the redirect URIs in Google Cloud Console exactly match what your app expects
- **Package name mismatch**: Ensure your Android package name is `com.ekehi.network`
- **Bundle ID mismatch**: Ensure your iOS bundle ID is `com.ekehi.network`
- **Appwrite configuration**: Make sure the OAuth provider is enabled in Appwrite authentication settings

### 7. Detailed Configuration Steps

#### In Google Cloud Console:
1. Go to APIs & Services > Credentials
2. Click on your OAuth 2.0 Client ID for Android/iOS
3. Under "Authorized redirect URIs", add:
   ```
   ekehi://oauth/return
   ```

#### In Appwrite Console:
1. Go to your project
2. Navigate to Authentication > Platforms
3. Click "Add Platform"
4. Select "Custom URL Scheme"
5. Leave Hostname empty
6. In Redirect URLs, add:
   ```
   ekehi://oauth/return
   ```
7. Click "Register"

[![React Native](https://img.shields.io/badge/React%20Native-0.74.5-blue.svg)](https://reactnative.dev/)
[![Expo](https://img.shields.io/badge/Expo-~51.0.28-000020.svg)](https://expo.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.3.3-blue.svg)](https://www.typescriptlang.org/)
[![Appwrite](https://img.shields.io/badge/Appwrite-20.0.0-red.svg)](https://appwrite.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A React Native mobile application for the Ekehi Network cryptocurrency mining platform, featuring an interactive tap-to-mine system, social task engine, referral program, token presale, wallet integration, and achievement system.

> **🚀 Migration Complete**: This project has been successfully migrated from Cloudflare Workers to Appwrite for improved scalability and maintainability. See [Migration Documentation](#migration-from-cloudflare-to-appwrite) for details.

## 📑 Table of Contents

- [Features Overview](#-features-overview)
- [Technology Stack](#-technology-stack)
- [Quick Start](#-quick-start)
- [Appwrite Configuration](#-appwrite-configuration)
- [Project Structure](#-project-structure)
- [Development Guide](#-development-guide)
- [Architecture & Design](#-architecture--design)
- [API Integration](#-api-integration)
- [Authentication System](#-authentication-system)
- [State Management](#-state-management)
- [Navigation](#-navigation)
- [UI/UX Design](#-uiux-design)
- [Platform Features](#-platform-features)
- [Performance](#-performance)
- [Building & Deployment](#-building--deployment)
- [Testing](#-testing)
- [Migration from Cloudflare to Appwrite](#-migration-from-cloudflare-to-appwrite)
- [Mining Session Recording](#-mining-session-recording)
- [Manual Mining System Update](#-manual-mining-system-update)
- [Data Operations](#-data-operations)
- [Presale Page](#-presale-page)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

## 🚀 Features Overview

### Core Mining System
- **🎯 Tap-to-Mine Interface**: Touch-optimized mining with haptic feedback and real-time balance updates
- **⚡ Mining Power System**: Upgradeable mining power with automatic calculations
- **🔥 Daily Streak Bonuses**: Consecutive login rewards and streak multipliers
- **📊 Real-time Analytics**: Live mining statistics and performance tracking

### Social & Community Features
- **📱 Social Task Engine**: Complete tasks on Twitter, YouTube, Telegram, and Discord for rewards
- **🤝 Multi-level Referral System**: Share referral codes and earn bonuses based on referral tiers
- **🏆 Global Leaderboards**: Competitive rankings and user achievements
- **🎖️ Achievement System**: Milestone-based rewards and progression tracking

### Financial Features
- **💰 Token Presale**: Early access token purchasing with automatic mining unlocks
- **👛 Wallet Integration**: Manage token balances, send transactions, and view history
- **💸 Token Transfers**: Send and receive tokens between users
- **📈 Portfolio Tracking**: Real-time balance monitoring and transaction history

### User Experience
- **🔐 Multi-Auth Support**: Google OAuth and email/password authentication
- **🛡 Security Features**: Email verification and password recovery (Hybrid Approach)
- **📱 Cross-platform**: Native iOS, Android, and web compatibility
- **💾 Offline Support**: Local data persistence with AsyncStorage
- **🔗 Deep Linking**: OAuth callbacks and referral link handling

## 🛠 Technology Stack

### Frontend Technologies
- **React Native 0.74.5** - Cross-platform mobile development framework
- **Expo SDK 51** - Development toolchain and runtime platform
- **TypeScript 5.3.3** - Type-safe JavaScript development
- **React Navigation** - Bottom tabs and native stack navigation
- **React Native Reanimated 3.10.1** - High-performance animations
- **Expo Router** - File-based routing system

### Backend & Services
- **Appwrite v20.0.0** - Backend-as-a-Service for authentication and database
- **Appwrite Database** - Document-based database for all app data
- **Appwrite Auth** - Google OAuth and email/password authentication
- **AsyncStorage** - Local data persistence and offline support

### UI & Design
- **Expo Linear Gradient** - Beautiful gradient effects and backgrounds
- **Expo Haptics** - Native touch feedback and vibrations
- **Lucide React Native** - Modern icon library
- **React Native SVG** - Scalable vector graphics support

### Development Tools
- **Expo CLI** - Development server and build tools
- **EAS Build** - Cloud-based building for iOS and Android
- **TypeScript Compiler** - Static type checking
- **React Native Debugger** - Development debugging tools

### Deployment & Hosting
- **Vercel** - Web version deployment for testing
- **Apple App Store** - iOS app distribution
- **Google Play Store** - Android app distribution
- **Expo Application Services** - Build and submission automation

## ⚡ Quick Start

### Prerequisites

**Required Software:**
- **Node.js 18+** - [Download here](https://nodejs.org/)
- **pnpm** - Package manager: `npm install -g pnpm`
- **Expo CLI** - Install globally: `pnpm add -g @expo/cli`

**For iOS Development:**
- **Xcode 14+** - macOS only
- **iOS Simulator** - Included with Xcode
- **CocoaPods** - iOS dependency manager

**For Android Development:**
- **Android Studio** - [Download here](https://developer.android.com/studio)
- **Android SDK** - Managed through Android Studio
- **Android Emulator** - Set up through Android Studio

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/ekehi-network-mobile.git
   cd ekehi-network-mobile
   ```

2. **Install Dependencies**
   ```bash
   pnpm install
   ```

3. **Configure Appwrite** (See [Appwrite Configuration](#-appwrite-configuration))
   ```bash
   # Test Appwrite connection
   pnpm run test-appwrite
   ```

4. **Start Development Server**
   ```bash
   pnpm start
   ```

5. **Run on Device/Simulator**
   ```bash
   # iOS (macOS only)
   pnpm run ios
   
   # Android
   pnpm run android
   
   # Web (for testing)
   pnpm run web
   ```

## 🔧 Appwrite Configuration

> **⚠️ CRITICAL**: You must configure Appwrite before the app will function properly.

### Step 1: Create Appwrite Project

1. **Sign up at [Appwrite Cloud](https://cloud.appwrite.io/)**
2. **Create a new project** named "Ekehi Network"
3. **Note your Project ID** (e.g., `64f7b2c8e1234567`)

### Step 2: Configure Authentication

1. **Go to Auth → Settings**
2. **Enable Google OAuth Provider**
   - Add your Google Client ID
   - Add your Google Client Secret
   - Set redirect URL: `ekehi://oauth/callback`

3. **Add Platform URLs**
   - Go to Settings → Platforms
   - Add iOS app with bundle ID
   - Add Android app with package name
   - Add Web app with domain

### Step 3: Create Database Structure

1. **Create Database**
   - Name: `ekehi-network-db`
   - Database ID: `ekehi-network-db`

2. **Create Collections** (9 total required):

   **a) `users` Collection**
   ```typescript
   Attributes:
   - email (String, 255, Required, Unique)
   - name (String, 255, Required)
   - createdAt (DateTime, Required)
   - lastLogin (DateTime, Optional)
   ```

   **b) `user_profiles` Collection**
   ```typescript
   Attributes:
   - userId (String, 255, Required, Unique)
   - username (String, 255, Optional)
   - totalCoins (Double, Required, Default: 0)
   - coinsPerSecond (Double, Required, Default: 0)
   - miningPower (Double, Required, Default: 1)
   - currentStreak (Integer, Required, Default: 0)
   - longestStreak (Integer, Required, Default: 0)
   - lastLoginDate (DateTime, Optional)
   - referralCode (String, 255, Optional, Unique)
   - referredBy (String, 255, Optional)
   - totalReferrals (Integer, Required, Default: 0)
   - lifetimeEarnings (Double, Required, Default: 0)
   - dailyMiningRate (Double, Required, Default: 2) // Updated: Now represents 2 EKH per 24-hour session
   - maxDailyEarnings (Double, Required, Default: 10000)
   - todayEarnings (Double, Required, Default: 0)
   - lastMiningDate (DateTime, Optional)
   - streakBonusClaimed (Integer, Required, Default: 0)
   - createdAt (DateTime, Required)
   - updatedAt (DateTime, Required)
   ```

   **c) `mining_sessions` Collection**
   ```typescript
   Attributes:
   - userId (String, 255, Required)
   - coinsEarned (Double, Required)
   - clicksMade (Integer, Required)
   - sessionDuration (Integer, Required)
   - createdAt (DateTime, Required)
   ```

   **d) `social_tasks` Collection**
   ```typescript
   Attributes:
   - title (String, 255, Required)
   - description (String, 1000, Required)
   - platform (String, 255, Required)
   - taskType (String, 255, Required)
   - rewardCoins (Double, Required)
   - actionUrl (String, 500, Optional)
   - verificationMethod (String, 255, Required)
   - isActive (Boolean, Required, Default: true)
   - sortOrder (Integer, Required, Default: 0)
   - createdAt (DateTime, Required)
   ```

   **e) `user_social_tasks` Collection**
   ```typescript
   Attributes:
   - userId (String, 255, Required)
   - taskId (String, 255, Required)
   - completedAt (DateTime, Required)
   ```

   **f) `achievements` Collection**
   ```typescript
   Attributes:
   - achievementId (String, 255, Required, Unique)
   - title (String, 255, Required)
   - description (String, 1000, Required)
   - type (String, 255, Required)
   - target (Double, Required)
   - reward (Double, Required)
   - rarity (String, 255, Required)
   - isActive (Boolean, Required, Default: true)
   - createdAt (DateTime, Required)
   ```

   **g) `user_achievements` Collection**
   ```typescript
   Attributes:
   - userId (String, 255, Required)
   - achievementId (String, 255, Required)
   - claimedAt (DateTime, Required)
   ```

   **h) `presale_purchases` Collection**
   ```typescript
   Attributes:
   - userId (String, 255, Required)
   - amountUsd (Double, Required)
   - tokensAmount (Double, Required)
   - transactionHash (String, 255, Optional)
   - status (String, 255, Required)
   - paymentMethod (String, 255, Optional)
   - createdAt (DateTime, Required)
   ```

   **i) `ad_views` Collection**
   ```typescript
   Attributes:
   - userId (String, 255, Required)
   - adType (String, 255, Required)
   - reward (Double, Required)
   - createdAt (DateTime, Required)
   ```

### Step 4: Set Collection Permissions

For each collection, set these permissions:
- **Create**: `users` (any authenticated user)
- **Read**: `users` (any authenticated user)
- **Update**: `users` (any authenticated user)
- **Delete**: `users` (any authenticated user)

### Step 5: Update Configuration Files

**Update `src/config/appwrite.ts`:**
```typescript
// Replace line 8:
const PROJECT_ID = 'your-actual-project-id-here';

// Replace lines 26-35 with your actual collection IDs:
collections: {
  users: 'your-actual-users-collection-id',
  userProfiles: 'your-actual-user-profiles-collection-id',
  miningSessions: 'your-actual-mining-sessions-collection-id',
  socialTasks: 'your-actual-social-tasks-collection-id',
  userSocialTasks: 'your-actual-user-social-tasks-collection-id',
  achievements: 'your-actual-achievements-collection-id',
  userAchievements: 'your-actual-user-achievements-collection-id',
  presalePurchases: 'your-actual-presale-purchases-collection-id',
  adViews: 'your-actual-ad-views-collection-id'
}
```

### Step 6: Test Configuration

```
# Test Appwrite connection
pnpm run test-appwrite

# Start development server
pnpm start
```

## 📁 Project Structure

```
mobile/
├── app/                           # Expo Router app directory
│   ├── (tabs)/                   # Tab navigation screens
│   │   ├── _layout.tsx          # Tab layout configuration
│   │   ├── mine.tsx             # Main mining interface
│   │   ├── social.tsx           # Social tasks and missions
│   │   ├── leaderboard.tsx      # Global rankings
│   │   ├── presale.tsx          # Token sale interface
│   │   ├── wallet.tsx           # Wallet management
│   │   └── profile.tsx          # User profile and settings
│   ├── oauth/                   # OAuth handling
│   │   └── return.tsx           # OAuth callback handler
│   ├── _layout.tsx              # Root layout with providers
│   ├── index.tsx                # Landing/redirect page
│   └── auth.tsx                 # Authentication screen
├── src/                          # Source code
│   ├── contexts/                # React Context providers
│   │   ├── AuthContext.tsx      # Authentication state
│   │   ├── MiningContext.tsx    # Mining operations
│   │   ├── WalletContext.tsx    # Wallet management
│   │   ├── NotificationContext.tsx # Toast notifications
│   │   ├── ReferralContext.tsx  # Referral system
│   │   └── PresaleContext.tsx   # Token presale
│   ├── components/              # Reusable UI components
│   │   ├── AchievementSystem.tsx
│   │   ├── AdModal.tsx
│   │   ├── AutoMiningStatus.tsx
│   │   └── NotificationSystem.tsx
│   ├── config/                  # Configuration files
│   │   ├── appwrite.ts          # Appwrite SDK setup
│   │   └── api.ts               # API configuration
│   ├── utils/                   # Utility functions
│   │   ├── validation.ts        # Form validation utilities
│   │   └── validation.test.ts   # Validation tests
│   └── types/                   # TypeScript definitions
│       └── index.ts             # Interface definitions
├── assets/                       # Static assets
├── android/                     # Android-specific code
├── ios/                         # iOS-specific code
├── docs/                        # Documentation
│   ├── APPWRITE_MIGRATION_GUIDE.md
│   ├── APPWRITE_MIGRATION_STATUS.md
│   ├── EMAIL_VALIDATION.md      # Email validation implementation
│   ├── EMAIL_VERIFICATION_AND_RECOVERY.md  # Email verification and password recovery (Hybrid Approach)
│   └── MIGRATION_REVIEW_REPORT.md
├── app.json                     # Expo configuration
├── eas.json                     # EAS Build configuration
├── package.json                 # Dependencies and scripts
├── tsconfig.json                # TypeScript configuration
├── babel.config.js              # Babel configuration
├── test-appwrite.js             # Appwrite connection test
└── migrate-data.js              # Data migration script
```

## 🔧 Development Guide

### Available Scripts

```bash
# Development
pnpm start                    # Start Expo dev server
pnpm run android             # Run on Android emulator/device
pnpm run ios                 # Run on iOS simulator/device
pnpm run web                 # Run web version (testing only)

# Backend Integration
pnpm run test-appwrite       # Test Appwrite connection
pnpm run migrate-data        # Migrate data to Appwrite

# Building
npx eas build --profile development --platform android
npx eas build --profile development --platform ios
npx eas build --profile production --platform android
npx eas build --profile production --platform ios

# App Store Submission
npx eas submit --platform android
npx eas submit --platform ios

# Web Deployment
pnpm run build               # Export for web
```

### Environment Setup

**iOS Development (macOS only):**
```bash
# Install Xcode from App Store
# Install iOS Simulator
# Install CocoaPods
sudo gem install cocoapods
```

**Android Development:**
```bash
# Install Android Studio
# Set up Android SDK
# Create Android Virtual Device (AVD)
# Set ANDROID_HOME environment variable
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/tools/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

## 🏗 Architecture & Design

### Architecture Pattern
- **Frontend**: React Native with Expo Router for file-based routing
- **State Management**: React Context API for modular state handling
- **Backend Integration**: Appwrite for authentication, database, and storage
- **Navigation**: Expo Router with tab-based navigation
- **Hybrid Approach**: Client-side business logic + Appwrite services

### Design Patterns
- **Context Pattern**: Global state management (Auth, Mining, Wallet, etc.)
- **Provider Pattern**: Wrapping app with context providers in `_layout.tsx`
- **Component-Based Architecture**: Reusable UI components
- **Separation of Concerns**: Clear separation between UI, business logic, and configuration

### Component Interaction
1. Root layout (`_layout.tsx`) initializes all context providers
2. Screens in `(tabs)` consume context data and trigger Appwrite API calls
3. Appwrite SDK handles authentication, database queries, and session management
4. Navigation managed via Expo Router with deep linking support

## 🔗 API Integration

### Appwrite Services Used
- **Authentication**: Google OAuth, email/password, session management
- **Database**: Document-based storage for all app data
- **Query System**: Real-time data filtering and retrieval
- **Permissions**: Role-based access control

### API Endpoints
- **Appwrite Endpoint**: `https://cloud.appwrite.io/v1`
- **Project ID**: Configured in `src/config/appwrite.ts`
- **Database ID**: `ekehi-network-db`

### Data Flow
1. User interacts with UI components
2. Context providers handle business logic
3. Appwrite SDK makes API calls
4. Real-time updates via Appwrite subscriptions
5. Local state updated and UI re-renders

## 🔐 Authentication System

### Supported Methods
- **Google OAuth**: Primary authentication method
- **Email/Password**: Alternative signup/login
- **Email Verification**: Confirm email ownership after signup (Hybrid Approach)
- **Password Recovery**: Reset forgotten passwords (Hybrid Approach)
- **Session Management**: Automatic token refresh
- **Deep Linking**: OAuth callbacks and verification/recovery handling
- **Form Validation**: Real-time email, password, and name validation

### Authentication Flow

**Mobile OAuth (Primary):**
1. User taps "Continue with Google"
2. App calls Appwrite SDK to initiate OAuth
3. Browser opens for Google authentication
4. Google redirects to `ekehi://oauth/callback`
5. Appwrite creates session automatically
6. User logged in and redirected to main app

**Email/Password Authentication:**
1. User enters email and password
2. Form validates email format and password strength
3. App calls Appwrite SDK to create session
4. User logged in and redirected to main app

**Web OAuth (Testing):**
1. User visits deployed web app
2. Clicks "Continue with Google"
3. Appwrite SDK handles OAuth flow
4. Session created and user redirected

### Form Validation

The app implements comprehensive form validation for email, password, and name fields:

- **Email Validation**: Regex-based validation for proper email format
- **Password Validation**: Length requirements (6-128 characters)
- **Name Validation**: Length and character restrictions (2-50 characters, letters and spaces only)
- **Real-time Feedback**: Immediate validation errors as users type
- **Submission Validation**: Complete form validation before authentication requests

See [`EMAIL_VALIDATION.md`](EMAIL_VALIDATION.md) for implementation details.

## 🎯 State Management

### Context Providers
- **AuthContext**: User authentication and session management
- **MiningContext**: Mining operations and user profile data
- **WalletContext**: Wallet connection and transaction management
- **NotificationContext**: Toast notifications and alerts
- **ReferralContext**: Referral system and code management
- **PresaleContext**: Token presale and purchase tracking

### Data Persistence
- **AsyncStorage**: Local caching for offline support
- **Appwrite Database**: Primary data storage
- **Session Tokens**: Managed by Appwrite SDK

## 🧭 Navigation

### Tab Navigation Structure
- **Mine**: Main mining interface and dashboard
- **Social**: Social tasks and mission completion
- **Leaderboard**: Global user rankings
- **Presale**: Token presale and purchase
- **Wallet**: Token management and transfers
- **Profile**: User account and settings

### Route Configuration
``typescript
// File-based routing with Expo Router
app/
├── (tabs)/           # Tab navigator
│   ├── mine.tsx     # /mine
│   ├── social.tsx   # /social
│   └── ...          # Other tabs
├── auth.tsx         # /auth
└── index.tsx        # /
```

## 🎨 UI/UX Design

### Design System
- **Color Palette**:
  - Primary Gold: `#ffa000`
  - Secondary Purple: `#8b5cf6`
  - Accent Blue: `#3b82f6`
  - Background Dark: `#1a1a2e`
- **Typography**: System fonts with consistent sizing
- **Gradients**: Linear gradients for backgrounds and buttons
- **Touch Targets**: Minimum 44px for accessibility

### Animations
- **React Native Reanimated**: High-performance 60fps animations
- **Haptic Feedback**: Native touch feedback on interactions
- **Micro-interactions**: Smooth transitions and visual feedback

## 📱 Platform Features

### iOS Specific
- Native haptic feedback patterns
- iOS-style navigation animations
- App Store Connect integration
- iOS design guidelines compliance

### Android Specific
- Material Design components
- Android haptic feedback
- Google Play Console integration
- Android design guidelines compliance

### Web (Testing)
- Progressive Web App capabilities
- Web-specific OAuth handling
- Responsive design for different screen sizes
- Browser compatibility testing

## ⚡ Performance

### Optimizations
- **Lazy Loading**: Route-based code splitting
- **Memoization**: React.memo for expensive components
- **Image Optimization**: Compressed assets and lazy loading
- **AsyncStorage**: Efficient local data caching
- **Database Queries**: Optimized Appwrite queries with indexing

### Monitoring
- Real-time performance tracking
- Memory usage optimization
- Network request optimization
- Battery usage considerations

## 🏗 Building & Deployment

### Development Builds
```bash
# iOS Development Build
npx eas build --profile development --platform ios

# Android Development Build
npx eas build --profile development --platform android
```

### Production Builds
```bash
# iOS Production Build
npx eas build --profile production --platform ios

# Android Production Build
npx eas build --profile production --platform android
```

### App Store Submission
```bash
# Submit to Apple App Store
npx eas submit --platform ios

# Submit to Google Play Store
npx eas submit --platform android
```

### Web Deployment
```bash
# Export for web
pnpm run build

# Deploy to Vercel (automatic on push)
# URL: https://ekehi-network-nojxotiyc-kamal-s-projects.vercel.app
```

## 🧪 Testing

### Test Coverage
- Component rendering and interactions
- Appwrite integration and error handling
- Authentication flows (mobile and web)
- Navigation and user journeys
- Mining logic and calculations
- State management functionality

### Testing Commands
```bash
# Test Appwrite connection
pnpm run test-appwrite

# Run on device for manual testing
pnpm run ios
pnpm run android
```

## 🔄 Migration from Cloudflare to Appwrite

### Migration Status: ✅ COMPLETE

The app has been successfully migrated from Cloudflare Workers to Appwrite:

**What Changed:**
- ✅ Replaced custom Cloudflare API with Appwrite SDK
- ✅ Migrated from D1 database to Appwrite Database
- ✅ Updated authentication to use Appwrite Auth
- ✅ Implemented hybrid approach for 5-function limit
- ✅ Fixed all TypeScript compilation errors
- ✅ Updated all context providers

**Migration Benefits:**
- 🚀 Simplified architecture (no custom backend to maintain)
- 🔒 Professional-grade security out of the box
- 📈 Automatic scaling handled by Appwrite
- 🛠 Better developer experience with dashboard
- 💰 Cost efficiency with generous free tier

**Migration Documentation:**
- [`APPWRITE_MIGRATION_GUIDE.md`](APPWRITE_MIGRATION_GUIDE.md) - Complete migration guide
- [`APPWRITE_MIGRATION_STATUS.md`](APPWRITE_MIGRATION_STATUS.md) - Migration status tracking
- [`MIGRATION_REVIEW_REPORT.md`](MIGRATION_REVIEW_REPORT.md) - Final review report

## 🔗 Ekehi Network Blockchain Integration

### Integration Status: ✅ IMPLEMENTED

The wallet system has been enhanced to use the Ekehi Network blockchain for actual token transactions:

**What Changed:**
- ✅ Integrated Ekehi Network API for blockchain transactions
- ✅ Wallet balance now fetched directly from blockchain
- ✅ Token transfers processed through Ekehi Network
- ✅ Hybrid approach maintains Appwrite for user profiles
- ✅ Secure API key management (placeholder for development)

**Integration Benefits:**
- 🔗 Real blockchain transactions for token transfers
- 🛡️ Enhanced security through blockchain validation
- ⚡ Real-time balance updates from network
- 🌐 Decentralized transaction processing

**Integration Documentation:**
- [`EKEHI_NETWORK_INTEGRATION.md`](EKEHI_NETWORK_INTEGRATION.md) - Complete integration guide

## 🐛 Troubleshooting

### Common Issues

**Issue: "Collection not found" errors**
```bash
# Solution: Verify collection IDs match Appwrite dashboard
# Check src/config/appwrite.ts collection IDs
```

**Issue: "Permission denied" errors**
```bash
# Solution: Check collection permissions in Appwrite dashboard
# Ensure 'users' role has read/write access
```

**Issue: OAuth not working**
```bash
# Solution: Verify OAuth configuration
# Check Google Console and Appwrite Auth settings
# Ensure redirect URLs match exactly
```

**Issue: "YOUR_PROJECT_ID" warning**
```bash
# Solution: Replace placeholder in src/config/appwrite.ts
# Get real project ID from Appwrite dashboard
```

### Debug Commands
```bash
# Test Appwrite connection
pnpm run test-appwrite

# Check Expo environment
npx expo doctor

# Clear Expo cache
npx expo start --clear

# Reset Metro bundler
npx expo start --reset-cache
```

### Support Resources
- [Appwrite Documentation](https://appwrite.io/docs)
- [Expo Documentation](https://docs.expo.dev/)
- [React Native Documentation](https://reactnative.dev/docs/getting-started)
- [Project Issues](https://github.com/your-repo/issues)

## 🤝 Contributing

### Development Workflow
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Make your changes and test thoroughly
4. Follow TypeScript best practices
5. Test on both iOS and Android
6. Update documentation if needed
7. Commit your changes: `git commit -m 'Add new feature'`
8. Push to the branch: `git push origin feature/new-feature`
9. Submit a pull request

### Code Standards
- Use TypeScript with strict mode
- Follow React Native best practices
- Use meaningful commit messages
- Add comments for complex logic
- Test changes on both platforms

### Review Process
- All PRs require review
- CI/CD checks must pass
- Test on physical devices when possible
- Update relevant documentation

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 📞 Support & Contact

For support, questions, or contributions:
- **Issues**: [GitHub Issues](https://github.com/your-repo/issues)
- **Documentation**: [Migration Reports](MIGRATION_REVIEW_REPORT.md)
- **Appwrite Help**: [Appwrite Discord](https://appwrite.io/discord)

**Project Status**: ✅ Production Ready (after Appwrite configuration)
**Last Updated**: January 2025
**Version**: 1.0.0

## 📈 Mining Session Recording

The Ekehi Network app now records detailed mining sessions to track user engagement and enable advanced analytics. This feature was implemented to address the issue where mining sessions and token awards were not being recorded.

### How It Works

1. **Session Tracking**: When users start mining (either manual or auto mining), a session is initiated
2. **Data Collection**: During the session, the app tracks:
   - Coins earned
   - Mining clicks performed
   - Session duration
3. **Session Recording**: When mining stops, a record is created in the Appwrite `mining_sessions` collection

### Implementation Details

- **Enhanced Mining Context**: The [MiningContext](file:///c:/Users/ARQAM%20TV/Downloads/mobile/src/contexts/MiningContext.tsx#L15-L32) now includes session recording functionality
- **Automatic Recording**: Sessions are recorded when users navigate away from mining pages or background the app
- **Spam Prevention**: Sessions shorter than 5 seconds are filtered out to prevent database spam
- **Cross-component Support**: Both manual and auto mining sessions are recorded

### Data Structure

Each mining session record contains:
- `userId`: The ID of the user who mined
- `coinsEarned`: Total coins earned during the session
- `clicksMade`: Number of mining clicks performed
- `sessionDuration`: Duration of the session in seconds
- `createdAt`: Timestamp when the session was recorded

### Verification

To verify that mining sessions are being recorded:
1. Perform mining activities in the app
2. Navigate away from the mining page or background the app
3. Check the Appwrite dashboard for new documents in the `mining_sessions` collection

For detailed implementation information, see [Mining Session Recording Documentation](./docs/MINING_SESSION_RECORDING.md).

## 🛠 Manual Mining System Update

> **⚠️ IMPORTANT**: The manual mining system has been updated from a per-click model to a 24-hour session model. See [Manual Mining System Update Documentation](Documentations/MANUAL_MINING_SYSTEM_UPDATE.md) for details.

### Overview
The manual mining system has been updated to provide a simpler, more predictable user experience. Instead of earning coins per click, users now start a 24-hour mining session with a single tap and earn a fixed 2 EKH reward upon completion.

### Key Changes
1. **24-Hour Session Model**: Single tap starts a 24-hour mining session
2. **Fixed Reward**: 2 EKH reward for completing 24-hour session
3. **Fixed Rate Display**: 0.0833 EKH/hour shown to all users
4. **Removed Per-Click Earnings**: Eliminated `coinsPerClick` property from user profiles

### Benefits
- **Simplicity**: Single tap to start mining instead of continuous interaction
- **Predictability**: Fixed reward system that's easy to understand
- **Engagement**: Encourages daily app interaction to check progress
- **Consistency**: All users see the same hourly rate

### Technical Details
- Removed `coinsPerClick` from `UserProfile` interface
- Updated `performMine` function to track session clicks without immediate rewards
- Modified user profile creation to exclude `coinsPerClick`
- Updated mining rate display to fixed 0.0833 EKH/hour

For complete implementation details, see [MANUAL_MINING_SYSTEM_UPDATE.md](Documentations/MANUAL_MINING_SYSTEM_UPDATE.md).

## 💰 Presale Page

The presale page has been enhanced with improved visual design, better user experience, and additional functionality to make token purchasing more engaging and user-friendly.

### Key Enhancements

1. **Visual Design Improvements**
   - Added dynamic progress bar showing presale progress
   - Enhanced token information display
   - Improved auto mining section with current rate display

2. **User Experience Improvements**
   - Quick purchase amount buttons ($10, $25, $50, $100)
   - Expanded benefits section with detailed descriptions
   - Better purchase history organization

3. **Functionality Enhancements**
   - Improved error handling with detailed messages
   - Progress animations for better engagement
   - Security information section

### New Features

- **Presale Progress Tracker**: Visual progress bar with statistics
- **Quick Purchase Buttons**: One-tap purchase amounts
- **Enhanced Benefits Display**: Detailed benefit descriptions with icons

For detailed information about the presale page enhancements, see [Presale Page Enhancements](./docs/PRESALE_PAGE_ENHANCEMENTS.md).
