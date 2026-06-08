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

@HiltWorker
class MatchSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val remoteNetworkApi: MatchApiService,
    private val localDatabaseDao: MatchDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Timber.i("MatchSyncWorker: Background periodic match sync routine initiated.")
        return try {
            val response = remoteNetworkApi.getMatches()
            // Fixed Cleanly: Accessing the interior matches array collection from the upgraded response object wrapper
            val networkMatches = response.matches
            
            if (networkMatches.isNotEmpty()) {
                val dbEntities = networkMatches.map { it.toDatabaseEntity() }
                localDatabaseDao.refreshAllMatches(dbEntities)
                Timber.i("MatchSyncWorker: Successfully cached background records count: ${networkMatches.size}")
            } else {
                Timber.w("MatchSyncWorker: Network data sync returned an empty collection array.")
            }
            
            Result.success()
        } catch (exception: Exception) {
            Timber.e(exception, "MatchSyncWorker: Periodic background data synchronization routine execution failed.")
            Result.retry()
        }
    }
}
