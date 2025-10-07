import { useEffect, useState } from 'react';
import { Stack, useSegments } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import * as SplashScreen from 'expo-splash-screen';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider, useAuth } from '@/contexts/AuthContext';
import { MiningProvider } from '@/contexts/MiningContext';
import { WalletProvider } from '@/contexts/WalletContext';
import { NotificationProvider } from '@/contexts/NotificationContext';
import { ReferralProvider } from '@/contexts/ReferralContext';
import { useRouter } from 'expo-router';
import { initializeDeepLinking } from '@/utils/deepLinking';
import { View, StyleSheet, Image } from 'react-native';
import PulseLoader from '@/components/PulseLoader';
import SplashPNG from '../assets/splash.png';

// Prevent the splash screen from auto-hiding before asset loading is complete
SplashScreen.preventAutoHideAsync();

// Simple loading component to show while app is initializing
const AppLoadingScreen = () => (
  <View style={styles.loadingContainer}>
    <Image source={SplashPNG} resizeMode="contain" style={{ width: 400, height: 400 }} />
    <PulseLoader size={14} />
  </View>
);

// Create a component that handles background authentication
function AuthenticatedApp() {
  const { user, isLoading, checkAuthStatus } = useAuth();
  const router = useRouter();
  const segments = useSegments();
  const [backgroundAuthChecked, setBackgroundAuthChecked] = useState(false);

  useEffect(() => {
    // Perform background authentication check while splash screen is visible
    const performBackgroundAuth = async () => {
      try {
        console.log('🔍 Performing background authentication check...');
        await checkAuthStatus();
        console.log('✅ Background authentication check completed');
      } catch (error) {
        console.error('❌ Background authentication check failed:', error);
      } finally {
        setBackgroundAuthChecked(true);
      }
    };

    // Only run background auth if it hasn't been checked yet
    if (!backgroundAuthChecked) {
      performBackgroundAuth();
    }
  }, [backgroundAuthChecked, checkAuthStatus]);

  useEffect(() => {
    // Once background auth is complete and we know the auth state, hide splash screen
    if (backgroundAuthChecked && !isLoading) {
      console.log('🎉 Hiding splash screen after background auth');
      SplashScreen.hideAsync();
    }
  }, [backgroundAuthChecked, isLoading]);

  // Redirect logic based on authentication state
  useEffect(() => {
    if (backgroundAuthChecked && !isLoading) {
      // Check if we're currently in the OAuth flow
      const inOAuthFlow = segments[0] === 'oauth';
      const inAuthFlow = segments[0] === 'auth';
      const inTabsFlow = segments[0] === '(tabs)';
      const onIndex = segments.length === 0 || segments[0] === 'index';
      
      // Don't redirect if we're in the OAuth flow - let it complete
      if (inOAuthFlow) {
        console.log('🔐 Currently in OAuth flow, not redirecting');
        return;
      }
      
      // Add a small delay to ensure navigation happens after splash screen is hidden
      const timer = setTimeout(() => {
        if (user) {
          console.log('👤 User authenticated, ensuring access to protected routes');
          // If user is authenticated but on auth page or index, redirect to main app
          if (inAuthFlow || onIndex) {
            router.replace('/(tabs)/mine');
          }
          // If already in tabs, don't redirect
        } else {
          console.log('👤 User not authenticated');
          // Only redirect if not already on auth or index page
          if (inTabsFlow) {
            console.log('User not authenticated in tabs, redirecting to index');
            router.replace('/');
          } else if (!inAuthFlow && !onIndex) {
            console.log('User not authenticated, navigating to landing page');
            router.replace('/');
          }
        }
      }, 100);
      
      return () => clearTimeout(timer);
    }
    // Return undefined explicitly to satisfy TypeScript
    return undefined;
  }, [backgroundAuthChecked, isLoading, user, router, segments]);

  // Show loading screen while authentication is being checked
  if (!backgroundAuthChecked || isLoading) {
    return <AppLoadingScreen />;
  }

  return (
    <>
      <StatusBar style="light" backgroundColor="#000000" />
      <Stack
        screenOptions={{
          headerShown: false,
          contentStyle: { backgroundColor: '#000000' },
        }}
      >
        <Stack.Screen name="index" />
        <Stack.Screen name="(tabs)" />
        <Stack.Screen name="auth" />
        <Stack.Screen 
          name="oauth/return" 
          options={{
            headerShown: false,
            gestureEnabled: false,
          }} 
        />
        <Stack.Screen 
          name="oauth/callback" 
          options={{
            headerShown: false,
            gestureEnabled: false,
          }} 
        />
      </Stack>
    </>
  );
}

export default function RootLayout() {
  const [appReady, setAppReady] = useState(false);
  const [forceReady, setForceReady] = useState(false);

  // Initialize deep linking
  useEffect(() => {
    const cleanup = initializeDeepLinking();
    return () => {
      cleanup();
    };
  }, []);

  // Emergency timeout to ensure app never gets stuck on splash screen
  useEffect(() => {
    const emergencyTimeout = setTimeout(() => {
      console.log('🚨 Emergency timeout: Forcing app to load');
      setForceReady(true);
      setAppReady(true);
      SplashScreen.hideAsync();
    }, 8000); // 8 second emergency timeout
    
    return () => clearTimeout(emergencyTimeout);
  }, []);

  useEffect(() => {
    async function prepare() {
      try {
        console.log('🚀 Preparing app...');
        // Minimal delay to ensure all providers are ready
        await new Promise(resolve => setTimeout(() => resolve(null), 100));
        console.log('✅ App preparation complete');
      } catch (e) {
        console.warn('⚠️ App preparation error:', e);
      } finally {
        setAppReady(true);
      }
    }

    if (!appReady && !forceReady) {
      prepare();
    }
  }, [appReady, forceReady]);

  if ((!appReady && !forceReady)) {
    console.log('⏳ Waiting for app to be ready...', { appReady, forceReady });
    return <AppLoadingScreen />;
  }

  console.log('🚀 Rendering app layout with authentication provider');

  return (
    <SafeAreaProvider>
      <AuthProvider>
        <WalletProvider>
          <MiningProvider>
            <ReferralProvider>
              <NotificationProvider>
                <AuthenticatedApp />
              </NotificationProvider>
            </ReferralProvider>
          </MiningProvider>
        </WalletProvider>
      </AuthProvider>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    backgroundColor: '#000000',
    justifyContent: 'center',
    alignItems: 'center',
  },

  
});