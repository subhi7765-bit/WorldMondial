package sa.mondial.world.core.common

import java.time.Instant
import java.time.format.DateTimeFormatter

object DateParser {
    fun parseToInstant(value: Any?): Instant {
        if (value == null) return Instant.now()
        return when (value) {
            is Long -> {
                try {
                    Instant.ofEpochMilli(value)
                } catch (e: Exception) {
                    Instant.now()
                }
            }
            is String -> {
                if (value.isBlank()) return Instant.now()
                try {
                    val longVal = value.toLongOrNull()
                    if (longVal != null) {
                        Instant.ofEpochMilli(longVal)
                    } else {
                        Instant.parse(value)
                    }
                } catch (e: Exception) {
                    try {
                        Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(value))
                    } catch (e2: Exception) {
                        Instant.now()
                    }
                }
            }
            else -> Instant.now()
        }
    }
}