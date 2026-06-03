package sa.mondial.world.feature.matches.domain

import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.domain.Match
import sa.mondial.world.feature.matches.data.MatchesRepository
import javax.inject.Inject

/**
 * Domain UseCase representing functional logic for match querying.
 * Strictly decoupled from specific presentation frameworks.
 */
class GetMatchesUseCase @Inject constructor(
    private val matchesRepository: MatchesRepository
) {
    /**
     * traditional flow execution.
     */
    operator fun invoke(forceRefresh: Boolean = false): Flow<Result<List<Match>>> {
        return matchesRepository.getStreamedMatches(forceRefresh)
    }

    /**
     * Paginated Paging 3 flow execution.
     */
    fun getPaged(forceRefresh: Boolean = false): Flow<PagingData<Match>> {
        return matchesRepository.getPagedMatches(forceRefresh)
    }
}