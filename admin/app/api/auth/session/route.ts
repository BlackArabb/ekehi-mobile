import { NextResponse } from 'next/server';
import { Client, Account } from 'appwrite';

export const dynamic = 'force-dynamic';

export async function GET() {
  try {
    const client = new Client()
      .setEndpoint('https://fra.cloud.appwrite.io/v1')
      .setProject('68c2dd6e002112935ed2');

    const account = new Account(client);
    const user = await account.get();

    return NextResponse.json({
      success: true,
      data: {
        userId: user.$id,
        email: user.email,
        name: user.name || user.email,
      }
    });
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: 'No active session' },
      { status: 401 }
    );
  }
}
