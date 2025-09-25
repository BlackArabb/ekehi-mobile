import { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, RefreshControl, TouchableOpacity } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { useAuth } from '@/contexts/AuthContext';
import { databases, appwriteConfig } from '@/config/appwrite';
import { Query } from 'appwrite';
import { LeaderboardEntry } from '@/types';
import { Trophy, Crown, Medal, Award, RefreshCw, Star, Zap, Flame } from 'lucide-react-native';
import JellyTriangleLoader from '@/components/JellyTriangleLoader';

export default function LeaderboardPage() {
  const router = useRouter();
  const { user } = useAuth();
  const insets = useSafeAreaInsets();
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    if (!user) {
      setTimeout(() => {
        router.replace('/');
      }, 100);
      return;
    }
    fetchLeaderboard();
  }, [user]);

  const fetchLeaderboard = async () => {
    try {
      const response = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        [
          Query.orderDesc('totalCoins'),
          Query.limit(50)
        ]
      );

      const leaderboardEntries = response.documents.map((doc: any, index: number) => ({
        rank: index + 1,
        username: doc.username || `user_${doc.userId.substring(0, 8)}`,
        totalCoins: doc.totalCoins,
        miningPower: doc.miningPower,
        currentStreak: doc.currentStreak,
        totalReferrals: doc.totalReferrals
      }));

      setLeaderboard(leaderboardEntries);
    } catch (error) {
      console.error('Failed to fetch leaderboard:', error);
    } finally {
      setIsLoading(false);
      setRefreshing(false);
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await fetchLeaderboard();
  };

  const getCraftRankBadge = (rank: number) => {
    const badgeProps = {
      size: rank === 1 ? 32 : rank <= 3 ? 28 : 24,
      style: [
        styles.craftBadge,
        rank === 1 && styles.championBadge,
        rank === 2 && styles.silverBadge,
        rank === 3 && styles.bronzeBadge,
        rank > 3 && styles.regularBadge
      ]
    };

    return (
      <LinearGradient
        colors={
          rank === 1 ? ['#FFD700', '#FFA500', '#FF8C00'] :
          rank === 2 ? ['#C0C0C0', '#A8A8A8', '#808080'] :
          rank === 3 ? ['#CD7F32', '#B87333', '#8B4513'] :
          ['#4A5568', '#2D3748', '#1A202C']
        }
        style={badgeProps.style}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
      >
        <View style={styles.craftBadgeInner}>
          {rank === 1 && <Crown size={20} color="#FFFFFF" />}
          {rank === 2 && <Medal size={18} color="#FFFFFF" />}
          {rank === 3 && <Award size={18} color="#FFFFFF" />}
          {rank > 3 && (
            <Text style={[styles.rankText, { fontSize: rank <= 10 ? 14 : 12 }]}>
              {rank}
            </Text>
          )}
        </View>
        
        {/* Craft-like decorative elements */}
        <View style={styles.craftDecoration}>
          {rank <= 3 && (
            <>
              <View style={[styles.craftGem, { backgroundColor: rank === 1 ? '#FF6B6B' : rank === 2 ? '#4ECDC4' : '#45B7D1' }]} />
              <View style={[styles.craftGem, { backgroundColor: rank === 1 ? '#4ECDC4' : rank === 2 ? '#45B7D1' : '#FF6B6B', right: 2, top: 12 }]} />
            </>
          )}
        </View>
      </LinearGradient>
    );
  };

  const getTierIndicator = (rank: number) => {
    if (rank === 1) return { name: 'LEGENDARY', color: '#FFD700', icon: Crown };
    if (rank <= 3) return { name: 'ELITE', color: '#C0C0C0', icon: Star };
    if (rank <= 10) return { name: 'MASTER', color: '#CD7F32', icon: Flame };
    if (rank <= 25) return { name: 'EXPERT', color: '#4A90E2', icon: Zap };
    return { name: 'MINER', color: '#6B7280', icon: Trophy };
  };

  if (isLoading) {
    return (
      <LinearGradient colors={['#0F172A', '#1E293B', '#334155']} style={styles.container}>
        <View style={styles.loadingContainer}>
          <JellyTriangleLoader size={40} color="#ffa000" speed={1750} />
        </View>
      </LinearGradient>
    );
  }

  return (
    <LinearGradient colors={['#0F172A', '#1E293B', '#334155']} style={styles.container}>
      <ScrollView 
        style={[styles.scrollView, { paddingTop: insets.top }]}
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor="#ffa000"
            colors={['#ffa000']}
          />
        }
      >
        {/* Professional Header */}
        <View style={styles.header}>
          <LinearGradient
            colors={['rgba(255, 160, 0, 0.15)', 'rgba(255, 160, 0, 0.05)']}
            style={styles.headerGradient}
          >
            <View style={styles.headerContent}>
              <View style={styles.headerTitle}>
                <Trophy size={28} color="#FFA000" />
                <Text style={styles.title}>LEADERBOARD</Text>
                <TouchableOpacity style={styles.refreshButton} onPress={onRefresh}>
                  <RefreshCw size={20} color="#ffffff" />
                </TouchableOpacity>
              </View>
              <Text style={styles.subtitle}>Elite miners of the Ekehi Network</Text>
            </View>
          </LinearGradient>
        </View>

        {/* Champion's Throne - Top 3 */}
        {leaderboard.length >= 3 && (
          <View style={styles.throneContainer}>
            <LinearGradient
              colors={['rgba(255, 215, 0, 0.1)', 'rgba(255, 215, 0, 0.05)']}
              style={styles.throneGradient}
            >
              <Text style={styles.throneTitle}>HALL OF LEGENDS</Text>
              
              <View style={styles.podiumArrangement}>
                {/* Second Place */}
                <View style={[styles.podiumSlot, styles.secondSlot]}>
                  <View style={styles.podiumPlatform}>
                    <Text style={styles.podiumNumber}>2</Text>
                  </View>
                  {getCraftRankBadge(2)}
                  <Text style={styles.championName} numberOfLines={1}>
                    {leaderboard[1]?.username}
                  </Text>
                  <Text style={styles.championCoins}>
                    {leaderboard[1]?.totalCoins.toLocaleString()}
                  </Text>
                  <Text style={styles.championLabel}>EKH</Text>
                </View>

                {/* First Place - Champion */}
                <View style={[styles.podiumSlot, styles.championSlot]}>
                  <View style={styles.crownContainer}>
                    <Crown size={24} color="#FFD700" />
                  </View>
                  <View style={styles.podiumPlatform}>
                    <Text style={styles.podiumNumber}>1</Text>
                  </View>
                  {getCraftRankBadge(1)}
                  <Text style={[styles.championName, styles.championNameGold]} numberOfLines={1}>
                    {leaderboard[0]?.username}
                  </Text>
                  <Text style={[styles.championCoins, styles.championCoinsGold]}>
                    {leaderboard[0]?.totalCoins.toLocaleString()}
                  </Text>
                  <Text style={styles.championLabel}>EKH</Text>
                  <Text style={styles.championTitle}>CHAMPION</Text>
                </View>

                {/* Third Place */}
                <View style={[styles.podiumSlot, styles.thirdSlot]}>
                  <View style={styles.podiumPlatform}>
                    <Text style={styles.podiumNumber}>3</Text>
                  </View>
                  {getCraftRankBadge(3)}
                  <Text style={styles.championName} numberOfLines={1}>
                    {leaderboard[2]?.username}
                  </Text>
                  <Text style={styles.championCoins}>
                    {leaderboard[2]?.totalCoins.toLocaleString()}
                  </Text>
                  <Text style={styles.championLabel}>EKH</Text>
                </View>
              </View>
            </LinearGradient>
          </View>
        )}

        {/* Professional Rankings Grid */}
        <View style={styles.rankingsSection}>
          <LinearGradient
            colors={['rgba(255, 255, 255, 0.05)', 'rgba(255, 255, 255, 0.02)']}
            style={styles.rankingsSectionGradient}
          >
            <Text style={styles.rankingsHeader}>GLOBAL RANKINGS</Text>
            
            {leaderboard.length === 0 ? (
              <View style={styles.emptyState}>
                <Trophy size={64} color="rgba(255, 255, 255, 0.3)" />
                <Text style={styles.emptyTitle}>No Rankings Available</Text>
                <Text style={styles.emptyText}>Start mining to claim your position</Text>
              </View>
            ) : (
              <View style={styles.rankingsGrid}>
                {leaderboard.map((entry, index) => {
                  const tier = getTierIndicator(entry.rank);
                  const TierIcon = tier.icon;
                  
                  return (
                    <LinearGradient
                      key={index}
                      colors={
                        entry.rank === 1 ? ['rgba(255, 215, 0, 0.15)', 'rgba(255, 215, 0, 0.05)'] :
                        entry.rank <= 3 ? ['rgba(192, 192, 192, 0.15)', 'rgba(192, 192, 192, 0.05)'] :
                        entry.rank <= 10 ? ['rgba(205, 127, 50, 0.15)', 'rgba(205, 127, 50, 0.05)'] :
                        ['rgba(255, 255, 255, 0.08)', 'rgba(255, 255, 255, 0.03)']
                      }
                      style={[
                        styles.rankCard,
                        entry.rank <= 3 && styles.topRankCard
                      ]}
                    >
                      <View style={styles.rankCardContent}>
                        {/* Left section - Rank & User */}
                        <View style={styles.rankLeft}>
                          <View style={styles.rankBadgeContainer}>
                            {getCraftRankBadge(entry.rank)}
                          </View>
                          
                          <View style={styles.userDetails}>
                            <View style={styles.userNameRow}>
                              <Text style={styles.userName} numberOfLines={1}>
                                {entry.username}
                              </Text>
                              <View style={[styles.tierBadge, { backgroundColor: tier.color + '20' }]}>
                                <TierIcon size={12} color={tier.color} />
                                <Text style={[styles.tierText, { color: tier.color }]}>
                                  {tier.name}
                                </Text>
                              </View>
                            </View>
                            
                            <View style={styles.userStats}>
                              <Text style={styles.userStat}>
                                Power: {entry.miningPower}
                              </Text>
                              <Text style={styles.userStat}>
                                Streak: {entry.currentStreak}
                              </Text>
                            </View>
                          </View>
                        </View>
                        
                        {/* Right section - Earnings */}
                        <View style={styles.rankRight}>
                          <Text style={styles.earningsAmount}>
                            {entry.totalCoins.toLocaleString()}
                          </Text>
                          <Text style={styles.earningsLabel}>EKH</Text>
                        </View>
                      </View>
                    </LinearGradient>
                  );
                })}
              </View>
            )}
          </LinearGradient>
        </View>
      </ScrollView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  content: {
    paddingHorizontal: 20,
    paddingBottom: 100,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },

  // Professional Header
  header: {
    marginBottom: 24,
    marginTop: 20,
  },
  headerGradient: {
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
    shadowColor: '#FFA000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 8,
  },
  headerContent: {
    alignItems: 'center',
  },
  headerTitle: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginBottom: 8,
  },
  title: {
    fontSize: 24,
    fontWeight: '800',
    color: '#FFFFFF',
    letterSpacing: 2,
  },
  refreshButton: {
    padding: 8,
    borderRadius: 12,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  subtitle: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    fontWeight: '500',
  },

  // Champion's Throne
  throneContainer: {
    marginBottom: 32,
  },
  throneGradient: {
    borderRadius: 24,
    padding: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 215, 0, 0.3)',
    shadowColor: '#FFD700',
    shadowOffset: { width: 0, height: 6 },
    shadowOpacity: 0.3,
    shadowRadius: 16,
    elevation: 12,
  },
  throneTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#FFD700',
    textAlign: 'center',
    letterSpacing: 1.5,
    marginBottom: 24,
  },
  podiumArrangement: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    justifyContent: 'center',
    gap: 8,
  },
  podiumSlot: {
    alignItems: 'center',
    flex: 1,
  },
  championSlot: {
    transform: [{ scale: 1.1 }],
    zIndex: 3,
  },
  secondSlot: {
    transform: [{ scale: 0.95 }],
    zIndex: 2,
  },
  thirdSlot: {
    transform: [{ scale: 0.95 }],
    zIndex: 2,
  },
  crownContainer: {
    position: 'absolute',
    top: -20,
    zIndex: 4,
  },
  podiumPlatform: {
    width: 40,
    height: 50,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderTopLeftRadius: 8,
    borderTopRightRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  podiumNumber: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
  championName: {
    fontSize: 14,
    fontWeight: '600',
    color: '#FFFFFF',
    marginTop: 8,
    marginBottom: 4,
    textAlign: 'center',
  },
  championNameGold: {
    color: '#FFD700',
    fontSize: 16,
  },
  championCoins: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFA000',
    textAlign: 'center',
  },
  championCoinsGold: {
    fontSize: 18,
    color: '#FFD700',
  },
  championLabel: {
    fontSize: 10,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
  },
  championTitle: {
    fontSize: 10,
    fontWeight: '600',
    color: '#FFD700',
    marginTop: 4,
    textAlign: 'center',
  },

  // Craft-like Rank Badges
  craftBadge: {
    width: 50,
    height: 50,
    borderRadius: 25,
    justifyContent: 'center',
    alignItems: 'center',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.4,
    shadowRadius: 8,
    elevation: 6,
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.3)',
  },
  championBadge: {
    width: 60,
    height: 60,
    borderRadius: 30,
    borderColor: 'rgba(255, 215, 0, 0.5)',
    shadowColor: '#FFD700',
  },
  silverBadge: {
    borderColor: 'rgba(192, 192, 192, 0.5)',
    shadowColor: '#C0C0C0',
  },
  bronzeBadge: {
    borderColor: 'rgba(205, 127, 50, 0.5)',
    shadowColor: '#CD7F32',
  },
  regularBadge: {
    width: 45,
    height: 45,
    borderRadius: 22.5,
    borderColor: 'rgba(74, 85, 104, 0.5)',
    shadowColor: '#4A5568',
  },
  craftBadgeInner: {
    justifyContent: 'center',
    alignItems: 'center',
    width: '100%',
    height: '100%',
  },
  craftDecoration: {
    position: 'absolute',
    width: '100%',
    height: '100%',
  },
  craftGem: {
    position: 'absolute',
    width: 8,
    height: 8,
    borderRadius: 4,
    top: 8,
    right: 6,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 4,
    elevation: 4,
  },
  rankText: {
    fontWeight: '800',
    color: '#FFFFFF',
  },

  // Rankings Section
  rankingsSection: {
    flex: 1,
  },
  rankingsSectionGradient: {
    borderRadius: 20,
    padding: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
  },
  rankingsHeader: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
    textAlign: 'center',
    marginBottom: 20,
    letterSpacing: 1,
  },
  rankingsGrid: {
    gap: 12,
  },
  rankCard: {
    borderRadius: 16,
    padding: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  topRankCard: {
    borderColor: 'rgba(255, 215, 0, 0.3)',
    shadowColor: '#FFD700',
    shadowOpacity: 0.2,
  },
  rankCardContent: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  rankLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
    gap: 12,
  },
  rankBadgeContainer: {
    alignItems: 'center',
  },
  userDetails: {
    flex: 1,
  },
  userNameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 4,
  },
  userName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
    flex: 1,
  },
  tierBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 8,
    gap: 4,
  },
  tierText: {
    fontSize: 10,
    fontWeight: '600',
  },
  userStats: {
    flexDirection: 'row',
    gap: 12,
  },
  userStat: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    fontWeight: '500',
  },
  rankRight: {
    alignItems: 'flex-end',
  },
  earningsAmount: {
    fontSize: 18,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  earningsLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.7)',
    fontWeight: '600',
  },

  // Empty State
  emptyState: {
    alignItems: 'center',
    paddingVertical: 48,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '600',
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 16,
    marginBottom: 8,
  },
  emptyText: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.4)',
    textAlign: 'center',
  },
});