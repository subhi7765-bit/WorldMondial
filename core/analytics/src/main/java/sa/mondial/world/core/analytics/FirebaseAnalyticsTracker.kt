package sa.mondial.world.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun logScreenView(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
    }

    override fun logEvent(eventName: String, params: Map<String, String>) {
        firebaseAnalytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                param(key, value)
            }
        }
    }

    override fun logError(errorMessage: String, isFatal: Boolean) {
        firebaseAnalytics.logEvent("app_error") {
            param("error_message", errorMessage)
            param("is_fatal", if (isFatal) "true" else "false")
        }
    }
}