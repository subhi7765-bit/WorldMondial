package sa.mondial.world.core.network

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modern thread-safe OkHttp [Authenticator] that orchestrates automated OAuth2 token expiration recovery.
 * Employs a asynchronous Kotlin [Mutex] barrier mechanism to ensure only a single concurrent network operation
 * requests a fresh token, gracefully preventing system-wide authorization race conditions.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val authRepository: AuthRepository
) : Authenticator {

    private val mutex = Mutex()

    /**
     * Suspending token evaluation logic designed to safely interface with core repository architectures.
     * Guarded by a mutual exclusion lock to handle multi-threaded token renewal requests sequentially.
     */
    suspend fun authenticateSuspend(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 3) {
            Timber.w("TokenAuthenticator: Aborting token refresh. Maximum authentication retry limit (3) exceeded.")
            return null
        }

        return mutex.withLock {
            Timber.i("TokenAuthenticator: Mutex locked. Requesting fresh authentication token from AuthRepository...")
            val newToken = authRepository.refreshToken()
            
            if (newToken != null) {
                Timber.i("TokenAuthenticator: Token refresh succeeded. Rebuilding signed request with fresh session headers.")
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } else {
                Timber.e("TokenAuthenticator: Token refresh failed. AuthRepository returned null credentials.")
                null
            }
        }
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 3) {
            return null
        }

        Timber.d("TokenAuthenticator: Intercepted 401 Unauthorized gateway response. Bridging synchronous network thread to coroutines.")
        
        // Non-blocking bridge leveraging a CompletableFuture container to safely execute suspending tokens without deadlocking standard OkHttp pipelines
        val future = CompletableFuture<Request?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val authenticatedRequest = authenticateSuspend(route, response)
                future.complete(authenticatedRequest)
            } catch (e: Exception) {
                Timber.e(e, "TokenAuthenticator: Asynchronous coroutine context authentication crashed.")
                future.complete(null)
            }
        }

        return try {
            future.get()
        } catch (e: Exception) {
            Timber.e(e, "TokenAuthenticator: Failed retrieving bridged request from CompletableFuture channel.")
            null
        }
    }

    /**
     * Recursively traverses historical network operations to extract the total number of consecutive 401 responses.
     */
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
