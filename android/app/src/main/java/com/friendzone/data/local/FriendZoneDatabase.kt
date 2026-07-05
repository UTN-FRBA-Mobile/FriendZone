package com.example.friendzone.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.friendzone.data.local.dao.CacheEntryDao
import com.example.friendzone.data.local.entity.CacheEntryEntity

@Database(
    entities = [CacheEntryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FriendZoneDatabase : RoomDatabase() {
    abstract fun cacheEntryDao(): CacheEntryDao
}
