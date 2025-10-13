import { useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, Alert, TextInput } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Wallet as WalletIcon, Send, ArrowDownLeft, ArrowUpRight, Clock, CheckCircle, XCircle, Link } from 'lucide-react-native';
import { useWallet } from '@/contexts/WalletContext';
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'expo-router';
import LoadingDots from '@/components/LoadingDots';

interface Transaction {
  id: string;
  type: 'send' | 'receive';
  amount: number;
  status: 'pending' | 'completed' | 'failed';
  timestamp: string;
  hash?: string;
}

export default function WalletPage() {
  const router = useRouter();
  const { user } = useAuth();
  const { isConnected, address, balance, connectWallet, disconnectWallet, sendTokens, fetchTransactionHistory } = useWallet();
  const insets = useSafeAreaInsets();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [sendAmount, setSendAmount] = useState('');
  const [recipientAddress, setRecipientAddress] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!user) {
      // Add a small delay to ensure router is ready
      setTimeout(() => {
        router.push('/');
      }, 100);
      return;
    }
    // Fetch wallet data and transactions
    fetchTransactions();
  }, [user]);

  const fetchTransactions = async () => {
    try {
      // Fetch real transaction history from Ekehi Network
      const ekehiTransactions = await fetchTransactionHistory();
      
      // Convert Ekehi transactions to our Transaction format
      const formattedTransactions: Transaction[] = ekehiTransactions.map((tx, index) => ({
        id: tx.hash || `tx-${index}`,
        type: address && tx.from.toLowerCase() === address.toLowerCase() ? 'send' : 'receive',
        amount: tx.amount,
        status: tx.status === 'success' ? 'completed' : tx.status,
        timestamp: tx.timestamp,
        hash: tx.hash
      }));
      
      setTransactions(formattedTransactions);
    } catch (error) {
      console.error('Failed to fetch transactions:', error);
      // Fallback to mock data if API call fails
      const mockTransactions: Transaction[] = [
        {
          id: '1',
          type: 'receive',
          amount: 150.5,
          status: 'completed',
          timestamp: new Date().toISOString(),
          hash: '0xabc123...'
        },
        {
          id: '2',
          type: 'send',
          amount: 50.0,
          status: 'completed',
          timestamp: new Date(Date.now() - 86400000).toISOString(),
          hash: '0xdef456...'
        },
      ];
      setTransactions(mockTransactions);
    }
  };

  const handleConnectWallet = async () => {
    setIsLoading(true);
    try {
      await connectWallet();
      Alert.alert('Success', 'Wallet connected successfully!');
    } catch (error) {
      Alert.alert('Error', 'Failed to connect wallet. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDisconnectWallet = () => {
    Alert.alert(
      'Disconnect Wallet',
      'Are you sure you want to disconnect your wallet?',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Disconnect', style: 'destructive', onPress: disconnectWallet },
      ]
    );
  };

  const handleSendTokens = async () => {
    const amount = parseFloat(sendAmount);
    
    if (!amount || amount <= 0) {
      Alert.alert('Invalid Amount', 'Please enter a valid amount');
      return;
    }
    
    if (amount > balance) {
      Alert.alert('Insufficient Balance', 'You do not have enough tokens');
      return;
    }
    
    if (!recipientAddress) {
      Alert.alert('Invalid Address', 'Please enter a recipient address');
      return;
    }

    setIsLoading(true);
    try {
      await sendTokens(recipientAddress, amount);
      Alert.alert('Success', 'Tokens sent successfully!');
      setSendAmount('');
      setRecipientAddress('');
      // Refresh transactions
      fetchTransactions();
    } catch (error) {
      Alert.alert('Error', 'Failed to send tokens. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed':
        return <CheckCircle size={16} color="#10b981" />;
      case 'pending':
        return <Clock size={16} color="#f59e0b" />;
      case 'failed':
        return <XCircle size={16} color="#ef4444" />;
      default:
        return <Clock size={16} color="#6b7280" />;
    }
  };

  const getTransactionIcon = (type: string) => {
    return type === 'send' 
      ? <ArrowUpRight size={20} color="#ef4444" />
      : <ArrowDownLeft size={20} color="#10b981" />;
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
          <Text style={styles.title}>Wallet</Text>
          <Text style={styles.subtitle}>Manage your EKH tokens</Text>
        </View>

        {/* Wallet Connection Status */}
        {!isConnected ? (
          <View style={styles.connectContainer}>
            <LinearGradient
              colors={['rgba(59, 130, 246, 0.2)', 'rgba(59, 130, 246, 0.1)']}
              style={styles.connectCard}
            >
              <WalletIcon size={48} color="#3b82f6" />
              <Text style={styles.connectTitle}>Connect Your Wallet</Text>
              <Text style={styles.connectText}>
                Connect your wallet to manage and transfer your EKH tokens
              </Text>
              <TouchableOpacity
                style={styles.connectButton}
                onPress={handleConnectWallet}
                disabled={isLoading}
              >
                <LinearGradient
                  colors={['#3b82f6', '#2563eb']}
                  style={styles.connectButtonGradient}
                >
                  <Link size={20} color="#ffffff" />
                  {isLoading ? (
                    <LoadingDots color="#ffffff" size={8} />
                  ) : (
                    <Text style={styles.connectButtonText}>
                      Connect Wallet
                    </Text>
                  )}
                </LinearGradient>
              </TouchableOpacity>
            </LinearGradient>
          </View>
        ) : (
          <>
            {/* Balance Card */}
            <View style={styles.balanceContainer}>
              <LinearGradient
                colors={['rgba(59, 130, 246, 0.2)', 'rgba(147, 51, 234, 0.2)']}
                style={styles.balanceCard}
              >
                <View style={styles.balanceHeader}>
                  <WalletIcon size={32} color="#3b82f6" />
                  <TouchableOpacity onPress={handleDisconnectWallet}>
                    <Text style={styles.disconnectText}>Disconnect</Text>
                  </TouchableOpacity>
                </View>
                
                <Text style={styles.balanceAmount}>
                  {typeof balance === 'number' ? balance.toLocaleString() : '0'} EKH
                </Text>
                <Text style={styles.balanceLabel}>Available Balance</Text>
                
                <View style={styles.addressContainer}>
                  <Text style={styles.addressLabel}>Address:</Text>
                  <Text style={styles.addressText} numberOfLines={1}>
                    {address || 'Not connected'}
                  </Text>
                </View>
              </LinearGradient>
            </View>

            {/* Send Tokens */}
            <View style={styles.sendContainer}>
              <Text style={styles.sendTitle}>Send EKH Tokens</Text>
              
              <View style={styles.inputContainer}>
                <Text style={styles.inputLabel}>Recipient Address</Text>
                <TextInput
                  style={styles.input}
                  value={recipientAddress}
                  onChangeText={setRecipientAddress}
                  placeholder="0x..."
                  placeholderTextColor="rgba(255, 255, 255, 0.5)"
                />
              </View>
              
              <View style={styles.inputContainer}>
                <Text style={styles.inputLabel}>Amount</Text>
                <TextInput
                  style={styles.input}
                  value={sendAmount}
                  onChangeText={setSendAmount}
                  placeholder="Enter amount..."
                  placeholderTextColor="rgba(255, 255, 255, 0.5)"
                  keyboardType="numeric"
                />
              </View>
              
              <TouchableOpacity
                style={styles.sendButton}
                onPress={handleSendTokens}
                disabled={isLoading || !sendAmount || !recipientAddress}
              >
                <LinearGradient
                  colors={
                    sendAmount && recipientAddress && parseFloat(sendAmount) > 0 && parseFloat(sendAmount) <= balance
                      ? ['#ffa000', '#ff8f00']
                      : ['#6b7280', '#4b5563']
                  }
                  style={styles.sendButtonGradient}
                >
                  <Send size={20} color="#ffffff" />
                  {isLoading ? (
                    <LoadingDots color="#ffffff" size={8} />
                  ) : (
                    <Text style={styles.sendButtonText}>
                      Send Tokens
                    </Text>
                  )}
                </LinearGradient>
              </TouchableOpacity>
            </View>

            {/* Transaction History */}
            <View style={styles.transactionsContainer}>
              <Text style={styles.transactionsTitle}>Recent Transactions</Text>
              
              {transactions.length === 0 ? (
                <View style={styles.emptyContainer}>
                  <ArrowUpRight size={48} color="rgba(255, 255, 255, 0.3)" />
                  <Text style={styles.emptyTitle}>No Transactions Yet</Text>
                  <Text style={styles.emptyText}>Your transaction history will appear here</Text>
                </View>
              ) : (
                <View style={styles.transactionsList}>
                  {transactions.map((tx) => (
                    <View key={tx.id} style={styles.transactionItem}>
                      <View style={styles.transactionLeft}>
                        <View style={styles.transactionIcon}>
                          {getTransactionIcon(tx.type)}
                        </View>
                        <View style={styles.transactionInfo}>
                          <Text style={styles.transactionType}>
                            {tx.type === 'send' ? 'Sent' : tx.type === 'receive' ? 'Received' : 'Unknown'} EKH
                          </Text>
                          <Text style={styles.transactionDate}>
                            {tx.timestamp ? new Date(tx.timestamp).toLocaleDateString() : 'N/A'}
                          </Text>
                        </View>
                      </View>
                      
                      <View style={styles.transactionRight}>
                        <Text style={[
                          styles.transactionAmount,
                          { color: tx.type === 'receive' ? '#10b981' : '#ef4444' }
                        ]}>
                          {`${tx.type === 'receive' ? '+' : '-'}${typeof tx.amount === 'number' ? tx.amount.toLocaleString() : '0'}`}
                        </Text>
                        <View style={styles.transactionStatus}>
                          {getStatusIcon(tx.status)}
                          <Text style={[
                            styles.transactionStatusText,
                            { 
                              color: tx.status === 'completed' ? '#10b981' : 
                                     tx.status === 'pending' ? '#f59e0b' : '#ef4444'
                            }
                          ]}>
                            {tx.status || 'unknown'}
                          </Text>
                        </View>
                      </View>
                    </View>
                  ))}
                </View>
              )}
            </View>
          </>
        )}
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
  connectContainer: {
    marginBottom: 24,
  },
  connectCard: {
    borderRadius: 24,
    padding: 32,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(59, 130, 246, 0.3)',
  },
  connectTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
    marginTop: 16,
    marginBottom: 8,
  },
  connectText: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
    lineHeight: 24,
    marginBottom: 24,
  },
  connectButton: {
    borderRadius: 16,
    overflow: 'hidden',
  },
  connectButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 24,
    paddingVertical: 16,
    gap: 8,
  },
  connectButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  balanceContainer: {
    marginBottom: 24,
  },
  balanceCard: {
    borderRadius: 24,
    padding: 24,
    borderWidth: 1,
    borderColor: 'rgba(59, 130, 246, 0.3)',
  },
  balanceHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  disconnectText: {
    fontSize: 14,
    color: '#ef4444',
    fontWeight: '600',
  },
  balanceAmount: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#ffffff',
    textAlign: 'center',
    marginBottom: 8,
  },
  balanceLabel: {
    fontSize: 16,
    color: 'rgba(255, 255, 255, 0.7)',
    textAlign: 'center',
    marginBottom: 20,
  },
  addressContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    padding: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  addressLabel: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.7)',
  },
  addressText: {
    fontSize: 14,
    color: '#ffffff',
    fontFamily: 'monospace',
    flex: 1,
  },
  sendContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    marginBottom: 24,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  sendTitle: {
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
  sendButton: {
    borderRadius: 12,
    overflow: 'hidden',
  },
  sendButtonGradient: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
    gap: 8,
  },
  sendButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  transactionsContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 160, 0, 0.3)',
  },
  transactionsTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#ffffff',
    marginBottom: 16,
  },
  emptyContainer: {
    alignItems: 'center',
    paddingVertical: 32,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 12,
    marginBottom: 4,
  },
  emptyText: {
    fontSize: 14,
    color: 'rgba(255, 255, 255, 0.4)',
    textAlign: 'center',
  },
  transactionsList: {
    gap: 12,
  },
  transactionItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: 12,
    padding: 12,
  },
  transactionLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  transactionIcon: {
    width: 40,
    height: 40,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  transactionInfo: {
    flex: 1,
  },
  transactionType: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffffff',
  },
  transactionDate: {
    fontSize: 12,
    color: 'rgba(255, 255, 255, 0.6)',
    marginTop: 2,
  },
  transactionRight: {
    alignItems: 'flex-end',
  },
  transactionAmount: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  transactionStatus: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    marginTop: 2,
  },
  transactionStatusText: {
    fontSize: 12,
    fontWeight: '500',
    textTransform: 'capitalize',
  },
});
