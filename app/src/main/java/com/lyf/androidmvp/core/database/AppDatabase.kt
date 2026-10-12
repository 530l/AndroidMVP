package com.lyf.androidmvp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lyf.androidmvp.core.database.dao.RepoDao
import com.lyf.androidmvp.core.database.entity.RepoEntity
import com.lyf.androidmvp.core.database.entity.RepoUserDataEntity

@Database(
    entities = [RepoEntity::class, RepoUserDataEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun repoDao(): RepoDao
}
