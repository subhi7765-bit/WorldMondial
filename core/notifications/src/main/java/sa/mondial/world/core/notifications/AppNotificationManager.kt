package sa.mondial.world.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import sa.mondial.world.core.data.LocalizationManager
import javax.inject.Inject
import javax.inject.Singleton

interface AppNotificationManager {
    fun showMatchNotification(
        matchId: String,
        title: String,
        body: String,
        channelType: String
    )
    fun createNotificationChannels()
}

@Singleton
class AndroidAppNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localizationManager: LocalizationManager
) : AppNotificationManager {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    override fun showMatchNotification(
        matchId: String,
        title: String,
        body: String,
        channelType: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val isEnabled = localizationManager.notificationsEnabled.first()
            if (!isEnabled) {
                timber.log.Timber.i("AndroidAppNotificationManager: Notifications disabled in localized Settings.")
                return@launch
            }

            val channelId = if (channelType == "live_goals") "live_goals_channel" else "match_reminders_channel"
            
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("mondial://match?matchId=$matchId")
                `package` = context.packageName
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                matchId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            if (channelType == "live_goals") {
                builder.setCategory(NotificationCompat.CATEGORY_ALARM)
            }

            try {
                notificationManager.notify(matchId.hashCode(), builder.build())
            } catch (e: SecurityException) {
                timber.log.Timber.e(e, "Missing POST_NOTIFICATIONS permission")
            }
        }
    }

    override fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val liveGoalsChannel = NotificationChannel(
                "live_goals_channel",
                "Live Goals",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-urgency real-time goal alerts"
                enableLights(true)
                enableVibration(true)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
                setSound(
                    Uri.parse("android.resource://" + context.packageName + "/raw/live_goals_sound"),
                    audioAttributes
                )
            }

            val remindersChannel = NotificationChannel(
                "match_reminders_channel",
                "Match Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for scheduled matches starting soon"
            }

            nManager.createNotificationChannel(liveGoalsChannel)
            nManager.createNotificationChannel(remindersChannel)
        }
    }
}