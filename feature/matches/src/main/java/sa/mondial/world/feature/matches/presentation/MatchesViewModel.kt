package sa.mondial.world.feature.matches.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sa.mondial.world.core.common.ErrorHandler
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.data.LocalizationManager
import sa.mondial.world.core.domain.Match
import sa.mondial.world.feature.matches.domain.GetMatchesUseCase
import sa.mondial.world.core.analytics.AnalyticsTracker
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val getMatchesUseCase: GetMatchesUseCase,
    private val localizationManager: LocalizationManager,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Match>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Match>>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Stateful trigger for Paging 3 flow
    private val _forceRefreshState = MutableStateFlow(false)

    // Advanced Paging 3 flow, cached withinviewModelScope ensuring flow survival during rotation
    val pagedMatchesFlow: Flow<PagingData<Match>> = _forceRefreshState
        .flatMapLatest { force ->
            getMatchesUseCase.getPaged(force)
        }
        .cachedIn(viewModelScope)

    val currentLanguage = localizationManager.currentLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ar"
    )

    init {
        analyticsTracker.logScreenView("MatchesScreen")
        loadMondialMatches(forceRefresh = false)
    }

    fun loadMondialMatches(forceRefresh: Boolean) {
        viewModelScope.launch {
            if (forceRefresh) {
                _isRefreshing.value = true
                _forceRefreshState.value = true
                analyticsTracker.logEvent("matches_pull_to_refresh", mapOf("force" to "true"))
            }
            
            getMatchesUseCase(forceRefresh)
                .collect { result ->
                    val isAr = currentLanguage.value == "ar"
                    when (result) {
                        is Result.Loading -> {
                            if (!forceRefresh) _uiState.value = UiState.Loading
                        }
                        is Result.Success -> {
                            _isRefreshing.value = false
                            if (result.data.isEmpty()) {
                                _uiState.value = UiState.Empty
                            } else {
                                _uiState.value = UiState.Success(
                                    data = result.data,
                                    isFromCache = !forceRefresh
                                )
                            }
                        }
                        is Result.Error -> {
                            _isRefreshing.value = false
                            val message = ErrorHandler.getLocalisedMessage(result.exception, isAr)
                            analyticsTracker.logError("Matches sync failed: ${result.exception.message}", false)
                            Timber.e(result.exception, "MatchesViewModel: Sync failed.")
                            
                            val currentState = _uiState.value
                            if (currentState is UiState.Success) {
                                _uiState.value = currentState.copy(offlineBannerMessage = message)
                            } else {
                                _uiState.value = UiState.Error(result.exception, message)
                            }
                        }
                    }
                }
        }
    }
}