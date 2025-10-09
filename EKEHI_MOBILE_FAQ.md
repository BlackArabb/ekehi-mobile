# Ekehi Network Mobile App - Frequently Asked Questions (FAQ)

## Table of Contents
1. [General Project Overview](#1-general-project-overview)
2. [Authentication and Account Management](#2-authentication-and-account-management)
3. [Mining System](#3-mining-system)
4. [Wallet Functionality](#4-wallet-functionality)
5. [Referral Program](#5-referral-program)
6. [Social Tasks](#6-social-tasks)
7. [Presale System](#7-presale-system)
8. [Technical Setup and Development](#8-technical-setup-and-development)
9. [Troubleshooting](#9-troubleshooting)
10. [Platform-Specific Information](#10-platform-specific-information)

## 1. General Project Overview

### What is Ekehi Network?
Ekehi Network is a cryptocurrency platform featuring a mobile application that allows users to mine EKH tokens through an interactive tap-to-mine system. The platform also includes social task completion, a referral program, token presale, wallet integration, and an achievement system.

### What platforms does Ekehi Network support?
The Ekehi Network mobile app is built with React Native and Expo, providing cross-platform support for:
- iOS devices
- Android devices
- Web browsers (through Expo web build)

Additionally, there is a separate landing page built with React and Vite, and an admin dashboard built with Next.js.

### What are the main features of the Ekehi Network app?
The Ekehi Network app offers several core features:
- **Tap-to-Mine Interface**: Touch-optimized mining with haptic feedback and real-time balance updates
- **Mining Power System**: Upgradeable mining power with automatic calculations
- **Daily Streak Bonuses**: Consecutive login rewards and streak multipliers
- **Social Task Engine**: Complete tasks on Twitter, YouTube, Telegram, and Discord for rewards
- **Multi-level Referral System**: Share referral codes and earn bonuses based on referral tiers
- **Token Presale**: Early access token purchasing with automatic mining unlocks
- **Wallet Integration**: Manage token balances, send transactions, and view history
- **Achievement System**: Milestone-based rewards and progression tracking
- **Global Leaderboards**: Competitive rankings

### How does the tap-to-mine system work?
The tap-to-mine system is the core feature of the Ekehi Network app. Users can earn EKH tokens by tapping on a large circular mining button on the main screen. Each tap provides haptic feedback and visual effects, with the amount earned based on your mining power. The system also includes:
- Electric effects around the button
- Glow effect on interaction
- Session stats showing earned coins and taps
- Progress bar for daily mining limits

### What is the EKH token?
EKH is the native cryptocurrency token of the Ekehi Network. Users can earn EKH tokens through the tap-to-mine system, complete social tasks, participate in the referral program, and purchase tokens during the presale. EKH tokens can be stored in the integrated wallet, sent to other users, and used within the Ekehi ecosystem.

## 2. Authentication and Account Management

### How do I create an account?
To create an account on Ekehi Network:
1. Open the app and tap "Start Mining"
2. Choose your preferred authentication method:
   - Continue with Google (OAuth)
   - Continue with Email (email/password)
3. Follow the prompts to complete registration
4. If using email authentication, you'll receive a verification email

### Can I use Google to sign in?
Yes, Ekehi Network supports Google OAuth authentication. This is the primary authentication method and provides a seamless sign-in experience. To use Google sign-in:
1. Tap "Continue with Google" on the authentication screen
2. Select your Google account
3. Grant necessary permissions
4. You'll be redirected back to the app once authentication is complete

### What should I do if I forget my password?
If you've signed up with email authentication and forget your password:
1. On the sign-in screen, tap "Forgot Password"
2. Enter your email address
3. Check your email for a password reset link
4. Follow the link and set a new password
5. You'll be redirected back to the app after resetting your password

### How secure is my data?
Ekehi Network takes user security seriously:
- All communications use HTTPS encryption
- Passwords are securely hashed and stored
- OAuth tokens are properly managed
- User data is stored in the Appwrite backend with appropriate security measures
- Email verification is required for email-based accounts

## 3. Mining System

### How do I mine EKH tokens?
To mine EKH tokens:
1. Open the Ekehi Network app
2. Navigate to the "Mine" tab (usually the first tab)
3. Tap the large circular mining button
4. Each tap will earn you EKH tokens based on your mining power
5. View your earnings in real-time on the screen

### What is the mining rate?
The default mining rate is 2 EKH tokens per day, which equates to approximately 0.083 EKH per hour. Your actual mining rate may be higher based on:
- Referral bonuses (0.2 EKH/second increase per referral)
- Streak bonuses
- Special promotions or events

### How do streaks work?
Ekehi Network rewards daily logins with streak bonuses:
- Log in each day to maintain your streak
- Longer streaks provide increasing bonus multipliers
- Missing a day will reset your streak to zero
- Streak bonuses are applied to your mining rate

### What affects my mining power?
Several factors can affect your mining power:
- **Base Rate**: All users start with the default 2 EKH/day rate
- **Referrals**: Each successful referral increases your rate by 0.2 EKH/second
- **Streak Bonuses**: Consecutive daily logins provide mining rate multipliers
- **Future Updates**: Additional power-ups and upgrades may be added in future versions

## 4. Wallet Functionality

### How do I connect my wallet?
To connect your wallet in the Ekehi Network app:
1. Navigate to the "Wallet" tab
2. If your wallet isn't connected, you'll see a "Connect Wallet" button
3. Tap "Connect Wallet"
4. The app will generate a wallet address for you
5. Your wallet is now connected and ready to use

### How do I send tokens to others?
To send EKH tokens to another user:
1. Navigate to the "Wallet" tab
2. Ensure your wallet is connected
3. Enter the recipient's wallet address in the "Recipient Address" field
4. Enter the amount of EKH tokens you wish to send
5. Tap "Send Tokens"
6. Confirm the transaction in the confirmation dialog

### How do I check my transaction history?
To view your transaction history:
1. Navigate to the "Wallet" tab
2. Scroll down to the "Recent Transactions" section
3. View your transaction history, including:
   - Transaction type (sent or received)
   - Amount of EKH tokens
   - Transaction status (pending, completed, or failed)
   - Date of transaction

### What is my wallet address?
Your wallet address is a unique identifier that allows you to receive EKH tokens from other users. To find your wallet address:
1. Navigate to the "Wallet" tab
2. Ensure your wallet is connected
3. Your wallet address will be displayed under "Address" in the balance card
4. You can share this address with others to receive tokens

## 5. Referral Program

### How does the referral system work?
The Ekehi Network referral system allows you to earn rewards by inviting friends:
1. Each user has a unique referral code
2. Share your referral code with friends
3. When friends use your code, both you and your friend receive rewards
4. Your mining rate increases with each successful referral

### How much can I earn from referrals?
The referral reward system works as follows:
- **For Referees**: New users receive 2.0 EKH coins for using a referral code
- **For Referrers**: You receive an increased mining rate of 0.2 EKH/second for each referral
- **Maximum Referrals**: Each user can refer up to 50 new users

### How do I share my referral code?
To share your referral code:
1. Navigate to the "Profile" tab
2. Find your referral code displayed on the screen
3. Use the "Copy" button to copy your code to the clipboard
4. Use the "Share" button to share your code via social media, messaging apps, or other platforms

### Is there a limit to how many people I can refer?
Yes, each user can successfully refer up to 50 new users. After reaching this limit, you won't be able to earn additional referral bonuses, but you can continue to use the app normally.

## 6. Social Tasks

### What are social tasks?
Social tasks are activities you can complete on various social media platforms to earn additional EKH tokens. These tasks help promote the Ekehi Network while providing rewards to users.

### How do I complete social tasks?
To complete social tasks:
1. Navigate to the "Social" tab
2. View the available social tasks (Twitter, YouTube, Telegram, Discord)
3. Tap on a task to open the corresponding platform
4. Complete the required action (follow, like, join, etc.)
5. Return to the app to claim your reward

### What rewards do I get for completing tasks?
Each social task has a specific EKH token reward. The exact amount varies by task and platform. Rewards are typically between 5-20 EKH tokens per completed task, depending on the difficulty and platform.

### Which platforms are supported for social tasks?
Currently, Ekehi Network supports social tasks on:
- Twitter (X)
- YouTube
- Telegram
- Discord
- Other platforms may be added in future updates

## 7. Presale System

### What is the token presale?
The token presale is an early access opportunity for users to purchase EKH tokens before they are available to the general public. Participating in the presale also unlocks additional mining features in the app.

### How do I participate in the presale?
To participate in the token presale:
1. Navigate to the "Presale" tab
2. Enter the amount of tokens you wish to purchase
3. Complete the purchase process
4. Your purchased tokens will be added to your wallet
5. Additional mining features will be unlocked automatically

### What are the minimum and maximum purchase amounts?
The presale system has the following limits:
- **Minimum Purchase**: 10 EKH tokens
- **Maximum Purchase**: 10,000 EKH tokens per transaction
- These limits may vary during special promotional periods

### What do I get for participating in the presale?
By participating in the presale, you receive:
- The EKH tokens you purchased
- Early access to mining features that are locked for non-presale users
- Priority access to future platform features
- Potential bonus tokens during promotional periods

## 8. Technical Setup and Development

### How do I set up the development environment?
To set up the development environment for the Ekehi Network mobile app:
1. Install Node.js 18 or higher
2. Install pnpm package manager
3. Install Expo CLI
4. Clone the repository
5. Run `pnpm install` in the project directory
6. Configure Appwrite settings in `src/config/appwrite.ts`
7. Start the development server with `pnpm start`

### What are the prerequisites for running the app?
The prerequisites for running the Ekehi Network app are:
- **Node.js**: Version 18 or higher
- **Package Manager**: pnpm (recommended) or npm
- **Expo CLI**: For development and building
- **Mobile Device**: For testing on iOS or Android (or simulators)
- **Google Account**: For OAuth authentication testing

### How do I configure OAuth?
To configure Google OAuth for the Ekehi Network app:
1. Set up a project in Google Cloud Console
2. Enable the required APIs (Google+, People API, Google Identity Services)
3. Configure the OAuth consent screen
4. Create OAuth 2.0 Client IDs for Android, iOS, and Web
5. Register platforms in the Appwrite Console
6. Enable and configure the Google OAuth provider in Appwrite Authentication settings
7. Update the redirect URIs in Google Cloud Console

### How do I run the app locally?
To run the app locally for development:
1. Navigate to the project directory
2. Run `pnpm install` to install dependencies
3. Run `pnpm start` to start the development server
4. Use the Expo Go app on your mobile device or a simulator/emulator to view the app
5. For web development, visit `http://localhost:8081` in your browser

## 9. Troubleshooting

### What should I do if the app crashes?
If the Ekehi Network app crashes:
1. Restart the app
2. Check for app updates in your device's app store
3. Clear the app cache and data (device settings > Apps > Ekehi Network > Storage)
4. Reinstall the app if the problem persists
5. Report the issue through the app's support channels with details about when the crash occurred

### Why am I not receiving mining rewards?
If you're not receiving expected mining rewards:
1. Check your internet connection
2. Ensure you're properly authenticated
3. Verify your wallet is connected
4. Check if you've exceeded daily mining limits
5. Restart the app to refresh your profile data
6. Contact support if the issue persists

### What if I can't connect my wallet?
If you're having trouble connecting your wallet:
1. Ensure you're logged in to the app
2. Check your internet connection
3. Restart the wallet connection process
4. Clear the app cache and try again
5. Check if the Ekehi Network API is experiencing issues
6. Contact support if problems continue

### How do I report bugs or issues?
To report bugs or issues with the Ekehi Network app:
1. Use the in-app feedback feature if available
2. Open an issue in the project's GitHub repository
3. Contact the development team through official support channels
4. Provide detailed information including:
   - Device type and operating system
   - App version
   - Steps to reproduce the issue
   - Screenshots if applicable

## 10. Platform-Specific Information

### Is there an iOS app?
Yes, Ekehi Network is available for iOS devices. The app is built with React Native and Expo, providing native performance on iOS devices. The app requires iOS 12.0 or later.

### Is there an Android app?
Yes, Ekehi Network is available for Android devices. The app is built with React Native and Expo, providing native performance on Android devices. The app requires Android 6.0 (API level 23) or later.

### Is there a web version?
Yes, Ekehi Network can be accessed through web browsers using Expo's web build capabilities. The web version provides the same core functionality as the mobile apps with a responsive interface that works on desktop and mobile browsers.

### Are there any differences between platforms?
The core functionality is consistent across all platforms (iOS, Android, and web). However, there may be minor differences due to platform-specific constraints:
- **Haptic Feedback**: May vary between iOS and Android devices
- **Deep Linking**: Implementation differs between platforms
- **Performance**: May vary based on device capabilities
- **Push Notifications**: Platform-specific implementation
- **Native Features**: Some device-specific features may be available on mobile but not web