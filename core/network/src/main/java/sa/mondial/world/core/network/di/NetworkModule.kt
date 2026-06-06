package sa.mondial.world.core.network.di

import android.content.Context
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
import sa.mondial.world.core.network.api.MatchApiService
import sa.mondial.world.core.network.api.NewsApiService
import sa.mondial.world.core.network.TokenAuthenticator
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// Centrally managed live football data API key constant configuration
private const val FOOTBALL_API_KEY = "cae19e00d5e35743c4328b10c5328a945ba5bea6" 
private const val BASE_URL = "https://api.football-data.org/v4/"

/**
 * Dependency Injection module orchestrating the infrastructure configuration for the Network Layer.
 * Provides singleton instances of [Json], [OkHttpClient], and [Retrofit] with robust error tracking.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a configured Kotlinx Serialization [Json] instance for type-safe parsing.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /**
     * Builds and provides an [OkHttpClient] equipped with request interceptors,
     * network timeouts, secure token headers, and automated authentication recovery wrappers.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(tokenAuthenticator: TokenAuthenticator): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val authenticatedRequest = originalRequest.newBuilder()
                    // Dynamically appends the proper live data authentication token flag
                    .header("X-Auth-Token", FOOTBALL_API_KEY)
                    .build()
                chain.proceed(authenticatedRequest)
            }
            .authenticator(tokenAuthenticator)
            .build()
    }

    /**
     * Configures and provides the main [Retrofit] HTTP client engine utilizing
     * explicit serialization converters and customized network transport protocols.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /**
     * Yields the proxy service api interface dedicated to pulling match updates and statistics.
     */
    @Provides
    @Singleton
    fun provideMatchApiService(retrofit: Retrofit): MatchApiService {
        return retrofit.create(MatchApiService::class.java)
    }

    /**
     * Yields the proxy service api interface dedicated to pulling global localized news feeds.
     */
    @Provides
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit): NewsApiService {
        return retrofit.create(NewsApiService::class.java)
    }
}
