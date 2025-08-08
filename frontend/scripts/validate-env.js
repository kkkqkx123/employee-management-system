#!/usr/bin/env node

/**
 * Environment validation script
 * Validates that all required environment variables are set for the target environment
 */

const fs = require('fs');
const path = require('path');

const REQUIRED_VARS = {
  development: [
    'VITE_API_BASE_URL',
    'VITE_WS_URL',
    'VITE_APP_NAME'
  ],
  staging: [
    'VITE_API_BASE_URL',
    'VITE_WS_URL',
    'VITE_APP_NAME',
    'VITE_APP_VERSION',
    'VITE_APP_ENVIRONMENT'
  ],
  production: [
    'VITE_API_BASE_URL',
    'VITE_WS_URL',
    'VITE_APP_NAME',
    'VITE_APP_VERSION',
    'VITE_APP_ENVIRONMENT',
    'VITE_SENTRY_DSN'
  ]
};

function loadEnvFile(envFile) {
  if (!fs.existsSync(envFile)) {
    return {};
  }
  
  const content = fs.readFileSync(envFile, 'utf8');
  const env = {};
  
  content.split('\n').forEach(line => {
    const trimmed = line.trim();
    if (trimmed && !trimmed.startsWith('#')) {
      const [key, ...valueParts] = trimmed.split('=');
      if (key && valueParts.length > 0) {
        env[key.trim()] = valueParts.join('=').trim();
      }
    }
  });
  
  return env;
}

function validateEnvironment(environment) {
  console.log(`🔍 Validating ${environment} environment...`);
  
  const envFile = path.join(__dirname, `../.env.${environment}`);
  const env = loadEnvFile(envFile);
  const requiredVars = REQUIRED_VARS[environment] || [];
  
  const missing = [];
  const empty = [];
  
  requiredVars.forEach(varName => {
    if (!(varName in env)) {
      missing.push(varName);
    } else if (!env[varName] || env[varName].trim() === '') {
      empty.push(varName);
    }
  });
  
  if (missing.length > 0) {
    console.error(`❌ Missing required environment variables:`);
    missing.forEach(varName => console.error(`   - ${varName}`));
  }
  
  if (empty.length > 0) {
    console.error(`⚠️  Empty environment variables:`);
    empty.forEach(varName => console.error(`   - ${varName}`));
  }
  
  if (missing.length === 0 && empty.length === 0) {
    console.log(`✅ All required environment variables are set for ${environment}`);
    return true;
  }
  
  return false;
}

function main() {
  const environment = process.argv[2] || process.env.NODE_ENV || 'development';
  
  console.log(`Environment validation for: ${environment}`);
  console.log('='.repeat(50));
  
  const isValid = validateEnvironment(environment);
  
  if (!isValid) {
    console.error(`\n❌ Environment validation failed for ${environment}`);
    process.exit(1);
  }
  
  console.log(`\n✅ Environment validation passed for ${environment}`);
}

if (require.main === module) {
  main();
}

module.exports = { validateEnvironment, loadEnvFile };