'use client'

import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { account } from '@/lib/appwriteClient'

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
    // Check if user is already logged in using Appwrite
    const checkSession = async () => {
      if (typeof window === 'undefined') return;
      
      try {
        // Try to get the current user session
        const currentUser = await account.get();
        if (currentUser) {
          const adminData: Admin = {
            id: currentUser.$id,
            email: currentUser.email,
            name: currentUser.name || currentUser.email
          };
          setAdmin(adminData);
        }
      } catch (error) {
        // If no session exists, user is not logged in
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
      // First, check if there's an existing session and delete it
      try {
        const currentSession = await account.get();
        if (currentSession) {
          console.log('Existing session found, deleting...');
          await account.deleteSession('current');
        }
      } catch (error) {
        // No existing session, continue with login
        console.log('No existing session to delete');
      }

      // Create new session with email/password
      await account.createEmailPasswordSession(email, password);
      
      // Get the current user after successful login
      const currentUser = await account.get();
      
      if (currentUser) {
        const adminData: Admin = {
          id: currentUser.$id,
          email: currentUser.email,
          name: currentUser.name || currentUser.email
        };
        
        setAdmin(adminData);
        return { success: true };
      } else {
        return { success: false, error: 'Could not retrieve user data after login' };
      }
    } catch (error: any) {
      console.error('Login error:', error);
      let errorMessage = 'Authentication failed. Please try again.';
      
      if (error?.message) {
        errorMessage = error.message;
      } else if (error?.type) {
        // Appwrite specific error types
        switch (error.type) {
          case 'user_invalid_credentials':
          case 'USER_INVALID':
            errorMessage = 'Invalid email or password';
            break;
          case 'user_not_found':
          case 'USER_NOT_FOUND':
            errorMessage = 'User not found';
            break;
          case 'user_invalid_token':
          case 'USER_PASSWORD_WRONG':
            errorMessage = 'Incorrect password';
            break;
          case 'rate_limit_exceeded':
          case 'RATE_LIMIT_EXCEEDED':
            errorMessage = 'Too many login attempts. Please try again later.';
            break;
          default:
            errorMessage = error.type;
        }
      }
      
      return { success: false, error: errorMessage };
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    setIsLoading(true);
    try {
      // Use Appwrite's logout function
      await account.deleteSession('current');
      setAdmin(null);
    } catch (error) {
      console.error('Logout error:', error);
      // Even if logout fails, clear local state
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