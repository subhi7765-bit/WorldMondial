package sa.mondial.world.core.common

sealed class AppError : Throwable() {
    data class Network(val code: Int, val message: String?) : AppError()
    data class Database(val message: String, override val cause: Throwable?) : AppError()
    data class Business(val messageAr: String, val messageEn: String) : AppError()
    data class Unknown(override val cause: Throwable?) : AppError()

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
                if (isArabic) "حدث خطأ غير معروف: ${cause?.localizedMessage ?: "مجهول"}"
                else "An unknown error occurred: ${cause?.localizedMessage ?: "Unknown"}"
            }
        }
    }
}