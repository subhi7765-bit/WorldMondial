package sa.mondial.world.core.network.dto

import kotlinx.serialization.Serializable
import sa.mondial.world.core.database.entity.MatchEntity
import sa.mondial.world.core.database.entity.MatchDetailsEntity
import sa.mondial.world.core.domain.LineupPlayer

@Serializable
data class MatchDto(
    val id: String,
    val homeNameAr: String,
    val homeNameEn: String,
    val awayNameAr: String,
    val awayNameEn: String,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val status: String = "UPCOMING",
    val roundAr: String = "",
    val roundEn: String = "",
    val utcTime: String = ""
) {
    fun toDatabaseEntity(): MatchEntity {
        return MatchEntity(
            id = id,
            homeNameAr = homeNameAr,
            homeNameEn = homeNameEn,
            awayNameAr = awayNameAr,
            awayNameEn = awayNameEn,
            homeScore = homeScore,
            awayScore = awayScore,
            status = status,
            roundAr = roundAr,
            roundEn = roundEn,
            utcTime = utcTime,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

@Serializable
data class MatchDetailsDto(
    val id: String,
    val homeNameAr: String,
    val homeNameEn: String,
    val homeFlag: String = "",
    val awayNameAr: String,
    val awayNameEn: String,
    val awayFlag: String = "",
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val status: String,
    val roundAr: String,
    val roundEn: String,
    val venueAr: String = "",
    val venueEn: String = "",
    val refereeAr: String = "",
    val refereeEn: String = "",
    val homeStartingXI: List<LineupPlayerDto> = emptyList(),
    val homeSubstitutes: List<LineupPlayerDto> = emptyList(),
    val awayStartingXI: List<LineupPlayerDto> = emptyList(),
    val awaySubstitutes: List<LineupPlayerDto> = emptyList(),
    val timelineEventsAr: List<String> = emptyList(),
    val timelineEventsEn: List<String> = emptyList()
) {
    fun toDatabaseEntity(timestampMs: Long): MatchDetailsEntity {
        return MatchDetailsEntity(
            id = id,
            homeNameAr = homeNameAr,
            homeNameEn = homeNameEn,
            homeFlag = homeFlag,
            awayNameAr = awayNameAr,
            awayNameEn = awayNameEn,
            awayFlag = awayFlag,
            homeScore = homeScore,
            awayScore = awayScore,
            status = status,
            roundAr = roundAr,
            roundEn = roundEn,
            venueAr = venueAr,
            venueEn = venueEn,
            refereeAr = refereeAr,
            refereeEn = refereeEn,
            homeStartingXI = homeStartingXI.map { it.toDomainModel() },
            homeSubstitutes = homeSubstitutes.map { it.toDomainModel() },
            awayStartingXI = awayStartingXI.map { it.toDomainModel() },
            awaySubstitutes = awaySubstitutes.map { it.toDomainModel() },
            timelineEventsAr = timelineEventsAr,
            timelineEventsEn = timelineEventsEn,
            timestampMs = timestampMs,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

@Serializable
data class LineupPlayerDto(
    val nameAr: String,
    val nameEn: String,
    val number: Int,
    val positionAr: String,
    val positionEn: String,
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