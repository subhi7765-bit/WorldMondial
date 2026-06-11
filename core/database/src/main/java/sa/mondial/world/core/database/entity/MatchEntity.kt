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
    val homeFlag: String, // جديد: رابط صورة صاحب الأرض
    val awayNameAr: String,
    val awayNameEn: String,
    val awayFlag: String, // جديد: رابط صورة الضيف
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
        
        val parsedStatus = when (status.uppercase()) {
            "IN_PLAY", "PAUSED", "LIVE" -> MatchStatus.LIVE
            "FINISHED", "AWARDED" -> MatchStatus.FINISHED
            else -> MatchStatus.UPCOMING
        }
        
        return Match(
            id = id,
            homeTeamNameAr = homeNameAr,
            homeTeamNameEn = homeNameEn,
            homeTeamFlagUrl = homeFlag, // تمرير الرابط للواجهة
            awayTeamNameAr = awayNameAr,
            awayTeamNameEn = awayNameEn,
            awayTeamFlagUrl = awayFlag, // تمرير الرابط للواجهة
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
