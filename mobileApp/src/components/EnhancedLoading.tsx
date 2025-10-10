import React from 'react';
import { View, Text, StyleSheet, ActivityIndicator } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import PulseLoader from '@/components/PulseLoader';

interface EnhancedLoadingProps {
  message?: string;
  showDots?: boolean;
  size?: 'small' | 'large';
  color?: string;
}

const EnhancedLoading: React.FC<EnhancedLoadingProps> = ({ 
  message = 'Loading...', 
  showDots = true,
  size = 'large',
  color = '#ffa000'
}) => {
  return (
    <View style={styles.container}>
      <LinearGradient
        colors={['rgba(255, 255, 255, 0.05)', 'rgba(255, 255, 255, 0.02)']}
        style={styles.card}
      >
        <View style={styles.content}>
          <ActivityIndicator 
            size={size} 
            color={color} 
            style={styles.spinner}
          />
          
          <Text style={styles.message}>{message}</Text>
          
          {showDots && (
            <View style={styles.dotsContainer}>
              <PulseLoader size={8} color={color} />
            </View>
          )}
        </View>
      </LinearGradient>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  card: {
    borderRadius: 16,
    padding: 30,
    minWidth: 200,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
    backgroundColor: 'rgba(0, 0, 0, 0.3)',
  },
  content: {
    alignItems: 'center',
  },
  spinner: {
    marginBottom: 20,
  },
  message: {
    fontSize: 16,
    color: '#ffffff',
    textAlign: 'center',
    marginBottom: 15,
    fontWeight: '500',
  },
  dotsContainer: {
    marginTop: 10,
  },
});

export default EnhancedLoading;