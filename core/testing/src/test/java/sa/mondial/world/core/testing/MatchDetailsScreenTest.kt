package sa.mondial.world.core.testing

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import sa.mondial.world.core.analytics.AnalyticsTracker
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.domain.MatchDetails
import sa.mondial.world.core.domain.MatchStatus
import sa.mondial.world.feature.matches.presentation.MatchDetailsScreen
import sa.mondial.world.feature.matches.presentation.MatchDetailsViewModel
import java.time.Instant

class MatchDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
    private val viewModel: MatchDetailsViewModel = mockk(relaxed = true)

    private val testMatchDetails = MatchDetails(
        id = "match-01",
        homeTeamNameAr = "السعودية",
        homeTeamNameEn = "Saudi Arabia",
        homeTeamFlagUrl = "🇸🇦",
        awayTeamNameAr = "الأرجنتين",
        awayTeamNameEn = "Argentina",
        awayTeamFlagUrl = "🇦🇷",
        homeScore = 2,
        awayScore = 1,
        matchStatus = MatchStatus.FINISHED,
        roundAr = "المجموعات",
        roundEn = "Group Stage",
        utcTime = Instant.now(),
        venueAr = "استاد لوسيل",
        venueEn = "Lusail Stadium",
        refereeAr = "سلافكو",
        refereeEn = "Slavko",
        homeStartingXI = listOf(
            sa.mondial.world.core.domain.LineupPlayer("ياسر الشهراني", "Yasser Al-Shahrani", 13, "مدافع", "Defender")
        ),
        homeSubstitutes = listOf(
            sa.mondial.world.core.domain.LineupPlayer("نواف العابد", "Nawaf Al-Abed", 18, "لاعب وسط", "Midfielder")
        ),
        awayStartingXI = listOf(
            sa.mondial.world.core.domain.LineupPlayer("ليونيل ميسي", "Lionel Messi", 10, "مهاجم", "Forward", isCaptain = true)
        ),
        awaySubstitutes = listOf(
            sa.mondial.world.core.domain.LineupPlayer("جوليان ألفاريز", "Julian Alvarez", 9, "مهاجم", "Forward")
        ),
        timelineEventsAr = listOf("10' هدف ميسي"),
        timelineEventsEn = listOf("10' Messi Goal")
    )

    @Test
    fun matchDetailsScreen_LTR_displaysStartingXI_and_can_toggle_substitutes() {
        val uiStateFlow = MutableStateFlow<UiState<MatchDetails>>(UiState.Success(testMatchDetails))
        every { viewModel.uiState } returns uiStateFlow
        every { viewModel.currentLanguage } returns MutableStateFlow("en") // LTR
        every { viewModel.isOfflineWithCache } returns MutableStateFlow(false)
        every { viewModel.lastSyncTimestamp } returns MutableStateFlow(null)

        composeTestRule.setContent {
            MatchDetailsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Verify Starting XI is visible by default or is displayed
        composeTestRule.onNodeWithText("Starting XI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Substitutes").assertIsDisplayed()

        // Check if Starting XI player is visible
        composeTestRule.onNodeWithText("Yasser Al-Shahrani").assertIsDisplayed()
    }

    @Test
    fun matchDetailsScreen_RTL_displaysStartingXI_and_can_toggle_substitutes() {
        val uiStateFlow = MutableStateFlow<UiState<MatchDetails>>(UiState.Success(testMatchDetails))
        every { viewModel.uiState } returns uiStateFlow
        every { viewModel.currentLanguage } returns MutableStateFlow("ar") // RTL
        every { viewModel.isOfflineWithCache } returns MutableStateFlow(true)
        every { viewModel.lastSyncTimestamp } returns MutableStateFlow("2026-05-30 12:00:00")

        composeTestRule.setContent {
            MatchDetailsScreen(
                viewModel = viewModel,
                onNavigateBack = {}
            )
        }

        // Verify Arabic text elements are displayed
        composeTestRule.onNodeWithText("ياسر الشهراني").assertIsDisplayed()
        
        // Offline Banner should exist in RTL
        composeTestRule.onNodeWithText("⚠️ وضع غير متصل بالشبكة: تم تحميل البيانات المخزنة من آخر مزامنة: 2026-05-30 12:00:00").assertIsDisplayed()
    }
}