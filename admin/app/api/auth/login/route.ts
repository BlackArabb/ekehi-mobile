import { NextResponse } from 'next/server';
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

    // Use Appwrite REST API directly for authentication
    const response = await fetch(
      `${API_CONFIG.APPWRITE_ENDPOINT}/account/sessions/email`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Appwrite-Project': API_CONFIG.APPWRITE_PROJECT_ID,
        },
        body: JSON.stringify({
          email: email,
          password: password,
        }),
      }
    );

    if (!response.ok) {
      const errorData = await response.json();
      console.error('Appwrite auth error:', errorData);
      
      let errorMessage = 'Login failed';
      
      if (response.status === 401 || errorData?.type === 'user_invalid_credentials') {
        errorMessage = 'Invalid email or password';
      } else if (errorData?.message) {
        errorMessage = errorData.message;
      }

      return NextResponse.json(
        { 
          success: false, 
          error: errorMessage,
          details: errorData?.message,
          type: errorData?.type,
          code: response.status
        },
        { status: 401 }
      );
    }

    const session = await response.json();
    
    // Return session info to client
    return NextResponse.json({
      success: true,
      data: {
        sessionId: session.secret,
        email: email,
        name: email.split('@')[0],
      }
    });
  } catch (error: any) {
    console.error('Login error:', error);
    console.error('Error message:', error?.message);
    
    let errorMessage = 'Login failed';
    
    if (error?.message) {
      errorMessage = error.message;
    }

    return NextResponse.json(
      { 
        success: false, 
        error: errorMessage, 
        details: error?.message
      },
      { status: 401 }
    );
  }
}
