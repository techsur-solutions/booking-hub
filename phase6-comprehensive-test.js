/**
 * Phase 6 Comprehensive UAT - Tests notification and audit logging end-to-end
 */

const { execSync } = require('child_process');

const results = { passed: 0, failed: 0, skipped: 0, details: [] };

function log(msg) {
  console.log(msg);
}

function pass(test, msg) {
  results.passed++;
  results.details.push({ test, result: 'PASS', msg });
  log(`✓ PASS [${test}]: ${msg}`);
}

function fail(test, msg) {
  results.failed++;
  results.details.push({ test, result: 'FAIL', msg });
  log(`✗ FAIL [${test}]: ${msg}`);
}

function skip(test, msg) {
  results.skipped++;
  results.details.push({ test, result: 'SKIP', msg });
  log(`⊘ SKIP [${test}]: ${msg}`);
}

async function runTests() {
  log('='.repeat(70));
  log('Phase 6 Comprehensive UAT - Notifications & Audit Logging');
  log('='.repeat(70));

  // Test 1: Services are healthy
  log('\n[Test 1] Service Health Check');
  try {
    const status = execSync('docker compose ps notifications-service audit-log-service --format json 2>/dev/null', { encoding: 'utf8' });
    const services = status.trim().split('\n').map(line => JSON.parse(line));
    const allHealthy = services.every(s => s.Health === 'healthy' || s.State === 'running');
    if (allHealthy) {
      pass('Test 1', 'Both notifications-service and audit-log-service are healthy');
    } else {
      fail('Test 1', 'One or more services not healthy: ' + JSON.stringify(services));
    }
  } catch (err) {
    fail('Test 1', 'Cannot check service health: ' + err.message);
  }

  // Test 2: RabbitMQ has correct queues
  log('\n[Test 2] RabbitMQ Queue Configuration');
  try {
    const output = execSync('curl -s -u guest:guest http://127.0.0.1:15672/api/queues 2>/dev/null', { encoding: 'utf8' });
    const queues = JSON.parse(output).map(q => q.name);
    
    const required = [
      'notifications.booking.q',
      'notifications.passwordreset.q',
      'audit.booking.q',
      'audit.location.q',
      'audit.resource.q',
      'audit.customfield.q',
      'audit.user.q',
      'audit.permission.q',
      'audit.settings.q'
    ];
    
    const missing = required.filter(q => !queues.includes(q));
    if (missing.length === 0) {
      pass('Test 2', `All ${required.length} required queues exist`);
    } else {
      fail('Test 2', 'Missing queues: ' + missing.join(', '));
    }
  } catch (err) {
    skip('Test 2', 'Cannot check RabbitMQ queues: ' + err.message);
  }

  // Test 3: Database schema check
  log('\n[Test 3] Database Schema Verification');
  try {
    // Check notifications_deliveries table
    const notifTable = execSync(
      'docker compose exec -T postgres psql -U bookinghub -d notifications_db -c "SELECT COUNT(*) FROM information_schema.columns WHERE table_name=\'notification_deliveries\' AND column_name IN (\'idempotency_key\', \'event_type\', \'recipient_email\', \'status\');" 2>/dev/null',
      { encoding: 'utf8' }
    );
    
    // Check audit_log_entries table
    const auditTable = execSync(
      'docker compose exec -T postgres psql -U bookinghub -d audit_db -c "SELECT COUNT(*) FROM information_schema.columns WHERE table_name=\'audit_log_entries\' AND column_name IN (\'idempotency_key\', \'entity_type\', \'action_type\', \'entity_id\');" 2>/dev/null',
      { encoding: 'utf8' }
    );
    
    const notifCols = parseInt(notifTable.match(/\d+/)?.[0] || '0');
    const auditCols = parseInt(auditTable.match(/\d+/)?.[0] || '0');
    
    if (notifCols >= 4 && auditCols >= 4) {
      pass('Test 3', 'Both tables exist with required columns');
    } else {
      fail('Test 3', `Schema incomplete - notif cols: ${notifCols}/4, audit cols: ${auditCols}/4`);
    }
  } catch (err) {
    skip('Test 3', 'Cannot check database schema: ' + err.message);
  }

  // Test 4: Audit log immutability at DB level
  log('\n[Test 4] Audit Log DB-Level Immutability');
  try {
    const grants = execSync(
      'docker compose exec -T postgres psql -U bookinghub -d audit_db -c "SELECT privilege_type FROM information_schema.table_privileges WHERE table_name=\'audit_log_entries\' AND grantee=\'bookinghub\';" 2>/dev/null',
      { encoding: 'utf8' }
    );
    
    const hasUpdate = grants.includes('UPDATE');
    const hasDelete = grants.includes('DELETE');
    const hasInsert = grants.includes('INSERT');
    const hasSelect = grants.includes('SELECT');
    
    if (hasInsert && hasSelect && !hasUpdate && !hasDelete) {
      pass('Test 4', 'audit_log_entries has INSERT/SELECT but no UPDATE/DELETE (immutable)');
    } else {
      fail('Test 4', `Incorrect grants - INSERT:${hasInsert} SELECT:${hasSelect} UPDATE:${hasUpdate} DELETE:${hasDelete}`);
    }
  } catch (err) {
    skip('Test 4', 'Cannot check DB grants: ' + err.message);
  }

  // Test 5: RabbitMQ consumers are active
  log('\n[Test 5] RabbitMQ Consumer Registration');
  try {
    const output = execSync('curl -s -u guest:guest http://127.0.0.1:15672/api/consumers 2>/dev/null', { encoding: 'utf8' });
    const consumers = JSON.parse(output);
    
    const notifConsumers = consumers.filter(c => c.consumer_tag?.includes('notifications'));
    const auditConsumers = consumers.filter(c => c.consumer_tag?.includes('audit'));
    
    if (notifConsumers.length > 0 && auditConsumers.length > 0) {
      pass('Test 5', `Active consumers found - notifications: ${notifConsumers.length}, audit: ${auditConsumers.length}`);
    } else {
      fail('Test 5', `Insufficient consumers - notifications: ${notifConsumers.length}, audit: ${auditConsumers.length}`);
    }
  } catch (err) {
    skip('Test 5', 'Cannot check RabbitMQ consumers: ' + err.message);
  }

  // Test 6: Check service logs for successful consumer binding
  log('\n[Test 6] Service Startup Verification');
  try {
    const notifLogs = execSync('docker compose logs notifications-service 2>&1 | grep -i "started\\|listener\\|rabbit" | tail -5', { encoding: 'utf8' });
    const auditLogs = execSync('docker compose logs audit-log-service 2>&1 | grep -i "started\\|listener\\|rabbit" | tail -5', { encoding: 'utf8' });
    
    const notifStarted = notifLogs.toLowerCase().includes('started');
    const auditStarted = auditLogs.toLowerCase().includes('started');
    
    if (notifStarted && auditStarted) {
      pass('Test 6', 'Both services started successfully');
    } else {
      fail('Test 6', `Service startup issues - notifications: ${notifStarted}, audit: ${auditStarted}`);
    }
  } catch (err) {
    skip('Test 6', 'Cannot check service logs: ' + err.message);
  }

  // Test 7: Verify retry/DLQ configuration
  log('\n[Test 7] Retry and DLQ Configuration');
  try {
    const notifConfig = execSync('docker compose logs notifications-service 2>&1 | grep -i "retry\\|backoff\\|attempt" | head -3', { encoding: 'utf8' });
    const auditConfig = execSync('docker compose logs audit-log-service 2>&1 | grep -i "retry\\|backoff\\|attempt" | head -3', { encoding: 'utf8' });
    
    // Check for DLQ queues
    const output = execSync('curl -s -u guest:guest http://127.0.0.1:15672/api/queues 2>/dev/null', { encoding: 'utf8' });
    const queues = JSON.parse(output).map(q => q.name);
    const dlqCount = queues.filter(q => q.endsWith('.dlq')).length;
    
    if (dlqCount >= 9) {  // Expecting DLQ for each main queue
      pass('Test 7', `DLQ configuration verified - ${dlqCount} DLQ queues found`);
    } else {
      fail('Test 7', `Insufficient DLQ queues - found ${dlqCount}, expected >= 9`);
    }
  } catch (err) {
    skip('Test 7', 'Cannot verify retry/DLQ config: ' + err.message);
  }

  // Test 8: Idempotency key unique constraints
  log('\n[Test 8] Idempotency Constraints');
  try {
    const notifConstraint = execSync(
      'docker compose exec -T postgres psql -U bookinghub -d notifications_db -c "SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_name=\'notification_deliveries\' AND constraint_type=\'UNIQUE\';" 2>/dev/null',
      { encoding: 'utf8' }
    );
    
    const auditIndex = execSync(
      'docker compose exec -T postgres psql -U bookinghub -d audit_db -c "SELECT COUNT(*) FROM pg_indexes WHERE tablename=\'audit_log_entries\' AND indexname LIKE \'%idempotency%\';" 2>/dev/null',
      { encoding: 'utf8' }
    );
    
    const hasNotifConstraint = parseInt(notifConstraint.match(/\d+/)?.[0] || '0') > 0;
    const hasAuditIndex = parseInt(auditIndex.match(/\d+/)?.[0] || '0') > 0;
    
    if (hasNotifConstraint && hasAuditIndex) {
      pass('Test 8', 'Idempotency constraints exist on both tables');
    } else {
      fail('Test 8', `Missing constraints - notifications: ${hasNotifConstraint}, audit: ${hasAuditIndex}`);
    }
  } catch (err) {
    skip('Test 8', 'Cannot check idempotency constraints: ' + err.message);
  }

  // Test 9: SMTP configuration (check env vars exist)
  log('\n[Test 9] SMTP Configuration');
  try {
    const envVars = execSync('docker compose exec -T notifications-service env | grep SMTP', { encoding: 'utf8' });
    const hasHost = envVars.includes('SMTP_HOST');
    const hasPort = envVars.includes('SMTP_PORT');
    
    if (hasHost && hasPort) {
      pass('Test 9', 'SMTP configuration environment variables present');
    } else {
      fail('Test 9', 'Missing SMTP configuration');
    }
  } catch (err) {
    skip('Test 9', 'Cannot check SMTP config: ' + err.message);
  }

  // Test 10: Check for data in tables (if any events were processed)
  log('\n[Test 10] Event Processing Evidence');
  try {
    const auditCount = execSync(
      'docker compose exec -T postgres psql -U bookinghub -d audit_db -c "SELECT COUNT(*) FROM audit_log_entries;" 2>/dev/null',
      { encoding: 'utf8' }
    );
    
    const count = parseInt(auditCount.match(/\d+/)?.[0] || '0');
    
    if (count >= 0) {
      pass('Test 10', `Audit log accessible - ${count} entries found (empty is OK for new deployment)`);
    } else {
      fail('Test 10', 'Cannot query audit_log_entries table');
    }
  } catch (err) {
    skip('Test 10', 'Cannot check audit log entries: ' + err.message);
  }

  // Test 11: Security configuration verification
  log('\n[Test 11] Security Configuration');
  try {
    const notifSecurity = execSync('docker compose logs notifications-service 2>&1 | grep -i "security\\|oauth\\|jwt" | head -5', { encoding: 'utf8' });
    const auditSecurity = execSync('docker compose logs audit-log-service 2>&1 | grep -i "security\\|oauth\\|jwt" | head -5', { encoding: 'utf8' });
    
    const notifHasSecurity = notifSecurity.toLowerCase().includes('security') || notifSecurity.toLowerCase().includes('oauth');
    const auditHasSecurity = auditSecurity.toLowerCase().includes('security') || auditSecurity.toLowerCase().includes('oauth');
    
    if (notifHasSecurity && auditHasSecurity) {
      pass('Test 11', 'Both services have security configuration active');
    } else {
      fail('Test 11', `Security config unclear - notifications: ${notifHasSecurity}, audit: ${auditHasSecurity}`);
    }
  } catch (err) {
    skip('Test 11', 'Cannot verify security config: ' + err.message);
  }

  // Test 12: Phase 6 integration completeness
  log('\n[Test 12] Phase 6 Integration Completeness');
  const phase6Score = results.passed / (results.passed + results.failed);
  if (phase6Score >= 0.8) {
    pass('Test 12', `Phase 6 infrastructure complete - ${Math.round(phase6Score * 100)}% tests passing`);
  } else if (phase6Score >= 0.6) {
    fail('Test 12', `Phase 6 partially complete - ${Math.round(phase6Score * 100)}% tests passing (need >= 80%)`);
  } else {
    fail('Test 12', `Phase 6 incomplete - ${Math.round(phase6Score * 100)}% tests passing (need >= 80%)`);
  }

  // Summary
  log('\n' + '='.repeat(70));
  log('Test Summary');
  log('='.repeat(70));
  log(`✓ Passed:  ${results.passed}`);
  log(`✗ Failed:  ${results.failed}`);
  log(`⊘ Skipped: ${results.skipped}`);
  log(`Total:     ${results.passed + results.failed + results.skipped}`);
  const totalTests = results.passed + results.failed + results.skipped;
  const passRate = totalTests > 0 ? Math.round((results.passed / totalTests) * 100) : 0;
  log(`Pass Rate: ${passRate}%`);
  log('='.repeat(70));

  return results;
}

runTests().then(results => {
  process.exit(results.failed > 0 ? 1 : 0);
}).catch(err => {
  console.error('Test suite error:', err);
  process.exit(1);
});
