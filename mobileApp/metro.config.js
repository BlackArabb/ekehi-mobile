// Learn more https://docs.expo.io/guides/customizing-metro
const { getDefaultConfig } = require('expo/metro-config');

/** @type {import('expo/metro-config').MetroConfig} */
const config = getDefaultConfig(__dirname);

// Add support for .mjs files
config.resolver.sourceExts.push('mjs');

// Optimize bundle size
config.resolver.assetExts = config.resolver.assetExts.filter(ext => !ext.includes('svg'));
config.resolver.sourceExts = [...config.resolver.sourceExts, 'svg'];

// Enable inline requires for better performance
config.transformer.getTransformOptions = async () => ({
  transform: {
    experimentalImportSupport: false,
    inlineRequires: true,
  },
});

module.exports = config;