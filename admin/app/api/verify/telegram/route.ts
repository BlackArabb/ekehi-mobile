import { NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';

export async function POST(request: Request) {
  try {
    const { chatId, userId } = await request.json();

    if (!chatId || !userId) {
      return NextResponse.json(
        { success: false, error: 'chatId and userId are required' },
        { status: 400 }
      );
    }

    const botToken = process.env.TELEGRAM_BOT_TOKEN;
    if (!botToken) {
      console.error('[Telegram Verify] TELEGRAM_BOT_TOKEN not set');
      return NextResponse.json(
        { success: false, error: 'Telegram service unavailable' },
        { status: 503 }
      );
    }

    const url = `https://api.telegram.org/bot${botToken}/getChatMember?chat_id=${encodeURIComponent(chatId)}&user_id=${userId}`;
    const response = await fetch(url);
    const data = await response.json() as any;

    if (!data.ok) {
      return NextResponse.json({ success: true, isMember: false });
    }

    const status = data.result?.status;
    const isMember = ['member', 'administrator', 'creator'].includes(status);

    return NextResponse.json({ success: true, isMember });
  } catch (error: any) {
    console.error('[Telegram Verify] Error:', error);
    return NextResponse.json(
      { success: false, error: 'Verification failed' },
      { status: 500 }
    );
  }
}
