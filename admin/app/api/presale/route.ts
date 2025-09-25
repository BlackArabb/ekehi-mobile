import { NextResponse } from 'next/server';
import { databases, collections } from '@/lib/appwrite';
import { Query } from 'appwrite';
import { API_CONFIG } from '@/src/config/api';

// GET /api/presale - Fetch presale purchases from Appwrite
export async function GET() {
  try {
    // Fetch presale purchases from Appwrite
    const response = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      collections.presalePurchases,
      [Query.orderDesc('$createdAt')]
    );
    
    // Calculate statistics
    const completedPresales = response.documents.filter((p: any) => p.status === 'completed');
    
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
    
    return NextResponse.json({ 
      success: true, 
      data: {
        presales: response.documents,
        stats
      }
    });
  } catch (error: any) {
    console.error('Error fetching presale data:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to fetch presale data' }, { status: 500 });
  }
}

// POST /api/presale - Create or update presale purchase in Appwrite
export async function POST(request: Request) {
  try {
    const presaleData = await request.json();
    
    let result;
    
    if (presaleData.$id) {
      // Update existing presale purchase
      result = await databases.updateDocument(
        API_CONFIG.DATABASE_ID,
        collections.presalePurchases,
        presaleData.$id,
        presaleData
      );
    } else {
      // Create new presale purchase
      result = await databases.createDocument(
        API_CONFIG.DATABASE_ID,
        collections.presalePurchases,
        'unique()',
        presaleData
      );
    }
    
    return NextResponse.json({ success: true, message: 'Presale purchase saved successfully', data: result });
  } catch (error: any) {
    console.error('Error saving presale purchase:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to save presale purchase' }, { status: 500 });
  }
}

// POST /api/presale/settings - Update presale settings (placeholder - would need a settings collection)
export async function POST_SETTINGS(request: Request) {
  try {
    const settings = await request.json();
    
    // In a real implementation, you would save to Appwrite
    // For now, we'll just return success
    
    return NextResponse.json({ success: true, message: 'Settings updated successfully' });
  } catch (error: any) {
    console.error('Error updating presale settings:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to update settings' }, { status: 500 });
  }
}

// DELETE /api/presale/[id] - Delete presale purchase from Appwrite
export async function DELETE(request: Request, { params }: { params: { id: string } }) {
  try {
    const { id } = params;
    
    if (!id) {
      return NextResponse.json({ success: false, error: 'Presale purchase ID is required' }, { status: 400 });
    }
    
    // Delete presale purchase from Appwrite
    await databases.deleteDocument(
      API_CONFIG.DATABASE_ID,
      collections.presalePurchases,
      id
    );
    
    return NextResponse.json({ success: true, message: `Presale purchase ${id} deleted successfully` });
  } catch (error: any) {
    console.error('Error deleting presale purchase:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to delete presale purchase' }, { status: 500 });
  }
}