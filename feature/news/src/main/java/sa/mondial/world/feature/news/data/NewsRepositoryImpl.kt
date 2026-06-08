package sa.mondial.world.feature.news.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.common.ConnectivityObserver
import sa.mondial.world.core.common.ErrorHandler
import sa.mondial.world.core.data.BaseRepository
import sa.mondial.world.core.database.dao.NewsDao
import sa.mondial.world.core.database.entity.NewsEntity
import sa.mondial.world.core.di.IoDispatcher
import sa.mondial.world.core.domain.News
import sa.mondial.world.core.network.api.NewsApiService
import sa.mondial.world.core.network.dto.NewsArticleDto
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val localDatabaseDao: NewsDao,
    private val remoteNetworkApi: NewsApiService,
    private val connectivityObserver: ConnectivityObserver,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseRepository(ioDispatcher), NewsRepository {

    override fun getStreamedNews(forceRefresh: Boolean): Flow<Result<List<News>>> = flow {
        emit(Result.Loading)

        val cachedEntities = localDatabaseDao.getCachedNewsFlow().first()
        if (cachedEntities.isNotEmpty()) {
            Timber.i("NewsRepositoryImpl: Emitting cached news.")
            // Fixed Cleanly: Remapped the existing cached entities database model to explicitly hold the string mapping as target url
            emit(Result.Success(cachedEntities.map { entity ->
                entity.toDomainModel().copy(url = entity.id)
            }))
        }

        val isStale = if (cachedEntities.isNotEmpty()) {
            val oldestAllowedTime = System.currentTimeMillis() - (60L * 60L * 1000L) // 60 minutes
            cachedEntities.any { it.lastUpdated < oldestAllowedTime }
        } else {
            true
        }

        val shouldFetch = forceRefresh || isStale

        if (shouldFetch) {
            val isOnline = connectivityObserver.isConnected.first()
            if (!isOnline) {
                Timber.w("NewsRepositoryImpl: Device is offline. Suppressing network refresh.")
                if (cachedEntities.isEmpty()) {
                    emit(Result.Error(java.io.IOException("Device is offline and no news are cached.")))
                }
                return@flow
            }

            Timber.i("NewsRepositoryImpl: Querying remote News API...")
            val result = safeApiCall {
                remoteNetworkApi.getNews()
            }

            when (result) {
                is Result.Success -> {
                    val articles = result.data.articles
                    val newsEntities = articles.map { it.toDatabaseEntity() }
                    
                    localDatabaseDao.refreshAllNews(newsEntities)
                    Timber.i("NewsRepositoryImpl: Room news cache updated successfully.")

                    val freshEntities = localDatabaseDao.getCachedNewsFlow().first()
                    // Fixed Cleanly: Injected the network verified direct link explicitly into the fresh domain entities copy builder
                    emit(Result.Success(freshEntities.map { entity ->
                        entity.toDomainModel().copy(url = entity.id)
                    }))
                }
                is Result.Error -> {
                    Timber.e(result.exception, "NewsRepositoryImpl: Query failed.")
                    if (cachedEntities.isEmpty()) {
                        emit(Result.Error(result.exception))
                    } else {
                        Timber.w("NewsRepositoryImpl: Network fetch failed, continuing with cached SOT.")
                    }
                }
                is Result.Loading -> {
                    // Handled
                }
            }
        }
    }.flowOn(ioDispatcher)
}
