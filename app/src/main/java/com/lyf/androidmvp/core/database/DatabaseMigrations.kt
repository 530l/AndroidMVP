package com.lyf.androidmvp.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 将已有收藏迁移到独立用户表，再重建只包含远端缓存字段的项目表。
        db.execSQL("CREATE TABLE IF NOT EXISTS repo_user_data (repoId INTEGER NOT NULL PRIMARY KEY, isFavorite INTEGER NOT NULL, userRemark TEXT NOT NULL)")
        db.execSQL("INSERT INTO repo_user_data (repoId, isFavorite, userRemark) SELECT id, isFavorite, '' FROM repos WHERE isFavorite = 1")
        db.execSQL("""
            CREATE TABLE repos_new (
                id INTEGER NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                fullName TEXT NOT NULL,
                description TEXT,
                ownerName TEXT NOT NULL,
                ownerAvatarUrl TEXT NOT NULL,
                stars INTEGER NOT NULL,
                forks INTEGER NOT NULL,
                language TEXT,
                htmlUrl TEXT NOT NULL,
                cachedAt INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("INSERT INTO repos_new SELECT id, name, fullName, description, ownerName, ownerAvatarUrl, stars, forks, language, htmlUrl, cachedAt FROM repos")
        db.execSQL("DROP TABLE repos")
        db.execSQL("ALTER TABLE repos_new RENAME TO repos")
    }
}
