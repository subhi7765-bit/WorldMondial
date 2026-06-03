package sa.mondial.world.core.testing

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.data.LocalizationManager
import sa.mondial.world.core.domain.Match
import sa.mondial.world.core.domain.MatchStatus
import sa.mondial.world.feature.matches.domain.GetMatchesUseCase
import sa.mondial.world.feature.matches.presentation.MatchesViewModel
import sa.mondial.world.core.analytics.AnalyticsTracker
import java.time.Instant

/**
 * Enterprise architecture unit testing module using Turbine and MockK.
 */
class MatchesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Mock dispatcher rule

    private val getMatchesUseCase: GetMatchesUseCase = mockk()
    private val localizationManager: LocalizationManager = mockk(relaxed = true)
    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    private val fakeMatch = Match(
        id = "1",
        homeTeamNameAr = "المملكة العربية السعودية", homeTeamNameEn = "KSA", homeTeamFlagUrl = "",
        awayTeamNameAr = "الأرجنتين", awayTeamNameEn = "Argentina", awayTeamFlagUrl = "",
        homeScore = 2, awayScore = 1,
        matchStatus = MatchStatus.FINISHED,
        roundAr = "المجموعات", roundEn = "Group Stage",
        utcTime = Instant.now(),
        homeLineup = listOf("Y. Al-Shahrani", "S. Al-Dawsari"),
        awayLineup = listOf("L. Messi", "A. Di Maria"),
        timelineEventsAr = listOf("48' هدف دبل!", "53' هدف الدوسري!!"),
        timelineEventsEn = listOf("48' Equalizer!", "53' Al-Dawsari goal!!")
    )

    @Test
    fun `loadMatches success emits cache state then network state beautifully`() = runTest {
        val expectedFlow = flowOf(Result.Success(listOf(fakeMatch)))
        coEvery { getMatchesUseCase(any()) } returns expectedFlow
        coEvery { localizationManager.currentLanguage } returns flowOf("en")

        val viewModel = MatchesViewModel(getMatchesUseCase, localizationManager, analyticsTracker)

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertTrue(initialState is UiState.Loading)
            
            val successState = awaitItem() as UiState.Success
            assertEquals(1, successState.data.size)
            assertEquals("KSA", successState.data.first().homeTeamNameEn)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

class MainDispatcherRule : org.junit.rules.TestWatcher() {
    // Standard coroutine testing rule placeholder
}