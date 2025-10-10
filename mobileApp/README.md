# Ekehi Network Mobile App

## Project Overview

Ekehi Network is a mobile mining application built with React Native and Expo, utilizing Appwrite as the backend service. The app allows users to mine EKH tokens through various activities and features a comprehensive presale system with auto-mining capabilities.

## Key Features

### 🔧 Core Functionality
- **User Authentication**: Email/password and Google OAuth sign-in
- **Token Mining**: Interactive mining with real-time coin accumulation
- **Auto Mining**: Passive income generation based on presale purchases
- **Social Tasks**: Engagement-based reward system
- **Achievements**: Milestone-based recognition system
- **Referral Program**: User acquisition through invite bonuses
- **Token Presale**: Early access token purchase with benefits
- **Wallet Management**: Secure token storage and transaction history
- **Leaderboard**: Community ranking based on mining performance

### 🎯 Auto Mining System
- **Eligibility**: Users must purchase a minimum amount during presale to unlock auto mining
- **Rate Calculation**: Mining rate increases proportionally with purchase amount
- **Maximum Limits**: 
  - Maximum Mining Rate Purchase Amount (mmPA): $10,000 for mining rate calculation
  - Maximum General Purchase Amount (mGPA): $50,000 total purchase limit
  - Maximum Mining Rate (mMR): 10 EKH/second cap
- **Real-time Updates**: Automatic profile updates without UI refresh disruption

### 📱 Technical Features
- **Cross-platform**: iOS and Android support via Expo
- **Real-time Updates**: WebSocket-powered live data synchronization
- **Offline Support**: AsyncStorage-based local data persistence
- **Performance Optimized**: Memoization and efficient rendering
- **Error Resilience**: Comprehensive error handling and retry mechanisms
- **Security**: Appwrite-powered authentication and data protection

## 🚀 Getting Started

### Prerequisites
- Node.js (v16 or higher)
- npm or pnpm
- Expo CLI
- Android Studio or Xcode for mobile development

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd mobileApp
```

2. Install dependencies:
```bash
npm install
# or
pnpm install
```

3. Start the development server:
```bash
npm start
# or
pnpm start
```

## 📁 Project Structure

```
mobileApp/
├── app/                 # App Router pages and layouts
├── src/                 # Source code
│   ├── components/      # Reusable UI components
│   ├── contexts/        # React Context providers
│   ├── services/        # API and utility services
│   ├── types/           # TypeScript type definitions
│   └── utils/           # Helper functions
├── assets/              # Images, fonts, and other assets
├── Documentations/      # Project documentation
└── Scripts/             # Utility scripts
```

## 🔐 Authentication Setup

### OAuth Configuration
To set up Google OAuth authentication:

1. Configure Google Cloud Platform OAuth credentials
2. Register platforms in Appwrite Console
3. Add redirect URLs:
   - Success: `ekehi://oauth/return`
   - Failure: `ekehi://auth`

For detailed instructions on configuring OAuth, see:
- [OAuth Setup Guide](./Documentations/OAUTH_SETUP_GUIDE.md)
- [Appwrite OAuth Configuration Fix](./Documentations/APPWRITE_OAUTH_FIX.md)
- [Appwrite Mobile Platform Setup Guide](./Documentations/APPWRITE_MOBILE_PLATFORM_SETUP.md)

### Test Credentials
A test user is pre-configured for development:
- Email: `test@ekehi.network`
- Password: `testpassword123`

## 🛠 Development

### Running the App
```bash
# Start development server
npm start

# Run on Android
npm run android

# Run on iOS
npm run ios

# Run in web browser
npm run web
```

### Testing OAuth Configuration
```bash
npm run test-oauth-config
```

### Building for Production
```bash
# Build for Android
npx expo build:android

# Build for iOS
npx expo build:ios

# Build for web
npx expo build:web
```

## 📖 Documentation

- [Feature Documentation](./Documentations/FEATURE_DOCUMENTATION.md)
- [Auto Mining Feature](./Documentations/AUTO_MINING_FEATURE.md)
- [OAuth Setup Guide](./Documentations/OAUTH_SETUP_GUIDE.md)
- [Appwrite OAuth Configuration Fix](./Documentations/APPWRITE_OAUTH_FIX.md)
- [Appwrite Mobile Platform Setup Guide](./Documentations/APPWRITE_MOBILE_PLATFORM_SETUP.md)
- [Auto Mining Improvements Summary](./Documentations/AUTO_MINING_IMPROVEMENTS_SUMMARY.md)

## 🐛 Troubleshooting

### Common Issues

1. **OAuth Redirect Errors**: Ensure redirect URLs are properly registered in Appwrite Console
2. **Auto Mining Not Updating**: Check purchase amounts meet minimum requirements
3. **Profile Refresh Issues**: Verify network connectivity and Appwrite configuration

### Need Help?

If you're having trouble with any part of the setup:
1. Run the OAuth test script: `npm run test-oauth-config`
2. Check the detailed documentation files listed above
3. Review Appwrite and Expo documentation
4. Reach out to the development team

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.