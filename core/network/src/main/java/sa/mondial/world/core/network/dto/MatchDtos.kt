package sa.mondial.world.core.network.dto

import kotlinx.serialization.Serializable
import sa.mondial.world.core.database.entity.MatchEntity
import sa.mondial.world.core.database.entity.MatchDetailsEntity
import sa.mondial.world.core.domain.LineupPlayer

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
        // قاموس الترجمة الذكي للفرق والمنتخبات (يمكنك إضافة أي فرق أخرى هنا لاحقاً)
        val arabicDictionary = mapOf(
            "Saudi Arabia" to "السعودية",
            "Argentina" to "الأرجنتين",
            "Mexico" to "المكسيك",
            "South Africa" to "جنوب أفريقيا",
            "Real Madrid FC" to "ريال مدريد",
            "FC Barcelona" to "برشلونة",
            "Manchester City FC" to "مانشستر سيتي",
            "Liverpool FC" to "ليفربول"
        )

        val hNameEn = homeTeam?.name ?: homeTeam?.shortName ?: "Unknown"
        val aNameEn = awayTeam?.name ?: awayTeam?.shortName ?: "Unknown"
        
        val hNameAr = arabicDictionary[hNameEn] ?: hNameEn
        val aNameAr = arabicDictionary[aNameEn] ?: aNameEn

        return MatchEntity(
            id = id.toString(),
            homeNameAr = hNameAr,
            homeNameEn = hNameEn,
            awayNameAr = aNameAr,
            awayNameEn = aNameEn,
            homeScore = score?.fullTime?.home ?: score?.regularTime?.home,
            awayScore = score?.fullTime?.away ?: score?.regularTime?.away,
            status = status ?: "UPCOMING",
            roundAr = "الجولة ${matchday ?: ""}",
            roundEn = "Matchday ${matchday ?: ""}",
            utcTime = utcDate ?: "",
            lastUpdated = System.currentTimeMillis()
        )
    }
}

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
        val hNameEn = homeTeam?.name ?: "Unknown"
        val aNameEn = awayTeam?.name ?: "Unknown"
        val hNameAr = hNameEn // مبدئياً نفس الإنجليزي في التفاصيل
        val aNameAr = aNameEn
        val hFlag = homeTeam?.crest ?: ""
        val aFlag = awayTeam?.crest ?: ""
        val refName = referees?.firstOrNull()?.name ?: "Unknown"
        val venueName = venue ?: "Unknown Stadium"

        return MatchDetailsEntity(
            id = id.toString(),
            homeNameAr = hNameAr,
            homeNameEn = hNameEn,
            homeFlag = hFlag,
            awayNameAr = aNameAr,
            awayNameEn = aNameEn,
            awayFlag = aFlag,
            homeScore = score?.fullTime?.home ?: score?.regularTime?.home,
            awayScore = score?.fullTime?.away ?: score?.regularTime?.away,
            status = status ?: "UPCOMING",
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
