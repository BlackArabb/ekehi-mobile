import { NextResponse } from 'next/server';
import { databases, collections } from '@/lib/appwrite';
import { API_CONFIG } from '@/src/config/api';

// DELETE /api/presale/[id] - Delete presale purchase from Appwrite
export async function DELETE(
  request: Request, 
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    
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