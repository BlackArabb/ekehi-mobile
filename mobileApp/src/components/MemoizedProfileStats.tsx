import React, { memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Zap, Calendar, Users, Trophy } from 'lucide-react-native';

interface MemoizedProfileStatsProps {
  profile: any;
  totalEKH: number;
}

const MemoizedProfileStats: React.FC<MemoizedProfileStatsProps> = ({ profile, totalEKH }) => {
  // Format numbers with commas
  const formatNumber = (num: number | undefined) => {
    if (num === undefined) return '0';
    return num.toLocaleString();
  };

  return (
    <View style={styles.statsGrid}>
      {/* Total EKH */}
      <View style={styles.statCard}>
        <Trophy size={24} color="#ffa000" />
        <Text style={styles.statValue}>{formatNumber(totalEKH)}</Text>
        <Text style={styles.statLabel}>Total EKH</Text>
      </View>
      
      {/* Mining Rate */}
      <View style={styles.statCard}>
        <Zap size={24} color="#8b5cf6" />
        <Text style={styles.statValue}>
          {profile?.dailyMiningRate ? (profile.dailyMiningRate / 24).toFixed(4) : '0.0000'}
        </Text>
        <Text style={styles.statLabel}>Mining Rate</Text>
        <Text style={styles.statSubLabel}>EKH/hour</Text>
      </View>
      
      {/* Day Streak */}
      <View style={styles.statCard}>
        <Calendar size={24} color="#10b981" />
        <Text style={styles.statValue}>{profile?.currentStreak || '0'}</Text>
        <Text style={styles.statLabel}>Day Streak</Text>
      </View>
      
      {/* Referrals */}
      <View style={styles.statCard}>
        <Users size={24} color="#f59e0b" />
        <Text style={styles.statValue}>{profile?.totalReferrals || '0'}</Text>
        <Text style={styles.statLabel}>Referrals</Text>
      </View>
      
      {/* Efficiency */}
      <View style={styles.statCard}>
        <Trophy size={24} color="#6366f1" />
        <Text style={styles.statValue}>
          {profile?.miningPower && profile?.dailyMiningRate 
            ? ((profile.dailyMiningRate / profile.miningPower) * 100).toFixed(1) 
            : '0'}
          %
        </Text>
        <Text style={styles.statLabel}>Efficiency</Text>
      </View>
    </View>
  );
};

// Memoize the entire component
export default memo(MemoizedProfileStats, (prevProps, nextProps) => {
  // Custom comparison function to prevent unnecessary re-renders
  return (
    prevProps.totalEKH === nextProps.totalEKH &&
    prevProps.profile?.dailyMiningRate === nextProps.profile?.dailyMiningRate &&
    prevProps.profile?.currentStreak === nextProps.profile?.currentStreak &&
    prevProps.profile?.totalReferrals === nextProps.profile?.totalReferrals &&
    prevProps.profile?.miningPower === nextProps.profile?.miningPower
  );
});

const styles = StyleSheet.create({
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    marginTop: 20,
  },
  statCard: {
    width: '48%',
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
  },
  statValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginTop: 8,
    marginBottom: 4,
  },
  statLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.7)',
    fontWeight: '500',
  },
  statSubLabel: {
    fontSize: 10,
    color: 'rgba(255, 255, 255, 0.5)',
  },
});