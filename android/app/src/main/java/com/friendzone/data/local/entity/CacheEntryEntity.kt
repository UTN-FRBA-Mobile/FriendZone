package com.example.friendzone.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_entries")
data class CacheEntryEntity(
    @PrimaryKey val key: String,
    val json: String,
    val updatedAt: Long,
)
