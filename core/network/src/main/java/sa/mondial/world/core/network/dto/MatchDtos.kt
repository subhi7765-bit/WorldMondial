package sa.mondial.world.core.network.dto

import kotlinx.serialization.Serializable
import sa.mondial.world.core.database.entity.MatchEntity
import sa.mondial.world.core.database.entity.MatchDetailsEntity
import sa.mondial.world.core.domain.LineupPlayer

// القاموس الشامل للمنتخبات والأندية (متاح لكل التطبيق)
val arabicDictionary = mapOf(
    // المنتخبات العربية
    "Saudi Arabia" to "السعودية", "Egypt" to "مصر", "Morocco" to "المغرب", 
    "Tunisia" to "تونس", "Algeria" to "الجزائر", "Qatar" to "قطر", 
    "UAE" to "الإمارات", "Iraq" to "العراق", "Syria" to "سوريا", "Oman" to "عمان",
    "Jordan" to "الأردن", "Palestine" to "فلسطين", "Lebanon" to "لبنان", "Bahrain" to "البحرين",
    
    // منتخبات كأس العالم العالمية
    "Argentina" to "الأرجنتين", "Brazil" to "البرازيل", "France" to "فرنسا", 
    "Germany" to "ألمانيا", "Spain" to "إسبانيا", "Portugal" to "البرتغال", 
    "England" to "إنجلترا", "Italy" to "إيطاليا", "Netherlands" to "هولندا", 
    "Belgium" to "بلجيكا", "Croatia" to "كرواتيا", "Uruguay" to "الأوروغواي", 
    "Mexico" to "المكسيك", "USA" to "أمريكا", "United States" to "أمريكا", 
    "Canada" to "كندا", "Japan" to "اليابان", "South Korea" to "كوريا الجنوبية", "Korea Republic" to "كوريا الجنوبية",
    "Australia" to "أستراليا", "Switzerland" to "سويسرا", "Denmark" to "الدنمارك", 
    "Colombia" to "كولومبيا", "Chile" to "تشيلي", "Senegal" to "السنغال", 
    "Cameroon" to "الكاميرون", "Ghana" to "غانا", "Nigeria" to "نيجيريا",
    "South Africa" to "جنوب أفريقيا", "Serbia" to "صربيا", "Poland" to "بولندا",
    "Wales" to "ويلز", "Iran" to "إيران", "Ecuador" to "الإكوادور", "Peru" to "بيرو",

    // الأندية الكبرى (لتغطية البطولات ودوري الأبطال)
    "Real Madrid FC" to "ريال مدريد", "Real Madrid" to "ريال مدريد",
    "FC Barcelona" to "برشلونة", "Barcelona" to "برشلونة",
    "Manchester City FC" to "مانشستر سيتي", "Manchester City" to "مانشستر سيتي",
    "Manchester United FC" to "مانشستر يونايتد", "Manchester United" to "مانشستر يونايتد",
    "Liverpool FC" to "ليفربول", "Liverpool" to "ليفربول",
    "Arsenal FC" to "أرسنال", "Arsenal" to "أرسنال",
    "Chelsea FC" to "تشيلسي", "Chelsea" to "تشيلسي",
    "Tottenham Hotspur FC" to "توتنهام", "Tottenham" to "توتنهام",
    "FC Bayern München" to "بايرن ميونخ", "Bayern Munich" to "بايرن ميونخ",
    "Borussia Dortmund" to "دورتموند",
    "Paris Saint-Germain FC" to "باريس سان جيرمان", "PSG" to "باريس سان جيرمان",
    "Juventus FC" to "يوفنتوس", "Juventus" to "يوفنتوس",
    "AC Milan" to "ميلان", "Inter Milan" to "إنتر ميلان", "Inter" to "إنتر ميلان"
)

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
        val hNameEn = homeTeam?.name ?: homeTeam?.shortName ?: "Unknown"
        val aNameEn = awayTeam?.name ?: awayTeam?.shortName ?: "Unknown"
        
        val hNameAr = arabicDictionary[hNameEn] ?: arabicDictionary[homeTeam?.shortName] ?: hNameEn
        val aNameAr = arabicDictionary[aNameEn] ?: arabicDictionary[awayTeam?.shortName] ?: aNameEn

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
        val hNameEn = homeTeam?.name ?: homeTeam?.shortName ?: "Unknown"
        val aNameEn = awayTeam?.name ?: awayTeam?.shortName ?: "Unknown"
        
        val hNameAr = arabicDictionary[hNameEn] ?: arabicDictionary[homeTeam?.shortName] ?: hNameEn
        val aNameAr = arabicDictionary[aNameEn] ?: arabicDictionary[awayTeam?.shortName] ?: aNameEn
        
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
