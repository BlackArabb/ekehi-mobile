# Android SDK Setup Instructions

## Issue
The build is failing with the error:
```
SDK location not found. Define a valid SDK location with an ANDROID_HOME environment variable or by setting the sdk.dir path in your project's local properties file.
```

## Solution
You need to install the Android SDK and configure its path in the project.

## Steps to Resolve

### 1. Install Android SDK

You have two options:

#### Option A: Install Android Studio (Recommended)
1. Download Android Studio from https://developer.android.com/studio
2. Run the installer and follow the setup wizard
3. During installation, ensure "Android SDK" is selected
4. Complete the installation

#### Option B: Install Command Line Tools Only
1. Download "Command line tools only" from https://developer.android.com/studio#command-tools
2. Extract the ZIP file to a directory like `C:\Android\Sdk`
3. Open a terminal in the `cmdline-tools\latest\bin` directory
4. Run: `sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"`

### 2. Configure SDK Path

After installing the Android SDK:

1. Open the `local.properties` file in the project root directory
2. Add the SDK path:
   ```
   sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   ```
   
   Or if you installed to a custom location:
   ```
   sdk.dir=C:\\Android\\Sdk
   ```

### 3. Verify Setup

1. Open a new terminal/command prompt
2. Navigate to the project directory
3. Run: `gradlew.bat assembleDebug`

The build should now succeed.