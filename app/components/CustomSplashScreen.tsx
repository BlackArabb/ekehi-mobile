import React, { useEffect } from 'react';
import { View, Text, StyleSheet, Image } from 'react-native';
import * as SplashScreen from 'expo-splash-screen';

const CustomSplashScreen: React.FC = () => {
  useEffect(() => {
    SplashScreen.preventAutoHideAsync();
    
    // Simulate loading time or actual app initialization
    setTimeout(async () => {
      await SplashScreen.hideAsync();
    }, 2000);
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        <Image 
          source={require('../../assets/splash.png')} 
          style={styles.logo}
          resizeMode="contain"
        />
        
        <View style={styles.textContainer}>
          <Text style={[styles.mainText, styles.textShadow]}>ekehi</Text>
          <Text style={[styles.networkText, styles.textShadow]}>network</Text>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
    justifyContent: 'center',
    alignItems: 'center',
  },
  content: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  logo: {
    width: 100,
    height: 100,
    marginBottom: 30,
  },
  textContainer: {
    alignItems: 'center',
  },
  mainText: {
    fontSize: 32,
    fontWeight: '800',
    color: '#ffffff',
    letterSpacing: 1.5,
  },
  networkText: {
    fontSize: 24,
    fontWeight: '600',
    color: '#ffa000',
    marginTop: 5,
    letterSpacing: 1,
  },
  textShadow: {
    textShadowColor: 'rgba(255, 160, 0, 0.5)',
    textShadowOffset: { width: 2, height: 2 },
    textShadowRadius: 4,
  },
});

export default CustomSplashScreen;