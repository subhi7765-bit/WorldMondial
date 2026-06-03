package sa.mondial.world.core.ui

import android.content.Context
import android.os.Build

sealed class WeatherCondition {
    object SUNNY : WeatherCondition()
    object CLOUDY : WeatherCondition()
    object RAIN : WeatherCondition()
}

/**
 * Extension function to retrieve localized weather descriptions.
 * Maps to string resources (mocked / resolved through language configuration).
 */
fun WeatherCondition.getDisplayString(context: Context): String {
    val localeLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales[0].language
    } else {
        @Suppress("DEPRECATION")
        context.resources.configuration.locale.language
    }
    val isAr = localeLanguage == "ar"
    return when (this) {
        WeatherCondition.SUNNY -> if (isAr) "مشمس ولطيف" else "Sunny & Clear"
        WeatherCondition.CLOUDY -> if (isAr) "غيوم متفرقة" else "Partly Cloudy"
        WeatherCondition.RAIN -> if (isAr) "أمطار خفيفة" else "Light Rain"
    }
}