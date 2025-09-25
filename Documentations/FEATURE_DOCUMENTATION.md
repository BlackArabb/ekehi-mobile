# Ekehi Network Mobile App - Feature Documentation

## 1.0 App Overview
The Ekehi Network Mobile App is a cryptocurrency mining platform that allows users to earn EKH tokens through various activities including tap-to-mine, social tasks, referrals, and more. The app is built using React Native with Expo and follows a modular architecture with TypeScript.

## 2.0 Technology Stack
- **Framework**: React Native with Expo (v51.0.28)
- **Language**: TypeScript (v5.3.3)
- **Routing**: Expo Router for file-based routing
- **State Management**: React Context API
- **UI Components**: Lucide icons, Expo Linear Gradient, React Native Reanimated
- **API Integration**: Cloudflare Workers backend
- **Authentication**: Google OAuth with deep linking
- **Local Storage**: AsyncStorage
- **Animations**: React Native Reanimated
- **Haptics**: Expo Haptics
- **UI Styling**: React Native StyleSheet with gradient backgrounds

## 3.0 Core Features

### 3.1 Tap-to-Mine System
**Description**: The core mining feature that allows users to earn EKH tokens by tapping on a mining button.

**Key Features**:
- Animated mining button with electric effects
- Haptic feedback on each tap
- Visual click effects with EKH amount shown
- Mining rate based on user level and power
- Daily mining limit with progress bar

**Technical Implementation**:
- Implemented in `app/(tabs)/mine.tsx`
- Uses [react-native-reanimated](file://c:\Users\ARQAM%20TV\Downloads\mobile\node_modules\react-native-reanimated) for button animation
- Haptics feedback with [expo-haptics](file://c:\Users\ARQAM%20TV\Downloads\mobile\node_modules\expo-haptics)
- Tracks session coins and clicks
- Visual effects with `LinearGradient` and position animation

**UI/UX**:
- Large circular mining button with pickaxe icon
- Electric effects around the button
- Glow effect on interaction
- Session stats showing earned coins and taps
- Progress bar for daily mining limit

### 3.2 Social Task Engine
**Description**: A system that allows users to complete platform-specific tasks (Twitter, YouTube, Telegram, Discord) for bonus rewards.

**Key Features**:
- Platform-specific task icons
- Deep linking to platform apps
- Reward system for completed tasks
- Task tracking and history

**Technical Implementation**:
- Implemented in `app/(tabs)/social.tsx`
- Platform detection with icon mapping
- Uses `Linking` to open platform apps
- Task completion tracking

**UI/UX**:
- Cards for each social task
- Platform-specific icons (Twitter, YouTube, etc.)
- Task descriptions and rewards
- Completion status indicators

### 3.3 Multi-Level Referral System
**Description**: A referral program that rewards users for inviting friends to the platform.

**Key Features**:
- Unique referral code for each user
- Referral tracking and statistics
- Mining rate bonuses for successful referrals
- Shareable referral link
- Copy to clipboard functionality

**Technical Implementation**:
- Implemented in `app/(tabs)/profile.tsx`
- Uses [expo-clipboard](file://c:\Users\ARQAM%20TV\Downloads\mobile\node_modules\expo-clipboard) for code copying
- Referral tracking in user profile data
- Referral statistics displayed in profile

**UI/UX**:
- Referral code display with copy button
- Share button for social media
- Referral statistics (total referrals, rewards)
- Visual indicators of referral bonuses

### 3.4 Token Presale
**Description**: An early access token purchasing system that unlocks additional mining features.

**Key Features**:
- Token purchase form
- Auto-mining unlock based on purchase
- Purchase history tracking
- Minimum purchase amount enforcement

**Technical Implementation**:
- Implemented in `app/(tabs)/presale.tsx`
- Purchase validation with min/max checks
- API integration for purchase processing
- Visual feedback on purchase success/failure

**UI/UX**:
- Purchase amount input field
- Price display with EKH token conversion
- Transaction history
- Visual feedback on purchase success

### 3.5 Wallet Management
**Description**: A system for managing EKH tokens, including sending, receiving, and transaction history.

**Key Features**:
- Wallet connection/disconnection
- Token sending functionality
- Transaction history
- Address management
- Balance display

**Technical Implementation**:
- Implemented in `app/(tabs)/wallet.tsx`
- Wallet context for managing state
- Transaction history with mock data
- Send transaction functionality
- Address validation and display

**UI/UX**:
- Wallet connection status display
- Balance card with address
- Send token form with recipient address and amount
- Transaction history list with type, amount, and status
- Status indicators for transaction states

### 3.6 Achievement System
**Description**: A milestone-based reward system that tracks user progress and achievements.

**Key Features**:
- Achievement tracking
- Rarity levels
- Reward system
- Achievement display

**Technical Implementation**:
- Implemented in `components/AchievementSystem.tsx`
- Modal display in mining screen
- Achievement tracking in user profile
- Visual indicators for achievement rarity

**UI/UX**:
- Achievement cards with icons
- Rarity indicators (common, rare, epic)
- Achievement progress tracking
- Rewards display

### 3.7 Leaderboards
**Description**: A competitive feature that shows global user rankings based on mining performance.

**Key Features**:
- Global user rankings
- Podium display for top users
- Ranking categories
- Personal ranking display

**Technical Implementation**:
- Implemented in `app/(tabs)/leaderboard.tsx`
- Leaderboard data from API
- Rank display with gradient backgrounds

**UI/UX**:
- Podium display for top 3 users
- Gradient backgrounds for different ranks
- User avatar/name display
- Ranking statistics

### 3.8 Authentication System
**Description**: Google OAuth authentication with deep linking.

**Key Features**:
- Google Sign-In
- Deep linking for native authentication
- Session management
- Redirect after authentication

**Technical Implementation**:
- Implemented in [auth.tsx](file://c:\Users\ARQAM%20TV\Downloads\mobile\app\auth.tsx) and `contexts/AuthContext.tsx`
- Uses [expo-web-browser](file://c:\Users\ARQAM%20TV\Downloads\mobile\node_modules\expo-web-browser) for OAuth
- Deep linking with `Linking` module
- Session management with AsyncStorage

**UI/UX**:
- Google Sign-In button
- Email Sign-In option
- Loading states
- Redirect after successful authentication

## 4.0 Navigation Structure
The app uses Expo Router for file-based routing with the following structure:

```
app/
├── auth.tsx                # Authentication screen
├── index.tsx                # Landing/home screen
└── (tabs)/                  # Tab-based navigation
    ├── mine.tsx             # Mining screen
    ├── social.tsx           # Social tasks screen
    ├── leaderboard.tsx      # Leaderboards screen
    ├── presale.tsx          # Token presale screen
    ├── wallet.tsx           # Wallet management screen
    └── profile.tsx          # User profile screen
```

## 5.0 State Management
The app uses React Context API for state management with the following contexts:

- **AuthContext**: Handles user authentication and session
- **MiningContext**: Manages mining state and operations
- **WalletContext**: Handles wallet connection and transactions
- **NotificationContext**: Manages in-app notifications
- **ReferralContext**: Tracks referral data and statistics
- **PresaleContext**: Manages presale state and operations

## 6.0 API Integration
The app communicates with a backend hosted on Cloudflare Workers:

- **Base URL**: Configured in `config/api.ts`
- **Authentication**: Bearer token via Google OAuth
- **Endpoints**:
  - User authentication
  - Profile management
  - Mining operations
  - Wallet transactions
  - Ad rewards
  - Social tasks
  - Presale purchases

## 7.0 UI/UX Design
The app follows a dark theme with vibrant accents and animations:

**Color Scheme**:
- Primary: Dark backgrounds (#1a1a2e, #16213e)
- Accents: Gold (#ffa000), Purple (#8b5cf6), Blue (#3b82f6)
- Gradients for buttons and cards

**Typography**:
- Headings: Bold with larger font sizes
- Body text: Regular with appropriate opacity
- Monospace for addresses and codes

**Components**:
- Gradient buttons
- Cards with rounded corners
- Animated elements
- Icons from Lucide
- Modals for achievements and ads
- Toast notifications

**Animations**:
- Button press animations
- Click effects with animation
- Page transitions
- Electric effects on mining button

## 8.0 Performance Optimizations
- Efficient state management with React Context
- Optimized animations with React Native Reanimated
- Code splitting with Expo Router
- Lazy loading of components
- Debounced input handling
- Efficient rendering with PureComponent patterns

## 9.0 Platform-Specific Features
- **Android**: Haptic feedback, deep linking
- **iOS**: Haptic feedback, deep linking
- **Web**: OAuth authentication flow

## 10.0 Security Considerations
- Secure token storage with AsyncStorage
- HTTPS for all API communications
- Input validation for all user inputs
- Error handling for API requests
- OAuth flow with secure redirect

## 11.0 Development Tools and Libraries
- TypeScript for type safety
- ESLint for code quality
- Prettier for code formatting
- React Native Debugger
- Expo Dev Tools

## 12.0 Development Best Practices
- Component-based architecture
- Separation of concerns
- Proper error handling
- Loading states for async operations
- User feedback for actions
- Responsive design for different screen sizes
- Accessibility considerations

## 13.0 Future Enhancement Opportunities
- Add more mining levels and rewards
- Implement push notifications
- Add more social task platforms
- Introduce new types of achievements
- Expand wallet functionality to support more tokens
- Add in-app messaging for referrals
- Implement more detailed analytics
- Add dark/light theme toggle
- Improve offline functionality
- Add more detailed tutorials and onboarding

## 14.0 Project Structure
The app follows a modular structure:

- `app/` - Expo Router-based screens and layouts
  - `(tabs)/` - Tab navigator screens (Mine, Social, Wallet, etc.)
  - `auth.tsx` - Authentication flow
  - `index.tsx` - Landing/home screen
  - `_layout.tsx` - Root layout with context providers
- `src/contexts/` - React Context providers for global state
  - AuthContext, MiningContext, WalletContext, etc.
- `src/types/` - TypeScript interfaces and types
- `src/components/` - Reusable UI components (e.g., AchievementSystem, AdModal)
- `assets/` - Images, icons, splash screens
- Configuration files: `app.json`, `tsconfig.json`, `package.json`