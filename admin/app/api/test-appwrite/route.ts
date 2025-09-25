import { NextResponse } from 'next/server';
import { Client, Databases, Query } from 'node-appwrite';
import { API_CONFIG } from '@/src/config/api';

// Test Appwrite connection and fetch users
export async function GET() {
  try {
    console.log('Testing Appwrite connection...');
    console.log('Endpoint:', API_CONFIG.APPWRITE_ENDPOINT);
    console.log('Project ID:', API_CONFIG.APPWRITE_PROJECT_ID);
    console.log('Database ID:', API_CONFIG.DATABASE_ID);
    console.log('Users Collection ID:', API_CONFIG.COLLECTIONS.USERS);
    console.log('API Key provided:', !!API_CONFIG.APPWRITE_API_KEY);
    
    // Create a new client instance for this test with admin permissions
    const adminClient = new Client();
    adminClient
      .setEndpoint(API_CONFIG.APPWRITE_ENDPOINT)
      .setProject(API_CONFIG.APPWRITE_PROJECT_ID)
      .setKey(API_CONFIG.APPWRITE_API_KEY || '');
    
    const adminDatabases = new Databases(adminClient);
    
    // Try to list documents from the users collection
    const response = await adminDatabases.listDocuments(
      API_CONFIG.DATABASE_ID,
      API_CONFIG.COLLECTIONS.USERS,
      [Query.limit(10)]
    );
    
    console.log('Successfully fetched users:', response.total);
    
    return NextResponse.json({ 
      success: true, 
      message: 'Successfully connected to Appwrite',
      totalUsers: response.total,
      users: response.documents.slice(0, 5) // Only return first 5 for testing
    });
  } catch (error: any) {
    console.error('Error connecting to Appwrite:', error);
    return NextResponse.json({ 
      success: false, 
      error: error.message || 'Failed to connect to Appwrite',
      stack: error.stack
    }, { status: 500 });
  }
}