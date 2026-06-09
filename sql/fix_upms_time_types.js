const fs = require('fs');
const path = require('path');

function walk(dir, fn) {
  if (!fs.existsSync(dir)) return;
  fs.readdirSync(dir).forEach(f => {
    const p = path.join(dir, f);
    const s = fs.statSync(p);
    if (s.isDirectory()) walk(p, fn);
    else if (f.endsWith('.java')) fn(p);
  });
}

const apiDir = 'D:/workspace/lending_pro/lending-framework/pig-upms/pig-upms-api/src/main/java/com/pig4cloud/pig/admin/api';
const bizDir = 'D:/workspace/lending_pro/lending-framework/pig-upms/pig-upms-biz/src/main/java/com/pig4cloud/pig/admin';

// 1. Entity: LocalDateTime -> Instant
walk(apiDir + '/entity', p => {
  let c = fs.readFileSync(p, 'utf8');
  if (c.includes('LocalDateTime')) {
    c = c.replace(/import java\.time\.LocalDateTime;/g, 'import java.time.Instant;');
    c = c.replace(/LocalDateTime/g, 'Instant');
    fs.writeFileSync(p, c, 'utf8');
    console.log('Entity:', path.basename(p));
  }
});

// 2. DTO: LocalDateTime -> Instant
walk(apiDir + '/dto', p => {
  let c = fs.readFileSync(p, 'utf8');
  if (c.includes('LocalDateTime')) {
    c = c.replace(/import java\.time\.LocalDateTime;/g, 'import java.time.Instant;');
    c = c.replace(/LocalDateTime/g, 'Instant');
    fs.writeFileSync(p, c, 'utf8');
    console.log('DTO:', path.basename(p));
  }
});

// 3. VO: LocalDateTime -> String, remove import
walk(apiDir + '/vo', p => {
  let c = fs.readFileSync(p, 'utf8');
  if (c.includes('LocalDateTime')) {
    c = c.replace(/import java\.time\.LocalDateTime;\r?\n/g, '');
    c = c.replace(/LocalDateTime/g, 'String');
    fs.writeFileSync(p, c, 'utf8');
    console.log('VO:', path.basename(p));
  }
});

// 4. Biz (controller, service, etc): LocalDateTime.now() -> Instant.now()
walk(bizDir, p => {
  let c = fs.readFileSync(p, 'utf8');
  if (c.includes('LocalDateTime')) {
    c = c.replace(/import java\.time\.LocalDateTime;/g, 'import java.time.Instant;');
    c = c.replace(/LocalDateTime\.now\(\)/g, 'Instant.now()');
    c = c.replace(/LocalDateTime/g, 'Instant');
    fs.writeFileSync(p, c, 'utf8');
    console.log('Biz:', path.basename(p));
  }
});

console.log('All pig-upms done');