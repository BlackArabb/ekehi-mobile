import { NextResponse } from 'next/server';
import { databases, collections } from '@/lib/appwrite';
import { Query } from 'appwrite';
import { API_CONFIG } from '@/src/config/api';

export const dynamic = 'force-dynamic';



// PATCH /api/social/submissions/[id] - Update submission status
export async function PATCH(request: Request, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    
    if (!id) {
      return NextResponse.json({ success: false, error: 'Submission ID is required' }, { status: 400 });
    }
    
    const body = await request.json();
    const { status, rejectionReason } = body;
    
    if (!status) {
      return NextResponse.json({ success: false, error: 'Status is required' }, { status: 400 });
    }
    
    // Prepare update data
    const updateData: any = {
      status
    };
    
    if (status === 'verified') {
      updateData.verifiedAt = new Date().toISOString();
    } else if (status === 'rejected' && rejectionReason) {
      updateData.rejectionReason = rejectionReason;
    }
    
    // Update the submission
    const result = await databases.updateDocument(
      API_CONFIG.DATABASE_ID,
      collections.userSocialTasks,
      id,
      updateData
    );
    
    // If verified, we should also award coins to the user
    if (status === 'verified') {
      try {
        // Get the submission to get task details
        const submission = await databases.getDocument(
          API_CONFIG.DATABASE_ID,
          collections.userSocialTasks,
          id
        ) as any;
        
        // Get the task to get reward amount
        const task = await databases.getDocument(
          API_CONFIG.DATABASE_ID,
          collections.socialTasks,
          submission.taskId
        ) as any;
        
        // Award coins to user
        if (task.rewardCoins > 0) {
          // We would typically call a function to update user's coin balance here
          console.log(`Awarding ${task.rewardCoins} coins to user ${submission.userId}`);
        }
      } catch (coinError) {
        console.error('Error awarding coins:', coinError);
        // Don't fail the whole operation if coin awarding fails
      }
    }
    
    // Transform result to match data model
    const transformedResult = {
      id: result.$id,
      userId: (result as any).userId,
      taskId: (result as any).taskId,
      status: (result as any).status,
      completedAt: (result as any).completedAt || result.$createdAt,
      verifiedAt: (result as any).verifiedAt || null,
      proofUrl: (result as any).proofUrl || null,
      proofEmail: (result as any).proofEmail || null,
      proofData: (result as any).proofData || null,
      verificationAttempts: (result as any).verificationAttempts || 0,
      rejectionReason: (result as any).rejectionReason || null,
      username: (result as any).username || null,
      createdAt: result.$createdAt,
      updatedAt: result.$updatedAt
    };
    
    return NextResponse.json({ 
      success: true, 
      message: `Submission ${status} successfully`,
      data: transformedResult
    });
  } catch (error: any) {
    console.error('Error updating social task submission:', error);
    
    // If API key is not configured, return an error message
    if (!API_CONFIG.APPWRITE_API_KEY) {
      return NextResponse.json({ 
        success: false, 
        error: 'Appwrite API key not configured. Please set APPWRITE_API_KEY in your .env file.' 
      }, { status: 500 });
    }
    
    return NextResponse.json({ success: false, error: error.message || 'Failed to update social task submission' }, { status: 500 });
  }
}
