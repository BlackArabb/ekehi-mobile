import React, { useMemo } from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Zap } from 'lucide-react-native';
import { usePresale } from '@/contexts/PresaleContext';
import type { UserProfile } from '@/types';

interface AutoMiningInfoProps {
  profile: UserProfile | null;
}

const AutoMiningInfo: React.FC<AutoMiningInfoProps> = ({ profile }) => {
  const { 
    isAutoMiningEligible, 
    calculateAutoMiningRate, 
    autoMiningMinPurchase, 
    purchases,
    maxMiningRatePurchaseAmount,
    maxGeneralPurchaseAmount,
    maxMiningRate
  } = usePresale();

  // Calculate total amount spent
  const totalSpent = useMemo(() => {
    return purchases.reduce((sum, purchase) => 
      purchase.status === 'completed' ? sum + purchase.amountUsd : sum, 0
    );
  }, [purchases]);

  // Calculate current auto mining rate
  const currentRate = profile?.coinsPerSecond || 0;
  
  // Calculate potential rate if user made more purchases
  const potentialRate = useMemo(() => {
    return calculateAutoMiningRate();
  }, [calculateAutoMiningRate]);

  // Check if user is eligible for auto mining
  const eligible = useMemo(() => {
    return isAutoMiningEligible();
  }, [isAutoMiningEligible]);

  // Check if user has reached maximum general purchase amount
  const hasReachedMaxGeneralPurchase = useMemo(() => {
    return totalSpent >= maxGeneralPurchaseAmount;
  }, [totalSpent, maxGeneralPurchaseAmount]);

  // Calculate remaining amount until max general purchase limit
  const remainingToMaxGeneralPurchase = useMemo(() => {
    return Math.max(0, maxGeneralPurchaseAmount - totalSpent);
  }, [totalSpent, maxGeneralPurchaseAmount]);

  // Calculate effective spent for mining rate (capped at maxMiningRatePurchaseAmount)
  const effectiveSpentForRate = useMemo(() => {
    return Math.min(totalSpent, maxMiningRatePurchaseAmount);
  }, [totalSpent, maxMiningRatePurchaseAmount]);

  // Check if user has reached maximum mining rate purchase amount
  const hasReachedMaxRatePurchase = useMemo(() => {
    return totalSpent >= maxMiningRatePurchaseAmount;
  }, [totalSpent, maxMiningRatePurchaseAmount]);

  return (
    <View style={styles.container}>
      <View style={styles.sectionHeader}>
        <Zap size={20} color="#10b981" />
        <Text style={styles.sectionTitle}>Auto Mining Status</Text>
      </View>
      
      <View style={styles.content}>
        {eligible ? (
          <View style={styles.statusContainer}>
            <LinearGradient
              colors={['rgba(16, 185, 129, 0.2)', 'rgba(16, 185, 129, 0.1)']}
              style={styles.statusCard}
            >
              <View style={styles.statusIconContainer}>
                <Zap size={24} color="#10b981" />
              </View>
              <Text style={styles.statusLabel}>Active</Text>
              <Text style={styles.statusValue}>{currentRate.toFixed(4)} EKH/sec</Text>
              <Text style={styles.statusDescription}>Current Rate</Text>
              {hasReachedMaxRatePurchase && (
                <Text style={styles.limitInfo}>Rate capped at ${maxMiningRatePurchaseAmount}</Text>
              )}
            </LinearGradient>
          </View>
        ) : (
          <View style={styles.statusContainer}>
            <LinearGradient
              colors={['rgba(255, 160, 0, 0.2)', 'rgba(255, 160, 0, 0.1)']}
              style={styles.statusCard}
            >
              <View style={styles.statusIconContainer}>
                <Zap size={24} color="#ffa000" />
              </View>
              <Text style={styles.statusLabel}>Not Eligible</Text>
              <Text style={styles.statusValue}>${totalSpent.toFixed(2)} / ${autoMiningMinPurchase}</Text>
              <Text style={styles.statusDescription}>Spent / Required</Text>
            </LinearGradient>
          </View>
        )}
        
        {totalSpent > 0 && (
          <View style={styles.statsContainer}>
            <View style={styles.statItem}>
              <Text style={styles.statValue}>${totalSpent.toFixed(2)}</Text>
              <Text style={styles.statLabel}>Total Spent</Text>
              {hasReachedMaxGeneralPurchase && (
                <Text style={styles.limitReached}>Max Purchase Reached</Text>
              )}
            </View>
            <View style={styles.statItem}>
              <Text style={styles.statValue}>{potentialRate.toFixed(4)}</Text>
              <Text style={styles.statLabel}>Potential Rate</Text>
              {currentRate >= maxMiningRate && (
                <Text style={styles.limitReached}>Max Rate Reached</Text>
              )}
            </View>
          </View>
        )}
        
        {/* Maximum Limits Information */}
        <View style={styles.limitsContainer}>
          <Text style={styles.limitsTitle}>Purchase Limits</Text>
          <View style={styles.limitItem}>
            <Text style={styles.limitLabel}>Max Rate Purchase:</Text>
            <Text style={styles.limitValue}>${maxMiningRatePurchaseAmount.toLocaleString()}</Text>
          </View>
          <View style={styles.limitItem}>
            <Text style={styles.limitLabel}>Max General Purchase:</Text>
            <Text style={styles.limitValue}>${maxGeneralPurchaseAmount.toLocaleString()}</Text>
          </View>
          <View style={styles.limitItem}>
            <Text style={styles.limitLabel}>Max Mining Rate:</Text>
            <Text style={styles.limitValue}>{maxMiningRate} EKH/sec</Text>
          </View>
          {!hasReachedMaxGeneralPurchase && (
            <View style={styles.limitItem}>
              <Text style={styles.limitLabel}>Remaining to Max:</Text>
              <Text style={styles.limitValue}>${remainingToMaxGeneralPurchase.toFixed(2)}</Text>
            </View>
          )}
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  content: {
    gap: 16,
  },
  statusContainer: {
    borderRadius: 16,
    overflow: 'hidden',
  },
  statusCard: {
    borderRadius: 16,
    padding: 16,
    alignItems: 'center',
  },
  statusIconContainer: {
    width: 50,
    height: 50,
    borderRadius: 25,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
  },
  statusLabel: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
    marginBottom: 4,
  },
  statusValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 4,
  },
  statusDescription: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.6)',
  },
  limitInfo: {
    fontSize: 12,
    color: '#ffa000',
    marginTop: 4,
    fontStyle: 'italic',
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 12,
    padding: 16,
  },
  statItem: {
    alignItems: 'center',
  },
  statValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 4,
  },
  statLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  limitReached: {
    fontSize: 10,
    color: '#10b981',
    fontWeight: 'bold',
    marginTop: 2,
  },
  limitsContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 12,
    padding: 16,
  },
  limitsTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 12,
    textAlign: 'center',
  },
  limitItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  limitLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  limitValue: {
    fontSize: 14,
    color: '#ffffff',
    fontWeight: '600',
  },
});

export default AutoMiningInfo;