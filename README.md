# Ekehi Network Mobile App & Landing Page

## Overview

A React Native mobile application for the Ekehi Network cryptocurrency mining platform, featuring an interactive tap-to-mine system, social task engine, referral program, token presale, wallet integration, and achievement system.

The project also includes a modern, responsive landing page built with Next.js for web presence and marketing.

## 🚀 Features

### Core Mining System
- **🎯 Tap-to-Mine Interface**: Touch-optimized mining with haptic feedback and real-time balance updates
- **⚡ Mining Power System**: Upgradeable mining power with automatic calculations
- **🔥 Daily Streak Bonuses**: Consecutive login rewards and streak multipliers
- **📊 Real-time Analytics**: Live mining statistics and performance tracking
- **🔄 Dynamic Mining Rates**: Personalized hourly mining rates based on user profile data

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
- **🛡 Security Features**: Email verification and password recovery
- **📱 Cross-platform**: Native iOS, Android, and web compatibility
- **💾 Offline Support**: Local data persistence with AsyncStorage
- **🔗 Deep Linking**: OAuth callbacks and referral link handling

## 🛠 Technology Stack

### Frontend Technologies
- **React Native** - Cross-platform mobile development framework
- **Expo SDK** - Development toolchain and runtime platform
- **TypeScript** - Type-safe JavaScript development
- **React Navigation** - Bottom tabs and native stack navigation
- **React Native Reanimated** - High-performance animations

### Backend & Services
- **Appwrite** - Backend-as-a-Service for authentication and database
- **Appwrite Database** - Document-based database for all app data
- **Appwrite Auth** - Google OAuth and email/password authentication

### UI & Design
- **Expo Linear Gradient** - Beautiful gradient effects and backgrounds
- **Expo Haptics** - Native touch feedback and vibrations
- **Lucide React Native** - Modern icon library

## ⚡ Quick Start

### Prerequisites
- **Node.js 18+**
- **pnpm** - Package manager
- **Expo CLI**

### Installation

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd ekehi-mobile
   ```

2. **Install Dependencies**
   ```bash
   pnpm install
   ```

3. **Configure Appwrite**
   - Set up Appwrite project and collections
   - Update configuration in `src/config/appwrite.ts`

4. **Start Development Server**
   ```bash
   pnpm start
   ```

## 📁 Project Structure

```
ekehi-mobile/
├── Ekehi-LandingPage/      # Next.js landing page
│   ├── components/         # Modern UI components
│   ├── pages/              # Next.js pages
│   ├── public/             # Static assets
│   └── styles/             # Global CSS styles
├── app/                    # Expo Router app directory
│   ├── (tabs)/            # Tab navigation screens
│   ├── oauth/             # OAuth handling
│   ├── _layout.tsx        # Root layout with providers
│   ├── index.tsx          # Landing/redirect page
│   └── auth.tsx           # Authentication screen
├── src/                   # Source code
│   ├── contexts/          # React Context providers
│   ├── components/        # Reusable UI components
│   ├── config/            # Configuration files
│   ├── utils/             # Utility functions
│   └── types/             # TypeScript definitions
├── assets/                # Static assets
├── android/               # Android-specific code
├── ios/                   # iOS-specific code
├── Documentations/        # Project documentation
├── admin/                 # Admin dashboard
└── Scripts/               # Utility scripts
```

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

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support, please open an issue in the GitHub repository.