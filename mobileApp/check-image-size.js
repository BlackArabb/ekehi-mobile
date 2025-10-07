const fs = require('fs');
const path = require('path');

// Function to get image dimensions from PNG header
function getPngDimensions(buffer) {
  // PNG signature: 89 50 4E 47 0D 0A 1A 0A
  if (buffer[0] === 0x89 && buffer[1] === 0x50 && buffer[2] === 0x4E && buffer[3] === 0x47) {
    // Width is at bytes 16-19 (big endian)
    const width = buffer.readUInt32BE(16);
    // Height is at bytes 20-23 (big endian)
    const height = buffer.readUInt32BE(20);
    return { width, height };
  }
  return null;
}

const imagePath = path.join(__dirname, 'assets', 'splash.png');

try {
  const buffer = fs.readFileSync(imagePath);
  const dimensions = getPngDimensions(buffer);
  
  if (dimensions) {
    console.log(`Current splash.png dimensions: ${dimensions.width}x${dimensions.height}`);
  } else {
    console.log('File is not a valid PNG');
  }
} catch (error) {
  console.error('Error reading file:', error.message);
}