'use client'

import { useState, useEffect, useMemo } from 'react'
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

  return (
    <div className="py-6">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-semibold text-white">Ad Campaigns Management</h1>
          <Button onClick={() => setShowAddModal(true)}>Create Campaign</Button>
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
        
        {/* Search, Filter, and Sort */}
        <div className="mt-6">
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
        </div>
        
        {/* Ad Campaigns Stats */}
        <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-4">
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
        </div>
        
        {/* Ad Campaigns List */}
        <div className="mt-6 glass-effect rounded-2xl shadow-2xl border border-purple-500/20 overflow-hidden">
          <div className="px-4 py-5 sm:px-6 border-b border-gray-700/50">
            <h3 className="text-lg font-medium leading-6 text-white">Ad Campaigns</h3>
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
                      Campaign
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Reward
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Duration
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Impressions
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Clicks
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      CTR
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Status
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-gray-800/30 divide-y divide-gray-700/50">
                  {filteredAndSortedCampaigns.map((campaign) => (
                    <tr key={campaign.id} className="hover:bg-gray-700/20 transition-colors duration-200">
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-white">{campaign.name}</div>
                        <div className="text-sm text-gray-400">{campaign.description}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                        {campaign.reward} EKH
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                        {campaign.duration}s
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                        {campaign.impressions.toLocaleString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                        {campaign.clicks.toLocaleString()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                        {calculateCTR(campaign.impressions, campaign.clicks)}%
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(campaign.isActive)}`}>
                          {campaign.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <button
                          onClick={() => toggleCampaignStatus(campaign.id)}
                          className="text-cyan-400 hover:text-cyan-300 mr-3 transition-colors duration-200"
                        >
                          {campaign.isActive ? 'Deactivate' : 'Activate'}
                        </button>
                        <button 
                          onClick={() => {
                            setSelectedCampaign(campaign)
                            setShowEditModal(true)
                          }}
                          className="text-purple-400 hover:text-purple-300 mr-3 transition-colors duration-200"
                        >
                          Edit
                        </button>
                        <button 
                          onClick={() => handleDeleteCampaign(campaign.id)}
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
                value={selectedCampaign.name}
                onChange={(e) => setSelectedCampaign({...selectedCampaign, name: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter campaign name"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Description</label>
              <textarea
                value={selectedCampaign.description}
                onChange={(e) => setSelectedCampaign({...selectedCampaign, description: e.target.value})}
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
                value={selectedCampaign.reward}
                onChange={(e) => setSelectedCampaign({...selectedCampaign, reward: parseFloat(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter reward amount"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Duration (seconds)</label>
              <input
                type="number"
                value={selectedCampaign.duration}
                onChange={(e) => setSelectedCampaign({...selectedCampaign, duration: parseInt(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter ad duration"
              />
            </div>
            
            <div className="flex items-center">
              <input
                id="active"
                name="active"
                type="checkbox"
                checked={selectedCampaign.isActive}
                onChange={(e) => setSelectedCampaign({...selectedCampaign, isActive: e.target.checked})}
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