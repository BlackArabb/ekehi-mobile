'use client'

import { useState, useEffect, useMemo } from 'react'
import Button from '@/components/Button'
import Modal from '@/components/Modal'
import Alert from '@/components/Alert'
import SearchFilter from '@/components/SearchFilter'

interface User {
  id: string
  name: string
  email: string
  status: 'active' | 'inactive'
  role: 'admin' | 'user'
  createdAt: string
  lastLogin: string
  walletBalance: number
}

export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showAddModal, setShowAddModal] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  
  const [newUser, setNewUser] = useState({
    name: '',
    email: '',
    status: 'active' as 'active' | 'inactive',
    role: 'user' as 'admin' | 'user'
  })
  
  const [stats, setStats] = useState({
    totalUsers: 0,
    activeUsers: 0,
    inactiveUsers: 0,
    adminUsers: 0
  })

  // Search, filter, and sort states
  const [searchQuery, setSearchQuery] = useState('')
  const [filters, setFilters] = useState<Record<string, any>>({})
  const [sortBy, setSortBy] = useState('')
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc')

  useEffect(() => {
    fetchUsers()
  }, [])

  const fetchUsers = async () => {
    try {
      setLoading(true)
      const response = await fetch('/api/users')
      const data = await response.json()
      
      if (data.success) {
        setUsers(data.data.users)
        setStats(data.data.stats)
      } else {
        setError(data.error || 'Failed to fetch users')
      }
    } catch (error) {
      setError('Failed to fetch users')
      console.error('Error fetching users:', error)
    } finally {
      setLoading(false)
    }
  }

  // Filter and sort users
  const filteredAndSortedUsers = useMemo(() => {
    let result = [...users]

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      result = result.filter(user => 
        user.name.toLowerCase().includes(query) ||
        user.email.toLowerCase().includes(query)
      )
    }

    // Apply filters
    Object.keys(filters).forEach(key => {
      if (filters[key]) {
        result = result.filter(user => {
          if (key === 'status') {
            return user.status === filters[key]
          }
          if (key === 'role') {
            return user.role === filters[key]
          }
          return true
        })
      }
    })

    // Apply sorting
    if (sortBy) {
      result.sort((a, b) => {
        let aValue: any = a[sortBy as keyof User]
        let bValue: any = b[sortBy as keyof User]

        // Handle date sorting
        if (sortBy === 'createdAt' || sortBy === 'lastLogin') {
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
  }, [users, searchQuery, filters, sortBy, sortOrder])

  const handleAddUser = async () => {
    try {
      const response = await fetch('/api/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          ...newUser,
          id: Math.random().toString(36).substr(2, 9),
          createdAt: new Date().toISOString(),
          lastLogin: new Date().toISOString(),
          walletBalance: 0
        })
      })
      
      const data = await response.json()
      
      if (data.success) {
        setUsers([...users, data.data])
        setNewUser({
          name: '',
          email: '',
          status: 'active',
          role: 'user'
        })
        setShowAddModal(false)
        
        // Update stats
        setStats(prev => ({
          ...prev,
          totalUsers: prev.totalUsers + 1,
          activeUsers: newUser.status === 'active' ? prev.activeUsers + 1 : prev.activeUsers,
          inactiveUsers: newUser.status === 'inactive' ? prev.inactiveUsers + 1 : prev.inactiveUsers,
          adminUsers: newUser.role === 'admin' ? prev.adminUsers + 1 : prev.adminUsers
        }))
      } else {
        setError(data.error || 'Failed to add user')
      }
    } catch (error) {
      setError('Failed to add user')
      console.error('Error adding user:', error)
    }
  }

  const handleUpdateUser = async () => {
    if (!selectedUser) return
    
    try {
      const response = await fetch('/api/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(selectedUser)
      })
      
      const data = await response.json()
      
      if (data.success) {
        setUsers(users.map(u => u.id === selectedUser.id ? selectedUser : u))
        setShowEditModal(false)
        setSelectedUser(null)
      } else {
        setError(data.error || 'Failed to update user')
      }
    } catch (error) {
      setError('Failed to update user')
      console.error('Error updating user:', error)
    }
  }

  const handleDeleteUser = async (id: string) => {
    if (!confirm('Are you sure you want to delete this user?')) return
    
    try {
      const response = await fetch(`/api/users/${id}`, {
        method: 'DELETE'
      })
      
      const data = await response.json()
      
      if (data.success) {
        const deletedUser = users.find(u => u.id === id)
        setUsers(users.filter(user => user.id !== id))
        
        // Update stats
        if (deletedUser) {
          setStats(prev => ({
            ...prev,
            totalUsers: prev.totalUsers - 1,
            activeUsers: deletedUser.status === 'active' ? prev.activeUsers - 1 : prev.activeUsers,
            inactiveUsers: deletedUser.status === 'inactive' ? prev.inactiveUsers - 1 : prev.inactiveUsers,
            adminUsers: deletedUser.role === 'admin' ? prev.adminUsers - 1 : prev.adminUsers
          }))
        }
      } else {
        setError(data.error || 'Failed to delete user')
      }
    } catch (error) {
      setError('Failed to delete user')
      console.error('Error deleting user:', error)
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
          <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-400 to-cyan-400 bg-clip-text text-transparent">Users Management</h1>
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
            Users Management
          </h1>
          <Button 
            onClick={() => setShowAddModal(true)}
            className="bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 text-white font-bold py-2 px-6 rounded-lg shadow-lg hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1"
          >
            Add New User
          </Button>
        </div>
        
        {/* Stats */}
        <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
            <div className="p-6 animated-gradient-slow">
              <div className="flex items-center">
                <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-purple-500 to-purple-600 p-3 shadow-lg">
                  <svg className="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Total Users</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.totalUsers}</div>
                      <div className="ml-2 text-sm text-green-400">+12%</div>
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
                  <svg className="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Active Users</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.activeUsers}</div>
                      <div className="ml-2 text-sm text-green-400">+8%</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
          
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-red-500/20">
            <div className="p-6 animated-gradient-slow">
              <div className="flex items-center">
                <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-red-500 to-red-600 p-3 shadow-lg">
                  <svg className="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-3300">Inactive Users</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.inactiveUsers}</div>
                      <div className="ml-2 text-sm text-yellow-400">-2%</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
          
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
            <div className="p-6 animated-gradient-slow">
              <div className="flex items-center">
                <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-purple-500 to-purple-600 p-3 shadow-lg">
                  <svg className="h-7 w-7 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Admin Users</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.adminUsers}</div>
                      <div className="ml-2 text-sm text-green-400">+1</div>
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
        <SearchFilter
          onSearch={handleSearch}
          onFilter={handleFilter}
          onSort={handleSort}
          filterOptions={[
            {
              name: 'status',
              options: [
                { value: 'active', label: 'Active' },
                { value: 'inactive', label: 'Inactive' }
              ]
            },
            {
              name: 'role',
              options: [
                { value: 'admin', label: 'Admin' },
                { value: 'user', label: 'User' }
              ]
            }
          ]}
          sortOptions={[
            { value: 'name:asc', label: 'Name (A-Z)' },
            { value: 'name:desc', label: 'Name (Z-A)' },
            { value: 'email:asc', label: 'Email (A-Z)' },
            { value: 'email:desc', label: 'Email (Z-A)' },
            { value: 'walletBalance:asc', label: 'Wallet (Low-High)' },
            { value: 'walletBalance:desc', label: 'Wallet (High-Low)' },
            { value: 'lastLogin:asc', label: 'Last Login (Old-New)' },
            { value: 'lastLogin:desc', label: 'Last Login (New-Old)' }
          ]}
        />
        
        {/* Users table */}
        <div className="mt-6 glass-effect rounded-2xl border border-purple-500/20 shadow-xl overflow-hidden">
          <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
            <h3 className="text-lg font-bold leading-6 text-white">All Users</h3>
          </div>
          
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-700/50">
              <thead className="glass-effect">
                <tr>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    User
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Email
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Status
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Role
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Wallet
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Last Login
                  </th>
                  <th scope="col" className="px-6 py-4 text-left text-sm font-bold text-gray-300 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-700/30">
                {filteredAndSortedUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-white/5 transition-colors duration-200">
                    <td className="px-6 py-5 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-12 w-12">
                          <div className="h-12 w-12 rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center shadow-lg">
                            <span className="text-white font-bold text-lg">
                              {user.name.charAt(0)}
                            </span>
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-base font-bold text-white">{user.name}</div>
                          <div className="text-sm text-gray-400">Joined {formatDate(user.createdAt)}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap">
                      <div className="text-base text-gray-200">{user.email}</div>
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap">
                      <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        user.status === 'active' 
                          ? 'bg-green-500/20 text-green-400 border border-green-500/30' 
                          : 'bg-red-500/20 text-red-400 border border-red-500/30'
                      }`}>
                        {user.status}
                      </span>
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap">
                      <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        user.role === 'admin' 
                          ? 'bg-purple-500/20 text-purple-400 border border-purple-500/30' 
                          : 'bg-gray-500/20 text-gray-400 border border-gray-500/30'
                      }`}>
                        {user.role}
                      </span>
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap text-base font-bold text-cyan-400">
                      {user.walletBalance} EKH
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap text-sm text-gray-400">
                      {formatDate(user.lastLogin)}
                    </td>
                    <td className="px-6 py-5 whitespace-nowrap text-sm">
                      <button 
                        onClick={() => {
                          setSelectedUser(user)
                          setShowEditModal(true)
                        }}
                        className="text-cyan-400 hover:text-cyan-300 font-medium mr-4 transition-colors duration-200"
                      >
                        Edit
                      </button>
                      <button 
                        onClick={() => handleDeleteUser(user.id)}
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
      
      {/* Add User Modal */}
      <Modal
        open={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Add New User"
      >
        <div className="space-y-4 glass-effect border border-purple-500/20 rounded-2xl p-6">
          <div>
            <label className="block text-sm font-medium text-gray-300">Full Name</label>
            <input
              type="text"
              value={newUser.name}
              onChange={(e) => setNewUser({...newUser, name: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              placeholder="Enter full name"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Email</label>
            <input
              type="email"
              value={newUser.email}
              onChange={(e) => setNewUser({...newUser, email: e.target.value})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              placeholder="Enter email address"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Status</label>
            <select
              value={newUser.status}
              onChange={(e) => setNewUser({...newUser, status: e.target.value as 'active' | 'inactive'})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
            >
              <option value="active" className="bg-gray-800">Active</option>
              <option value="inactive" className="bg-gray-800">Inactive</option>
            </select>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300">Role</label>
            <select
              value={newUser.role}
              onChange={(e) => setNewUser({...newUser, role: e.target.value as 'admin' | 'user'})}
              className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
            >
              <option value="user" className="bg-gray-800">User</option>
              <option value="admin" className="bg-gray-800">Admin</option>
            </select>
          </div>
          <div className="mt-6 flex justify-end space-x-3">
            <Button 
              onClick={() => setShowAddModal(false)}
              className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white border border-gray-600 hover:border-gray-500 rounded-lg transition-colors duration-200"
            >
              Cancel
            </Button>
            <Button 
              onClick={handleAddUser}
              className="px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 rounded-lg shadow-lg transition-all duration-300"
            >
              Add User
            </Button>
          </div>
        </div>
      </Modal>
      
      {/* Edit User Modal */}
      <Modal
        open={showEditModal && selectedUser !== null}
        onClose={() => {
          setShowEditModal(false)
          setSelectedUser(null)
        }}
        title="Edit User"
      >
        {selectedUser && (
          <div className="space-y-4 glass-effect border border-purple-500/20 rounded-2xl p-6">
            <div>
              <label className="block text-sm font-medium text-gray-300">Full Name</label>
              <input
                type="text"
                value={selectedUser.name}
                onChange={(e) => setSelectedUser({...selectedUser, name: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
                placeholder="Enter full name"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Email</label>
              <input
                type="email"
                value={selectedUser.email}
                onChange={(e) => setSelectedUser({...selectedUser, email: e.target.value})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white placeholder-gray-400 focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
                placeholder="Enter email address"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Status</label>
              <select
                value={selectedUser.status}
                onChange={(e) => setSelectedUser({...selectedUser, status: e.target.value as 'active' | 'inactive'})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              >
                <option value="active" className="bg-gray-800">Active</option>
                <option value="inactive" className="bg-gray-800">Inactive</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-300">Role</label>
              <select
                value={selectedUser.role}
                onChange={(e) => setSelectedUser({...selectedUser, role: e.target.value as 'admin' | 'user'})}
                className="mt-1 block w-full rounded-lg border border-gray-700/50 bg-gray-800/50 text-white focus:border-purple-500 focus:ring-purple-500 sm:text-sm"
              >
                <option value="user" className="bg-gray-800">User</option>
                <option value="admin" className="bg-gray-800">Admin</option>
              </select>
            </div>
          </div>
        )}
        <div className="mt-6 flex justify-end space-x-3">
          <Button 
            onClick={() => {
              setShowEditModal(false)
              setSelectedUser(null)
            }}
            className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white border border-gray-600 hover:border-gray-500 rounded-lg transition-colors duration-200"
          >
            Cancel
          </Button>
          <Button 
            onClick={handleUpdateUser}
            className="px-4 py-2 text-sm font-medium text-white bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 rounded-lg shadow-lg transition-all duration-300"
          >
            Update User
          </Button>
        </div>
      </Modal>
    </div>
  )
}
