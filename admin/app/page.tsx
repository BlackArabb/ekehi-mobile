'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'

export default function Home() {
  const router = useRouter()

  useEffect(() => {
    // Redirect to dashboard
    router.push('/dashboard')
  }, [router])

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900">
      <div className="flex flex-col items-center">
        <div className="relative">
          <div className="w-32 h-32 border-8 border-purple-500 rounded-full animate-spin border-t-transparent"></div>
          <div className="absolute top-0 left-0 w-32 h-32 border-8 border-cyan-400 rounded-full animate-spin border-b-transparent opacity-50" style={{ animationDirection: 'reverse', animationDuration: '1.5s' }}></div>
        </div>
        <div className="mt-8 w-16 h-16 bg-gradient-to-br from-purple-500 to-cyan-400 rounded-full animate-pulse"></div>
      </div>
    </div>
  )
}