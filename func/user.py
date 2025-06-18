import aiosqlite

_db_conn = None

async def getdb():
  global _db_conn
  if _db_conn is None:
    _db_conn = await aiosqlite.connect('db/user.db')
    await _db_conn.execute("PRAGMA journal_mode=WAL")
    await _db_conn.execute("PRAGMA synchronous=NORMAL")
    await _db_conn.execute("PRAGMA temp_store=MEMORY")
  try:
    await _db_conn.execute("SELECT 1")
  except aiosqlite.Error:
    _db_conn = await aiosqlite.connect('db/user.db')
    await _db_conn.execute("PRAGMA journal_mode=WAL")
    await _db_conn.execute("PRAGMA synchronous=NORMAL")
    await _db_conn.execute("PRAGMA temp_store=MEMORY")
  return _db_conn

async def userTable():
  conn = await getdb()
  c = await conn.cursor()
  await c.execute("""CREATE TABLE IF NOT EXISTS user(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    password TEXT NOT NULL,
    games INTEGER DEFAULT 0,
    bestscore INTEGER DEFAULT 0,
    lastscore INTEGER DEFAULT 0,
    lastplayed TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )""")
  await conn.commit()
  await conn.close()

async def addUser(username, password):
  conn = await getdb()
  async with conn.execute("SELECT * FROM user WHERE username = ?", (username,)) as c:
    if await c.fetchone() is not None:
      raise Exception("Username already exists")
    else:
      pass
  async with conn.execute("INSERT INTO user (username, password) VALUES (?, ?)", (username, password)) as c:
    pass
  await conn.commit()
  return True

async def loginUser(username, password):
  conn = await getdb()
  async with conn.execute("SELECT * FROM user WHERE username = ? AND password = ?", (username, password)) as c:
    user = await c.fetchone()
    if user is None:
      raise Exception("Invalid username or password")
    else:
      return True

async def addGame(username):
  conn = await getdb()
  async with conn.execute("UPDATE user SET games = games + 1 WHERE username = ?", (username,)):
    pass
  await conn.commit()

async def updateScore(username, score):
  conn = await getdb()
  async with conn.execute("UPDATE user SET lastscore = ?, lastplayed = CURRENT_TIMESTAMP WHERE username = ?", (score, username)):
    pass
  async with conn.execute("SELECT bestscore FROM user WHERE username = ?", (username,)) as c:
    bestscore = await c.fetchone()
    if bestscore[0] < score:
      await c.execute("UPDATE user SET bestscore = ? WHERE username = ?", (score, username))
  await conn.commit()

async def getUser(username):
  conn = await getdb()
  async with conn.execute("SELECT * FROM user WHERE username = ?", (username,)) as c:
    pass
  user = await c.fetchone()
  return user

async def getLeaderboard():
  conn = await getdb()
  async with conn.execute("SELECT username, bestscore FROM user ORDER BY bestscore DESC LIMIT 10") as c:
    users = await c.fetchall()
    return users