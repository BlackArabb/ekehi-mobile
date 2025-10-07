#!/bin/bash

# Install pnpm
echo "Installing pnpm..."
npm install -g pnpm

# Clear npm cache to avoid issues
echo "Clearing npm cache..."
npm cache clean --force

# Install dependencies with pnpm, with retry logic
echo "Installing dependencies..."
pnpm install --no-frozen-lockfile --prefer-offline || {
  echo "First install attempt failed, retrying..."
  pnpm install --no-frozen-lockfile
}

# Verify TypeScript installation
echo "Verifying TypeScript installation..."
pnpm list typescript

# Build the project
echo "Building the project..."
pnpm run build