#!/bin/bash

DB_NAME="jChat.db"

check_sqlite3() {
    if ! command -v sqlite3 &> /dev/null; then
        echo "SQLite3 is not installed. Please install it first."
        exit 1
    fi
}

create_database() {
    if [ -f "$DB_NAME" ]; then
        rm "$DB_NAME"
        echo "Existing database removed."
    fi

    sqlite3 "$DB_NAME" << EOF
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    salt TEXT NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT
);

CREATE TABLE conversations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    conversation_name TEXT,
    is_group_chat BOOLEAN DEFAULT 0
);

CREATE TABLE user_conversations (
    user_id INTEGER,
    conversation_id INTEGER,
    PRIMARY KEY (user_id, conversation_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id)
);

CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sender_id INTEGER NOT NULL,
    conversation_id INTEGER NOT NULL,
    content TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (conversation_id) REFERENCES conversations(id)
);

CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_conversation ON messages(conversation_id);
CREATE INDEX idx_user_conversations_user ON user_conversations(user_id);
CREATE INDEX idx_user_conversations_conversation ON user_conversations(conversation_id);

EOF

    if [ -f "$DB_NAME" ]; then
        echo "Database '$DB_NAME' created successfully."
        echo "Tables created: users, conversations, user_conversations, messages"
    else
        echo "Failed to create database."
        exit 1
    fi
}

check_sqlite3

create_database
