# PNPM Android Build Fix

## Issue
When building the Android release APK using `gradlew assembleRelease`, the build was failing with the error:
```
Error: Cannot find module '@react-native/assets-registry/registry.js'
```

And then with:
```
Error: Cannot find module 'babel-preset-expo'
```

## Root Cause
This is a common issue with pnpm and React Native projects. According to the pnpm dependency declaration memory:
> "In Expo projects using pnpm, all required packages (like expo-asset) must be explicitly declared in package.json dependencies, even if they appear in pnpm-lock.yaml, to prevent Metro bundler errors during Android release builds."

Packages that are transitively included in pnpm-lock.yaml still need to be explicitly declared in package.json to be available during the build process.

## Solution
Added missing dependencies to package.json:

1. Added `@react-native/assets-registry` to the dependencies:
```json
{
  "dependencies": {
    "@react-native/assets-registry": "0.74.87",
    // ... other dependencies
  }
}
```

2. Added `babel-preset-expo` to the devDependencies:
```json
{
  "devDependencies": {
    "babel-preset-expo": "~11.0.0",
    // ... other devDependencies
  }
}
```

## Verification
After adding the dependencies and running `pnpm install`, the Android release build should complete successfully.

## Additional Notes
This is a pnpm-specific issue where dependencies that are transitively included in pnpm-lock.yaml still need to be explicitly declared in package.json to be available during the build process. This is different from npm/yarn behavior where transitive dependencies are automatically available.