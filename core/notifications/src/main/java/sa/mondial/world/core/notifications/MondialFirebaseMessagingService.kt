package sa.mondial.world.core.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import sa.mondial.world.core.analytics.AnalyticsTracker
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface NotificationEventManager {
    val foregroundEvents: SharedFlow<ForegroundPayload>
    fun emitEventBlocking(payload: ForegroundPayload)
}

@Singleton
class NotificationEventManagerImpl @Inject constructor() : NotificationEventManager {
    private val _foregroundEvents = MutableSharedFlow<ForegroundPayload>(extraBufferCapacity = 64)
    override val foregroundEvents: SharedFlow<ForegroundPayload> = _foregroundEvents.asSharedFlow()

    override fun emitEventBlocking(payload: ForegroundPayload) {
        _foregroundEvents.tryEmit(payload)
    }
}

@dagger.Module
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
abstract class NotificationsModule {
    @dagger.Binds
    @Singleton
    abstract fun bindNotificationEventManager(
        impl: NotificationEventManagerImpl
    ): NotificationEventManager

    @dagger.Binds
    @Singleton
    abstract fun bindAppNotificationManager(
        impl: AndroidAppNotificationManager
    ): AppNotificationManager
}

data class ForegroundPayload(
    val matchId: String,
    val homeScore: Int,
    val awayScore: Int,
    val eventText: String
)

@AndroidEntryPoint
class MondialFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: AppNotificationManager

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    @Inject
    lateinit var notificationEventManager: NotificationEventManager

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.i("FCM Message Received: ${message.data}")

        val matchId = message.data["matchId"] ?: "unknown"
        val homeScore = message.data["homeScore"]?.toIntOrNull() ?: 0
        val awayScore = message.data["awayScore"]?.toIntOrNull() ?: 0
        val eventText = message.data["eventText"] ?: "Goal scored!"

        analyticsTracker.logEvent(
            "notification_received",
            mapOf("matchId" to matchId, "homeScore" to homeScore.toString(), "awayScore" to awayScore.toString())
        )

        val payload = ForegroundPayload(matchId, homeScore, awayScore, eventText)
        notificationEventManager.emitEventBlocking(payload)

        // Show match notification
        val title = message.notification?.title ?: message.data["title"] ?: "Live Score Update"
        val body = message.notification?.body ?: message.data["body"] ?: "$eventText ($homeScore - $awayScore)"
        val type = message.data["type"] ?: "live_goals"

        notificationManager.showMatchNotification(
            matchId = matchId,
            title = title,
            body = body,
            channelType = type
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.i("Mondial FCM Token Refreshed: $token")
        analyticsTracker.logEvent("fcm_token_refreshed", mapOf("token" to token))
    }
}