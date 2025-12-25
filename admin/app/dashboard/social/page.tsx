'use client'

import { useState, useEffect, useMemo } from 'react'
import Button from '@/components/Button'
import Modal from '@/components/Modal'
import Alert from '@/components/Alert'
import SearchFilter from '@/components/SearchFilter'

interface SocialTask {
  id: string
  title: string
  description: string
  platform: string
  taskType: string
  rewardCoins: number
  actionUrl?: string
  verificationMethod: string
  verificationData?: Record<string, string> | null
  isActive: boolean
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export default function SocialPage() {
  const [tasks, setTasks] = useState<SocialTask[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [selectedTask, setSelectedTask] = useState<SocialTask | null>(null)
  
  const [newTask, setNewTask] = useState({
    title: '',
    description: '',
    platform: 'Twitter',
    taskType: 'generic',
    rewardCoins: 50,
    actionUrl: '',
    verificationMethod: 'manual',
    verificationData: {},
    isActive: true,
    sortOrder: 0
  })

  const [stats, setStats] = useState({
    totalTasks: 0,
    activeTasks: 0,
    inactiveTasks: 0
  })

  // Search, filter, and sort states
  const [searchQuery, setSearchQuery] = useState('')
  const [filters, setFilters] = useState<Record<string, any>>({})
  const [sortBy, setSortBy] = useState('')
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc')

  // Fetch social tasks from API
  useEffect(() => {
    const fetchTasks = async () => {
      try {
        setLoading(true)
        const response = await fetch('/api/social')
        const data = await response.json()
        
        if (data.success) {
          setTasks(data.data.tasks)
          setStats(data.data.stats)
        } else {
          setError(data.error || 'Failed to fetch social tasks')
        }
      } catch (err) {
        setError('Failed to fetch social tasks')
        console.error('Error fetching social tasks:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchTasks()
  }, [])

  // Filter and sort tasks
  const filteredAndSortedTasks = useMemo(() => {
    let result = [...tasks]

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      result = result.filter(task => 
        task.title.toLowerCase().includes(query) ||
        task.description.toLowerCase().includes(query)
      )
    }

    // Apply filters
    Object.keys(filters).forEach(key => {
      if (filters[key]) {
        result = result.filter(task => {
          if (key === 'platform') {
            return task.platform === filters[key]
          }
          if (key === 'status') {
            return task.isActive.toString() === filters[key]
          }
          return true
        })
      }
    })

    // Apply sorting
    if (sortBy) {
      result.sort((a, b) => {
        let aValue: any = a[sortBy as keyof SocialTask]
        let bValue: any = b[sortBy as keyof SocialTask]

        // Handle date sorting
        if (sortBy === 'createdAt' && a.createdAt && b.createdAt) {
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
  }, [tasks, searchQuery, filters, sortBy, sortOrder])

  const toggleTaskStatus = async (id: string) => {
    try {
      const task = tasks.find(t => t.id === id)
      if (!task) return
      
      const updatedTask = { ...task, isActive: !task.isActive }
      
      const response = await fetch('/api/social', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedTask)
      })
      
      const data = await response.json()
      
      if (data.success) {
        setTasks(tasks.map(t => t.id === id ? updatedTask : t))
        // Update stats
        setStats(prev => ({
          ...prev,
          activeTasks: updatedTask.isActive ? prev.activeTasks + 1 : prev.activeTasks - 1,
          inactiveTasks: updatedTask.isActive ? prev.inactiveTasks - 1 : prev.inactiveTasks + 1
        }))
      } else {
        setError(data.error || 'Failed to update task')
      }
    } catch (err) {
      setError('Failed to update task')
      console.error('Error updating task:', err)
    }
  }

  const handleAddTask = async () => {
    try {
      const taskToAdd = {
        ...newTask
      }
      
      const response = await fetch('/api/social', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(taskToAdd)
      })
      
      const data = await response.json()
      
      if (data.success) {
        // Update tasks with the response from the server
        setTasks([...tasks, data.data])
        setNewTask({
          title: '',
          description: '',
          platform: 'Twitter',
          taskType: 'generic',
          rewardCoins: 50,
          actionUrl: '',
          verificationMethod: 'manual',
          verificationData: {},
          isActive: true,
          sortOrder: 0
        })
        setShowAddModal(false)
        // Update stats
        setStats(prev => ({
          ...prev,
          totalTasks: prev.totalTasks + 1,
          activeTasks: newTask.isActive ? prev.activeTasks + 1 : prev.activeTasks,
          inactiveTasks: newTask.isActive ? prev.inactiveTasks : prev.inactiveTasks + 1
        }))
      } else {
        setError(data.error || 'Failed to add task')
      }
    } catch (err) {
      setError('Failed to add task')
      console.error('Error adding task:', err)
    }
  }

  const handleUpdateTask = async () => {
    if (!selectedTask) return
    
    try {
      const response = await fetch('/api/social', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(selectedTask)
      })
      
      const data = await response.json()
      
      if (data.success) {
        setTasks(tasks.map(t => t.id === selectedTask.id ? data.data : t))
        setShowEditModal(false)
        setSelectedTask(null)
      } else {
        setError(data.error || 'Failed to update task')
      }
    } catch (err) {
      setError('Failed to update task')
      console.error('Error updating task:', err)
    }
  }

  const handleDeleteTask = async (id: string) => {
    if (!confirm('Are you sure you want to delete this task?')) return
    
    try {
      const response = await fetch(`/api/social/${id}`, {
        method: 'DELETE'
      })
      
      const data = await response.json()
      
      if (data.success) {
        const deletedTask = tasks.find(t => t.id === id)
        setTasks(tasks.filter(task => task.id !== id))
        // Update stats
        if (deletedTask) {
          setStats(prev => ({
            ...prev,
            totalTasks: prev.totalTasks - 1,
            activeTasks: deletedTask.isActive ? prev.activeTasks - 1 : prev.activeTasks,
            inactiveTasks: deletedTask.isActive ? prev.inactiveTasks : prev.inactiveTasks - 1
          }))
        }
      } else {
        setError(data.error || 'Failed to delete task')
      }
    } catch (err) {
      setError('Failed to delete task')
      console.error('Error deleting task:', err)
    }
  }

  const getPlatformColor = (platform: string) => {
    switch (platform.toLowerCase()) {
      case 'twitter': return 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
      case 'telegram': return 'bg-blue-600/20 text-blue-400 border border-blue-600/30'
      case 'facebook': return 'bg-blue-800/20 text-blue-400 border border-blue-800/30'
      case 'instagram': return 'bg-pink-500/20 text-pink-400 border border-pink-500/30'
      case 'youtube': return 'bg-red-600/20 text-red-400 border border-red-600/30'
      case 'discord': return 'bg-indigo-600/20 text-indigo-400 border border-indigo-600/30'
      default: return 'bg-gray-500/20 text-gray-400 border border-gray-500/30'
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

  if (loading) {
    return (
      <div className="py-6 relative">
        <div className="particles"></div>
        
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          <div className="flex justify-between items-center">
            <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-400 via-cyan-400 to-pink-400 bg-clip-text text-transparent">
              Social Tasks Management
            </h1>
            <div className="text-sm text-gray-400">
              Loading...
            </div>
          </div>
        </div>
        
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          {/* Stats Skeletons */}
          <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-3">
            {Array.from({ length: 3 }).map((_, index) => (
              <div key={`stat-skeleton-${index}`} className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
                <div className="p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 rounded-xl bg-gray-700/50 p-3 shadow-lg">
                      <div className="h-6 w-6 bg-gray-600/50 rounded-full animate-pulse"></div>
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
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-700/50">
                <thead className="glass-effect">
                  <tr>
                    {Array.from({ length: 7 }).map((_, index) => (
                      <th key={index} className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                        <div className="h-4 bg-gray-700/50 rounded w-16 animate-pulse"></div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-700/30">
                  {Array.from({ length: 5 }).map((_, rowIndex) => (
                    <tr key={rowIndex} className="hover:bg-white/5 transition-colors duration-200">
                      {Array.from({ length: 7 }).map((_, colIndex) => (
                        <td key={colIndex} className="px-6 py-5 whitespace-nowrap animate-pulse">
                          <div className="h-4 bg-gray-700/50 rounded w-24"></div>
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="py-6">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-semibold text-white">Social Tasks Management</h1>
          <div className="flex space-x-2">
            <Button onClick={() => window.location.href = '/dashboard/social/validation'}>
              Validate Submissions
            </Button>
            <Button onClick={() => setShowAddModal(true)}>Add New Task</Button>
          </div>
        </div>
        
        {/* Stats */}
        <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-3">
          {loading && stats.totalTasks === 0 ? (
            // Show skeleton loaders when stats are loading
            Array.from({ length: 3 }).map((_, index) => (
              <div key={`stat-skeleton-${index}`} className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
                <div className="p-6">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 rounded-xl bg-gray-700/50 p-3 shadow-lg">
                      <div className="h-6 w-6 bg-gray-600/50 rounded-full animate-pulse"></div>
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
                      <svg className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                      </svg>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Total Tasks</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.totalTasks}</div>
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
                      <svg className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Active Tasks</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.activeTasks}</div>
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
                      <svg className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Inactive Tasks</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.inactiveTasks}</div>
                        </dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}
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
                name: 'platform',
                options: [
                  { value: 'Twitter', label: 'Twitter' },
                  { value: 'Telegram', label: 'Telegram' },
                  { value: 'Facebook', label: 'Facebook' },
                  { value: 'Instagram', label: 'Instagram' },
                  { value: 'YouTube', label: 'YouTube' },
                  { value: 'Discord', label: 'Discord' }
                ]
              },
              {
                name: 'status',
                options: [
                  { value: 'true', label: 'Active' },
                  { value: 'false', label: 'Inactive' }
                ]
              }
            ]}
            sortOptions={[
              { value: 'title:asc', label: 'Title (A-Z)' },
              { value: 'title:desc', label: 'Title (Z-A)' },
              { value: 'rewardCoins:asc', label: 'Reward (Low-High)' },
              { value: 'rewardCoins:desc', label: 'Reward (High-Low)' },
              { value: 'createdAt:asc', label: 'Date (Old-New)' },
              { value: 'createdAt:desc', label: 'Date (New-Old)' }
            ]}
          />
        </div>
        
        {/* Social Tasks List */}
        <div className="mt-6 glass-effect rounded-2xl shadow-2xl border border-purple-500/20 overflow-hidden">
          <div className="px-4 py-5 sm:px-6 border-b border-gray-700/50">
            <h3 className="text-lg font-medium leading-6 text-white">Social Tasks</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-700/50">
              <thead className="bg-gray-800/50">
                <tr>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Task
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Platform
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Type
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Reward
                  </th>
                  <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                    Verification
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
                {filteredAndSortedTasks.map((task) => (
                  <tr key={task.id} className="hover:bg-gray-700/20 transition-colors duration-200">
                    <td className="px-6 py-4">
                      <div className="text-sm font-medium text-white">{task.title}</div>
                      <div className="text-sm text-gray-400">{task.description}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getPlatformColor(task.platform)}`}>
                        {task.platform}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                      <span className="px-2 py-1 bg-gray-700/50 rounded text-xs">
                        {task.taskType}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                      {task.rewardCoins} EKH
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                      <span className={`px-2 py-1 rounded text-xs ${task.verificationMethod === 'auto' ? 'bg-green-500/20 text-green-400' : 'bg-yellow-500/20 text-yellow-400'}`}>
                        {task.verificationMethod}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        task.isActive 
                          ? 'bg-green-500/20 text-green-400 border border-green-500/30' 
                          : 'bg-red-500/20 text-red-400 border border-red-500/30'
                      }`}>
                        {task.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <button
                        onClick={() => toggleTaskStatus(task.id)}
                        className="text-cyan-400 hover:text-cyan-300 mr-3 transition-colors duration-200"
                      >
                        {task.isActive ? 'Deactivate' : 'Activate'}
                      </button>
                      <button 
                        onClick={() => {
                          setSelectedTask(task)
                          setShowEditModal(true)
                        }}
                        className="text-purple-400 hover:text-purple-300 mr-3 transition-colors duration-200"
                      >
                        Edit
                      </button>
                      <button 
                        onClick={() => handleDeleteTask(task.id)}
                        className="text-pink-400 hover:text-pink-300 transition-colors duration-200"
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
      
      {/* Add Task Modal */}
      <Modal
        open={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New Social Task"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300">Task Title</label>
            <input
              type="text"
              value={newTask.title}
              onChange={(e) => setNewTask({...newTask, title: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              placeholder="Enter task title"
            />
          </div>
                      
          <div>
            <label className="block text-sm font-medium text-gray-300">Task Type</label>
            <select
              value={newTask.taskType}
              onChange={(e) => setNewTask({...newTask, taskType: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
            >
              <option value="generic" className="bg-gray-800">Generic</option>
              <option value="follow" className="bg-gray-800">Follow</option>
              <option value="like" className="bg-gray-800">Like</option>
              <option value="share" className="bg-gray-800">Share</option>
              <option value="comment" className="bg-gray-800">Comment</option>
              <option value="join" className="bg-gray-800">Join</option>
            </select>
          </div>
                      
          <div>
            <label className="block text-sm font-medium text-gray-300">Action URL</label>
            <input
              type="text"
              value={newTask.actionUrl}
              onChange={(e) => setNewTask({...newTask, actionUrl: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              placeholder="Enter action URL (optional)"
            />
          </div>
                      
          <div>
            <label className="block text-sm font-medium text-gray-300">Verification Method</label>
            <select
              value={newTask.verificationMethod}
              onChange={(e) => setNewTask({...newTask, verificationMethod: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
            >
              <option value="manual" className="bg-gray-800">Manual</option>
              <option value="auto" className="bg-gray-800">Auto</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Platform</label>
            <select
              value={newTask.platform}
              onChange={(e) => setNewTask({...newTask, platform: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
            >
              <option value="Twitter" className="bg-gray-800">Twitter</option>
              <option value="Telegram" className="bg-gray-800">Telegram</option>
              <option value="Facebook" className="bg-gray-800">Facebook</option>
              <option value="Instagram" className="bg-gray-800">Instagram</option>
              <option value="YouTube" className="bg-gray-800">YouTube</option>
              <option value="Discord" className="bg-gray-800">Discord</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Description</label>
            <textarea
              value={newTask.description}
              onChange={(e) => setNewTask({...newTask, description: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              placeholder="Enter task description"
              rows={3}
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Reward (EKH)</label>
            <input
              type="number"
              value={newTask.rewardCoins}
              onChange={(e) => setNewTask({...newTask, rewardCoins: parseInt(e.target.value) || 0})}
              className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              placeholder="Enter reward amount"
            />
          </div>
          
          <div className="flex items-center">
            <input
              id="active"
              name="active"
              type="checkbox"
              checked={newTask.isActive}
              onChange={(e) => setNewTask({...newTask, isActive: e.target.checked})}
              className="h-4 w-4 rounded border-gray-600 text-purple-500 focus:ring-purple-500 bg-gray-800/50"
            />
            <label htmlFor="active" className="ml-2 block text-sm text-gray-300">
              Active
            </label>
          </div>
        </div>
      </Modal>
      
      {/* Edit Task Modal */}
      <Modal
        open={showEditModal && selectedTask !== null}
        onClose={() => {
          setShowEditModal(false)
          setSelectedTask(null)
        }}
        title="Edit Social Task"
      >
        {selectedTask && (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300">Task Title</label>
              <input
                type="text"
                value={selectedTask?.title || ''}
                onChange={(e) => selectedTask && setSelectedTask({...selectedTask, title: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter task title"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Task Type</label>
              <select
                value={selectedTask?.taskType || 'generic'}
                onChange={(e) => selectedTask && setSelectedTask({...selectedTask, taskType: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              >
                <option value="generic" className="bg-gray-800">Generic</option>
                <option value="follow" className="bg-gray-800">Follow</option>
                <option value="like" className="bg-gray-800">Like</option>
                <option value="share" className="bg-gray-800">Share</option>
                <option value="comment" className="bg-gray-800">Comment</option>
                <option value="join" className="bg-gray-800">Join</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Action URL</label>
              <input
                type="text"
                value={selectedTask?.actionUrl || ''}
                onChange={(e) => selectedTask && setSelectedTask({...selectedTask, actionUrl: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter action URL (optional)"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Verification Method</label>
              <select
                value={selectedTask?.verificationMethod || 'manual'}
                onChange={(e) => selectedTask && setSelectedTask({...selectedTask, verificationMethod: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              >
                <option value="manual" className="bg-gray-800">Manual</option>
                <option value="auto" className="bg-gray-800">Auto</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Platform</label>
              <select
                value={selectedTask?.platform || 'Twitter'}
                onChange={(e) => selectedTask && setSelectedTask({...selectedTask, platform: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
              >
                <option value="Twitter" className="bg-gray-800">Twitter</option>
                <option value="Telegram" className="bg-gray-800">Telegram</option>
                <option value="Facebook" className="bg-gray-800">Facebook</option>
                <option value="Instagram" className="bg-gray-800">Instagram</option>
                <option value="YouTube" className="bg-gray-800">YouTube</option>
                <option value="Discord" className="bg-gray-800">Discord</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Description</label>
              <textarea
                value={selectedTask?.description || ''}
                onChange={(e) => selectedTask && setSelectedTask({...selectedTask, description: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter task description"
                rows={3}
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Reward (EKH)</label>
              <input
                type="number"
                value={selectedTask?.rewardCoins || 0}
                onChange={(e) => selectedTask && setSelectedTask({...selectedTask, rewardCoins: parseInt(e.target.value) || 0})}
                className="mt-1 block w-full rounded-lg border border-gray-700 bg-gray-800/50 text-white shadow-sm focus:border-purple-500 focus:ring-purple-500 focus:ring-1 focus:ring-opacity-50 sm:text-sm px-3 py-2 transition-colors duration-200"
                placeholder="Enter reward amount"
              />
            </div>
            
            <div className="flex items-center">
              <input
                id="active"
                name="active"
                type="checkbox"
                checked={selectedTask?.isActive || false}
                onChange={(e) => selectedTask && setSelectedTask({...selectedTask, isActive: e.target.checked})}
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