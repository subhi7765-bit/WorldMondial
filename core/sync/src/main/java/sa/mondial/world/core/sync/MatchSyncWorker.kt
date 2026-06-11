package sa.mondial.world.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import sa.mondial.world.core.database.dao.MatchDao
import sa.mondial.world.core.network.api.MatchApiService
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class MatchSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val matchDao: MatchDao,
    private val matchApiService: MatchApiService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Timber.i("MatchSyncWorker: Starting background sync...")
            
            // توسيع النافذة لـ 7 أيام لتفادي فروقات الـ UTC
            val today = LocalDate.now(ZoneId.systemDefault())
            val dateFrom = today.minusDays(3).toString()
            val dateTo = today.plusDays(3).toString()

            val response = matchApiService.getMatches(dateFrom = dateFrom, dateTo = dateTo)
            val entities = response.matches.map { it.toDatabaseEntity() }
            
            matchDao.refreshAllMatches(entities)
            
            Timber.i("MatchSyncWorker: Background sync completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "MatchSyncWorker: Background sync failed.")
            Result.retry()
        }
    }
}
