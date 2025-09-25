import { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ActivityIndicator, Alert, TouchableOpacity } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuth } from '@/contexts/AuthContext';

export default function VerifyEmailPage() {
  const router = useRouter();
  const params = useLocalSearchParams();
  const { updateEmailVerification, checkAuthStatus } = useAuth();
  const [verificationStatus, setVerificationStatus] = useState<'pending' | 'success' | 'error'>('pending');

  useEffect(() => {
    const verifyEmail = async () => {
      try {
        console.log('Starting email verification with params:', params);
        
        const userId = params.userId as string;
        const secret = params.secret as string;

        if (!userId || !secret) {
          throw new Error('Missing verification parameters');
        }

        console.log('Calling updateEmailVerification with:', { userId, secret });
        await updateEmailVerification(userId, secret);
        
        setVerificationStatus('success');
        console.log('Email verification successful');
        
        // Refresh auth status to get updated user data
        await checkAuthStatus();
        
        // Redirect to main app after a delay
        setTimeout(() => {
          router.replace('/(tabs)/mine');
        }, 3000);
      } catch (error: any) {
        console.error('Email verification failed:', error);
        setVerificationStatus('error');
        
        Alert.alert(
          'Verification Failed',
          error.message || 'Failed to verify your email. Please try again or request a new verification email.',
          [
            {
              text: 'OK',
              onPress: () => router.replace('/auth')
            }
          ]
        );
      }
    };

    verifyEmail();
  }, [params, router, updateEmailVerification, checkAuthStatus]);

  return (
    <LinearGradient
      colors={['#1a1a2e', '#16213e', '#0f3460']}
      style={styles.container}
    >
      <View style={styles.content}>
        {verificationStatus === 'pending' && (
          <>
            <ActivityIndicator size="large" color="#ffa000" />
            <Text style={styles.title}>Verifying Email</Text>
            <Text style={styles.subtitle}>Please wait while we verify your email address...</Text>
          </>
        )}

        {verificationStatus === 'success' && (
          <>
            <Text style={[styles.title, styles.successText]}>Email Verified!</Text>
            <Text style={styles.subtitle}>Your email has been successfully verified.</Text>
            <Text style={styles.infoText}>Redirecting to the app...</Text>
          </>
        )}

        {verificationStatus === 'error' && (
          <>
            <Text style={[styles.title, styles.errorText]}>Verification Failed</Text>
            <Text style={styles.subtitle}>We couldn't verify your email address.</Text>
            <TouchableOpacity 
              style={styles.button}
              onPress={() => router.replace('/auth')}
            >
              <Text style={styles.buttonText}>Back to Sign In</Text>
            </TouchableOpacity>
          </>
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
    color: '#4CAF50',
  },
  errorText: {
    color: '#f44336',
  },
  button: {
    backgroundColor: '#ffa000',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginTop: 24,
  },
  buttonText: {
    color: '#000000',
    fontSize: 16,
    fontWeight: 'bold',
  },
});