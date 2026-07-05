package com.example.friendzone.di

import android.content.Context
import androidx.room.Room
import com.example.friendzone.data.local.FriendZoneDatabase
import com.example.friendzone.data.local.dao.CacheEntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideFriendZoneDatabase(
        @ApplicationContext context: Context,
    ): FriendZoneDatabase = Room.databaseBuilder(
        context,
        FriendZoneDatabase::class.java,
        "friendzone_cache.db",
    ).build()

    @Provides
    fun provideCacheEntryDao(database: FriendZoneDatabase): CacheEntryDao =
        database.cacheEntryDao()
}
