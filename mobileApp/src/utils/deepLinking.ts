import * as Linking from 'expo-linking';
import AsyncStorage from '@react-native-async-storage/async-storage';

export const handleDeepLink = async (url: string) => {
  try {
    const { hostname, path } = Linking.parse(url);
    
    if (hostname === 'referral' && path) {
      const referralCode = path.replace('/', '');
      // Store the referral code for later use during signup
      await AsyncStorage.setItem('referralCode', referralCode);
      console.log('Referral code saved:', referralCode);
      return { type: 'referral', code: referralCode };
    }
    
    return { type: 'unknown', url };
  } catch (error) {
    console.error('Error handling deep link:', error);
    return { type: 'error', error };
  }
};

export const initializeDeepLinking = () => {
  const handleUrl = (event: { url: string }) => {
    handleDeepLink(event.url);
  };

  const subscription = Linking.addEventListener('url', handleUrl);
  
  // Check initial URL
  Linking.getInitialURL().then((url) => {
    if (url) {
      handleDeepLink(url);
    }
  });

  return () => {
    subscription.remove();
  };
};