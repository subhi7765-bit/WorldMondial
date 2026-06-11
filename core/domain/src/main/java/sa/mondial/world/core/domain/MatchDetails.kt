package sa.mondial.world.core.domain

import java.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LineupPlayer(
    val nameAr: String,
    val nameEn: String,
    val number: Int,
    val positionAr: String,
    val positionEn: String,
    val isCaptain: Boolean = false,
    val isGoalkeeper: Boolean = false
)

data class MatchDetails(
    val id: String,
    val competitionNameAr: String = "", // جديد: اسم البطولة عربي
    val competitionNameEn: String = "", // جديد: اسم البطولة إنجليزي
    val competitionEmblem: String = "", // جديد: شعار البطولة
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
    val venueAr: String,
    val venueEn: String,
    val refereeAr: String,
    val refereeEn: String,
    val homeStartingXI: List<LineupPlayer>,
    val homeSubstitutes: List<LineupPlayer>,
    val awayStartingXI: List<LineupPlayer>,
    val awaySubstitutes: List<LineupPlayer>,
    val timelineEventsAr: List<String>,
    val timelineEventsEn: List<String>,
    val lastSyncTimeMs: Long = 0L
)
