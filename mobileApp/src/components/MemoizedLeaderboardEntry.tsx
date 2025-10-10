import React, { memo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Trophy, Crown, Star, Zap, Flame } from 'lucide-react-native';
import { LeaderboardEntry } from '@/types';

// Memoized function to get tier indicator
const getTierIndicator = memoize((rank: number) => {
  if (rank === 1) return { name: 'LEGENDARY', color: '#FFD700', icon: Crown };
  if (rank <= 3) return { name: 'ELITE', color: '#C0C0C0', icon: Star };
  if (rank <= 10) return { name: 'MASTER', color: '#CD7F32', icon: Flame };
  if (rank <= 25) return { name: 'EXPERT', color: '#4A90E2', icon: Zap };
  return { name: 'MINER', color: '#6B7280', icon: Trophy };
});

// Simple memoization function
function memoize<T extends (...args: any[]) => any>(fn: T): T {
  const cache = new Map<string, ReturnType<T>>();
  return function (...args: Parameters<T>): ReturnType<T> {
    const key = JSON.stringify(args);
    if (cache.has(key)) {
      return cache.get(key)!;
    }
    const result = fn(...args);
    cache.set(key, result);
    return result;
  } as T;
}

// Memoized function to get craft rank badge styles
const getCraftRankBadgeStyles = memoize((rank: number) => {
  if (rank === 1) {
    return {
      colors: ['rgba(255, 215, 0, 0.15)', 'rgba(255, 215, 0, 0.05)'],
      borderColor: 'rgba(255, 215, 0, 0.3)',
      shadowColor: '#FFD700'
    };
  }
  
  if (rank === 2) {
    return {
      colors: ['rgba(192, 192, 192, 0.15)', 'rgba(192, 192, 192, 0.05)'],
      borderColor: 'rgba(192, 192, 192, 0.3)',
      shadowColor: '#C0C0C0'
    };
  }
  
  if (rank === 3) {
    return {
      colors: ['rgba(205, 127, 50, 0.15)', 'rgba(205, 127, 50, 0.05)'],
      borderColor: 'rgba(205, 127, 50, 0.3)',
      shadowColor: '#CD7F32'
    };
  }
  
  return {
    colors: ['rgba(255, 255, 255, 0.08)', 'rgba(255, 255, 255, 0.03)'],
    borderColor: 'rgba(255, 255, 255, 0.2)',
    shadowColor: '#000000'
  };
});

interface MemoizedLeaderboardEntryProps {
  entry: LeaderboardEntry;
  index: number;
}

const MemoizedLeaderboardEntry: React.FC<MemoizedLeaderboardEntryProps> = ({ entry, index }) => {
  const tier = getTierIndicator(entry.rank);
  const TierIcon = tier.icon;
  const badgeStyles = getCraftRankBadgeStyles(entry.rank);
  
  // Format numbers with commas
  const formatNumber = (num: number | undefined) => {
    if (num === undefined) return '0';
    return num.toLocaleString();
  };

  return (
    <LinearGradient
      colors={badgeStyles.colors}
      style={[
        styles.rankCard,
        entry.rank <= 3 && styles.topRankCard
      ]}
    >
      <View style={styles.rankCardContent}>
        {/* Left section - Rank & User */}
        <View style={styles.rankLeft}>
          <View style={styles.rankBadgeContainer}>
            <View style={[styles.craftBadge, styles.regularBadge, { borderColor: badgeStyles.borderColor }]}>
              <View style={styles.craftBadgeInner}>
                <Text style={[styles.rankText, { fontSize: entry.rank <= 10 ? 14 : 12 }]}>
                  {entry.rank}
                </Text>
              </View>
              
              {/* Craft-like decorative elements */}
              <View style={styles.craftDecoration}>
                <View style={[styles.craftGem, { backgroundColor: '#4ECDC4', right: 2, top: 12 }]} />
              </View>
            </View>
          </View>
          
          <View style={[styles.tierBadge, { backgroundColor: tier.color + '20' }]}>
            <TierIcon size={12} color={tier.color} />
            <Text style={[styles.tierText, { color: tier.color }]}>
              {tier.name}
            </Text>
          </View>
          
          <View style={styles.userDetails}>
            <View style={styles.userNameRow}>
              <Text style={styles.userName} numberOfLines={1}>
                {entry.username}
              </Text>
            </View>
            
            <View style={styles.userStats}>
              <Text style={styles.userStat}>
                Power: {entry.miningPower}
              </Text>
              {entry.currentStreak !== undefined && (
                <Text style={styles.userStat}>
                  Streak: {entry.currentStreak}
                </Text>
              )}
            </View>
          </View>
        </View>
        
        {/* Right section - Earnings */}
        <View style={styles.rankRight}>
          <Text style={styles.earningsAmount}>
            {formatNumber(entry.totalCoins)}
          </Text>
          <Text style={styles.earningsLabel}>EKH</Text>
        </View>
      </View>
    </LinearGradient>
  );
};

// Memoize the entire component
export default memo(MemoizedLeaderboardEntry, (prevProps, nextProps) => {
  // Custom comparison function to prevent unnecessary re-renders
  return (
    prevProps.entry.rank === nextProps.entry.rank &&
    prevProps.entry.username === nextProps.entry.username &&
    prevProps.entry.totalCoins === nextProps.entry.totalCoins &&
    prevProps.entry.miningPower === nextProps.entry.miningPower &&
    prevProps.entry.currentStreak === nextProps.entry.currentStreak &&
    prevProps.index === nextProps.index
  );
});

const styles = StyleSheet.create({
  rankCard: {
    borderRadius: 16,
    marginBottom: 12,
    borderWidth: 1,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  topRankCard: {
    borderWidth: 2,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 6,
  },
  rankCardContent: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 16,
  },
  rankLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  rankBadgeContainer: {
    marginRight: 12,
  },
  craftBadge: {
    width: 40,
    height: 40,
    borderRadius: 12,
    borderWidth: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  regularBadge: {
    borderColor: 'rgba(255, 255, 255, 0.2)',
    backgroundColor: 'rgba(0, 0, 0, 0.3)',
  },
  craftBadgeInner: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    justifyContent: 'center',
    alignItems: 'center',
  },
  rankText: {
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  craftDecoration: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
  },
  craftGem: {
    position: 'absolute',
    width: 6,
    height: 6,
    borderRadius: 3,
  },
  tierBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 12,
    marginRight: 12,
  },
  tierText: {
    fontSize: 10,
    fontWeight: '600',
    marginLeft: 4,
  },
  userDetails: {
    flex: 1,
  },
  userNameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 4,
  },
  userName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
    maxWidth: 120,
  },
  userStats: {
    flexDirection: 'row',
    gap: 8,
  },
  userStat: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  rankRight: {
    alignItems: 'flex-end',
  },
  earningsAmount: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#FFA000',
  },
  earningsLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.7)',
  },
});