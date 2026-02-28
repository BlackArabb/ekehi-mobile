/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  outputFileTracingRoot: __dirname,
  images: {
    domains: ['images.unsplash.com', 'www.google.com'],
  },
  // Configure headers for PWA and service worker
  async headers() {
    return [
      {
        source: '/sw.js',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, max-age=0, must-revalidate',
          },
          {
            key: 'Service-Worker-Allowed',
            value: '/',
          },
        ],
      },
      {
        source: '/manifest.json',
        headers: [
          {
            key: 'Content-Type',
            value: 'application/manifest+json',
          },
        ],
      },
      {
        source: '/(.*)',
        headers: [
          {
            key: 'Cache-Control',
            value: 'public, max-age=0, must-revalidate',
          },
        ],
      },
    ];
  },
  // Increase webpack timeout to handle chunk loading issues
  webpack: (config, { dev, isServer }) => {
    // Increase timeout for chunk loading
    if (!dev) {
      config.optimization = config.optimization || {};
      config.optimization.chunkIds = 'named';
    }
    
    // Add timeout handling for development
    if (dev && !isServer) {
      config.devServer = config.devServer || {};
      config.devServer.timeout = '120000'; // 2 minutes timeout
    }
    
    return config;
  },
  // Configure runtime chunk behavior
  experimental: {
    appDir: true,
    // Increase chunk timeout
    serverComponentsExternalPackages: [],
  },
}

module.exports = nextConfig
