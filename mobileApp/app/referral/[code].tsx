import React, { useEffect } from 'react';
import { View, Text, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useReferral } from '@/contexts/ReferralContext';
import { useAuth } from '@/contexts/AuthContext';

export default function ReferralLinkPage() {
  const { code } = useLocalSearchParams();
  const router = useRouter();
  const { claimReferral } = useReferral();
  const { user } = useAuth();

  useEffect(() => {
    const processReferral = async () => {
      if (!code || typeof code !== 'string') {
        Alert.alert('Invalid Referral Link', 'This referral link is invalid.');
        router.replace('/(tabs)/profile');
        return;
      }

      if (!user) {
        // If user is not logged in, we should prompt them to log in first
        // For now, we'll redirect to the main page where they can log in
        Alert.alert(
          'Login Required', 
          'Please log in to claim this referral code.',
          [{ text: 'OK', onPress: () => router.replace('/') }]
        );
        return;
      }

      try {
        const result = await claimReferral(code);
        if (result.success) {
          Alert.alert(
            'Referral Claimed!', 
            result.message,
            [{ text: 'OK', onPress: () => router.replace('/(tabs)/profile') }]
          );
        } else {
          Alert.alert(
            'Error', 
            result.message,
            [{ text: 'OK', onPress: () => router.replace('/(tabs)/profile') }]
          );
        }
      } catch (error) {
        console.error('Error claiming referral:', error);
        Alert.alert(
          'Error', 
          'Failed to claim referral code. Please try again.',
          [{ text: 'OK', onPress: () => router.replace('/(tabs)/profile') }]
        );
      }
    };

    processReferral();
  }, [code, user]);

  return (
    <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
      <View style={styles.content}>
        <ActivityIndicator size="large" color="#ffa000" />
        <Text style={styles.loadingText}>Processing referral code...</Text>
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
    padding: 20,
  },
  loadingText: {
    fontSize: 18,
    color: '#ffffff',
    marginTop: 16,
    textAlign: 'center',
  },
});