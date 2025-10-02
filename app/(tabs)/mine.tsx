import { useEffect, useState, useRef, useCallback } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Modal, Animated, Easing } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import * as Haptics from 'expo-haptics';
import { Pickaxe, Coins, Flame, Users, TrendingUp, Zap, Trophy, Play, X, Share2, Store, Wallet, User } from 'lucide-react-native';
import { useMining } from '@/contexts/MiningContext';
import { useAuth } from '@/contexts/AuthContext';
import { useReferral } from '@/contexts/ReferralContext';
import { useRouter } from 'expo-router';
import { useNotifications } from '@/contexts/NotificationContext';
import AutoMiningStatus from '@/components/AutoMiningStatus';
import AchievementSystem from '@/components/AchievementSystem';
import AdModal from '@/components/AdModal';
import NotificationSystem from '@/components/NotificationSystem';
import * as Clipboard from 'expo-clipboard';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { databases, appwriteConfig } from '@/config/appwrite';
import { ID } from 'appwrite';
import PulseLoader from '@/components/PulseLoader';
import CircularProgressBar from '@/components/CircularProgressBar';

export default function MinePage() {
    const router = useRouter();
    const { user, isLoading: authLoading } = useAuth();
    const { profile, sessionCoins, sessionClicks, refreshProfile, startMiningSession, addCoins, endMiningSession } = useMining();
    const { referralCode, generateReferralLink } = useReferral();
    const insets = useSafeAreaInsets();
    const [showAchievements, setShowAchievements] = useState(false);
    const [is24HourMiningActive, setIs24HourMiningActive] = useState(false);
    const [miningStartTime, setMiningStartTime] = useState<number | null>(null);
    const [remainingTime, setRemainingTime] = useState(24 * 60 * 60); // 24 hours in seconds
    const [sessionReward, setSessionReward] = useState(2); // 2 EKH total for the 24-hour session
    const [finalRewardClaimed, setFinalRewardClaimed] = useState(false); // Track if final reward has been claimed
    const [showAdModal, setShowAdModal] = useState(false); // For ad bonus modal
    const [adCooldown, setAdCooldown] = useState(0); // Cooldown timer for ad watching
    const [clickEffects, setClickEffects] = useState<Array<{ id: number; x: number; y: number }>>([]); // For click effects animation
    const [copied, setCopied] = useState(false); // For referral link copy status

    const { showNotification } = useNotifications();

    const miningIntervalRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const timeIntervalRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const adCooldownRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        // Handle authentication state changes
        if (!user && !authLoading) {
            // If user is not authenticated and not loading, redirect to index
            console.log('User not authenticated in mining screen, redirecting to index');
            // Use a timeout to ensure the router is ready
            setTimeout(() => {
                try {
                    router.replace('/');
                } catch (error) {
                    console.error('Navigation error:', error);
                    // Fallback navigation
                    try {
                        router.push('/');
                    } catch (fallbackError) {
                        console.error('Fallback navigation error:', fallbackError);
                    }
                }
            }, 100);
            return;
        }

        if (user && !authLoading) {
            startMiningSession();

            // Check if there's an ongoing 24-hour mining session
            checkOngoingMiningSession();

            // Check for existing ad cooldown
            checkAdCooldown();
        }

        // Cleanup intervals on unmount
        return () => {
            if (miningIntervalRef.current) {
                clearInterval(miningIntervalRef.current);
            }
            if (timeIntervalRef.current) {
                clearInterval(timeIntervalRef.current);
            }
            if (adCooldownRef.current) {
                clearInterval(adCooldownRef.current);
            }
            // End mining session when component unmounts
            endMiningSession();
        };
    }, [user, authLoading]);

    const checkOngoingMiningSession = async () => {
        try {
            const savedSession = await AsyncStorage.getItem('miningSession');
            if (savedSession) {
                const sessionData = JSON.parse(savedSession);
                const now = Date.now();
                const elapsed = Math.floor((now - sessionData.startTime) / 1000);
                const remaining = sessionData.duration - elapsed;

                if (remaining > 0) {
                    setIs24HourMiningActive(true);
                    setMiningStartTime(sessionData.startTime);
                    setRemainingTime(remaining);
                    setSessionReward(sessionData.reward || 2);
                    setFinalRewardClaimed(sessionData.finalRewardClaimed || false);

                    // Start the mining process
                    start24HourMining();
                } else {
                    // Session expired, but check if final reward was claimed
                    if (!sessionData.finalRewardClaimed) {
                        // Claim the final reward
                        await claimFinalReward(sessionData.reward || 2);
                    }
                    // Clear the session
                    await AsyncStorage.removeItem('miningSession');
                }
            }
        } catch (error) {
            // Silently handle mining session check errors
        }
    };

    const checkAdCooldown = async () => {
        try {
            const lastAdTime = await AsyncStorage.getItem('lastAdWatchTime');
            if (lastAdTime) {
                const elapsed = Math.floor((Date.now() - parseInt(lastAdTime)) / 1000);
                const remainingCooldown = Math.max(0, 300 - elapsed); // 5 minutes cooldown
                setAdCooldown(remainingCooldown);

                if (remainingCooldown > 0) {
                    startAdCooldown(remainingCooldown);
                }
            }
        } catch (error) {
            // Silently handle ad cooldown check errors
        }
    };

    const copyReferralLink = async () => {
        if (!referralCode) return;
        
        try {
            const referralLink = generateReferralLink();
            await Clipboard.setStringAsync(referralLink);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
            
            showNotification({
                type: 'success',
                title: 'Copied!',
                message: 'Referral link copied to clipboard',
                duration: 2000,
            });
        } catch (error) {
            showNotification({
                type: 'error',
                title: 'Copy Failed',
                message: 'Failed to copy referral link',
                duration: 2000,
            });
        }
    };

    const start24HourMining = () => {
        if (miningIntervalRef.current) {
            clearInterval(miningIntervalRef.current);
        }

        if (timeIntervalRef.current) {
            clearInterval(timeIntervalRef.current);
        }

        // Update remaining time every second
        timeIntervalRef.current = setInterval(() => {
            setRemainingTime(prev => {
                if (prev <= 1) {
                    // Mining session completed
                    complete24HourMining();
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
    };

    const complete24HourMining = async () => {
        // Clear intervals
        if (miningIntervalRef.current) {
            clearInterval(miningIntervalRef.current);
            miningIntervalRef.current = null;
        }

        if (timeIntervalRef.current) {
            clearInterval(timeIntervalRef.current);
            timeIntervalRef.current = null;
        }

        // Show notification that mining is complete and reward is ready to claim
        showNotification({
            type: 'success',
            title: 'Mining Session Complete!',
            message: 'Your 24-hour mining session has finished. Press the mining button to claim your 2 EKH reward.',
            duration: 5000,
        });
    };

    const claimFinalReward = async (reward: number) => {
        if (!profile) return;

        try {
            // Update user profile with the final reward
            const updatedProfile = {
                ...profile,
                totalCoins: profile.totalCoins + reward,
                todayEarnings: profile.todayEarnings + reward,
                updatedAt: new Date().toISOString()
            };

            // Update document in Appwrite database
            await databases.updateDocument(
                appwriteConfig.databaseId,
                appwriteConfig.collections.userProfiles,
                profile.id,
                {
                    totalCoins: updatedProfile.totalCoins,
                    todayEarnings: updatedProfile.todayEarnings,
                    updatedAt: updatedProfile.updatedAt
                }
            );

            // Update local state
            setFinalRewardClaimed(true);

            // Save session data with final reward claimed flag
            if (miningStartTime) {
                const sessionData = {
                    startTime: miningStartTime,
                    duration: 24 * 60 * 60, // 24 hours
                    reward: reward,
                    finalRewardClaimed: true
                };
                await AsyncStorage.setItem('miningSession', JSON.stringify(sessionData));
            }

            // Show notification
            showNotification({
                type: 'success',
                title: 'Reward Claimed!',
                message: `You earned ${reward} EKH tokens from your 24-hour mining session!`,
                duration: 5000,
            });

            // Refresh profile to get updated data
            refreshProfile();
        } catch (error) {
            showNotification({
                type: 'error',
                title: 'Claim Failed',
                message: 'Failed to claim your mining reward. Please try again.',
                duration: 3000,
            });
        }
    };

    const handleMine = async () => {
        // If 24-hour mining is already active and completed, claim the reward
        if (is24HourMiningActive && remainingTime <= 0 && !finalRewardClaimed) {
            await claimFinalReward(sessionReward);
            return;
        }

        // If 24-hour mining is already active and not completed, don't do anything
        if (is24HourMiningActive && remainingTime > 0) {
            // Provide haptic feedback for disabled button press
            Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
            return;
        }

        // Start a new 24-hour mining session with a single press
        const startTime = Date.now();
        setMiningStartTime(startTime);
        setIs24HourMiningActive(true);
        setSessionReward(2);
        setFinalRewardClaimed(false);

        // Save session data
        const sessionData = {
            startTime,
            duration: 24 * 60 * 60, // 24 hours in seconds
            reward: 2,
            finalRewardClaimed: false
        };
        await AsyncStorage.setItem('miningSession', JSON.stringify(sessionData));

        // Start the mining process
        start24HourMining();

        // Show notification
        showNotification({
            type: 'success',
            title: 'Mining Session Started!',
            message: 'Your 24-hour mining session has begun. You will earn 2 EKH when it completes.',
            duration: 5000,
        });
    };

    const formatTime = (seconds: number) => {
        const hrs = Math.floor(seconds / 3600);
        const mins = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;
        return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    // Ad bonus functions
    const startAdCooldown = (seconds: number) => {
        setAdCooldown(seconds);
        if (adCooldownRef.current) {
            clearInterval(adCooldownRef.current);
        }

        adCooldownRef.current = setInterval(() => {
            setAdCooldown(prev => {
                if (prev <= 1) {
                    if (adCooldownRef.current) {
                        clearInterval(adCooldownRef.current);
                    }
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
    };

    const handleWatchAd = () => {
        setShowAdModal(true);
    };

    const handleAdComplete = async () => {
        // Use real AdMob service instead of simulation
        // This function is now handled by the AdModal component
        // We return a default success response here as a fallback
        return Promise.resolve({ success: true, reward: 0.5 });
    };

    const handleAdReward = async (result: { success: boolean; reward?: number; error?: string }) => {
        if (result.success && result.reward && profile) {
            try {
                // Add coins to user profile
                await addCoins(result.reward);

                // Record ad view in database
                await databases.createDocument(
                    appwriteConfig.databaseId,
                    appwriteConfig.collections.adViews,
                    ID.unique(),
                    {
                        userId: user?.id,
                        adType: 'bonus',
                        reward: result.reward,
                        createdAt: new Date().toISOString()
                    }
                );

                // Set cooldown
                const now = Date.now();
                await AsyncStorage.setItem('lastAdWatchTime', now.toString());
                startAdCooldown(300); // 5 minutes cooldown

                // Show notification
                showNotification({
                    type: 'success',
                    title: 'Bonus Earned!',
                    message: `You earned ${result.reward} EKH from watching the ad!`,
                    duration: 3000,
                });

                // Refresh profile to get updated data
                refreshProfile();

                return;
            } catch (error) {
                showNotification({
                    type: 'error',
                    title: 'Reward Failed',
                    message: 'Failed to process your ad reward. Please try again.',
                    duration: 3000,
                });
                return;
            }
        } else {
            showNotification({
                type: 'error',
                title: 'Ad Not Completed',
                message: result.error || 'Ad was not completed successfully.',
                duration: 3000,
            });
            return;
        }
    };

    const resetMiningSession = () => {
        // Clear intervals
        if (miningIntervalRef.current) {
            clearInterval(miningIntervalRef.current);
            miningIntervalRef.current = null;
        }

        if (timeIntervalRef.current) {
            clearInterval(timeIntervalRef.current);
            timeIntervalRef.current = null;
        }

        if (adCooldownRef.current) {
            clearInterval(adCooldownRef.current);
            adCooldownRef.current = null;
        }

        // Reset state
        setIs24HourMiningActive(false);
        setMiningStartTime(null);
        setRemainingTime(24 * 60 * 60);
        setSessionReward(2);
        setFinalRewardClaimed(false);

        // Clear AsyncStorage
        AsyncStorage.removeItem('miningSession').catch(() => {
            // Silently handle errors
        });
    };

    // Show loading state while checking authentication
    if (authLoading) {
        return (
            <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
                <View style={styles.loadingContainer}>
                    <PulseLoader />
                </View>
            </LinearGradient>
        );
    }

    // Show loading state while profile is loading
    if (!profile && user && !authLoading) {
        return (
            <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
                <View style={styles.loadingContainer}>
                    <PulseLoader />
                </View>
            </LinearGradient>
        );
    }

    // If user is not authenticated, show nothing (redirect will happen in useEffect)
    if (!user && !authLoading) {
        return null;
    }

    // If profile is not loaded yet, show nothing
    if (!profile) {
        return null;
    }

    const dailyProgress = (profile.todayEarnings / 2) * 100; // Based on 2 EKH total reward
    const progressPercentage = ((24 * 60 * 60 - remainingTime) / (24 * 60 * 60)) * 100;
    
    // Calculate mining rate per second to reach 2 EKH in 24 hours
    const miningRatePerSecond = 2 / (24 * 60 * 60); // 2 EKH / 86400 seconds
    const miningRatePerMinute = miningRatePerSecond * 60;
    const miningRatePerHour = miningRatePerSecond * 3600;

    return (
        <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
            <ScrollView
                style={[styles.scrollView, { paddingTop: insets.top }]}
                contentContainerStyle={styles.content}
                showsVerticalScrollIndicator={false}
            >
                {/* Professional User Card */}
                <View style={styles.userCard}>
                    <View style={styles.userCardGradient}>
                        <View style={styles.userCardContent}>
                            <View style={styles.userInfoContainer}>
                                <View style={styles.userAvatar}>
                                    <User size={20} color="#ffffff" />
                                </View>
                                <View style={styles.userInfo}>
                                    <Text style={styles.username} numberOfLines={1}>{profile.username || user?.name || 'User'}</Text>
                                    <Text style={styles.userScore}>{profile.totalCoins.toLocaleString()} EKH</Text>
                                </View>
                            </View>
                        </View>
                    </View>
                </View>

                {/* Stats Display */}
                <View style={styles.statsContainer}>
                    <View style={styles.statCard}>
                        <Flame size={20} color="#ef4444" />
                        <Text style={styles.statValue}>{profile.currentStreak}</Text>
                        <Text style={styles.statLabel}>Streak</Text>
                    </View>

                    <View style={styles.statCard}>
                        <Users size={20} color="#3b82f6" />
                        <Text style={styles.statValue}>{profile.totalReferrals}</Text>
                        <Text style={styles.statLabel}>Referrals</Text>
                    </View>

                    <View style={styles.statCard}>
                        <TrendingUp size={20} color="#10b981" />
                        <Text style={styles.statValue}>{(2 / 24).toFixed(4)}</Text>
                        <Text style={styles.statLabel}>EKH/hour</Text>
                    </View>
                </View>

               

                {/* Mining Button - Clean Implementation */}
                <View style={styles.miningContainer}>
                    <View style={styles.miningButtonWrapper}>
                        {/* Floating Energy Particles - keep existing */}
                        <View style={styles.particlesContainer} pointerEvents="none">
                            {/* Your existing particles code */}
                        </View>

                        {/* Circular Progress Bar */}
                        {is24HourMiningActive && remainingTime > 0 && (
                            <View style={styles.circularProgressContainer} pointerEvents="none">
                                <CircularProgressBar 
                                    size={180} 
                                    strokeWidth={8} 
                                    progress={progressPercentage}
                                    strokeColor="#ffa000"
                                    backgroundColor="rgba(255, 255, 255, 0.1)"
                                    showStars={true}
                                    pulsate={true}
                                />
                            </View>
                        )}

                        {/* Main Mining Button */}
                        <TouchableOpacity
                            style={styles.miningButton}
                            onPress={handleMine}
                            activeOpacity={0.8}
                            disabled={is24HourMiningActive && remainingTime > 0}
                        >
                            <LinearGradient
                                colors={
                                    is24HourMiningActive
                                        ? remainingTime > 0
                                            ? ['#10b981', '#059669']
                                            : ['#ffa000', '#ff8f00']
                                        : ['#ffa000', '#ff8f00', '#ff6f00']
                                }
                                style={styles.miningButtonGradient}
                            >
                                {is24HourMiningActive && remainingTime <= 0 ? (
                                    <Coins size={60} color="#ffffff" />
                                ) : (
                                    <Pickaxe size={60} color="#ffffff" />
                                )}
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>

                    <Text style={styles.miningRate}>
                        {is24HourMiningActive
                            ? remainingTime > 0
                                ? `Mining...`
                                : `Claim ${sessionReward} EKH Reward`
                            : `Start Extended Session (+${sessionReward} EKH)`}
                    </Text>
                </View>

                {/* Ad Bonus Button */}
                <View style={styles.adBonusContainer}>
                    <TouchableOpacity
                        style={styles.adBonusButton}
                        onPress={handleWatchAd}
                        disabled={adCooldown > 0}
                        activeOpacity={0.8}
                    >
                        <LinearGradient
                            colors={adCooldown > 0 ? ['#6b7280', '#4b5563'] : ['#8b5cf6', '#7c3aed']}
                            style={styles.adBonusButtonGradient}
                        >
                            <View style={styles.adButtonContent}>
                                <Play size={20} color="#ffffff" />
                                <Text style={styles.adBonusButtonText}>
                                    {adCooldown > 0 ? `Available in ${formatTime(adCooldown)}` : 'Watch Ad for +0.5 EKH'}
                                </Text>
                            </View>
                        </LinearGradient>
                    </TouchableOpacity>
                                    
                    <Text style={styles.adBonusDescription}>
                        Watch a short ad to earn bonus EKH tokens
                    </Text>
                </View>

                {/* Auto Mining Status */}
                {profile && (
                    <AutoMiningStatus profile={profile} onRefresh={refreshProfile} />
                )}

                {/* Referral Card */}
                <View style={styles.sessionContainer}>
                    <Text style={styles.sessionTitle}>Invite Friends</Text>
                    <Text style={styles.sessionStatLabel}>Share your referral link and earn rewards</Text>
                    <View style={styles.sessionStats}>
                        <View style={styles.sessionStat}>
                            <Text style={styles.sessionStatValue}>1.0 EKH</Text>
                            <Text style={styles.sessionStatLabel}>Per Referral</Text>
                        </View>
                        <View style={styles.sessionStat}>
                            <Text style={styles.sessionStatValue}>0.5 EKH</Text>
                            <Text style={styles.sessionStatLabel}>For You</Text>
                        </View>
                    </View>
                    <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginTop: 16 }}>
                        <Text style={[styles.sessionStatLabel, { flex: 1, marginRight: 8 }]} numberOfLines={1}>
                            {referralCode ? `ekehi://referral/${referralCode}` : 'Loading...'}</Text>
                        <TouchableOpacity 
                            style={{ backgroundColor: '#ffa000', padding: 8, borderRadius: 8 }}
                            onPress={copyReferralLink}
                            disabled={!referralCode}
                        >
                            <Share2 size={20} color="#ffffff" />
                        </TouchableOpacity>
                    </View>
                </View>

                {/* Action Buttons */}
                <View style={styles.actionButtonsContainer}>
                    <TouchableOpacity
                        style={styles.actionButton}
                        onPress={() => setShowAchievements(true)}
                    >
                        <LinearGradient
                            colors={['#8b5cf6', '#7c3aed']}
                            style={styles.actionButtonGradient}
                        >
                            <Trophy size={20} color="#ffffff" />
                            <Text style={styles.actionButtonText}>Achievements</Text>
                        </LinearGradient>
                    </TouchableOpacity>
                </View>

                {/* Quick Access - Glassmorphic Cards */}
                <View style={styles.quickAccessSection}>
                    <Text style={styles.quickAccessTitle}>Quick Access</Text>
                    <View style={styles.quickAccessContainer}>
                        <TouchableOpacity
                            style={styles.quickAccessCard}
                            onPress={() => router.push('/(tabs)/social')}
                        >
                            <LinearGradient
                                colors={['rgba(59, 130, 246, 0.2)', 'rgba(37, 99, 235, 0.3)']}
                                style={styles.quickAccessCardGradient}
                            >
                                <View style={styles.quickAccessRow}>
                                    <Share2 size={20} color="#3b82f6" />
                                    <Text style={styles.quickAccessCardLabel}>Social</Text>
                                </View>
                            </LinearGradient>
                        </TouchableOpacity>

                        <TouchableOpacity
                            style={styles.quickAccessCard}
                            onPress={() => router.push('/(tabs)/leaderboard')}
                        >
                            <LinearGradient
                                colors={['rgba(245, 158, 11, 0.2)', 'rgba(217, 119, 6, 0.3)']}
                                style={styles.quickAccessCardGradient}
                            >
                                <View style={styles.quickAccessRow}>
                                    <Trophy size={20} color="#f59e0b" />
                                    <Text style={styles.quickAccessCardLabel}>Leaders</Text>
                                </View>
                            </LinearGradient>
                        </TouchableOpacity>

                        <TouchableOpacity
                            style={styles.quickAccessCard}
                            onPress={() => router.push('/(tabs)/presale')}
                        >
                            <LinearGradient
                                colors={['rgba(16, 185, 129, 0.2)', 'rgba(5, 150, 105, 0.3)']}
                                style={styles.quickAccessCardGradient}
                            >
                                <View style={styles.quickAccessRow}>
                                    <Store size={20} color="#10b981" />
                                    <Text style={styles.quickAccessCardLabel}>Presale</Text>
                                </View>
                            </LinearGradient>
                        </TouchableOpacity>

                        <TouchableOpacity
                            style={styles.quickAccessCard}
                            onPress={() => router.push('/(tabs)/wallet')}
                        >
                            <LinearGradient
                                colors={['rgba(139, 92, 246, 0.2)', 'rgba(124, 58, 237, 0.3)']}
                                style={styles.quickAccessCardGradient}
                            >
                                <View style={styles.quickAccessRow}>
                                    <Wallet size={20} color="#8b5cf6" />
                                    <Text style={styles.quickAccessCardLabel}>Wallet</Text>
                                </View>
                            </LinearGradient>
                        </TouchableOpacity>

                        <TouchableOpacity
                            style={styles.quickAccessCard}
                            onPress={() => router.push('/(tabs)/profile')}
                        >
                            <LinearGradient
                                colors={['rgba(236, 72, 153, 0.2)', 'rgba(219, 39, 119, 0.3)']}
                                style={styles.quickAccessCardGradient}
                            >
                                <View style={styles.quickAccessRow}>
                                    <User size={20} color="#ec4899" />
                                    <Text style={styles.quickAccessCardLabel}>Profile</Text>
                                </View>
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>
                </View>

                {/* Click Effects */}
                {clickEffects.map((effect: { id: number; x: number; y: number }) => (
                    <View
                        key={effect.id}
                        style={[
                            styles.clickEffect,
                            { left: effect.x, top: effect.y }
                        ]}
                    >
                        <LinearGradient
                            colors={['#ffa000', '#ff8f00']}
                            style={styles.clickEffectGradient}
                        >
                            <Text style={styles.clickEffectText}>+ Mining</Text>
                        </LinearGradient>
                    </View>
                ))}
            </ScrollView>

            {/* Modals and Overlays */}
            {/* Achievements Modal */}
            <Modal
                visible={showAchievements}
                animationType="slide"
                presentationStyle="pageSheet"
            >
                <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={{ flex: 1 }}>
                    <View style={{ paddingTop: insets.top + 20, paddingHorizontal: 20, flex: 1 }}>
                        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                            <Text style={{ fontSize: 24, fontWeight: 'bold', color: '#ffffff' }}>Achievements</Text>
                            <TouchableOpacity
                                onPress={() => setShowAchievements(false)}
                                style={{ padding: 8 }}
                            >
                                <X size={24} color="#ffffff" />
                            </TouchableOpacity>
                        </View>
                        <AchievementSystem profile={profile} onRefresh={refreshProfile} />
                    </View>
                </LinearGradient>
            </Modal>

            {/* Ad Modal */}
            <AdModal
                isVisible={showAdModal}
                onClose={() => setShowAdModal(false)}
                onComplete={handleAdReward}
                title="Watch Ad for Bonus"
                description="Watch a short advertisement to earn 0.5 EKH bonus tokens!"
                reward={0.5}
                onTestEvent={(event, data) => {
                    console.log('[AdModal Test Event]', event, data);
                    // In a real testing environment, you might want to send this to a testing framework
                }}
            />

            {/* Notification System */}
            <NotificationSystem />
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
        gap: 20,
    },
    loadingText: {
        color: '#ffffff',
        fontSize: 18,
    },
    header: {
        alignItems: 'center',
        marginBottom: 24,
        marginTop: 20,
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
        color: '#ffffff',
        marginBottom: 8,
    },
    subtitle: {
        fontSize: 16,
        color: 'rgba(255, 255, 255, 0.7)',
        textAlign: 'center',
    },
    // Professional User Card Styles
    userCard: {
        backgroundColor: 'rgba(255, 255, 255, 0.1)',
        borderRadius: 16,
        marginBottom: 16,
        borderWidth: 1,
        borderColor: 'rgba(255, 160, 0, 0.3)',
        width: '100%',
        height: 80,
    },
    userCardGradient: {
        borderRadius: 16,
        padding: 12,
        backgroundColor: 'rgba(255, 160, 0, 0.1)',
        height: '100%',
    },
    userCardContent: {
        flexDirection: 'row',
        alignItems: 'center',
        height: '100%',
    },
    userInfoContainer: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'center',
    },
    userAvatar: {
        width: 40,
        height: 40,
        borderRadius: 20,
        backgroundColor: 'rgba(255, 160, 0, 0.2)',
        justifyContent: 'center',
        alignItems: 'center',
        marginRight: 12,
    },
    userInfo: {
        flex: 1,
    },
    username: {
        fontSize: 16,
        fontWeight: '600',
        color: '#ffffff',
        marginBottom: 2,
    },
    userScore: {
        fontSize: 14,
        color: '#ffa000',
        fontWeight: '700',
    },
    miningRateText: {
        fontSize: 12,
        color: 'rgba(255, 255, 255, 0.7)',
        marginTop: 2,
    },
    statsContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        marginBottom: 24,
    },
    statCard: {
        backgroundColor: 'rgba(255, 255, 255, 0.1)',
        borderRadius: 16,
        padding: 16,
        alignItems: 'center',
        flex: 1,
        marginHorizontal: 4,
        borderWidth: 1,
        borderColor: 'rgba(255, 255, 255, 0.2)',
    },
    statValue: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#ffffff',
        marginVertical: 8,
    },
    statLabel: {
        fontSize: 14,
        color: 'rgba(255, 255, 255, 0.7)',
    },
    miningProgressContainer: {
        backgroundColor: 'rgba(255, 255, 255, 0.1)',
        borderRadius: 16,
        padding: 20,
        marginBottom: 24,
        borderWidth: 1,
        borderColor: 'rgba(255, 160, 0, 0.3)',
    },
    miningProgressHeader: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: 12,
    },
    miningProgressTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#ffffff',
    },
    miningProgressTime: {
        fontSize: 16,
        fontWeight: '600',
        color: '#ffa000',
    },
    miningProgressBar: {
        height: 8,
        backgroundColor: 'rgba(255, 255, 255, 0.1)',
        borderRadius: 4,
        overflow: 'hidden',
        marginBottom: 8,
    },
    miningProgressFill: {
        height: '100%',
        backgroundColor: '#ffa000',
        borderRadius: 4,
    },
    miningEarningsText: {
        fontSize: 14,
        color: 'rgba(255, 255, 255, 0.7)',
        textAlign: 'center',
    },
    miningContainer: {
        alignItems: 'center',
        marginBottom: 30,
        marginTop: 16,
    },
    miningButtonWrapper: {
        position: 'relative',
        marginBottom: 16,
    },
    particlesContainer: {
        ...StyleSheet.absoluteFillObject,
        zIndex: 1,
    },
    particle: {
        position: 'absolute',
        width: 12,
        height: 12,
        borderRadius: 6,
    },
    circularProgressContainer: {
        position: 'absolute',
        width: '100%',
        height: '100%',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 2,
    },
    progressTextContainer: {
        alignItems: 'center',
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: [{ translateX: -50 }, { translateY: -50 }],
    },
    progressTimeText: {
        fontSize: 16,
        fontWeight: 'bold',
        color: '#ffffff',
    },
    progressLabel: {
        fontSize: 12,
        color: 'rgba(255, 255, 255, 0.7)',
    },
    miningButton: {
        width: 150,
        height: 150,
        borderRadius: 75,
        zIndex: 3,
    },
    miningButtonGradient: {
        width: '100%',
        height: '100%',
        borderRadius: 75,
        justifyContent: 'center',
        alignItems: 'center',
    },
    miningRate: {
        fontSize: 16,
        color: '#ffffff',
        fontWeight: '600',
        textAlign: 'center',
    },
    adBonusContainer: {
        marginBottom: 30,
    },
    adBonusButton: {
        borderRadius: 16,
        overflow: 'hidden',
        marginBottom: 12,
    },
    adBonusButtonGradient: {
        paddingVertical: 16,
        paddingHorizontal: 20,
    },
    adButtonContent: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 12,
    },
    adBonusButtonText: {
        color: '#ffffff',
        fontSize: 16,
        fontWeight: '600',
    },
    adBonusDescription: {
        fontSize: 14,
        color: 'rgba(255, 255, 255, 0.6)',
        textAlign: 'center',
    },
    sessionContainer: {
        backgroundColor: 'rgba(255, 255, 255, 0.1)',
        borderRadius: 16,
        padding: 20,
        marginBottom: 24,
        borderWidth: 1,
        borderColor: 'rgba(255, 255, 255, 0.2)',
    },
    sessionTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#ffffff',
        marginBottom: 16,
        textAlign: 'center',
    },
    sessionStats: {
        flexDirection: 'row',
        justifyContent: 'space-around',
    },
    sessionStat: {
        alignItems: 'center',
    },
    sessionStatValue: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#ffffff',
        marginBottom: 4,
    },
    sessionStatLabel: {
        fontSize: 14,
        color: 'rgba(255, 255, 255, 0.7)',
    },
    sessionLoadingContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    sessionLoadingText: {
        color: '#ffffff',
        fontSize: 18,
    },
    actionButtonsContainer: {
        flexDirection: 'row',
        justifyContent: 'center',
        marginBottom: 30,
    },
    actionButton: {
        borderRadius: 16,
        overflow: 'hidden',
        marginHorizontal: 8,
    },
    actionButtonGradient: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingVertical: 12,
        paddingHorizontal: 20,
        gap: 8,
    },
    actionButtonText: {
        color: '#ffffff',
        fontSize: 16,
        fontWeight: '600',
    },
    quickAccessSection: {
        marginBottom: 30,
    },
    quickAccessTitle: {
        fontSize: 18,
        fontWeight: 'bold',
        color: '#ffffff',
        marginBottom: 16,
        textAlign: 'center',
    },
    quickAccessContainer: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
    },
    quickAccessCard: {
        width: '30%',
        borderRadius: 16,
        overflow: 'hidden',
        marginBottom: 16,
    },
    quickAccessCardGradient: {
        padding: 16,
        alignItems: 'center',
    },
    quickAccessRow: {
        alignItems: 'center',
        gap: 8,
    },
    quickAccessCardLabel: {
        fontSize: 12,
        color: '#ffffff',
        fontWeight: '600',
        textAlign: 'center',
    },
    clickEffect: {
        position: 'absolute',
        zIndex: 1000,
    },
    clickEffectGradient: {
        paddingVertical: 4,
        paddingHorizontal: 8,
        borderRadius: 12,
    },
    clickEffectText: {
        color: '#ffffff',
        fontSize: 12,
        fontWeight: 'bold',
    },
    progressText: {
        fontSize: 8,
        color: 'rgba(255, 255, 255, 0.5)',
        marginTop: 2,
    },
    miningTimeText: {
        fontSize: 24,
        fontWeight: 'bold',
        color: '#ffffff',
    },
});