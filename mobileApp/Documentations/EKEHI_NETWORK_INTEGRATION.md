# Ekehi Network Integration

This document explains how the Ekehi Network blockchain API has been integrated into the wallet and presale functionality.

## Overview

The wallet and presale systems have been updated to use the Ekehi Network blockchain for actual token transactions while maintaining Appwrite for user profile management. This hybrid approach provides the benefits of blockchain security and decentralization for financial transactions while keeping user data management efficient.

## Architecture

### Components

1. **Appwrite** - Handles user authentication and profile management
2. **Ekehi Network API** - Processes blockchain transactions and token minting
3. **Wallet Context** - Manages wallet state and coordinates between Appwrite and Ekehi Network
4. **Presale Context** - Manages token purchases and integrates with payment processing

### Data Flow

1. User connects wallet → Address generated and stored in Appwrite
2. Balance refresh → Fetches from Ekehi Network API
3. Token transfer → Processed via Ekehi Network API
4. Token purchase → Payment processed, tokens minted via Ekehi Network API
5. Transaction history → Fetched directly from Ekehi Network API

## API Endpoints

### Base URL
```
https://api.ekehi.network/v1
```

### Endpoints Implemented

1. **Get Balance**
   - `GET /balance/{address}`
   - Returns current token balance for an address

2. **Transfer Tokens**
   - `POST /transfer`
   - Initiates a token transfer between addresses

3. **Get Transaction History**
   - `GET /transactions/{address}`
   - Returns transaction history for an address

4. **Mint Tokens**
   - `POST /mint`
   - Mints new tokens for presale purchases

## Configuration

The API configuration is stored in `src/config/api.ts`:

```typescript
EKEHI_NETWORK: {
  BASE_URL: 'https://api.ekehi.network/v1',
  API_KEY: 'YOUR_API_KEY',
  CHAIN_ID: 'ekehi-mainnet',
  TOKEN_CONTRACT_ADDRESS: '0x...' // Contract address
}
```

## Security Considerations

1. **API Keys** - Should be secured and not exposed in client code in production
2. **Transaction Validation** - All transactions are processed through the blockchain
3. **Address Management** - Wallet addresses are stored in Appwrite but controlled by users
4. **Payment Processing** - Integration with secure payment providers for fiat transactions

## Implementation Details

### Wallet Integration
- Real-time balance fetching from blockchain
- Actual token transfers through Ekehi Network
- Transaction history fetched directly from blockchain
- Fallback to mock data if API is unavailable

### Presale Integration
- Payment processing integrated with secure payment providers
- Token minting through Ekehi Network API upon successful payment
- Transaction hashes stored for verification
- Real purchase history from Appwrite database

## Future Enhancements

1. **Smart Contract Integration** - Once Ekehi Network supports smart contracts
2. **Advanced Wallet Features** - Multi-token support, staking, etc.
3. **Enhanced Transaction History** - More detailed transaction information
4. **Cross-chain Support** - Integration with other blockchain networks

## Implementation Files

- `src/contexts/WalletContext.tsx` - Wallet logic with blockchain integration
- `src/contexts/PresaleContext.tsx` - Presale logic with payment and minting integration
- `src/config/api.ts` - API configuration
- `app/(tabs)/wallet.tsx` - Wallet UI with real transaction history
- `app/(tabs)/presale.tsx` - Presale UI (existing functionality maintained)