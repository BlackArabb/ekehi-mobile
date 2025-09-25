import { View, Text, StyleSheet, TouchableOpacity, Alert, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import * as Linking from 'expo-linking';

export default function TestDeepLink() {
  const router = useRouter();

  const testDeepLink = async () => {
    try {
      const url = 'ekehi://oauth/return';
      const supported = await Linking.canOpenURL(url);
      
      if (supported) {
        await Linking.openURL(url);
        Alert.alert('Success', 'Deep link opened successfully');
      } else {
        Alert.alert('Error', `Cannot open deep link: ${url}`);
      }
    } catch (error: any) {
      Alert.alert('Error', `Failed to open deep link: ${error?.message || error}`);
    }
  };

  const showDeepLinkInfo = () => {
    const info = `
Platform: ${Platform.OS}
Deep Link Scheme: ekehi
Expected URL: ekehi://oauth/return
Package/Bundle ID: com.ekehi.network

Make sure this deep link is properly configured in:
1. Appwrite Console > Authentication > Platforms
2. Google Cloud Console > OAuth Client > Redirect URIs
    `;
    
    Alert.alert('Deep Link Information', info);
  };

  return (
    <LinearGradient
      colors={['#1a1a2e', '#16213e', '#0f3460']}
      style={styles.container}
    >
      <View style={styles.content}>
        <Text style={styles.title}>Deep Link Test</Text>
        <Text style={styles.subtitle}>
          Test if your deep linking is properly configured
        </Text>
        
        <View style={styles.buttonContainer}>
          <TouchableOpacity style={styles.button} onPress={testDeepLink}>
            <Text style={styles.buttonText}>Test Deep Link</Text>
          </TouchableOpacity>
          
          <TouchableOpacity style={[styles.button, styles.infoButton]} onPress={showDeepLinkInfo}>
            <Text style={styles.buttonText}>Show Configuration Info</Text>
          </TouchableOpacity>
          
          <TouchableOpacity 
            style={[styles.button, styles.backButton]} 
            onPress={() => router.replace('/')}
          >
            <Text style={styles.buttonText}>Back to Home</Text>
          </TouchableOpacity>
        </View>
        
        <View style={styles.instructions}>
          <Text style={styles.instructionTitle}>Configuration Instructions:</Text>
          <Text style={styles.instructionText}>
            1. In Appwrite Console, add "ekehi://oauth/return" as a redirect URL
          </Text>
          <Text style={styles.instructionText}>
            2. In Google Cloud Console, add "ekehi://oauth/return" as a redirect URI
          </Text>
          <Text style={styles.instructionText}>
            3. Ensure your package name is "com.ekehi.network"
          </Text>
        </View>
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
    fontSize: 28,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 16,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 18,
    color: 'rgba(255, 255, 255, 0.8)',
    marginBottom: 32,
    textAlign: 'center',
    lineHeight: 24,
  },
  buttonContainer: {
    width: '100%',
    marginBottom: 32,
  },
  button: {
    backgroundColor: '#8b5cf6',
    padding: 16,
    borderRadius: 12,
    marginBottom: 16,
    alignItems: 'center',
  },
  infoButton: {
    backgroundColor: '#1e40af',
  },
  backButton: {
    backgroundColor: '#4b5563',
  },
  buttonText: {
    color: '#ffffff',
    fontSize: 18,
    fontWeight: '600',
  },
  instructions: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    padding: 20,
    width: '100%',
  },
  instructionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 16,
    textAlign: 'center',
  },
  instructionText: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.8)',
    marginBottom: 12,
    lineHeight: 22,
  },
});