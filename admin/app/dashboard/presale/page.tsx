'use client'

import { useState, useEffect, useMemo } from 'react'
import DashboardChart from '@/components/DashboardChart'
import Button from '@/components/Button'
import Modal from '@/components/Modal'
import Alert from '@/components/Alert'
import SearchFilter from '@/components/SearchFilter'

interface PresalePurchase {
  id: string
  userId: string
  userName: string
  amountUsd: number
  tokensAmount: number
  status: 'completed' | 'pending' | 'failed'
  createdAt: string
  transactionHash?: string
}

interface PresaleStats {
  totalRaised: number
  tokensSold: number
  participants: number
}

export default function PresalePage() {
  const [presales, setPresales] = useState<PresalePurchase[]>([])
  const [stats, setStats] = useState<PresaleStats>({ totalRaised: 0, tokensSold: 0, participants: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [settings, setSettings] = useState({
    isActive: true,
    tokenPrice: 0.1,
    minPurchase: 10
  })
  
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [selectedPresale, setSelectedPresale] = useState<PresalePurchase | null>(null)
  
  const [newPresale, setNewPresale] = useState({
    userId: '',
    userName: '',
    amountUsd: 0,
    tokensAmount: 0,
    status: 'pending' as 'completed' | 'pending' | 'failed',
    transactionHash: ''
  })

  // Search, filter, and sort states
  const [searchQuery, setSearchQuery] = useState('')
  const [filters, setFilters] = useState<Record<string, any>>({})
  const [sortBy, setSortBy] = useState('')
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc')

  // Mock data for charts
  const salesData = [
    { name: 'Jan', sales: 4000 },
    { name: 'Feb', sales: 3000 },
    { name: 'Mar', sales: 2000 },
    { name: 'Apr', sales: 2780 },
    { name: 'May', sales: 1890 },
    { name: 'Jun', sales: 2390 },
  ]

  const statusData = [
    { name: 'Completed', value: 65 },
    { name: 'Pending', value: 25 },
    { name: 'Failed', value: 10 },
  ]

  useEffect(() => {
    fetchPresaleData()
  }, [])

  const fetchPresaleData = async () => {
    try {
      setLoading(true)
      const response = await fetch('/api/presale')
      const data = await response.json()
      
      if (data.success) {
        setPresales(data.data.presales)
        setStats(data.data.stats)
      } else {
        setError(data.error || 'Failed to fetch presale data')
      }
    } catch (error) {
      setError('Failed to fetch presale data')
      console.error('Error fetching presale data:', error)
    } finally {
      setLoading(false)
    }
  }

  // Filter and sort presales
  const filteredAndSortedPresales = useMemo(() => {
    let result = [...presales]

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      result = result.filter(presale => 
        presale.userName.toLowerCase().includes(query) ||
        presale.userId.toLowerCase().includes(query) ||
        (presale.transactionHash && presale.transactionHash.toLowerCase().includes(query))
      )
    }

    // Apply filters
    Object.keys(filters).forEach(key => {
      if (filters[key]) {
        result = result.filter(presale => {
          if (key === 'status') {
            return presale.status === filters[key]
          }
          return true
        })
      }
    })

    // Apply sorting
    if (sortBy) {
      result.sort((a, b) => {
        let aValue: any = a[sortBy as keyof PresalePurchase]
        let bValue: any = b[sortBy as keyof PresalePurchase]

        // Handle date sorting
        if (sortBy === 'createdAt') {
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
  }, [presales, searchQuery, filters, sortBy, sortOrder])

  const togglePresaleStatus = () => {
    setSettings(prev => ({ ...prev, isActive: !prev.isActive }))
  }

  const handleSaveSettings = async () => {
    try {
      const response = await fetch('/api/presale/settings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(settings),
      })
      
      const data = await response.json()
      
      if (data.success) {
        alert('Settings saved successfully!')
      } else {
        setError(data.error || 'Failed to save settings')
      }
    } catch (error) {
      setError('Failed to save settings')
      console.error('Error saving settings:', error)
    }
  }

  const handleAddPresale = async () => {
    try {
      const response = await fetch('/api/presale', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...newPresale,
          id: Math.random().toString(36).substr(2, 9),
          createdAt: new Date().toISOString()
        }),
      })
      
      const data = await response.json()
      
      if (data.success) {
        setPresales([...presales, data.data])
        setNewPresale({
          userId: '',
          userName: '',
          amountUsd: 0,
          tokensAmount: 0,
          status: 'pending',
          transactionHash: ''
        })
        setShowAddModal(false)
        // Update stats
        setStats(prev => ({
          totalRaised: prev.totalRaised + newPresale.amountUsd,
          tokensSold: prev.tokensSold + newPresale.tokensAmount,
          participants: prev.participants + 1
        }))
      } else {
        setError(data.error || 'Failed to add presale purchase')
      }
    } catch (error) {
      setError('Failed to add presale purchase')
      console.error('Error adding presale purchase:', error)
    }
  }

  const handleUpdatePresale = async () => {
    if (!selectedPresale) return
    
    try {
      const response = await fetch('/api/presale', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(selectedPresale),
      })
      
      const data = await response.json()
      
      if (data.success) {
        setPresales(presales.map(p => p.id === selectedPresale.id ? selectedPresale : p))
        setShowEditModal(false)
        setSelectedPresale(null)
      } else {
        setError(data.error || 'Failed to update presale purchase')
      }
    } catch (error) {
      setError('Failed to update presale purchase')
      console.error('Error updating presale purchase:', error)
    }
  }

  const handleDeletePresale = async (id: string) => {
    if (!confirm('Are you sure you want to delete this presale purchase?')) return
    
    try {
      const response = await fetch(`/api/presale/${id}`, {
        method: 'DELETE',
      })
      
      const data = await response.json()
      
      if (data.success) {
        const deletedPresale = presales.find(p => p.id === id)
        setPresales(presales.filter(presale => presale.id !== id))
        // Update stats
        if (deletedPresale) {
          setStats(prev => ({
            totalRaised: prev.totalRaised - deletedPresale.amountUsd,
            tokensSold: prev.tokensSold - deletedPresale.tokensAmount,
            participants: prev.participants - 1
          }))
        }
      } else {
        setError(data.error || 'Failed to delete presale purchase')
      }
    } catch (error) {
      setError('Failed to delete presale purchase')
      console.error('Error deleting presale purchase:', error)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
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

  if (loading) {
    return (
      <div className="py-6">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-400 to-cyan-400 bg-clip-text text-transparent">Presale Management</h1>
        </div>
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          <div className="mt-8 flex justify-center items-center h-64 glass-effect rounded-2xl border border-purple-500/20">
            <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-purple-500"></div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="py-6 relative">
      {/* Floating particles effect */}
      <div className="particles"></div>
      
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-400 via-cyan-400 to-pink-400 bg-clip-text text-transparent">
            Presale Management
          </h1>
          <Button 
            onClick={() => setShowAddModal(true)}
            className="bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 text-white font-bold py-2 px-6 rounded-lg shadow-lg hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1"
          >
            Add New Purchase
          </Button>
        </div>
        
        {/* Stats */}
        <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-3">
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
            <div className="p-6 animated-gradient-slow">
              <div className="flex items-center">
                <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-purple-500 to-purple-600 p-3 shadow-lg">
                  <svg className="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Total Raised</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">${stats.totalRaised.toLocaleString()}</div>
                      <div className="ml-2 text-sm text-green-400">+15%</div>
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
                  <svg className="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Tokens Sold</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.tokensSold.toLocaleString()}</div>
                      <div className="ml-2 text-sm text-green-400">+12%</div>
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
                  <svg className="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Participants</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.participants}</div>
                      <div className="ml-2 text-sm text-green-400">+8%</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        {/* Error message */}
        {error && (
          <div className="mt-6">
            <Alert 
              title="Error" 
              message={error} 
              type="error" 
              onClose={() => setError(null)} 
              className="glass-effect border border-red-500/20 rounded-2xl"
            />
          </div>
        )}
        
        {/* Charts */}
        <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-2">
          {/* Sales Chart */}
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
            <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
              <h3 className="text-lg font-bold leading-6 text-white">Sales Trend</h3>
            </div>
            <div className="p-5">
              <DashboardChart 
                title="Sales Trend"
                type="bar"
                data={salesData}
                dataKey="sales"
                nameKey="name"
                height={300}
              />
            </div>
          </div>

          {/* Status Distribution */}
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-cyan-500/20">
            <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
              <h3 className="text-lg font-bold leading-6 text-white">Status Distribution</h3>
            </div>
            <div className="p-5">
              <DashboardChart 
                title="Status Distribution"
                type="pie"
                data={statusData}
                dataKey="value"
                nameKey="name"
                height={300}
              />
            </div>
          </div>
        </div>
        
        {/* Settings */}
        <div className="mt-8 glass-effect rounded-2xl border border-purple-500/20 p-6 shadow-xl">
          <div className="flex justify-between items-center">
            <h3 className="text-lg font-bold leading-6 text-white">Presale Settings</h3>
            <Button 
              onClick={handleSaveSettings}
              className="bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 text-white font-bold py-2 px-4 rounded-lg shadow-lg transition-all duration-300"
            >
              Save Settings
            </Button>
          </div>
          
          <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-3">
            <div>
              <label className="block text-sm font-medium text-gray-300">Presale Status</label>
              <div className="mt-2 flex items-center">
                <button
                  onClick={togglePresaleStatus}
                  className={`relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-purple-500 focus:ring-offset-2 ${
                    settings.isActive ? 'bg-green-500' : 'bg-gray-600'
                  }`}
                >
                  <span
                    className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
                      settings.isActive ? 'translate-x-5' : 'translate-x-0'
                    }`}
                  />
                </button>
                <span className="ml-3 text-sm text-gray-300">
                  {settings.isActive ? 'Active' : 'Inactive'}
                </span>
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Token Price (USD)</label>
              <input
                type="number"
                value={settings.tokenPrice}
                onChange={(e) => setSettings({...settings, tokenPrice: parseFloat(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
                placeholder="0.1"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Minimum Purchase (USD)</label>
              <input
                type="number"
                value={settings.minPurchase}
                onChange={(e) => setSettings({...settings, minPurchase: parseFloat(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
                placeholder="10"
              />
            </div>
          </div>
        </div>
        
        {/* Search, Filter, and Sort */}
        <div className="mt-8 glass-effect rounded-2xl border border-purple-500/20 p-6 shadow-xl">
          <SearchFilter
            onSearch={handleSearch}
            onFilter={handleFilter}
            onSort={handleSort}
            filterOptions={[{
              name: 'status',
              options: [
                { value: 'completed', label: 'Completed' },
                { value: 'pending', label: 'Pending' },
                { value: 'failed', label: 'Failed' }
              ]
            }]}
            sortOptions={[{
              value: 'userName:asc', label: 'User (A-Z)'
            }, {
              value: 'userName:desc', label: 'User (Z-A)'
            }, {
              value: 'amountUsd:asc', label: 'Amount (Low-High)'
            }, {
              value: 'amountUsd:desc', label: 'Amount (High-Low)'
            }, {
              value: 'createdAt:asc', label: 'Date (Old-New)'
            }, {
              value: 'createdAt:desc', label: 'Date (New-Old)'
            }]}
          />
        </div>
        
        {/* Presale table */}
        <div className="mt-8 glass-effect rounded-2xl border border-purple-500/20 shadow-xl overflow-hidden">
          <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
            <h3 className="text-lg font-bold leading-6 text-white">Presale Purchases</h3>
          </div>
          
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-700/50">
              <thead className="glass-effect">
                <tr>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    User
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Amount (USD)
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Tokens
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Status
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Transaction
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Date
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-700/30">
                {filteredAndSortedPresales.map((presale) => (
                  <tr key={presale.id} className="hover:bg-white/5 transition-colors duration-200">
                    <td className="px-6 py-5 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-12 w-12">
                          <div className="h-12 w-12 rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center shadow-lg">
                            <span className="text-white font-bold text-lg">
                              {presale.userName.charAt(0)}
                            </span>
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-base font-bold text-white">{presale.userName}</div>
                          <div className="text-sm text-gray-400">ID: {presale.userId}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap text-base font-bold text-cyan-400">
                      ${presale.amountUsd.toLocaleString()}
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap text-base font-bold text-purple-400">
                      {presale.tokensAmount.toLocaleString()}
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap">
                      <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        presale.status === 'completed' 
                          ? 'bg-green-500/20 text-green-400 border border-green-500/30' 
                          : presale.status === 'pending'
                          ? 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30'
                          : 'bg-red-500/20 text-red-400 border border-red-500/30'
                      }`}>
                        {presale.status}
                      </span>
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-300">
                      {presale.transactionHash ? (
                        <span className="font-mono text-xs">{presale.transactionHash.substring(0, 10)}...</span>
                      ) : (
                        <span className="text-gray-500">N/A</span>
                      )}
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-400">
                      {formatDate(presale.createdAt)}
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap text-sm">
                      <button 
                        onClick={() => {
                          setSelectedPresale(presale)
                          setShowEditModal(true)
                        }}
                        className="text-cyan-400 hover:text-cyan-300 font-medium mr-4 transition-colors duration-200"
                      >
                        Edit
                      </button>
                      <button 
                        onClick={() => handleDeletePresale(presale.id)}
                        className="text-red-500 hover:text-red-400 font-medium transition-colors duration-200"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
      
      {/* Add Presale Modal */}
      <Modal
        open={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New Presale Purchase"
      >
        <div className="space-y-4 glass-effect border border-purple-500/20 rounded-2xl p-6">
          <div>
            <label className="block text-sm font-medium text-gray-300">User ID</label>
            <input
              type="text"
              value={newPresale.userId}
              onChange={(e) => setNewPresale({...newPresale, userId: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              placeholder="Enter user ID"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">User Name</label>
            <input
              type="text"
              value={newPresale.userName}
              onChange={(e) => setNewPresale({...newPresale, userName: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              placeholder="Enter user name"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Amount (USD)</label>
            <input
              type="number"
              value={newPresale.amountUsd}
              onChange={(e) => setNewPresale({...newPresale, amountUsd: parseFloat(e.target.value) || 0})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              placeholder="Enter amount in USD"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Tokens Amount</label>
            <input
              type="number"
              value={newPresale.tokensAmount}
              onChange={(e) => setNewPresale({...newPresale, tokensAmount: parseFloat(e.target.value) || 0})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              placeholder="Enter tokens amount"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Status</label>
            <select
              value={newPresale.status}
              onChange={(e) => setNewPresale({...newPresale, status: e.target.value as 'completed' | 'pending' | 'failed'})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
            >
              <option value="pending" className="bg-gray-800">Pending</option>
              <option value="completed" className="bg-gray-800">Completed</option>
              <option value="failed" className="bg-gray-800">Failed</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Transaction Hash</label>
            <input
              type="text"
              value={newPresale.transactionHash}
              onChange={(e) => setNewPresale({...newPresale, transactionHash: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              placeholder="Enter transaction hash (optional)"
            />
          </div>
          
          <div className="mt-6 flex justify-end space-x-3">
            <Button 
              onClick={() => setShowAddModal(false)}
              className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white border border-gray-600 hover:border-gray-500 rounded-lg transition-colors duration-200"
            >
              Cancel
            </Button>
            <Button 
              onClick={handleAddPresale}
              className="px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 rounded-lg shadow-lg transition-all duration-300"
            >
              Add Purchase
            </Button>
          </div>
        </div>
      </Modal>
      
      {/* Edit Presale Modal */}
      <Modal
        open={showEditModal && selectedPresale !== null}
        onClose={() => {
          setShowEditModal(false)
          setSelectedPresale(null)
        }}
        title="Edit Presale Purchase"
      >
        {selectedPresale && (
          <div className="space-y-4 glass-effect border border-purple-500/20 rounded-2xl p-6">
            <div>
              <label className="block text-sm font-medium text-gray-300">User ID</label>
              <input
                type="text"
                value={selectedPresale.userId}
                onChange={(e) => setSelectedPresale({...selectedPresale, userId: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
                placeholder="Enter user ID"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">User Name</label>
              <input
                type="text"
                value={selectedPresale.userName}
                onChange={(e) => setSelectedPresale({...selectedPresale, userName: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
                placeholder="Enter user name"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Amount (USD)</label>
              <input
                type="number"
                value={selectedPresale.amountUsd}
                onChange={(e) => setSelectedPresale({...selectedPresale, amountUsd: parseFloat(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
                placeholder="Enter amount in USD"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Tokens Amount</label>
              <input
                type="number"
                value={selectedPresale.tokensAmount}
                onChange={(e) => setSelectedPresale({...selectedPresale, tokensAmount: parseFloat(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
                placeholder="Enter tokens amount"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Status</label>
              <select
                value={selectedPresale.status}
                onChange={(e) => setSelectedPresale({...selectedPresale, status: e.target.value as 'completed' | 'pending' | 'failed'})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              >
                <option value="pending" className="bg-gray-800">Pending</option>
                <option value="completed" className="bg-gray-800">Completed</option>
                <option value="failed" className="bg-gray-800">Failed</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Transaction Hash</label>
              <input
                type="text"
                value={selectedPresale.transactionHash || ''}
                onChange={(e) => setSelectedPresale({...selectedPresale, transactionHash: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
                placeholder="Enter transaction hash (optional)"
              />
            </div>
          </div>
        )}
        <div className="mt-6 flex justify-end space-x-3">
          <Button 
            onClick={() => {
              setShowEditModal(false)
              setSelectedPresale(null)
            }}
            className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white border border-gray-600 hover:border-gray-500 rounded-lg transition-colors duration-200"
          >
            Cancel
          </Button>
          <Button 
            onClick={handleUpdatePresale}
            className="px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 rounded-lg shadow-lg transition-all duration-300"
          >
            Update Purchase
          </Button>
        </div>
      </Modal>
    </div>
  )
}