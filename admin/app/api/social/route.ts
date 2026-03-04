import { NextResponse } from 'next/server';
import { API_CONFIG } from '@/src/config/api';

export const dynamic = 'force-dynamic';

const headers = {
  'Content-Type': 'application/json',
  'X-Appwrite-Key': API_CONFIG.APPWRITE_API_KEY || '',
};

// GET /api/social - Fetch social tasks
export async function GET() {
  try {
    console.log('[Social API] Fetching social tasks...');
    const url = `${API_CONFIG.APPWRITE_ENDPOINT}/databases/${API_CONFIG.DATABASE_ID}/collections/${API_CONFIG.COLLECTIONS.SOCIAL_TASKS}/documents`;
    const response = await fetch(url, { method: 'GET', headers });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Social tasks fetch failed: ${response.status} - ${errorText}`);
    }

    const data = await response.json() as any;
    const tasks = data.documents || [];

    const activeTasks = tasks.filter((task: any) => task.isActive).length;
    const stats = { totalTasks: tasks.length, activeTasks, inactiveTasks: tasks.length - activeTasks };

    const transformedTasks = tasks.map((task: any) => ({
      id: task.$id, title: task.title, description: task.description, platform: task.platform,
      taskType: task.taskType || 'generic', rewardCoins: task.rewardCoins || 0, actionUrl: task.actionUrl || null,
      verificationMethod: task.verificationMethod || 'manual', isActive: task.isActive || false,
      sortOrder: task.sortOrder || 0, maxCompletionsPerDay: task.maxCompletionsPerDay || 1,
      cooldownMinutes: task.cooldownMinutes || 0, createdAt: task.$createdAt, updatedAt: task.$updatedAt
    }));

    return NextResponse.json({ success: true, data: { tasks: transformedTasks, stats } });
  } catch (error: any) {
    console.error('[Social API] Error:', error);
    return NextResponse.json({ success: false, error: error.message }, { status: 500 });
  }
}

// POST /api/social - Create or update social task
export async function POST(request: Request) {
  try {
    const taskData = await request.json();
    const baseUrl = `${API_CONFIG.APPWRITE_ENDPOINT}/databases/${API_CONFIG.DATABASE_ID}/collections/${API_CONFIG.COLLECTIONS.SOCIAL_TASKS}/documents`;
    const url = taskData.id ? `${baseUrl}/${taskData.id}` : baseUrl;
    const method = taskData.id ? 'PATCH' : 'POST';

    const payload = {
      title: taskData.title, description: taskData.description, platform: taskData.platform,
      taskType: taskData.taskType || 'generic', rewardCoins: taskData.rewardCoins || 0,
      actionUrl: taskData.actionUrl || null, verificationMethod: taskData.verificationMethod || 'manual',
      isActive: taskData.isActive !== undefined ? taskData.isActive : true,
      sortOrder: taskData.sortOrder || 0, maxCompletionsPerDay: taskData.maxCompletionsPerDay || 1,
      cooldownMinutes: taskData.cooldownMinutes || 0
    };

    const res = await fetch(url, { method, headers, body: JSON.stringify(payload) });
    if (!res.ok) {
      const errorText = await res.text();
      throw new Error(`Save failed: ${res.status} - ${errorText}`);
    }
    const result = await res.json();
    return NextResponse.json({ success: true, message: 'Social task saved', data: result });
  } catch (error: any) {
    console.error('[Social API] POST Error:', error);
    return NextResponse.json({ success: false, error: error.message }, { status: 500 });
  }
}

// DELETE /api/social/[id] - Delete social task
export async function DELETE(request: Request, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    if (!id) return NextResponse.json({ success: false, error: 'ID required' }, { status: 400 });

    const url = `${API_CONFIG.APPWRITE_ENDPOINT}/databases/${API_CONFIG.DATABASE_ID}/collections/${API_CONFIG.COLLECTIONS.SOCIAL_TASKS}/documents/${id}`;
    const response = await fetch(url, { method: 'DELETE', headers });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Delete failed: ${response.status} - ${errorText}`);
    }
    return NextResponse.json({ success: true, message: `Task ${id} deleted` });
  } catch (error: any) {
    console.error('[Social API] DELETE Error:', error);
    return NextResponse.json({ success: false, error: error.message }, { status: 500 });
  }
}
