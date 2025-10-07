import { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView, SafeAreaView } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuth } from '@/contexts/AuthContext';
import { validatePassword } from '@/utils/validation';
import LoadingDots from '@/components/LoadingDots';

export default function ResetPasswordPage() {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [confirmPasswordError, setConfirmPasswordError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isReset, setIsReset] = useState(false);
  const router = useRouter();
  const params = useLocalSearchParams();
  const { updatePasswordRecovery } = useAuth();

  const validateForm = () => {
    // Reset errors
    setPasswordError('');
    setConfirmPasswordError('');
    
    let isValid = true;
    
    // Password validation
    if (!password) {
      setPasswordError('Password is required');
      isValid = false;
    } else {
      const passwordValidation = validatePassword(password);
      if (!passwordValidation.isValid) {
        setPasswordError(passwordValidation.message);
        isValid = false;
      }
    }
    
    // Confirm password validation
    if (!confirmPassword) {
      setConfirmPasswordError('Please confirm your password');
      isValid = false;
    } else if (password !== confirmPassword) {
      setConfirmPasswordError('Passwords do not match');
      isValid = false;
    }
    
    return isValid;
  };

  const handleResetPassword = async () => {
    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    try {
      const userId = params.userId as string;
      const secret = params.secret as string;

      if (!userId || !secret) {
        throw new Error('Missing reset parameters');
      }

      console.log('Resetting password for user:', userId);
      await updatePasswordRecovery(userId, secret, password);
      setIsReset(true);
      
      Alert.alert(
        'Password Reset',
        'Your password has been successfully reset. You can now sign in with your new password.',
        [
          {
            text: 'OK',
            onPress: () => router.replace('/auth')
          }
        ]
      );
    } catch (error: any) {
      console.error('Password reset failed:', error);
      Alert.alert(
        'Reset Failed',
        error.message || 'Failed to reset your password. Please try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handlePasswordChange = (text: string) => {
    setPassword(text);
    // Clear error when user starts typing
    if (passwordError) setPasswordError('');
  };

  const handleConfirmPasswordChange = (text: string) => {
    setConfirmPassword(text);
    // Clear error when user starts typing
    if (confirmPasswordError) setConfirmPasswordError('');
  };

  return (
    <LinearGradient
      colors={['#1a1a2e', '#16213e', '#0f3460']}
      style={styles.container}
    >
      <SafeAreaView style={styles.safeArea}>
        <ScrollView contentContainerStyle={styles.scrollContainer}>
          <View style={styles.content}>
            <Text style={styles.title}>Reset Password</Text>
            <Text style={styles.subtitle}>
              Enter your new password below.
            </Text>

            {!isReset ? (
              <>
                <View style={styles.inputContainer}>
                  <Text style={styles.label}>New Password</Text>
                  <TextInput
                    style={[styles.input, passwordError ? styles.inputError : undefined]}
                    value={password}
                    onChangeText={handlePasswordChange}
                    placeholder="Enter your new password"
                    placeholderTextColor="#aaa"
                    secureTextEntry
                  />
                  {passwordError ? <Text style={styles.errorText}>{passwordError}</Text> : null}
                </View>

                <View style={styles.inputContainer}>
                  <Text style={styles.label}>Confirm Password</Text>
                  <TextInput
                    style={[styles.input, confirmPasswordError ? styles.inputError : undefined]}
                    value={confirmPassword}
                    onChangeText={handleConfirmPasswordChange}
                    placeholder="Confirm your new password"
                    placeholderTextColor="#aaa"
                    secureTextEntry
                  />
                  {confirmPasswordError ? <Text style={styles.errorText}>{confirmPasswordError}</Text> : null}
                </View>

                <TouchableOpacity
                  style={[styles.button, isLoading && styles.buttonDisabled]}
                  onPress={handleResetPassword}
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <LoadingDots color="#000000" size={8} />
                  ) : (
                    <Text style={styles.buttonText}>
                      Reset Password
                    </Text>
                  )}
                </TouchableOpacity>
              </>
            ) : (
              <View style={styles.successContainer}>
                <Text style={styles.successText}>✓</Text>
                <Text style={styles.successMessage}>Password Reset Successfully</Text>
                <Text style={styles.successDescription}>
                  You can now sign in with your new password.
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