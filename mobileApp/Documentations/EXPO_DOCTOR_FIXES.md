# Expo Doctor Fixes Summary

## Overview
This document summarizes the fixes applied to resolve issues identified by `expo-doctor` command.

## Issues Fixed

### 1. .expo Directory Not Ignored by Git
**Problem**: The .expo directory was not ignored by Git, which could lead to committing machine-specific device history and development server settings.

**Solution**: Added ".expo" to the .gitignore file.

### 2. Invalid Splash Screen resizeMode Value
**Problem**: The app.json contained an invalid value "center" for the splash/resizeMode field. Valid values are "contain", "cover", and "native".

**Solution**: Changed the resizeMode value from "center" to "contain".

### 3. Incorrect Dependencies Installed Directly
**Problem**: Several packages should not be installed directly:
- "@types/react-native" - types are included with the "react-native" package
- "expo-app-loading" - has been removed as of SDK 49, should use expo-splash-screen instead
- "@expo/config-plugins" - should use "expo/config-plugins" instead
- "@expo/prebuild-config" - should not be installed directly

**Solution**: Removed these packages from package.json dependencies and devDependencies.

## Remaining Warning

### App Config Fields Not Synced in Non-CNG Project
**Warning**: This project contains native project folders but also has native configuration properties in app.json, indicating it is configured to use Prebuild. When the android/ios folders are present, EAS Build will not sync the following properties: orientation, icon, userInterfaceStyle, splash, ios, android, plugins, scheme.

**Explanation**: This is a common warning when you have both native project folders (android/ios) and configuration properties in app.json. This warning indicates that when building with EAS Build, some properties in app.json won't be synced to the native projects because they already exist.

**Resolution Options**:
1. Remove the native project folders (android/ios) if you want to use Prebuild exclusively
2. Remove the conflicting properties from app.json if you're managing native projects manually

**Current Status**: This is just a warning and not an error. It won't affect the functionality of your app.

## Verification
After applying all fixes, running `npx expo-doctor` now shows:
- 15/16 checks passed
- Only 1 check failed (the non-critical warning about app config fields)

## Summary
All critical issues have been resolved. The remaining warning is informational and doesn't impact app functionality. Your Expo project is now in good health with all major issues addressed.