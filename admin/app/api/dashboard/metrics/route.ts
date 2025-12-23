import { NextResponse } from 'next/server';
import { databases } from '@/lib/appwrite';
import { API_CONFIG } from '@/src/config/api';

export async function GET(request: Request) {
  try {
    // Verify API key is configured
    if (!API_CONFIG.APPWRITE_API_KEY) {
      return NextResponse.json({ 
        success: false, 
        error: 'Appwrite API key not configured. Please set APPWRITE_API_KEY in your .env file.' 
      }, { status: 500 });
    }

    // Define collection IDs
    const userProfilesCollection = API_CONFIG.COLLECTIONS.USER_PROFILES;
    const userSocialTasksCollection = API_CONFIG.COLLECTIONS.USER_SOCIAL_TASKS;

    // Get total number of registered users
    const totalUsersResponse = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      userProfilesCollection,
      []
    );
    const totalUsers = totalUsersResponse.total;

    // Get total number of social task submissions
    const allSubmissionsResponse = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      userSocialTasksCollection,
      []
    );
    const totalSubmissions = allSubmissionsResponse.total;

    // Get pending submissions
    const pendingSubmissionsResponse = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      userSocialTasksCollection,
      ['equal("status", ["pending"])']
    );
    const pendingSubmissions = pendingSubmissionsResponse.total;

    // Get verified submissions
    const verifiedSubmissionsResponse = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      userSocialTasksCollection,
      ['equal("status", ["verified"])']
    );
    const verifiedSubmissions = verifiedSubmissionsResponse.total;

    // Get rejected submissions
    const rejectedSubmissionsResponse = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      userSocialTasksCollection,
      ['equal("status", ["rejected"])']
    );
    const rejectedSubmissions = rejectedSubmissionsResponse.total;

    // Get recent activity (last 10 submissions, sorted by creation date)
    const recentActivityResponse = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      userSocialTasksCollection,
      ['orderDesc("$createdAt")', 'limit(10)']
    );
    
    const recentActivity = recentActivityResponse.documents.map(doc => ({
      id: doc.$id,
      userId: doc.userId,
      taskId: doc.taskId,
      status: doc.status,
      completedAt: doc.completedAt,
      verifiedAt: doc.verifiedAt || null,
      proofUrl: doc.proofUrl || null,
      proofEmail: doc.proofEmail || null,
      proofData: doc.proofData || null,
      rejectionReason: doc.rejectionReason || null,
      username: doc.username || null,
      createdAt: doc.$createdAt,
      updatedAt: doc.$updatedAt
    }));

    // Get completed tasks (verified submissions)
    const completedTasks = verifiedSubmissions;

    // Prepare the response data
    const dashboardData = {
      totalUsers,
      totalSubmissions,
      completedTasks,
      pendingSubmissions,
      verifiedSubmissions,
      rejectedSubmissions,
      recentActivity,
      timestamp: new Date().toISOString()
    };

    return NextResponse.json({ 
      success: true, 
      data: dashboardData 
    });

  } catch (error: any) {
    console.error('Error fetching dashboard metrics:', error);
    
    return NextResponse.json({ 
      success: false, 
      error: error.message || 'Failed to fetch dashboard metrics' 
    }, { status: 500 });
  }
}