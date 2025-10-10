import { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Alert, TextInput } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
// @ts-ignore - TypeScript declaration issue with lucide-react-native
import { Store, DollarSign, Zap, Clock, CheckCircle, TrendingUp, Lock, Gift, BarChart3, Users, Award } from 'lucide-react-native';
import { usePresale } from '@/contexts/PresaleContext';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'expo-router';
import { useMining } from '@/contexts/MiningContext';
import LoadingDots from '@/components/LoadingDots';

export default function PresalePage() {
  const router = useRouter();
  const { user } = useAuth();
  const { profile } = useMining();
  const { isActive, tokenPrice, minPurchase, purchases, isLoading, purchaseTokens, fetchPurchases } = usePresale();
  const insets = useSafeAreaInsets();
  const [purchaseAmount, setPurchaseAmount] = useState('');
  const [selectedPayment, setSelectedPayment] = useState('crypto');

  useEffect(() => {
    if (!user) {
      // Add a small delay to ensure router is ready
      setTimeout(() => {
        router.replace('/');
      }, 100);
      return;
    }
    fetchPurchases();
  }, [user]);

  const handlePurchase = async () => {
    const amount = parseFloat(purchaseAmount);
    
    if (!amount || amount < minPurchase) {
      Alert.alert('Invalid Amount', `Minimum purchase is $${minPurchase}`);
      return;
    }

    try {
      const result = await purchaseTokens(amount, selectedPayment);
      if (result.success) {
        Alert.alert('Success!', result.message);
        setPurchaseAmount('');
      } else {
        Alert.alert('Error', result.message);
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to process purchase. Please try again.');
    }
  };

  const calculateTokens = (usdAmount: number) => {
    if (!tokenPrice || tokenPrice <= 0) return 0;
    return usdAmount / tokenPrice;
  };

  // Safe calculation of total purchased tokens
  const totalPurchased = Array.isArray(purchases) ? purchases.reduce((sum, purchase) => 
    purchase && purchase.status === 'completed' ? sum + (purchase.tokensAmount || 0) : sum, 0
  ) : 0;

  // Safe calculation of total spent amount
  const totalSpent = Array.isArray(purchases) ? purchases.reduce((sum, purchase) => 
    purchase && purchase.status === 'completed' ? sum + (purchase.amountUsd || 0) : sum, 0
  ) : 0;

  const autoMiningRate = totalPurchased > 0 ? (totalPurchased / 10000) : 0;
  
  // Calculate progress percentage for presale with safety checks
  const progressPercentage = totalPurchased > 0 && 100000 > 0 ? 
    Math.min(100, (totalPurchased / 100000) * 100) : 0; // Assuming 100,000 token goal
  
  // Pre-calculate the minimum purchase placeholder to avoid template literals in TextInput
  const minPurchasePlaceholder = minPurchase > 0 ? `Min $${minPurchase}` : 'Min $10';

  return (
    <LinearGradient colors={['#1a1a2e', '#16213e', '#0f3460']} style={styles.container}>
      <ScrollView 
        style={[styles.scrollView, { paddingTop: insets.top }]}
        contentContainerStyle={styles.content}
        showsVerticalScrollIndicator={false}
      >
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.title}>Token Presale</Text>
          <Text style={styles.subtitle}>Get early access to EKH tokens</Text>
        </View>

        {/* Presale Status */}
        <View style={styles.statusContainer}>
          <View style={styles.statusCard}>
            <View style={[styles.statusIndicator, { backgroundColor: isActive ? '#10b981' : '#ef4444' }]} />
            <Text style={styles.statusText}>
              {isActive ? 'Presale Active' : 'Presale Ended'}
            </Text>
          </View>
        </View>

        {/* Presale Progress */}
        <View style={styles.progressContainer}>
          <View style={styles.progressHeader}>
            <Text style={styles.progressTitle}>Presale Progress</Text>
            <Text style={styles.progressText}>{isNaN(progressPercentage) ? '0.0' : progressPercentage.toFixed(1)}%</Text>
          </View>
          <View style={styles.progressBar}>
            <View 
              style={[
                styles.progressFill, 
                {
                  width: `${isNaN(progressPercentage) ? 0 : progressPercentage}%`
                }
              ]} 
            />
          </View>
          <View style={styles.progressStats}>
            <Text style={styles.progressStat}>{isNaN(totalPurchased) ? '0' : totalPurchased.toLocaleString()} EKH</Text>
            <Text style={styles.progressStat}>100,000 EKH Goal</Text>
          </View>
        </View>

        {/* Token Info */}
        <View style={styles.tokenInfoContainer}>
          <View style={styles.tokenInfoCard}>
            <DollarSign size={24} color="#ffa000" />
            <View style={styles.tokenInfoText}>
              <Text style={styles.tokenInfoValue}>${isNaN(tokenPrice) ? '0.1000' : tokenPrice.toFixed(4)}</Text>
              <Text style={styles.tokenInfoLabel}>per EKH</Text>
            </View>
          </View>
          
          <View style={styles.tokenInfoCard}>
            <Store size={24} color="#3b82f6" />
            <View style={styles.tokenInfoText}>
              <Text style={styles.tokenInfoValue}>${isNaN(minPurchase) ? '10' : minPurchase}</Text>
              <Text style={styles.tokenInfoLabel}>min purchase</Text>
            </View>
          </View>
        </View>

        {/* Auto Mining Benefit */}
        <View style={styles.benefitContainer}>
          <LinearGradient
            colors={['rgba(16, 185, 129, 0.2)', 'rgba(16, 185, 129, 0.1)']}
            style={styles.benefitCard}
          >
            <Zap size={32} color="#10b981" />
            <View style={styles.benefitContent}>
              <Text style={styles.benefitTitle}>Auto Mining Unlocked!</Text>
              <Text style={styles.benefitText}>
                Purchasing tokens unlocks passive income generation at 1 EKH/second per 10,000 tokens
              </Text>
              {profile && profile.coinsPerSecond && profile.coinsPerSecond > 0 && (
                <Text style={styles.currentRate}>
                  Current rate: {isNaN(profile.coinsPerSecond) ? '0.00' : profile.coinsPerSecond.toFixed(2)} EKH/second
                </Text>
              )}
            </View>
          </LinearGradient>
        </View>

        {/* Purchase Form */}
        {isActive && (
          <View style={styles.purchaseContainer}>
            <Text style={styles.purchaseTitle}>Purchase Tokens</Text>
            
            <View style={styles.inputContainer}>
              <Text style={styles.inputLabel}>Amount (USD)</Text>
              <TextInput
                style={styles.input}
                value={purchaseAmount}
                onChangeText={setPurchaseAmount}
                placeholder={minPurchasePlaceholder}
                placeholderTextColor="rgba(255, 255, 255, 0.5)"
                keyboardType="numeric"
              />
              {purchaseAmount && (
                <Text style={styles.tokenCalculation}>
                  ≈ {calculateTokens(parseFloat(purchaseAmount) || 0).toLocaleString()} EKH
                </Text>
              )}
            </View>

            {/* Quick Amount Buttons */}
            <View style={styles.quickAmountContainer}>
              <Text style={styles.quickAmountLabel}>Quick Amounts</Text>
              <View style={styles.quickAmountButtons}>
                {[10, 25, 50, 100].map((amount) => (
                  <TouchableOpacity
                    key={amount}
                    style={styles.quickAmountButton}
                    onPress={() => setPurchaseAmount(amount.toString())}
                  >
                    <Text style={styles.quickAmountText}>${amount}</Text>
                  </TouchableOpacity>
                ))}
              </View>
            </View>

            {/* Payment Method */}
            <View style={styles.paymentContainer}>
              <Text style={styles.paymentLabel}>Payment Method</Text>
              <View style={styles.paymentOptions}>
                <TouchableOpacity
                  style={[
                    styles.paymentOption,
                    selectedPayment === 'crypto' && styles.paymentOptionSelected
                  ]}
                  onPress={() => setSelectedPayment('crypto')}
                >
                  <Text style={[
                    styles.paymentOptionText,
                    selectedPayment === 'crypto' && styles.paymentOptionTextSelected
                  ]}>
                    Cryptocurrency
                  </Text>
                </TouchableOpacity>
                
                <TouchableOpacity
                  style={[
                    styles.paymentOption,
                    selectedPayment === 'card' && styles.paymentOptionSelected
                  ]}
                  onPress={() => setSelectedPayment('card')}
                >
                  <Text style={[
                    styles.paymentOptionText,
                    selectedPayment === 'card' && styles.paymentOptionTextSelected
                  ]}>
                    Credit Card
                  </Text>
                </TouchableOpacity>
              </View>
            </View>

            <TouchableOpacity
              style={styles.purchaseButton}
              onPress={handlePurchase}
              disabled={isLoading || !purchaseAmount}
            >
              <LinearGradient
                colors={['#ffa000', '#ff8f00']}
                style={styles.purchaseButtonGradient}
              >
                <Store size={20} color="#ffffff" />
                {isLoading ? (
                  <LoadingDots color="#ffffff" size={8} />
                ) : (
                  <Text style={styles.purchaseButtonText}>
                    Purchase Tokens
                  </Text>
                )}
              </LinearGradient>
            </TouchableOpacity>
          </View>
        )}

        {/* Your Purchases */}
        {totalPurchased > 0 && (
          <View style={styles.purchasesContainer}>
            <Text style={styles.purchasesTitle}>Your Purchases</Text>
            
            <View style={styles.purchaseStats}>
              <View style={styles.purchaseStatCard}>
                <Text style={styles.purchaseStatValue}>{isNaN(totalPurchased) ? '0' : totalPurchased.toLocaleString()}</Text>
                <Text style={styles.purchaseStatLabel}>EKH Tokens</Text>
              </View>
              
              <View style={styles.purchaseStatCard}>
                <Text style={styles.purchaseStatValue}>${isNaN(totalSpent) ? '0.00' : totalSpent.toFixed(2)}</Text>
                <Text style={styles.purchaseStatLabel}>Total Spent</Text>
              </View>
              
              <View style={styles.purchaseStatCard}>
                <Text style={styles.purchaseStatValue}>{isNaN(autoMiningRate) ? '0.00' : autoMiningRate.toFixed(2)}/s</Text>
                <Text style={styles.purchaseStatLabel}>Auto Mining</Text>
              </View>
            </View>

            {/* Recent Purchases */}
            <View style={styles.recentPurchases}>
              {Array.isArray(purchases) && purchases.slice(0, 3).map((purchase, index) => (
                <View key={index} style={styles.purchaseItem}>
                  <View style={styles.purchaseItemLeft}>
                    <Text style={styles.purchaseItemTokens}>
                      {purchase && purchase.tokensAmount ? purchase.tokensAmount.toLocaleString() : '0'} EKH
                    </Text>
                    <Text style={styles.purchaseItemDate}>
                      {purchase && purchase.createdAt ? new Date(purchase.createdAt).toLocaleDateString() : 'N/A'}
                    </Text>
                  </View>
                  
                  <View style={styles.purchaseItemRight}>
                    <Text style={styles.purchaseItemAmount}>
                      ${purchase && purchase.amountUsd ? purchase.amountUsd.toFixed(2) : '0.00'}
                    </Text>
                    <View style={styles.purchaseItemStatus}>
                      {purchase && purchase.status === 'completed' ? (
                        <CheckCircle size={16} color="#10b981" />
                      ) : (
                        <Clock size={16} color="#f59e0b" />
                      )}
                      <Text style={[
                        styles.purchaseItemStatusText,
                        { color: purchase && purchase.status === 'completed' ? '#10b981' : '#f59e0b' }
                      ]}>
                        {purchase && purchase.status ? purchase.status : 'pending'}
                      </Text>
                    </View>
                  </View>
                </View>
              ))}
            </View>
          </View>
        )}

        {/* Benefits */}
        <View style={styles.benefitsContainer}>
          <Text style={styles.benefitsTitle}>Presale Benefits</Text>
          
          <View style={styles.benefitsList}>
            <View style={styles.benefitItem}>
              <TrendingUp size={20} color="#10b981" />
              <View>
                <Text style={styles.benefitItemText}>Early access pricing</Text>
                <Text style={styles.benefitItemSubtext}>Save up to 40% compared to public sale</Text>
              </View>
            </View>
            
            <View style={styles.benefitItem}>
              <Zap size={20} color="#ffa000" />
              <View>
                <Text style={styles.benefitItemText}>Unlock auto mining feature</Text>
                <Text style={styles.benefitItemSubtext}>Generate passive income 24/7</Text>
              </View>
            </View>
            
            <View style={styles.benefitItem}>
              <Gift size={20} color="#3b82f6" />
              <View>
                <Text style={styles.benefitItemText}>Exclusive bonuses</Text>
                <Text style={styles.benefitItemSubtext}>Special rewards for early supporters</Text>
              </View>
            </View>
            
            <View style={styles.benefitItem}>
              <BarChart3 size={20} color="#8b5cf6" />
              <View>
                <Text style={styles.benefitItemText}>Priority access</Text>
                <Text style={styles.benefitItemSubtext}>First access to new platform features</Text>
              </View>
            </View>
            
            <View style={styles.benefitItem}>
              <Users size={20} color="#ec4899" />
              <View>
                <Text style={styles.benefitItemText}>Community access</Text>
                <Text style={styles.benefitItemSubtext}>Join our exclusive presale community</Text>
              </View>
            </View>
            
            <View style={styles.benefitItem}>
              <Award size={20} color="#f59e0b" />
              <View>
                <Text style={styles.benefitItemText}>VIP status</Text>
                <Text style={styles.benefitItemSubtext}>Special recognition in our ecosystem</Text>
              </View>
            </View>
          </View>
        </View>
        
        {/* Presale Info */}
        <View style={styles.infoContainer}>
          <View style={styles.infoCard}>
            <Lock size={24} color="#6b7280" />
            <Text style={styles.infoText}>
              All transactions are securely processed and your tokens will be available in your wallet immediately after confirmation.
            </Text>
          </View>
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
  statusContainer: {
    alignItems: 'center',
    marginBottom: 24,
  },
  statusCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    gap: 8,
  },
  statusIndicator: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
  statusText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#ffffff',
  },
  progressContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  progressHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  progressTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  progressText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffa000',
  },
  progressBar: {
    height: 12,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 6,
    overflow: 'hidden',
    marginBottom: 8,
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#ffa000',
    borderRadius: 6,
  },
  progressStats: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  progressStat: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  tokenInfoContainer: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 24,
  },
  tokenInfoCard: {
    flex: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 16,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  tokenInfoText: {
    flex: 1,
  },
  tokenInfoValue: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  tokenInfoLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
  },
  benefitContainer: {
    marginBottom: 24,
  },
  benefitCard: {
    borderRadius: 16,
    padding: 16,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
    borderWidth: 1,
    borderColor: 'rgba(16, 185, 129, 0.3)',
  },
  benefitContent: {
    flex: 1,
  },
  benefitTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 4,
  },
  benefitText: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    lineHeight: 20,
    marginBottom: 8,
  },
  currentRate: {
    fontSize: 14,
    color: '#10b981',
    fontWeight: '600',
  },
  purchaseContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  purchaseTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 16,
  },
  inputContainer: {
    marginBottom: 16,
  },
  inputLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    marginBottom: 8,
  },
  input: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    padding: 16,
    fontSize: 16,
    color: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  tokenCalculation: {
    fontSize: 14,
    color: '#ffa000',
    marginTop: 8,
    fontWeight: '600',
  },
  quickAmountContainer: {
    marginBottom: 16,
  },
  quickAmountLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    marginBottom: 8,
  },
  quickAmountButtons: {
    flexDirection: 'row',
    gap: 8,
  },
  quickAmountButton: {
    flex: 1,
    backgroundColor: 'rgba(255, 160, 0, 0.2)',
    borderRadius: 8,
    paddingVertical: 10,
    alignItems: 'center',
  },
  quickAmountText: {
    fontSize: 14,
    color: '#ffa000',
    fontWeight: '600',
  },
  paymentContainer: {
    marginBottom: 20,
  },
  paymentLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    marginBottom: 12,
  },
  paymentOptions: {
    flexDirection: 'row',
    gap: 12,
  },
  paymentOption: {
    flex: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    padding: 12,
    alignItems: 'center',
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  paymentOptionSelected: {
    borderColor: '#ffa000',
    backgroundColor: 'rgba(255, 160, 0, 0.2)',
  },
  paymentOptionText: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    fontWeight: '600',
  },
  paymentOptionTextSelected: {
    color: '#ffa000',
  },
  purchaseButton: {
    borderRadius: 12,
    overflow: 'hidden',
  },
  purchaseButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
    gap: 8,
  },
  purchaseButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  purchasesContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  purchasesTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 16,
  },
  purchaseStats: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 20,
  },
  purchaseStatCard: {
    flex: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    padding: 12,
    alignItems: 'center',
  },
  purchaseStatValue: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffa000',
  },
  purchaseStatLabel: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 2,
  },
  recentPurchases: {
    gap: 12,
  },
  purchaseItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 12,
    padding: 12,
  },
  purchaseItemLeft: {
    flex: 1,
  },
  purchaseItemTokens: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffffff',
  },
  purchaseItemDate: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 2,
  },
  purchaseItemRight: {
    alignItems: 'flex-end',
  },
  purchaseItemAmount: {
    fontSize: 14,
    fontWeight: '600',
    color: '#ffa000',
  },
  purchaseItemStatus: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    marginTop: 2,
  },
  purchaseItemStatusText: {
    fontSize: 12,
    fontWeight: '500',
    textTransform: 'capitalize',
  },
  benefitsContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  benefitsTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 16,
  },
  benefitsList: {
    gap: 16,
  },
  benefitItem: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 12,
  },
  benefitItemText: {
    fontSize: 16,
    color: '#ffffff',
    fontWeight: '600',
    marginBottom: 2,
  },
  benefitItemSubtext: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  infoContainer: {
    marginBottom: 24,
  },
  infoCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 16,
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 12,
    borderWidth: 1,
    borderColor: 'rgba(107, 114, 128, 0.3)',
  },
  infoText: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
    lineHeight: 20,
    flex: 1,
  },
});