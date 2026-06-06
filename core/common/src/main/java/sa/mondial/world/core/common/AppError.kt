package sa.mondial.world.core.common

/**
 * Sealed class representing all application-level errors.
 * Inherits from [Throwable] to fully integrate with standard exception handling mechanics,
 * ensuring proper tracking of crash logging and dependency injection error handling.
 */
sealed class AppError(
    override val message: String? = null,
    override val cause: Throwable? = null
) : Throwable(message, cause) {

    /**
     * Represents a network connectivity or HTTP API error.
     *
     * @property code The HTTP status code or custom network error identifier.
     * @property message The descriptive error message associated with the failure.
     */
    data class Network(
        val code: Int,
        override val message: String?
    ) : AppError(message = message)

    /**
     * Represents a local database or persistence layer execution failure.
     *
     * @property message The contextual description of the database operation that failed.
     * @property cause The underlying lower-level exception thrown by the database framework.
     */
    data class Database(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message = message, cause = cause)

    /**
     * Represents a domain-specific business logic validation error.
     * Contains localized strings for both Arabic and English support.
     */
    data class Business(
        val messageAr: String,
        val messageEn: String
    ) : AppError(message = messageEn)

    /**
     * Fallback error wrapper for unhandled or unexpected lower-level exceptions.
     */
    data class Unknown(
        override val cause: Throwable?
    ) : AppError(message = cause?.message, cause = cause)

    /**
     * Returns a localized description of the error based on the application's locale language setting.
     *
     * @param isArabic True if the application is currently configured in Arabic mode, false for English.
     * @return A descriptive user-facing message.
     */
    fun localizedDescription(isArabic: Boolean): String {
        return when (this) {
            is Network -> {
                if (isArabic) "خطأ في الشبكة (رمز: $code)"
                else "Network error (Code: $code)"
            }
            is Database -> {
                if (isArabic) "خطأ في قاعدة البيانات أثناء: $message"
                else "Database transaction error during: $message"
            }
            is Business -> {
                if (isArabic) messageAr else messageEn
            }
            is Unknown -> {
                val fallback = if (isArabic) "مجهول" else "Unknown"
                val systemMessage = cause?.localizedMessage ?: fallback
                if (isArabic) "حدث خطأ غير معروف: $systemMessage"
                else "An unknown error occurred: $systemMessage"
            }
        }
    }
}
