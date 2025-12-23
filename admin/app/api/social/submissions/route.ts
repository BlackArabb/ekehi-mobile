import { NextResponse } from 'next/server';
import { databases, collections } from '@/lib/appwrite';
import { Query } from 'appwrite';
import { API_CONFIG } from '@/src/config/api';

// GET /api/social/submissions - Fetch social task submissions
export async function GET(request: Request) {
  try {
    // Get query parameters
    const { searchParams } = new URL(request.url);
    const status = searchParams.get('status') || null;
    const taskId = searchParams.get('taskId') || null;
    const userId = searchParams.get('userId') || null;
    
    // Build queries
    const queries = [];
    
    if (status) {
      queries.push(Query.equal('status', status));
    }
    
    if (taskId) {
      queries.push(Query.equal('taskId', taskId));
    }
    
    if (userId) {
      queries.push(Query.equal('userId', userId));
    }
    
    // Add ordering
    queries.push(Query.orderDesc('$createdAt'));
    
    // Fetch user social tasks from Appwrite
    const response = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      collections.userSocialTasks,
      queries
    );
    
    // Transform submissions to match admin panel data model
    const transformedSubmissions = response.documents.map((submission: any) => ({
      id: submission.$id,
      userId: submission.userId,
      taskId: submission.taskId,
      status: submission.status,
      completedAt: submission.completedAt || submission.$createdAt,
      verifiedAt: submission.verifiedAt || null,
      proofUrl: submission.proofUrl || null,
      proofEmail: submission.proofEmail || null,
      proofData: submission.proofData || null,
      verificationAttempts: submission.verificationAttempts || 0,
      rejectionReason: submission.rejectionReason || null,
      username: submission.username || null,
      createdAt: submission.$createdAt,
      updatedAt: submission.$updatedAt
    }));
    
    // Fetch related task details for each submission
    const taskIds = Array.from(new Set(transformedSubmissions.map((s: any) => s.taskId)));
    const tasksMap: Record<string, any> = {};
    
    if (taskIds.length > 0) {
      // Fetch tasks in batches to avoid query limits
      const batchSize = 100;
      for (let i = 0; i < taskIds.length; i += batchSize) {
        const batch = taskIds.slice(i, i + batchSize);
        const taskQueries = [Query.equal('$id', batch)];
        
        const tasksResponse = await databases.listDocuments(
          API_CONFIG.DATABASE_ID,
          collections.socialTasks,
          taskQueries
        );
        
        tasksResponse.documents.forEach((task: any) => {
          tasksMap[task.$id] = {
            id: task.$id,
            title: task.title,
            description: task.description,
            platform: task.platform,
            rewardCoins: task.rewardCoins,
            taskType: task.taskType || 'generic'
          };
        });
      }
    }
    
    // Fetch user details for each submission
    const userIds = Array.from(new Set(transformedSubmissions.map((s: any) => s.userId)));
    const usersMap: Record<string, any> = {};
    
    if (userIds.length > 0) {
      // Fetch users in batches
      const batchSize = 100;
      for (let i = 0; i < userIds.length; i += batchSize) {
        const batch = userIds.slice(i, i + batchSize);
        const userQueries = [Query.equal('$id', batch)];
        
        const usersResponse = await databases.listDocuments(
          API_CONFIG.DATABASE_ID,
          collections.users,
          userQueries
        );
        
        usersResponse.documents.forEach((user: any) => {
          usersMap[user.$id] = {
            id: user.$id,
            name: user.name || 'Unknown User',
            email: user.email || 'No email'
          };
        });
      }
    }
    
    // Combine submissions with task and user details
    const enrichedSubmissions = transformedSubmissions.map((submission: any) => ({
      ...submission,
      task: tasksMap[submission.taskId] || null,
      user: usersMap[submission.userId] || null
    }));
    
    return NextResponse.json({ 
      success: true, 
      data: {
        submissions: enrichedSubmissions,
        total: response.total
      }
    });
  } catch (error: any) {
    console.error('Error fetching social task submissions:', error);
    
    // If API key is not configured, return an error message
    if (!API_CONFIG.APPWRITE_API_KEY) {
      return NextResponse.json({ 
        success: false, 
        error: 'Appwrite API key not configured. Please set APPWRITE_API_KEY in your .env file.' 
      }, { status: 500 });
    }
    
    return NextResponse.json({ success: false, error: error.message || 'Failed to fetch social task submissions' }, { status: 500 });
  }
}