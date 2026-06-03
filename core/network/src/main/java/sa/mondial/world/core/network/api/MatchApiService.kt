package sa.mondial.world.core.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import sa.mondial.world.core.network.dto.MatchDto
import sa.mondial.world.core.network.dto.MatchDetailsDto

interface MatchApiService {
    @GET("/matches")
    suspend fun getMatches(): List<MatchDto>

    @GET("/matches/{id}/details")
    suspend fun getMatchDetails(@Path("id") id: String): MatchDetailsDto

    @GET("/matches/paged")
    suspend fun fetchPagedMatches(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): List<MatchDto>
}