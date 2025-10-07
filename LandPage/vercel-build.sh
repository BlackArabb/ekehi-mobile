#!/bin/bash

# Debug: Print environment information
echo "=== Environment Information ==="
echo "Node version: $(node --version)"
echo "NPM version: $(npm --version)"
echo "Current directory: $(pwd)"
echo "Directory contents:"
ls -la

# Install pnpm
echo "=== Installing pnpm ==="
npm install -g pnpm
echo "PNPM version: $(pnpm --version)"

# Clear npm cache to avoid issues
echo "=== Clearing npm cache ==="
npm cache clean --force

# Use existing .npmrc configuration for network settings
echo "=== .npmrc configuration ==="
if [ -f .npmrc ]; then
  cat .npmrc
else
  echo ".npmrc file not found"
fi

# Check package.json
echo "=== Package.json ==="
if [ -f package.json ]; then
  echo "package.json found"
  cat package.json | grep -E "(name|version|scripts|dependencies|devDependencies)"
else
  echo "ERROR: package.json not found!"
  exit 1
fi

# Install dependencies with pnpm
echo "=== Installing dependencies ==="
echo "Running pnpm install --no-frozen-lockfile --prefer-offline"
pnpm install --no-frozen-lockfile --prefer-offline

# Check if install was successful
if [ $? -ne 0 ]; then
  echo "First install attempt failed"
  
  # Try alternative approaches
  echo "Trying with clean install..."
  rm -rf node_modules
  pnpm install --no-frozen-lockfile
  
  if [ $? -ne 0 ]; then
    echo "Second install attempt failed"
    echo "Listing available space:"
    df -h
    echo "Listing pnpm store:"
    pnpm store path 2>/dev/null || echo "Could not get pnpm store path"
    exit 1
  fi
fi

# Verify dependencies
echo "=== Verifying dependencies ==="
echo "node_modules size:"
du -sh node_modules 2>/dev/null || echo "Could not determine node_modules size"

echo "Checking for vite:"
if [ -d "node_modules/vite" ]; then
  echo "vite found"
else
  echo "vite NOT found"
  echo "Available modules in node_modules:"
  ls node_modules | grep -E "(vite|react)" || echo "No matching modules found"
fi

# Build the project
echo "=== Building the project ==="
echo "Available scripts:"
cat package.json | grep -A 10 '"scripts"'
echo "Running pnpm run build"
pnpm run build

# Check build result
if [ $? -ne 0 ]; then
  echo "Build failed"
  exit 1
else
  echo "Build successful"
fi