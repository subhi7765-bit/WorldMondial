package sa.mondial.world.feature.matches.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.data.BaseRepository
import sa.mondial.world.core.di.IoDispatcher
import sa.mondial.world.core.domain.Match
import sa.mondial.world.core.database.dao.MatchDao
import sa.mondial.world.core.network.api.MatchApiService
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class MatchesRepositoryImpl @Inject constructor(
    private val localDatabaseDao: MatchDao,
    private val remoteNetworkApi: MatchApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BaseRepository(ioDispatcher), MatchesRepository {

    override fun getStreamedMatches(forceRefresh: Boolean): Flow<Result<List<Match>>> {
        val dbFlow = localDatabaseDao.getCachedMatchesFlow()
            .map { entities -> Result.Success(entities.map { it.toDomainModel() }) as Result<List<Match>> }

        val networkTrigger = flow {
            emit(Result.Loading)
            try {
                val today = LocalDate.now(ZoneId.systemDefault())
                val dateFrom = today.minusDays(3).toString()
                val dateTo = today.plusDays(3).toString()

                val networkResponse = remoteNetworkApi.getMatches(dateFrom = dateFrom, dateTo = dateTo)
                val newEntities = networkResponse.matches.map { it.toDatabaseEntity() }
                
                // الذكاء الاصطناعي: حماية المباريات المباشرة من السيرفرات المتأخرة
                val oldEntities = localDatabaseDao.getAllMatchesSync()
                val mergedEntities = newEntities.map { newEntity ->
                    val oldEntity = oldEntities.find { it.id == newEntity.id }
                    if (oldEntity != null) {
                        val oldIsActive = oldEntity.status in listOf("IN_PLAY", "PAUSED", "LIVE", "FINISHED")
                        val newIsUpcoming = newEntity.status in listOf("TIMED", "SCHEDULED", "UPCOMING")
                        
                        if (oldIsActive && newIsUpcoming) {
                            newEntity.copy(
                                status = oldEntity.status,
                                homeScore = oldEntity.homeScore,
                                awayScore = oldEntity.awayScore
                            )
                        } else newEntity
                    } else newEntity
                }

                localDatabaseDao.refreshAllMatches(mergedEntities)
                emit(Result.Success(emptyList<Match>()))
            } catch (exception: Throwable) {
                emit(Result.Error(exception))
            }
        }

        return combine(dbFlow, networkTrigger) { dbResult, networkResult ->
            when (networkResult) {
                is Result.Loading -> if (dbResult is Result.Success && dbResult.data.isNotEmpty() && !forceRefresh) dbResult else Result.Loading
                is Result.Error -> if (dbResult is Result.Success && dbResult.data.isNotEmpty()) dbResult else Result.Error(networkResult.exception)
                else -> dbResult
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun getMatchDetails(matchId: String): sa.mondial.world.core.domain.MatchDetails {
        return withContext(ioDispatcher) {
            try {
                val remoteDto = remoteNetworkApi.getMatchDetails(matchId)
                var dbEntity = remoteDto.toDatabaseEntity(timestampMs = System.currentTimeMillis())
                
                // الذكاء الاصطناعي لشاشة التفاصيل
                val oldEntity = localDatabaseDao.getMatchDetails(matchId)
                if (oldEntity != null) {
                    val oldIsActive = oldEntity.status in listOf("IN_PLAY", "PAUSED", "LIVE", "FINISHED")
                    val newIsUpcoming = dbEntity.status in listOf("TIMED", "SCHEDULED", "UPCOMING")
                    
                    if (oldIsActive && newIsUpcoming) {
                        dbEntity = dbEntity.copy(
                            status = oldEntity.status,
                            homeScore = oldEntity.homeScore,
                            awayScore = oldEntity.awayScore
                        )
                    }
                }

                localDatabaseDao.insertMatchDetails(dbEntity)
                dbEntity.toDomainModel()
            } catch (throwable: Throwable) {
                val cached = localDatabaseDao.getMatchDetails(matchId)
                if (cached != null) cached.toDomainModel() else throw throwable
            }
        }
    }
}
