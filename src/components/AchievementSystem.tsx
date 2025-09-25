import { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Alert } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { 
  Trophy, 
  Star, 
  Crown, 
  Target, 
  Coins, 
  Users, 
  Flame,
  Medal,
  Gift,
  Lock,
  Sparkles
} from 'lucide-react-native';
import { UserProfile, Achievement } from '@/types';
import { databases, appwriteConfig } from '@/config/appwrite';
import { ID, Query } from 'appwrite';
import LoadingDots from '@/components/LoadingDots';

const API_BASE_URL = 'https://ekehi-network-api.your-subdomain.workers.dev';

interface AchievementSystemProps {
  profile: UserProfile;
  onRefresh?: () => void;
}

const rarityConfig = {
  common: {
    colors: ['#6b7280', '#4b5563'],
    textColor: '#9ca3af',
  },
  rare: {
    colors: ['#3b82f6', '#2563eb'],
    textColor: '#60a5fa',
  },
  epic: {
    colors: ['#8b5cf6', '#7c3aed'],
    textColor: '#a78bfa',
  },
  legendary: {
    colors: ['#ffa000', '#ff8f00'],
    textColor: '#ffa000',
  }
};

export default function AchievementSystem({ profile, onRefresh }: AchievementSystemProps) {
  const [claiming, setClaiming] = useState<string | null>(null);
  const [achievements, setAchievements] = useState<Achievement[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    fetchAchievements();
  }, [profile]);

  const fetchAchievements = async () => {
    try {
      // Fetch all achievements from Appwrite database
      const achievementsResponse = await databases.listDocuments(
        appwriteConfig.databaseId,
        appwriteConfig.collections.achievements
      );

      // Fetch user's claimed achievements
      if (profile) {
        const userAchievementsResponse = await databases.listDocuments(
          appwriteConfig.databaseId,
          appwriteConfig.collections.userAchievements,
          [Query.equal('userId', [profile.userId])]
        );

        // Create a set of claimed achievement IDs
        const claimedAchievementIds = new Set(
          userAchievementsResponse.documents.map(doc => doc.achievementId)
        );

        // Map achievements with unlock status based on user profile
        const achievementsWithStatus = achievementsResponse.documents.map(ach => {
          let isUnlocked = false;
          let progress = 0;
          
          // Determine if achievement is unlocked based on type and user stats
          switch (ach.type) {
            case 'coins':
              progress = profile.totalCoins;
              isUnlocked = profile.totalCoins >= ach.target;
              break;
            case 'streak':
              progress = profile.currentStreak;
              isUnlocked = profile.currentStreak >= ach.target;
              break;
            case 'referrals':
              progress = profile.totalReferrals;
              isUnlocked = profile.totalReferrals >= ach.target;
              break;
            default:
              isUnlocked = false;
          }
          
          return {
            id: ach.$id,
            achievementId: ach.achievementId,
            title: ach.title,
            description: ach.description,
            type: ach.type,
            target: ach.target,
            reward: ach.reward,
            rarity: ach.rarity,
            isActive: ach.isActive,
            isUnlocked: isUnlocked,
            isClaimed: claimedAchievementIds.has(ach.$id),
            progress: progress
          };
        });

        setAchievements(achievementsWithStatus);
      }
    } catch (error) {
      console.error('Failed to fetch achievements:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleClaim = async (achievementId: string) => {
    if (claiming) return;

    setClaiming(achievementId);
    try {
      if (!profile) return;
      
      // Find the achievement in our list
      const achievement = achievements.find(a => a.id === achievementId);
      if (!achievement) {
        throw new Error('Achievement not found');
      }
      
      // Create user achievement record
      await databases.createDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userAchievements,
        ID.unique(),
        {
          userId: profile.userId,
          achievementId: achievementId,
          claimedAt: new Date().toISOString()
        }
      );

      // Update user profile with reward
      const updatedProfile = {
        ...profile,
        totalCoins: profile.totalCoins + achievement.reward,
        lifetimeEarnings: profile.lifetimeEarnings + achievement.reward,
        todayEarnings: profile.todayEarnings + achievement.reward,
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

      Alert.alert('Success!', `You earned ${achievement.reward} EKH tokens!`);
      
      // Update achievement as claimed
      setAchievements(prev => 
        prev.map(ach => 
          ach.id === achievementId 
            ? { ...ach, isClaimed: true }
            : ach
        )
      );
      
      // Refresh profile to update coins
      onRefresh?.();
    } catch (error) {
      console.error('Claim failed:', error);
      Alert.alert('Error', 'Failed to claim achievement. Please try again.');
    } finally {
      setClaiming(null);
    }
  };

  const getIcon = (type: string) => {
    switch (type) {
      case 'coins':
        return Coins;
      case 'streak':
        return Flame;
      case 'referrals':
        return Users;
      case 'social':
        return Star;
      default:
        return Trophy;
    }
  };

  const getRarityConfig = (rarity: string) => {
    return rarityConfig[rarity as keyof typeof rarityConfig] || rarityConfig.common;
  };

  const unlockedCount = achievements.filter(a => a.isUnlocked).length;
  const totalRewards = achievements
    .filter(a => a.isUnlocked && a.isClaimed)
    .reduce((sum, a) => sum + a.reward, 0);

  if (isLoading) {
    return (
      <View style={styles.loadingContainer}>
        <Text style={styles.loadingText}>Loading achievements...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.titleContainer}>
          <Trophy size={32} color="#ffa000" />
          <Text style={styles.title}>Achievements</Text>
          <Star size={20} color="#ffa000" />
        </View>
        <Text style={styles.subtitle}>Unlock rewards and show your mining prowess</Text>
      </View>

      {/* Stats Overview */}
      <View style={styles.statsContainer}>
        <View style={styles.statCard}>
          <Target size={24} color="#ffa000" />
          <Text style={styles.statValue}>{unlockedCount}/{achievements.length}</Text>
          <Text style={styles.statLabel}>Unlocked</Text>
        </View>

        <View style={styles.statCard}>
          <Gift size={24} color="#10b981" />
          <Text style={styles.statValue}>{totalRewards.toLocaleString()}</Text>
          <Text style={styles.statLabel}>Rewards</Text>
        </View>
      </View>

      {/* Achievement List */}
      <ScrollView style={styles.achievementsList} showsVerticalScrollIndicator={false}>
        {achievements.map((achievement) => {
          const Icon = getIcon(achievement.type);
          const rarity = getRarityConfig(achievement.rarity);
          const isClaiming = claiming === achievement.id;
          const canClaim = achievement.isUnlocked && !achievement.isClaimed;

          return (
            <View key={achievement.id} style={styles.achievementCard}>
              <LinearGradient
                colors={achievement.isUnlocked ? rarity.colors : ['rgba(75, 85, 99, 0.5)', 'rgba(55, 65, 81, 0.5)']}
                style={styles.achievementGradient}
              >
                {/* Achievement Icon */}
                <View style={styles.achievementHeader}>
                  <View style={[
                    styles.iconContainer,
                    { backgroundColor: achievement.isUnlocked ? rarity.textColor : '#6b7280' }
                  ]}>
                    {achievement.isUnlocked ? (
                      <Icon size={28} color="#ffffff" />
                    ) : (
                      <Lock size={28} color="#ffffff" />
                    )}
                  </View>

                  {/* Achievement Info */}
                  <View style={styles.achievementInfo}>
                    <View style={styles.titleRow}>
                      <Text style={[
                        styles.achievementTitle,
                        { color: achievement.isUnlocked ? rarity.textColor : '#9ca3af' }
                      ]}>
                        {achievement.title}
                      </Text>
                      <View style={[
                        styles.rarityBadge,
                        { borderColor: achievement.isUnlocked ? rarity.textColor : '#6b7280' }
                      ]}>
                        <Text style={[
                          styles.rarityText,
                          { color: achievement.isUnlocked ? rarity.textColor : '#9ca3af' }
                        ]}>
                          {achievement.rarity.toUpperCase()}
                        </Text>
                      </View>
                    </View>
                    
                    <Text style={styles.achievementDescription}>{achievement.description}</Text>
                    
                    {/* Progress */}
                    <View style={styles.progressContainer}>
                      <View style={styles.progressBar}>
                        <View 
                          style={[
                            styles.progressFill,
                            { 
                              width: `${Math.min(100, (achievement.progress || 0) / achievement.target * 100)}%`,
                              backgroundColor: achievement.isUnlocked ? rarity.textColor : '#6b7280'
                            }
                          ]}
                        />
                      </View>
                      <Text style={styles.progressText}>
                        {achievement.progress || 0} / {achievement.target}
                      </Text>
                    </View>

                    {/* Reward */}
                    <View style={styles.rewardContainer}>
                      <Gift size={16} color="#10b981" />
                      <Text style={styles.rewardText}>+{achievement.reward.toLocaleString()} EKH</Text>
                    </View>
                  </View>
                </View>

                {/* Claim Button */}
                <View style={styles.actionContainer}>
                  {achievement.isClaimed ? (
                    <View style={styles.claimedButton}>
                      <Text style={styles.claimedText}>Claimed</Text>
                    </View>
                  ) : canClaim ? (
                    <TouchableOpacity
                      style={styles.claimButton}
                      onPress={() => handleClaim(achievement.id)}
                      disabled={isClaiming}
                    >
                      <LinearGradient
                        colors={isClaiming ? ['#6b7280', '#4b5563'] : rarity.colors}
                        style={styles.claimButtonGradient}
                      >
                        <Gift size={16} color="#ffffff" />
                        {isClaiming ? (
                          <LoadingDots color="#ffffff" size={8} />
                        ) : (
                          <Text style={styles.claimButtonText}>
                            Claim
                          </Text>
                        )}
                      </LinearGradient>
                    </TouchableOpacity>
                  ) : (
                    <View style={styles.lockedButton}>
                      <Lock size={16} color="#6b7280" />
                      <Text style={styles.lockedText}>Locked</Text>
                    </View>
                  )}
                </View>
              </LinearGradient>

              {/* Legendary sparkle effect */}
              {achievement.isUnlocked && achievement.rarity === 'legendary' && (
                <View style={styles.sparkleContainer}>
                  {[...Array(3)].map((_, i) => (
                    <View key={i} style={[styles.sparkle, { 
                      left: 20 + i * 60,
                      top: 20 + (i % 2) * 40,
                    }]}>
                      <Sparkles size={8} color="#ffa000" />
                    </View>
                  ))}
                </View>
              )}
            </View>
          );
        })}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    color: '#ffffff',
    fontSize: 16,
  },
  header: {
    alignItems: 'center',
    marginBottom: 24,
  },
  titleContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 8,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  subtitle: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
  },
  statsContainer: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 24,
  },
  statCard: {
    flex: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 16,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  statValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
    marginTop: 8,
  },
  statLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 4,
  },
  achievementsList: {
    flex: 1,
  },
  achievementCard: {
    marginBottom: 16,
    borderRadius: 16,
    overflow: 'hidden',
    position: 'relative',
  },
  achievementGradient: {
    padding: 16,
  },
  achievementHeader: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginBottom: 16,
  },
  iconContainer: {
    width: 56,
    height: 56,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  achievementInfo: {
    flex: 1,
  },
  titleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 4,
  },
  achievementTitle: {
    fontSize: 16,
    fontWeight: '600',
    flex: 1,
  },
  rarityBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    borderWidth: 1,
  },
  rarityText: {
    fontSize: 10,
    fontWeight: 'bold',
  },
  achievementDescription: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    marginBottom: 12,
  },
  progressContainer: {
    marginBottom: 8,
  },
  progressBar: {
    height: 6,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 3,
    overflow: 'hidden',
    marginBottom: 4,
  },
  progressFill: {
    height: '100%',
    borderRadius: 3,
  },
  progressText: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    textAlign: 'right',
  },
  rewardContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  rewardText: {
    fontSize: 14,
    color: '#10b981',
    fontWeight: '600',
  },
  actionContainer: {
    alignItems: 'flex-end',
  },
  claimButton: {
    borderRadius: 12,
    overflow: 'hidden',
  },
  claimButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 10,
    gap: 6,
  },
  claimButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#ffffff',
  },
  claimedButton: {
    backgroundColor: 'rgba(16, 185, 129, 0.2)',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderWidth: 1,
    borderColor: 'rgba(16, 185, 129, 0.3)',
  },
  claimedText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#10b981',
  },
  lockedButton: {
    backgroundColor: 'rgba(107, 114, 128, 0.2)',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 10,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  lockedText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#6b7280',
  },
  sparkleContainer: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    pointerEvents: 'none',
  },
  sparkle: {
    position: 'absolute',
  },
});
