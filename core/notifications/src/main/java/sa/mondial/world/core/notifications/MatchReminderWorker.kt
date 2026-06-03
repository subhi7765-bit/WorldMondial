package sa.mondial.world.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MatchReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return onRunWork()
    }

    suspend fun onRunWork(): Result {
        val attempt = runAttemptCount
        timber.log.Timber.i("MatchReminderWorker: Executing reminder work onRunWork, attempt number: $attempt")
        
        val matchId = inputData.getString("matchId") ?: return Result.failure()

        try {
            // Simulated operation that may need retries (e.g. synching clock or fetching state)
            // Retry logic up to 3 times: attempts are 0-indexed: 0, 1, 2
            if (attempt < 2) {
                timber.log.Timber.w("MatchReminderWorker: Transient error simulated on attempt $attempt. Retrying work...")
                return Result.retry()
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channelId = "live_scores_channel"

            val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Match Starting Soon!")
                .setContentText("Our match will kick off in 30 minutes. Stay tuned!")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(matchId.hashCode() + 1000, notification)
            timber.log.Timber.i("MatchReminderWorker: Alert notification dispatched successfully on attempt $attempt")
            return Result.success()
        } catch (e: Exception) {
            timber.log.Timber.e(e, "MatchReminderWorker: Error occurred during attempt $attempt")
            return if (attempt < 2) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}