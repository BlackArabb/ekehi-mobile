import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert, ScrollView, SafeAreaView, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { useAuth } from '@/contexts/AuthContext';
import { validateEmail, validatePassword, validateName } from '@/utils/validation';
import LoadingDots from '@/components/LoadingDots';

export default function AuthPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [isSignUp, setIsSignUp] = useState(false);
  const [isEmailAuthLoading, setIsEmailAuthLoading] = useState(false); // Separate loading state for email auth
  const [isGoogleAuthLoading, setIsGoogleAuthLoading] = useState(false); // Separate loading state for Google auth
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [nameError, setNameError] = useState('');
  const [autoFilled, setAutoFilled] = useState(false);
  const [autoLoginAttempted, setAutoLoginAttempted] = useState(false);
  const [signupErrorOccurred, setSignupErrorOccurred] = useState(false); // New state to track signup errors
  const router = useRouter();
  const { signIn, signInWithEmail, signUp } = useAuth();

  // Detect auto-filled credentials
  useEffect(() => {
    // Check if both email and password are filled (likely from auto-fill)
    // Only attempt auto-login if we haven't had a signup error
    if (email && password && !autoFilled && !autoLoginAttempted && !isEmailAuthLoading && !isGoogleAuthLoading && !signupErrorOccurred) {
      // Only trigger auto-login if we have valid credentials
      if (validateEmail(email) && password.length >= 6) {
        setAutoFilled(true);
        setAutoLoginAttempted(true);
        // Small delay to ensure fields are properly populated
        setTimeout(() => {
          handleEmailAuth();
        }, 300);
      }
    }
  }, [email, password, autoFilled, autoLoginAttempted, isEmailAuthLoading, isGoogleAuthLoading, signupErrorOccurred]);

  const validateForm = () => {
    // Reset errors
    setEmailError('');
    setPasswordError('');
    setNameError('');
    
    let isValid = true;
    
    // Email validation
    if (!email) {
      setEmailError('Email is required');
      isValid = false;
    } else if (!validateEmail(email)) {
      setEmailError('Please enter a valid email address');
      isValid = false;
    }
    
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
    
    // Name validation (only for sign up)
    if (isSignUp) {
      if (!name) {
        setNameError('Name is required');
        isValid = false;
      } else {
        const nameValidation = validateName(name);
        if (!nameValidation.isValid) {
          setNameError(nameValidation.message);
          isValid = false;
        }
      }
    }
    
    return isValid;
  };

  const handleEmailAuth = async () => {
    if (!validateForm()) {
      // Reset auto-filled state if form validation fails
      if (autoFilled) {
        setAutoFilled(false);
      }
      return;
    }

    setIsEmailAuthLoading(true);
    try {
      if (isSignUp) {
        await signUp(email, password, name);
        Alert.alert('Success', 'Account created successfully! Please check your email for verification instructions.');
        router.replace('/(tabs)/mine');
        // Reset error state on successful signup
        setSignupErrorOccurred(false);
      } else {
        await signInWithEmail(email, password);
        router.replace('/(tabs)/mine');
      }
      // Reset auto-filled state on successful authentication
      if (autoFilled) {
        setAutoFilled(false);
        setAutoLoginAttempted(false);
      }
    } catch (error: any) {
      console.error('Email auth error:', error);
      let errorMessage = error.message || 'Failed to authenticate';
      
      // Handle specific Appwrite error codes
      if (error.code === 401) {
        errorMessage = 'Invalid email or password. Please try again.';
      } else if (error.code === 409) {
        errorMessage = 'An account with this email already exists. Please sign in instead.';
      } else if (error.code === 400) {
        errorMessage = 'Invalid input. Please check your email and password.';
      }
      
      Alert.alert('Authentication Error', errorMessage);
      setAutoFilled(false); // Reset auto-filled state on error
      setAutoLoginAttempted(false); // Reset auto-login attempted state on error
      
      // Set signup error state if this was a signup attempt
      if (isSignUp) {
        setSignupErrorOccurred(true);
      }
    } finally {
      setIsEmailAuthLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    setIsGoogleAuthLoading(true);
    try {
      await signIn();
      // Navigation will be handled by the OAuth flow
      // Don't reset loading state here as it will be handled by the OAuth flow
    } catch (error: any) {
      console.error('Google sign in error:', error);
      let errorMessage = error.message || 'Failed to authenticate with Google';
      
      // Handle specific OAuth errors
      if (errorMessage.includes('Invalid redirect URL')) {
        errorMessage = 'OAuth is not properly configured. Please contact support.';
      } else if (errorMessage.includes('cancelled')) {
        errorMessage = 'Authentication was cancelled.';
      } else if (errorMessage.includes('timeout')) {
        errorMessage = 'Authentication timed out. Please check your internet connection and try again.';
      }
      
      Alert.alert('Authentication Error', errorMessage);
    } finally {
      // Only reset loading state on error, not on success as navigation is handled by OAuth flow
      // Add a small delay to ensure the OAuth flow has started
      setTimeout(() => {
        setIsGoogleAuthLoading(false);
      }, 2000);
    }
  };

  const handleEmailChange = (text: string) => {
    setEmail(text);
    // Clear error when user starts typing
    if (emailError) setEmailError('');
  };

  const handlePasswordChange = (text: string) => {
    setPassword(text);
    // Clear error when user starts typing
    if (passwordError) setPasswordError('');
  };

  const handleNameChange = (text: string) => {
    setName(text);
    // Clear error when user starts typing
    if (nameError) setNameError('');
  };

  return (
    <LinearGradient
      colors={['#1a1a2e', '#16213e', '#0f3460']}
      style={styles.container}
    >
      <SafeAreaView style={styles.safeArea}>
        <ScrollView contentContainerStyle={styles.scrollContainer}>
          <View style={styles.content}>
            <Text style={styles.title}>{isSignUp ? 'Create Account' : 'Welcome Back'}</Text>
            <Text style={styles.subtitle}>
              {isSignUp ? 'Sign up to get started' : 'Sign in to continue'}
            </Text>

            {/* Auth Card */}
            <View style={styles.authCard}>
              {isSignUp && (
                <View style={styles.inputContainer}>
                  <Text style={styles.label}>Name</Text>
                  <TextInput
                    style={[styles.input, nameError ? styles.inputError : undefined]}
                    value={name}
                    onChangeText={handleNameChange}
                    placeholder="Enter your name"
                    placeholderTextColor="#aaa"
                  />
                  {nameError ? <Text style={styles.errorText}>{nameError}</Text> : null}
                </View>
              )}

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

              <View style={styles.inputContainer}>
                <Text style={styles.label}>Password</Text>
                <TextInput
                  style={[styles.input, passwordError ? styles.inputError : undefined]}
                  value={password}
                  onChangeText={handlePasswordChange}
                  placeholder="Enter your password"
                  placeholderTextColor="#aaa"
                  secureTextEntry
                />
                {passwordError ? <Text style={styles.errorText}>{passwordError}</Text> : null}
              </View>

              <TouchableOpacity
                style={[styles.button, isEmailAuthLoading && styles.buttonDisabled]}
                onPress={handleEmailAuth}
                disabled={isEmailAuthLoading}
              >
                {isEmailAuthLoading ? (
                  <LoadingDots color="#000000" size={8} />
                ) : (
                  <Text style={styles.buttonText}>
                    {isSignUp ? 'Sign Up' : 'Sign In'}
                  </Text>
                )}
              </TouchableOpacity>

              <View style={styles.divider}>
                <View style={styles.dividerLine} />
                <Text style={styles.dividerText}>or</Text>
                <View style={styles.dividerLine} />
              </View>

              <TouchableOpacity
                style={[styles.googleButton, isGoogleAuthLoading && styles.buttonDisabled]}
                onPress={handleGoogleSignIn}
                disabled={isGoogleAuthLoading}
              >
                {isGoogleAuthLoading ? (
                  <LoadingDots color="#000000" size={8} />
                ) : (
                  <Text style={styles.googleButtonText}>
                    Continue with Google
                  </Text>
                )}
              </TouchableOpacity>
            </View>

            <TouchableOpacity
              style={styles.switchMode}
              onPress={() => {
                setIsSignUp(!isSignUp);
                // Clear errors when switching modes
                setEmailError('');
                setPasswordError('');
                setNameError('');
                // Reset signup error state when switching modes
                setSignupErrorOccurred(false);
              }}
            >
              <Text style={styles.switchModeText}>
                {isSignUp
                  ? 'Already have an account? Sign In'
                  : "Don't have an account? Sign Up"}
              </Text>
            </TouchableOpacity>

            {!isSignUp && (
              <TouchableOpacity
                style={styles.forgotPassword}
                onPress={() => router.push('/forgot-password')}
              >
                <Text style={styles.forgotPasswordText}>Forgot Password?</Text>
              </TouchableOpacity>
            )}
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
  authCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 24,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
    ...Platform.select({
      android: {
        elevation: 4,
      },
      ios: {
        shadowColor: '#000',
        shadowOffset: {
          width: 0,
          height: 2,
        },
        shadowOpacity: 0.25,
        shadowRadius: 3.84,
      },
    }),
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
  divider: {
    flexDirection: 'row',
    alignItems: 'center',
    marginVertical: 24,
  },
  dividerLine: {
    flex: 1,
    height: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
  },
  dividerText: {
    color: 'rgba(255, 255, 255, 0.5)',
    paddingHorizontal: 16,
    fontSize: 14,
  },
  googleButton: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 16,
    alignItems: 'center',
    marginBottom: 8,
  },
  googleButtonText: {
    color: '#000000',
    fontSize: 16,
    fontWeight: 'bold',
  },
  switchMode: {
    alignItems: 'center',
    marginBottom: 16,
  },
  switchModeText: {
    color: '#ffa000',
    fontSize: 16,
  },
  forgotPassword: {
    alignItems: 'center',
  },
  forgotPasswordText: {
    color: '#ffa000',
    fontSize: 16,
  },
});