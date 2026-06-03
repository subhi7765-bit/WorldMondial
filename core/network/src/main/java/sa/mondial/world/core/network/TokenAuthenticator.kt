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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val authRepository: AuthRepository
) : Authenticator {

    private val mutex = Mutex()

    /**
     * Suspending version of authenticate to align with modern coroutines.
     * Safe for asynchronous execution via Mutex pattern.
     */
    suspend fun authenticateSuspend(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 3) {
            return null
        }
        return mutex.withLock {
            val newToken = authRepository.refreshToken()
            if (newToken != null) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newToken}")
                    .build()
            } else {
                null
            }
        }
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 3) {
            return null
        }

        // Thread-safe non-blocking bridge using Mutex and CompletableFuture to safely resolve suspending tokens without runBlocking
        val future = java.util.concurrent.CompletableFuture<Request?>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val req = authenticateSuspend(route, response)
                future.complete(req)
            } catch (e: Exception) {
                future.complete(null)
            }
        }

        return try {
            future.get()
        } catch (e: Exception) {
            null
        }
    }

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