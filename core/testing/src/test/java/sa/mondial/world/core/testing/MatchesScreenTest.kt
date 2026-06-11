package sa.mondial.world.core.testing

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import sa.mondial.world.core.analytics.AnalyticsTracker
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.domain.Match
import sa.mondial.world.feature.matches.presentation.MatchesScreen
import sa.mondial.world.feature.matches.presentation.MatchesViewModel
import sa.mondial.world.feature.matches.presentation.SelectedDay

class MatchesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val viewModel: MatchesViewModel = mockk(relaxed = true)

    @Test
    fun matchesScreen_displaysLoadingState_initially() {
        // Setup mock responses for the new standard UiState flow
        every { viewModel.uiState } returns MutableStateFlow(UiState.Loading)
        every { viewModel.currentLanguage } returns MutableStateFlow("en")
        every { viewModel.isRefreshing } returns MutableStateFlow(false)
        every { viewModel.selectedDay } returns MutableStateFlow(SelectedDay.TODAY)

        composeTestRule.setContent {
            MatchesScreen(
                viewModel = viewModel,
                onNavigateToDetails = { _ -> }
            )
        }

        // Verify the shimmer loader is displayed when in loading state
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }
}
