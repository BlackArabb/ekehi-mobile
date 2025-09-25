// Cloudflare D1 Data Export Script
// 
// This script helps export data from Cloudflare D1 database to JSON files
// for migration to Appwrite Database
// 
// Usage:
// 1. Export SQL data: pnpm run wrangler-export
// 2. Convert to JSON: pnpm run export-cloudflare-data

const fs = require('fs');
const path = require('path');

// Configuration
const CLOUDFLARE_DATA_DIR = './cloudflare-data';
const OUTPUT_JSON_DIR = './cloudflare-json-export';
const SQL_FILE = 'ekehi-network-db.sql';

// Table names in your Cloudflare D1 database
const TABLES = [
  'users',
  'user_profiles', 
  'mining_sessions',
  'social_tasks',
  'user_social_tasks',
  'achievements',
  'user_achievements',
  'presale_purchases',
  'ad_views'
];

// Create output directory if it doesn't exist
if (!fs.existsSync(OUTPUT_JSON_DIR)) {
  fs.mkdirSync(OUTPUT_JSON_DIR, { recursive: true });
}

// Create cloudflare-data directory if it doesn't exist
if (!fs.existsSync(CLOUDFLARE_DATA_DIR)) {
  fs.mkdirSync(CLOUDFLARE_DATA_DIR, { recursive: true });
}

// Function to parse INSERT statements from SQL
function parseInsertStatements(sqlContent, tableName) {
  const records = [];
  const lines = sqlContent.split('\n');
  
  for (const line of lines) {
    // Match INSERT INTO statements for the specific table
    const insertRegex = new RegExp(`INSERT INTO \`?${tableName}\`?\s*\(([^)]+)\)\s*VALUES\s*\(([^)]+)\)`, 'i');
    const match = line.match(insertRegex);
    
    if (match) {
      const columns = match[1].split(',').map(col => col.trim().replace(/[`'"]/g, ''));
      const values = match[2].split(',').map(val => {
        val = val.trim();
        // Remove quotes and handle NULL values
        if (val === 'NULL' || val === 'null') {
          return null;
        }
        // Remove surrounding quotes
        if ((val.startsWith("'") && val.endsWith("'")) || 
            (val.startsWith('"') && val.endsWith('"'))) {
          return val.slice(1, -1);
        }
        // Try to parse as number
        if (!isNaN(val) && !isNaN(parseFloat(val))) {
          return parseFloat(val);
        }
        return val;
      });
      
      // Create record object
      const record = {};
      columns.forEach((col, index) => {
        record[col] = values[index];
      });
      
      records.push(record);
    }
  }
  
  return records;
}

// Main export function
async function exportCloudflareData() {
  console.log('🚀 Starting Cloudflare D1 SQL to JSON export...');
  
  // Check if SQL file exists
  const sqlFile = path.join(CLOUDFLARE_DATA_DIR, SQL_FILE);
  
  if (!fs.existsSync(sqlFile)) {
    console.log('❌ SQL file not found!');
    console.log('Please run the following command first:');
    console.log('pnpm run wrangler-export');
    console.log('');
    console.log('This will create:', sqlFile);
    return;
  }
  
  console.log(`📂 Found SQL file: ${sqlFile}`);
  
  // Read SQL content
  const sqlContent = fs.readFileSync(sqlFile, 'utf8');
  console.log(`📄 SQL file size: ${(sqlContent.length / 1024).toFixed(2)} KB`);
  
  // Export each table to JSON
  let totalRecords = 0;
  
  for (const tableName of TABLES) {
    try {
      console.log(`\n🔍 Processing table: ${tableName}`);
      const data = parseInsertStatements(sqlContent, tableName);
      const outputFile = path.join(OUTPUT_JSON_DIR, `${tableName}.json`);
      
      fs.writeFileSync(outputFile, JSON.stringify(data, null, 2));
      console.log(`✅ Exported ${data.length} records from ${tableName}`);
      
      totalRecords += data.length;
    } catch (error) {
      console.error(`❌ Failed to export ${tableName}:`, error.message);
    }
  }
  
  console.log('\n' + '='.repeat(50));
  console.log('✅ Export completed!');
  console.log(`📊 Total records exported: ${totalRecords}`);
  console.log(`📁 JSON files saved to: ${OUTPUT_JSON_DIR}`);
  console.log('\n🚀 Next step: Run "pnpm run migrate-data" to import to Appwrite');
}

// Execute the export
exportCloudflareData();
