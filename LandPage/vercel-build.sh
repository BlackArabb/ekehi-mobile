#!/bin/bash

# Install pnpm
npm install -g pnpm

# Install dependencies with pnpm
pnpm install --no-frozen-lockfile

# Build the project
pnpm run build