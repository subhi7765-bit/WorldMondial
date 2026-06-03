package sa.mondial.world.core.analytics

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimberAnalyticsTracker @Inject constructor() : AnalyticsTracker {
    override fun logScreenView(screenName: String) {
        Timber.i("Analytics: [ScreenView] $screenName")
    }

    override fun logEvent(eventName: String, params: Map<String, String>) {
        Timber.i("Analytics: [Event] Name: $eventName, Params: $params")
    }

    override fun logError(errorMessage: String, isFatal: Boolean) {
        Timber.e("Analytics: [Error] (Fatal: $isFatal) - $errorMessage")
    }
}