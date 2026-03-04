import { NextResponse } from 'next/server';
import { API_CONFIG } from '@/src/config/api';

export const dynamic = 'force-dynamic';

// GET /api/presale - Fetch presale purchases using REST API
export async function GET() {
  try {
    console.log('[Presale API] Fetching presale purchases...');

    const headers = {
      'Content-Type': 'application/json',
      'X-Appwrite-Key': API_CONFIG.APPWRITE_API_KEY || '',
      'X-Appwrite-Project': API_CONFIG.APPWRITE_PROJECT_ID || '',
    };

    const url = `${API_CONFIG.APPWRITE_ENDPOINT}/databases/${API_CONFIG.DATABASE_ID}/collections/${API_CONFIG.COLLECTIONS.PRESALE_PURCHASES}/documents`;

    const response = await fetch(url, { method: 'GET', headers });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Presale fetch failed: ${response.status} - ${errorText}`);
    }

    const data = await response.json() as any;
    const presales = data.documents || [];

    // Calculate statistics
    const completedPresales = presales.filter((p: any) => p.status === 'completed');
    
    const totalRaised = completedPresales.reduce((sum: number, p: any) => sum + (p.amountUsd || 0), 0);
    const tokensSold = completedPresales.reduce((sum: number, p: any) => sum + (p.tokensAmount || 0), 0);
    
    const participants = new Set(
      completedPresales.map((p: any) => p.userId)
    ).size;
    
    const stats = {
      totalRaised,
      tokensSold,
      participants
    };

    console.log('[Presale API] Success:', { count: presales.length });

    return NextResponse.json({ 
      success: true, 
      data: {
        presales,
        stats
      }
    });

  } catch (error: any) {
    console.error('[Presale API] Error:', error);
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to fetch presales' },
      { status: 500 }
    );
  }
}
