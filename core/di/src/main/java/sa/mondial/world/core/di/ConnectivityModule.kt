package sa.mondial.world.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sa.mondial.world.core.common.ConnectivityObserver
import sa.mondial.world.core.common.AndroidConnectivityObserver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityModule {

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(
        impl: AndroidConnectivityObserver
    ): ConnectivityObserver
}