// app/auth/callback.tsx
import { useEffect, useRef } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuth } from '@/contexts/AuthContext';
import { account } from '@/config/appwrite';

// Global variable to track last auth check time
declare global {
  var lastAuthCheck: number | undefined;
}

export default function AuthCallbackPage() {
  const router = useRouter();
  const params = useLocalSearchParams();
  const { checkAuthStatus } = useAuth();
  const hasProcessed = useRef(false);

  useEffect(() => {
    if (hasProcessed.current) return;
    hasProcessed.current = true;

    const handleAuthCallback = async () => {
      try {
        console.log('Auth callback received with params:', params);

        // Extract the secret and userId from params
        const secret = params.secret as string;
        const userId = params.userId as string;

        if (secret && userId) {
          console.log('Creating Appwrite session with secret and userId...');
          console.log('  - User ID:', userId);
          console.log('  - Secret length:', secret.length);

          try {
            // Create the session using the OAuth response
            await account.createSession(userId, secret);
            console.log('Session created successfully!');

            // Update auth context
            // Only check auth status if we haven't checked recently
            const now = Date.now();
            // @ts-ignore
            const lastCheck = global.lastAuthCheck || 0;
            if (now - lastCheck > 3000) { // Only check if it's been more than 3 seconds
              // @ts-ignore
              global.lastAuthCheck = now;
              await checkAuthStatus();
            }

            console.log('Redirecting to main app...');
            router.replace('/(tabs)/mine');

          } catch (sessionError: any) {
            console.error('Failed to create session:', sessionError);
            // Even if session creation fails, try to check auth status
            // Only check auth status if we haven't checked recently
            const now = Date.now();
            // @ts-ignore
            const lastCheck = global.lastAuthCheck || 0;
            if (now - lastCheck > 3000) { // Only check if it's been more than 3 seconds
              // @ts-ignore
              global.lastAuthCheck = now;
              await checkAuthStatus();
            }
            setTimeout(() => {
              router.replace('/(tabs)/mine');
            }, 1000);
          }
        } else {
          console.log('No OAuth credentials received, checking auth status anyway...');
          // For cases where we don't receive direct credentials, 
          // just check if a session was established
          await new Promise(resolve => setTimeout(resolve, 3000)); // Give more time for session to be established
          
          // Only check auth status if we haven't checked recently
          const now = Date.now();
          // @ts-ignore
          const lastCheck = global.lastAuthCheck || 0;
          if (now - lastCheck > 3000) { // Only check if it's been more than 3 seconds
            // @ts-ignore
            global.lastAuthCheck = now;
            await checkAuthStatus();
          }
          
          // Add a delay to ensure auth status is checked before redirecting
          setTimeout(() => {
            router.replace('/(tabs)/mine');
          }, 1000);
        }

      } catch (error) {
        console.error('Auth callback error:', error);
        Alert.alert(
          'Authentication Error', 
          'Failed to complete authentication. Please try again.\n\n' +
          'If this error persists:\n' +
          '1. Check your Appwrite OAuth configuration\n' +
          '2. Ensure redirect URLs are properly registered\n' +
          '3. Verify Google OAuth client settings\n\n' +
          `Error: ${error instanceof Error ? error.message : 'Unknown error'}`
        );
        // If there's an error, redirect to auth page
        router.replace('/auth');
      }
    };

    handleAuthCallback();
  }, [params, router, checkAuthStatus]);

  return (
    <LinearGradient
      colors={['#1a1a2e', '#16213e', '#0f3460']}
      style={styles.container}
    >
      <View style={styles.content}>
        <ActivityIndicator size="large" color="#ffa000" />
        <Text style={styles.title}>Completing Authentication</Text>
        <Text style={styles.subtitle}>Creating your session...</Text>
        <Text style={styles.infoText}>Please wait while we complete the sign in process</Text>
        <Text style={styles.infoText}>If this takes more than 10 seconds, please check your configuration</Text>
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
});