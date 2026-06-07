package sa.mondial.world.feature.news.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sa.mondial.world.feature.news.data.NewsRepository
import sa.mondial.world.feature.news.data.NewsRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NewsDataModule {

    // Fixed Cleanly: Binds the NewsRepository interface to its generated explicit implementation for Hilt Graph linking
    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        newsRepositoryImpl: NewsRepositoryImpl
    ): NewsRepository
}
