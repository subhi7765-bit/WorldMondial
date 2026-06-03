package sa.mondial.world.feature.matches.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.data.BaseRepository
import sa.mondial.world.core.di.IoDispatcher
import sa.mondial.world.core.domain.Match
import sa.mondial.world.core.database.dao.MatchDao
import sa.mondial.world.core.database.entity.MatchEntity
import sa.mondial.world.core.database.entity.MatchDetailsEntity
import sa.mondial.world.core.network.api.MatchApiService
import sa.mondial.world.core.network.dto.MatchDto
import sa.mondial.world.core.network.dto.MatchDetailsDto
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

/**
 * Enterprise production repository featuring Cache-Then-Network and Paging 3 synchronization.
 * Room DB serves as Single Source of Truth, keeping state reactive and responsive offline.
 */
class MatchesRepositoryImpl @Inject constructor(
    private val localDatabaseDao: MatchDao,
    private val remoteKeysDao: sa.mondial.world.core.database.dao.MatchRemoteKeysDao,
    private val remoteNetworkApi: MatchApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseRepository(ioDispatcher), MatchesRepository {

    override fun getStreamedMatches(forceRefresh: Boolean): Flow<Result<List<Match>>> {
        val dbFlow = localDatabaseDao.getCachedMatchesFlow()
            .map { entities -> Result.Success(entities.map { it.toDomainModel() }) as Result<List<Match>> }

        val networkTrigger = flow {
            emit(Result.Loading)
            try {
                Timber.i("MatchesRepository: Initializing network sync stream...")
                val networkResponse = remoteNetworkApi.getMatches()
                val dbEntities = networkResponse.map { it.toDatabaseEntity() }
                localDatabaseDao.refreshAllMatches(dbEntities)
                Timber.i("MatchesRepository: Room Cache updated successfully.")
                emit(Result.Success(emptyList<Match>())) // Sentinel completion
            } catch (exception: Throwable) {
                Timber.e(exception, "MatchesRepository: Parallel network sync failed.")
                emit(Result.Error(exception))
            }
        }

        return combine(dbFlow, networkTrigger) { dbResult, networkResult ->
            when (networkResult) {
                is Result.Loading -> {
                    if (dbResult is Result.Success && dbResult.data.isNotEmpty() && !forceRefresh) {
                        dbResult
                    } else {
                        Result.Loading
                    }
                }
                is Result.Error -> {
                    if (dbResult is Result.Success && dbResult.data.isNotEmpty()) {
                        dbResult
                    } else {
                        Result.Error(networkResult.exception)
                    }
                }
                else -> dbResult
            }
        }.flowOn(ioDispatcher)
    }

    @OptIn(androidx.paging.ExperimentalPagingApi::class)
    override fun getPagedMatches(forceRefresh: Boolean): Flow<PagingData<sa.mondial.world.core.domain.Match>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                prefetchDistance = 2
            ),
            remoteMediator = MatchRemoteMediator(localDatabaseDao, remoteKeysDao, remoteNetworkApi, forceRefresh),
            pagingSourceFactory = { MatchDbPagingSource(localDatabaseDao) }
        ).flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomainModel() }
        }
    }

    override suspend fun getMatchDetails(matchId: String): sa.mondial.world.core.domain.MatchDetails {
        return withContext(ioDispatcher) {
            try {
                // Try fetching fresh data from the dedicated network endpoint
                Timber.i("MatchesRepositoryImpl: Fetching match details from /matches/$matchId/details endpoint")
                val remoteDto = remoteNetworkApi.getMatchDetails(matchId)
                val dbEntity = remoteDto.toDatabaseEntity(timestampMs = System.currentTimeMillis())
                localDatabaseDao.insertMatchDetails(dbEntity)
                dbEntity.toDomainModel()
            } catch (throwable: Throwable) {
                Timber.e(throwable, "MatchesRepositoryImpl: Network fetch failed. Attempting Room DB SOT fallback.")
                val cached = localDatabaseDao.getMatchDetails(matchId)
                if (cached != null) {
                    Timber.i("MatchesRepositoryImpl: Room Cache HIT for details of $matchId, last updated at ${cached.timestampMs}")
                    cached.toDomainModel()
                } else {
                    Timber.e("MatchesRepositoryImpl: Room Cache MISS for details of $matchId.")
                    throw throwable
                }
            }
        }
    }
}