import { NextResponse } from 'next/server';
import { Client, Databases, Query, Users } from 'node-appwrite';
import { API_CONFIG } from '@/src/config/api';

// Create a new client with admin permissions for server-side operations
const adminClient = new Client();
adminClient
  .setEndpoint(API_CONFIG.APPWRITE_ENDPOINT)
  .setProject(API_CONFIG.APPWRITE_PROJECT_ID)
  .setKey(API_CONFIG.APPWRITE_API_KEY || '');

const adminDatabases = new Databases(adminClient);
const adminUsers = new Users(adminClient);

/**
 * GET /api/users - Fetch all users from Appwrite
 * 
 * This endpoint retrieves user data from the Appwrite database collection.
 * It supports pagination, search, and sorting functionality.
 * 
 * Features:
 * - Pagination with configurable page size
 * - Full-text search across name and email fields
 * - Sorting by various fields (name, email, wallet balance, etc.)
 * - Statistics calculation for user management dashboard
 * - Email retrieval from profile data with fallback message
 * 
 * Query parameters:
 * - page: Page number (default: 1)
 * - limit: Items per page (default: 10)
 * - search: Search term for name/email
 * - sortBy: Field to sort by
 * - sortOrder: Sort order (asc/desc, default: desc)
 * 
 * @param {Request} request - The incoming request object
 * 
 * @returns {Response} JSON response with users data and statistics
 * 
 * Example usage:
 * GET /api/users?page=1&limit=10&search=john&sortBy=name&sortOrder=asc
 * 
 * Response:
 * {
 *   "success": true,
 *   "data": {
 *     "users": [...],
 *     "stats": {...},
 *     "total": 150,
 *     "page": 1,
 *     "limit": 10
 *   }
 * }
 */
export async function GET(request: Request) {
  try {
    console.log('Fetching users from Appwrite...');
    console.log('Endpoint:', API_CONFIG.APPWRITE_ENDPOINT);
    console.log('Project ID:', API_CONFIG.APPWRITE_PROJECT_ID);
    console.log('Database ID:', API_CONFIG.DATABASE_ID);
    console.log('Users Collection ID:', API_CONFIG.COLLECTIONS.USER_PROFILES);
    console.log('API Key provided:', !!API_CONFIG.APPWRITE_API_KEY);
    
    const { searchParams } = new URL(request.url);
    const page = parseInt(searchParams.get('page') || '1');
    const limit = parseInt(searchParams.get('limit') || '10');
    const search = searchParams.get('search') || '';
    const sortBy = searchParams.get('sortBy') || '';
    const sortOrder = searchParams.get('sortOrder') || 'desc';
    
    // Build queries
    const queries = [
      Query.limit(limit),
      Query.offset((page - 1) * limit)
    ];
    
    // Add sorting query
    // Map frontend field names to actual database field names if needed
    let actualSortField = sortBy;
    switch(sortBy) {
      case 'name':
        actualSortField = 'name'; // Assuming this is the same
        break;
      case 'email':
        actualSortField = 'email'; // Assuming this is the same
        break;
      case 'walletBalance':
        actualSortField = 'totalCoins'; // Map to actual field name
        break;
      case 'lastLogin':
        actualSortField = 'lastLoginAt'; // Map to actual field name
        break;
      case 'createdAt':
        actualSortField = '$createdAt'; // Appwrite system field
        break;
      default:
        actualSortField = sortBy;
    }
    
    if (sortBy) {
      if (sortOrder === 'asc') {
        queries.push(Query.orderAsc(actualSortField));
      } else {
        queries.push(Query.orderDesc(actualSortField));
      }
    } else {
      // Default sorting by creation date if no sort specified
      queries.push(Query.orderDesc('$createdAt'));
    }
    
    if (search) {
      // Appwrite doesn't support search on non-indexed attributes, so we'll use a workaround
      // For now, just get all users and filter on the client side if needed
      // Or use contains query if the field is indexed
      // We'll use a combination of contains queries for name and email
      // Using actual database field names
      const nameQuery = Query.contains('name', search);
      const emailQuery = Query.contains('email', search);
      queries.push(Query.or([nameQuery, emailQuery]));
    }
    
    // Fetch users from Appwrite using the USER_PROFILES collection
    const response = await adminDatabases.listDocuments(
      API_CONFIG.DATABASE_ID,
      API_CONFIG.COLLECTIONS.USER_PROFILES, // Changed from USERS to USER_PROFILES
      queries
    );
    
    console.log('Successfully fetched users:', response.total);
    
    // Fetch additional statistics
    // For accurate stats, we need to get all users, but this is expensive
    // We'll optimize by getting stats separately
    const allUsers = await adminDatabases.listDocuments(
      API_CONFIG.DATABASE_ID,
      API_CONFIG.COLLECTIONS.USER_PROFILES, // Changed from USERS to USER_PROFILES
      [Query.limit(1000)]
    );
    
    const totalUsers = allUsers.total;
    const activeUsers = allUsers.documents.filter((user: any) => 
      user.status === 'active' || user.status === undefined // Default to active if not specified
    ).length;
    const inactiveUsers = totalUsers - activeUsers;
    const adminUsers = allUsers.documents.filter((user: any) => 
      user.role === 'admin' || user.is_admin === true
    ).length;
    
    // Calculate user registration trends over time
    const registrationTrends = allUsers.documents.reduce((acc: Record<string, number>, user: any) => {
      // Group by month-year
      const date = new Date(user.$createdAt);
      const monthYear = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      acc[monthYear] = (acc[monthYear] || 0) + 1;
      return acc;
    }, {});
    
    // Calculate user activity statistics
    const userActivityStats = {
      registrationTrends: Object.entries(registrationTrends).map(([date, count]) => ({
        date,
        count
      })).sort((a, b) => a.date.localeCompare(b.date))
    };

    const stats = {
      totalUsers,
      activeUsers,
      inactiveUsers,
      adminUsers,
      userActivityStats
    };
    
    // Transform user data to match the expected format for the frontend
    const transformedUsers = response.documents.map((user: any) => {
      // First try to get email from profile data
      let userEmail = user.email || user.userEmail || '';
      
      // If no email in profile, show a message
      if (!userEmail) {
        userEmail = 'Email not available';
      }
      
      return {
        id: user.$id,
        name: user.name || user.username || 'Unknown User',
        email: userEmail,
        status: user.status || 'active',
        role: user.role || user.is_admin === true ? 'admin' : 'user',
        createdAt: user.$createdAt,
        lastLogin: user.lastLoginAt || user.$updatedAt || user.$createdAt,
        walletBalance: user.totalCoins || user.walletBalance || user.balance || 0
      };
    });
    
    return NextResponse.json({ 
      success: true, 
      data: {
        users: transformedUsers,
        stats,
        total: response.total,
        page,
        limit
      }
    });
  } catch (error: any) {
    console.error('Error fetching users:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to fetch users' }, { status: 500 });
  }
}

/**
 * POST /api/users - Create or update user in Appwrite
 * 
 * This endpoint handles both creating new users and updating existing users.
 * It stores user data in the Appwrite database collection with proper email
 * duplication in both 'email' and 'userEmail' fields for compatibility.
 * 
 * Features:
 * - Creates new users with unique IDs
 * - Updates existing users when ID is provided
 * - Validates required fields (name and email)
 * - Stores email in both 'email' and 'userEmail' fields
 * - Maps frontend fields to appropriate database fields
 * 
 * Request body format:
 * {
 *   "id": "user-id", // Optional - if provided, updates existing user
 *   "name": "User Name",
 *   "email": "user@example.com",
 *   "status": "active|inactive", // default: active
 *   "role": "admin|user", // default: user
 *   "walletBalance": 0 // default: 0
 * }
 * 
 * @param {Request} request - The incoming request object with user data in body
 * 
 * @returns {Response} JSON response with success status and user data
 * 
 * Example usage:
 * POST /api/users with body: {"name": "John Doe", "email": "john@example.com"}
 * 
 * Response:
 * {
 *   "success": true,
 *   "message": "User created successfully",
 *   "data": {"id": "new-user-id", "name": "John Doe", ...}
 * }
 */
export async function POST(request: Request) {
  try {
    const userData = await request.json();
    
    // Validate required fields
    if (!userData.name || !userData.email) {
      return NextResponse.json({ success: false, error: 'Name and email are required' }, { status: 400 });
    }
    
    let result;
    
    if (userData.id) {
      // Update existing user
      result = await adminDatabases.updateDocument(
        API_CONFIG.DATABASE_ID,
        API_CONFIG.COLLECTIONS.USER_PROFILES, // Changed from USERS to USER_PROFILES
        userData.id,
        {
          name: userData.name,
          email: userData.email,
          userEmail: userData.email, // Store email in userEmail field as well
          status: userData.status || 'active',
          role: userData.role || 'user',
          totalCoins: userData.walletBalance || userData.totalCoins || 0
        }
      );
    } else {
      // Create new user
      result = await adminDatabases.createDocument(
        API_CONFIG.DATABASE_ID,
        API_CONFIG.COLLECTIONS.USER_PROFILES, // Changed from USERS to USER_PROFILES
        'unique()',
        {
          name: userData.name,
          email: userData.email,
          userEmail: userData.email, // Store email in userEmail field as well
          status: userData.status || 'active',
          role: userData.role || 'user',
          totalCoins: userData.walletBalance || userData.totalCoins || 0
        }
      );
    }
    
    // Transform result to match expected format
    const transformedResult = {
      id: result.$id,
      name: result.name || result.username || 'Unknown User',
      email: result.email || result.userEmail || '',
      status: result.status || 'active',
      role: result.role || result.is_admin === true ? 'admin' : 'user',
      createdAt: result.$createdAt,
      lastLogin: result.lastLoginAt || result.$updatedAt || result.$createdAt,
      walletBalance: result.totalCoins || result.walletBalance || result.balance || 0
    };
    
    return NextResponse.json({ 
      success: true, 
      message: userData.id ? 'User updated successfully' : 'User created successfully',
      data: transformedResult
    });
  } catch (error: any) {
    console.error('Error saving user:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to save user' }, { status: 500 });
  }
}

