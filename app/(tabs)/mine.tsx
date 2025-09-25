import { useEffect, useState, useRef } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Dimensions, Modal } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import * as Haptics from 'expo-haptics';
import { Pickaxe, Coins, Flame, Users, TrendingUp, Zap, Trophy, Play, X, Share2, Store, Wallet, User } from 'lucide-react-native';
import { useMining } from '@/contexts/MiningContext';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'expo-router';
import { useNotifications } from '@/contexts/NotificationContext';
import Animated, { useSharedValue, useAnimatedStyle, withRepeat, Easing, withTiming, withSequence } from 'react-native-reanimated';
import AutoMiningStatus from '@/components/AutoMiningStatus';
import AchievementSystem from '@/components/AchievementSystem';
import AdModal from '@/components/AdModal';
import NotificationSystem from '@/components/NotificationSystem';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { databases, appwriteConfig } from '@/config/appwrite';
import { ID } from 'appwrite';
import JellyTriangleLoader from '@/components/JellyTriangleLoader';


export default function MinePage() {
    const router = useRouter();
    const { user, isLoading: authLoading } = useAuth();
    const { profile, sessionCoins, sessionClicks, refreshProfile, startMiningSession, addCoins, endMiningSession } = useMining();
    const insets = useSafeAreaInsets();
    const [clickEffects, setClickEffects] = useState<Array<{ id: number; x: number; y: number }>>([]);
    const [showAchievements, setShowAchievements] = useState(false);
    const [is24HourMiningActive, setIs24HourMiningActive] = useState(false);
    const [miningStartTime, setMiningStartTime] = useState<number | null>(null);
    const [remainingTime, setRemainingTime] = useState(24 * 60 * 60); // 24 hours in seconds
    const [sessionReward, setSessionReward] = useState(2); // 2 EKH total for the 24-hour session
    const [finalRewardClaimed, setFinalRewardClaimed] = useState(false); // Track if final reward has been claimed
    const [showAdModal, setShowAdModal] = useState(false); // For ad bonus modal
    const [adCooldown, setAdCooldown] = useState(0); // Cooldown timer for ad watching

    const { showNotification } = useNotifications();
    const scale = useSharedValue(1);
    const rotation = useSharedValue(0);
    const borderRotation = useSharedValue(0); // For mining button border rotation
    const axePulse = useSharedValue(1); // For axe pulsing animation

    // Particle animations
    const particleAnim1 = useSharedValue(1);
    const particleAnim2 = useSharedValue(1);
    const particleAnim3 = useSharedValue(1);
    const particleAnim4 = useSharedValue(1);
    const particleAnim5 = useSharedValue(1);
    const particleAnim6 = useSharedValue(1);
    const orbitRotation1 = useSharedValue(0);
    const orbitRotation2 = useSharedValue(0);
    const electricalGlow = useSharedValue(0.3);
    const planetScale1 = useSharedValue(1);
    const planetScale2 = useSharedValue(1);

    const miningIntervalRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const timeIntervalRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const adCooldownRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    // Initialize particle animations
    useEffect(() => {
        particleAnim1.value = withRepeat(
            withSequence(
                withTiming(1.5, { duration: 1000 }),
                withTiming(1, { duration: 1500 })
            ),
            -1,
            true
        );

        setTimeout(() => {
            particleAnim2.value = withRepeat(
                withSequence(
                    withTiming(1.5, { duration: 1000 }),
                    withTiming(1, { duration: 1500 })
                ),
                -1,
                true
            );
        }, 500);

        setTimeout(() => {
            particleAnim3.value = withRepeat(
                withSequence(
                    withTiming(1.5, { duration: 1000 }),
                    withTiming(1, { duration: 1500 })
                ),
                -1,
                true
            );
        }, 1000);

        setTimeout(() => {
            particleAnim4.value = withRepeat(
                withSequence(
                    withTiming(1.5, { duration: 1000 }),
                    withTiming(1, { duration: 1500 })
                ),
                -1,
                true
            );
        }, 1500);

        setTimeout(() => {
            particleAnim5.value = withRepeat(
                withSequence(
                    withTiming(1.5, { duration: 1000 }),
                    withTiming(1, { duration: 1500 })
                ),
                -1,
                true
            );
        }, 2000);

        setTimeout(() => {
            particleAnim6.value = withRepeat(
                withSequence(
                    withTiming(1.5, { duration: 1000 }),
                    withTiming(1, { duration: 1500 })
                ),
                -1,
                true
            );
        }, 2500);
    }, []);

   useEffect(() => {
  if (is24HourMiningActive && remainingTime > 0) {
    // Start pulsating when mining is active
    axePulse.value = withRepeat(
      withSequence(
        withTiming(1.2, { duration: 600, easing: Easing.inOut(Easing.ease) }),
        withTiming(1, { duration: 600, easing: Easing.inOut(Easing.ease) })
      ),
      -1,
      false
    );
  } else {
    // Stop pulsating and return to normal size
    axePulse.value = withTiming(1, { duration: 300 });
  }
}, [is24HourMiningActive, remainingTime]);

// Orbital and electrical effects
useEffect(() => {
  if (is24HourMiningActive) {
    // Start orbital rotations - continue throughout entire mining session
    orbitRotation1.value = withRepeat(
      withTiming(360, { duration: 3000, easing: Easing.linear }),
      -1,
      false
    );
    
    orbitRotation2.value = withRepeat(
      withTiming(-360, { duration: 4500, easing: Easing.linear }),
      -1,
      false
    );
    
    // Planet pulsing - continue throughout entire mining session
    planetScale1.value = withRepeat(
      withSequence(
        withTiming(1.3, { duration: 1200 }),
        withTiming(1, { duration: 1200 })
      ),
      -1,
      false
    );
    
    planetScale2.value = withRepeat(
      withSequence(
        withTiming(1.2, { duration: 1500 }),
        withTiming(1, { duration: 1500 })
      ),
      -1,
      false
    );
    
    // Electrical glow only while mining is in progress
    if (remainingTime > 0) {
      electricalGlow.value = withRepeat(
        withSequence(
          withTiming(0.8, { duration: 800 }),
          withTiming(0.3, { duration: 800 })
        ),
        -1,
        false
      );
    } else {
      // Mining completed - dim the electrical glow but keep orbits going
      electricalGlow.value = withTiming(0.2, { duration: 500 });
    }
  } else {
    // Stop all animations only when mining session ends completely
    orbitRotation1.value = withTiming(0, { duration: 500 });
    orbitRotation2.value = withTiming(0, { duration: 500 });
    electricalGlow.value = withTiming(0.3, { duration: 500 });
    planetScale1.value = withTiming(1, { duration: 500 });
    planetScale2.value = withTiming(1, { duration: 500 });
  }
}, [is24HourMiningActive, remainingTime]);

    useEffect(() => {
        // Handle authentication state changes
        if (!user && !authLoading) {
            // If user is not authenticated and not loading, redirect to index
            console.log('User not authenticated in mining screen, redirecting to index');
            router.replace('/');
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


    const orbit1AnimatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ rotate: `${orbitRotation1.value}deg` }],
        };
    });

    const orbit2AnimatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ rotate: `${orbitRotation2.value}deg` }],
        };
    });

    const electricalRingStyle = useAnimatedStyle(() => {
        return {
            opacity: electricalGlow.value,
            shadowOpacity: electricalGlow.value,
        };
    });

    const planet1Style = useAnimatedStyle(() => {
        return {
            transform: [{ scale: planetScale1.value }],
        };
    });

    const planet2Style = useAnimatedStyle(() => {
        return {
            transform: [{ scale: planetScale2.value }],
        };
    });


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

        // Start border rotation animation for the mining button
        borderRotation.value = withRepeat(
            withTiming(360, { duration: 2000, easing: Easing.linear }),
            -1,
            false
        );
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

        // Stop border rotation animation
        borderRotation.value = withTiming(0, { duration: 500 });

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

    const resetMiningSession = async () => {
        // Clear intervals
        if (miningIntervalRef.current) {
            clearInterval(miningIntervalRef.current);
            miningIntervalRef.current = null;
        }

        if (timeIntervalRef.current) {
            clearInterval(timeIntervalRef.current);
            timeIntervalRef.current = null;
        }

        // Reset border rotation animation
        borderRotation.value = withTiming(0, { duration: 500 });

        // Reset state
        setIs24HourMiningActive(false);
        setMiningStartTime(null);
        setRemainingTime(24 * 60 * 60);
        setSessionReward(2);
        setFinalRewardClaimed(false);

        // Clear saved session
        await AsyncStorage.removeItem('miningSession');
    };

    const formatTime = (seconds: number) => {
        const hrs = Math.floor(seconds / 3600);
        const mins = Math.floor((seconds % 3600) / 60);
        const secs = seconds % 60;
        return `${hrs.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    const animatedStyle = useAnimatedStyle(() => {
        return {
            transform: [
                { scale: is24HourMiningActive ? 1 : scale.value },
                { rotate: is24HourMiningActive ? `${rotation.value}deg` : `${rotation.value}deg` }
            ],
        };
    });

    // Border rotation animation for mining button
    const borderAnimatedStyle = useAnimatedStyle(() => {
        return {
            transform: [
                { rotate: `${borderRotation.value}deg` }
            ],
        };
    });

    // Particle animated styles
    const particleStyle1 = useAnimatedStyle(() => {
        return {
            transform: [{ scale: particleAnim1.value }],
            opacity: 0.6 * (2 - particleAnim1.value),
        };
    });

    const particleStyle2 = useAnimatedStyle(() => {
        return {
            transform: [{ scale: particleAnim2.value }],
            opacity: 0.6 * (2 - particleAnim2.value),
        };
    });

    const particleStyle3 = useAnimatedStyle(() => {
        return {
            transform: [{ scale: particleAnim3.value }],
            opacity: 0.6 * (2 - particleAnim3.value),
        };
    });

    const particleStyle4 = useAnimatedStyle(() => {
        return {
            transform: [{ scale: particleAnim4.value }],
            opacity: 0.6 * (2 - particleAnim4.value),
        };
    });

    const particleStyle5 = useAnimatedStyle(() => {
        return {
            transform: [{ scale: particleAnim5.value }],
            opacity: 0.6 * (2 - particleAnim5.value),
        };
    });

    const particleStyle6 = useAnimatedStyle(() => {
        return {
            transform: [{ scale: particleAnim6.value }],
            opacity: 0.6 * (2 - particleAnim6.value),
        };
    });

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
        // Simulate ad completion
        return new Promise<{ success: boolean; reward?: number; error?: string }>((resolve) => {
            setTimeout(() => {
                resolve({ success: true, reward: 0.5 });
            }, 2000);
        });
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

                return { success: true };
            } catch (error) {
                showNotification({
                    type: 'error',
                    title: 'Reward Failed',
                    message: 'Failed to process your ad reward. Please try again.',
                    duration: 3000,
                });
                return { success: false, error: 'Failed to process reward' };
            }
        } else {
            return { success: false, error: result.error || 'Ad completion failed' };
        }
    };

    // Show loading state while checking authentication
    if (authLoading) {
        return (
            <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
                <View style={styles.loadingContainer}>
                    <JellyTriangleLoader size={40} color="#ffa000" speed={1750} />
                </View>
            </LinearGradient>
        );
    }

    // Show loading state while profile is loading
    if (!profile && user && !authLoading) {
        return (
            <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
                <View style={styles.loadingContainer}>
                    <JellyTriangleLoader size={40} color="#ffa000" speed={1750} />
                    <Text style={styles.loadingText}>Loading profile...</Text>
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

    const dailyProgress = (profile.todayEarnings / profile.maxDailyEarnings) * 100;
    const coinsPerClick = profile.dailyMiningRate / 100;
    const progressPercentage = ((24 * 60 * 60 - remainingTime) / (24 * 60 * 60)) * 100;

    // Calculate mining rate per hour
    const miningRatePerHour = profile.dailyMiningRate / 24;

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
                                    <Text style={styles.miningRateText}>{miningRatePerHour.toFixed(2)} EKH/hr</Text>
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
                        <Text style={styles.statValue}>{profile.miningPower}</Text>
                        <Text style={styles.statLabel}>Power</Text>
                    </View>
                </View>

                {/* Extended Mining Progress */}
                {is24HourMiningActive && (
                    <View style={styles.miningProgressContainer}>
                        <View style={styles.miningProgressHeader}>
                            <Text style={styles.miningProgressTitle}>Extended Mining Session</Text>
                            <Text style={styles.miningProgressTime}>{formatTime(remainingTime)}</Text>
                        </View>
                        <View style={styles.miningProgressBar}>
                            <View
                                style={[
                                    styles.miningProgressFill,
                                    { width: `${progressPercentage}%` }
                                ]}
                            />
                        </View>
                        <Text style={styles.miningEarningsText}>
                            {remainingTime > 0
                                ? `Mining in progress...`
                                : `Completed! Press to claim ${sessionReward} EKH`}
                        </Text>
                    </View>
                )}

                {/* Mining Button - Clean Implementation */}
                <View style={styles.miningContainer}>
                    <View style={styles.miningButtonWrapper}>
                        {/* Floating Energy Particles - keep existing */}
                        <View style={styles.particlesContainer} pointerEvents="none">
                            {/* Your existing particles code */}
                        </View>

                        {/* Enhanced Rotating Ring System */}
                        <View style={styles.ringSystem} pointerEvents="none">
                            {/* Main Electrical Ring */}
                            <Animated.View
                                style={[
                                    styles.electricalRing,
                                    borderAnimatedStyle,
                                    electricalRingStyle
                                ]}
                            />

                            {/* Orbital Ring 1 */}
                            <Animated.View
                                style={[
                                    styles.orbitRing,
                                    orbit1AnimatedStyle
                                ]}
                            >
                                <Animated.View style={[styles.planet1, planet1Style]} />
                            </Animated.View>

                            {/* Orbital Ring 2 */}
                            <Animated.View
                                style={[
                                    styles.orbitRing2,
                                    orbit2AnimatedStyle
                                ]}
                            >
                                <Animated.View style={[styles.planet2, planet2Style]} />
                                <Animated.View style={[styles.planet2, planet2Style, { top: '75%' }]} />
                            </Animated.View>

                            {/* Inner Glow Ring */}
                            <Animated.View
                                style={[
                                    styles.innerGlowRing,
                                    electricalRingStyle
                                ]}
                            />
                        </View>

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
                                {is24HourMiningActive && remainingTime > 0 ? (
                                    <Animated.View style={animatedStyle}>
                                        <Zap size={60} color="#ffffff" />
                                    </Animated.View>
                                ) : is24HourMiningActive && remainingTime <= 0 ? (
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
                                ? ` `
                                : `Claim ${sessionReward} EKH Reward`
                            : `Start Extended Session (+${sessionReward} EKH)`}
                    </Text>
                </View>

                {/* Ad Bonus Button - REVERTED TO PREVIOUS STATE */}
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
                <AutoMiningStatus profile={profile} onRefresh={refreshProfile} />

                {/* Session Stats */}
                <View style={styles.sessionContainer}>
                    <Text style={styles.sessionTitle}>This Session</Text>
                    <View style={styles.sessionStats}>
                        <View style={styles.sessionStat}>
                            <Text style={styles.sessionStatValue}>{sessionCoins.toFixed(2)}</Text>
                            <Text style={styles.sessionStatLabel}>EKH Earned</Text>
                        </View>
                        <View style={styles.sessionStat}>
                            <Text style={styles.sessionStatValue}>{sessionClicks}</Text>
                            <Text style={styles.sessionStatLabel}>Streaks</Text>
                        </View>
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
                    <Animated.View
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
                            <Text style={styles.clickEffectText}>+{coinsPerClick.toFixed(3)}</Text>
                        </LinearGradient>
                    </Animated.View>
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
                onComplete={async () => {
                    const result = await handleAdComplete();
                    return handleAdReward(result);
                }}
                title="Watch Ad for Bonus"
                description="Watch a short advertisement to earn 0.5 EKH bonus tokens!"
                reward={0.5}
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
    rotatingRing: {
        position: 'absolute',
        width: 180,
        height: 180,
        borderRadius: 90,
        borderWidth: 3,
        borderColor: 'rgba(255, 160, 0, 0.5)',
        zIndex: 2,
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

    ringSystem: {
        position: 'absolute',
        width: '100%',
        height: '100%',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 2,
    },
    electricalRing: {
        position: 'absolute',
        width: 180,
        height: 180,
        borderRadius: 90,
        borderWidth: 3,
        borderColor: '#00d4ff',
        shadowColor: '#00d4ff',
        shadowOffset: { width: 0, height: 0 },
        shadowRadius: 10,
        shadowOpacity: 0.6,
    },
    orbitRing: {
        position: 'absolute',
        width: 200,
        height: 200,
        borderRadius: 100,
        borderWidth: 1,
        borderColor: 'rgba(255, 160, 0, 0.3)',
    },
    orbitRing2: {
        position: 'absolute',
        width: 160,
        height: 160,
        borderRadius: 80,
        borderWidth: 1,
        borderColor: 'rgba(16, 185, 129, 0.3)',
    },
    planet1: {
        position: 'absolute',
        width: 8,
        height: 8,
        borderRadius: 4,
        backgroundColor: '#ffa000',
        top: -4,
        left: '50%',
        marginLeft: -4,
        shadowColor: '#ffa000',
        shadowOffset: { width: 0, height: 0 },
        shadowRadius: 4,
        shadowOpacity: 0.8,
    },
    planet2: {
        position: 'absolute',
        width: 6,
        height: 6,
        borderRadius: 3,
        backgroundColor: '#10b981',
        top: -3,
        left: '50%',
        marginLeft: -3,
        shadowColor: '#10b981',
        shadowOffset: { width: 0, height: 0 },
        shadowRadius: 3,
        shadowOpacity: 0.8,
    },
    innerGlowRing: {
        position: 'absolute',
        width: 140,
        height: 140,
        borderRadius: 70,
        borderWidth: 2,
        borderColor: '#818cf8',
        shadowColor: '#818cf8',
        shadowOffset: { width: 0, height: 0 },
        shadowRadius: 8,
        shadowOpacity: 0.4,
    },
});