package sa.mondial.world.feature.matches.domain

import sa.mondial.world.core.domain.MatchDetails
import sa.mondial.world.feature.matches.data.MatchesRepository
import javax.inject.Inject

/**
 * Enterprise scope Clean Architecture UseCase retrieving single detailed match record.
 */
class GetMatchDetailsUseCase @Inject constructor(
    private val matchesRepository: MatchesRepository
) {
    suspend operator fun invoke(matchId: String): MatchDetails {
        return matchesRepository.getMatchDetails(matchId)
    }
}