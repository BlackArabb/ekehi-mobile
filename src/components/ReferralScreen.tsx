import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, TextInput, Alert, ScrollView, Clipboard, ActivityIndicator } from 'react-native';
import { useReferral } from '@/contexts/ReferralContext';
import { useAuth } from '@/contexts/AuthContext';
import { Share2, Users, Gift, Copy, Coins } from 'lucide-react-native';

const ReferralScreen = () => {
  const { 
    referralCode, 
    totalReferrals, 
    referredBy, 
    isLoading, 
    claimReferral, 
    generateReferralLink, 
    getReferralHistory 
  } = useReferral();
  const { user } = useAuth();
  const [referralCodeInput, setReferralCodeInput] = useState('');
  const [referralHistory, setReferralHistory] = useState<any[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);

  useEffect(() => {
    loadReferralHistory();
  }, []);

  const loadReferralHistory = async () => {
    if (!user) return;
    
    setLoadingHistory(true);
    try {
      const history = await getReferralHistory();
      setReferralHistory(history);
    } catch (error) {
      console.error('Failed to load referral history:', error);
    } finally {
      setLoadingHistory(false);
    }
  };

  const handleClaimReferral = async () => {
    if (!referralCodeInput.trim()) {
      Alert.alert('Error', 'Please enter a referral code');
      return;
    }

    try {
      const result = await claimReferral(referralCodeInput.trim());
      if (result.success) {
        Alert.alert('Success', result.message);
        setReferralCodeInput('');
      } else {
        Alert.alert('Error', result.message);
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to claim referral code');
    }
  };

  const copyToClipboard = async () => {
    if (referralCode) {
      await Clipboard.setString(generateReferralLink());
      Alert.alert('Copied', 'Referral link copied to clipboard');
    }
  };

  const shareReferralLink = async () => {
    if (referralCode) {
      // In a real app, you would use a sharing library here
      Alert.alert('Share', `Share this referral link: ${generateReferralLink()}`);
    }
  };

  if (isLoading) {
    return (
      <View style={styles.container}>
        <ActivityIndicator size="large" color="#ffa000" />
        <Text style={styles.loadingText}>Loading referral information...</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <View style={styles.card}>
        <View style={styles.header}>
          <Share2 size={24} color="#ffa000" />
          <Text style={styles.title}>Referral Program</Text>
        </View>
        <Text style={styles.description}>
          Invite friends to join Ekehi Network and earn rewards!
        </Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.sectionTitle}>Your Referral Code</Text>
        <View style={styles.referralCodeContainer}>
          <Text style={styles.referralCode}>{referralCode || 'Generating...'}</Text>
          <TouchableOpacity onPress={copyToClipboard} style={styles.copyButton}>
            <Copy size={20} color="#fff" />
          </TouchableOpacity>
        </View>
        <TouchableOpacity 
          style={styles.shareButton}
          onPress={shareReferralLink}
          disabled={isLoading}
        >
          <Text style={styles.buttonText}>Share Referral Link</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.card}>
        <Text style={styles.sectionTitle}>Claim Referral</Text>
        <Text style={styles.description}>
          Enter a referral code you received from a friend to get 2 EKH!
        </Text>
        <TextInput
          style={styles.input}
          placeholder="Enter referral code"
          value={referralCodeInput}
          onChangeText={setReferralCodeInput}
        />
        <TouchableOpacity 
          style={styles.claimButton}
          onPress={handleClaimReferral}
          disabled={isLoading}
        >
          <Text style={styles.buttonText}>Claim 2 EKH</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.card}>
        <View style={styles.statsContainer}>
          <View style={styles.statItem}>
            <Users size={24} color="#ffa000" />
            <Text style={styles.statValue}>{totalReferrals}</Text>
            <Text style={styles.statLabel}>Referrals</Text>
          </View>
          <View style={styles.statItem}>
            <Gift size={24} color="#ffa000" />
            <Text style={styles.statValue}>1.0 EKH</Text>
            <Text style={styles.statLabel}>Per Referral</Text>
          </View>
          <View style={styles.statItem}>
            <Coins size={24} color="#ffa000" />
            <Text style={styles.statValue}>0.5 EKH</Text>
            <Text style={styles.statLabel}>For You</Text>
          </View>
        </View>
      </View>

      {referredBy && (
        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Referred By</Text>
          <Text style={styles.description}>
            You were referred by another user
          </Text>
        </View>
      )}

      <View style={styles.card}>
        <View style={styles.header}>
          <Users size={20} color="#ffa000" />
          <Text style={styles.sectionTitle}>Your Referrals</Text>
        </View>
        {loadingHistory ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="small" color="#ffa000" />
            <Text style={styles.loadingText}>Loading referral history...</Text>
          </View>
        ) : referralHistory.length > 0 ? (
          referralHistory.map((referral) => (
            <View key={referral.id} style={styles.referralItem}>
              <Text style={styles.referralName}>{referral.username}</Text>
              <Text style={styles.referralDate}>
                Joined: {new Date(referral.joinedDate).toLocaleDateString()}
              </Text>
            </View>
          ))
        ) : (
          <Text style={styles.description}>No referrals yet. Share your code to get started!</Text>
        )}
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0f0f1a',
    padding: 16,
  },
  card: {
    marginBottom: 16,
    backgroundColor: '#1a1a2e',
    borderRadius: 12,
    padding: 16,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
    marginLeft: 8,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 8,
  },
  description: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    marginBottom: 16,
  },
  referralCodeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: 'rgba(255, 160, 0, 0.1)',
    padding: 16,
    borderRadius: 8,
    marginBottom: 16,
  },
  referralCode: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffa000',
    flex: 1,
  },
  copyButton: {
    backgroundColor: '#ffa000',
    padding: 8,
    borderRadius: 6,
  },
  shareButton: {
    backgroundColor: '#ffa000',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
    marginBottom: 8,
  },
  input: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 8,
    padding: 12,
    color: '#fff',
    marginBottom: 16,
  },
  claimButton: {
    backgroundColor: '#1e88e5',
    padding: 16,
    borderRadius: 8,
    alignItems: 'center',
  },
  buttonText: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: 16,
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  statItem: {
    alignItems: 'center',
  },
  statValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#fff',
    marginVertical: 4,
  },
  statLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  referralItem: {
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255, 255, 255, 0.1)',
  },
  referralName: {
    fontSize: 16,
    color: '#fff',
    fontWeight: '500',
  },
  referralDate: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.5)',
  },
  loadingContainer: {
    alignItems: 'center',
    padding: 16,
  },
  loadingText: {
    color: '#fff',
    marginTop: 8,
  },
});

export default ReferralScreen;