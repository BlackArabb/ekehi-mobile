const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

// Simple function to create a basic splash screen description
function createSplashDescription() {
  const description = `
Custom Splash Screen for Ekehi Network App:

1. Logo size: Reduced to 100x100 pixels
2. Text effect: "ekehi network" placed under the logo
3. Text styling:
   - "ekehi" in white with shadow effect
   - "network" in orange (#ffa000) with shadow effect
4. Background: Black (#000000)

Implementation plan:
- Replace assets/splash.png with new 100x100 logo version
- Add text rendering in the custom splash component
- Update app.json to use new splash configuration if needed
`;

  console.log(description);
  
  // Create a simple text file with instructions
  const instructionsPath = path.join(__dirname, 'SPLASH_SCREEN_INSTRUCTIONS.txt');
  fs.writeFileSync(instructionsPath, description);
  console.log('Instructions written to:', instructionsPath);
}

createSplashDescription();

async function createSplashScreen() {
  try {
    console.log('Creating custom splash screen...');
    
    // Get the current splash image
    const inputPath = path.join(__dirname, 'assets', 'splash.png');
    
    // Create a new splash screen with reduced logo size and text
    const outputPath = path.join(__dirname, 'assets', 'custom-splash.png');
    
    // Create a base image with the same background color as specified in app.json
    const baseImage = sharp({
      create: {
        width: 1000,
        height: 1000,
        channels: 4,
        background: { r: 0, g: 0, b: 0, alpha: 1 } // #000000 (black)
      }
    });
    
    // Resize the logo to 100x100
    const logoBuffer = await sharp(inputPath)
      .resize(100, 100)
      .toBuffer();
    
    // Create the final image by compositing the logo and text
    const finalImage = await baseImage
      .composite([
        {
          input: logoBuffer,
          top: 350,
          left: 450
        }
      ])
      .png()
      .toBuffer();
    
    // Since Sharp doesn't have built-in text rendering, we'll create a separate image for text
    // and composite it. For now, we'll just create the logo-centered splash screen.
    await sharp(finalImage)
      .toFile(outputPath);
    
    console.log('Custom splash screen created successfully!');
    console.log('Output path:', outputPath);
    
    // Also create a 200x200 version for compatibility
    const smallOutputPath = path.join(__dirname, 'assets', 'custom-splash-200.png');
    await sharp(finalImage)
      .resize(200, 200)
      .toFile(smallOutputPath);
    
    console.log('Small version created:', smallOutputPath);
    
  } catch (error) {
    console.error('Error creating splash screen:', error);
  }
}

createSplashScreen();