package sa.mondial.world.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import sa.mondial.world.core.domain.Match
import sa.mondial.world.core.domain.MatchStatus
import sa.mondial.world.core.common.DateParser

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val homeNameAr: String,
    val homeNameEn: String,
    val awayNameAr: String,
    val awayNameEn: String,
    val homeScore: Int?,
    val awayScore: Int?,
    val status: String,
    val roundAr: String,
    val roundEn: String,
    val utcTime: String,
    val lastUpdated: Long = 0L
) {
    fun toDomainModel(): Match {
        val parsedTime = DateParser.parseToInstant(utcTime)
        
        // THE FIX: Correctly mapping remote server statuses to our Domain Enums
        val parsedStatus = when (status.uppercase()) {
            "IN_PLAY", "PAUSED", "LIVE" -> MatchStatus.LIVE
            "FINISHED", "AWARDED" -> MatchStatus.FINISHED
            else -> MatchStatus.UPCOMING // Covers TIMED, SCHEDULED, POSTPONED
        }
        
        return Match(
            id = id,
            homeTeamNameAr = homeNameAr,
            homeTeamNameEn = homeNameEn,
            homeTeamFlagUrl = "",
            awayTeamNameAr = awayNameAr,
            awayTeamNameEn = awayNameEn,
            awayTeamFlagUrl = "",
            homeScore = homeScore,
            awayScore = awayScore,
            matchStatus = parsedStatus,
            roundAr = roundAr,
            roundEn = roundEn,
            utcTime = parsedTime,
            homeLineup = emptyList(),
            awayLineup = emptyList(),
            timelineEventsAr = emptyList(),
            timelineEventsEn = emptyList()
        )
    }
}
