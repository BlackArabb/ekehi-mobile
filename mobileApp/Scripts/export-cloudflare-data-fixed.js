// Cloudflare D1 Data Export Script (Fixed Version)
// 
// This script helps export data from Cloudflare D1 database to JSON files
// for migration to Appwrite Database

const fs = require('fs');
const path = require('path');

// Configuration
const CLOUDFLARE_DATA_DIR = './cloudflare-data';
const OUTPUT_JSON_DIR = './cloudflare-json-export';
const SQL_FILE = 'ekehi-network-db.sql';

// Create output directory if it doesn't exist
if (!fs.existsSync(OUTPUT_JSON_DIR)) {
  fs.mkdirSync(OUTPUT_JSON_DIR, { recursive: true });
}

// Simple SQL parser for INSERT statements
function parseTableData(sqlContent, tableName) {
  const records = [];
  const lines = sqlContent.split('\n');
  
  // Find the CREATE TABLE statement to get column names
  const createTableRegex = new RegExp(`CREATE TABLE ${tableName}\\s*\\(([^;]+?)\\);`, 'is');
  const createMatch = sqlContent.match(createTableRegex);
  
  if (!createMatch) {
    console.log(`⚠️ Could not find CREATE TABLE statement for ${tableName}`);
    return records;
  }
  
  // Extract column names
  const createTableContent = createMatch[1];
  const columnLines = createTableContent.split(',');
  const columns = [];
  
  for (const colLine of columnLines) {
    const trimmed = colLine.trim();
    if (!trimmed.startsWith('FOREIGN KEY') && !trimmed.startsWith('UNIQUE') && 
        !trimmed.startsWith('PRIMARY KEY') && !trimmed.startsWith('INDEX')) {
      const colMatch = trimmed.match(/^(\\w+)/);
      if (colMatch) {
        columns.push(colMatch[1]);
      }
    }
  }
  
  console.log(`📋 Found columns for ${tableName}:`, columns);
  
  // Parse INSERT statements
  for (const line of lines) {
    if (line.trim().startsWith(`INSERT INTO ${tableName} VALUES`)) {
      const valuesMatch = line.match(/VALUES\\s*\\((.+)\\);?$/);
      if (valuesMatch) {
        const valuesStr = valuesMatch[1];
        
        // Simple CSV-like parsing for VALUES
        const values = [];
        let current = '';
        let inQuotes = false;
        let quoteChar = null;
        
        for (let i = 0; i < valuesStr.length; i++) {
          const char = valuesStr[i];
          
          if (!inQuotes && (char === "'" || char === '"')) {
            inQuotes = true;
            quoteChar = char;
          } else if (inQuotes && char === quoteChar) {
            inQuotes = false;
            quoteChar = null;
          } else if (!inQuotes && char === ',') {
            values.push(processValue(current.trim()));
            current = '';
          } else {
            current += char;
          }
        }
        
        // Add the last value
        if (current.trim()) {
          values.push(processValue(current.trim()));
        }
        
        // Create record object
        if (values.length === columns.length) {
          const record = {};
          columns.forEach((col, index) => {
            record[col] = values[index];
          });
          records.push(record);
        } else {
          console.log(`⚠️ Column count mismatch for ${tableName}: expected ${columns.length}, got ${values.length}`);
        }
      }
    }
  }
  
  return records;
}

// Process individual values
function processValue(val) {
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
}

// Main export function
async function exportCloudflareData() {
  console.log('🚀 Starting Cloudflare D1 SQL to JSON export...');
  
  const sqlFile = path.join(CLOUDFLARE_DATA_DIR, SQL_FILE);
  
  if (!fs.existsSync(sqlFile)) {
    console.log('❌ SQL file not found!');
    console.log('Please run: pnpm run wrangler-export');
    return;
  }
  
  console.log(`📂 Found SQL file: ${sqlFile}`);
  const sqlContent = fs.readFileSync(sqlFile, 'utf8');
  
  // Tables to export
  const tables = [
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
  
  let totalRecords = 0;
  
  for (const tableName of tables) {
    try {
      console.log(`\\n🔍 Processing table: ${tableName}`);
      const data = parseTableData(sqlContent, tableName);
      const outputFile = path.join(OUTPUT_JSON_DIR, `${tableName}.json`);
      
      fs.writeFileSync(outputFile, JSON.stringify(data, null, 2));
      console.log(`✅ Exported ${data.length} records from ${tableName}`);
      
      totalRecords += data.length;
    } catch (error) {
      console.error(`❌ Failed to export ${tableName}:`, error.message);
    }
  }
  
  console.log('\\n' + '='.repeat(50));
  console.log('✅ Export completed!');
  console.log(`📊 Total records exported: ${totalRecords}`);
  console.log(`📁 JSON files saved to: ${OUTPUT_JSON_DIR}`);
  console.log('\\n🚀 Next step: Run "pnpm run migrate-data" to import to Appwrite');
}

// Execute the export
exportCloudflareData().catch(console.error);