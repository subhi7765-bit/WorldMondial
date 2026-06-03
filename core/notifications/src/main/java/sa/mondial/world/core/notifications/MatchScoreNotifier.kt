package sa.mondial.world.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sa.mondial.world.core.data.LocalizationManager
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchScoreNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localizationManager: LocalizationManager
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    fun showLiveScoreNotification(
        matchId: String,
        homeScore: Int,
        awayScore: Int,
        eventText: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val isEnabled = localizationManager.notificationsEnabled.first()
            if (!isEnabled) {
                Timber.i("MatchScoreNotifier: Push notifications deactivated in Settings panel. Suppressing alert.")
                return@launch
            }
            val channelId = "live_scores_channel"
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = android.net.Uri.parse("https://sa.mondial.world/match?matchId=$matchId")
                `package` = context.packageName
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                matchId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Live Score Update")
                .setContentText("$eventText ($homeScore - $awayScore)")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(matchId.hashCode(), notification)
        }
    }

    fun scheduleMatchReminder(matchId: String, matchTimeUtc: String) {
        val kickOffTime = Instant.parse(matchTimeUtc)
        val reminderTime = kickOffTime.minus(Duration.ofMinutes(30))
        val delayMs = reminderTime.toEpochMilli() - Instant.now().toEpochMilli()

        if (delayMs <= 0) {
            Timber.w("Match score notification not scheduled since kickoff is within 30 mins or past.")
            return
        }

        val data = workDataOf(
            "matchId" to matchId
        )

        val reminderRequest = OneTimeWorkRequestBuilder<MatchReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "reminder_$matchId",
                ExistingWorkPolicy.REPLACE,
                reminderRequest
            )
        Timber.i("WorkManager scheduled match reminder for $matchId with delay of ${delayMs / 1000}s")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val liveScoreChannel = NotificationChannel(
                "live_scores_channel",
                "Live Scores",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time live score updates"
            }
            notificationManager.createNotificationChannel(liveScoreChannel)
        }
    }
}