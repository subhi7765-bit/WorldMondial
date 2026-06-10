package sa.mondial.world.feature.matches.domain

import kotlinx.coroutines.flow.Flow
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.domain.Match
import sa.mondial.world.feature.matches.data.MatchesRepository
import javax.inject.Inject

class GetMatchesUseCase @Inject constructor(
    private val matchesRepository: MatchesRepository
) {
    operator fun invoke(forceRefresh: Boolean = false): Flow<Result<List<Match>>> {
        return matchesRepository.getStreamedMatches(forceRefresh)
    }
}
