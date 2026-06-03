package sa.mondial.world.core.testing

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import androidx.paging.PagingData
import androidx.paging.LoadState
import androidx.paging.LoadStates
import org.junit.Rule
import org.junit.Test
import sa.mondial.world.core.analytics.AnalyticsTracker
import sa.mondial.world.feature.matches.presentation.MatchesScreen
import sa.mondial.world.feature.matches.presentation.MatchesViewModel

class MatchesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val viewModel: MatchesViewModel = mockk(relaxed = true)

    @Test
    fun matchesScreen_displaysLoadingState_initially() {
        val loadingPagingData = PagingData.empty<sa.mondial.world.core.domain.Match>(
            sourceLoadStates = LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false)
            )
        )
        every { viewModel.pagedMatchesFlow } returns flowOf(loadingPagingData)
        every { viewModel.currentLanguage } returns MutableStateFlow("en")
        every { viewModel.isRefreshing } returns MutableStateFlow(false)

        composeTestRule.setContent {
            MatchesScreen(
                viewModel = viewModel,
                onNavigateToDetails = { _ -> }
            )
        }

        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }
}