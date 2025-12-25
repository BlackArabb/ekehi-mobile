import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

// List of paths that require authentication
const protectedPaths = [
  '/dashboard',
]

// Public paths that should always be accessible
const publicPaths = [
  '/auth/login',
  '/auth/register',
  '/',
]

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  
  // Allow public paths
  const isPublicPath = publicPaths.some(path => pathname === path || pathname.startsWith(path))
  if (isPublicPath) {
    return NextResponse.next()
  }
  
  // Allow all API routes (they handle their own auth)
  if (pathname.startsWith('/api/')) {
    return NextResponse.next()
  }
  
  // Check if the path requires authentication
  const isProtectedPath = protectedPaths.some(path => pathname.startsWith(path))
  
  if (isProtectedPath) {
    // Get all Appwrite session cookies
    // Appwrite creates cookies with pattern: a_session_{projectId}_{sessionId}
    const cookies = request.cookies.getAll()
    const hasAppwriteSession = cookies.some(cookie => 
      cookie.name.startsWith('a_session_')
    )
    
    // If no Appwrite session cookie exists, redirect to login
    if (!hasAppwriteSession) {
      console.log('No Appwrite session found, redirecting to login')
      const loginUrl = new URL('/auth/login', request.url)
      return NextResponse.redirect(loginUrl)
    }
    
    // If session cookie exists, allow access
    // Client-side will handle validation
    console.log('Appwrite session found, allowing access')
    return NextResponse.next()
  }
  
  // Allow all other paths
  return NextResponse.next()
}

// Configure which paths the middleware should run on
export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - images, icons, and other static assets
     */
    '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp|ico)$).*)',
  ],
}