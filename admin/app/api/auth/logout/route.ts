import { NextResponse } from 'next/server';
import { Client, Account } from 'appwrite';

export async function DELETE() {
  try {
    const client = new Client()
      .setEndpoint('https://fra.cloud.appwrite.io/v1')
      .setProject('68c2dd6e002112935ed2');

    const account = new Account(client);
    await account.deleteSession('current');

    return NextResponse.json({ success: true });
  } catch (error: any) {
    console.error('Logout error:', error);
    return NextResponse.json(
      { success: false, error: 'Logout failed' },
      { status: 500 }
    );
  }
}
