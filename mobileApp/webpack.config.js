const createExpoWebpackConfigAsync = require('@expo/webpack-config');

module.exports = async function (env, argv) {
  const config = await createExpoWebpackConfigAsync(env, argv);
  
  // Fix for Platform module issue
  config.resolve.alias = {
    ...config.resolve.alias,
    'react-native$': 'react-native-web',
    'react-native/Libraries/Utilities/Platform$': 'react-native-web/dist/exports/Platform',
  };

  // Exclude react-native-google-mobile-ads from web build since it's native only
  if (config.module && config.module.rules) {
    config.module.rules.push({
      test: /react-native-google-mobile-ads/,
      use: 'null-loader',
    });
  }

  // Optimize bundle size for production
  if (config.mode === 'production') {
    // Enable scope hoisting for smaller bundles
    config.optimization.concatenateModules = true;
    
    // Split chunks for better caching
    config.optimization.splitChunks = {
      chunks: 'all',
      cacheGroups: {
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
          chunks: 'all',
        },
      },
    };
    
    // Minimize JavaScript
    if (config.optimization.minimizer && config.optimization.minimizer.length > 0) {
      // Ensure Terser is properly configured
      const terserPlugin = config.optimization.minimizer.find(
        plugin => plugin.constructor.name === 'TerserPlugin'
      );
      
      if (terserPlugin) {
        terserPlugin.options.terserOptions = {
          ...terserPlugin.options.terserOptions,
          compress: {
            ...terserPlugin.options.terserOptions.compress,
            drop_console: true, // Remove console.log in production
            drop_debugger: true, // Remove debugger statements
          },
        };
      }
    }
  }

  return config;
};