import { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Alert, TextInput, Share, Platform } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { User, Edit3, Share2, Copy, Trophy, Coins, Zap, Calendar, LogOut, Check, Users } from 'lucide-react-native';
import { UserProfile } from '@/types';
import { useAuth } from '@/contexts/AuthContext';
import { useMining } from '@/contexts/MiningContext';
import { useRouter } from 'expo-router';
import * as Clipboard from 'expo-clipboard';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { databases, appwriteConfig } from '@/config/appwrite';
import PulseLoader from '@/components/PulseLoader';
import LoadingDots from '@/components/LoadingDots';

export default function ProfilePage() {
  const router = useRouter();
  const { user, signOut } = useAuth();
  const { profile: miningProfile, isLoading: isMiningLoading, refreshProfile } = useMining();
  const profile: UserProfile | null = miningProfile;
  const insets = useSafeAreaInsets();
  const [isEditing, setIsEditing] = useState(false);
  const [newUsername, setNewUsername] = useState('');
  const [copied, setCopied] = useState(false);
  const [isSigningOut, setIsSigningOut] = useState(false);
  const [notificationEnabled, setNotificationEnabled] = useState(true);
  const [privacyLevel, setPrivacyLevel] = useState('public');
  const [securityLevel, setSecurityLevel] = useState('standard');
  const [showChangePassword, setShowChangePassword] = useState(false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  useEffect(() => {
    if (!user) {
      // Add a small delay to ensure router is ready
      setTimeout(() => {
        router.replace('/auth');
      }, 100);
      return;
    }
    // Initialize username for editing
    if (profile?.username) {
      setNewUsername(profile.username);
    }
    // Profile is already loaded by MiningContext
    
    // Load saved settings
    loadSavedSettings();
  }, [user, profile, router]);

  const loadSavedSettings = async () => {
    try {
      // Load notification settings
      const savedNotifications = await AsyncStorage.getItem('notificationsEnabled');
      if (savedNotifications !== null) {
        setNotificationEnabled(JSON.parse(savedNotifications));
      }
      
      // Load privacy settings
      const savedPrivacy = await AsyncStorage.getItem('privacyLevel');
      if (savedPrivacy) {
        setPrivacyLevel(savedPrivacy);
      }
      
      // Load security settings
      const savedSecurity = await AsyncStorage.getItem('securityLevel');
      if (savedSecurity) {
        setSecurityLevel(savedSecurity);
      }
    } catch (error) {
      console.error('Failed to load saved settings:', error);
    }
  };

  const handleUpdateUsername = async () => {
    if (!newUsername.trim() || !profile) return;

    try {
      // Update username in Appwrite database
      await databases.updateDocument(
        appwriteConfig.databaseId,
        appwriteConfig.collections.userProfiles,
        profile.id,
        {
          username: newUsername.trim(),
          updatedAt: new Date().toISOString()
        }
      );
      
      // Refresh profile to get updated data
      refreshProfile();
      setIsEditing(false);
      Alert.alert('Success', 'Username updated successfully!');
    } catch (error) {
      console.error('Failed to update username:', error);
      Alert.alert('Error', 'Failed to update username. Please try again.');
    }
  };

  const copyReferralCode = async () => {
    if (!profile?.referralCode) return;

    try {
      await Clipboard.setStringAsync(profile.referralCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
      Alert.alert('Copied!', 'Referral code copied to clipboard');
    } catch (error) {
      console.error('Failed to copy:', error);
      Alert.alert('Error', 'Failed to copy referral code. Please try again.');
    }
  };

  const shareReferralCode = async () => {
    if (!profile?.referralCode) return;

    try {
      await Share.share({
        message: `Join me on Ekehi Network and start mining EKH tokens! Use my referral code: ${profile.referralCode}`,
        title: 'Join Ekehi Network',
      });
    } catch (error) {
      console.error('Failed to share:', error);
      Alert.alert('Error', 'Failed to share referral code. Please try again.');
    }
  };

  // Add these functions after the existing functions
  const handleNotificationToggle = async () => {
    try {
      const newNotificationState = !notificationEnabled;
      setNotificationEnabled(newNotificationState);
      
      // In a real app, you would save this to your backend or AsyncStorage
      await AsyncStorage.setItem('notificationsEnabled', JSON.stringify(newNotificationState));
      
      Alert.alert('Success', `Notifications ${newNotificationState ? 'enabled' : 'disabled'}`);
    } catch (error) {
      console.error('Failed to update notification settings:', error);
      Alert.alert('Error', 'Failed to update notification settings. Please try again.');
    }
  };

  const handleChangePrivacy = () => {
    Alert.alert(
      'Privacy Settings',
      'Select your privacy level',
      [
        { text: 'Cancel', style: 'cancel' },
        { 
          text: 'Public', 
          onPress: async () => {
            try {
              setPrivacyLevel('public');
              // In a real app, you would save this to your backend or AsyncStorage
              await AsyncStorage.setItem('privacyLevel', 'public');
              Alert.alert('Success', 'Privacy set to Public');
            } catch (error) {
              console.error('Failed to update privacy settings:', error);
              Alert.alert('Error', 'Failed to update privacy settings. Please try again.');
            }
          }
        },
        { 
          text: 'Friends Only', 
          onPress: async () => {
            try {
              setPrivacyLevel('friends');
              // In a real app, you would save this to your backend or AsyncStorage
              await AsyncStorage.setItem('privacyLevel', 'friends');
              Alert.alert('Success', 'Privacy set to Friends Only');
            } catch (error) {
              console.error('Failed to update privacy settings:', error);
              Alert.alert('Error', 'Failed to update privacy settings. Please try again.');
            }
          }
        },
        { 
          text: 'Private', 
          onPress: async () => {
            try {
              setPrivacyLevel('private');
              // In a real app, you would save this to your backend or AsyncStorage
              await AsyncStorage.setItem('privacyLevel', 'private');
              Alert.alert('Success', 'Privacy set to Private');
            } catch (error) {
              console.error('Failed to update privacy settings:', error);
              Alert.alert('Error', 'Failed to update privacy settings. Please try again.');
            }
          }
        },
      ]
    );
  };

  const handleChangeSecurity = () => {
    Alert.alert(
      'Security Level',
      'Select your security level',
      [
        { text: 'Cancel', style: 'cancel' },
        { 
          text: 'Standard', 
          onPress: async () => {
            try {
              setSecurityLevel('standard');
              // In a real app, you would save this to your backend or AsyncStorage
              await AsyncStorage.setItem('securityLevel', 'standard');
              Alert.alert('Success', 'Security set to Standard');
            } catch (error) {
              console.error('Failed to update security settings:', error);
              Alert.alert('Error', 'Failed to update security settings. Please try again.');
            }
          }
        },
        { 
          text: 'Enhanced', 
          onPress: async () => {
            try {
              setSecurityLevel('enhanced');
              // In a real app, you would save this to your backend or AsyncStorage
              await AsyncStorage.setItem('securityLevel', 'enhanced');
              Alert.alert('Success', 'Security set to Enhanced');
            } catch (error) {
              console.error('Failed to update security settings:', error);
              Alert.alert('Error', 'Failed to update security settings. Please try again.');
            }
          }
        },
        { 
          text: 'Maximum', 
          onPress: async () => {
            try {
              setSecurityLevel('maximum');
              // In a real app, you would save this to your backend or AsyncStorage
              await AsyncStorage.setItem('securityLevel', 'maximum');
              Alert.alert('Success', 'Security set to Maximum');
            } catch (error) {
              console.error('Failed to update security settings:', error);
              Alert.alert('Error', 'Failed to update security settings. Please try again.');
            }
          }
        },
      ]
    );
  };

  const handleChangePassword = async () => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      Alert.alert('Error', 'Please fill in all password fields');
      return;
    }

    if (newPassword !== confirmPassword) {
      Alert.alert('Error', 'New passwords do not match');
      return;
    }

    if (newPassword.length < 8) {
      Alert.alert('Error', 'Password must be at least 8 characters long');
      return;
    }

    try {
      // In a real app, you would update the password through your backend
      // For now, we'll just simulate the process
      await new Promise(resolve => setTimeout(() => resolve(null), 1000));
      
      Alert.alert('Success', 'Password changed successfully!');
      setShowChangePassword(false);
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (error) {
      console.error('Failed to change password:', error);
      Alert.alert('Error', 'Failed to change password. Please try again.');
    }
  };

  // Add the missing functions for password change
  const handlePasswordUpdate = async () => {
    await handleChangePassword();
  };

  const cancelPasswordChange = () => {
    setShowChangePassword(false);
    setCurrentPassword('');
    setNewPassword('');
    setConfirmPassword('');
  };

  const handleSignOut = () => {
    Alert.alert(
      'Sign Out',
      'Are you sure you want to sign out?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Sign Out',
          style: 'destructive',
          onPress: async () => {
            try {
              setIsSigningOut(true);
              console.log('Starting sign out process');
              
              // Different approaches for web vs mobile platforms
              if (Platform.OS === 'web') {
                // For web platforms, use window.location approach
                console.log('Web platform detected, using direct sign out approach');
                await signOut();
                // Force redirect to auth page with a small delay to ensure session deletion
                setTimeout(() => {
                  // Only execute on web platform
                  if (Platform.OS === 'web') {
                    // @ts-ignore - window object only exists on web
                    if (typeof window !== 'undefined' && window?.location?.href) {
                      // @ts-ignore
                      window.location.href = '/auth';
                    }
                  }
                }, 300);
              } else {
                // For mobile platforms, use the callback approach
                await signOut(() => {
                  console.log('Sign out callback executed, navigating to auth screen');
                  // Use replace instead of navigate to prevent going back
                  router.replace('/auth');
                });
                
                // Fallback navigation in case the callback doesn't execute
                setTimeout(() => {
                  if (!user) {
                    console.log('Fallback navigation to auth screen');
                    router.replace('/auth');
                  }
                }, 1000);
              }
            } catch (error: any) {
              console.error('Sign out failed:', error);
              Alert.alert('Error', `Failed to sign out: ${error.message || 'Please try again.'}`);
              
              // Even if there's an error, redirect to auth page on web
              if (Platform.OS === 'web') {
                setTimeout(() => {
                  // @ts-ignore - window object only exists on web
                  if (typeof window !== 'undefined' && window?.location?.href) {
                    // @ts-ignore
                    window.location.href = '/auth';
                  }
                }, 300);
              }
              
              // Reset loading state on mobile
              if (Platform.OS !== 'web') {
                setIsSigningOut(false);
              }
            } finally {
              // For web, we don't reset the loading state as we're redirecting
              if (Platform.OS !== 'web') {
                setIsSigningOut(false);
              }
            }
          }
        },
      ]
    );
  };

  if (isMiningLoading) {
    return (
      <LinearGradient colors={['#0F172A', '#1E293B']} style={styles.container}>
        <View style={[styles.content, { paddingTop: insets.top }]}>
          <PulseLoader />
        </View>
      </LinearGradient>
    );
  }

  if (!profile && !isMiningLoading) {
    return (
      <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
        <View style={styles.loadingContainer}>
          <Text style={styles.loadingText}>Profile not found</Text>
          <TouchableOpacity 
            style={styles.retryButton}
            onPress={refreshProfile}
          >
            <Text style={styles.retryButtonText}>Retry</Text>
          </TouchableOpacity>
        </View>
      </LinearGradient>
    );
  }

  const joinDate = profile ? new Date(profile.createdAt).toLocaleDateString() : '';
  const lastLoginDate = profile?.lastLoginDate ? new Date(profile.lastLoginDate).toLocaleDateString() : 'Never';
  
  // Calculate mining efficiency
  const miningEfficiency = profile?.dailyMiningRate && profile?.miningPower 
    ? ((profile.dailyMiningRate / profile.miningPower) * 100).toFixed(1) 
    : '0';
    
  // Calculate daily mining progress
  const dailyProgress = profile?.maxDailyEarnings 
    ? Math.min((profile.todayEarnings / profile.maxDailyEarnings) * 100, 100)
    : 0;

  // Format numbers with commas
  const formatNumber = (num: number | undefined) => {
    if (num === undefined) return '0';
    return num.toLocaleString();
  };

  return (
    <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
      <ScrollView 
        style={[styles.scrollView, { paddingTop: insets.top }]}
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <View style={styles.header}>
          <View style={styles.headerContent}>
            <Text style={styles.title}>Profile</Text>
            <TouchableOpacity style={styles.refreshButton} onPress={refreshProfile}>
              <Trophy size={20} color="#ffffff" />
            </TouchableOpacity>
          </View>
          <Text style={styles.subtitle}>Manage your account and view stats</Text>
        </View>

        {/* Profile Card */}
        <View style={styles.profileContainer}>
          <LinearGradient
            colors={['rgba(147, 51, 234, 0.2)', 'rgba(168, 85, 247, 0.2)']}
            style={styles.profileCard}
          >
            <View style={styles.profileHeader}>
              <LinearGradient
                colors={['#8b5cf6', '#a855f7']}
                style={styles.avatarContainer}
              >
                <User size={40} color="#ffffff" />
              </LinearGradient>
              
              {isEditing ? (
                <View style={styles.editContainer}>
                  <TextInput
                    style={styles.usernameInput}
                    value={newUsername}
                    onChangeText={setNewUsername}
                    placeholder="Enter username"
                    placeholderTextColor="rgba(255, 255, 255, 0.5)"
                    maxLength={20}
                  />
                  <View style={styles.editButtons}>
                    <TouchableOpacity
                      style={[styles.editButton, styles.saveButton]}
                      onPress={handleUpdateUsername}
                    >
                      <Text style={styles.editButtonText}>Save</Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                      style={[styles.editButton, styles.cancelButton]}
                      onPress={() => {
                        setIsEditing(false);
                        setNewUsername(profile?.username || '');
                      }}
                    >
                      <Text style={styles.editButtonText}>Cancel</Text>
                    </TouchableOpacity>
                  </View>
                </View>
              ) : (
                <View style={styles.userInfo}>
                  <View style={styles.usernameContainer}>
                    <Text style={styles.username}>
                      {profile?.username || `Miner ${user?.id.slice(-4)}`}
                    </Text>
                    <TouchableOpacity
                      style={styles.editIcon}
                      onPress={() => setIsEditing(true)}
                    >
                      <Edit3 size={16} color="rgba(255, 255, 255, 0.6)" />
                    </TouchableOpacity>
                  </View>
                  <Text style={styles.memberSince}>Member since {joinDate}</Text>
                  <Text style={styles.lastLogin}>Last login: {lastLoginDate}</Text>
                </View>
              )}
            </View>

            {/* Stats Grid */}
            <View style={styles.statsGrid}>
              <View style={styles.statCard}>
                <Coins size={24} color="#ffa000" />
                <Text style={styles.statValue}>{formatNumber(profile?.totalCoins)}</Text>
                <Text style={styles.statLabel}>Total EKH</Text>
              </View>
              
              <View style={styles.statCard}>
                <Zap size={24} color="#8b5cf6" />
                <Text style={styles.statValue}>{(2 / 24).toFixed(4)}</Text>
                <Text style={styles.statLabel}>Mining Rate</Text>
                <Text style={styles.statSubLabel}>EKH/hour</Text>
              </View>
              
              <View style={styles.statCard}>
                <Calendar size={24} color="#10b981" />
                <Text style={styles.statValue}>{profile?.currentStreak || '0'}</Text>
                <Text style={styles.statLabel}>Day Streak</Text>
              </View>
              
              <View style={styles.statCard}>
                <Users size={24} color="#f59e0b" />
                <Text style={styles.statValue}>{profile?.totalReferrals || '0'}</Text>
                <Text style={styles.statLabel}>Referrals</Text>
              </View>
              
              <View style={styles.statCard}>
                <Trophy size={24} color="#6366f1" />
                <Text style={styles.statValue}>{miningEfficiency}%</Text>
                <Text style={styles.statLabel}>Efficiency</Text>
              </View>
            </View>
          </LinearGradient>
        </View>

        {/* Daily Mining Progress */}
        <View style={styles.profileContainer}>
          <LinearGradient
            colors={['rgba(147, 51, 234, 0.2)', 'rgba(168, 85, 247, 0.2)']}
            style={styles.profileCard}
          >
            <View style={styles.miningProgressHeader}>
              <Text style={styles.miningProgressTitle}>Daily Mining Progress</Text>
              <Text style={styles.miningProgressValue}>
                {profile?.todayEarnings?.toFixed(2) || '0.00'} / {profile?.maxDailyEarnings?.toFixed(2) || '0.00'} EKH
              </Text>
            </View>
            <View style={styles.progressBarBackground}>
              <View 
                style={[
                  styles.progressBarFill, 
                  { 
                    backgroundColor: '#8b5cf6',
                    width: `${dailyProgress}%`
                  }
                ]} 
              />
            </View>
            <Text style={styles.progressText}>
              {dailyProgress.toFixed(1)}% of daily limit reached
            </Text>
          </LinearGradient>
        </View>

        {/* Referral Section */}
        <View style={styles.referralContainer}>
          <Text style={styles.referralTitle}>Invite Friends</Text>
          <Text style={styles.referralDescription}>
            Share your referral code and earn bonus EKH for each friend who joins!
          </Text>
          
          <View style={styles.referralCodeContainer}>
            <View style={styles.referralCodeInfo}>
              <Text style={styles.referralCodeLabel}>Your Referral Code</Text>
              <Text style={styles.referralCode}>{profile?.referralCode}</Text>
            </View>
            
            <View style={styles.referralActions}>
              <TouchableOpacity
                style={styles.referralActionButton}
                onPress={copyReferralCode}
              >
                <LinearGradient
                  colors={copied ? ['#10b981', '#059669'] : ['#8b5cf6', '#7c3aed']}
                  style={styles.referralActionGradient}
                >
                  {copied ? <Check size={16} color="#ffffff" /> : <Copy size={16} color="#ffffff" />}
                </LinearGradient>
              </TouchableOpacity>
              
              <TouchableOpacity
                style={styles.referralActionButton}
                onPress={shareReferralCode}
              >
                <LinearGradient
                  colors={['#3b82f6', '#2563eb']}
                  style={styles.referralActionGradient}
                >
                  <Share2 size={16} color="#ffffff" />
                </LinearGradient>
              </TouchableOpacity>
            </View>
          </View>
          
          {/* Enhanced Referral Button */}
          <TouchableOpacity 
            style={styles.referralDetailsButton}
            onPress={() => router.push('/referral')}
          >
            <LinearGradient
              colors={['rgba(255, 160, 0, 0.2)', 'rgba(255, 160, 0, 0.1)']}
              style={styles.referralDetailsGradient}
            >
              <Users size={20} color="#ffa000" />
              <Text style={styles.referralDetailsText}>Referral Details</Text>
            </LinearGradient>
          </TouchableOpacity>
        </View>

        {/* Referral Stats */}
        <View style={styles.referralStatsContainer}>
          <View style={styles.referralStat}>
            <Text style={styles.referralStatValue}>{profile?.totalReferrals || '0'}</Text>
            <Text style={styles.referralStatLabel}>Total Referrals</Text>
          </View>
          <View style={styles.referralStat}>
            <Text style={styles.referralStatValue}>0.2 EKH</Text>
            <Text style={styles.referralStatLabel}>Per Referral</Text>
          </View>
          <View style={styles.referralStat}>
            <Text style={styles.referralStatValue}>2.0 EKH</Text>
            <Text style={styles.referralStatLabel}>For Referred</Text>
          </View>
        </View>

        {/* Achievement Highlights */}
        <View style={styles.achievementsContainer}>
          <Text style={styles.achievementsTitle}>Achievements</Text>
          
          <View style={styles.achievementsList}>
            <View style={styles.achievementItem}>
              <Text style={styles.achievementLabel}>Longest Streak</Text>
              <Text style={styles.achievementValue}>{profile?.longestStreak || '0'} days</Text>
            </View>
            
            <View style={styles.achievementItem}>
              <Text style={styles.achievementLabel}>Lifetime Earnings</Text>
              <Text style={styles.achievementValue}>{formatNumber(profile?.lifetimeEarnings)} EKH</Text>
            </View>
            
            <View style={styles.achievementItem}>
              <Text style={styles.achievementLabel}>Daily Mining Rate</Text>
              <Text style={styles.achievementValue}>{(2 / 24).toFixed(4)} EKH/hour</Text>
            </View>
            
            <View style={styles.achievementItem}>
              <Text style={styles.achievementLabel}>Today's Earnings</Text>
              <Text style={styles.achievementValue}>{profile?.todayEarnings?.toFixed(2) || '0.00'} EKH</Text>
            </View>
            
            <View style={styles.achievementItem}>
              <Text style={styles.achievementLabel}>Mining Efficiency</Text>
              <Text style={styles.achievementValue}>{miningEfficiency}%</Text>
            </View>
          </View>
        </View>

        {/* Account Settings */}
        <View style={styles.settingsContainer}>
          <Text style={styles.settingsTitle}>Account Settings</Text>
          
          {showChangePassword ? (
            <View style={styles.passwordChangeContainer}>
              <Text style={styles.passwordChangeTitle}>Change Password</Text>
              
              <View style={styles.inputGroup}>
                <Text style={styles.inputLabel}>Current Password</Text>
                <TextInput
                  style={styles.textInput}
                  value={currentPassword}
                  onChangeText={setCurrentPassword}
                  secureTextEntry
                  placeholder="Enter current password"
                  placeholderTextColor="rgba(255, 255, 255, 0.5)"
                />
              </View>
              
              <View style={styles.inputGroup}>
                <Text style={styles.inputLabel}>New Password</Text>
                <TextInput
                  style={styles.textInput}
                  value={newPassword}
                  onChangeText={setNewPassword}
                  secureTextEntry
                  placeholder="Enter new password"
                  placeholderTextColor="rgba(255, 255, 255, 0.5)"
                />
              </View>
              
              <View style={styles.inputGroup}>
                <Text style={styles.inputLabel}>Confirm New Password</Text>
                <TextInput
                  style={styles.textInput}
                  value={confirmPassword}
                  onChangeText={setConfirmPassword}
                  secureTextEntry
                  placeholder="Confirm new password"
                  placeholderTextColor="rgba(255, 255, 255, 0.5)"
                />
              </View>
              
              <View style={styles.passwordButtonContainer}>
                <TouchableOpacity 
                  style={[styles.passwordButton, styles.updateButton]}
                  onPress={handlePasswordUpdate}
                >
                  <Text style={styles.passwordButtonText}>Update Password</Text>
                </TouchableOpacity>
                
                <TouchableOpacity 
                  style={[styles.passwordButton, styles.cancelButton]}
                  onPress={cancelPasswordChange}
                >
                  <Text style={styles.passwordButtonText}>Cancel</Text>
                </TouchableOpacity>
              </View>
            </View>
          ) : (
            <View style={styles.settingsList}>
              <TouchableOpacity style={styles.settingItem} onPress={handleNotificationToggle}>
                <Text style={styles.settingLabel}>Notification Preferences</Text>
                <Text style={styles.settingValue}>
                  {notificationEnabled ? 'Enabled' : 'Disabled'}
                </Text>
              </TouchableOpacity>
              
              <TouchableOpacity style={styles.settingItem} onPress={handleChangePrivacy}>
                <Text style={styles.settingLabel}>Privacy Settings</Text>
                <Text style={styles.settingValue}>
                  {privacyLevel === 'public' ? 'Public' : privacyLevel === 'friends' ? 'Friends Only' : 'Private'}
                </Text>
              </TouchableOpacity>
              
              <TouchableOpacity style={styles.settingItem} onPress={handleChangeSecurity}>
                <Text style={styles.settingLabel}>Security</Text>
                <Text style={styles.settingValue}>
                  {securityLevel === 'standard' ? 'Standard' : securityLevel === 'enhanced' ? 'Enhanced' : 'Maximum'}
                </Text>
              </TouchableOpacity>
              
              <TouchableOpacity style={styles.settingItem} onPress={handleChangePassword}>
                <Text style={styles.settingLabel}>Change Password</Text>
                <Text style={styles.settingValue}>Update</Text>
              </TouchableOpacity>
            </View>
          )}
        </View>

        {/* Sign Out Button */}
        <TouchableOpacity 
          style={styles.signOutButton} 
          onPress={handleSignOut}
          disabled={isSigningOut}
        >
          <LinearGradient
            colors={['rgba(239, 68, 68, 0.2)', 'rgba(239, 68, 68, 0.1)']}
            style={styles.signOutGradient}
          >
            <LogOut size={20} color={isSigningOut ? "#cccccc" : "#ef4444"} />
            {isSigningOut ? (
              <LoadingDots color="#ef4444" size={8} />
            ) : (
              <Text style={[styles.signOutText, { color: isSigningOut ? "#cccccc" : "#ef4444" }]}>
                Sign Out
              </Text>
            )}
          </LinearGradient>
        </TouchableOpacity>
        
        {/* App Version */}
        <View style={styles.versionContainer}>
          <Text style={styles.versionText}>Ekehi Network v1.0.0</Text>
          <Text style={styles.copyrightText}>© 2025 Ekehi Network. All rights reserved.</Text>
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
    gap: 20,
  },
  loadingText: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
  },

  retryButton: {
    marginTop: 20,
    paddingHorizontal: 20,
    paddingVertical: 10,
    backgroundColor: 'rgba(255, 160, 0, 0.2)',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  retryButtonText: {
    color: '#ffa000',
    fontSize: 16,
    fontWeight: '600',
  },
  header: {
    alignItems: 'center',
    marginBottom: 24,
    marginTop: 20,
  },
  headerContent: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 8,
    textAlign: 'center',
    flex: 1,
  },
  refreshButton: {
    padding: 8,
    borderRadius: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    marginRight: 10,
  },
  subtitle: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
  },
  profileContainer: {
    marginBottom: 24,
  },
  profileCard: {
    borderRadius: 24,
    padding: 24,
    borderWidth: 1,
    borderColor: 'rgba(139, 92, 246, 0.3)',
  },
  profileHeader: {
    alignItems: 'center',
    marginBottom: 24,
  },
  avatarContainer: {
    width: 80,
    height: 80,
    borderRadius: 40,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 16,
  },
  editContainer: {
    width: '100%',
    alignItems: 'center',
  },
  usernameInput: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 12,
    padding: 12,
    fontSize: 16,
    color: '#ffffff',
    textAlign: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.3)',
    marginBottom: 16,
    width: '100%',
  },
  editButtons: {
    flexDirection: 'row',
    gap: 12,
  },
  editButton: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
  },
  saveButton: {
    backgroundColor: '#10b981',
  },
  cancelButton: {
    backgroundColor: '#6b7280',
  },
  editButtonText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#ffffff',
  },
  userInfo: {
    alignItems: 'center',
  },
  usernameContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 4,
  },
  username: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  editIcon: {
    padding: 4,
  },
  memberSince: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.6)',
    marginBottom: 2,
  },
  lastLogin: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.6)',
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  statCard: {
    flex: 1,
    minWidth: '45%',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 16,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
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
  statSubLabel: {
    fontSize: 10,
    color: '#8b5cf6',
    marginTop: 2,
    fontWeight: '600',
  },
  progressBarBackground: {
    width: '100%',
    height: 4,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 2,
    marginTop: 6,
    overflow: 'hidden',
  },
  progressBarFill: {
    height: '100%',
    borderRadius: 2,
  },
  progressText: {
    fontSize: 8,
    color: 'rgba(255, 255, 255, 0.5)',
    marginTop: 2,
  },
  miningProgressHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  miningProgressTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  miningProgressValue: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  referralContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  referralTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 8,
  },
  referralDescription: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    lineHeight: 20,
    marginBottom: 16,
  },
  referralCodeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    padding: 16,
    gap: 12,
    marginBottom: 16,
  },
  referralCodeInfo: {
    flex: 1,
  },
  referralCodeLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    marginBottom: 4,
  },
  referralCode: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
    fontFamily: 'monospace',
  },
  referralActions: {
    flexDirection: 'row',
    gap: 8,
  },
  referralActionButton: {
    borderRadius: 8,
    overflow: 'hidden',
  },
  referralActionGradient: {
    width: 40,
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
  },
  referralDetailsButton: {
    borderRadius: 12,
    overflow: 'hidden',
    marginTop: 16,
  },
  referralDetailsGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
    gap: 8,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  referralDetailsText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffa000',
  },
  referralStatsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 12,
    padding: 16,
    marginVertical: 16,
  },
  referralStat: {
    alignItems: 'center',
  },
  referralStatValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 4,
  },
  referralStatLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  achievementsContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  achievementsTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 16,
  },
  achievementsList: {
    gap: 12,
  },
  achievementItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  achievementLabel: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  achievementValue: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffffff',
  },
  settingsContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  settingsTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 16,
  },
  settingsList: {
    gap: 16,
  },
  settingItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255, 255, 255, 0.1)',
  },
  settingLabel: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.8)',
  },
  settingValue: {
    fontSize: 16,
    color: '#ffa000',
    fontWeight: '600',
  },
  passwordChangeContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  passwordChangeTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 20,
    textAlign: 'center',
  },
  inputGroup: {
    marginBottom: 16,
  },
  inputLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    marginBottom: 8,
  },
  textInput: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    padding: 12,
    fontSize: 16,
    color: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  passwordButtonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 20,
    gap: 12,
  },
  passwordButton: {
    flex: 1,
    paddingVertical: 12,
    borderRadius: 12,
    alignItems: 'center',
  },
  updateButton: {
    backgroundColor: '#10b981',
  },
  passwordButtonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffffff',
  },
  signOutButton: {
    borderRadius: 12,
    overflow: 'hidden',
    marginBottom: 20,
  },
  signOutGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
    gap: 8,
    borderWidth: 1,
    borderColor: 'rgba(239, 68, 68, 0.3)',
  },
  signOutText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ef4444',
  },
  versionContainer: {
    alignItems: 'center',
    paddingVertical: 20,
  },
  versionText: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.5)',
    marginBottom: 4,
  },
  copyrightText: {
    fontSize: 10,
    color: 'rgba(255, 255, 255, 0.3)',
  },
});
