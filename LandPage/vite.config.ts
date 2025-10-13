import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  server: {
    allowedHosts: true,
    // Improve mobile development experience
    host: true,
    port: 3000,
  },
  build: {
    chunkSizeWarningLimit: 5000,
    // Optimize for mobile
    rollupOptions: {
      output: {
        // Enable cache-busting with content-based hashes
        entryFileNames: 'assets/[name].[hash].js',
        chunkFileNames: 'assets/[name].[hash].js',
        assetFileNames: 'assets/[name].[hash].[ext]',
        manualChunks: {
          vendor: ['react', 'react-dom'],
          ui: ['lucide-react'],
          charts: ['chart.js', 'react-chartjs-2'],
          routing: ['react-router'],
          validation: ['zod', '@hono/zod-validator']
        }
      }
    },
    // Enable CDN-friendly asset names
    assetsInlineLimit: 4096,
    cssCodeSplit: true,
    sourcemap: false, // Disable sourcemaps in production for smaller bundle size
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  // Optimize for mobile devices
  define: {
    // Enable mobile-specific optimizations
    __MOBILE_OPTIMIZED__: JSON.stringify(true),
  },
  css: {
    // Enable CSS optimization
    postcss: './postcss.config.js',
  },
  // Configure base URL for CDN usage
  base: process.env.NODE_ENV === 'production' ? 'https://cdn.ekehi.xyz/' : '/',
});