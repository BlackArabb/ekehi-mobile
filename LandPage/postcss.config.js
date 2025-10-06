export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {
      // Add browser prefixes for better mobile support
      overrideBrowserslist: [
        '> 1%',
        'last 2 versions',
        'not dead',
        'iOS >= 9',
        'Android >= 4.4'
      ]
    },
  },
};