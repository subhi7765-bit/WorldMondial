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
                Timber.i("MatchesRepository: Initializing network sync stream...")
                // حساب التواريخ (من أمس إلى غد) لنجبر السيرفر على إرسال بيانات الـ 3 أيام
                val today = LocalDate.now(ZoneId.systemDefault())
                val dateFrom = today.minusDays(1).toString()
                val dateTo = today.plusDays(1).toString()

                val networkResponse = remoteNetworkApi.getMatches(dateFrom = dateFrom, dateTo = dateTo)
                val dbEntities = networkResponse.matches.map { it.toDatabaseEntity() }
                localDatabaseDao.refreshAllMatches(dbEntities)
                emit(Result.Success(emptyList<Match>()))
            } catch (exception: Throwable) {
                Timber.e(exception, "MatchesRepository: Network sync failed.")
                emit(Result.Error(exception))
            }
        }

        return combine(dbFlow, networkTrigger) { dbResult, networkResult ->
            when (networkResult) {
                is Result.Loading -> {
                    if (dbResult is Result.Success && dbResult.data.isNotEmpty() && !forceRefresh) dbResult else Result.Loading
                }
                is Result.Error -> {
                    if (dbResult is Result.Success && dbResult.data.isNotEmpty()) dbResult else Result.Error(networkResult.exception)
                }
                else -> dbResult
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun getMatchDetails(matchId: String): sa.mondial.world.core.domain.MatchDetails {
        return withContext(ioDispatcher) {
            try {
                val remoteDto = remoteNetworkApi.getMatchDetails(matchId)
                val dbEntity = remoteDto.toDatabaseEntity(timestampMs = System.currentTimeMillis())
                localDatabaseDao.insertMatchDetails(dbEntity)
                dbEntity.toDomainModel()
            } catch (throwable: Throwable) {
                val cached = localDatabaseDao.getMatchDetails(matchId)
                if (cached != null) cached.toDomainModel() else throw throwable
            }
        }
    }
}
