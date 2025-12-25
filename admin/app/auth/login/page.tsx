'use client'

import { useState, useEffect, useRef } from 'react'
import { useRouter } from 'next/navigation'
import { useAuth } from '@/contexts/AuthContext'
import { LockClosedIcon, EnvelopeIcon } from '@heroicons/react/24/outline'

export default function Login() {
  const router = useRouter()
  const { login, isLoading: authLoading, admin } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const hasRedirected = useRef(false) // Prevent multiple redirects
  const [isRedirecting, setIsRedirecting] = useState(false) // Track redirect state

  // Redirect if already logged in
  useEffect(() => {
    if (admin && !authLoading && !hasRedirected.current && !isRedirecting) {
      hasRedirected.current = true
      setIsRedirecting(true)
      console.log('User already logged in, redirecting to dashboard');
      console.log('Will redirect in 100ms')
      setTimeout(() => {
        console.log('Executing redirect to /dashboard from useEffect')
        router.replace('/dashboard');
      }, 100);
    }
  }, [admin, authLoading, router, isRedirecting]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    
    try {
      console.log('Attempting login with email:', email)
      const result = await login(email, password)
      console.log('Login result:', result)
      
      if (result.success) {
        console.log('Login successful, redirecting to dashboard')
        setIsRedirecting(true)
        console.log('Set isRedirecting to true, will redirect in 100ms')
        setTimeout(() => {
          console.log('Executing redirect to /dashboard')
          router.replace('/dashboard')
        }, 100);
      } else {
        setError(result.error || 'Login failed')
        console.log('Login failed with error:', result.error)
      }
    } catch (err) {
      setError('An unexpected error occurred')
      console.error('Login error:', err)
    } finally {
      setLoading(false)
    }
  }

  // Debug: Show current auth state
  console.log('Login page - Current admin state:', admin)
  console.log('Login page - Auth loading state:', authLoading)
  console.log('Login page - Is redirecting:', isRedirecting)
  console.log('Login page - Has redirected ref:', hasRedirected.current)

  // If redirecting, show redirect screen immediately
  if (isRedirecting) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-purple-500 mx-auto mb-4"></div>
          <div className="text-xl font-semibold text-gray-300">Redirecting to dashboard...</div>
        </div>
      </div>
    );
  }

  // Show loading while checking auth
  if (authLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-purple-500 mx-auto mb-4"></div>
          <div className="text-xl font-semibold text-gray-300">Loading...</div>
        </div>
      </div>
    );
  }

  // If already logged in, show redirecting message
  if (admin) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-purple-500 mx-auto mb-4"></div>
          <div className="text-xl font-semibold text-gray-300">Redirecting to dashboard...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900 py-12 px-4 sm:px-6 lg:px-8">
      {/* Floating particles effect */}
      <div className="particles"></div>
      
      <div className="w-full max-w-md space-y-8 relative z-10">
        <div>
          <div className="mx-auto h-20 w-20 rounded-full bg-gradient-to-br from-purple-500 to-cyan-500 flex items-center justify-center shadow-2xl">
            <span className="text-white font-bold text-3xl">E</span>
          </div>
          <h2 className="mt-6 text-center text-4xl font-bold bg-gradient-to-r from-purple-400 via-cyan-400 to-pink-400 bg-clip-text text-transparent">
            Admin Login
          </h2>
          <p className="mt-2 text-center text-sm text-gray-400">
            Sign in to access the Ekehi admin dashboard
          </p>
        </div>

        <div className="glass-effect rounded-2xl shadow-2xl border border-purple-500/20 p-8">
          <form className="space-y-6" onSubmit={handleSubmit}>
            {error && (
              <div className="rounded-lg bg-red-500/10 border border-red-500/50 p-4">
                <p className="text-sm text-red-400">{error}</p>
              </div>
            )}

            <div className="space-y-4">
              <div>
                <label htmlFor="email-address" className="block text-sm font-medium text-gray-300 mb-2">
                  Email address
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <EnvelopeIcon className="h-5 w-5 text-gray-400" />
                  </div>
                  <input
                    id="email-address"
                    name="email"
                    type="email"
                    autoComplete="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="block w-full pl-10 pr-3 py-3 border border-gray-600 rounded-lg bg-gray-800/50 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                    placeholder="Enter your email"
                  />
                </div>
              </div>

              <div>
                <label htmlFor="password" className="block text-sm font-medium text-gray-300 mb-2">
                  Password
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <LockClosedIcon className="h-5 w-5 text-gray-400" />
                  </div>
                  <input
                    id="password"
                    name="password"
                    type="password"
                    autoComplete="current-password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="block w-full pl-10 pr-3 py-3 border border-gray-600 rounded-lg bg-gray-800/50 text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
                    placeholder="Enter your password"
                  />
                </div>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <input
                  id="remember-me"
                  name="remember-me"
                  type="checkbox"
                  className="h-4 w-4 rounded border-gray-600 bg-gray-800/50 text-purple-500 focus:ring-purple-500 focus:ring-offset-gray-900"
                />
                <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-300">
                  Remember me
                </label>
              </div>

              <div className="text-sm">
                <a href="#" className="font-medium text-purple-400 hover:text-purple-300 transition-colors">
                  Forgot password?
                </a>
              </div>
            </div>

            <div>
              <button
                type="submit"
                disabled={loading || authLoading}
                className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-medium rounded-lg text-white bg-gradient-to-r from-purple-500 to-cyan-500 hover:from-purple-600 hover:to-cyan-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-purple-500 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-xl transform hover:-translate-y-0.5"
              >
                {loading || authLoading ? (
                  <div className="flex items-center">
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                    Signing in...
                  </div>
                ) : (
                  'Sign in'
                )}
              </button>
            </div>
          </form>
        </div>

        <div className="text-center">
          <p className="text-sm text-gray-500">
            © 2024 Ekehi Admin Dashboard. All rights reserved.
          </p>
        </div>
      </div>
    </div>
  )
}