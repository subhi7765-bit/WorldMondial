package sa.mondial.world.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import sa.mondial.world.core.database.entity.MatchEntity
import sa.mondial.world.core.database.entity.MatchDetailsEntity

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY utcTime")
    fun getCachedMatchesFlow(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY utcTime LIMIT :limit OFFSET :offset")
    suspend fun getPagedMatches(limit: Int, offset: Int): List<MatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchesBatch(entities: List<MatchEntity>)

    @Query("DELETE FROM matches")
    suspend fun deleteAllMatches()

    @Transaction
    suspend fun refreshAllMatches(entities: List<MatchEntity>) {
        deleteAllMatches()
        val updated = entities.map { it.copy(lastUpdated = System.currentTimeMillis()) }
        insertMatchesBatch(updated)
    }

    @Query("SELECT * FROM match_details WHERE id = :matchId")
    suspend fun getMatchDetails(matchId: String): MatchDetailsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatchDetails(entity: MatchDetailsEntity)
}
