'use client'

import { useState, useEffect, useMemo } from 'react'
import Button from '@/components/Button'
import Modal from '@/components/Modal'
import Alert from '@/components/Alert'
import SearchFilter from '@/components/SearchFilter'

interface SocialTaskSubmission {
  id: string
  userId: string
  taskId: string
  status: string
  completedAt: string
  verifiedAt: string | null
  proofUrl: string | null
  proofEmail: string | null
  proofData: Record<string, any> | null
  verificationAttempts: number
  rejectionReason: string | null
  username: string | null
  createdAt: string
  updatedAt: string
  task: {
    id: string
    title: string
    description: string
    platform: string
    rewardCoins: number
    taskType: string
  } | null
  user: {
    id: string
    name: string
    email: string
  } | null
}

export default function SocialTaskValidationPage() {
  const [submissions, setSubmissions] = useState<SocialTaskSubmission[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedSubmission, setSelectedSubmission] = useState<SocialTaskSubmission | null>(null)
  const [showDetailsModal, setShowDetailsModal] = useState(false)
  const [actionLoading, setActionLoading] = useState(false)
  
  // Filter and sort states
  const [searchQuery, setSearchQuery] = useState('')
  const [filters, setFilters] = useState<Record<string, any>>({
    status: 'pending'
  })
  const [sortBy, setSortBy] = useState('createdAt')
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc')

  // Fetch social task submissions from API
  useEffect(() => {
    const fetchSubmissions = async () => {
      try {
        setLoading(true)
        const queryParams = new URLSearchParams()
        
        // Add filters to query params
        Object.entries(filters).forEach(([key, value]) => {
          if (value) {
            queryParams.append(key, value)
          }
        })
        
        const response = await fetch(`/api/social/submissions?${queryParams.toString()}`)
        const data = await response.json()
        
        if (data.success) {
          setSubmissions(data.data.submissions)
        } else {
          setError(data.error || 'Failed to fetch social task submissions')
        }
      } catch (err) {
        setError('Failed to fetch social task submissions')
        console.error('Error fetching social task submissions:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchSubmissions()
  }, [filters])

  // Filter and sort submissions
  const filteredAndSortedSubmissions = useMemo(() => {
    let result = [...submissions]

    // Apply search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      result = result.filter(submission => 
        (submission.user?.name?.toLowerCase().includes(query) ||
        submission.user?.email?.toLowerCase().includes(query) ||
        submission.task?.title?.toLowerCase().includes(query))
      )
    }

    // Apply sorting
    if (sortBy) {
      result.sort((a, b) => {
        let aValue: any = a[sortBy as keyof SocialTaskSubmission]
        let bValue: any = b[sortBy as keyof SocialTaskSubmission]

        // Handle nested properties
        if (sortBy === 'task.title' && a.task && b.task) {
          aValue = a.task.title
          bValue = b.task.title
        } else if (sortBy === 'user.name' && a.user && b.user) {
          aValue = a.user.name
          bValue = b.user.name
        }

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
  }, [submissions, searchQuery, sortBy, sortOrder])

  const handleApprove = async (id: string) => {
    if (!confirm('Are you sure you want to approve this submission?')) return
    
    try {
      setActionLoading(true)
      const response = await fetch(`/api/social/submissions/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: 'verified' })
      })
      
      const data = await response.json()
      
      if (data.success) {
        // Update the submission in the list
        setSubmissions(submissions.map(s => 
          s.id === id ? { ...s, status: 'verified', verifiedAt: new Date().toISOString() } : s
        ))
      } else {
        setError(data.error || 'Failed to approve submission')
      }
    } catch (err) {
      setError('Failed to approve submission')
      console.error('Error approving submission:', err)
    } finally {
      setActionLoading(false)
    }
  }

  const handleReject = async (id: string) => {
    const reason = prompt('Please enter a reason for rejection:')
    if (!reason) return
    
    try {
      setActionLoading(true)
      const response = await fetch(`/api/social/submissions/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: 'rejected', rejectionReason: reason })
      })
      
      const data = await response.json()
      
      if (data.success) {
        // Update the submission in the list
        setSubmissions(submissions.map(s => 
          s.id === id ? { ...s, status: 'rejected', rejectionReason: reason } : s
        ))
      } else {
        setError(data.error || 'Failed to reject submission')
      }
    } catch (err) {
      setError('Failed to reject submission')
      console.error('Error rejecting submission:', err)
    } finally {
      setActionLoading(false)
    }
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'pending':
        return <span className="px-2 py-1 text-xs rounded-full bg-yellow-500/20 text-yellow-400 border border-yellow-500/30">Pending</span>
      case 'verified':
        return <span className="px-2 py-1 text-xs rounded-full bg-green-500/20 text-green-400 border border-green-500/30">Verified</span>
      case 'rejected':
        return <span className="px-2 py-1 text-xs rounded-full bg-red-500/20 text-red-400 border border-red-500/30">Rejected</span>
      default:
        return <span className="px-2 py-1 text-xs rounded-full bg-gray-500/20 text-gray-400 border border-gray-500/30">{status}</span>
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
      <div className="py-6">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          <h1 className="text-2xl font-semibold text-white">Social Task Validation</h1>
        </div>
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          <div className="mt-6 flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-500"></div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="py-6">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-semibold text-white">Social Task Validation</h1>
          <Button onClick={() => window.location.href = '/dashboard/social'}>
            Back to Tasks
          </Button>
        </div>
        
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
                  { value: 'pending', label: 'Pending' },
                  { value: 'verified', label: 'Verified' },
                  { value: 'rejected', label: 'Rejected' }
                ]
              }
            ]}
            sortOptions={[
              { value: 'createdAt:desc', label: 'Date (Newest)' },
              { value: 'createdAt:asc', label: 'Date (Oldest)' },
              { value: 'task.title:asc', label: 'Task Title (A-Z)' },
              { value: 'user.name:asc', label: 'User Name (A-Z)' }
            ]}
          />
        </div>
        
        {/* Stats */}
        <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-3">
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
                    <dt className="truncate text-sm font-medium text-gray-300">Total Submissions</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{submissions.length}</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
          
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-yellow-500/20">
            <div className="p-6 animated-gradient-slow">
              <div className="flex items-center">
                <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-yellow-500 to-yellow-600 p-3 shadow-lg">
                  <svg className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Pending Review</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">
                        {submissions.filter(s => s.status === 'pending').length}
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
                  <svg className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Verified</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">
                        {submissions.filter(s => s.status === 'verified').length}
                      </div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        {/* Social Task Submissions List */}
        <div className="mt-6 glass-effect rounded-2xl shadow-2xl border border-purple-500/20 overflow-hidden">
          <div className="px-4 py-5 sm:px-6 border-b border-gray-700/50">
            <h3 className="text-lg font-medium leading-6 text-white">Pending Submissions</h3>
          </div>
          <div className="overflow-x-auto">
            {filteredAndSortedSubmissions.length === 0 ? (
              <div className="text-center py-12">
                <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
                <h3 className="mt-2 text-sm font-medium text-gray-300">No submissions found</h3>
                <p className="mt-1 text-sm text-gray-500">
                  {Object.keys(filters).length > 0 || searchQuery 
                    ? 'No submissions match your current filters.' 
                    : 'There are currently no social task submissions to review.'}
                </p>
              </div>
            ) : (
              <table className="min-w-full divide-y divide-gray-700/50">
                <thead className="bg-gray-800/50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      User
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Task
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Platform
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Reward
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Submitted
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                      Proof Info
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
                  {filteredAndSortedSubmissions.map((submission) => (
                    <tr key={submission.id} className="hover:bg-gray-700/20 transition-colors duration-200">
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-white">
                          {submission.user?.name || 'Unknown User'}
                        </div>
                        <div className="text-sm text-gray-400">
                          {submission.user?.email || 'No email'}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="text-sm font-medium text-white">
                          {submission.task?.title || 'Unknown Task'}
                        </div>
                        <div className="text-sm text-gray-400">
                          {submission.task?.taskType}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {submission.task && (
                          <span className={`px-3 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getPlatformColor(submission.task.platform)}`}>
                            {submission.task.platform}
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-white">
                        {submission.task?.rewardCoins} EKH
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-400">
                        {new Date(submission.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4">
                        {(() => {
                          // Parse proofData if it's a string
                          let parsedProofData = submission.proofData;
                          if (typeof submission.proofData === 'string') {
                            try {
                              parsedProofData = JSON.parse(submission.proofData);
                            } catch (e) {
                              // If parsing fails, use raw data
                              parsedProofData = { raw: submission.proofData };
                            }
                          }
                          
                          return (
                            <>
                              {submission.proofEmail && (
                                <div className="text-sm text-gray-400">
                                  Email: {submission.proofEmail}
                                </div>
                              )}
                              {submission.user?.email && !submission.proofEmail && (
                                <div className="text-sm text-gray-400">
                                  User Email: {submission.user.email}
                                </div>
                              )}
                              {parsedProofData && typeof parsedProofData === 'object' && (
                                <>
                                  {parsedProofData.submitted_username && (
                                    <div className="text-sm text-gray-400">
                                      @{parsedProofData.submitted_username}
                                    </div>
                                  )}
                                  {parsedProofData.username && !parsedProofData.submitted_username && (
                                    <div className="text-sm text-gray-400">
                                      @{parsedProofData.username}
                                    </div>
                                  )}
                                  {parsedProofData.telegram_user_id && (
                                    <div className="text-sm text-gray-400">
                                      T: {parsedProofData.telegram_user_id}
                                    </div>
                                  )}
                                  {parsedProofData.proofEmail && (
                                    <div className="text-sm text-gray-400">
                                      Proof Email: {parsedProofData.proofEmail}
                                    </div>
                                  )}
                                  {parsedProofData.email && !parsedProofData.proofEmail && !submission.proofEmail && (
                                    <div className="text-sm text-gray-400">
                                      Email: {parsedProofData.email}
                                    </div>
                                  )}
                                  {parsedProofData.twitter_handle && (
                                    <div className="text-sm text-gray-400">
                                      @: {parsedProofData.twitter_handle}
                                    </div>
                                  )}
                                  {parsedProofData.submitted_proof_url && (
                                    <div>
                                      <a 
                                        href={parsedProofData.submitted_proof_url} 
                                        target="_blank" 
                                        rel="noopener noreferrer"
                                        className="text-cyan-400 hover:text-cyan-300 text-xs"
                                      >
                                        View Proof URL
                                      </a>
                                    </div>
                                  )}
                                  {parsedProofData.proof_url && !parsedProofData.submitted_proof_url && (
                                    <div>
                                      <a 
                                        href={parsedProofData.proof_url} 
                                        target="_blank" 
                                        rel="noopener noreferrer"
                                        className="text-cyan-400 hover:text-cyan-300 text-xs"
                                      >
                                        View Proof URL
                                      </a>
                                    </div>
                                  )}
                                </>
                              )}
                              {submission.proofUrl && !parsedProofData?.submitted_proof_url && !parsedProofData?.proof_url && (
                                <a 
                                  href={submission.proofUrl} 
                                  target="_blank" 
                                  rel="noopener noreferrer"
                                  className="text-cyan-400 hover:text-cyan-300 text-xs"
                                >
                                  View Proof
                                </a>
                              )}
                            </>
                          );
                        })()}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {getStatusBadge(submission.status)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        {submission.status === 'pending' ? (
                          <>
                            <button
                              onClick={() => handleApprove(submission.id)}
                              disabled={actionLoading}
                              className="text-green-400 hover:text-green-300 mr-3 transition-colors duration-200 disabled:opacity-50"
                            >
                              Approve
                            </button>
                            <button 
                              onClick={() => handleReject(submission.id)}
                              disabled={actionLoading}
                              className="text-red-400 hover:text-red-300 transition-colors duration-200 disabled:opacity-50"
                            >
                              Reject
                            </button>
                          </>
                        ) : (
                          <button
                            onClick={() => {
                              setSelectedSubmission(submission)
                              setShowDetailsModal(true)
                            }}
                            className="text-cyan-400 hover:text-cyan-300 transition-colors duration-200"
                          >
                            View Details
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
      
      {/* Submission Details Modal */}
      <Modal
        open={showDetailsModal && selectedSubmission !== null}
        onClose={() => {
          setShowDetailsModal(false)
          setSelectedSubmission(null)
        }}
        title="Submission Details"
      >
        {selectedSubmission && (
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <h4 className="text-sm font-medium text-gray-300">User</h4>
                <p className="text-white">{selectedSubmission.user?.name || 'Unknown User'}</p>
                <p className="text-gray-400 text-sm">{selectedSubmission.user?.email || 'No email'}</p>
              </div>
              
              <div>
                <h4 className="text-sm font-medium text-gray-300">Task</h4>
                <p className="text-white">{selectedSubmission.task?.title || 'Unknown Task'}</p>
                <p className="text-gray-400 text-sm">{selectedSubmission.task?.description || ''}</p>
              </div>
              
              <div>
                <h4 className="text-sm font-medium text-gray-300">Status</h4>
                <p>{getStatusBadge(selectedSubmission.status)}</p>
              </div>
              
              <div>
                <h4 className="text-sm font-medium text-gray-300">Submitted</h4>
                <p className="text-white">{new Date(selectedSubmission.createdAt).toLocaleString()}</p>
              </div>
              
              {(selectedSubmission.proofEmail || (selectedSubmission.user?.email && !selectedSubmission.proofEmail)) && (
                <div>
                  <h4 className="text-sm font-medium text-gray-300">Email</h4>
                  <p className="text-white">{selectedSubmission.proofEmail || selectedSubmission.user?.email}</p>
                </div>
              )}
              
              {selectedSubmission.verifiedAt && (
                <div>
                  <h4 className="text-sm font-medium text-gray-300">Verified</h4>
                  <p className="text-white">{new Date(selectedSubmission.verifiedAt).toLocaleString()}</p>
                </div>
              )}
              
              {selectedSubmission.rejectionReason && (
                <div>
                  <h4 className="text-sm font-medium text-gray-300">Rejection Reason</h4>
                  <p className="text-white">{selectedSubmission.rejectionReason}</p>
                </div>
              )}
            </div>
            
            {(selectedSubmission.proofData || selectedSubmission.proofUrl) && (
              <div>
                <h4 className="text-sm font-medium text-gray-300">Proof Details</h4>
                <div className="mt-2 bg-gray-800/50 rounded-lg p-3 space-y-2">
                  {(() => {
                    // Parse proofData if it's a string
                    let parsedProofData = selectedSubmission.proofData;
                    if (typeof selectedSubmission.proofData === 'string') {
                      try {
                        parsedProofData = JSON.parse(selectedSubmission.proofData);
                      } catch (e) {
                        // If parsing fails, treat as plain string
                        parsedProofData = { raw: selectedSubmission.proofData };
                      }
                    }
                    
                    return (
                      <>
                        {parsedProofData && typeof parsedProofData === 'object' && Object.keys(parsedProofData).length > 0 ? (
                          <>
                            {parsedProofData.platform && (
                              <div>
                                <span className="text-gray-400 text-sm">Platform:</span>
                                <span className="text-white ml-2">{parsedProofData.platform}</span>
                              </div>
                            )}
                            {parsedProofData.telegram_user_id && (
                              <div>
                                <span className="text-gray-400 text-sm">Telegram User ID:</span>
                                <span className="text-white ml-2">{parsedProofData.telegram_user_id}</span>
                              </div>
                            )}
                            {parsedProofData.submitted_telegram_id && (
                              <div>
                                <span className="text-gray-400 text-sm">Submitted Telegram ID:</span>
                                <span className="text-white ml-2">{parsedProofData.submitted_telegram_id}</span>
                              </div>
                            )}
                            {parsedProofData.username && (
                              <div>
                                <span className="text-gray-400 text-sm">Username:</span>
                                <span className="text-white ml-2">{parsedProofData.username}</span>
                              </div>
                            )}
                            {(parsedProofData.proofEmail || parsedProofData.email) && (
                              <div>
                                <span className="text-gray-400 text-sm">Email:</span>
                                <span className="text-white ml-2">{parsedProofData.proofEmail || parsedProofData.email}</span>
                              </div>
                            )}
                            {parsedProofData.submitted_username && (
                              <div>
                                <span className="text-gray-400 text-sm">Submitted Username:</span>
                                <span className="text-white ml-2">{parsedProofData.submitted_username}</span>
                              </div>
                            )}
                            {parsedProofData.username && !parsedProofData.submitted_username && (
                              <div>
                                <span className="text-gray-400 text-sm">Username:</span>
                                <span className="text-white ml-2">{parsedProofData.username}</span>
                              </div>
                            )}
                            {parsedProofData.twitter_handle && (
                              <div>
                                <span className="text-gray-400 text-sm">Twitter Handle:</span>
                                <span className="text-white ml-2">{parsedProofData.twitter_handle}</span>
                              </div>
                            )}
                            {parsedProofData.submitted_proof_url && (
                              <div>
                                <span className="text-gray-400 text-sm">Submitted Proof URL:</span>
                                <a 
                                  href={parsedProofData.submitted_proof_url} 
                                  target="_blank" 
                                  rel="noopener noreferrer"
                                  className="text-cyan-400 hover:text-cyan-300 transition-colors duration-200 ml-2 break-all"
                                >
                                  {parsedProofData.submitted_proof_url}
                                </a>
                              </div>
                            )}
                            {parsedProofData.proof_url && !parsedProofData.submitted_proof_url && (
                              <div>
                                <span className="text-gray-400 text-sm">Proof URL:</span>
                                <a 
                                  href={parsedProofData.proof_url} 
                                  target="_blank" 
                                  rel="noopener noreferrer"
                                  className="text-cyan-400 hover:text-cyan-300 transition-colors duration-200 ml-2 break-all"
                                >
                                  {parsedProofData.proof_url}
                                </a>
                              </div>
                            )}
                            {parsedProofData.post_url && (
                              <div>
                                <span className="text-gray-400 text-sm">Post URL:</span>
                                <a 
                                  href={parsedProofData.post_url} 
                                  target="_blank" 
                                  rel="noopener noreferrer"
                                  className="text-cyan-400 hover:text-cyan-300 transition-colors duration-200 ml-2 break-all"
                                >
                                  {parsedProofData.post_url}
                                </a>
                              </div>
                            )}
                            {parsedProofData.submitted_at && (
                              <div>
                                <span className="text-gray-400 text-sm">Submitted At:</span>
                                <span className="text-white ml-2">
                                  {new Date(parseInt(parsedProofData.submitted_at)).toLocaleString()}
                                </span>
                              </div>
                            )}
                            {Object.keys(parsedProofData).filter(key => 
                              ![
                                'platform', 
                                'telegram_user_id', 
                                'submitted_telegram_id', 
                                'username',
                                'submitted_username',
                                'proofEmail',
                                'email',
                                'twitter_handle', 
                                'proof_url',
                                'submitted_proof_url',
                                'post_url', 
                                'submitted_at'
                              ].includes(key)
                            ).map(key => (
                              <div key={key}>
                                <span className="text-gray-400 text-sm">{key}:</span>
                                <span className="text-white ml-2 break-all">{String(parsedProofData[key])}</span>
                              </div>
                            ))}
                          </>
                        ) : (
                          <div>
                            <span className="text-gray-400 text-sm">Raw Proof Data:</span>
                            <span className="text-white ml-2 break-all">{String(selectedSubmission.proofData)}</span>
                          </div>
                        )}
                      </>
                    );
                  })()}
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}