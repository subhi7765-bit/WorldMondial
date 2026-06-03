package sa.mondial.world.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import sa.mondial.world.core.domain.News

@Entity(tableName = "news")
data class NewsEntity(
    @PrimaryKey val id: String,
    val titleAr: String,
    val titleEn: String,
    val bodyAr: String,
    val bodyEn: String,
    val bannerImageUrl: String?,
    val publicationDate: String,
    val categoryAr: String?,
    val categoryEn: String?,
    val readTimeAr: String?,
    val readTimeEn: String?,
    val isTrending: Boolean,
    val lastUpdated: Long
) {
    fun toDomainModel(): News {
        val parsedTime = try { Instant.parse(publicationDate) } catch (e: Exception) { Instant.now() }
        return News(
            id = id,
            titleAr = titleAr,
            titleEn = titleEn,
            bodyAr = bodyAr,
            bodyEn = bodyEn,
            bannerImageUrl = bannerImageUrl ?: "",
            publicationDate = parsedTime,
            categoryAr = categoryAr ?: "تغطية عاجلة",
            categoryEn = categoryEn ?: "Flash News",
            readTimeAr = readTimeAr ?: "قراءة في 4 دقائق",
            readTimeEn = readTimeEn ?: "4 min read",
            isTrending = isTrending
        )
    }
}