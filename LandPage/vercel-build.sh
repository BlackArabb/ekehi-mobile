#!/bin/bash

# Explicitly check and use Node.js 22
echo "Checking Node.js version..."
NODE_VERSION=$(node --version)
echo "Current Node.js version: $NODE_VERSION"

# Install pnpm
echo "Installing pnpm..."
npm install -g pnpm@8.15.8
echo "PNPM version: $(pnpm --version)"

# Clear npm cache
echo "Clearing npm cache..."
npm cache clean --force

# Show current directory and files
echo "Current directory: $(pwd)"
echo "Directory contents:"
ls -la

# Show .npmrc configuration
echo "=== .npmrc configuration ==="
cat .npmrc

# Remove conflicting lock files
echo "Removing conflicting lock files..."
rm -f package-lock.json

# Set npm registry to a more reliable mirror and configure network settings
echo "Configuring npm registry and network settings..."
npm config set registry https://registry.npmjs.org/
npm config set strict-ssl false

# Try npm first as it might be more reliable
echo "Attempting installation with npm first..."
npm install --prefer-offline || {
  echo "npm install failed, trying pnpm..."
  
  # First attempt: Standard pnpm install
  echo "Attempt 1: Standard pnpm install"
  pnpm install --no-frozen-lockfile --fetch-timeout=60000 --prefer-offline || {
    echo "First pnpm install attempt failed"
    
    # Second attempt: Clean install with registry override
    echo "Attempt 2: Clean pnpm install with registry override"
    rm -rf node_modules
    pnpm store prune 2>/dev/null || echo "Could not prune pnpm store"
    
    # Try with explicit registry
    pnpm install --no-frozen-lockfile --registry=https://registry.npmjs.org/ --fetch-timeout=120000 || {
      echo "Second pnpm install attempt failed"
      exit 1
    }
  }
}

# Verify node_modules was created
if [ ! -d "node_modules" ]; then
  echo "ERROR: node_modules directory was not created"
  exit 1
fi

# Verify vite is installed
echo "Checking if vite is installed..."
if [ ! -d "node_modules/vite" ]; then
  echo "vite not found in node_modules"
  ls -la node_modules/ | grep vite || echo "No vite directory found"
  
  # Try to install vite specifically
  echo "Attempting to install vite specifically..."
  pnpm add vite --save-dev || npm install vite --save-dev
fi

# Check if vite command is available in PATH
echo "Checking if vite command is available..."
if command -v vite &> /dev/null; then
  echo "vite command found in PATH"
  vite --version
else
  echo "vite command not found in PATH, will use direct path"
  if [ -f "node_modules/.bin/vite" ]; then
    echo "Direct vite binary found"
    node_modules/.bin/vite --version
  else
    echo "ERROR: vite binary not found in node_modules/.bin/"
    exit 1
  fi
fi

# Build with fallback options
echo "Building project..."
BUILD_EXIT_CODE=0
if command -v vite &> /dev/null; then
  pnpm run build || BUILD_EXIT_CODE=$?
else
  node_modules/.bin/vite build || BUILD_EXIT_CODE=$?
fi

if [ $BUILD_EXIT_CODE -ne 0 ]; then
  echo "Build failed with exit code $BUILD_EXIT_CODE"
  exit 1
fi

echo "Build completed successfully!"