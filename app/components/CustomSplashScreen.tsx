import React from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import * as SplashScreen from 'expo-splash-screen';

export default function CustomSplashScreen() {
  React.useEffect(() => {
    // Keep the splash screen visible while we fetch resources
    SplashScreen.preventAutoHideAsync();
    
    // Simulate loading time
    setTimeout(async () => {
      await SplashScreen.hideAsync();
    }, 2000);
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.content}>
        {/* Logo with reduced size */}
        <Image 
          source={require('../../assets/splash.png')} 
          style={styles.logo}
          resizeMode="contain"
        />
        
        {/* Text effect for "ekehi network" */}
        <View style={styles.textContainer}>
          <Text style={styles.title}>
            ekehi
          </Text>
          <Text style={styles.subtitle}>
            network
          </Text>
        </View>
        
        {/* Simple loader dots */}
        <View style={styles.loaderContainer}>
          <View style={[styles.loaderDot, { opacity: 0.6 }]} />
          <View style={[styles.loaderDot, { opacity: 0.8 }]} />
          <View style={[styles.loaderDot, { opacity: 1 }]} />
        </View>
      </View>
    </View>
  );
}

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
    marginBottom: 40,
  },
  title: {
    fontSize: 32,
    fontWeight: '800',
    color: '#ffffff',
    letterSpacing: 1.5,
    textShadowColor: 'rgba(255, 160, 0, 0.5)',
    textShadowOffset: { width: 2, height: 2 },
    textShadowRadius: 4,
  },
  subtitle: {
    fontSize: 24,
    fontWeight: '600',
    color: '#ffa000',
    marginTop: 5,
    letterSpacing: 1,
    textShadowColor: 'rgba(255, 160, 0, 0.5)',
    textShadowOffset: { width: 2, height: 2 },
    textShadowRadius: 4,
  },
  loaderContainer: {
    flexDirection: 'row',
    marginTop: 20,
  },
  loaderDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#ffa000',
    marginHorizontal: 5,
  },
});