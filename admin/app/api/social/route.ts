import { NextResponse } from 'next/server';
import { databases, collections } from '@/lib/appwrite';
import { Query } from 'appwrite';
import { API_CONFIG } from '@/src/config/api';

// GET /api/social - Fetch social tasks from Appwrite
export async function GET() {
  try {
    // Fetch social tasks from Appwrite
    const response = await databases.listDocuments(
      API_CONFIG.DATABASE_ID,
      collections.socialTasks,
      [Query.orderAsc('sortOrder')]
    );
    
    // Calculate statistics
    const activeTasks = response.documents.filter((task: any) => task.isActive).length;
    const totalTasks = response.total;
    
    const stats = {
      totalTasks,
      activeTasks,
      inactiveTasks: totalTasks - activeTasks
    };
    
    // Transform tasks to match KtMobileApp data model
    const transformedTasks = response.documents.map((task: any) => ({
      id: task.$id,
      title: task.title,
      description: task.description,
      platform: task.platform,
      taskType: task.taskType || 'generic',
      rewardCoins: task.rewardCoins || 0,
      actionUrl: task.actionUrl || null,
      verificationMethod: task.verificationMethod || 'manual',
      verificationData: task.verificationData || null,
      isActive: task.isActive || false,
      sortOrder: task.sortOrder || 0,
      createdAt: task.$createdAt || new Date().toISOString(),
      updatedAt: task.$updatedAt || new Date().toISOString()
    }));
    
    return NextResponse.json({ 
      success: true, 
      data: {
        tasks: transformedTasks,
        stats
      }
    });
  } catch (error: any) {
    console.error('Error fetching social tasks:', error);
    
    // If API key is not configured, return an error message instead of mock data
    if (!API_CONFIG.APPWRITE_API_KEY) {
      return NextResponse.json({ 
        success: false, 
        error: 'Appwrite API key not configured. Please set APPWRITE_API_KEY in your .env file.' 
      }, { status: 500 });
    }
    
    return NextResponse.json({ success: false, error: error.message || 'Failed to fetch social tasks' }, { status: 500 });
  }
}

// POST /api/social - Create or update social task
export async function POST(request: Request) {
  try {
    const taskData = await request.json();
    
    // Transform task data to match Appwrite collection structure
    const transformedTaskData = {
      title: taskData.title,
      description: taskData.description,
      platform: taskData.platform,
      taskType: taskData.taskType || 'generic',
      rewardCoins: taskData.rewardCoins || 0,
      actionUrl: taskData.actionUrl || null,
      verificationMethod: taskData.verificationMethod || 'manual',
      verificationData: taskData.verificationData || null,
      isActive: taskData.isActive !== undefined ? taskData.isActive : true,
      sortOrder: taskData.sortOrder || 0
    };
    
    let result;
    
    if (taskData.id) {
      // Update existing social task
      result = await databases.updateDocument(
        API_CONFIG.DATABASE_ID,
        collections.socialTasks,
        taskData.id,
        transformedTaskData
      );
    } else {
      // Create new social task
      result = await databases.createDocument(
        API_CONFIG.DATABASE_ID,
        collections.socialTasks,
        'unique()',
        transformedTaskData
      );
    }
    
    // Transform result to match KtMobileApp data model
    const transformedResult = {
      id: result.$id,
      title: result.title,
      description: result.description,
      platform: result.platform,
      taskType: result.taskType || 'generic',
      rewardCoins: result.rewardCoins || 0,
      actionUrl: result.actionUrl || null,
      verificationMethod: result.verificationMethod || 'manual',
      verificationData: (result as any).verificationData || null,
      isActive: result.isActive || false,
      sortOrder: result.sortOrder || 0,
      createdAt: result.$createdAt || new Date().toISOString(),
      updatedAt: result.$updatedAt || new Date().toISOString()
    };
    
    return NextResponse.json({ success: true, message: 'Social task saved successfully', data: transformedResult });
  } catch (error: any) {
    console.error('Error saving social task:', error);
    
    // If API key is not configured, return an error message
    if (!API_CONFIG.APPWRITE_API_KEY) {
      return NextResponse.json({ 
        success: false, 
        error: 'Appwrite API key not configured. Please set APPWRITE_API_KEY in your .env file.' 
      }, { status: 500 });
    }
    
    return NextResponse.json({ success: false, error: error.message || 'Failed to save social task' }, { status: 500 });
  }
}

// DELETE /api/social/[id] - Delete social task from Appwrite
export async function DELETE(request: Request, { params }: { params: Promise<any> }) {
  try {
    const { id } = await params;
    
    if (!id) {
      return NextResponse.json({ success: false, error: 'Social task ID is required' }, { status: 400 });
    }
    
    // Delete social task from Appwrite
    await databases.deleteDocument(
      API_CONFIG.DATABASE_ID,
      collections.socialTasks,
      id
    );
    
    return NextResponse.json({ success: true, message: `Social task ${id} deleted successfully` });
  } catch (error: any) {
    console.error('Error deleting social task:', error);
    
    // If API key is not configured, return an error message
    if (!API_CONFIG.APPWRITE_API_KEY) {
      return NextResponse.json({ 
        success: false, 
        error: 'Appwrite API key not configured. Please set APPWRITE_API_KEY in your .env file.' 
      }, { status: 500 });
    }
    
    return NextResponse.json({ success: false, error: error.message || 'Failed to delete social task' }, { status: 500 });
  }
}