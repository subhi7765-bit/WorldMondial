package sa.mondial.world.feature.matches.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import sa.mondial.world.core.common.ErrorHandler
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.data.LocalizationManager
import sa.mondial.world.core.domain.Match
import sa.mondial.world.feature.matches.domain.GetMatchesUseCase
import sa.mondial.world.core.analytics.AnalyticsTracker
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class SelectedDay { YESTERDAY, TODAY, TOMORROW }

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val getMatchesUseCase: GetMatchesUseCase,
    private val localizationManager: LocalizationManager,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedDay = MutableStateFlow(SelectedDay.TODAY)
    val selectedDay: StateFlow<SelectedDay> = _selectedDay.asStateFlow()

    private val _forceRefreshTrigger = MutableStateFlow(false)

    val currentLanguage = localizationManager.currentLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ar"
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<List<Match>>> = _forceRefreshTrigger
        .flatMapLatest { force -> getMatchesUseCase(force) }
        .combine(_selectedDay) { result, day ->
            val isAr = currentLanguage.value == "ar"
            when (result) {
                is Result.Loading -> UiState.Loading
                is Result.Success -> {
                    _isRefreshing.value = false // إيقاف الدائرة بنجاح
                    val filteredMatches = result.data.filter { match ->
                        val matchLocalDate = match.utcTime.atZone(ZoneId.systemDefault()).toLocalDate()
                        val today = LocalDate.now(ZoneId.systemDefault())
                        val targetDate = when (day) {
                            SelectedDay.YESTERDAY -> today.minusDays(1)
                            SelectedDay.TODAY -> today
                            SelectedDay.TOMORROW -> today.plusDays(1)
                        }
                        matchLocalDate == targetDate
                    }
                    
                    if (filteredMatches.isEmpty()) UiState.Empty
                    else UiState.Success(filteredMatches, isFromCache = false)
                }
                is Result.Error -> {
                    _isRefreshing.value = false // إيقاف الدائرة عند الخطأ
                    val msg = ErrorHandler.getLocalisedMessage(result.exception, isAr)
                    UiState.Error(result.exception, msg)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    init {
        analyticsTracker.logScreenView("MatchesScreen")
    }

    fun selectDay(day: SelectedDay) {
        _selectedDay.value = day
    }

    fun loadMondialMatches(forceRefresh: Boolean) {
        if (forceRefresh) {
            _isRefreshing.value = true
        }
        _forceRefreshTrigger.value = forceRefresh
    }
}
