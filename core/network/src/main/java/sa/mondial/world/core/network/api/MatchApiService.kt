package sa.mondial.world.core.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import sa.mondial.world.core.network.dto.MatchDetailsDto
import kotlinx.serialization.Serializable
import sa.mondial.world.core.network.dto.MatchDto

@Serializable
data class FootballMatchResponse(
    val matches: List<MatchDto>
)

interface MatchApiService {
    // تحديث قوي: طلب المباريات بناءً على نطاق زمني محدد
    @GET("matches")
    suspend fun getMatches(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String
    ): FootballMatchResponse

    @GET("matches/{id}")
    suspend fun getMatchDetails(@Path("id") id: String): MatchDetailsDto
}
