import React, { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { useAuth } from '@/contexts/AuthContext';

export default function TestAuthDebug() {
  const { checkAuthStatus, user, isLoading } = useAuth();
  const [debugInfo, setDebugInfo] = useState<string>('');

  const handleCheckAuth = async () => {
    try {
      setDebugInfo('Starting auth check...');
      console.log('🔍 Manual debug: Starting auth check');
      await checkAuthStatus();
      setDebugInfo('Auth check completed');
      console.log('✅ Manual debug: Auth check completed');
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : String(error);
      console.error('❌ Manual debug: Auth check failed:', error);
      setDebugInfo(`Auth check failed: ${errorMessage}`);
      Alert.alert('Check Failed', `Failed to check authentication status: ${errorMessage}`);
    }
  };

  useEffect(() => {
    setDebugInfo(`User state: ${user ? 'Authenticated' : 'Not authenticated'}, Loading: ${isLoading ? 'Yes' : 'No'}`);
  }, [user, isLoading]);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Auth Debug Test</Text>
      <Text style={styles.debugText}>{debugInfo}</Text>
      <TouchableOpacity style={styles.button} onPress={handleCheckAuth}>
        <Text style={styles.buttonText}>Check Auth Status</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    justifyContent: 'center',
    backgroundColor: '#1a1a2e',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 20,
    textAlign: 'center',
  },
  debugText: {
    fontSize: 16,
    color: '#ffffff',
    marginBottom: 20,
    textAlign: 'center',
    minHeight: 60,
  },
  button: {
    backgroundColor: '#4285f4',
    padding: 15,
    borderRadius: 10,
    alignItems: 'center',
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: 'bold',
  },
});