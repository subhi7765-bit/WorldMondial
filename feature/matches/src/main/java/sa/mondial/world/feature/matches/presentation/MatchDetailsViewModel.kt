package sa.mondial.world.feature.matches.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import sa.mondial.world.core.common.ErrorHandler
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.data.LocalizationManager
import sa.mondial.world.core.domain.MatchDetails
import sa.mondial.world.feature.matches.domain.GetMatchDetailsUseCase
import sa.mondial.world.core.analytics.AnalyticsTracker
import sa.mondial.world.core.notifications.NotificationEventManager
import timber.log.Timber
import java.time.Instant
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class MatchDetailsViewModel @Inject constructor(
    private val getMatchDetailsUseCase: GetMatchDetailsUseCase,
    private val localizationManager: LocalizationManager,
    private val analyticsTracker: AnalyticsTracker,
    private val notificationEventManager: NotificationEventManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: String = savedStateHandle.get<String>("matchId") ?: "match-01"

    private val _uiState = MutableStateFlow<UiState<MatchDetails>>(UiState.Loading)
    val uiState: StateFlow<UiState<MatchDetails>> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    val currentLanguage = localizationManager.currentLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ar"
    )

    private val _isOfflineWithCache = MutableStateFlow(false)
    val isOfflineWithCache: StateFlow<Boolean> = _isOfflineWithCache.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<String?>(null)
    val lastSyncTimestamp: StateFlow<String?> = _lastSyncTimestamp.asStateFlow()

    init {
        analyticsTracker.logScreenView("MatchDetailsScreen")
        loadDetails()
        observeForegroundLiveEvents()
    }

    private fun observeForegroundLiveEvents() {
        viewModelScope.launch {
            notificationEventManager.foregroundEvents
                .filter { it.matchId == matchId }
                .collect { payload ->
                    _snackbarEvent.emit(payload.eventText)
                    val currentState = _uiState.value
                    if (currentState is UiState.Success) {
                        val updated = currentState.data.copy(
                            homeScore = payload.homeScore,
                            awayScore = payload.awayScore
                        )
                        _uiState.value = UiState.Success(updated)
                    }
                }
        }
    }

    fun loadDetails() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _isOfflineWithCache.value = false
            try {
                val details = getMatchDetailsUseCase(matchId)
                
                val currentMillis = System.currentTimeMillis()
                val age = currentMillis - details.lastSyncTimeMs
                val isStaleCacheFallback = age > 60000L

                if (isStaleCacheFallback && details.lastSyncTimeMs > 0L) {
                    _isOfflineWithCache.value = true
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .withZone(ZoneId.systemDefault())
                    _lastSyncTimestamp.value = formatter.format(Instant.ofEpochMilli(details.lastSyncTimeMs))
                    analyticsTracker.logEvent("match_details_offline_cache_loaded", mapOf("matchId" to matchId))
                } else {
                    _isOfflineWithCache.value = false
                    _lastSyncTimestamp.value = null
                    analyticsTracker.logEvent("match_details_loaded", mapOf("matchId" to matchId))
                }

                _uiState.value = UiState.Success(details)
                Timber.i("MatchDetailsViewModel: Successfully retrieved details for $matchId")
            } catch (throwable: Throwable) {
                val isAr = currentLanguage.value == "ar"
                val displayMsg = ErrorHandler.getLocalisedMessage(throwable, isAr)
                _uiState.value = UiState.Error(throwable, displayMsg)
                analyticsTracker.logError("Failed to retrieve details for $matchId: ${throwable.message}", false)
                Timber.e(throwable, "MatchDetailsViewModel: Failed to retrieve details for $matchId")
            }
        }
    }
}
