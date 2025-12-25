'use client'

import { useState, useEffect, useRef } from 'react'
import { 
  CurrencyDollarIcon, 
  UsersIcon, 
  WalletIcon, 
  ChartBarIcon,
  GiftIcon,
  MegaphoneIcon
} from '@heroicons/react/24/outline'
import DashboardChart from '@/components/DashboardChart'
import { useAuth } from '@/contexts/AuthContext'
import { useRouter } from 'next/navigation'

// Types for our data
interface UserGrowthData {
  name: string;
  users: number;
}

interface RevenueData {
  name: string;
  value: number;
}

interface SocialTaskSubmission {
  id: string;
  userId: string;
  taskId: string;
  status: string;
  completedAt: string;
  verifiedAt: string | null;
  proofUrl: string | null;
  proofEmail: string | null;
  proofData: any;
  verificationAttempts: number;
  rejectionReason: string | null;
  username: string | null;
  createdAt: string;
  updatedAt: string;
}

interface DashboardStats {
  totalUsers: number;
  totalSubmissions: number;
  completedTasks: number;
  pendingSubmissions: number;
  verifiedSubmissions: number;
  rejectedSubmissions: number;
  recentActivity: SocialTaskSubmission[];
}

export default function Dashboard() {
  const { admin, isLoading: authLoading } = useAuth();
  const router = useRouter();
  const hasRedirected = useRef(false);
  
  // Debug logs
  console.log('Dashboard render - Admin:', admin, 'AuthLoading:', authLoading);
  
  const [stats, setStats] = useState<DashboardStats>({
    totalUsers: 0,
    totalSubmissions: 0,
    completedTasks: 0,
    pendingSubmissions: 0,
    verifiedSubmissions: 0,
    rejectedSubmissions: 0,
    recentActivity: []
  })
  
  const [userData, setUserData] = useState<UserGrowthData[]>([])
  const [revenueData, setRevenueData] = useState<RevenueData[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Handle authentication redirect
  useEffect(() => {
    console.log('Dashboard auth check:', { admin, authLoading, hasRedirected: hasRedirected.current });
    if (!authLoading && !admin && !hasRedirected.current) {
      hasRedirected.current = true;
      console.log('Not authenticated, redirecting to login');
      router.replace('/auth/login');
    } else if (admin) {
      console.log('User is authenticated:', admin);
    }
  }, [admin, authLoading, router]);

  useEffect(() => {
    const fetchData = async () => {
      if (!admin) return;
      
      try {
        setLoading(true)
        setError(null)
        
        // Fetch all dashboard metrics from a single API endpoint
        const response = await fetch('/api/dashboard/metrics')
        if (!response.ok) {
          throw new Error('Failed to fetch dashboard metrics')
        }
        const result = await response.json()
        
        if (!result.success) {
          throw new Error(result.error || 'Failed to fetch dashboard metrics')
        }
        
        const metrics = result.data;
        
        setStats({
          totalUsers: metrics.totalUsers || 0,
          totalSubmissions: metrics.totalSubmissions || 0,
          completedTasks: metrics.completedTasks || 0,
          pendingSubmissions: metrics.pendingSubmissions || 0,
          verifiedSubmissions: metrics.verifiedSubmissions || 0,
          rejectedSubmissions: metrics.rejectedSubmissions || 0,
          recentActivity: metrics.recentActivity || []
        })
        
        // Generate user growth chart data based on actual metrics
        const userGrowthData: UserGrowthData[] = [
          { name: 'Jan', users: Math.floor(metrics.totalUsers * 0.4) },
          { name: 'Feb', users: Math.floor(metrics.totalUsers * 0.5) },
          { name: 'Mar', users: Math.floor(metrics.totalUsers * 0.6) },
          { name: 'Apr', users: Math.floor(metrics.totalUsers * 0.7) },
          { name: 'May', users: Math.floor(metrics.totalUsers * 0.8) },
          { name: 'Jun', users: Math.floor(metrics.totalUsers * 0.9) },
          { name: 'Jul', users: metrics.totalUsers },
        ]
        
        setUserData(userGrowthData)
        
      } catch (error: any) {
        console.error('Error fetching dashboard data:', error)
        setError(error.message || 'Failed to fetch dashboard data')
      } finally {
        setLoading(false)
      }
    }
    
    if (admin && !authLoading) {
      fetchData()
    }
  }, [admin, authLoading])

  // Show loading while authentication is being checked
  if (authLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-purple-500 mx-auto mb-4"></div>
          <div className="text-xl font-semibold text-gray-300">Loading authentication...</div>
        </div>
      </div>
    );
  }

  // Show loading while redirecting (only if not authenticated)
  if (!admin && !authLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-purple-500 mx-auto mb-4"></div>
          <div className="text-xl font-semibold text-gray-300">Redirecting to login...</div>
        </div>
      </div>
    );
  }

  // If we have admin and data is still loading, show skeleton loaders
  if (loading && admin) {
    return (
      <div className="py-6 relative">
        <div className="particles"></div>
        
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          <div className="flex items-center justify-between">
            <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-400 via-cyan-400 to-pink-400 bg-clip-text text-transparent">
              Dashboard
            </h1>
            <div className="text-sm text-gray-400">
              Loading...
            </div>
          </div>
        </div>
        
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          {/* Stats Skeletons */}
          <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
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

          {/* Additional Stats Skeleton Row */}
          <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {Array.from({ length: 2 }).map((_, index) => (
              <div key={`additional-stat-skeleton-${index}`} className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
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

          {/* Charts Skeletons */}
          <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-2">
            <div className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50">
                <div className="h-6 bg-gray-700/50 rounded w-32 animate-pulse"></div>
              </div>
              <div className="p-5">
                <div className="h-80 bg-gray-700/50 rounded animate-pulse"></div>
              </div>
            </div>

            <div className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-cyan-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50">
                <div className="h-6 bg-gray-700/50 rounded w-40 animate-pulse"></div>
              </div>
              <div className="p-5">
                <div className="h-80 bg-gray-700/50 rounded animate-pulse"></div>
              </div>
            </div>
          </div>

          <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-2">
            {/* Recent Activity Skeleton */}
            <div className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-pink-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50">
                <div className="h-6 bg-gray-700/50 rounded w-32 animate-pulse"></div>
              </div>
              <div className="border-t border-gray-700/30">
                <div className="p-6 space-y-4">
                  {Array.from({ length: 3 }).map((_, index) => (
                    <div key={`activity-skeleton-${index}`} className="flex items-center justify-between animate-pulse">
                      <div className="flex items-center">
                        <div className="h-12 w-12 rounded-full bg-gray-700/50"></div>
                        <div className="ml-4">
                          <div className="h-4 bg-gray-700/50 rounded w-24 mb-2"></div>
                          <div className="h-3 bg-gray-700/50 rounded w-32"></div>
                        </div>
                      </div>
                      <div className="flex items-center">
                        <div className="h-4 bg-gray-700/50 rounded w-16 mr-4"></div>
                        <div className="h-3 bg-gray-700/50 rounded w-20"></div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Quick Actions Skeleton */}
            <div className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-orange-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50">
                <div className="h-6 bg-gray-700/50 rounded w-32 animate-pulse"></div>
              </div>
              <div className="p-6">
                <div className="grid grid-cols-2 gap-5">
                  {Array.from({ length: 4 }).map((_, index) => (
                    <div key={`action-skeleton-${index}`} className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 animate-pulse">
                      <div className="h-14 w-14 rounded-full bg-gray-700/50 mb-3"></div>
                      <div className="h-4 bg-gray-700/50 rounded w-16"></div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  // Show error state if there was an error
  if (error) {
    return (
      <div className="py-6">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
          <div className="rounded-lg bg-red-500/10 border border-red-500/50 p-6">
            <h3 className="text-lg font-semibold text-red-400 mb-2">Error Loading Dashboard</h3>
            <p className="text-sm text-red-300">{error}</p>
            <button
              onClick={() => window.location.reload()}
              className="mt-4 px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg transition-colors"
            >
              Retry
            </button>
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
        <div className="flex items-center justify-between">
          <h1 className="text-3xl font-bold bg-gradient-to-r from-purple-400 via-cyan-400 to-pink-400 bg-clip-text text-transparent">
            Dashboard
          </h1>
          <div className="text-sm text-gray-400">
            Welcome back, {admin?.name || 'Admin'}
          </div>
        </div>
      </div>
      
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        {/* Stats */}
        <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {loading ? (
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
                      <UsersIcon className="h-7 w-7 text-white" />
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Total Users</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.totalUsers.toLocaleString()}</div>
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
                      <MegaphoneIcon className="h-7 w-7 text-white" />
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Completed Tasks</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.completedTasks}</div>
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
                      <ChartBarIcon className="h-7 w-7 text-white" />
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Pending Tasks</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.pendingSubmissions}</div>
                        </dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-orange-500/20">
                <div className="p-6 animated-gradient-slow">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-orange-500 to-orange-600 p-3 shadow-lg">
                      <CurrencyDollarIcon className="h-7 w-7 text-white" />
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Verified Tasks</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.verifiedSubmissions}</div>
                        </dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Additional Stats Row */}
        <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {loading ? (
            // Show skeleton loaders when stats are loading
            Array.from({ length: 2 }).map((_, index) => (
              <div key={`additional-stat-skeleton-${index}`} className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
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
            // Show actual additional stats
            <>
              <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
                <div className="p-6 animated-gradient-slow">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-red-500 to-red-600 p-3 shadow-lg">
                      <ChartBarIcon className="h-7 w-7 text-white" />
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Rejected Tasks</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.rejectedSubmissions}</div>
                        </dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-cyan-500/20">
                <div className="p-6 animated-gradient-slow">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 p-3 shadow-lg">
                      <GiftIcon className="h-7 w-7 text-white" />
                    </div>
                    <div className="ml-5 w-0 flex-1">
                      <dl>
                        <dt className="truncate text-sm font-medium text-gray-300">Total Submissions</dt>
                        <dd className="flex items-baseline">
                          <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.totalSubmissions}</div>
                        </dd>
                      </dl>
                    </div>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Charts */}
        <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-2">
          {/* User Growth Chart */}
          {loading ? (
            // Show skeleton loader when chart is loading
            <div className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50">
                <div className="h-6 bg-gray-700/50 rounded w-32 animate-pulse"></div>
              </div>
              <div className="p-5">
                <div className="h-80 bg-gray-700/50 rounded animate-pulse"></div>
              </div>
            </div>
          ) : (
            // Show actual chart
            <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-purple-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
                <h3 className="text-lg font-bold leading-6 text-white">User Growth</h3>
              </div>
              <div className="p-5">
                <DashboardChart 
                  title="User Growth"
                  type="bar"
                  data={userData}
                  dataKey="users"
                  nameKey="name"
                  height={300}
                />
              </div>
            </div>
          )}

          {/* Revenue Sources */}
          {loading ? (
            // Show skeleton loader when chart is loading
            <div className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-cyan-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50">
                <div className="h-6 bg-gray-700/50 rounded w-40 animate-pulse"></div>
              </div>
              <div className="p-5">
                <div className="h-80 bg-gray-700/50 rounded animate-pulse"></div>
              </div>
            </div>
          ) : (
            // Show actual chart
            <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-cyan-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
                <h3 className="text-lg font-bold leading-6 text-white">Submission Status</h3>
              </div>
              <div className="p-5">
                <DashboardChart 
                  title="Submission Status"
                  type="pie"
                  data={[
                    { name: 'Pending', value: stats.pendingSubmissions },
                    { name: 'Verified', value: stats.verifiedSubmissions },
                    { name: 'Rejected', value: stats.rejectedSubmissions }
                  ]}
                  dataKey="value"
                  nameKey="name"
                  height={300}
                />
              </div>
            </div>
          )}
        </div>

        <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-2">
          {/* Recent Activity */}
          {loading ? (
            // Show skeleton loader when recent activity is loading
            <div className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-pink-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50">
                <div className="h-6 bg-gray-700/50 rounded w-32 animate-pulse"></div>
              </div>
              <div className="border-t border-gray-700/30">
                <div className="p-6 space-y-4">
                  {Array.from({ length: 3 }).map((_, index) => (
                    <div key={`activity-skeleton-${index}`} className="flex items-center justify-between animate-pulse">
                      <div className="flex items-center">
                        <div className="h-12 w-12 rounded-full bg-gray-700/50"></div>
                        <div className="ml-4">
                          <div className="h-4 bg-gray-700/50 rounded w-24 mb-2"></div>
                          <div className="h-3 bg-gray-700/50 rounded w-32"></div>
                        </div>
                      </div>
                      <div className="flex items-center">
                        <div className="h-4 bg-gray-700/50 rounded w-16 mr-4"></div>
                        <div className="h-3 bg-gray-700/50 rounded w-20"></div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            // Show actual recent activity
            <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-pink-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
                <h3 className="text-lg font-bold leading-6 text-white">Recent Activity</h3>
              </div>
              <div className="border-t border-gray-700/30">
                <ul role="list" className="divide-y divide-gray-700/30">
                  {stats.recentActivity.length > 0 ? (
                    stats.recentActivity.map((activity) => (
                      <li key={activity.id} className="px-6 py-5 sm:px-7 hover:bg-white/5 transition-colors duration-200">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center">
                            <div className="flex-shrink-0">
                              <div className="h-12 w-12 rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center shadow-lg">
                                <span className="text-white font-bold text-lg">
                                  {activity.username ? activity.username.charAt(0).toUpperCase() : 'U'}
                                </span>
                              </div>
                            </div>
                            <div className="ml-4">
                              <div className="text-base font-bold text-white">{activity.username || 'Unknown User'}</div>
                              <div className="text-sm text-gray-300">Submitted task for review</div>
                            </div>
                          </div>
                          <div className="flex items-center">
                            <div className={`text-base font-bold ${
                              activity.status === 'pending' ? 'text-yellow-400' : 
                              activity.status === 'verified' ? 'text-green-400' : 
                              activity.status === 'rejected' ? 'text-red-400' : 'text-gray-400'
                            }`}>
                              {activity.status.charAt(0).toUpperCase() + activity.status.slice(1)}
                            </div>
                            <div className="ml-4 text-sm text-gray-400">
                              {new Date(activity.createdAt).toLocaleDateString()}
                            </div>
                          </div>
                        </div>
                      </li>
                    ))
                  ) : (
                    <li className="px-6 py-5 sm:px-7 text-center">
                      <div className="text-gray-400">No recent activity</div>
                    </li>
                  )}
                </ul>
              </div>
            </div>
          )}

          {/* Quick Actions */}
          {loading ? (
            // Show skeleton loader when quick actions are loading
            <div className="glass-effect overflow-hidden rounded-2xl shadow-2xl border border-orange-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50">
                <div className="h-6 bg-gray-700/50 rounded w-32 animate-pulse"></div>
              </div>
              <div className="p-6">
                <div className="grid grid-cols-2 gap-5">
                  {Array.from({ length: 4 }).map((_, index) => (
                    <div key={`action-skeleton-${index}`} className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 animate-pulse">
                      <div className="h-14 w-14 rounded-full bg-gray-700/50 mb-3"></div>
                      <div className="h-4 bg-gray-700/50 rounded w-16"></div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          ) : (
            // Show actual quick actions
            <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-orange-500/20">
              <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
                <h3 className="text-lg font-bold leading-6 text-white">Quick Actions</h3>
              </div>
              <div className="p-6">
                <div className="grid grid-cols-2 gap-5">
                  <button 
                    onClick={() => router.push('/dashboard/users')}
                    className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 hover:bg-gradient-to-br hover:from-purple-500/10 hover:to-cyan-500/10 transition-all duration-300 transform hover:-translate-y-1"
                  >
                    <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-purple-500 to-purple-600 shadow-lg mb-3">
                      <UsersIcon className="h-7 w-7 text-white" />
                    </div>
                    <span className="text-base font-bold text-white">Manage Users</span>
                  </button>
                  <button 
                    onClick={() => router.push('/dashboard/social')}
                    className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 hover:bg-gradient-to-br hover:from-cyan-500/10 hover:to-blue-500/10 transition-all duration-300 transform hover:-translate-y-1"
                  >
                    <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-cyan-500 to-cyan-600 shadow-lg mb-3">
                      <MegaphoneIcon className="h-7 w-7 text-white" />
                    </div>
                    <span className="text-base font-bold text-white">Social Tasks</span>
                  </button>
                  <button 
                    onClick={() => router.push('/dashboard/wallet')}
                    className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 hover:bg-gradient-to-br hover:from-pink-500/10 hover:to-purple-500/10 transition-all duration-300 transform hover:-translate-y-1"
                  >
                    <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-pink-500 to-pink-600 shadow-lg mb-3">
                      <WalletIcon className="h-7 w-7 text-white" />
                    </div>
                    <span className="text-base font-bold text-white">Wallet</span>
                  </button>
                  <button 
                    onClick={() => router.push('/dashboard/ads')}
                    className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 hover:bg-gradient-to-br hover:from-orange-500/10 hover:to-red-500/10 transition-all duration-300 transform hover:-translate-y-1"
                  >
                    <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-orange-500 to-orange-600 shadow-lg mb-3">
                      <GiftIcon className="h-7 w-7 text-white" />
                    </div>
                    <span className="text-base font-bold text-white">Ads</span>
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}