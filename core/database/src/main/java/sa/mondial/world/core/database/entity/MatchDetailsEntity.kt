package sa.mondial.world.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import sa.mondial.world.core.database.converters.RoomConverters
import sa.mondial.world.core.domain.LineupPlayer
import sa.mondial.world.core.domain.MatchDetails
import sa.mondial.world.core.domain.MatchStatus
import java.time.Instant
import sa.mondial.world.core.common.DateParser

@Entity(tableName = "match_details")
@TypeConverters(RoomConverters::class)
data class MatchDetailsEntity(
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
    val timestampMs: Long,
    val lastUpdated: Long = 0L
) {
    fun toDomainModel(): MatchDetails {
        val parsedTime = DateParser.parseToInstant(timestampMs)
        val parsedStatus = try { MatchStatus.valueOf(status) } catch (e: Exception) { MatchStatus.FINISHED }
        return MatchDetails(
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
            venueAr = venueAr,
            venueEn = venueEn,
            refereeAr = refereeAr,
            refereeEn = refereeEn,
            homeStartingXI = homeStartingXI,
            homeSubstitutes = homeSubstitutes,
            awayStartingXI = awayStartingXI,
            awaySubstitutes = awaySubstitutes,
            timelineEventsAr = timelineEventsAr,
            timelineEventsEn = timelineEventsEn
        )
    }
}
