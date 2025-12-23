'use client'

import { createContext, useContext, useState, useEffect, ReactNode } from 'react'

interface Admin {
  id: string
  email: string
  name: string
}

interface AuthContextType {
  admin: Admin | null
  login: (email: string, password: string) => Promise<{ success: boolean; error?: string }>
  logout: () => void
  isLoading: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [admin, setAdmin] = useState<Admin | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    // Check if admin is already logged in (from localStorage or session)
    const storedAdmin = localStorage.getItem('admin')
    if (storedAdmin) {
      setAdmin(JSON.parse(storedAdmin))
    }
    setIsLoading(false)
  }, [])

  const login = async (email: string, password: string): Promise<{ success: boolean; error?: string }> => {
    setIsLoading(true)
    
    try {
      // In a real implementation, this would call your authentication API
      // For now, we'll simulate authentication with a delay
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // Simple validation for demo purposes
      if (email === 'admin@ekehi.com' && password === 'admin123') {
        const adminData: Admin = {
          id: 'admin-1',
          email: 'admin@ekehi.com',
          name: 'Admin User'
        }
        
        setAdmin(adminData)
        localStorage.setItem('admin', JSON.stringify(adminData))
        
        return { success: true }
      } else {
        return { success: false, error: 'Invalid email or password' }
      }
    } catch (error) {
      return { success: false, error: 'Authentication failed. Please try again.' }
    } finally {
      setIsLoading(false)
    }
  }

  const logout = async () => {
    setIsLoading(true);
    try {
      // Simulate a small delay for the logout process
      await new Promise(resolve => setTimeout(resolve, 300));
      
      setAdmin(null)
      localStorage.removeItem('admin')
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <AuthContext.Provider value={{ admin, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}