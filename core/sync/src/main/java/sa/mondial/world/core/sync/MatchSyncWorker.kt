package sa.mondial.world.core.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import sa.mondial.world.core.network.api.MatchApiService
import sa.mondial.world.feature.news.data.NewsRepository
import timber.log.Timber

class MatchSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncEntryPoint {
        fun matchApiService(): MatchApiService
        fun newsRepository(): NewsRepository
    }

    override suspend fun doWork(): Result {
        Timber.i("MatchSyncWorker: Starting background synchronization task...")
        
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncEntryPoint::class.java
        )
        val matchApiService = entryPoint.matchApiService()
        val newsRepository = entryPoint.newsRepository()

        return try {
            // 1. Fetch latest matches
            Timber.i("MatchSyncWorker: Syncing match schedule and results...")
            val matchesResponse = matchApiService.getMatches()
            Timber.i("MatchSyncWorker: Successfully synchronized ${matchesResponse.size} matches from network Api.")

            // 2. Fetch latest news
            Timber.i("MatchSyncWorker: Syncing RSS news feed...")
            newsRepository.getStreamedNews(forceRefresh = true).collect { result ->
                Timber.d("MatchSyncWorker: News sync state update: ${result}")
            }
            Timber.i("MatchSyncWorker: News synchronized successfully.")

            Timber.i("MatchSyncWorker: Background sync completed successfully.")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "MatchSyncWorker: Synchronization error occurred.")
            if (runAttemptCount < 3) {
                Timber.w("MatchSyncWorker: Retrying background sync task (attempt ${runAttemptCount + 1})...")
                Result.retry()
            } else {
                Timber.e("MatchSyncWorker: Background sync failed after max attempts.")
                Result.failure()
            }
        }
    }
}