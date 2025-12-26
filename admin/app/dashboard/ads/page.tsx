'use client'

import { useState, useEffect, useMemo, Fragment } from 'react'
import Button from '@/components/Button'
import Modal from '@/components/Modal'
import Alert from '@/components/Alert'
import SearchFilter from '@/components/SearchFilter'

interface AdCampaign {
  id: string
  name: string
  description: string
  reward: number
  duration: number // in seconds
  isActive: boolean
  impressions: number
  clicks: number
  createdAt: string
}

interface AdStats {
  totalCampaigns: number
  totalImpressions: number
  totalClicks: number
  avgCTR: number
}

export default function AdsPage() {
  const [campaigns, setCampaigns] = useState<AdCampaign[]>([])
  const [stats, setStats] = useState<AdStats>({ totalCampaigns: 0, totalImpressions: 0, totalClicks: 0, avgCTR: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [selectedCampaign, setSelectedCampaign] = useState<AdCampaign | null>(null)
  
  const [newCampaign, setNewCampaign] = useState({
    name: '',
    description: '',
    reward: 0.1,
    duration: 30,
    isActive: true
  })

  // Search, filter, and sort states
  const [searchQuery, setSearchQuery] = useState('')
  const [filters, setFilters] = useState<Record<string, any>>({})
  const [sortBy, setSortBy] = useState('')
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc')

  useEffect(() => {
    fetchAdData()
  }, [])

  const fetchAdData = async () => {
    try {
      setLoading(true)
      const response = await fetch('/api/ads')
      const data = await response.json()
      
      if (data.success) {
        setCampaigns(data.data.campaigns)
        setStats(data.data.stats)
      } else {
        setError(data.error || 'Failed to fetch ad data')
      }
    } catch (error) {
      setError('Failed to fetch ad data')
      console.error('Error fetching ad data:', error)
    } finally {
      setLoading(false)
    }
  }

  // Filter and sort campaigns
  const filteredAndSortedCampaigns = useMemo(() => {
    let result = [...campaigns]

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      result = result.filter(campaign => 
        campaign.name.toLowerCase().includes(query) ||
        campaign.description.toLowerCase().includes(query)
      )
    }

    // Apply filters
    Object.keys(filters).forEach(key => {
      if (filters[key]) {
        result = result.filter(campaign => {
          if (key === 'status') {
            return campaign.isActive.toString() === filters[key]
          }
          return true
        })
      }
    })

    // Apply sorting
    if (sortBy) {
      result.sort((a, b) => {
        let aValue: any = a[sortBy as keyof AdCampaign]
        let bValue: any = b[sortBy as keyof AdCampaign]

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
  }, [campaigns, searchQuery, filters, sortBy, sortOrder])

  const toggleCampaignStatus = async (id: string) => {
    try {
      const campaign = campaigns.find(c => c.id === id)
      if (!campaign) return
      
      const updatedCampaign = { ...campaign, isActive: !campaign.isActive }
      
      const response = await fetch('/api/ads', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(updatedCampaign),
      })
      
      const data = await response.json()
      
      if (data.success) {
        setCampaigns(campaigns.map(c => c.id === id ? updatedCampaign : c))
      } else {
        setError(data.error || 'Failed to update campaign status')
      }
    } catch (error) {
      setError('Failed to update campaign status')
      console.error('Error updating campaign status:', error)
    }
  }

  const handleAddCampaign = async () => {
    try {
      const response = await fetch('/api/ads', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...newCampaign,
          id: Math.random().toString(36).substr(2, 9),
          impressions: 0,
          clicks: 0,
          createdAt: new Date().toISOString()
        }),
      })
      
      const data = await response.json()
      
      if (data.success) {
        setCampaigns([...campaigns, data.data])
        setNewCampaign({
          name: '',
          description: '',
          reward: 0.1,
          duration: 30,
          isActive: true
        })
        setShowAddModal(false)
        // Update stats
        setStats(prev => ({
          ...prev,
          totalCampaigns: prev.totalCampaigns + 1
        }))
      } else {
        setError(data.error || 'Failed to create campaign')
      }
    } catch (error) {
      setError('Failed to create campaign')
      console.error('Error creating campaign:', error)
    }
  }

  const handleUpdateCampaign = async () => {
    if (!selectedCampaign) return
    
    try {
      const response = await fetch('/api/ads', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(selectedCampaign),
      })
      
      const data = await response.json()
      
      if (data.success) {
        setCampaigns(campaigns.map(c => c.id === selectedCampaign.id ? selectedCampaign : c))
        setShowEditModal(false)
        setSelectedCampaign(null)
      } else {
        setError(data.error || 'Failed to update campaign')
      }
    } catch (error) {
      setError('Failed to update campaign')
      console.error('Error updating campaign:', error)
    }
  }

  const handleDeleteCampaign = async (id: string) => {
    if (!confirm('Are you sure you want to delete this campaign?')) return
    
    try {
      const response = await fetch(`/api/ads/${id}`, {
        method: 'DELETE',
      })
      
      const data = await response.json()
      
      if (data.success) {
        setCampaigns(campaigns.filter(c => c.id !== id))
        // Update stats
        setStats(prev => ({
          ...prev,
          totalCampaigns: prev.totalCampaigns - 1
        }))
      } else {
        setError(data.error || 'Failed to delete campaign')
      }
    } catch (error) {
      setError('Failed to delete campaign')
      console.error('Error deleting campaign:', error)
    }
  }

  // Calculate CTR (Click-Through Rate)
  const calculateCTR = (impressions: number, clicks: number) => {
    if (impressions === 0) return 0
    return ((clicks / impressions) * 100).toFixed(2)
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
  const getStatusColor = (isActive: boolean) => {
    return isActive 
      ? 'bg-green-500/20 text-green-400 border border-green-500/30' 
      : 'bg-red-500/20 text-red-400 border border-red-500/30'
  }

  if (loading) {
    return (
      <div className="py-6 relative">
        <div className="particles"></div>
        
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          <div className="flex justify-between items-center">
            <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-400 via-cyan-400 to-pink-400 bg-clip-text text-transparent">
              Ad Campaigns Management
            </h1>
            <div className="text-sm text-gray-400">
              Loading...
            </div>
          </div>
        </div>
        
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          {/* Stats Skeletons */}
          <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-4">
            {Array.from({ length: 4 }).map((_, index) => (
              <div key={`stat-skeleton-${index}`} className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
                <div className="p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 rounded-xl bg-gray-700/50 p-3 shadow-lg">
                      <div className="h-7 w-7 bg-gray-600/50 rounded-full animate-pulse"></div>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <div className="h-4 bg-gray-700/50 rounded w-24 mb-3 animate-pulse"></div>
                      <div className="h-8 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Search Filter Skeleton */}
          <div className="mt-8 glass-effect rounded-2xl border border-purple-500/20 p-6 shadow-xl">
            <div className="h-40 bg-gray-700/50 rounded animate-pulse"></div>
          </div>

          {/* Table Skeleton */}
          <div className="mt-8 glass-effect rounded-2xl border border-purple-500/20 shadow-xl overflow-hidden">
            <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50">
              <div className="h-6 bg-gray-700/50 rounded w-40 animate-pulse"></div>
            </div>
            
            <div className="overflow-x-auto max-w-full">
              {/* Mobile view - Skeleton cards */}
              <div className="block md:hidden">
                {Array.from({ length: 5 }).map((_, index) => (
                  <div key={`skeleton-card-${index}`} className="bg-gray-800/30 m-4 p-4 rounded-lg border border-gray-700/50 animate-pulse">
                    <div className="flex items-center mb-4">
                      <div className="h-10 w-10 rounded-full bg-gray-700/50 mr-3"></div>
                      <div>
                        <div className="h-4 bg-gray-700/50 rounded w-24 mb-2"></div>
                        <div className="h-3 bg-gray-700/50 rounded w-32"></div>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-2 text-sm text-gray-300">
                      <div><div className="h-3 bg-gray-700/50 rounded w-16"></div></div>
                      <div><div className="h-3 bg-gray-700/50 rounded w-16"></div></div>
                      <div><div className="h-3 bg-gray-700/50 rounded w-16"></div></div>
                      <div><div className="h-3 bg-gray-700/50 rounded w-16"></div></div>
                    </div>
                    <div className="mt-3 flex justify-end space-x-2">
                      <div className="h-5 bg-gray-700/50 rounded w-16"></div>
                      <div className="h-5 bg-gray-700/50 rounded w-12"></div>
                      <div className="h-5 bg-gray-700/50 rounded w-12"></div>
                    </div>
                  </div>
                ))}
              </div>
              
              {/* Desktop view - Skeleton table */}
              <div className="hidden md:block min-w-full">
                <table className="min-w-full divide-y divide-gray-700/50">
                  <thead className="glass-effect">
                    <tr>
                      {Array.from({ length: 8 }).map((_, index) => (
                        <th key={index} className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-700/30">
                    {Array.from({ length: 5 }).map((_, rowIndex) => (
                      <tr key={rowIndex} className="hover:bg-white/5 transition-colors duration-200">
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-10 w-10">
                              <div className="h-10 w-10 rounded-full bg-gray-700/50 animate-pulse"></div>
                            </div>
                            <div className="ml-4">
                              <div className="h-4 bg-gray-700/50 rounded w-24 mb-2 animate-pulse"></div>
                              <div className="h-3 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-20 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-5 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap text-sm">
                          <div className="flex space-x-4">
                            <div className="h-5 bg-gray-700/50 rounded w-12 animate-pulse"></div>
                            <div className="h-5 bg-gray-700/50 rounded w-12 animate-pulse"></div>
                            <div className="h-5 bg-gray-700/50 rounded w-12 animate-pulse"></div>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="py-6 overflow-x-hidden">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-400 to-cyan-400 bg-clip-text text-transparent">Ad Campaigns Management</h1>
          <Button 
            onClick={() => setShowAddModal(true)}
            className="bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 text-white font-bold py-2 px-6 rounded-lg shadow-lg hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1"
          >
            Create Campaign
          </Button>
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
            />
          </div>
        )}
        
        {/* Search, Filter, Sort, and Refresh */}
        <div className="mt-8 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <SearchFilter
            onSearch={handleSearch}
            onFilter={handleFilter}
            onSort={handleSort}
            filterOptions={[
              {
                name: 'status',
                options: [
                  { value: 'true', label: 'Active' },
                  { value: 'false', label: 'Inactive' }
                ]
              }
            ]}
            sortOptions={[
              { value: 'name:asc', label: 'Name (A-Z)' },
              { value: 'name:desc', label: 'Name (Z-A)' },
              { value: 'reward:asc', label: 'Reward (Low-High)' },
              { value: 'reward:desc', label: 'Reward (High-Low)' },
              { value: 'createdAt:asc', label: 'Date (Old-New)' },
              { value: 'createdAt:desc', label: 'Date (New-Old)' }
            ]}
          />
          <button
            onClick={fetchAdData}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 text-white font-medium rounded-lg shadow-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Refresh
          </button>
        </div>
        
        {/* Ad Campaigns Stats */}
        <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-4">
          {loading && stats.totalCampaigns === 0 ? (
            // Show skeleton loaders when stats are loading
            Array.from({ length: 4 }).map((_, index) => (
              <div key={`stat-skeleton-${index}`} className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
                <div className="p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 rounded-xl bg-gray-700/50 p-3 shadow-lg">
                      <div className="h-7 w-7 bg-gray-600/50 rounded-full animate-pulse"></div>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <div className="h-4 bg-gray-700/50 rounded w-24 mb-3 animate-pulse"></div>
                      <div className="h-8 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                    </div>
                  </div>
                </div>
              </div>
            ))
          ) : (
            // Show actual stats
            <>
              <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
                <div className="p-6 animated-gradient-slow">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-purple-500 to-purple-600 p-3 shadow-lg">
                      <span className="text-white text-lg">📺</span>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Total Campaigns</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.totalCampaigns}</div>
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
                      <span className="text-white text-lg">👁️</span>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Total Impressions</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">
                            {stats.totalImpressions.toLocaleString()}
                          </div>
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
                      <span className="text-white text-lg">👆</span>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Total Clicks</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">
                            {stats.totalClicks.toLocaleString()}
                          </div>
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
                      <span className="text-white text-lg">📈</span>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Avg. CTR</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">
                            {stats.avgCTR.toFixed(2)}%
                          </div>
                        </dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
        
        {/* Ad Campaigns List */}
        <div className="mt-8 glass-effect rounded-2xl border border-purple-500/20 shadow-xl overflow-hidden">
          <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
            <h3 className="text-lg font-bold leading-6 text-white">Ad Campaigns</h3>
          </div>
                  
          <div className="overflow-x-auto">
            {/* Mobile view - Card layout */}
            <div className="block md:hidden">
              {loading && filteredAndSortedCampaigns.length === 0 ? (
                // Mobile skeleton cards
                Array.from({ length: 5 }).map((_, index) => (
                  <div key={`skeleton-card-${index}`} className="bg-gray-800/30 m-4 p-4 rounded-lg border border-gray-700/50 animate-pulse">
                    <div className="flex items-center mb-4">
                      <div className="h-10 w-10 rounded-full bg-gray-700/50 mr-3"></div>
                      <div>
                        <div className="h-4 bg-gray-700/50 rounded w-24 mb-2"></div>
                        <div className="h-3 bg-gray-700/50 rounded w-32"></div>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-2 text-sm text-gray-300">
                      <div><div className="h-3 bg-gray-700/50 rounded w-16"></div></div>
                      <div><div className="h-3 bg-gray-700/50 rounded w-16"></div></div>
                      <div><div className="h-3 bg-gray-700/50 rounded w-16"></div></div>
                      <div><div className="h-3 bg-gray-700/50 rounded w-16"></div></div>
                    </div>
                    <div className="mt-3 flex justify-end space-x-2">
                      <div className="h-5 bg-gray-700/50 rounded w-16"></div>
                      <div className="h-5 bg-gray-700/50 rounded w-12"></div>
                      <div className="h-5 bg-gray-700/50 rounded w-12"></div>
                    </div>
                  </div>
                ))
              ) : (
                // Mobile campaign cards
                filteredAndSortedCampaigns.map((campaign) => (
                  <div key={campaign.id} className="bg-gray-800/30 m-4 p-4 rounded-lg border border-gray-700/50 hover:bg-white/5 transition-colors duration-200">
                    <div className="flex items-center mb-3">
                      <div className="flex-shrink-0 h-10 w-10">
                        <div className="h-10 w-10 rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center shadow-lg">
                          <span className="text-white font-bold text-lg">
                            {campaign.name.charAt(0)}
                          </span>
                        </div>
                      </div>
                      <div className="ml-3">
                        <div className="text-base font-bold text-white">{campaign.name}</div>
                        <div className="text-xs text-gray-400 max-w-xs truncate" title={campaign.description}>{campaign.description}</div>
                      </div>
                    </div>
                            
                    <div className="grid grid-cols-2 gap-3 text-sm text-gray-300 mb-3">
                      <div><span className="font-bold text-gray-400">Reward:</span> {campaign.reward} EKH</div>
                      <div><span className="font-bold text-gray-400">Duration:</span> {campaign.duration}s</div>
                      <div><span className="font-bold text-gray-400">Impressions:</span> {campaign.impressions.toLocaleString()}</div>
                      <div><span className="font-bold text-gray-400">Clicks:</span> {campaign.clicks.toLocaleString()}</div>
                      <div><span className="font-bold text-gray-400">CTR:</span> {calculateCTR(campaign.impressions, campaign.clicks)}%</div>
                      <div><span className="font-bold text-gray-400">Status:</span> 
                        <span className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(campaign.isActive)}`}>
                          {campaign.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </div>
                    </div>
                            
                    <div className="flex justify-end space-x-3 pt-2 border-t border-gray-700/50">
                      <button
                        onClick={() => toggleCampaignStatus(campaign.id)}
                        className="text-cyan-400 hover:text-cyan-300 font-medium text-sm transition-colors duration-200"
                      >
                        {campaign.isActive ? 'Deactivate' : 'Activate'}
                      </button>
                      <button 
                        onClick={() => {
                          setSelectedCampaign(campaign)
                          setShowEditModal(true)
                        }}
                        className="text-purple-400 hover:text-purple-300 font-medium text-sm transition-colors duration-200"
                      >
                        Edit
                      </button>
                      <button 
                        onClick={() => handleDeleteCampaign(campaign.id)}
                        className="text-red-500 hover:text-red-400 font-medium text-sm transition-colors duration-200"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                ))
              )}
            </div>
                    
            {/* Desktop view - Table layout */}
            <div className="hidden md:block min-w-full">
              {loading && filteredAndSortedCampaigns.length === 0 ? (
                // Show skeleton loaders when loading
                <table className="min-w-full divide-y divide-gray-700/50">
                  <thead className="glass-effect">
                    <tr>
                      {Array.from({ length: 8 }).map((_, index) => (
                        <th key={index} className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-700/30">
                    {Array.from({ length: 5 }).map((_, index) => (
                      <tr key={`skeleton-${index}`} className="hover:bg-white/5 transition-colors duration-200">
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-10 w-10">
                              <div className="h-10 w-10 rounded-full bg-gray-700/50 animate-pulse"></div>
                            </div>
                            <div className="ml-4">
                              <div className="h-4 bg-gray-700/50 rounded w-24 mb-2 animate-pulse"></div>
                              <div className="h-3 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-20 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="h-5 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap text-sm">
                          <div className="flex space-x-4">
                            <div className="h-5 bg-gray-700/50 rounded w-12 animate-pulse"></div>
                            <div className="h-5 bg-gray-700/50 rounded w-12 animate-pulse"></div>
                            <div className="h-5 bg-gray-700/50 rounded w-12 animate-pulse"></div>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                // Show actual campaign data
                <table className="min-w-full divide-y divide-gray-700/50">
                  <thead className="glass-effect">
                    <tr>
                      <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                        Campaign
                      </th>
                      <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                        Reward
                      </th>
                      <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                        Duration
                      </th>
                      <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                        Impressions
                      </th>
                      <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                        Clicks
                      </th>
                      <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                        CTR
                      </th>
                      <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                        Status
                      </th>
                      <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-700/30">
                    {filteredAndSortedCampaigns.map((campaign) => (
                      <tr key={campaign.id} className="hover:bg-white/5 transition-colors duration-200">
                        <td className="px-6 py-5 whitespace-nowrap">
                          <div className="flex items-center">
                            <div className="flex-shrink-0 h-10 w-10">
                              <div className="h-10 w-10 rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center shadow-lg">
                                <span className="text-white font-bold text-lg">
                                  {campaign.name.charAt(0)}
                                </span>
                              </div>
                            </div>
                            <div className="ml-4">
                              <div className="text-base font-bold text-white">{campaign.name}</div>
                              <div className="text-sm text-gray-400 max-w-md truncate" title={campaign.description}>{campaign.description}</div>
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap text-base font-bold text-cyan-400">
                          {campaign.reward} EKH
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-300">
                          {campaign.duration}s
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-300">
                          {campaign.impressions.toLocaleString()}
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-300">
                          {campaign.clicks.toLocaleString()}
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-300">
                          {calculateCTR(campaign.impressions, campaign.clicks)}%
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap">
                          <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(campaign.isActive)}`}>
                            {campaign.isActive ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="px-6 py-5 whitespace-nowrap text-sm">
                          <button
                            onClick={() => toggleCampaignStatus(campaign.id)}
                            className="text-cyan-400 hover:text-cyan-300 font-medium mr-4 transition-colors duration-200"
                          >
                            {campaign.isActive ? 'Deactivate' : 'Activate'}
                          </button>
                          <button 
                            onClick={() => {
                              setSelectedCampaign(campaign)
                              setShowEditModal(true)
                            }}
                            className="text-purple-400 hover:text-purple-300 font-medium mr-4 transition-colors duration-200"
                          >
                            Edit
                          </button>
                          <button 
                            onClick={() => handleDeleteCampaign(campaign.id)}
                            className="text-red-500 hover:text-red-400 font-medium transition-colors duration-200"
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
      </div>
      
      {/* Add Campaign Modal */}
      <Modal
        open={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Create New Ad Campaign"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300">Campaign Name</label>
            <input
              type="text"
              value={newCampaign.name}
              onChange={(e) => setNewCampaign({...newCampaign, name: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              placeholder="Enter campaign name"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Description</label>
            <textarea
              value={newCampaign.description}
              onChange={(e) => setNewCampaign({...newCampaign, description: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              placeholder="Enter campaign description"
              rows={3}
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Reward (EKH)</label>
            <input
              type="number"
              step="0.01"
              value={newCampaign.reward}
              onChange={(e) => setNewCampaign({...newCampaign, reward: parseFloat(e.target.value) || 0})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              placeholder="Enter reward amount"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Duration (seconds)</label>
            <input
              type="number"
              value={newCampaign.duration}
              onChange={(e) => setNewCampaign({...newCampaign, duration: parseInt(e.target.value) || 0})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              placeholder="Enter ad duration"
            />
          </div>
          
          <div className="flex items-center">
            <input
              id="active"
              name="active"
              type="checkbox"
              checked={newCampaign.isActive}
              onChange={(e) => setNewCampaign({...newCampaign, isActive: e.target.checked})}
              className="h-4 w-4 rounded border-gray-600 text-purple-500 focus:ring-purple-500 bg-gray-800/50"
            />
            <label htmlFor="active" className="ml-2 block text-sm text-gray-300">
              Active
            </label>
          </div>
        </div>
      </Modal>
      
      {/* Edit Campaign Modal */}
      <Modal
        open={showEditModal && selectedCampaign !== null}
        onClose={() => {
          setShowEditModal(false)
          setSelectedCampaign(null)
        }}
        title="Edit Ad Campaign"
      >
        {selectedCampaign && (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300">Campaign Name</label>
              <input
                type="text"
                value={selectedCampaign?.name || ''}
                onChange={(e) => selectedCampaign && setSelectedCampaign({...selectedCampaign, name: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter campaign name"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Description</label>
              <textarea
                value={selectedCampaign?.description || ''}
                onChange={(e) => selectedCampaign && setSelectedCampaign({...selectedCampaign, description: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter campaign description"
                rows={3}
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Reward (EKH)</label>
              <input
                type="number"
                step="0.01"
                value={selectedCampaign?.reward || 0}
                onChange={(e) => selectedCampaign && setSelectedCampaign({...selectedCampaign, reward: parseFloat(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter reward amount"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Duration (seconds)</label>
              <input
                type="number"
                value={selectedCampaign?.duration || 0}
                onChange={(e) => selectedCampaign && setSelectedCampaign({...selectedCampaign, duration: parseInt(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter ad duration"
              />
            </div>
            
            <div className="flex items-center">
              <input
                id="active"
                name="active"
                type="checkbox"
                checked={selectedCampaign?.isActive || false}
                onChange={(e) => selectedCampaign && setSelectedCampaign({...selectedCampaign, isActive: e.target.checked})}
                className="h-4 w-4 rounded border-gray-600 text-purple-500 focus:ring-purple-500 bg-gray-800/50"
              />
              <label htmlFor="active" className="ml-2 block text-sm text-gray-300">
                Active
              </label>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}