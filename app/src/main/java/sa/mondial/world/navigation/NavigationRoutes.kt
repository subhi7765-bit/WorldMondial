package sa.mondial.world.navigation

import kotlinx.serialization.Serializable

/**
 * Strict compile-time route declarations meeting Android Jetpack Navigation 2.8+ guidelines.
 * Entirely Type-Safe with no raw strings used for destinations.
 */
@Serializable
sealed interface DashboardDestination {

    @Serializable
    object Matches : DashboardDestination

    @Serializable
    object News : DashboardDestination

    @Serializable
    data class MatchDetails(
        val matchId: String,
        val homeTeamName: String,
        val awayTeamName: String
    ) : DashboardDestination

    @Serializable
    data class MatchDetailsRoute(
        val matchId: String
    ) : DashboardDestination

    @Serializable
    object Settings : DashboardDestination
}