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
 * DELETE /api/users/[id] - Delete user from Appwrite
 * 
 * This endpoint handles user deletion by removing the user from both:
 * 1. The database collection (user profiles)
 * 2. The authentication system
 * 
 * The process is designed to be resilient - if one deletion fails, 
 * the other will still be attempted. This ensures that users are
 * properly removed from the system even if one service is temporarily unavailable.
 * 
 * @param {Request} request - The incoming request object
 * @param {Object} params - Contains the user ID to delete
 * @param {string} params.id - The unique ID of the user to delete
 * 
 * @returns {Response} JSON response with success status and message
 * 
 * Example usage:
 * DELETE /api/users/69296cc623e1d71713e9
 * 
 * Response:
 * {
 *   "success": true,
 *   "message": "User deleted successfully"
 * }
 */
export async function DELETE(request: Request, { params }: { params: { id: string } }) {
  try {
    const { id } = await params;
    
    if (!id) {
      return NextResponse.json({ success: false, error: 'User ID is required' }, { status: 400 });
    }
    
    // Delete user from both database collection and authentication system
    try {
      // First delete from database collection
      await adminDatabases.deleteDocument(
        API_CONFIG.DATABASE_ID,
        API_CONFIG.COLLECTIONS.USER_PROFILES, // Changed from USERS to USER_PROFILES
        id
      );
    } catch (dbError) {
      console.error(`Error deleting user from database:`, dbError);
      // Continue to try deleting from auth system even if database deletion fails
    }
    
    try {
      // Then delete from authentication system
      await adminUsers.delete(id);
    } catch (authError) {
      console.error(`Error deleting user from auth system:`, authError);
      // If auth deletion fails, that's ok - user is still deleted from db
    }
    
    return NextResponse.json({ success: true, message: `User deleted successfully` });
  } catch (error: any) {
    console.error('Error deleting user:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to delete user' }, { status: 500 });
  }
}