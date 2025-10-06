import path from "path";
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { cloudflare } from "@cloudflare/vite-plugin";

export default defineConfig({
  plugins: [react(), cloudflare()],
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
        manualChunks: {
          vendor: ['react', 'react-dom'],
          ui: ['lucide-react'],
        }
      }
    }
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
  }
});