#!/bin/bash

# Explicitly check and use Node.js 22
echo "Checking Node.js version..."
NODE_VERSION=$(node --version)
echo "Current Node.js version: $NODE_VERSION"

# If not Node.js 22, we'll have to work with what's available
# Vercel should automatically use the version specified in package.json engines

# Install pnpm with the same version as specified in package.json
echo "Installing pnpm..."
npm install -g pnpm@9.12.3

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

# Install dependencies with proper error handling
echo "Installing dependencies..."
PNPM_INSTALL_EXIT_CODE=0
pnpm install --no-frozen-lockfile --fetch-timeout=60000 --prefer-offline || PNPM_INSTALL_EXIT_CODE=$?

# Check installation result
if [ $PNPM_INSTALL_EXIT_CODE -ne 0 ]; then
  echo "Install failed with exit code $PNPM_INSTALL_EXIT_CODE, trying clean install..."
  rm -rf node_modules
  pnpm install --no-frozen-lockfile --fetch-timeout=60000 || {
    echo "Clean install also failed"
    exit 1
  }
fi

# Verify node_modules was created
if [ ! -d "node_modules" ]; then
  echo "ERROR: node_modules directory was not created"
  exit 1
fi

# Verify vite is installed
echo "Checking if vite is installed..."
if [ ! -d "node_modules/vite" ]; then
  echo "vite not found in node_modules, this is a critical error"
  echo "Contents of node_modules:"
  ls -la node_modules/ | head -20
  exit 1
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
pnpm run build || BUILD_EXIT_CODE=$?

if [ $BUILD_EXIT_CODE -ne 0 ]; then
  echo "Build with pnpm failed with exit code $BUILD_EXIT_CODE, trying direct vite build..."
  if [ -f "node_modules/.bin/vite" ]; then
    node_modules/.bin/vite build
  else
    echo "ERROR: Cannot run vite build, binary not found"
    exit 1
  fi
fi

echo "Build completed successfully!"