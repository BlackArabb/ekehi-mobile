#!/bin/bash

# Explicitly check and use Node.js 22
echo "Checking Node.js version..."
NODE_VERSION=$(node --version)
echo "Current Node.js version: $NODE_VERSION"

# Install yarn and pnpm
echo "Installing yarn and pnpm..."
npm install -g yarn pnpm@8.15.8
echo "Yarn version: $(yarn --version)"
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
rm -f package-lock.json yarn.lock

# Set npm registry to a more reliable mirror and configure network settings
echo "Configuring npm registry and network settings..."
npm config set registry https://registry.npmjs.org/
npm config set strict-ssl false

# Workaround for ERR_INVALID_THIS error
echo "Setting environment variables to workaround ERR_INVALID_THIS error..."
export NODE_TLS_REJECT_UNAUTHORIZED=0
export npm_config_registry=https://registry.npmjs.org/
export npm_config_strict_ssl=false

# Try yarn first as it might be more reliable
echo "Attempting installation with yarn..."
yarn install --prefer-offline --legacy-peer-deps || {
  echo "yarn install failed, trying npm..."
  
  # Try npm with legacy peer deps
  echo "Attempting installation with npm and legacy peer deps..."
  npm install --prefer-offline --legacy-peer-deps || {
    echo "npm install failed, trying pnpm with legacy peer deps..."
    
    # First attempt: Standard pnpm install with legacy peer deps
    echo "Attempt 1: Standard pnpm install with legacy peer deps"
    pnpm install --no-frozen-lockfile --fetch-timeout=180000 --prefer-offline --legacy-peer-deps || {
      echo "First pnpm install attempt failed"
      
      # Second attempt: Clean install with registry override
      echo "Attempt 2: Clean pnpm install with registry override"
      rm -rf node_modules
      pnpm store prune 2>/dev/null || echo "Could not prune pnpm store"
      
      # Try with explicit registry and legacy peer deps
      pnpm install --no-frozen-lockfile --registry=https://registry.npmjs.org/ --fetch-timeout=180000 --legacy-peer-deps || {
        echo "Second pnpm install attempt failed"
        
        # Third attempt: Force install with npm as fallback
        echo "Attempt 3: Force install with npm as fallback"
        npm install --force --legacy-peer-deps || {
          echo "All install attempts failed"
          exit 1
        }
      }
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
if command -v vite &> /dev/null; then
  yarn build || BUILD_EXIT_CODE=$?
else
  node_modules/.bin/vite build || BUILD_EXIT_CODE=$?
fi

if [ $BUILD_EXIT_CODE -ne 0 ]; then
  echo "Build failed with exit code $BUILD_EXIT_CODE"
  exit 1
fi

echo "Build completed successfully!"