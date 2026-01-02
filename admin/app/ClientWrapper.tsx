'use client';

import { AuthProvider } from '@/contexts/AuthContext';
import { Suspense } from 'react';
import React from 'react';

// Loading component to show while chunks are loading
function LayoutLoading() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900">
      <div className="text-center">
        <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-purple-500 mx-auto mb-4"></div>
        <div className="text-xl font-semibold text-gray-300">Loading...</div>
      </div>
    </div>
  );
}

// Error boundary component to catch chunk loading errors
class LayoutErrorBoundary extends React.Component<{
  children: React.ReactNode;
}, { hasError: boolean }> {
  constructor(props: { children: React.ReactNode }) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Layout Error Boundary caught an error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900">
          <div className="text-center p-8 bg-gray-800 rounded-lg max-w-md">
            <h2 className="text-2xl font-bold text-red-400 mb-4">Loading Error</h2>
            <p className="text-gray-300 mb-4">There was an issue loading the application. Please refresh the page.</p>
            <button 
              onClick={() => window.location.reload()}
              className="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 transition-colors"
            >
              Refresh Page
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default function ClientWrapper({ children }: { children: React.ReactNode }) {
  return (
    <LayoutErrorBoundary>
      <Suspense fallback={<LayoutLoading />}>  
        <AuthProvider>
          {children}
        </AuthProvider>
      </Suspense>
    </LayoutErrorBoundary>
  );
}