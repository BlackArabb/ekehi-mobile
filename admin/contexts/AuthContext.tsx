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
  logout: () => Promise<void>
  isLoading: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [admin, setAdmin] = useState<Admin | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isInitialized, setIsInitialized] = useState(false)

  useEffect(() => {
    // Check if user is already logged in using server API
    const checkSession = async () => {
      if (typeof window === 'undefined') return;
      
      try {
        const response = await fetch('/api/auth/session');
        const result = await response.json();
        
        if (result.success) {
          const adminData: Admin = {
            id: result.data.userId,
            email: result.data.email,
            name: result.data.name
          };
          setAdmin(adminData);
        } else {
          setAdmin(null);
        }
      } catch (error) {
        console.log('No active session found');
        setAdmin(null);
      } finally {
        setIsLoading(false);
        setIsInitialized(true);
      }
    };

    checkSession();
  }, []);

  const login = async (email: string, password: string): Promise<{ success: boolean; error?: string }> => {
    setIsLoading(true);
    
    try {
      // Use server-side API to avoid CORS issues
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      
      const result = await response.json();
      
      if (result.success) {
        const adminData: Admin = {
          id: result.data.userId,
          email: result.data.email,
          name: result.data.name
        };
        
        setAdmin(adminData);
        return { success: true };
      } else {
        // Show more detailed error for debugging
        const detailedError = result.details || result.error;
        console.log('Login failed - Details:', result);
        return { success: false, error: detailedError || 'Login failed' };
      }
    } catch (error: any) {
      console.error('Login error:', error);
      return { success: false, error: error.message || 'Authentication failed' };
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    setIsLoading(true);
    try {
      await fetch('/api/auth/logout', { method: 'DELETE' });
      setAdmin(null);
    } catch (error) {
      console.error('Logout error:', error);
      setAdmin(null);
    } finally {
      setIsLoading(false);
    }
  };

  // Don't render children until auth is initialized to prevent hydration mismatch
  if (!isInitialized) {
    return (
      <AuthContext.Provider value={{ admin, login, logout, isLoading: true }}>
        <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-purple-500 mx-auto mb-4"></div>
            <div className="text-xl font-semibold text-gray-300">Initializing...</div>
          </div>
        </div>
      </AuthContext.Provider>
    );
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