import { NextResponse } from 'next/server';
import { databases, collections } from '@/lib/appwrite';
import { Query } from 'appwrite';
import { API_CONFIG } from '@/src/config/api';

// GET /api/ads - Fetch ad campaigns from Appwrite
export async function GET() {
  try {
    // Fetch ad campaigns from Appwrite (using adViews collection to get campaign data)
    const response = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      collections.adViews,
      [Query.orderDesc('$createdAt')]
    );
    
    // Group ad views by campaign to create mock campaigns
    const campaignMap: Record<string, any> = {};
    
    response.documents.forEach((adView: any) => {
      const campaignId = adView.campaignId || 'default';
      if (!campaignMap[campaignId]) {
        campaignMap[campaignId] = {
          id: campaignId,
          name: `Campaign ${campaignId}`,
          description: `Ad campaign ${campaignId}`,
          reward: adView.reward || 0.1,
          duration: 30,
          isActive: true,
          impressions: 0,
          clicks: 0,
          createdAt: adView.$createdAt
        };
      }
      campaignMap[campaignId].impressions += 1;
      if (adView.clicked) {
        campaignMap[campaignId].clicks += 1;
      }
    });
    
    const mockCampaigns = Object.values(campaignMap);
    
    // Calculate statistics
    const totalImpressions = mockCampaigns.reduce((sum: number, campaign: any) => sum + campaign.impressions, 0);
    const totalClicks = mockCampaigns.reduce((sum: number, campaign: any) => sum + campaign.clicks, 0);
    const avgCTR = totalImpressions > 0 ? (totalClicks / totalImpressions) * 100 : 0;
    
    const stats = {
      totalCampaigns: mockCampaigns.length,
      totalImpressions,
      totalClicks,
      avgCTR
    };
    
    return NextResponse.json({ 
      success: true, 
      data: {
        campaigns: mockCampaigns,
        stats
      }
    });
  } catch (error: any) {
    console.error('Error fetching ad data:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to fetch ad data' }, { status: 500 });
  }
}

// POST /api/ads - Create or update ad campaign in Appwrite
export async function POST(request: Request) {
  try {
    const campaignData = await request.json();
    
    // In a real implementation, you would save to a campaigns collection in Appwrite
    // For now, we'll just return success with the campaign data
    
    return NextResponse.json({ success: true, message: 'Campaign saved successfully', data: campaignData });
  } catch (error: any) {
    console.error('Error saving ad campaign:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to save campaign' }, { status: 500 });
  }
}

// DELETE /api/ads/[id] - Delete ad campaign from Appwrite
export async function DELETE(request: Request, { params }: { params: Promise<any> }) {
  try {
    const { id } = await params;
    
    if (!id) {
      return NextResponse.json({ success: false, error: 'Campaign ID is required' }, { status: 400 });
    }
    
    // In a real implementation, you would delete from a campaigns collection in Appwrite
    // For now, we'll just return success
    
    return NextResponse.json({ success: true, message: `Campaign ${id} deleted successfully` });
  } catch (error: any) {
    console.error('Error deleting ad campaign:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to delete campaign' }, { status: 500 });
  }
}