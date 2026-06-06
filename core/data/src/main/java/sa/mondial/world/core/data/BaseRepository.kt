package sa.mondial.world.core.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.common.AppError
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import timber.log.Timber
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject

/**
 * Base abstract repository orchestrating safe network and database transactions.
 * Enforces strict thread boundaries using an injected coroutine dispatcher and encapsulates
 * automated retry mechanisms with exponential backoff.
 */
abstract class BaseRepository(
    private val ioDispatcher: CoroutineDispatcher
) {

    // Fixed visibility modifier from protected to public to allow Dagger-Hilt field injection to compile smoothly
    @Inject
    lateinit var analyticsTracker: sa.mondial.world.core.analytics.AnalyticsTracker

    /**
     * Executes an asynchronous API or storage call within safe asynchronous IO bounds, catches any unhandled
     * downstream exceptions, logs analytical troubleshooting insights, and maps failures into an [AppError] wrapper.
     *
     * @param T The data type returned by the successful transaction block.
     * @param maxAttempts The total number of execution retries allowed before throwing a final failure wrapper.
     * @param initialDelayMillis The initial suspension period before executing the first retry block.
     * @param apiCall The suspending lambda containing the actual network execution block.
     * @return A wrapped [Result] containing either standard [Result.Success] or tracked [Result.Error].
     */
    suspend fun <T> safeApiCall(
        maxAttempts: Int = 3,
        initialDelayMillis: Long = 1000L,
        apiCall: suspend () -> T
    ): Result<T> {
        return withContext(ioDispatcher) {
            var attempts = 0
            var lastThrowable: Throwable? = null

            while (attempts < maxAttempts) {
                try {
                    return@withContext Result.Success(apiCall())
                } catch (throwable: Throwable) {
                    attempts++
                    lastThrowable = throwable

                    // Track critical server issues (401 Unauthorized / 503 Service Unavailable) to analytics safely
                    if (throwable is retrofit2.HttpException) {
                        val code = throwable.code()
                        if (code == 401 || code == 503) {
                            val requestUrl = throwable.response()?.raw()?.request?.url?.toString() ?: "unknown_endpoint"
                            try {
                                if (::analyticsTracker.isInitialized) {
                                    analyticsTracker.logEvent(
                                        "network_http_error",
                                        mapOf("code" to code.toString(), "endpoint" to requestUrl)
                                    )
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "safeApiCall: Failed logging network HTTP error to analytics")
                            }
                        }
                    } else if (throwable is sa.mondial.world.core.common.HttpException) {
                        val code = throwable.code
                        if (code == 401 || code == 503) {
                            try {
                                if (::analyticsTracker.isInitialized) {
                                    analyticsTracker.logEvent(
                                        "network_http_error",
                                        mapOf("code" to code.toString())
                                    )
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "safeApiCall: Failed logging network mock HTTP error to analytics")
                            }
                        }
                    }

                    // Apply exponential backoff multiplier delay if further evaluation attempts are permitted
                    if (attempts < maxAttempts) {
                        val backoffDelay = initialDelayMillis * (1 shl (attempts - 1))
                        Timber.w(throwable, "safeApiCall: Attempt $attempts failed. Retrying in ${backoffDelay}ms...")
                        delay(backoffDelay)
                    }
                }
            }

            // Exceeded maximum retry attempts; package transaction logs and report to Crashlytics
            val finalThrowable = lastThrowable ?: RuntimeException("Execution failure context missing")
            Timber.e(finalThrowable, "safeApiCall: Logging final exception context to Firebase Crashlytics")
            try {
                FirebaseCrashlytics.getInstance().recordException(finalThrowable)
            } catch (e: Exception) {
                Timber.e(e, "Firebase Crashlytics framework is not initialized yet")
            }

            // Map standard system failures into structured AppError invariants
            val appError = when (finalThrowable) {
                is UnknownHostException, is ConnectException, is SocketTimeoutException -> {
                    AppError.Network(0, finalThrowable.localizedMessage)
                }
                is retrofit2.HttpException -> {
                    AppError.Network(finalThrowable.code(), finalThrowable.localizedMessage)
                }
                is sa.mondial.world.core.common.HttpException -> {
                    AppError.Network(finalThrowable.code, finalThrowable.localizedMessage)
                }
                is IOException -> {
                    AppError.Database("Local storage or API IO transaction conversion failure", finalThrowable)
                }
                is AppError -> finalThrowable
                else -> AppError.Unknown(finalThrowable)
            }
            Result.Error(appError)
        }
    }
}
