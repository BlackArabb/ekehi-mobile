import { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { 
  Zap, 
  Coins, 
  Play, 
  // Remove the problematic imports and use only the ones we know work
  TrendingUp,
  Timer,
  Gem,
  ShoppingBag
} from 'lucide-react-native';
import { UserProfile } from '@/types';
import { useRouter } from 'expo-router';
import { databases, appwriteConfig } from '@/config/appwrite';
import { useMining } from '@/contexts/MiningContext';

interface AutoMiningStatusProps {
  profile: UserProfile;
  onRefresh?: () => void;
}

export default function AutoMiningStatus({ profile, onRefresh }: AutoMiningStatusProps) {
  const router = useRouter();
  const { endMiningSession } = useMining();
  const [isActive, setIsActive] = useState(false);
  const [sessionTime, setSessionTime] = useState(0);
  const [liveEarnings, setLiveEarnings] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // Check if auto mining is available (coinsPerSecond > 0)
    if (profile.coinsPerSecond > 0) {
      // Auto start mining if user has purchased tokens
      setIsActive(true);
    }
  }, [profile.coinsPerSecond]);

  useEffect(() => {
    let interval: ReturnType<typeof setTimeout>;
    
    if (isActive && profile.coinsPerSecond > 0) {
      interval = setInterval(() => {
        setSessionTime(prev => prev + 1);
        setLiveEarnings(prev => prev + profile.coinsPerSecond);
        
        // Update backend every 10 seconds
        if (sessionTime % 10 === 0) {
          updateBackendEarnings();
        }
      }, 1000);
    }

    return () => {
      if (interval) {
        clearInterval(interval);
      }
      // End mining session when component unmounts
      endMiningSession();
    };
  }, [isActive, profile.coinsPerSecond, sessionTime]);

  const updateBackendEarnings = async () => {
    try {
      if (!profile) return;
      
      // Calculate earnings for the last 10 seconds
      const earnings = profile.coinsPerSecond * 10;
      
      // Update user profile with new earnings
      const updatedProfile = {
        ...profile,
        totalCoins: profile.totalCoins + earnings,
        lifetimeEarnings: profile.lifetimeEarnings + earnings,
        todayEarnings: profile.todayEarnings + earnings,
        updatedAt: new Date().toISOString()
      };

      await databases.updateDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        profile.id,
        {
          totalCoins: updatedProfile.totalCoins,
          lifetimeEarnings: updatedProfile.lifetimeEarnings,
          todayEarnings: updatedProfile.todayEarnings,
          updatedAt: updatedProfile.updatedAt
        }
      );
      
      onRefresh?.();
    } catch (error) {
      console.error('Failed to update auto mining earnings:', error);
    }
  };

  const handleToggle = async () => {
    if (profile.coinsPerSecond === 0) {
      Alert.alert(
        'Auto Mining Locked',
        'Purchase EKH tokens in presale to unlock auto mining!',
        [
          { text: 'Cancel', style: 'cancel' },
          { text: 'View Presale', onPress: () => router.push('/(tabs)/presale') },
        ]
      );
      return;
    }

    setIsLoading(true);
    try {
      if (isActive) {
        // Stop auto mining - update final earnings
        await updateBackendEarnings();
        setIsActive(false);
        setSessionTime(0);
        setLiveEarnings(0);
        // End mining session when stopping
        await endMiningSession();
        Alert.alert('Auto Mining Stopped', 'Your earnings have been saved!');
      } else {
        // Start auto mining
        setIsActive(true);
        setSessionTime(0);
        setLiveEarnings(0);
        Alert.alert('Auto Mining Started', 'You are now earning passive income!');
      }
    } catch (error) {
      console.error('Failed to toggle auto mining:', error);
      Alert.alert('Error', 'Failed to toggle auto mining. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    
    if (hours > 0) {
      return `${hours}h ${minutes}m ${secs}s`;
    } else if (minutes > 0) {
      return `${minutes}m ${secs}s`;
    } else {
      return `${secs}s`;
    }
  };

  if (profile.coinsPerSecond === 0) {
    return (
      <View style={styles.container}>
        <LinearGradient
          colors={['rgba(107, 114, 128, 0.2)', 'rgba(75, 85, 99, 0.1)']}
          style={styles.lockedCard}
        >
          <View style={styles.lockedContent}>
            <View style={styles.lockedIconContainer}>
              <Zap size={32} color="#6b7280" />
            </View>
            <Text style={styles.lockedTitle}>Auto Mining Locked</Text>
            <Text style={styles.lockedDescription}>
              Purchase EKH tokens in presale to unlock passive income generation!
            </Text>
            
            <TouchableOpacity style={styles.presaleButton} onPress={() => router.push('/(tabs)/presale')}>
              <LinearGradient
                colors={['#ffa000', '#ff8f00']}
                style={styles.presaleButtonGradient}
              >
                <ShoppingBag size={16} color="#ffffff" />
                <Text style={styles.presaleButtonText}>Join Presale</Text>
              </LinearGradient>
            </TouchableOpacity>
          </View>
        </LinearGradient>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <LinearGradient
        colors={['rgba(34, 197, 94, 0.2)', 'rgba(16, 185, 129, 0.1)']}
        style={styles.card}
      >
        {/* Header */}
        <View style={styles.header}>
          <View style={styles.titleContainer}>
            <View style={[
              styles.iconContainer,
              { backgroundColor: isActive ? '#10b981' : '#6b7280' }
            ]}>
              <Zap size={24} color="#ffffff" />
            </View>
            <View style={styles.titleInfo}>
              <Text style={styles.title}>Auto Mining</Text>
              <Text style={styles.subtitle}>
                {isActive ? 'Currently active' : 'Ready to start'}
              </Text>
            </View>
          </View>

          <TouchableOpacity 
            style={styles.toggleButton} 
            onPress={handleToggle}
            disabled={isLoading}
          >
            <LinearGradient
              colors={isActive ? ['#ef4444', '#dc2626'] : ['#10b981', '#059669']}
              style={styles.toggleButtonGradient}
            >
              {isActive ? (
                <>
                  {/* Use a simple text instead of an icon that might not exist */}
                  <Text style={styles.toggleButtonText}>Stop</Text>
                </>
              ) : (
                <>
                  <Play size={16} color="#ffffff" />
                  <Text style={styles.toggleButtonText}>Start</Text>
                </>
              )}
            </LinearGradient>
          </TouchableOpacity>
        </View>

        {/* Stats Grid */}
        <View style={styles.statsGrid}>
          <View style={styles.statCard}>
            <TrendingUp size={20} color="#10b981" />
            <Text style={styles.statLabel}>Per Second</Text>
            <Text style={styles.statValue}>{profile.coinsPerSecond.toFixed(2)}</Text>
          </View>

          <View style={styles.statCard}>
            <Coins size={20} color="#ffa000" />
            <Text style={styles.statLabel}>Balance</Text>
            <Text style={styles.statValue}>{profile.totalCoins.toLocaleString()}</Text>
          </View>
        </View>

        {/* Session Info */}
        {isActive && (
          <View style={styles.sessionContainer}>
            <View style={styles.sessionCard}>
              <View style={styles.sessionRow}>
                <View style={styles.sessionInfo}>
                  <Timer size={20} color="#6b7280" />
                  <Text style={styles.sessionLabel}>Session Time</Text>
                </View>
                <Text style={styles.sessionValue}>{formatTime(sessionTime)}</Text>
              </View>
            </View>

            <View style={styles.sessionCard}>
              <View style={styles.sessionRow}>
                <View style={styles.sessionInfo}>
                  <Gem size={20} color="#10b981" />
                  <Text style={styles.sessionLabel}>Live Earnings</Text>
                </View>
                <View style={styles.sessionValueContainer}>
                  <Text style={styles.sessionValue}>+{liveEarnings.toFixed(2)}</Text>
                  <Text style={styles.sessionSubtext}>This session</Text>
                </View>
              </View>
            </View>

            {/* Projected Earnings */}
            <View style={styles.projectedContainer}>
              <Text style={styles.projectedTitle}>Projected Earnings</Text>
              <View style={styles.projectedGrid}>
                <View style={styles.projectedItem}>
                  <Text style={styles.projectedValue}>
                    {(profile.coinsPerSecond * 60).toFixed(0)}
                  </Text>
                  <Text style={styles.projectedLabel}>Per Minute</Text>
                </View>
                <View style={styles.projectedItem}>
                  <Text style={styles.projectedValue}>
                    {(profile.coinsPerSecond * 3600).toFixed(0)}
                  </Text>
                  <Text style={styles.projectedLabel}>Per Hour</Text>
                </View>
                <View style={styles.projectedItem}>
                  <Text style={styles.projectedValue}>
                    {(profile.coinsPerSecond * 86400).toFixed(0)}
                  </Text>
                  <Text style={styles.projectedLabel}>Per Day</Text>
                </View>
              </View>
            </View>
          </View>
        )}

        {/* Status Indicator */}
        <View style={styles.statusContainer}>
          <View style={[
            styles.statusIndicator,
            { backgroundColor: isActive ? 'rgba(16, 185, 129, 0.2)' : 'rgba(107, 114, 128, 0.2)' }
          ]}>
            <View style={[
              styles.statusDot,
              { backgroundColor: isActive ? '#10b981' : '#6b7280' }
            ]} />
            <Text style={[
              styles.statusText,
              { color: isActive ? '#10b981' : '#6b7280' }
            ]}>
              {isActive ? 'Mining Active' : 'Ready to Mine'}
            </Text>
          </View>
        </View>
      </LinearGradient>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    marginBottom: 16,
  },
  card: {
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(16, 185, 129, 0.3)',
  },
  lockedCard: {
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(107, 114, 128, 0.3)',
  },
  lockedContent: {
    alignItems: 'center',
  },
  lockedIconContainer: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: 'rgba(107, 114, 128, 0.3)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  lockedTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#6b7280',
    marginBottom: 8,
    textAlign: 'center',
  },
  lockedDescription: {
    fontSize: 14,
    color: 'rgba(107, 114, 128, 0.8)',
    textAlign: 'center',
    lineHeight: 20,
    marginBottom: 20,
  },
  presaleButton: {
    borderRadius: 12,
    overflow: 'hidden',
  },
  presaleButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 12,
    gap: 6,
  },
  presaleButtonText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  titleContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  iconContainer: {
    width: 48,
    height: 48,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  titleInfo: {
    flex: 1,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  subtitle: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
  },
  toggleButton: {
    borderRadius: 12,
    overflow: 'hidden',
  },
  toggleButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    gap: 6,
  },
  toggleButtonText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  statsGrid: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 16,
  },
  statCard: {
    flex: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    padding: 12,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  statLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 4,
    marginBottom: 2,
  },
  statValue: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  sessionContainer: {
    marginBottom: 16,
  },
  sessionCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 12,
    padding: 12,
    marginBottom: 8,
  },
  sessionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  sessionInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  sessionLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  sessionValue: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  sessionValueContainer: {
    alignItems: 'flex-end',
  },
  sessionSubtext: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.5)',
  },
  projectedContainer: {
    marginTop: 8,
  },
  projectedTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: '#ffffff',
    marginBottom: 12,
    textAlign: 'center',
  },
  projectedGrid: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  projectedItem: {
    alignItems: 'center',
  },
  projectedValue: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#10b981',
  },
  projectedLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 2,
  },
  statusContainer: {
    alignItems: 'center',
  },
  statusIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
    gap: 8,
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  statusText: {
    fontSize: 14,
    fontWeight: '600',
  },
});