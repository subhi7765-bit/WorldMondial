package sa.mondial.world.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import sa.mondial.world.core.database.entity.MatchRemoteKeys

@Dao
interface MatchRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<MatchRemoteKeys>)

    @Query("SELECT * FROM match_remote_keys WHERE matchId = :matchId")
    suspend fun getRemoteKeysForMatchId(matchId: String): MatchRemoteKeys?

    @Query("DELETE FROM match_remote_keys")
    suspend fun clearRemoteKeys()
}