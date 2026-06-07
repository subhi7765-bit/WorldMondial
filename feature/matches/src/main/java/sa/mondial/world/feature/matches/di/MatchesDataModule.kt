package sa.mondial.world.feature.matches.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sa.mondial.world.feature.matches.data.MatchesRepository
import sa.mondial.world.feature.matches.data.MatchesRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MatchesDataModule {

    // Fixed Cleanly: Binds the MatchesRepository interface to its generated explicit implementation for Hilt Graph linking
    @Binds
    @Singleton
    abstract fun bindMatchesRepository(
        matchesRepositoryImpl: MatchesRepositoryImpl
    ): MatchesRepository
}
