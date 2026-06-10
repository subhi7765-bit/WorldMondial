package sa.mondial.world.feature.matches.data

import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.domain.Match

interface MatchesRepository {
    fun getStreamedMatches(forceRefresh: Boolean): Flow<Result<List<Match>>>
    
    // THE FIX: Added datePrefix parameter
    fun getPagedMatches(forceRefresh: Boolean, datePrefix: String): Flow<PagingData<Match>>

    suspend fun getMatchDetails(matchId: String): sa.mondial.world.core.domain.MatchDetails
}
