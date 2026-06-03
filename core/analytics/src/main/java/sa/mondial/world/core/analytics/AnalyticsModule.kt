package sa.mondial.world.core.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TimberTracker

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FirebaseTracker

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(
        impl: FirebaseAnalyticsTracker
    ): AnalyticsTracker

    @Binds
    @Singleton
    @TimberTracker
    abstract fun bindTimberAnalyticsTracker(
        impl: TimberAnalyticsTracker
    ): AnalyticsTracker

    @Binds
    @Singleton
    @FirebaseTracker
    abstract fun bindFirebaseAnalyticsTracker(
        impl: FirebaseAnalyticsTracker
    ): AnalyticsTracker
}