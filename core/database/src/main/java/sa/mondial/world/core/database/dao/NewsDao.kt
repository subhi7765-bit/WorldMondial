package sa.mondial.world.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import sa.mondial.world.core.database.entity.NewsEntity

@Dao
interface NewsDao {
    @Query("SELECT * FROM news ORDER BY publicationDate DESC")
    fun getCachedNewsFlow(): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<NewsEntity>)

    @Query("DELETE FROM news")
    suspend fun deleteAllNews()

    @Transaction
    suspend fun refreshAllNews(news: List<NewsEntity>) {
        deleteAllNews()
        insertAll(news)
    }
}