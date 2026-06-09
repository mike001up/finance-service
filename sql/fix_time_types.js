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

const apiDir = 'D:/workspace/lending_pro/finance-service/finance-api/src/main/java/com/ehome/mal/finance/api';
const bizDir = 'D:/workspace/lending_pro/finance-service/finance-biz/src/main/java/com/ehome/mal/finance';

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

// 4. Service impl + other biz: LocalDateTime.now() -> Instant.now()
walk(bizDir + '/service', p => {
  let c = fs.readFileSync(p, 'utf8');
  if (c.includes('LocalDateTime')) {
    c = c.replace(/import java\.time\.LocalDateTime;/g, 'import java.time.Instant;');
    c = c.replace(/LocalDateTime\.now\(\)/g, 'Instant.now()');
    c = c.replace(/LocalDateTime/g, 'Instant');
    fs.writeFileSync(p, c, 'utf8');
    console.log('Service:', path.basename(p));
  }
});

// 5. Event
walk(bizDir + '/event', p => {
  let c = fs.readFileSync(p, 'utf8');
  if (c.includes('LocalDateTime')) {
    c = c.replace(/import java\.time\.LocalDateTime;/g, 'import java.time.Instant;');
    c = c.replace(/LocalDateTime\.now\(\)/g, 'Instant.now()');
    c = c.replace(/LocalDateTime/g, 'Instant');
    fs.writeFileSync(p, c, 'utf8');
    console.log('Event:', path.basename(p));
  }
});

// 6. Controller (in case any use LocalDateTime)
walk(bizDir + '/controller', p => {
  let c = fs.readFileSync(p, 'utf8');
  if (c.includes('LocalDateTime')) {
    c = c.replace(/import java\.time\.LocalDateTime;/g, 'import java.time.Instant;');
    c = c.replace(/LocalDateTime/g, 'Instant');
    fs.writeFileSync(p, c, 'utf8');
    console.log('Controller:', path.basename(p));
  }
});

// 7. Config
walk(bizDir + '/config', p => {
  let c = fs.readFileSync(p, 'utf8');
  if (c.includes('LocalDateTime')) {
    c = c.replace(/import java\.time\.LocalDateTime;/g, 'import java.time.Instant;');
    c = c.replace(/LocalDateTime/g, 'Instant');
    fs.writeFileSync(p, c, 'utf8');
    console.log('Config:', path.basename(p));
  }
});

console.log('All done');