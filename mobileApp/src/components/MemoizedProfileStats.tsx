import React, { memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Zap, Calendar, Users, Trophy } from 'lucide-react-native';
import { usePresale } from '@/contexts/PresaleContext';

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

  // Get auto mining eligibility from presale context
  const { isAutoMiningEligible } = usePresale();
  
  // Calculate individual mining rates
  const manualMiningRate = profile ? (profile.dailyMiningRate || 2) / 24 : 0.0833;
  const autoMiningRate = profile ? profile.autoMiningRate || 0 : 0;
  const referralBonusRate = profile ? profile.referralBonusRate || 0 : 0;
  
  // Only show auto mining rate if user is eligible
  const showAutoMiningRate = profile && isAutoMiningEligible();
  const showReferralRate = profile && profile.totalReferrals > 0 && profile.totalReferrals <= 50;
  
  // Calculate total mining rate
  const totalMiningRate = manualMiningRate + (showAutoMiningRate ? autoMiningRate : 0) + (showReferralRate ? referralBonusRate : 0);

  return (
    <View style={styles.statsGrid}>
      {/* Total EKH */}
      <View style={styles.statCard}>
        <Trophy size={24} color="#ffa000" />
        <Text style={styles.statValue}>{formatNumber(totalEKH)}</Text>
        <Text style={styles.statLabel}>Total EKH</Text>
      </View>
      
      {/* Manual Mining Rate */}
      <View style={styles.statCard}>
        <Zap size={24} color="#8b5cf6" />
        <Text style={styles.statValue}>
          {manualMiningRate.toFixed(4)}
        </Text>
        <Text style={styles.statLabel}>Manual Rate</Text>
        <Text style={styles.statSubLabel}>EKH/hour</Text>
      </View>
      
      {/* Auto Mining Rate - Only shown if user is eligible */}
      {showAutoMiningRate && (
        <View style={styles.statCard}>
          <Zap size={24} color="#10b981" />
          <Text style={styles.statValue}>
            {autoMiningRate.toFixed(4)}
          </Text>
          <Text style={styles.statLabel}>Auto Rate</Text>
          <Text style={styles.statSubLabel}>EKH/hour</Text>
        </View>
      )}
      
      {/* Referral Bonus Rate - Only shown if user has referrals */}
      {showReferralRate && (
        <View style={styles.statCard}>
          <Users size={24} color="#f59e0b" />
          <Text style={styles.statValue}>
            {referralBonusRate.toFixed(4)}
          </Text>
          <Text style={styles.statLabel}>Referral Bonus</Text>
          <Text style={styles.statSubLabel}>EKH/hour</Text>
        </View>
      )}
      
      {/* Total Mining Rate */}
      <View style={styles.statCard}>
        <Zap size={24} color="#6366f1" />
        <Text style={styles.statValue}>
          {totalMiningRate.toFixed(4)}
        </Text>
        <Text style={styles.statLabel}>Total Mining Rate</Text>
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
    </View>
  );
};

// Memoize the entire component
export default memo(MemoizedProfileStats, (prevProps, nextProps) => {
  // Custom comparison function to prevent unnecessary re-renders
  return (
    prevProps.totalEKH === nextProps.totalEKH &&
    prevProps.profile?.dailyMiningRate === nextProps.profile?.dailyMiningRate &&
    prevProps.profile?.autoMiningRate === nextProps.profile?.autoMiningRate &&
    prevProps.profile?.referralBonusRate === nextProps.profile?.referralBonusRate &&
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