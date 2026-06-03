package sa.mondial.world.feature.news.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sa.mondial.world.core.common.ErrorHandler
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.data.LocalizationManager
import sa.mondial.world.core.domain.News
import sa.mondial.world.feature.news.domain.GetNewsUseCase
import sa.mondial.world.core.analytics.AnalyticsTracker
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val localizationManager: LocalizationManager,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<News>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<News>>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val currentLanguage = localizationManager.currentLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ar"
    )

    init {
        analyticsTracker.logScreenView("NewsFeedScreen")
        loadMondialNews(forceRefresh = false)
    }

    fun loadMondialNews(forceRefresh: Boolean) {
        viewModelScope.launch {
            if (forceRefresh) {
                _isRefreshing.value = true
                analyticsTracker.logEvent("news_pull_to_refresh", mapOf("force" to "true"))
            }

            getNewsUseCase(forceRefresh)
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
                            analyticsTracker.logError("News loading failed: ${result.exception.message}", false)
                            Timber.e(result.exception, "NewsViewModel: Load failed.")
                            
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