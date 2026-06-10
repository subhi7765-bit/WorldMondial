package sa.mondial.world.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import sa.mondial.world.core.network.BuildConfig
import sa.mondial.world.core.network.api.MatchApiService
import sa.mondial.world.core.network.api.NewsApiService
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MatchHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NewsHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MatchRetrofitEngine

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NewsRetrofitEngine

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    @MatchHttpClient
    fun provideMatchOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    // SECURED: Reading securely from BuildConfig instead of hardcoded strings
                    .header("X-Auth-Token", BuildConfig.FOOTBALL_API_KEY)
                    .header("User-Agent", "WorldMondialApp/1.0")
                    .build()
                chain.proceed(request)
            }.build()
    }

    @Provides
    @Singleton
    @NewsHttpClient
    fun provideNewsOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(interceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    // SECURED: Reading securely from BuildConfig instead of hardcoded strings
                    .header("X-Api-Key", BuildConfig.NEWS_API_KEY)
                    .header("User-Agent", "WorldMondialApp/1.0")
                    .build()
                chain.proceed(request)
            }.build()
    }

    @Provides
    @Singleton
    @MatchRetrofitEngine
    fun provideMatchRetrofit(@MatchHttpClient okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.FOOTBALL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @NewsRetrofitEngine
    fun provideNewsRetrofit(@NewsHttpClient okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.NEWS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideMatchApiService(@MatchRetrofitEngine retrofit: Retrofit): MatchApiService {
        return retrofit.create(MatchApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNewsApiService(@NewsRetrofitEngine retrofit: Retrofit): NewsApiService {
        return retrofit.create(NewsApiService::class.java)
    }
}
