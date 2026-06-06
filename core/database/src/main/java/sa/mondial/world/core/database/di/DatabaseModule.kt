package sa.mondial.world.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sa.mondial.world.core.database.MondialDatabase
import sa.mondial.world.core.database.dao.MatchDao
import sa.mondial.world.core.database.dao.MatchRemoteKeysDao
import sa.mondial.world.core.database.dao.NewsDao
import javax.inject.Singleton

/**
 * Dependency Injection module managing the local Room Database lifecycle
 * and providing SQLite Data Access Objects (DAOs) across the repository layers.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Builds and provides a single, thread-safe instance of the Room database engine.
     * Configured with destructive migration fallbacks optimized for standard application updates.
     */
    @Provides
    @Singleton
    fun provideMondialDatabase(
        @ApplicationContext context: Context
    ): MondialDatabase {
        return Room.databaseBuilder(
            context,
            MondialDatabase::class.java,
            "mondial.db"
        )
        // Updated to adhere to modern Room 2.7+ specifications to safely drop tables upon version mismatch
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }

    /**
     * Provides the Data Access Object handling database operations for match data models.
     */
    @Provides
    @Singleton
    fun provideMatchDao(database: MondialDatabase): MatchDao {
        return database.matchDao()
    }

    /**
     * Provides the Data Access Object handling database operations for localized sports news feeds.
     */
    @Provides
    @Singleton
    fun provideNewsDao(database: MondialDatabase): NewsDao {
        return database.newsDao()
    }

    /**
     * Provides the Data Access Object handling keys for remote pagination syncing structures.
     */
    @Provides
    @Singleton
    fun provideMatchRemoteKeysDao(database: MondialDatabase): MatchRemoteKeysDao {
        return database.matchRemoteKeysDao()
    }
}
