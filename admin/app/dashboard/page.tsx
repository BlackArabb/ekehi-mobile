'use client'

import { useState, useEffect } from 'react'
import { 
  CurrencyDollarIcon, 
  UsersIcon, 
  WalletIcon, 
  ChartBarIcon,
  ArrowTrendingUpIcon,
  GiftIcon,
  MegaphoneIcon
} from '@heroicons/react/24/outline'
import DashboardChart from '@/components/DashboardChart'
import { databases, collections } from '@/lib/appwrite'

// Types for our data
interface User {
  $id: string
  name: string
  email: string
  status: string
  createdAt: string
}

interface PresalePurchase {
  $id: string
  userId: string
  amount: number
  createdAt: string
}

interface SocialTask {
  $id: string
  title: string
  reward: number
}

interface AdView {
  $id: string
  userId: string
  createdAt: string
}

interface UserGrowthData {
  name: string;
  users: number;
}

interface RevenueData {
  name: string;
  value: number;
}

export default function Dashboard() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalRevenue: 0,
    activePresales: 0,
    pendingTasks: 0
  })
  
  const [recentActivity, setRecentActivity] = useState<any[]>([])
  const [userData, setUserData] = useState<UserGrowthData[]>([])
  const [revenueData, setRevenueData] = useState<RevenueData[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true)
        
        // Fetch users
        const usersResponse = await databases.listDocuments(
          '68c336e7000f87296feb',
          collections.users
        )
        
        // Fetch presale purchases
        const presaleResponse = await databases.listDocuments(
          '68c336e7000f87296feb',
          collections.presalePurchases
        )
        
        // Fetch social tasks
        const socialTasksResponse = await databases.listDocuments(
          '68c336e7000f87296feb',
          collections.socialTasks
        )
        
        // Fetch ad views
        const adViewsResponse = await databases.listDocuments(
          '68c336e7000f87296feb',
          collections.adViews
        )
        
        // Calculate statistics
        const totalUsers = usersResponse.total
        const totalRevenue = presaleResponse.documents.reduce((sum: number, purchase: any) => sum + (purchase.amount || 0), 0)
        const activePresales = presaleResponse.total
        const pendingTasks = socialTasksResponse.total
        
        setStats({
          totalUsers,
          totalRevenue,
          activePresales,
          pendingTasks
        })
        
        // Prepare recent activity (mock data for now, but using real structure)
        const activity = [
          { id: 1, user: 'Alex Johnson', action: 'Completed presale purchase', amount: '+$250', time: '2 min ago' },
          { id: 2, user: 'Maria Garcia', action: 'Completed social task', amount: '+50 EKH', time: '15 min ago' },
          { id: 3, user: 'David Smith', action: 'Watched ad', amount: '+0.1 EKH', time: '1 hour ago' },
          { id: 4, user: 'Sarah Williams', action: 'Referred new user', amount: '+100 EKH', time: '2 hours ago' },
          { id: 5, user: 'James Brown', action: 'Completed mining session', amount: '+25 EKH', time: '3 hours ago' },
        ]
        
        setRecentActivity(activity)
        
        // Prepare chart data
        const userGrowthData: UserGrowthData[] = [
          { name: 'Jan', users: 4000 },
          { name: 'Feb', users: 3000 },
          { name: 'Mar', users: 2000 },
          { name: 'Apr', users: 2780 },
          { name: 'May', users: 1890 },
          { name: 'Jun', users: 2390 },
          { name: 'Jul', users: 3490 },
        ]
        
        const revenueSourcesData: RevenueData[] = [
          { name: 'Presale', value: 400 },
          { name: 'Ads', value: 300 },
          { name: 'Referrals', value: 300 },
          { name: 'Other', value: 200 },
        ]
        
        setUserData(userGrowthData)
        setRevenueData(revenueSourcesData)
        
      } catch (error) {
        console.error('Error fetching dashboard data:', error)
      } finally {
        setLoading(false)
      }
    }
    
    fetchData()
  }, [])

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-500"></div>
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
            Welcome back, Admin
          </div>
        </div>
      </div>
      
      <div className="mx-auto max-w-7xl px-4 sm:px-6 md:px-8">
        {/* Stats */}
        <div className="mt-6 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
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
                      <div className="ml-2 text-sm text-green-400">+12%</div>
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
                  <CurrencyDollarIcon className="h-7 w-7 text-white" />
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Total Revenue</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">${stats.totalRevenue.toLocaleString()}</div>
                      <div className="ml-2 text-sm text-green-400">+25%</div>
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
                    <dt className="truncate text-sm font-medium text-gray-300">Active Presales</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.activePresales}</div>
                      <div className="ml-2 text-sm text-green-400">+8%</div>
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
                  <MegaphoneIcon className="h-7 w-7 text-white" />
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="truncate text-sm font-medium text-gray-300">Pending Tasks</dt>
                    <dd className="flex items-baseline">
                      <div className="text-3xl font-bold text-white drop-shadow-lg">{stats.pendingTasks}</div>
                      <div className="ml-2 text-sm text-yellow-400">-3%</div>
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Charts */}
        <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-2">
          {/* User Growth Chart */}
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

          {/* Revenue Sources */}
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-cyan-500/20">
            <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
              <h3 className="text-lg font-bold leading-6 text-white">Revenue Sources</h3>
            </div>
            <div className="p-5">
              <DashboardChart 
                title="Revenue Sources"
                type="pie"
                data={revenueData}
                dataKey="value"
                nameKey="name"
                height={300}
              />
            </div>
          </div>
        </div>

        <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-2">
          {/* Recent Activity */}
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-pink-500/20">
            <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
              <h3 className="text-lg font-bold leading-6 text-white">Recent Activity</h3>
            </div>
            <div className="border-t border-gray-700/30">
              <ul role="list" className="divide-y divide-gray-700/30">
                {recentActivity.map((activity) => (
                  <li key={activity.id} className="px-6 py-5 sm:px-7 hover:bg-white/5 transition-colors duration-200">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center">
                        <div className="flex-shrink-0">
                          <div className="h-12 w-12 rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center shadow-lg">
                            <span className="text-white font-bold text-lg">
                              {activity.user.charAt(0)}
                            </span>
                          </div>
                        </div>
                        <div className="ml-4">
                          <div className="text-base font-bold text-white">{activity.user}</div>
                          <div className="text-sm text-gray-300">{activity.action}</div>
                        </div>
                      </div>
                      <div className="flex items-center">
                        <div className="text-base font-bold text-cyan-400">{activity.amount}</div>
                        <div className="ml-4 text-sm text-gray-400">{activity.time}</div>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="glass-effect hover-glow overflow-hidden rounded-2xl shadow-2xl border border-orange-500/20">
            <div className="px-6 py-5 sm:px-7 border-b border-gray-700/50 animated-gradient">
              <h3 className="text-lg font-bold leading-6 text-white">Quick Actions</h3>
            </div>
            <div className="p-6">
              <div className="grid grid-cols-2 gap-5">
                <button className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 hover:bg-gradient-to-br hover:from-purple-500/10 hover:to-cyan-500/10 transition-all duration-300 transform hover:-translate-y-1">
                  <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-purple-500 to-purple-600 shadow-lg mb-3">
                    <UsersIcon className="h-7 w-7 text-white" />
                  </div>
                  <span className="text-base font-bold text-white">Manage Users</span>
                </button>
                <button className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 hover:bg-gradient-to-br hover:from-cyan-500/10 hover:to-blue-500/10 transition-all duration-300 transform hover:-translate-y-1">
                  <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-cyan-500 to-cyan-600 shadow-lg mb-3">
                    <CurrencyDollarIcon className="h-7 w-7 text-white" />
                  </div>
                  <span className="text-base font-bold text-white">Presale</span>
                </button>
                <button className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 hover:bg-gradient-to-br hover:from-pink-500/10 hover:to-purple-500/10 transition-all duration-300 transform hover:-translate-y-1">
                  <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-pink-500 to-pink-600 shadow-lg mb-3">
                    <WalletIcon className="h-7 w-7 text-white" />
                  </div>
                  <span className="text-base font-bold text-white">Wallet</span>
                </button>
                <button className="flex flex-col items-center justify-center rounded-xl border border-gray-700/50 p-6 hover:bg-gradient-to-br hover:from-orange-500/10 hover:to-red-500/10 transition-all duration-300 transform hover:-translate-y-1">
                  <div className="flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-orange-500 to-orange-600 shadow-lg mb-3">
                    <GiftIcon className="h-7 w-7 text-white" />
                  </div>
                  <span className="text-base font-bold text-white">Ads</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}