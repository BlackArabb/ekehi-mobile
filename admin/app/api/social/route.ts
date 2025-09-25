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
    
    return NextResponse.json({ 
      success: true, 
      data: {
        tasks: response.documents,
        stats
      }
    });
  } catch (error: any) {
    console.error('Error fetching social tasks:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to fetch social tasks' }, { status: 500 });
  }
}

// POST /api/social - Create or update social task in Appwrite
export async function POST(request: Request) {
  try {
    const taskData = await request.json();
    
    let result;
    
    if (taskData.$id) {
      // Update existing social task
      result = await databases.updateDocument(
        API_CONFIG.DATABASE_ID,
        collections.socialTasks,
        taskData.$id,
        taskData
      );
    } else {
      // Create new social task
      result = await databases.createDocument(
        API_CONFIG.DATABASE_ID,
        collections.socialTasks,
        'unique()',
        taskData
      );
    }
    
    return NextResponse.json({ success: true, message: 'Social task saved successfully', data: result });
  } catch (error: any) {
    console.error('Error saving social task:', error);
    return NextResponse.json({ success: false, error: error.message || 'Failed to save social task' }, { status: 500 });
  }
}

// DELETE /api/social/[id] - Delete social task from Appwrite
export async function DELETE(request: Request, { params }: { params: { id: string } }) {
  try {
    const { id } = params;
    
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
    return NextResponse.json({ success: false, error: error.message || 'Failed to delete social task' }, { status: 500 });
  }
}