import { useEffect, useState } from 'react';
import React from 'react';
import { Stack } from 'expo-router';
import { AuthProvider } from '@/contexts/AuthContext';
import { MiningProvider } from '@/contexts/MiningContext';
import { ReferralProvider } from '@/contexts/ReferralContext';
import { NotificationProvider } from '@/contexts/NotificationContext';
import { PresaleProvider } from '@/contexts/PresaleContext';
import { WalletProvider } from '@/contexts/WalletContext'; // Added WalletProvider import
import AutoMiningManager from '@/components/AutoMiningManager';

export default function RootLayout() {
  return (
    <AuthProvider>
      <PresaleProvider>
        <MiningProvider>
          <ReferralProvider>
            <NotificationProvider>
              <WalletProvider>
                <AutoMiningManager />
                <Stack>
                  <Stack.Screen name="index" options={{ headerShown: false }} />
                  <Stack.Screen name="auth" options={{ headerShown: false }} />
                  <Stack.Screen name="forgot-password" options={{ headerShown: false }} />
                  <Stack.Screen name="reset-password" options={{ headerShown: false }} />
                  <Stack.Screen name="verify-email" options={{ headerShown: false }} />
                  <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
                  <Stack.Screen name="oauth/callback" options={{ headerShown: false }} />
                  <Stack.Screen name="oauth/return" options={{ headerShown: false }} />
                  <Stack.Screen name="referral/[code]" options={{ headerShown: false }} />
                </Stack>
              </WalletProvider>
            </NotificationProvider>
          </ReferralProvider>
        </MiningProvider>
      </PresaleProvider>
    </AuthProvider>
  );
}