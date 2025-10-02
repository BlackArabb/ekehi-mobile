import { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView, SafeAreaView } from 'react-native';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuth } from '@/contexts/AuthContext';
import { validateEmail } from '@/utils/validation';
import LoadingDots from '@/components/LoadingDots';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSent, setIsSent] = useState(false);
  const router = useRouter();
  const { sendPasswordRecovery } = useAuth();

  const validateForm = () => {
    // Reset errors
    setEmailError('');
    
    // Email validation
    if (!email) {
      setEmailError('Email is required');
      return false;
    } else if (!validateEmail(email)) {
      setEmailError('Please enter a valid email address');
      return false;
    }
    
    return true;
  };

  const handleSendRecovery = async () => {
    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    try {
      // For web, we'll use the current origin
      let recoveryUrl = '';
      // @ts-ignore - window object only exists on web
      if (typeof window !== 'undefined' && window?.location?.origin) {
        // @ts-ignore
        recoveryUrl = `${window.location.origin}/reset-password`;
      } else {
        // Fallback for mobile platforms
        recoveryUrl = 'ekehi://reset-password';
      }
      console.log('Sending password recovery to:', email, 'with URL:', recoveryUrl);
      
      await sendPasswordRecovery(email, recoveryUrl);
      setIsSent(true);
      
      Alert.alert(
        'Recovery Email Sent',
        'Please check your email for instructions to reset your password.'
      );
    } catch (error: any) {
      console.error('Password recovery failed:', error);
      Alert.alert(
        'Recovery Failed',
        error.message || 'Failed to send recovery email. Please try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleEmailChange = (text: string) => {
    setEmail(text);
    // Clear error when user starts typing
    if (emailError) setEmailError('');
  };

  return (
    <LinearGradient
      colors={['#1a1a2e', '#16213e', '#0f3460']}
      style={styles.container}
    >
      <SafeAreaView style={styles.safeArea}>
        <ScrollView contentContainerStyle={styles.scrollContainer}>
          <View style={styles.content}>
            <Text style={styles.title}>Forgot Password</Text>
            <Text style={styles.subtitle}>
              {isSent 
                ? 'We have sent password reset instructions to your email.' 
                : 'Enter your email address and we will send you instructions to reset your password.'}
            </Text>

            {!isSent ? (
              <>
                <View style={styles.inputContainer}>
                  <Text style={styles.label}>Email</Text>
                  <TextInput
                    style={[styles.input, emailError ? styles.inputError : undefined]}
                    value={email}
                    onChangeText={handleEmailChange}
                    placeholder="Enter your email"
                    placeholderTextColor="#aaa"
                    keyboardType="email-address"
                    autoCapitalize="none"
                  />
                  {emailError ? <Text style={styles.errorText}>{emailError}</Text> : null}
                </View>

                <TouchableOpacity
                  style={[styles.button, isLoading && styles.buttonDisabled]}
                  onPress={handleSendRecovery}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <LoadingDots color="#000000" size={8} />
                  ) : (
                    <Text style={styles.buttonText}>
                      Send Recovery Email
                    </Text>
                  )}
                </TouchableOpacity>
              </>
            ) : (
              <View style={styles.successContainer}>
                <Text style={styles.successText}>✓</Text>
                <Text style={styles.successMessage}>Email Sent Successfully</Text>
                <Text style={styles.successDescription}>
                  Please check your email for instructions to reset your password.
                </Text>
              </View>
            )}

            <TouchableOpacity
              style={styles.switchMode}
              onPress={() => router.replace('/auth')}
            >
              <Text style={styles.switchModeText}>Back to Sign In</Text>
            </TouchableOpacity>
          </View>
        </ScrollView>
      </SafeAreaView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  safeArea: {
    flex: 1,
  },
  scrollContainer: {
    flexGrow: 1,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 24,
    paddingVertical: 40,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: '#ffffff',
    textAlign: 'center',
    marginBottom: 12,
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
    marginBottom: 32,
  },
  inputContainer: {
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    color: '#ffffff',
    marginBottom: 8,
  },
  input: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    padding: 16,
    color: '#ffffff',
    fontSize: 16,
  },
  inputError: {
    borderColor: '#ff4444',
    borderWidth: 1,
  },
  errorText: {
    color: '#ff4444',
    fontSize: 14,
    marginTop: 4,
  },
  button: {
    backgroundColor: '#ffa000',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginTop: 8,
  },
  buttonDisabled: {
    opacity: 0.6,
  },
  buttonText: {
    color: '#000000',
    fontSize: 16,
    fontWeight: 'bold',
  },
  switchMode: {
    alignItems: 'center',
    marginTop: 24,
  },
  switchModeText: {
    color: '#ffa000',
    fontSize: 16,
  },
  successContainer: {
    alignItems: 'center',
    paddingVertical: 40,
  },
  successText: {
    fontSize: 48,
    color: '#4CAF50',
    marginBottom: 16,
  },
  successMessage: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 8,
  },
  successDescription: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
  },
});