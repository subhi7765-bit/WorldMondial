package sa.mondial.world.core.common

/**
 * Strict production-grade sealed interface representing unidirectional layout state.
 * Fully type-safe and optimized for Jetpack Compose recomposition boundaries.
 */
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    
    data class Success<out T>(
        val data: T,
        val isFromCache: Boolean = false,
        val offlineBannerMessage: String? = null
    ) : UiState<T>
    
    data class Error(
        val throwable: Throwable,
        val displayMessage: String
    ) : UiState<Nothing>
    
    object Empty : UiState<Nothing>
}