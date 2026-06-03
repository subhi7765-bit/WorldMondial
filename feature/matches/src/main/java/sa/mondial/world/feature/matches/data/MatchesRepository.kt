package sa.mondial.world.feature.matches.data

import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.domain.Match

/**
 * Repository interface following Clean Architecture boundaries.
 * Coordinates traditional cache-first visual streams alongside Paging 3 queries.
 */
interface MatchesRepository {
    
    /**
     * Fetches Mondial Matches applying strict Cache-Then-Network policy flow.
     * Emits Database cache immediately, executing parallel network fetch to update Room.
     */
    fun getStreamedMatches(forceRefresh: Boolean): Flow<Result<List<Match>>>

    /**
     * Provides an enterprise Paging 3 pipeline to seamlessly fetch pages of match results.
     */
    fun getPagedMatches(forceRefresh: Boolean): Flow<PagingData<Match>>

    /**
     * Retrieves the high-production quality detailed statistics, timeline, and formations
     * for a specific Mondial Match.
     */
    suspend fun getMatchDetails(matchId: String): sa.mondial.world.core.domain.MatchDetails
}