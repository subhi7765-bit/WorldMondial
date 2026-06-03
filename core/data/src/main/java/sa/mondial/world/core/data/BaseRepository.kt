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
 * Base abstract class ensuring secure network parsing with specific thread enforcement.
 */
abstract class BaseRepository(
    private val ioDispatcher: CoroutineDispatcher
) {
    @Inject
    protected lateinit var analyticsTracker: sa.mondial.world.core.analytics.AnalyticsTracker

    /**
     * Executes safe coroutine transaction across Io bounds with internal catch pipeline mapping to AppErrors.
     * Implements configurable retry attempts with customizable initial delay and exponential backoff.
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

                    // Catch standard HTTP 401 and 503 errors and generate troubleshooting events
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

                    if (attempts < maxAttempts) {
                        val backoffDelay = initialDelayMillis * (1 shl (attempts - 1))
                        Timber.w(throwable, "safeApiCall: Attempt $attempts failed. Retrying in ${backoffDelay}ms...")
                        delay(backoffDelay)
                    }
                }
            }

            val finalThrowable = lastThrowable ?: RuntimeException("Execution failure")
            Timber.e(finalThrowable, "safeApiCall: Logging exception context to Firebase Crashlytics")
            try {
                FirebaseCrashlytics.getInstance().recordException(finalThrowable)
            } catch (e: Exception) {
                Timber.e(e, "Firebase Crashlytics not initialized yet")
            }

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
                    AppError.Database("Local storage or API IO transaction", finalThrowable)
                }
                is AppError -> finalThrowable
                else -> AppError.Unknown(finalThrowable)
            }
            Result.Error(appError)
        }
    }
}