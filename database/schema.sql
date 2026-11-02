PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS expense_groups (
    group_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT,
    created_by INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(created_by) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS group_members (
    group_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    joined_at TEXT NOT NULL,
    PRIMARY KEY(group_id, user_id),
    FOREIGN KEY(group_id) REFERENCES expense_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS categories (
    category_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS expenses (
    expense_id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id INTEGER NOT NULL,
    description TEXT NOT NULL,
    amount NUMERIC NOT NULL CHECK(amount > 0),
    paid_by INTEGER NOT NULL,
    category_id INTEGER NOT NULL,
    split_type TEXT NOT NULL CHECK(split_type IN ('EQUAL','EXACT','PERCENTAGE')),
    expense_date TEXT NOT NULL,
    created_at TEXT NOT NULL,
    FOREIGN KEY(group_id) REFERENCES expense_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY(paid_by) REFERENCES users(user_id),
    FOREIGN KEY(category_id) REFERENCES categories(category_id)
);

CREATE TABLE IF NOT EXISTS expense_splits (
    split_id INTEGER PRIMARY KEY AUTOINCREMENT,
    expense_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    share_amount NUMERIC NOT NULL CHECK(share_amount >= 0),
    percentage NUMERIC,
    FOREIGN KEY(expense_id) REFERENCES expenses(expense_id) ON DELETE CASCADE,
    FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE(expense_id, user_id)
);

CREATE TABLE IF NOT EXISTS settlements (
    settlement_id INTEGER PRIMARY KEY AUTOINCREMENT,
    group_id INTEGER NOT NULL,
    payer_id INTEGER NOT NULL,
    receiver_id INTEGER NOT NULL,
    amount NUMERIC NOT NULL CHECK(amount > 0),
    settlement_date TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('PENDING','COMPLETED')),
    FOREIGN KEY(group_id) REFERENCES expense_groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY(payer_id) REFERENCES users(user_id),
    FOREIGN KEY(receiver_id) REFERENCES users(user_id),
    CHECK(payer_id <> receiver_id)
);
