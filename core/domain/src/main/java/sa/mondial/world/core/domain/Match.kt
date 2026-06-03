package sa.mondial.world.core.domain

import java.time.Instant

/**
 * Immutable Domain Model representing an official Mondial Match.
 * Clean domain models strictly decoupled from Network and Persistence entities.
 */
data class Match(
    val id: String,
    val homeTeamNameAr: String,
    val homeTeamNameEn: String,
    val homeTeamFlagUrl: String,
    val awayTeamNameAr: String,
    val awayTeamNameEn: String,
    val awayTeamFlagUrl: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val matchStatus: MatchStatus,
    val roundAr: String,
    val roundEn: String,
    val utcTime: Instant,
    val homeLineup: List<String>,
    val awayLineup: List<String>,
    val timelineEventsAr: List<String>,
    val timelineEventsEn: List<String>
)

enum class MatchStatus {
    UPCOMING, LIVE, FINISHED
}