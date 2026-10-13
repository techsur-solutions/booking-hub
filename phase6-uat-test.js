/**
 * Phase 6 UAT Automated Tests
 * Tests notifications-service and audit-log-service features
 */

const http = require('http');
const https = require('https');

const BASE_URL = 'http://127.0.0.1:3000';
const GATEWAY_URL = 'http://127.0.0.1:3000';

// Test results tracker
const results = {
  passed: [],
  failed: [],
  skipped: []
};

// Helper: Make HTTP request
function request(options, body = null) {
  return new Promise((resolve, reject) => {
    const client = options.protocol === 'https:' ? https : http;
    const req = client.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        resolve({
          statusCode: res.statusCode,
          headers: res.headers,
          body: data,
          json: () => {
            try { return JSON.parse(data); } catch(e) { return null; }
          }
        });
      });
    });
    req.on('error', reject);
    if (body) req.write(typeof body === 'string' ? body : JSON.stringify(body));
    req.end();
  });
}

// Helper: Get admin JWT token from Keycloak
async function getAdminToken() {
  try {
    const params = new URLSearchParams({
      grant_type: 'password',
      client_id: 'bookinghub-client',
      username: 'admin',
      password: 'admin123',
      scope: 'openid profile email'
    });

    const res = await request({
      hostname: '127.0.0.1',
      port: 8081,
      path: '/realms/bookinghub/protocol/openid-connect/token',
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Content-Length': Buffer.byteLength(params.toString())
      }
    }, params.toString());

    if (res.statusCode === 200) {
      const data = res.json();
      return data.access_token;
    }
    console.log('Token request failed:', res.statusCode, res.body);
    return null;
  } catch (err) {
    console.log('Token error:', err.message);
    return null;
  }
}

// Test 1: Check notifications-service health
async function test1_NotificationsServiceHealth() {
  console.log('\n[Test 1] Notifications Service Health...');
  try {
    const res = await request({
      hostname: '127.0.0.1',
      port: 3000,
      path: '/api/notifications/actuator/health',
      method: 'GET'
    });
    
    if (res.statusCode === 200 || res.statusCode === 401 || res.statusCode === 403) {
      results.passed.push('Test 1: Notifications service is reachable');
      console.log('✓ PASS - Service reachable (status: ' + res.statusCode + ')');
      return true;
    } else {
      results.failed.push('Test 1: Unexpected status ' + res.statusCode);
      console.log('✗ FAIL - Unexpected status:', res.statusCode);
      return false;
    }
  } catch (err) {
    results.failed.push('Test 1: ' + err.message);
    console.log('✗ FAIL -', err.message);
    return false;
  }
}

// Test 2: Check audit-log-service health
async function test2_AuditLogServiceHealth() {
  console.log('\n[Test 2] Audit Log Service Health...');
  try {
    const res = await request({
      hostname: '127.0.0.1',
      port: 3000,
      path: '/api/audit-log/actuator/health',
      method: 'GET'
    });
    
    if (res.statusCode === 200 || res.statusCode === 401 || res.statusCode === 403) {
      results.passed.push('Test 2: Audit log service is reachable');
      console.log('✓ PASS - Service reachable (status: ' + res.statusCode + ')');
      return true;
    } else {
      results.failed.push('Test 2: Unexpected status ' + res.statusCode);
      console.log('✗ FAIL - Unexpected status:', res.statusCode);
      return false;
    }
  } catch (err) {
    results.failed.push('Test 2: ' + err.message);
    console.log('✗ FAIL -', err.message);
    return false;
  }
}

// Test 3: Check RabbitMQ is running and has the required queues
async function test3_RabbitMQQueues() {
  console.log('\n[Test 3] RabbitMQ Queue Setup...');
  try {
    // Check if RabbitMQ management API is accessible
    const res = await request({
      hostname: '127.0.0.1',
      port: 15672,
      path: '/api/queues',
      method: 'GET',
      headers: {
        'Authorization': 'Basic ' + Buffer.from('guest:guest').toString('base64')
      }
    });
    
    if (res.statusCode === 200) {
      const queues = res.json();
      const requiredQueues = [
        'notifications.q',
        'audit.booking.q',
        'audit.location.q',
        'audit.resource.q',
        'audit.customfield.q',
        'audit.user.q',
        'audit.permission.q',
        'audit.settings.q'
      ];
      
      const foundQueues = queues.map(q => q.name);
      const missing = requiredQueues.filter(q => !foundQueues.includes(q));
      
      if (missing.length === 0) {
        results.passed.push('Test 3: All required RabbitMQ queues exist');
        console.log('✓ PASS - All 8 required queues found');
        return true;
      } else {
        results.failed.push('Test 3: Missing queues: ' + missing.join(', '));
        console.log('✗ FAIL - Missing queues:', missing.join(', '));
        return false;
      }
    } else {
      results.failed.push('Test 3: Cannot access RabbitMQ API (status: ' + res.statusCode + ')');
      console.log('✗ FAIL - Cannot access RabbitMQ API');
      return false;
    }
  } catch (err) {
    results.skipped.push('Test 3: RabbitMQ API not accessible - ' + err.message);
    console.log('⊘ SKIP -', err.message);
    return null;
  }
}

// Test 4: Verify audit log endpoint requires authentication
async function test4_AuditLogAuth() {
  console.log('\n[Test 4] Audit Log Authentication...');
  try {
    const res = await request({
      hostname: '127.0.0.1',
      port: 3000,
      path: '/api/audit-log',
      method: 'GET'
    });
    
    if (res.statusCode === 401 || res.statusCode === 403) {
      results.passed.push('Test 4: Audit log endpoint requires authentication');
      console.log('✓ PASS - Endpoint protected (status: ' + res.statusCode + ')');
      return true;
    } else {
      results.failed.push('Test 4: Endpoint not protected (status: ' + res.statusCode + ')');
      console.log('✗ FAIL - Expected 401/403, got:', res.statusCode);
      return false;
    }
  } catch (err) {
    results.failed.push('Test 4: ' + err.message);
    console.log('✗ FAIL -', err.message);
    return false;
  }
}

// Test 5: Verify notification endpoints require authentication
async function test5_NotificationAuth() {
  console.log('\n[Test 5] Notification Endpoints Authentication...');
  try {
    const endpoints = ['/api/notifications/delivery-status', '/api/notifications/dead-letter'];
    let allProtected = true;
    
    for (const path of endpoints) {
      const res = await request({
        hostname: '127.0.0.1',
        port: 3000,
        path: path,
        method: 'GET'
      });
      
      if (res.statusCode !== 401 && res.statusCode !== 403) {
        allProtected = false;
        console.log('  ✗', path, '- Not protected (status:', res.statusCode + ')');
      } else {
        console.log('  ✓', path, '- Protected (status:', res.statusCode + ')');
      }
    }
    
    if (allProtected) {
      results.passed.push('Test 5: All notification endpoints require authentication');
      console.log('✓ PASS - All endpoints protected');
      return true;
    } else {
      results.failed.push('Test 5: Some notification endpoints not protected');
      console.log('✗ FAIL - Some endpoints not protected');
      return false;
    }
  } catch (err) {
    results.failed.push('Test 5: ' + err.message);
    console.log('✗ FAIL -', err.message);
    return false;
  }
}

// Test 6: Check database has notification_deliveries table
async function test6_DatabaseSchema() {
  console.log('\n[Test 6] Database Schema Check...');
  try {
    const { execSync } = require('child_process');
    
    // Check notifications_deliveries table
    const notifCheck = execSync(
      'docker compose exec -T postgres psql -U bookinghub -d notifications_db -c "\\dt notification_deliveries" 2>&1',
      { encoding: 'utf8' }
    );
    
    // Check audit_log_entries table with idempotency_key
    const auditCheck = execSync(
      'docker compose exec -T postgres psql -U bookinghub -d audit_db -c "\\d audit_log_entries" 2>&1',
      { encoding: 'utf8' }
    );
    
    const hasNotifTable = notifCheck.includes('notification_deliveries');
    const hasAuditTable = auditCheck.includes('audit_log_entries');
    const hasIdempotencyKey = auditCheck.includes('idempotency_key');
    
    if (hasNotifTable && hasAuditTable && hasIdempotencyKey) {
      results.passed.push('Test 6: Database schema correct (notification_deliveries and audit_log_entries with idempotency_key)');
      console.log('✓ PASS - All required tables and columns exist');
      return true;
    } else {
      const issues = [];
      if (!hasNotifTable) issues.push('notification_deliveries missing');
      if (!hasAuditTable) issues.push('audit_log_entries missing');
      if (!hasIdempotencyKey) issues.push('idempotency_key column missing');
      results.failed.push('Test 6: Schema issues - ' + issues.join(', '));
      console.log('✗ FAIL - Schema issues:', issues.join(', '));
      return false;
    }
  } catch (err) {
    results.skipped.push('Test 6: Cannot check database schema - ' + err.message);
    console.log('⊘ SKIP -', err.message);
    return null;
  }
}

// Test 7: Verify audit log immutability (no PUT/DELETE/POST methods)
async function test7_AuditLogImmutability() {
  console.log('\n[Test 7] Audit Log Immutability...');
  try {
    const methods = ['POST', 'PUT', 'DELETE'];
    let allBlocked = true;
    
    for (const method of methods) {
      const res = await request({
        hostname: '127.0.0.1',
        port: 3000,
        path: '/api/audit-log/00000000-0000-0000-0000-000000000001',
        method: method,
        headers: {
          'Content-Type': 'application/json'
        }
      }, JSON.stringify({}));
      
      // 404 or 405 means method not allowed/not found, which is correct
      // 401/403 means auth required, but method exists (not ideal for immutability)
      if (res.statusCode === 404 || res.statusCode === 405) {
        console.log('  ✓', method, '- Blocked (status:', res.statusCode + ')');
      } else {
        allBlocked = false;
        console.log('  ✗', method, '- Not blocked (status:', res.statusCode + ')');
      }
    }
    
    if (allBlocked) {
      results.passed.push('Test 7: Audit log is immutable (no mutating methods)');
      console.log('✓ PASS - All mutating methods blocked');
      return true;
    } else {
      results.failed.push('Test 7: Audit log allows mutating operations');
      console.log('✗ FAIL - Some mutating methods not blocked');
      return false;
    }
  } catch (err) {
    results.failed.push('Test 7: ' + err.message);
    console.log('✗ FAIL -', err.message);
    return false;
  }
}

// Test 8: Check service logs for successful startup
async function test8_ServiceLogs() {
  console.log('\n[Test 8] Service Startup Logs...');
  try {
    const { execSync } = require('child_process');
    
    const notifLogs = execSync(
      'docker compose logs --tail 50 notifications-service 2>&1 | grep -i "Started\\|Netty\\|error\\|exception" || true',
      { encoding: 'utf8' }
    );
    
    const auditLogs = execSync(
      'docker compose logs --tail 50 audit-log-service 2>&1 | grep -i "Started\\|Netty\\|error\\|exception" || true',
      { encoding: 'utf8' }
    );
    
    const notifStarted = notifLogs.toLowerCase().includes('started') && !notifLogs.toLowerCase().includes('error');
    const auditStarted = auditLogs.toLowerCase().includes('started') && !auditLogs.toLowerCase().includes('error');
    
    if (notifStarted && auditStarted) {
      results.passed.push('Test 8: Both services started successfully without errors');
      console.log('✓ PASS - Both services started cleanly');
      return true;
    } else {
      const issues = [];
      if (!notifStarted) issues.push('notifications-service startup issue');
      if (!auditStarted) issues.push('audit-log-service startup issue');
      results.failed.push('Test 8: ' + issues.join(', '));
      console.log('✗ FAIL -', issues.join(', '));
      return false;
    }
  } catch (err) {
    results.skipped.push('Test 8: Cannot check logs - ' + err.message);
    console.log('⊘ SKIP -', err.message);
    return null;
  }
}

// Main test runner
async function runAllTests() {
  console.log('='.repeat(60));
  console.log('Phase 6 UAT Automated Test Suite');
  console.log('Testing: Notifications & Audit Logging');
  console.log('='.repeat(60));
  
  await test1_NotificationsServiceHealth();
  await test2_AuditLogServiceHealth();
  await test3_RabbitMQQueues();
  await test4_AuditLogAuth();
  await test5_NotificationAuth();
  await test6_DatabaseSchema();
  await test7_AuditLogImmutability();
  await test8_ServiceLogs();
  
  console.log('\n' + '='.repeat(60));
  console.log('Test Results Summary');
  console.log('='.repeat(60));
  console.log('✓ PASSED:', results.passed.length);
  results.passed.forEach(r => console.log('  -', r));
  console.log('\n✗ FAILED:', results.failed.length);
  results.failed.forEach(r => console.log('  -', r));
  console.log('\n⊘ SKIPPED:', results.skipped.length);
  results.skipped.forEach(r => console.log('  -', r));
  
  const total = results.passed.length + results.failed.length + results.skipped.length;
  const passRate = total > 0 ? Math.round((results.passed.length / total) * 100) : 0;
  console.log('\nPass Rate:', passRate + '%', '(' + results.passed.length + '/' + total + ')');
  console.log('='.repeat(60));
  
  process.exit(results.failed.length > 0 ? 1 : 0);
}

runAllTests().catch(err => {
  console.error('Test suite error:', err);
  process.exit(1);
});
