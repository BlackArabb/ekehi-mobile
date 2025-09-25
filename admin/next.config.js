/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  outputFileTracingRoot: __dirname,
  images: {
    domains: ['images.unsplash.com', 'www.google.com'],
  },
}

module.exports = nextConfig