package sa.mondial.world.feature.matches.data

import kotlinx.coroutines.flow.Flow
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.domain.Match
import sa.mondial.world.core.domain.MatchDetails

interface MatchesRepository {
    fun getStreamedMatches(forceRefresh: Boolean): Flow<Result<List<Match>>>
    suspend fun getMatchDetails(matchId: String): MatchDetails
}
