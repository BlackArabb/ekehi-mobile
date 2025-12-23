import { NextResponse } from 'next/server';
import { Client, Databases, Query } from 'node-appwrite';
import { API_CONFIG } from '@/src/config/api';

// Create a new client with admin permissions for server-side operations
const adminClient = new Client();
adminClient
  .setEndpoint(API_CONFIG.APPWRITE_ENDPOINT)
  .setProject(API_CONFIG.APPWRITE_PROJECT_ID)
  .setKey(API_CONFIG.APPWRITE_API_KEY || '');

const adminDatabases = new Databases(adminClient);

// GET /api/users - Fetch all users from Appwrite
export async function GET(request: Request) {
  try {
    console.log('Fetching users from Appwrite...');
    console.log('Endpoint:', API_CONFIG.APPWRITE_ENDPOINT);
    console.log('Project ID:', API_CONFIG.APPWRITE_PROJECT_ID);
    console.log('Database ID:', API_CONFIG.DATABASE_ID);
    console.log('Users Collection ID:', API_CONFIG.COLLECTIONS.USERS);
    console.log('API Key provided:', !!API_CONFIG.APPWRITE_API_KEY);
    
    const { searchParams } = new URL(request.url);
    const page = parseInt(searchParams.get('page') || '1');
    const limit = parseInt(searchParams.get('limit') || '10');
    const search = searchParams.get('search') || '';
    
    // Build queries
    const queries = [
      Query.limit(limit),
      Query.offset((page - 1) * limit),
      Query.orderDesc('$createdAt')
    ];
    
    if (search) {
      queries.push(Query.search('name', search));
    }
    
    // Fetch users from Appwrite
    const response = await adminDatabases.listDocuments(
      API_CONFIG.DATABASE_ID,
      API_CONFIG.COLLECTIONS.USERS,
      queries
    );
    
    console.log('Successfully fetched users:', response.total);
    
    // Fetch additional statistics
    const allUsers = await adminDatabases.listDocuments(
      API_CONFIG.DATABASE_ID,
      API_CONFIG.COLLECTIONS.USERS,
      [Query.limit(1000)] // Get all for stats calculation
    );
    
    const totalUsers = allUsers.total;
    const activeUsers = allUsers.documents.filter((user: any) => user.status === 'active').length;
    const adminUsers = allUsers.documents.filter((user: any) => user.role === 'admin').length;
    
    const stats = {
      totalUsers,
      activeUsers,
      inactiveUsers: totalUsers - activeUsers,
      adminUsers
    };
    
    return NextResponse.json({ 
      success: true, 
      data: {
        users: response.documents,
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

// POST /api/users - Create or update user in Appwrite
export async function POST(request: Request) {
  try {
    const userData = await request.json();
    
    // Validate required fields
    if (!userData.name || !userData.email) {
      return NextResponse.json({ success: false, error: 'Name and email are required' }, { status: 400 });
    }
    
    let result;
    
    if (userData.$id) {
      // Update existing user
      result = await adminDatabases.updateDocument(
        API_CONFIG.DATABASE_ID,
        API_CONFIG.COLLECTIONS.USERS,
        userData.$id,
        {
          name: userData.name,
          email: userData.email,
          status: userData.status || 'active',
          role: userData.role || 'user'
        }
      );
    } else {
      // Create new user
      result = await adminDatabases.createDocument(
        API_CONFIG.DATABASE_ID,
        API_CONFIG.COLLECTIONS.USERS,
        'unique()',
        {
          name: userData.name,
          email: userData.email,
          status: userData.status || 'active',
          role: userData.role || 'user'
        }
      );
    }
    
    return NextResponse.json({ 
      success: true, 
      message: userData.$id ? 'User updated successfully' : 'User created successfully',
      data: result
    });
  } catch (error: any) {
    console.error('Error saving user:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to save user' }, { status: 500 });
  }
}

// DELETE /api/users/[id] - Delete user from Appwrite
export async function DELETE(request: Request, { params }: { params: Promise<any> }) {
  try {
    const { id } = await params;
    
    if (!id) {
      return NextResponse.json({ success: false, error: 'User ID is required' }, { status: 400 });
    }
    
    // Delete user from Appwrite
    await adminDatabases.deleteDocument(
      API_CONFIG.DATABASE_ID,
      API_CONFIG.COLLECTIONS.USERS,
      id
    );
    
    return NextResponse.json({ success: true, message: `User deleted successfully` });
  } catch (error: any) {
    console.error('Error deleting user:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to delete user' }, { status: 500 });
  }
}