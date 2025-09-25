'use client'

import { useState, useEffect, useMemo } from 'react'
import DashboardChart from '@/components/DashboardChart'
import Button from '@/components/Button'
import Modal from '@/components/Modal'
import Alert from '@/components/Alert'
import SearchFilter from '@/components/SearchFilter'

interface WalletTransaction {
  id: string
  userId: string
  userName: string
  type: 'deposit' | 'withdrawal' | 'transfer' | 'reward'
  amount: number
  status: 'completed' | 'pending' | 'failed'
  timestamp: string
  walletAddress?: string
}

interface WalletStats {
  totalBalance: number
  totalDeposits: number
  totalWithdrawals: number
  rewardsDistributed: number
}

export default function WalletPage() {
  const [transactions, setTransactions] = useState<WalletTransaction[]>([])
  const [stats, setStats] = useState<WalletStats>({ totalBalance: 0, totalDeposits: 0, totalWithdrawals: 0, rewardsDistributed: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [selectedTransaction, setSelectedTransaction] = useState<WalletTransaction | null>(null)
  
  const [newTransaction, setNewTransaction] = useState({
    userId: '',
    userName: '',
    type: 'deposit' as 'deposit' | 'withdrawal' | 'transfer' | 'reward',
    amount: 0,
    status: 'pending' as 'completed' | 'pending' | 'failed',
    walletAddress: ''
  })

  // Search, filter, and sort states
  const [searchQuery, setSearchQuery] = useState('')
  const [filters, setFilters] = useState<Record<string, any>>({})
  const [sortBy, setSortBy] = useState('')
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc')

  // Mock data for charts
  const transactionData = [
    { name: 'Jan', transactions: 400 },
    { name: 'Feb', transactions: 300 },
    { name: 'Mar', transactions: 200 },
    { name: 'Apr', transactions: 278 },
    { name: 'May', transactions: 189 },
    { name: 'Jun', transactions: 239 },
  ]

  const typeData = [
    { name: 'Deposits', value: 45 },
    { name: 'Withdrawals', value: 25 },
    { name: 'Transfers', value: 20 },
    { name: 'Rewards', value: 10 },
  ]

  useEffect(() => {
    fetchWalletData()
  }, [])

  const fetchWalletData = async () => {
    try {
      setLoading(true)
      const response = await fetch('/api/wallet')
      const data = await response.json()
      
      if (data.success) {
        setTransactions(data.data.transactions)
        setStats(data.data.stats)
      } else {
        setError(data.error || 'Failed to fetch wallet data')
      }
    } catch (error) {
      setError('Failed to fetch wallet data')
      console.error('Error fetching wallet data:', error)
    } finally {
      setLoading(false)
    }
  }

  // Filter and sort transactions
  const filteredAndSortedTransactions = useMemo(() => {
    let result = [...transactions]

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      result = result.filter(transaction => 
        transaction.userName.toLowerCase().includes(query) ||
        transaction.userId.toLowerCase().includes(query) ||
        (transaction.walletAddress && transaction.walletAddress.toLowerCase().includes(query))
      )
    }

    // Apply filters
    Object.keys(filters).forEach(key => {
      if (filters[key]) {
        result = result.filter(transaction => {
          if (key === 'type') {
            return transaction.type === filters[key]
          }
          if (key === 'status') {
            return transaction.status === filters[key]
          }
          return true
        })
      }
    })

    // Apply sorting
    if (sortBy) {
      result.sort((a, b) => {
        let aValue: any = a[sortBy as keyof WalletTransaction]
        let bValue: any = b[sortBy as keyof WalletTransaction]

        // Handle date sorting
        if (sortBy === 'timestamp') {
          aValue = new Date(aValue).getTime()
          bValue = new Date(bValue).getTime()
        }

        if (sortOrder === 'asc') {
          return aValue > bValue ? 1 : -1
        } else {
          return aValue < bValue ? 1 : -1
        }
      })
    }

    return result
  }, [transactions, searchQuery, filters, sortBy, sortOrder])

  const handleAddTransaction = async () => {
    try {
      const response = await fetch('/api/wallet/transaction', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...newTransaction,
          id: Math.random().toString(36).substr(2, 9),
          timestamp: new Date().toISOString()
        }),
      })
      
      const data = await response.json()
      
      if (data.success) {
        setTransactions([...transactions, data.data])
        setNewTransaction({
          userId: '',
          userName: '',
          type: 'deposit',
          amount: 0,
          status: 'pending',
          walletAddress: ''
        })
        setShowAddModal(false)
      } else {
        setError(data.error || 'Failed to add transaction')
      }
    } catch (error) {
      setError('Failed to add transaction')
      console.error('Error adding transaction:', error)
    }
  }

  const handleUpdateTransaction = async () => {
    if (!selectedTransaction) return
    
    try {
      const response = await fetch('/api/wallet/transaction', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(selectedTransaction),
      })
      
      const data = await response.json()
      
      if (data.success) {
        setTransactions(transactions.map(t => t.id === selectedTransaction.id ? selectedTransaction : t))
        setShowEditModal(false)
        setSelectedTransaction(null)
      } else {
        setError(data.error || 'Failed to update transaction')
      }
    } catch (error) {
      setError('Failed to update transaction')
      console.error('Error updating transaction:', error)
    }
  }

  const handleDeleteTransaction = async (id: string) => {
    if (!confirm('Are you sure you want to delete this transaction?')) return
    
    try {
      // In a real implementation, you would call a DELETE endpoint
      // For now, we'll just remove it from the local state
      setTransactions(transactions.filter(t => t.id !== id))
    } catch (error) {
      setError('Failed to delete transaction')
      console.error('Error deleting transaction:', error)
    }
  }

  // Handle search, filter, and sort events
  const handleSearch = (query: string) => {
    setSearchQuery(query)
  }

  const handleFilter = (newFilters: Record<string, any>) => {
    setFilters(newFilters)
  }

  const handleSort = (field: string, order: 'asc' | 'desc') => {
    setSortBy(field)
    setSortOrder(order)
  }

  // Get status color classes
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'completed': return 'bg-green-500/20 text-green-400 border border-green-500/30'
      case 'pending': return 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30'
      case 'failed': return 'bg-red-500/20 text-red-400 border border-red-500/30'
      default: return 'bg-gray-500/20 text-gray-400 border border-gray-500/30'
    }
  }

  // Get type color classes
  const getTypeColor = (type: string) => {
    switch (type) {
      case 'deposit': return 'bg-green-500/20 text-green-400 border border-green-500/30'
      case 'withdrawal': return 'bg-red-500/20 text-red-400 border border-red-500/30'
      case 'transfer': return 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
      case 'reward': return 'bg-purple-500/20 text-purple-400 border border-purple-500/30'
      default: return 'bg-gray-500/20 text-gray-400 border border-gray-500/30'
    }
  }

  return (
    <div className="py-6">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-semibold text-white">Wallet Management</h1>
          <Button onClick={() => setShowAddModal(true)}>Add Transaction</Button>
        </div>
      </div>
      
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        {/* Error message */}
        {error && (
          <div className="mt-4">
            <Alert 
              title="Error" 
              message={error} 
              type="error" 
              onClose={() => setError(null)} 
            />
          </div>
        )}
        
        {/* Search, Filter, and Sort */}
        <div className="mt-6">
          <SearchFilter
            onSearch={handleSearch}
            onFilter={handleFilter}
            onSort={handleSort}
            filterOptions={[
              {
                name: 'type',
                options: [
                  { value: 'deposit', label: 'Deposit' },
                  { value: 'withdrawal', label: 'Withdrawal' },
                  { value: 'transfer', label: 'Transfer' },
                  { value: 'reward', label: 'Reward' }
                ]
              },
              {
                name: 'status',
                options: [
                  { value: 'completed', label: 'Completed' },
                  { value: 'pending', label: 'Pending' },
                  { value: 'failed', label: 'Failed' }
                ]
              }
            ]}
            sortOptions={[
              { value: 'userName:asc', label: 'User Name (A-Z)' },
              { value: 'userName:desc', label: 'User Name (Z-A)' },
              { value: 'amount:asc', label: 'Amount (Low-High)' },
              { value: 'amount:desc', label: 'Amount (High-Low)' },
              { value: 'timestamp:asc', label: 'Date (Old-New)' },
              { value: 'timestamp:desc', label: 'Date (New-Old)' }
            ]}
          />
        </div>
        
        {/* Wallet Statistics */}
        <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-4">
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
            <div className="p-6 animated-gradient-slow">
              <div className="flex items-center">
                <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-purple-500 to-purple-600 p-3 shadow-lg">
                  <span className="text-white text-lg">💰</span>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Total Balance</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.totalBalance.toLocaleString()} EKH</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
          
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-cyan-500/20">
            <div className="p-6 animated-gradient-slow">
              <div className="flex items-center">
                <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-cyan-500 to-cyan-600 p-3 shadow-lg">
                  <span className="text-white text-lg">📥</span>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Total Deposits</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.totalDeposits.toLocaleString()} EKH</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
          
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-pink-500/20">
            <div className="p-6 animated-gradient-slow">
              <div className="flex items-center">
                <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-pink-500 to-pink-600 p-3 shadow-lg">
                  <span className="text-white text-lg">📤</span>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Total Withdrawals</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.totalWithdrawals.toLocaleString()} EKH</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
          
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-green-500/20">
            <div className="p-6 animated-gradient-slow">
              <div className="flex items-center">
                <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-green-500 to-green-600 p-3 shadow-lg">
                  <span className="text-white text-lg">🎁</span>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Rewards Distributed</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.rewardsDistributed.toLocaleString()} EKH</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        {/* Charts */}
        <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div className="glass-effect rounded-2xl shadow-2xl border border-purple-500/20 p-6">
            <DashboardChart 
              title="Transaction Volume"
              type="bar"
              data={transactionData}
              dataKey="transactions"
              nameKey="name"
              height={300}
            />
          </div>
          
          <div className="glass-effect rounded-2xl shadow-2xl border border-cyan-500/20 p-6">
            <DashboardChart 
              title="Transaction Types"
              type="pie"
              data={typeData}
              dataKey="value"
              nameKey="name"
              height={300}
            />
          </div>
        </div>
        
        {/* Wallet Transactions */}
        <div className="mt-6 glass-effect rounded-2xl shadow-2xl border border-purple-500/20 overflow-hidden">
          <div className="px-4 py-5 sm:px-6 border-b border-gray-700/50">
            <h3 className="text-lg font-medium leading-6 text-white">Wallet Transactions</h3>
          </div>
          <div className="overflow-x-auto">
            {loading ? (
              <div className="px-4 py-5 sm:px-6">
                <div className="flex justify-center items-center h-32">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-500"></div>
                </div>
              </div>
            ) : (
              <table className="min-w-full divide-y divide-gray-700/50">
                <thead className="bg-gray-800/50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      User
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Type
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Amount
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Status
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Date
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-gray-800/30 divide-y divide-gray-700/50">
                  {filteredAndSortedTransactions.map((transaction) => (
                    <tr key={transaction.id} className="hover:bg-gray-700/20 transition-colors duration-200">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-white">{transaction.userName}</div>
                        <div className="text-sm text-gray-400">{transaction.userId}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getTypeColor(transaction.type)}`}>
                          {transaction.type}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                        {transaction.amount.toLocaleString()} EKH
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(transaction.status)}`}>
                          {transaction.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-400">
                        {new Date(transaction.timestamp).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <button 
                          onClick={() => {
                            setSelectedTransaction(transaction)
                            setShowEditModal(true)
                          }}
                          className="text-cyan-400 hover:text-cyan-300 mr-3 transition-colors duration-200"
                        >
                          Edit
                        </button>
                        <button 
                          onClick={() => handleDeleteTransaction(transaction.id)}
                          className="text-pink-400 hover:text-pink-300 transition-colors duration-200"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
      
      {/* Add Transaction Modal */}
      <Modal
        open={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add Transaction"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300">User ID</label>
            <input
              type="text"
              value={newTransaction.userId}
              onChange={(e) => setNewTransaction({...newTransaction, userId: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">User Name</label>
            <input
              type="text"
              value={newTransaction.userName}
              onChange={(e) => setNewTransaction({...newTransaction, userName: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Type</label>
            <select
              value={newTransaction.type}
              onChange={(e) => setNewTransaction({...newTransaction, type: e.target.value as any})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
            >
              <option value="deposit" className="bg-gray-800">Deposit</option>
              <option value="withdrawal" className="bg-gray-800">Withdrawal</option>
              <option value="transfer" className="bg-gray-800">Transfer</option>
              <option value="reward" className="bg-gray-800">Reward</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Amount</label>
            <input
              type="number"
              value={newTransaction.amount}
              onChange={(e) => setNewTransaction({...newTransaction, amount: parseFloat(e.target.value) || 0})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Status</label>
            <select
              value={newTransaction.status}
              onChange={(e) => setNewTransaction({...newTransaction, status: e.target.value as any})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
            >
              <option value="pending" className="bg-gray-800">Pending</option>
              <option value="completed" className="bg-gray-800">Completed</option>
              <option value="failed" className="bg-gray-800">Failed</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Wallet Address</label>
            <input
              type="text"
              value={newTransaction.walletAddress}
              onChange={(e) => setNewTransaction({...newTransaction, walletAddress: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
            />
          </div>
        </div>
      </Modal>
      
      {/* Edit Transaction Modal */}
      <Modal
        open={showEditModal && selectedTransaction !== null}
        onClose={() => {
          setShowEditModal(false)
          setSelectedTransaction(null)
        }}
        title="Edit Transaction"
      >
        {selectedTransaction && (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300">User ID</label>
              <input
                type="text"
                value={selectedTransaction.userId}
                onChange={(e) => setSelectedTransaction({...selectedTransaction, userId: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">User Name</label>
              <input
                type="text"
                value={selectedTransaction.userName}
                onChange={(e) => setSelectedTransaction({...selectedTransaction, userName: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Type</label>
              <select
                value={selectedTransaction.type}
                onChange={(e) => setSelectedTransaction({...selectedTransaction, type: e.target.value as any})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              >
                <option value="deposit" className="bg-gray-800">Deposit</option>
                <option value="withdrawal" className="bg-gray-800">Withdrawal</option>
                <option value="transfer" className="bg-gray-800">Transfer</option>
                <option value="reward" className="bg-gray-800">Reward</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Amount</label>
              <input
                type="number"
                value={selectedTransaction.amount}
                onChange={(e) => setSelectedTransaction({...selectedTransaction, amount: parseFloat(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Status</label>
              <select
                value={selectedTransaction.status}
                onChange={(e) => setSelectedTransaction({...selectedTransaction, status: e.target.value as any})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              >
                <option value="pending" className="bg-gray-800">Pending</option>
                <option value="completed" className="bg-gray-800">Completed</option>
                <option value="failed" className="bg-gray-800">Failed</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Wallet Address</label>
              <input
                type="text"
                value={selectedTransaction.walletAddress || ''}
                onChange={(e) => setSelectedTransaction({...selectedTransaction, walletAddress: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              />
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}