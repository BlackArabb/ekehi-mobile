import { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { useRouter, useSegments, useLocalSearchParams } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import * as WebBrowser from 'expo-web-browser';
import { useAuth } from '@/contexts/AuthContext';
import * as Linking from 'expo-linking';
import { account } from '@/config/appwrite';

// Type declaration for window object on web platform
declare const window: any;

export default function OAuthReturnPage() {
  const router = useRouter();
  const segments = useSegments();
  const params = useLocalSearchParams();
  const { checkAuthStatus, user, isLoading } = useAuth();
  const navigationTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);
  const hasNavigated = useRef(false);
  const hasProcessedCallback = useRef(false);

  useEffect(() => {
    console.log('OAuthReturnPage mounted - checking auth status');
    console.log('Current segments:', segments);
    console.log('URL params:', params);
    
    // Prevent multiple processing
    if (hasProcessedCallback.current) {
      return;
    }
    hasProcessedCallback.current = true;
    
    const handleOAuthCallback = async () => {
      try {
        // Complete any pending auth session
        const completionResult = WebBrowser.maybeCompleteAuthSession();
        console.log('Auth session completion result:', completionResult);
        
        // Get current URL to extract OAuth parameters
        let currentUrl = '';
        try {
          // Only access window object on web platforms
          if (typeof window !== 'undefined' && window?.location?.href) {
            currentUrl = window.location.href;
          } else {
            // Fallback for non-web platforms
            currentUrl = await Linking.getInitialURL() || '';
          }
        } catch (e) {
          // Fallback for non-web platforms
          currentUrl = await Linking.getInitialURL() || '';
        }
        
        console.log('Current URL:', currentUrl);
        
        // Check if we have OAuth success parameters in the URL
        const urlObj = new URL(currentUrl, 'http://localhost'); // Provide base URL to avoid errors
        const secret = urlObj.searchParams.get('secret') || (params.secret as string);
        const userId = urlObj.searchParams.get('userId') || (params.userId as string);
        
        if (secret && userId) {
          console.log('OAuth parameters found:', { userId, secretLength: secret.length });
          
          try {
            // Create the Appwrite session using the OAuth parameters
            console.log('Creating Appwrite session with OAuth parameters...');
            await account.createSession(userId, secret);
            console.log('Appwrite session created successfully!');
            
            // Wait a moment to ensure the session is established
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            // Now check auth status to update the context
            await checkAuthStatus();
            console.log('Auth status updated after session creation');
            
            // Navigate to main app after successful authentication
            setTimeout(() => {
              if (!hasNavigated.current) {
                console.log('Navigating to main app after successful OAuth');
                hasNavigated.current = true;
                router.replace('/(tabs)/mine');
              }
            }, 1500);
            
          } catch (sessionError: any) {
            console.error('Failed to create Appwrite session:', sessionError);
            Alert.alert(
              'Authentication Error',
              'Failed to complete OAuth authentication. Please try signing in again.'
            );
            
            // Navigate back to auth on session creation failure
            setTimeout(() => {
              if (!hasNavigated.current) {
                hasNavigated.current = true;
                router.replace('/auth');
              }
            }, 2000);
          }
          
        } else {
          console.log('No OAuth parameters found, checking if already authenticated');
          
          // Even without direct parameters, check auth status
          // as the session might have been established
          await new Promise(resolve => setTimeout(resolve, 2000));
          
          try {
            await checkAuthStatus();
          } catch (error) {
            console.log('Auth check failed:', error);
          }
          
          // Wait and then navigate based on auth status
          setTimeout(() => {
            if (!hasNavigated.current) {
              hasNavigated.current = true;
              // Will be handled by the user effect below
            }
          }, 3000);
        }
        
      } catch (error) {
        console.error('Error handling OAuth callback:', error);
        
        // On error, navigate back to auth after a delay
        setTimeout(() => {
          if (!hasNavigated.current) {
            console.log('Navigating to auth due to error');
            hasNavigated.current = true;
            router.replace('/auth');
          }
        }, 3000);
      }
    };
    
    handleOAuthCallback();
    
    return () => {
      console.log('OAuthReturnPage unmounting');
      if (navigationTimeout.current) {
        clearTimeout(navigationTimeout.current);
      }
    };
  }, [router, params, segments]);

  // Watch for user authentication and navigate
  useEffect(() => {
    if (user && !hasNavigated.current) {
      console.log('User authenticated, navigating to mining screen');
      hasNavigated.current = true;
      router.replace('/(tabs)/mine');
    } else if (!isLoading && !user && hasProcessedCallback.current && !hasNavigated.current) {
      // If we've processed the callback but still no user after loading is complete
      console.log('No user found after OAuth processing, navigating to auth');
      hasNavigated.current = true;
      setTimeout(() => {
        router.replace('/auth');
      }, 1000);
    }
  }, [user, isLoading, router]);

  return (
    <LinearGradient
      colors={['#1a1a2e', '#16213e', '#0f3460']}
      style={styles.container}
    >
      <View style={styles.content}>
        <ActivityIndicator size="large" color="#ffa000" />
        <Text style={styles.title}>Completing Authentication</Text>
        <Text style={styles.subtitle}>Please wait while we complete the sign in process...</Text>
        {user ? (
          <Text style={styles.successText}>Welcome back! Redirecting to your account...</Text>
        ) : (
          <Text style={styles.infoText}>Creating your session...</Text>
        )}
      </View>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
    marginTop: 24,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
    marginTop: 12,
    textAlign: 'center',
    lineHeight: 24,
  },
  infoText: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.5)',
    marginTop: 16,
    textAlign: 'center',
  },
  successText: {
    fontSize: 14,
    color: '#4CAF50',
    marginTop: 16,
    textAlign: 'center',
    fontWeight: 'bold',
  },
});