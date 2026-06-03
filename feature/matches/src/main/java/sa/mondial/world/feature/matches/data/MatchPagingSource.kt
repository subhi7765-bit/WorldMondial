package sa.mondial.world.feature.matches.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import sa.mondial.world.core.domain.Match
import sa.mondial.world.core.database.dao.MatchDao
import sa.mondial.world.core.database.dao.MatchRemoteKeysDao
import sa.mondial.world.core.database.entity.MatchEntity as DbMatchEntity
import sa.mondial.world.core.database.entity.MatchRemoteKeys
import sa.mondial.world.core.network.api.MatchApiService
import timber.log.Timber

/**
 * Enterprise-grade Room backed RemoteMediator coordinating local SOT with pagination queries.
 */
@OptIn(ExperimentalPagingApi::class)
class MatchRemoteMediator(
    private val localDatabaseDao: MatchDao,
    private val remoteKeysDao: MatchRemoteKeysDao,
    private val remoteNetworkApi: MatchApiService,
    private val forceRefresh: Boolean
) : RemoteMediator<Int, DbMatchEntity>() {

    override suspend fun initialize(): InitializeAction {
        return if (forceRefresh) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, DbMatchEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextKey?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevKey = remoteKeys?.prevKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    prevKey
                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextKey
                }
            }

            Timber.i("MatchRemoteMediator: Synced network results loaded for loadType=$loadType page=$page")
            
            val remoteDtos = remoteNetworkApi.fetchPagedMatches(page = page, limit = state.config.pageSize)
            val endOfPaginationReached = remoteDtos.isEmpty()

            val mappedEntities = remoteDtos.map { it.toDatabaseEntity() }

            if (loadType == LoadType.REFRESH) {
                remoteKeysDao.clearRemoteKeys()
                localDatabaseDao.refreshAllMatches(mappedEntities)
            } else {
                localDatabaseDao.insertMatchesBatch(mappedEntities)
            }

            val prevKey = if (page == 1) null else page - 1
            val nextKey = if (endOfPaginationReached) null else page + 1
            val keys = remoteDtos.map {
                MatchRemoteKeys(
                    matchId = it.id,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
            }
            remoteKeysDao.insertAll(keys)

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            Timber.e(exception, "MatchRemoteMediator: Paginated network execution failed")
            if (loadType == LoadType.REFRESH) {
                MediatorResult.Error(java.io.IOException("Mondial Service Sync Failed: Unable to retrieve the initial match pages. Please check internet connection.", exception))
            } else {
                MediatorResult.Error(exception)
            }
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, DbMatchEntity>): MatchRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { match ->
            remoteKeysDao.getRemoteKeysForMatchId(match.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, DbMatchEntity>): MatchRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { match ->
            remoteKeysDao.getRemoteKeysForMatchId(match.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, DbMatchEntity>): MatchRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { matchId ->
                remoteKeysDao.getRemoteKeysForMatchId(matchId)
            }
        }
    }
}

/**
 * Strict database backup PagingSource implementation sourcing Room cache dynamically.
 */
class MatchDbPagingSource(
    private val localDatabaseDao: MatchDao
) : PagingSource<Int, DbMatchEntity>() {

    override fun getRefreshKey(state: PagingState<Int, DbMatchEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DbMatchEntity> {
        val pageKey = params.key ?: 1
        return try {
            val limit = params.loadSize
            val offset = (pageKey - 1) * limit
            val entities = localDatabaseDao.getPagedMatches(limit, offset)

            LoadResult.Page(
                data = entities,
                prevKey = if (pageKey == 1) null else pageKey - 1,
                nextKey = if (entities.isEmpty() || entities.size < limit) null else pageKey + 1
            )
        } catch (exception: Exception) {
            Timber.e(exception, "MatchDbPagingSource: Database cache load failed")
            LoadResult.Error(exception)
        }
    }
}