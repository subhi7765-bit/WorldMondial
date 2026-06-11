package sa.mondial.world.core.network.dto

import kotlinx.serialization.Serializable
import sa.mondial.world.core.database.entity.MatchEntity
import sa.mondial.world.core.database.entity.MatchDetailsEntity
import sa.mondial.world.core.domain.LineupPlayer

// 1. DTOs to match the exact JSON structure from football-data.org
@Serializable
data class TeamDto(
    val id: Int? = null,
    val name: String? = null,
    val shortName: String? = null,
    val crest: String? = null
)

@Serializable
data class ScoreDetailDto(
    val home: Int? = null,
    val away: Int? = null
)

@Serializable
data class ScoreDto(
    val fullTime: ScoreDetailDto? = null,
    val regularTime: ScoreDetailDto? = null
)

@Serializable
data class RefereeDto(
    val id: Int? = null,
    val name: String? = null
)

// 2. The Main Match DTO corresponding to the API response
@Serializable
data class MatchDto(
    val id: Int,
    val utcDate: String? = null,
    val status: String? = null,
    val matchday: Int? = null,
    val homeTeam: TeamDto? = null,
    val awayTeam: TeamDto? = null,
    val score: ScoreDto? = null
) {
    fun toDatabaseEntity(): MatchEntity {
        // Safe extraction from nested objects
        val hName = homeTeam?.name ?: "Unknown"
        val aName = awayTeam?.name ?: "Unknown"
        val matchStatus = status ?: "UPCOMING"
        
        return MatchEntity(
            id = id.toString(),
            homeNameAr = hName,
            homeNameEn = hName,
            awayNameAr = aName,
            awayNameEn = aName,
            homeScore = score?.fullTime?.home ?: score?.regularTime?.home,
            awayScore = score?.fullTime?.away ?: score?.regularTime?.away,
            status = matchStatus,
            roundAr = "الجولة ${matchday ?: ""}",
            roundEn = "Matchday ${matchday ?: ""}",
            utcTime = utcDate ?: "",
            lastUpdated = System.currentTimeMillis()
        )
    }
}

// 3. Match Details DTO mapping
@Serializable
data class MatchDetailsDto(
    val id: Int,
    val utcDate: String? = null,
    val status: String? = null,
    val matchday: Int? = null,
    val homeTeam: TeamDto? = null,
    val awayTeam: TeamDto? = null,
    val score: ScoreDto? = null,
    val referees: List<RefereeDto>? = null,
    val venue: String? = null
) {
    fun toDatabaseEntity(timestampMs: Long): MatchDetailsEntity {
        val hName = homeTeam?.name ?: "Unknown"
        val aName = awayTeam?.name ?: "Unknown"
        val hFlag = homeTeam?.crest ?: ""
        val aFlag = awayTeam?.crest ?: ""
        val matchStatus = status ?: "UPCOMING"
        val refName = referees?.firstOrNull()?.name ?: "Unknown"
        val venueName = venue ?: "Unknown Stadium"

        return MatchDetailsEntity(
            id = id.toString(),
            homeNameAr = hName,
            homeNameEn = hName,
            homeFlag = hFlag,
            awayNameAr = aName,
            awayNameEn = aName,
            awayFlag = aFlag,
            homeScore = score?.fullTime?.home ?: score?.regularTime?.home,
            awayScore = score?.fullTime?.away ?: score?.regularTime?.away,
            status = matchStatus,
            roundAr = "الجولة ${matchday ?: ""}",
            roundEn = "Matchday ${matchday ?: ""}",
            venueAr = venueName,
            venueEn = venueName,
            refereeAr = refName,
            refereeEn = refName,
            homeStartingXI = emptyList(),
            homeSubstitutes = emptyList(),
            awayStartingXI = emptyList(),
            awaySubstitutes = emptyList(),
            timelineEventsAr = emptyList(),
            timelineEventsEn = emptyList(),
            timestampMs = timestampMs,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

// 4. Lineup Players (Fallback for future pro-tier integration)
@Serializable
data class LineupPlayerDto(
    val nameAr: String = "",
    val nameEn: String = "",
    val number: Int = 0,
    val positionAr: String = "",
    val positionEn: String = "",
    val isCaptain: Boolean = false,
    val isGoalkeeper: Boolean = false
) {
    fun toDomainModel(): LineupPlayer {
        return LineupPlayer(
            nameAr = nameAr,
            nameEn = nameEn,
            number = number,
            positionAr = positionAr,
            positionEn = positionEn,
            isCaptain = isCaptain,
            isGoalkeeper = isGoalkeeper
        )
    }
}
