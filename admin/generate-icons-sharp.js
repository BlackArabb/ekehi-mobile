#!/usr/bin/env node

/**
 * Generate PNG icons from SVG using sharp
 * Run with: node generate-icons-sharp.js
 */

const sharp = require('sharp');
const path = require('path');
const fs = require('fs');

const publicDir = path.join(__dirname, 'public');

// Read SVG file
const svgPath = path.join(publicDir, 'icon.svg');
const svgContent = fs.readFileSync(svgPath);

console.log('Generating PNG icons from SVG...\n');

async function generateIcons() {
  // 144x144 icon (required for PWA install)
  await sharp(svgContent)
    .resize(144, 144)
    .png()
    .toFile(path.join(publicDir, 'icon-144x144.png'));
  console.log('✓ Created icon-144x144.png');

  // 192x192 icon
  await sharp(svgContent)
    .resize(192, 192)
    .png()
    .toFile(path.join(publicDir, 'icon-192x192.png'));
  console.log('✓ Created icon-192x192.png');

  // 512x512 icon
  await sharp(svgContent)
    .resize(512, 512)
    .png()
    .toFile(path.join(publicDir, 'icon-512x512.png'));
  console.log('✓ Created icon-512x512.png');

  // Maskable icon 
  await sharp(svgContent)
    .resize(192, 192)
    .png()
    .toFile(path.join(publicDir, 'icon-maskable.png'));
  console.log('✓ Created icon-maskable.png');

  // Screenshot narrow (540x720)
  await sharp(svgContent)
    .resize(540, 720)
    .png()
    .toFile(path.join(publicDir, 'screenshot-540x720.png'));
  console.log('✓ Created screenshot-540x720.png');

  // Screenshot wide (1280x720)
  await sharp(svgContent)
    .resize(1280, 720)
    .png()
    .toFile(path.join(publicDir, 'screenshot-1280x720.png'));
  console.log('✓ Created screenshot-1280x720.png');

  console.log('\n✓ All icons generated successfully!');
}

generateIcons().catch(console.error);
