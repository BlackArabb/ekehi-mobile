#!/usr/bin/env node

// Script to verify production build configuration
const fs = require('fs');
const path = require('path');

console.log('🔍 Verifying Production Build Configuration...\n');

// Check 1: EAS Configuration
console.log('📋 Checking EAS Configuration...');
const easConfigPath = path.join(__dirname, '..', 'eas.json');
if (fs.existsSync(easConfigPath)) {
  const easConfig = JSON.parse(fs.readFileSync(easConfigPath, 'utf8'));
  
  // Check production build settings
  if (easConfig.build && easConfig.build.production) {
    const prodConfig = easConfig.build.production;
    
    // Android settings
    if (prodConfig.android) {
      console.log('  ✅ Android production configuration found');
      if (prodConfig.android.buildType === 'app-bundle') {
        console.log('  ✅ Using App Bundle for smaller size');
      } else {
        console.log('  ⚠️  Consider using App Bundle for smaller size');
      }
      
      if (prodConfig.android.minify === true) {
        console.log('  ✅ JavaScript minification enabled');
      } else {
        console.log('  ❌ JavaScript minification not enabled');
      }
      
      if (prodConfig.android.split === true) {
        console.log('  ✅ Split APKs enabled for smaller downloads');
      } else {
        console.log('  ⚠️  Consider enabling split APKs for smaller downloads');
      }
    }
    
    // iOS settings
    if (prodConfig.ios) {
      console.log('  ✅ iOS production configuration found');
      if (prodConfig.ios.buildConfiguration === 'Release') {
        console.log('  ✅ Using Release build configuration');
      } else {
        console.log('  ❌ Not using Release build configuration');
      }
    }
    
    // Environment variables
    if (prodConfig.env) {
      console.log('  ✅ Environment variables configured');
      if (prodConfig.env.ENV === 'production') {
        console.log('  ✅ Production environment variable set');
      }
    }
  } else {
    console.log('  ❌ No production build configuration found');
  }
} else {
  console.log('  ❌ eas.json not found');
}

// Check 2: App Configuration
console.log('\n📱 Checking App Configuration...');
const appConfigPath = path.join(__dirname, '..', 'app.json');
if (fs.existsSync(appConfigPath)) {
  const appConfig = JSON.parse(fs.readFileSync(appConfigPath, 'utf8'));
  
  if (appConfig.expo) {
    console.log('  ✅ Expo configuration found');
    
    // Check basic app info
    if (appConfig.expo.name && appConfig.expo.slug) {
      console.log(`  ✅ App name: ${appConfig.expo.name}`);
      console.log(`  ✅ App slug: ${appConfig.expo.slug}`);
    }
    
    // Check bundle identifiers
    if (appConfig.expo.ios && appConfig.expo.ios.bundleIdentifier) {
      console.log(`  ✅ iOS bundle identifier: ${appConfig.expo.ios.bundleIdentifier}`);
    }
    
    if (appConfig.expo.android && appConfig.expo.android.package) {
      console.log(`  ✅ Android package: ${appConfig.expo.android.package}`);
    }
    
    // Check deep linking scheme
    if (appConfig.expo.scheme) {
      console.log(`  ✅ Deep linking scheme: ${appConfig.expo.scheme}`);
    }
    
    // Check plugins
    if (appConfig.expo.plugins) {
      console.log('  ✅ Plugins configured');
      appConfig.expo.plugins.forEach(plugin => {
        if (typeof plugin === 'string') {
          console.log(`    - ${plugin}`);
        } else if (Array.isArray(plugin) && plugin.length > 0) {
          console.log(`    - ${plugin[0]}`);
        }
      });
    }
  }
} else {
  console.log('  ❌ app.json not found');
}

// Check 3: Package.json scripts
console.log('\n⚙️  Checking Build Scripts...');
const packageJsonPath = path.join(__dirname, '..', 'package.json');
if (fs.existsSync(packageJsonPath)) {
  const packageJson = JSON.parse(fs.readFileSync(packageJsonPath, 'utf8'));
  
  if (packageJson.scripts) {
    console.log('  ✅ Build scripts found');
    
    const requiredScripts = [
      'build:android',
      'build:ios',
      'submit:android',
      'submit:ios'
    ];
    
    requiredScripts.forEach(script => {
      if (packageJson.scripts[script]) {
        console.log(`  ✅ ${script}: ${packageJson.scripts[script]}`);
      } else {
        console.log(`  ❌ ${script}: Not found`);
      }
    });
  }
} else {
  console.log('  ❌ package.json not found');
}

// Check 4: Webpack Configuration
console.log('\n🌐 Checking Webpack Configuration...');
const webpackConfigPath = path.join(__dirname, '..', 'webpack.config.js');
if (fs.existsSync(webpackConfigPath)) {
  const webpackConfig = fs.readFileSync(webpackConfigPath, 'utf8');
  console.log('  ✅ webpack.config.js found');
  
  // Check for production optimizations
  if (webpackConfig.includes('mode === \'production\'')) {
    console.log('  ✅ Production mode optimizations found');
  }
  
  if (webpackConfig.includes('drop_console: true')) {
    console.log('  ✅ Console.log removal in production enabled');
  }
  
  if (webpackConfig.includes('splitChunks')) {
    console.log('  ✅ Code splitting configured');
  }
} else {
  console.log('  ⚠️  webpack.config.js not found (may not be needed for Expo projects)');
}

// Check 5: Metro Configuration
console.log('\n🚇 Checking Metro Configuration...');
const metroConfigPath = path.join(__dirname, '..', 'metro.config.js');
if (fs.existsSync(metroConfigPath)) {
  const metroConfig = fs.readFileSync(metroConfigPath, 'utf8');
  console.log('  ✅ metro.config.js found');
  
  // Check for performance optimizations
  if (metroConfig.includes('inlineRequires: true')) {
    console.log('  ✅ Inline requires enabled for better performance');
  }
  
  if (metroConfig.includes('experimentalImportSupport: false')) {
    console.log('  ✅ Experimental import support configured');
  }
} else {
  console.log('  ❌ metro.config.js not found');
}

// Check 6: TypeScript Configuration
console.log('\n📝 Checking TypeScript Configuration...');
const tsConfigPath = path.join(__dirname, '..', 'tsconfig.json');
if (fs.existsSync(tsConfigPath)) {
  const tsConfig = JSON.parse(fs.readFileSync(tsConfigPath, 'utf8'));
  console.log('  ✅ tsconfig.json found');
  
  // Check strict mode
  if (tsConfig.compilerOptions && tsConfig.compilerOptions.strict === true) {
    console.log('  ✅ Strict mode enabled');
  } else {
    console.log('  ⚠️  Strict mode not enabled');
  }
  
  // Check other important settings
  if (tsConfig.compilerOptions) {
    const options = tsConfig.compilerOptions;
    if (options.noUnusedLocals === true) {
      console.log('  ✅ No unused locals checking enabled');
    }
    
    if (options.noUnusedParameters === true) {
      console.log('  ✅ No unused parameters checking enabled');
    }
    
    if (options.noImplicitReturns === true) {
      console.log('  ✅ No implicit returns checking enabled');
    }
  }
} else {
  console.log('  ❌ tsconfig.json not found');
}

// Check 7: Babel Configuration
console.log('\n🔧 Checking Babel Configuration...');
const babelConfigPath = path.join(__dirname, '..', 'babel.config.js');
if (fs.existsSync(babelConfigPath)) {
  const babelConfig = fs.readFileSync(babelConfigPath, 'utf8');
  console.log('  ✅ babel.config.js found');
  
  // Check for required plugins
  if (babelConfig.includes('react-native-reanimated/plugin')) {
    console.log('  ✅ Reanimated plugin configured');
  }
} else {
  console.log('  ❌ babel.config.js not found');
}

console.log('\n✅ Production Build Verification Complete!');
console.log('\n📋 Recommendations:');
console.log('  1. Test the build locally with: eas build --local');
console.log('  2. Ensure all environment variables are properly set');
console.log('  3. Verify Appwrite credentials are correct for production');
console.log('  4. Test deep linking and OAuth flows');
console.log('  5. Run performance tests on built APK/IPA');