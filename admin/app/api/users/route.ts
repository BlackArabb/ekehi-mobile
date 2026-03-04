import { NextResponse } from 'next/server';
import { API_CONFIG } from '@/src/config/api';

export const dynamic = 'force-dynamic';

// GET /api/users - Fetch users from Appwrite using REST API
export async function GET(request: Request) {
  try {
    const { searchParams } = new URL(request.url);
    const page = parseInt(searchParams.get('page') || '1');
    const limit = parseInt(searchParams.get('limit') || '10');
    const offset = (page - 1) * limit;

    console.log('[Users API] Fetching users from Appwrite...');

    // Use REST API directly to avoid node-appwrite SDK bug
    const headers = {
      'Content-Type': 'application/json',
      'X-Appwrite-Key': API_CONFIG.APPWRITE_API_KEY || '',
    };

    // Build URL with query params
    const baseUrl = `${API_CONFIG.APPWRITE_ENDPOINT}/databases/${API_CONFIG.DATABASE_ID}/collections/${API_CONFIG.COLLECTIONS.USER_PROFILES}/documents`;
    const params = new URLSearchParams({
      limit: limit.toString(),
      offset: offset.toString(),
    });
    const url = `${baseUrl}?${params.toString()}`;

    const response = await fetch(url, { method: 'GET', headers });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Users fetch failed: ${response.status} - ${errorText}`);
    }

    const data = await response.json() as any;
    const users = data.documents || [];
    const total = data.total || 0;

    // Format users data
    const formattedUsers = users.map((user: any) => ({
      $id: user.$id,
      userId: user.userId,
      username: user.username,
      email: user.email || `${user.username}@placeholder.com`,
      totalCoins: user.totalCoins || 0,
      walletBalance: user.walletBalance || 0,
      isActive: user.isActive ?? true,
      createdAt: user.$createdAt,
      updatedAt: user.$updatedAt,
    }));

    // Calculate stats
    const stats = {
      totalUsers: total,
      activeUsers: users.filter((u: any) => u.isActive !== false).length,
      inactiveUsers: users.filter((u: any) => u.isActive === false).length,
    };

    console.log('[Users API] Success:', { total, page, limit });

    return NextResponse.json({
      success: true,
      data: {
        users: formattedUsers,
        stats,
        total,
        page,
        limit,
      },
    });

  } catch (error: any) {
    console.error('[Users API] Error:', error);
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to fetch users' },
      { status: 500 }
    );
  }
}
