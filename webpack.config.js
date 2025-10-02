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

  return config;
};