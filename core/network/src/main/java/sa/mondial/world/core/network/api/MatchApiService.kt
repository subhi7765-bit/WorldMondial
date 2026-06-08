package sa.mondial.world.core.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import sa.mondial.world.core.network.dto.MatchDto
import sa.mondial.world.core.network.dto.MatchDetailsDto
import kotlinx.serialization.Serializable

// Wrapper to successfully parse the JSON Object root from api.football-data.org
@Serializable
data class FootballMatchResponse(
    val matches: List<MatchDto>
)

interface MatchApiService {

    // Fixed Cleanly: Adjusted return type to FootballMatchResponse wrapper to fix JSON parsing serialization mismatch
    @GET("matches")
    suspend fun getMatches(): FootballMatchResponse

    @GET("matches/{id}")
    suspend fun getMatchDetails(@Path("id") id: String): MatchDetailsDto

    @GET("matches")
    suspend fun fetchPagedMatches(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): FootballMatchResponse
}
