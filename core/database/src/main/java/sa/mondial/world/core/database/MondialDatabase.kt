package sa.mondial.world.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import sa.mondial.world.core.database.converters.RoomConverters
import sa.mondial.world.core.database.dao.MatchDao
import sa.mondial.world.core.database.dao.MatchRemoteKeysDao
import sa.mondial.world.core.database.dao.NewsDao
import sa.mondial.world.core.database.entity.MatchEntity
import sa.mondial.world.core.database.entity.MatchDetailsEntity
import sa.mondial.world.core.database.entity.MatchRemoteKeys
import sa.mondial.world.core.database.entity.NewsEntity

@Database(
    entities = [MatchEntity::class, MatchDetailsEntity::class, NewsEntity::class, MatchRemoteKeys::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class MondialDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun newsDao(): NewsDao
    abstract fun matchRemoteKeysDao(): MatchRemoteKeysDao
}