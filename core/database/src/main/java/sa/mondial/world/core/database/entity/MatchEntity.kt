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
    val homeFlag: String,
    val awayNameAr: String,
    val awayNameEn: String,
    val awayFlag: String,
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
        
        // فصلنا حالة "PAUSED" لتصبح "بين الشوطين"
        val parsedStatus = when (status.uppercase()) {
            "IN_PLAY", "LIVE" -> MatchStatus.LIVE
            "PAUSED" -> MatchStatus.HALF_TIME
            "FINISHED", "AWARDED" -> MatchStatus.FINISHED
            else -> MatchStatus.UPCOMING
        }
        
        return Match(
            id = id,
            homeTeamNameAr = homeNameAr,
            homeTeamNameEn = homeNameEn,
            homeTeamFlagUrl = homeFlag,
            awayTeamNameAr = awayNameAr,
            awayTeamNameEn = awayNameEn,
            awayTeamFlagUrl = awayFlag,
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
