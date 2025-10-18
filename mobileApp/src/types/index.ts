export interface UserProfile {
  id: string;
  userId: string;
  username?: string;
  totalCoins: number;
  coinsPerSecond: number; // Deprecated - will be replaced with autoMiningRate
  autoMiningRate: number; // New field for auto mining rate
  miningPower: number;
  referralBonusRate: number; // New field for referral bonus
  currentStreak: number;
  longestStreak: number;
  lastLoginDate?: string;
  referralCode?: string;
  referredBy?: string;
  totalReferrals: number;
  lifetimeEarnings: number;
  dailyMiningRate: number;
  maxDailyEarnings: number;
  todayEarnings: number;
  lastMiningDate?: string;
  streakBonusClaimed: number;
  createdAt: string;
  updatedAt: string;
}

export interface MiningSession {
  id: string;
  userId: string;
  coinsEarned: number;
  clicksMade: number;
  sessionDuration: number;
  createdAt: string;
  updatedAt: string;
}

export interface SocialTask {
  id: string;
  title: string;
  description: string;
  platform: string;
  taskType: string;
  rewardCoins: number;
  actionUrl?: string;
  verificationMethod: string;
  isActive: boolean;
  sortOrder: number;
  isCompleted?: boolean;
}

export interface Achievement {
  id: string;
  achievementId: string;
  title: string;
  description: string;
  type: string;
  target: number;
  reward: number;
  rarity: string;
  isActive: boolean;
  isUnlocked?: boolean;
  isClaimed?: boolean;
  progress?: number;
}

export interface LeaderboardEntry {
  rank: number;
  username: string;
  totalCoins: number;
  miningPower: number;
  currentStreak?: number;
  totalReferrals?: number;
  lastLoginDate?: string;
}

export interface PresalePurchase {
  id: string;
  userId: string;
  amountUsd: number;
  tokensAmount: number;
  transactionHash?: string;
  status: string;
  paymentMethod?: string;
  createdAt: string;
}

export interface APIResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
}

export interface User {
  id: string;
  email?: string;
  name?: string;
}
