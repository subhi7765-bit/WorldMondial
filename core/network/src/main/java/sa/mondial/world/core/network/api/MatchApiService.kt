package sa.mondial.world.core.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import sa.mondial.world.core.network.dto.MatchDto
import sa.mondial.world.core.network.dto.MatchDetailsDto

/**
 * Retrofit API declaration layer interfacing with the football data network endpoints.
 * Handles collection queries for live events, scheduling pipelines, and targeted match profiles.
 */
interface MatchApiService {

    /**
     * Retrieves the standard complete catalog of matches for the active daily schedule.
     * Note: Removed leading slash to ensure correct base URL sub-path resolution.
     */
    @GET("matches")
    suspend fun getMatches(): List<MatchDto>

    /**
     * Fetches detailed data structures and historical statistics for a specific match entry.
     *
     * @param id The absolute unique identifier of the selected match.
     */
    @GET("matches/{id}")
    suspend fun getMatchDetails(@Path("id") id: String): MatchDetailsDto

    /**
     * Collects a paginated subset of match records optimized for lazy-loading lists and mediators.
     *
     * @param page The index position of the targeted data page.
     * @param limit The total number of items requested to return inside the payload array.
     */
    @GET("matches")
    suspend fun fetchPagedMatches(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): List<MatchDto>
}
