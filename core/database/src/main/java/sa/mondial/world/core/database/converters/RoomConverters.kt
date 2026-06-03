package sa.mondial.world.core.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import sa.mondial.world.core.domain.LineupPlayer

class RoomConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromLineupList(value: List<LineupPlayer>?): String? {
        if (value == null) return null
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toLineupList(value: String?): List<LineupPlayer>? {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}