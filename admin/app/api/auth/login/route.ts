import { NextResponse } from 'next/server';
import { Client, Account } from 'node-appwrite';
import { API_CONFIG } from '@/src/config/api';

export async function POST(request: Request) {
  try {
    const { email, password } = await request.json();

    if (!email || !password) {
      return NextResponse.json(
        { success: false, error: 'Email and password are required' },
        { status: 400 }
      );
    }

    // Create client without API key - let user authenticate directly
    const client = new Client()
      .setEndpoint(API_CONFIG.APPWRITE_ENDPOINT)
      .setProject(API_CONFIG.APPWRITE_PROJECT_ID);

    const account = new Account(client);

    // Create session - Appwrite will handle user role validation
    const session = await account.createEmailPasswordSession(email, password);

    // Get current user (without using account scopes that cause role issues)
    // We'll return basic session info instead
    return NextResponse.json({
      success: true,
      data: {
        sessionId: session.$id,
        email: email,
        name: email.split('@')[0],
      }
    });
  } catch (error: any) {
    console.error('Login error:', error);
    console.error('Error message:', error?.message);
    console.error('Error code:', error?.code);
    console.error('Error type:', error?.type);
    console.error('Full error:', JSON.stringify(error));
    
    let errorMessage = 'Login failed';
    
    // Check for various error patterns
    if (error?.type === 'user_invalid_credentials' || 
        error?.type === 'USER_INVALID_CREDENTIALS' ||
        error?.message?.toLowerCase().includes('invalid credentials') ||
        error?.message?.toLowerCase().includes('wrong password') ||
        error?.code === 401) {
      errorMessage = 'Invalid email or password';
    } else if (error?.type === 'user_not_found' || 
               error?.type === 'USER_NOT_FOUND' ||
               error?.message?.toLowerCase().includes('user not found')) {
      errorMessage = 'User not found';
    } else if (error?.message) {
      errorMessage = error.message;
    }

    return NextResponse.json(
      { 
        success: false, 
        error: errorMessage, 
        details: error?.message,
        type: error?.type,
        code: error?.code
      },
      { status: 401 }
    );
  }
}
