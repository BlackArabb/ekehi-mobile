/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/react-app/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'ekehi-gold': '#ffa000',
        'deep-black': '#000000',
        'rich-charcoal': '#0a0a0a',
        'dark-slate': '#1a1a1a',
        'amber-glow': '#ffb333',
        'dark-amber': '#cc8000',
        'pure-white': '#ffffff',
        'soft-white': '#f5f5f5',
        'medium-gray': '#9e9e9e',
        'charcoal-gray': '#424242',
        'success-green': '#4caf50',
        'warning-amber': '#ff9800',
        'info-blue': '#2196f3',
        'error-red': '#f44336',
      },
      fontFamily: {
        'display': ['Space Grotesk', 'Inter', 'sans-serif'],
        'mono': ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      animation: {
        'float': 'float 6s ease-in-out infinite',
        'pulse-gold': 'pulse-gold 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        'fade-in': 'fadeIn 0.8s ease-out',
        'slide-up': 'slideUp 0.8s ease-out',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-20px)' },
        },
        'pulse-gold': {
          '0%, 100%': { boxShadow: '0 0 0 0 rgba(255, 160, 0, 0.7)' },
          '70%': { boxShadow: '0 0 0 10px rgba(255, 160, 0, 0)' },
        },
        fadeIn: {
          from: { opacity: '0' },
          to: { opacity: '1' },
        },
        slideUp: {
          from: { opacity: '0', transform: 'translateY(30px)' },
          to: { opacity: '1', transform: 'translateY(0)' },
        },
      },
      backdropBlur: {
        lg: '20px',
      },
      // Add more responsive breakpoints for better mobile support
      screens: {
        'xs': '480px',
        'sm': '640px',
        'md': '768px',
        'lg': '1024px',
        'xl': '1280px',
        '2xl': '1536px',
      },
    },
  },
  plugins: [],
  // Enable mobile-first approach
  corePlugins: {
    accessibility: true,
  },
};