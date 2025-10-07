PRAGMA defer_foreign_keys=TRUE;
CREATE TABLE d1_migrations(
		id         INTEGER PRIMARY KEY AUTOINCREMENT,
		name       TEXT UNIQUE,
		applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
INSERT INTO d1_migrations VALUES(1,'0001_create_tables.sql','2025-09-07 21:54:56');
INSERT INTO d1_migrations VALUES(2,'0002_seed_data.sql','2025-09-07 21:54:57');
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    name TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TEXT
);
INSERT INTO users VALUES('111503823123987272638','alghareeb.mk@gmail.com','Muhammad Kamaludeen','2025-09-08 11:58:26','2025-09-11 16:21:13');
INSERT INTO users VALUES('100918368155980132753','ekehiofficial@gmail.com','Ekehi Official','2025-09-08 22:13:18','2025-09-11 16:22:17');
CREATE TABLE user_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT UNIQUE NOT NULL,
    username TEXT,
    total_coins REAL NOT NULL DEFAULT 0,
    coins_per_click INTEGER NOT NULL DEFAULT 1,
    coins_per_second REAL NOT NULL DEFAULT 0,
    mining_power REAL NOT NULL DEFAULT 1,
    current_streak INTEGER NOT NULL DEFAULT 0,
    longest_streak INTEGER NOT NULL DEFAULT 0,
    last_login_date TEXT,
    referral_code TEXT UNIQUE,
    referred_by TEXT,
    total_referrals INTEGER NOT NULL DEFAULT 0,
    lifetime_earnings REAL NOT NULL DEFAULT 0,
    daily_mining_rate REAL NOT NULL DEFAULT 1000,
    max_daily_earnings REAL NOT NULL DEFAULT 10000,
    today_earnings REAL NOT NULL DEFAULT 0,
    last_mining_date TEXT,
    streak_bonus_claimed INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (referred_by) REFERENCES users(id)
);
INSERT INTO user_profiles VALUES(1,'111503823123987272638',NULL,0,1,0,1,0,0,NULL,NULL,NULL,0,0,1000,10000,0,NULL,0,'2025-09-08 11:58:26','2025-09-08 11:58:26');
INSERT INTO user_profiles VALUES(2,'100918368155980132753',NULL,0,1,0,1,0,0,NULL,NULL,NULL,0,0,1000,10000,0,NULL,0,'2025-09-08 22:13:18','2025-09-08 22:13:18');
CREATE TABLE mining_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    coins_earned REAL NOT NULL,
    clicks_made INTEGER NOT NULL,
    session_duration INTEGER NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE social_tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    platform TEXT NOT NULL,
    task_type TEXT NOT NULL,
    reward_coins REAL NOT NULL,
    action_url TEXT,
    verification_method TEXT NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO social_tasks VALUES(1,'Follow on Twitter','Follow our official Twitter account','Twitter','follow',50,'https://twitter.com/ekehi_network','manual',1,1,'2025-09-07 21:54:57');
INSERT INTO social_tasks VALUES(2,'Join Telegram','Join our Telegram community','Telegram','join',75,'https://t.me/ekehi_network','manual',1,2,'2025-09-07 21:54:57');
INSERT INTO social_tasks VALUES(3,'Subscribe on YouTube','Subscribe to our YouTube channel','YouTube','subscribe',100,'https://youtube.com/@ekehi_network','manual',1,3,'2025-09-07 21:54:57');
INSERT INTO social_tasks VALUES(4,'Share on Facebook','Share our latest post on Facebook','Facebook','share',150,'https://facebook.com/ekehi_network','manual',1,4,'2025-09-07 21:54:57');
CREATE TABLE user_social_tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    task_id INTEGER NOT NULL,
    completed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (task_id) REFERENCES social_tasks(id),
    UNIQUE(user_id, task_id)
);
CREATE TABLE achievements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    achievement_id TEXT UNIQUE NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    type TEXT NOT NULL, 
    target REAL NOT NULL,
    reward REAL NOT NULL,
    rarity TEXT NOT NULL, 
    is_active INTEGER NOT NULL DEFAULT 1,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO achievements VALUES(1,'first_mine','First Mine','Complete your first mining action','total_coins',1,10,'common',1,'2025-09-07 21:54:57');
INSERT INTO achievements VALUES(2,'hundred_coins','Hundred Club','Mine 100 EKH tokens','total_coins',100,25,'common',1,'2025-09-07 21:54:57');
INSERT INTO achievements VALUES(3,'thousand_coins','Thousand Club','Mine 1,000 EKH tokens','total_coins',1000,100,'common',1,'2025-09-07 21:54:57');
INSERT INTO achievements VALUES(4,'ten_thousand_coins','Ten Thousand Club','Mine 10,000 EKH tokens','total_coins',10000,500,'rare',1,'2025-09-07 21:54:57');
INSERT INTO achievements VALUES(5,'first_streak','First Streak','Maintain a 1-day mining streak','mining_streak',1,20,'common',1,'2025-09-07 21:54:57');
INSERT INTO achievements VALUES(6,'week_streak','Week Streak','Maintain a 7-day mining streak','mining_streak',7,150,'rare',1,'2025-09-07 21:54:57');
INSERT INTO achievements VALUES(7,'month_streak','Month Streak','Maintain a 30-day mining streak','mining_streak',30,1000,'epic',1,'2025-09-07 21:54:57');
INSERT INTO achievements VALUES(8,'first_referral','First Referral','Refer your first friend','referrals',1,50,'common',1,'2025-09-07 21:54:57');
INSERT INTO achievements VALUES(9,'five_referrals','Social Butterfly','Refer 5 friends','referrals',5,300,'rare',1,'2025-09-07 21:54:57');
INSERT INTO achievements VALUES(10,'ten_referrals','Community Builder','Refer 10 friends','referrals',10,1000,'epic',1,'2025-09-07 21:54:57');
CREATE TABLE user_achievements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    achievement_id TEXT NOT NULL,
    claimed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (achievement_id) REFERENCES achievements(achievement_id),
    UNIQUE(user_id, achievement_id)
);
CREATE TABLE presale_purchases (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    amount_usd REAL NOT NULL,
    tokens_amount REAL NOT NULL,
    transaction_hash TEXT,
    status TEXT NOT NULL, 
    payment_method TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE TABLE ad_views (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    ad_type TEXT NOT NULL,
    reward REAL NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
DELETE FROM sqlite_sequence;
INSERT INTO sqlite_sequence VALUES('d1_migrations',2);
INSERT INTO sqlite_sequence VALUES('social_tasks',4);
INSERT INTO sqlite_sequence VALUES('achievements',10);
INSERT INTO sqlite_sequence VALUES('user_profiles',2);
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_total_coins ON user_profiles(total_coins DESC);
CREATE INDEX idx_user_profiles_referral_code ON user_profiles(referral_code);
CREATE INDEX idx_mining_sessions_user_id ON mining_sessions(user_id);
CREATE INDEX idx_social_tasks_active ON social_tasks(is_active);
CREATE INDEX idx_user_social_tasks_user_id ON user_social_tasks(user_id);
CREATE INDEX idx_user_social_tasks_task_id ON user_social_tasks(task_id);
CREATE INDEX idx_achievements_active ON achievements(is_active);
CREATE INDEX idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX idx_presale_purchases_user_id ON presale_purchases(user_id);
CREATE INDEX idx_ad_views_user_id ON ad_views(user_id);
