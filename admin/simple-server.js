const http = require('http');
const fs = require('fs');
const path = require('path');

const server = http.createServer((req, res) => {
  console.log(`Request: ${req.method} ${req.url}`);
  
  // Serve the dashboard page
  if (req.url === '/' || req.url === '/dashboard') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end('<h1>Ekehi Admin Dashboard</h1><p>Dashboard page</p>');
  } 
  // Serve the users page
  else if (req.url === '/dashboard/users') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end('<h1>Users Management</h1><p>Users page</p>');
  }
  // Serve the presale page
  else if (req.url === '/dashboard/presale') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end('<h1>Presale Management</h1><p>Presale page</p>');
  }
  // Serve the wallet page
  else if (req.url === '/dashboard/wallet') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end('<h1>Wallet Management</h1><p>Wallet page</p>');
  }
  // Serve the social tasks page
  else if (req.url === '/dashboard/social') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end('<h1>Social Tasks Management</h1><p>Social tasks page</p>');
  }
  // Serve the ads page
  else if (req.url === '/dashboard/ads') {
    res.writeHead(200, { 'Content-Type': 'text/html' });
    res.end('<h1>Ads Management</h1><p>Ads page</p>');
  }
  // Serve 404 for other routes
  else {
    res.writeHead(404, { 'Content-Type': 'text/html' });
    res.end('<h1>404 Not Found</h1><p>Page not found</p>');
  }
});

server.listen(3001, () => {
  console.log('Simple server running on http://localhost:3001');
  console.log('Available pages:');
  console.log('  /dashboard - Dashboard');
  console.log('  /dashboard/users - Users Management');
  console.log('  /dashboard/presale - Presale Management');
  console.log('  /dashboard/wallet - Wallet Management');
  console.log('  /dashboard/social - Social Tasks Management');
  console.log('  /dashboard/ads - Ads Management');
});