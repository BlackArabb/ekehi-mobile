import { useEffect, useRef, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, ActivityIndicator } from 'react-native';
import { useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { Pickaxe, Coins, Users, TrendingUp, Gift, Shield } from 'lucide-react-native';
import { useAuth } from '@/contexts/AuthContext';
import AsyncStorage from '@react-native-async-storage/async-storage';

export default function HomeScreen() {
  const router = useRouter();
  const { user, isLoading: authLoading } = useAuth();
  
  const [manualLoading, setManualLoading] = useState(false);
  const [hasVisited, setHasVisited] = useState(false);
  const navigationTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Check if user has visited before
  useEffect(() => {
    const checkFirstVisit = async () => {
      try {
        const visited = await AsyncStorage.getItem('hasVisitedLanding');
        if (visited === 'true') {
          setHasVisited(true);
        }
      } catch (error) {
        // If there's an error, we'll show the landing page
        setHasVisited(false);
      }
    };
    
    checkFirstVisit();
  }, []);
  
  // Force loading to false after 15 seconds if stuck
  useEffect(() => {
    const forceLoadingTimeout = setTimeout(() => {
      if (authLoading) {
        setManualLoading(true);
      }
    }, 15000);
    
    return () => {
      clearTimeout(forceLoadingTimeout);
    };
  }, [authLoading]);
  
  // Use manual loading override if needed
  const actualIsLoading = manualLoading ? false : authLoading;

  useEffect(() => {
    // Clear any existing timeout
    if (navigationTimeout.current) {
      clearTimeout(navigationTimeout.current);
    }
    
    // Add a delay to ensure the router is ready
    navigationTimeout.current = setTimeout(() => {
      if (user && !actualIsLoading) {
        try {
          console.log('User authenticated on index page, navigating to mine tab');
          // Only navigate if we're not already on the mine screen
          router.replace('/(tabs)/mine');
        } catch (error) {
          console.error('Navigation error:', error);
        }
      } else if (hasVisited && !user && !actualIsLoading) {
        // If user has visited before and is not authenticated, go directly to auth
        try {
          console.log('Returning user, navigating to auth page');
          router.replace('/auth');
        } catch (error) {
          console.error('Navigation error:', error);
        }
      }
    }, 300);

    return () => {
      if (navigationTimeout.current) {
        clearTimeout(navigationTimeout.current);
      }
    }
  }, [user, actualIsLoading, router, hasVisited]);

  const handleGetStarted = async () => {
    // Mark as visited
    try {
      await AsyncStorage.setItem('hasVisitedLanding', 'true');
    } catch (error) {
      // Silently handle storage error
    }
    
    // Clear any existing timeout
    if (navigationTimeout.current) {
      clearTimeout(navigationTimeout.current);
    }
    
    // Add a delay to ensure the router is ready
    navigationTimeout.current = setTimeout(() => {
      try {
        if (user && !actualIsLoading) {
          console.log('User authenticated, navigating to mine tab');
          router.replace('/(tabs)/mine');
        } else {
          console.log('User not authenticated, navigating to auth page');
          router.push('/auth');
        }
      } catch (error) {
        console.error('Navigation error:', error);
        // Try alternative navigation method
        try {
          router.replace('/auth');
        } catch (fallbackError) {
          console.error('Fallback navigation also failed:', fallbackError);
        }
      }
    }, 100); // Reduced delay for faster response
  };

  // Statistics data
  const stats = [
    { value: '10K+', label: 'Active Miners' },
    { value: '500K+', label: 'EKH Tokens Mined' },
    { value: '24/7', label: 'Mining Available' },
    { value: '100%', label: 'Secure' },
  ];

  // Features data
  const features = [
    {
      icon: <Pickaxe size={24} color="#ffa000" />,
      title: 'Tap to Mine',
      description: 'Earn EKH tokens by tapping the mining button. Simple, fun, and rewarding.'
    },
    {
      icon: <Users size={24} color="#3b82f6" />,
      title: 'Referral System',
      description: 'Invite friends and boost your mining rate with our multi-level referral program.'
    },
    {
      icon: <Gift size={24} color="#8b5cf6" />,
      title: 'Daily Rewards',
      description: 'Claim daily bonuses and participate in special events for extra tokens.'
    },
    {
      icon: <TrendingUp size={24} color="#10b981" />,
      title: 'Leaderboards',
      description: 'Compete with other miners and climb the global rankings for exclusive rewards.'
    },
    {
      icon: <Coins size={24} color="#f59e0b" />,
      title: 'Token Presale',
      description: 'Early access to EKH token purchase with special mining unlocks and benefits.'
    },
    {
      icon: <Shield size={24} color="#ef4444" />,
      title: 'Secure Wallet',
      description: 'Store your EKH tokens safely with our integrated wallet system and blockchain security.'
    }
  ];

  return (
    <LinearGradient
      colors={['#1a1a2e', '#16213e', '#0f3460']}
      style={styles.container}
    >
      <ScrollView 
        style={styles.content}
        contentContainerStyle={styles.contentContainer}
      >
        {/* Header */}
        <View style={styles.header}>
          <View style={styles.logoContainer}>
            <View style={styles.iconContainer}>
              <Pickaxe size={60} color="#ffa000" />
            </View>
            <Text style={styles.title}>Ekehi Network</Text>
            <Text style={styles.subtitle}>
              The Future of Mobile Cryptocurrency Mining
            </Text>
          </View>
        </View>

        {/* Statistics */}
        <View style={styles.statsContainer}>
          {stats.map((stat, index) => (
            <View key={index} style={styles.statItem}>
              <Text style={styles.statValue}>{stat.value}</Text>
              <Text style={styles.statLabel}>{stat.label}</Text>
            </View>
          ))}
        </View>

        {/* Features Section */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Why Choose Ekehi Network?</Text>
          <Text style={styles.sectionSubtitle}>
            A revolutionary platform that combines cryptocurrency mining with social engagement
          </Text>
        </View>

        <View style={styles.featuresContainer}>
          {features.map((feature, index) => (
            <View key={index} style={styles.featureCard}>
              <View style={styles.featureIcon}>{feature.icon}</View>
              <Text style={styles.featureTitle}>{feature.title}</Text>
              <Text style={styles.featureDesc}>{feature.description}</Text>
            </View>
          ))}
        </View>

        {/* How It Works */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>How It Works</Text>
          <Text style={styles.sectionSubtitle}>
            Get started with Ekehi Network in just a few simple steps
          </Text>
        </View>

        <View style={styles.stepsContainer}>
          <View style={styles.step}>
            <View style={styles.stepNumber}>
              <Text style={styles.stepNumberText}>1</Text>
            </View>
            <View style={styles.stepContent}>
              <Text style={styles.stepTitle}>Create Account</Text>
              <Text style={styles.stepDesc}>Sign up with Google or email to join our network</Text>
            </View>
          </View>
          
          <View style={styles.step}>
            <View style={styles.stepNumber}>
              <Text style={styles.stepNumberText}>2</Text>
            </View>
            <View style={styles.stepContent}>
              <Text style={styles.stepTitle}>Start Mining</Text>
              <Text style={styles.stepDesc}>Tap the mining button to earn EKH tokens instantly</Text>
            </View>
          </View>
          
          <View style={styles.step}>
            <View style={styles.stepNumber}>
              <Text style={styles.stepNumberText}>3</Text>
            </View>
            <View style={styles.stepContent}>
              <Text style={styles.stepTitle}>Boost Earnings</Text>
              <Text style={styles.stepDesc}>Complete tasks, invite friends, and climb leaderboards</Text>
            </View>
          </View>
          
          <View style={styles.step}>
            <View style={styles.stepNumber}>
              <Text style={styles.stepNumberText}>4</Text>
            </View>
            <View style={styles.stepContent}>
              <Text style={styles.stepTitle}>Withdraw Rewards</Text>
              <Text style={styles.stepDesc}>Transfer your EKH tokens to your wallet anytime</Text>
            </View>
          </View>
        </View>

        {/* CTA Section */}
        <View style={styles.ctaContainer}>
          <Text style={styles.ctaTitle}>Ready to Start Mining?</Text>
          <Text style={styles.ctaSubtitle}>
            Join thousands of miners earning EKH tokens every day
          </Text>
          
          <View style={styles.buttonContainer}>
            <TouchableOpacity style={styles.button} onPress={handleGetStarted}>
              <LinearGradient
                colors={['#ffa000', '#ff8f00']}
                style={styles.buttonGradient}
              >
                {actualIsLoading || user ? (
                  <ActivityIndicator size="small" color="#ffffff" />
                ) : (
                  <Text style={styles.buttonText}>
                    Get Started Now
                  </Text>
                )}
              </LinearGradient>
            </TouchableOpacity>
          </View>
        </View>

        {/* Footer */}
        <View style={styles.footer}>
          <Text style={styles.footerText}>© 2025 Ekehi Network. All rights reserved.</Text>
          <View style={styles.footerLinks}>
            <Text style={styles.footerLink}>Privacy Policy</Text>
            <Text style={styles.footerLink}>Terms of Service</Text>
            <Text style={styles.footerLink}>Support</Text>
          </View>
        </View>
      </ScrollView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
  },
  contentContainer: {
    padding: 20,
    paddingBottom: 40,
  },
  header: {
    alignItems: 'center',
    marginBottom: 30,
    marginTop: 20,
  },
  logoContainer: {
    alignItems: 'center',
  },
  iconContainer: {
    width: 100,
    height: 100,
    borderRadius: 50,
    backgroundColor: 'rgba(255, 160, 0, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
  },
  title: {
    fontSize: 36,
    fontWeight: '800',
    color: '#ffffff',
    marginBottom: 10,
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 18,
    color: 'rgba(255, 255, 255, 0.8)',
    textAlign: 'center',
    lineHeight: 26,
    maxWidth: 300,
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    flexWrap: 'wrap',
    marginBottom: 40,
    marginTop: 20,
  },
  statItem: {
    alignItems: 'center',
    minWidth: 80,
    marginBottom: 15,
  },
  statValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffa000',
    marginBottom: 4,
  },
  statLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
  },
  section: {
    marginBottom: 30,
    alignItems: 'center',
  },
  sectionTitle: {
    fontSize: 28,
    fontWeight: '700',
    color: '#ffffff',
    marginBottom: 10,
    textAlign: 'center',
  },
  sectionSubtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
    lineHeight: 24,
    maxWidth: 320,
  },
  featuresContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginBottom: 40,
  },
  featureCard: {
    width: '48%',
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 15,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
  },
  featureIcon: {
    marginBottom: 15,
  },
  featureTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#ffffff',
    marginBottom: 8,
  },
  featureDesc: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.6)',
    lineHeight: 20,
  },
  stepsContainer: {
    marginBottom: 40,
  },
  step: {
    flexDirection: 'row',
    marginBottom: 30,
    alignItems: 'flex-start',
  },
  stepNumber: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: 'rgba(255, 160, 0, 0.2)',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
    borderWidth: 2,
    borderColor: '#ffa000',
  },
  stepNumberText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffa000',
  },
  stepContent: {
    flex: 1,
  },
  stepTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#ffffff',
    marginBottom: 5,
  },
  stepDesc: {
    fontSize: 15,
    color: 'rgba(255, 255, 255, 0.7)',
    lineHeight: 22,
  },
  ctaContainer: {
    backgroundColor: 'rgba(255, 160, 0, 0.1)',
    borderRadius: 20,
    padding: 30,
    alignItems: 'center',
    marginBottom: 30,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  ctaTitle: {
    fontSize: 26,
    fontWeight: '700',
    color: '#ffffff',
    marginBottom: 10,
    textAlign: 'center',
  },
  ctaSubtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.8)',
    textAlign: 'center',
    marginBottom: 25,
    lineHeight: 24,
  },
  buttonContainer: {
    width: '100%',
    alignItems: 'center',
  },
  button: {
    borderRadius: 16,
    overflow: 'hidden',
    width: '100%',
    maxWidth: 300,
  },
  buttonGradient: {
    paddingVertical: 18,
    paddingHorizontal: 32,
    alignItems: 'center',
    justifyContent: 'center',
    height: 54, // Maintain consistent height
  },
  buttonText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  secondaryButton: {
    paddingVertical: 18,
    paddingHorizontal: 32,
    alignItems: 'center',
    backgroundColor: 'rgba(139, 92, 246, 0.2)',
    borderRadius: 16,
    width: '100%',
    maxWidth: 300,
    marginTop: 15,
    borderWidth: 1,
    borderColor: 'rgba(139, 92, 246, 0.5)',
  },
  footer: {
    alignItems: 'center',
    paddingTop: 20,
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.1)',
  },
  footerText: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.5)',
    marginBottom: 10,
  },
  footerLinks: {
    flexDirection: 'row',
    gap: 20,
  },
  footerLink: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.5)',
  },
});