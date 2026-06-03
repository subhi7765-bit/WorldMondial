package sa.mondial.world.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_remote_keys")
data class MatchRemoteKeys(
    @PrimaryKey val matchId: String,
    val prevKey: Int?,
    val nextKey: Int?
)