# KtMobile App Architecture

## Project Structure

```
ktMobileApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ekehi/network/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── MainApplication.kt
│   │   │   │   ├── SplashActivity.kt
│   │   │   │   ├── OAuthCallbackActivity.kt
│   │   │   │   ├── analytics/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── local/
│   │   │   │   ├── di/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── usecase/
│   │   │   │   │   └── model/
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── MiningScreen.kt
│   │   │   │   │   │   ├── ProfileScreen.kt
│   │   │   │   │   │   └── components/
│   │   │   │   │   ├── viewmodel/
│   │   │   │   │   └── navigation/
│   │   │   │   ├── service/
│   │   │   │   │   ├── StartIoService.kt
│   │   │   │   │   ├── MiningService.kt
│   │   │   │   │   └── AppwriteService.kt
│   │   │   │   ├── security/
│   │   │   │   ├── performance/
│   │   │   │   └── util/
│   │   │   └── res/
│   │   └── test/
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Key Components Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    MainApplication.kt                       │
│  - Hilt Application class                                   │
│  - Initializes DI and services                              │
└─────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                      MainActivity.kt                        │
│  - Main entry point for authenticated users                 │
│  - Compose UI navigation                                    │
└─────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                   Compose UI Layer                          │
│  MiningScreen.kt ◄────────── ProfileScreen.kt               │
│  - Ad bonus button           - Exit ad integration          │
│  - Mining controls           - User settings                │
└─────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    StartIoService.kt                        │
│  - Rewarded video ads                                       │
│  - Interstitial ads (fallback)                              │
│  - Exit ads                                                 │
└─────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    Start.io SDK                             │
│  - Ad loading and display                                   │
│  - Analytics and tracking                                   │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow

```
1. User opens app → SplashActivity authenticates → MainActivity
2. User navigates to MiningScreen → Ad bonus button
3. MiningScreen calls StartIoService.loadRewardedVideoAd()
4. StartIoService creates ad instance and loads ad
5. User clicks "Watch Ad" button
6. MiningScreen calls StartIoService.showRewardedVideoAd()
7. StartIoService shows ad with fallback to interstitial
8. Ad completes → Reward callback invoked
9. UI updates to show earned tokens
```