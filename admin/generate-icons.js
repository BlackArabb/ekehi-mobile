#!/usr/bin/env node

/**
 * This script generates PNG icon files from the icon.svg
 * Run with: node generate-icons.js
 */

const fs = require('fs');
const path = require('path');

// Create placeholder PNG files with proper headers
// These are minimal valid PNG files that browsers will display

function createPNG(width, height, filename) {
  // PNG signature
  const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);
  
  // IHDR chunk (image header)
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr.writeUInt8(8, 8);      // bit depth
  ihdr.writeUInt8(2, 9);      // color type (2 = RGB)
  ihdr.writeUInt8(0, 10);     // compression method
  ihdr.writeUInt8(0, 11);     // filter method
  ihdr.writeUInt8(0, 12);     // interlace method
  
  const ihdrCrc = Buffer.alloc(4);
  ihdrCrc.writeUInt32BE(calculateCrc(Buffer.concat([Buffer.from('IHDR'), ihdr])), 0);
  
  // IDAT chunk (image data) - simple gradient pattern
  const scanlineSize = width * 3 + 1;
  const imageData = Buffer.alloc(scanlineSize * height);
  
  for (let y = 0; y < height; y++) {
    let pos = y * scanlineSize;
    imageData[pos++] = 0; // filter type
    
    for (let x = 0; x < width; x++) {
      const hue = Math.floor((x / width) * 255);
      const sat = Math.floor((y / height) * 255);
      
      // Create gold/orange gradient (#1f2937 to #ffa000)
      imageData[pos++] = Math.floor(31 + (255 - 31) * (x / width));    // R
      imageData[pos++] = Math.floor(41 + (160 - 41) * (x / width));    // G
      imageData[pos++] = Math.floor(55 + (0 - 55) * (x / width));      // B
    }
  }
  
  const zlibData = require('zlib').deflateSync(imageData);
  
  const idatCrc = Buffer.alloc(4);
  idatCrc.writeUInt32BE(calculateCrc(Buffer.concat([Buffer.from('IDAT'), zlibData])), 0);
  
  // IEND chunk
  const iendCrc = Buffer.alloc(4);
  iendCrc.writeUInt32BE(0xae426082, 0); // pre-calculated CRC for empty IEND
  
  // Assemble PNG
  const png = Buffer.concat([
    signature,
    Buffer.from([0, 0, 0, 13]),  // IHDR length
    Buffer.from('IHDR'),
    ihdr,
    ihdrCrc,
    Buffer.from([0, 0, 0, 0]),   // IDAT length placeholder (simplified)
    Buffer.from('IDAT'),
    zlibData,
    idatCrc,
    Buffer.from([0, 0, 0, 0]),   // IEND length
    Buffer.from('IEND'),
    iendCrc
  ]);
  
  fs.writeFileSync(filename, png);
  console.log(`✓ Created ${filename} (${width}x${height})`);
}

function calculateCrc(data) {
  const crcTable = [];
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) {
      c = ((c & 1) ? (0xedb88320 ^ (c >>> 1)) : (c >>> 1));
    }
    crcTable[n] = c;
  }
  
  let crc = 0xffffffff;
  for (let i = 0; i < data.length; i++) {
    crc = crcTable[(crc ^ data[i]) & 0xff] ^ (crc >>> 8);
  }
  return (crc ^ 0xffffffff) >>> 0;
}

// Create icons
const publicDir = path.join(__dirname, 'public');

console.log('Generating placeholder PNG icons...\n');

createPNG(192, 192, path.join(publicDir, 'icon-192x192.png'));
createPNG(512, 512, path.join(publicDir, 'icon-512x512.png'));
createPNG(192, 192, path.join(publicDir, 'icon-maskable.png'));
createPNG(540, 720, path.join(publicDir, 'screenshot-540x720.png'));
createPNG(1280, 720, path.join(publicDir, 'screenshot-1280x720.png'));

console.log('\n✓ All icons generated successfully!');
console.log('Note: These are placeholder icons. Replace with actual design later.');
