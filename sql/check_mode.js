const mysql = require('mysql2/promise');
(async () => {
  const c = await mysql.createConnection({host:'127.0.0.1',port:3306,user:'root',password:'001Sea001!'});
  const [r] = await c.query("SHOW VARIABLES LIKE 'sql_mode'");
  console.log(r);
  await c.end();
})();