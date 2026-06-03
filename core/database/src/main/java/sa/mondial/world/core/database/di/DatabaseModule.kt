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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMondialDatabase(
        @ApplicationContext context: Context
    ): MondialDatabase {
        return Room.databaseBuilder(
            context,
            MondialDatabase::class.java,
            "mondial.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideMatchDao(database: MondialDatabase): MatchDao {
        return database.matchDao()
    }

    @Provides
    @Singleton
    fun provideNewsDao(database: MondialDatabase): NewsDao {
        return database.newsDao()
    }

    @Provides
    @Singleton
    fun provideMatchRemoteKeysDao(database: MondialDatabase): MatchRemoteKeysDao {
        return database.matchRemoteKeysDao()
    }
}