package sa.mondial.world.core.common

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Enterprise Centralized Error Handler to categorize Throwables mapping to user-friendly messages.
 * Respects localization structures (Arabic vs English translations).
 */
object ErrorHandler {

    fun getLocalisedMessage(throwable: Throwable, isArabic: Boolean): String {
        if (throwable is AppError) {
            return throwable.localizedDescription(isArabic)
        }
        return when (throwable) {
            is UnknownHostException, is ConnectException -> {
                if (isArabic) "تعذر الاتصال بالخادم. يرجى التحقق من شبكة الانترنت."
                else "No internet connection. Please verify your data or wifi."
            }
            is SocketTimeoutException -> {
                if (isArabic) "انتهت مهلة طلب الاتصال. يرجى المحاولة لاحقاً."
                else "Connection timed out. Please try again later."
            }
            is IOException -> {
                if (isArabic) "فشل في تحميل البيانات الدورية للكاش."
                else "Data load error. Local storage or parsing issues."
            }
            is retrofit2.HttpException -> {
                val code = throwable.code()
                val message = when (code) {
                    401 -> if (isArabic) "غير مصرح لك بالوصول. يرجى تسجيل الدخول." else "Unauthorized. Please renew login."
                    404 -> if (isArabic) "المباراة أو الخبر غير متوفر حالياً." else "Mondial content not found."
                    in 500..599 -> if (isArabic) "خطأ فني في خادم المونديال." else "Mondial service is down (5xx Error)."
                    else -> if (isArabic) "حدث خطأ غير متوقع في جلب البيانات." else "An unexpected API error occurred."
                }
                AppError.Network(code, message).localizedDescription(isArabic)
            }
            is HttpException -> {
                when (throwable.code) {
                    401 -> if (isArabic) "غير مصرح لك بالوصول. يرجى تسجيل الدخول." else "Unauthorized. Please renew login."
                    404 -> if (isArabic) "المباراة أو الخبر غير متوفر حالياً." else "Mondial content not found."
                    in 500..599 -> if (isArabic) "خطأ فني في خادم المونديال." else "Mondial service is down (5xx Error)."
                    else -> if (isArabic) "حدث خطأ غير متوقع في جلب البيانات." else "An unexpected API error occurred."
                }
            }
            else -> {
                if (isArabic) "حدث خطأ غير متوقع: ${throwable.localizedMessage ?: "مجهول"}"
                else "System level fail: ${throwable.localizedMessage ?: "Unknown error"}"
            }
        }
    }
}

/**
 * Mock HttpException representing REST response failures.
 */
class HttpException(val code: Int) : RuntimeException("HTTP Server returned code $code")