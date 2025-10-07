#!/bin/bash

# Install pnpm
echo "Installing pnpm..."
npm install -g pnpm

# Clear npm cache to avoid issues
echo "Clearing npm cache..."
npm cache clean --force

# Set npm registry to a more reliable mirror
echo "Setting npm registry..."
npm config set registry https://registry.npmjs.org/

# Install dependencies with pnpm, with retry logic and network timeout settings
echo "Installing dependencies..."
export PNPM_NETWORK_TIMEOUT=60000
pnpm install --no-frozen-lockfile --prefer-offline --network-timeout=60000 || {
  echo "First install attempt failed, retrying with clean install..."
  rm -rf node_modules
  pnpm store prune
  pnpm install --no-frozen-lockfile --network-timeout=60000 || {
    echo "Second install attempt failed, trying with npm registry override..."
    pnpm install --no-frozen-lockfile --registry=https://registry.npmjs.org/ --network-timeout=60000
  }
}

# Verify that vite is installed
echo "Verifying vite installation..."
pnpm list vite

# Verify TypeScript installation
echo "Verifying TypeScript installation..."
pnpm list typescript

# Build the project
echo "Building the project..."
pnpm run build