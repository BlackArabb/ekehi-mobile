import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

// List of paths that require authentication
const protectedPaths = [
  '/dashboard',
  '/api/users',
  '/api/presale',
  '/api/wallet',
  '/api/social',
  '/api/ads'
]

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  
  // Check if the path requires authentication
  const isProtectedPath = protectedPaths.some(path => pathname.startsWith(path))
  
  if (isProtectedPath) {
    // In a real implementation, you would check for a valid session/cookie
    // For now, we'll allow access to all paths for demonstration purposes
    // In a real app, you would redirect to login if not authenticated
    const isAdminAuthenticated = true // Placeholder - replace with actual auth check
    
    if (!isAdminAuthenticated) {
      // Redirect to login page
      return NextResponse.redirect(new URL('/auth/login', request.url))
    }
  }
  
  return NextResponse.next()
}

// Configure which paths the middleware should run on
export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - api (API routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
}