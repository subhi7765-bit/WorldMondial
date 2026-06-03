package sa.mondial.world.core.analytics

interface AnalyticsTracker {
    fun logScreenView(screenName: String)
    fun logEvent(eventName: String, params: Map<String, String>)
    fun logError(errorMessage: String, isFatal: Boolean)
}