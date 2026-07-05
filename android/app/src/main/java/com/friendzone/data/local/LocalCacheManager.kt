package com.example.friendzone.data.local

import com.example.friendzone.data.local.dao.CacheEntryDao
import com.example.friendzone.data.local.entity.CacheEntryEntity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalCacheManager @Inject constructor(
    private val cacheEntryDao: CacheEntryDao,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun <T> getList(key: String, serializer: kotlinx.serialization.KSerializer<T>): List<T>? {
        val entry = cacheEntryDao.get(key) ?: return null
        return runCatching {
            json.decodeFromString(ListSerializer(serializer), entry.json)
        }.getOrNull()
    }

    suspend fun <T> putList(key: String, items: List<T>, serializer: kotlinx.serialization.KSerializer<T>) {
        cacheEntryDao.put(
            CacheEntryEntity(
                key = key,
                json = json.encodeToString(ListSerializer(serializer), items),
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun delete(key: String) {
        cacheEntryDao.delete(key)
    }

    suspend fun deleteByPrefix(prefix: String) {
        cacheEntryDao.deleteByPrefix(prefix)
    }

    suspend fun clearAll() {
        cacheEntryDao.clearAll()
    }
}
