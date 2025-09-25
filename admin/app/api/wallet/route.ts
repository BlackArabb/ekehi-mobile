import { NextResponse } from 'next/server';
import { databases, collections } from '@/lib/appwrite';
import { Query } from 'appwrite';
import { API_CONFIG } from '@/src/config/api';

// GET /api/wallet - Fetch wallet transactions from Appwrite
export async function GET() {
  try {
    // Fetch wallet transactions from Appwrite (assuming there's a wallet_transactions collection)
    // For now, we'll fetch mining sessions as they represent wallet activity
    const response = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      collections.miningSessions,
      [Query.orderDesc('$createdAt')]
    );
    
    // Calculate statistics from mining sessions
    const totalMiningSessions = response.total;
    const totalMiningRewards = response.documents.reduce((sum: number, session: any) => sum + (session.reward || 0), 0);
    
    // In a real implementation, you would have a dedicated wallet_transactions collection
    const mockTransactions = response.documents.map((session: any, index: number) => ({
      id: session.$id,
      userId: session.userId,
      userName: `User ${index + 1}`,
      type: 'mining_reward',
      amount: session.reward || 0,
      status: 'completed',
      timestamp: session.$createdAt,
      walletAddress: session.userId // Using userId as wallet address for demo
    }));
    
    const stats = {
      totalBalance: totalMiningRewards * 2, // Mock total balance
      totalDeposits: totalMiningRewards * 0.5, // Mock deposits
      totalWithdrawals: totalMiningRewards * 0.1, // Mock withdrawals
      rewardsDistributed: totalMiningRewards
    };
    
    return NextResponse.json({ 
      success: true, 
      data: {
        transactions: mockTransactions,
        stats
      }
    });
  } catch (error: any) {
    console.error('Error fetching wallet data:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to fetch wallet data' }, { status: 500 });
  }
}

// POST /api/wallet/transaction - Create or update wallet transaction in Appwrite
export async function POST(request: Request) {
  try {
    const transaction = await request.json();
    
    // In a real implementation, you would save to a wallet_transactions collection in Appwrite
    // For now, we'll just return success with the transaction data
    
    return NextResponse.json({ success: true, message: 'Transaction processed successfully', data: transaction });
  } catch (error: any) {
    console.error('Error processing transaction:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to process transaction' }, { status: 500 });
  }
}