package sa.mondial.world.core.network.dto

import kotlinx.serialization.Serializable
import sa.mondial.world.core.database.entity.NewsEntity

@Serializable
data class NewsSourceDto(
    val id: String? = null,
    val name: String? = null
)

@Serializable
data class NewsArticleDto(
    val source: NewsSourceDto? = null,
    val author: String? = null,
    val title: String? = null,
    val description: String? = null,
    val url: String? = null,
    val urlToImage: String? = null,
    val publishedAt: String? = null,
    val content: String? = null
) {
    fun toDatabaseEntity(): NewsEntity {
        // Fixed Cleanly: Saved the actual secure destination web URL directly as the primary identifier instead of a destructive hashCode integer to preserve intent launching
        val cleanUrl = url ?: "https://newsapi.org"
        val t = title ?: "Mondial News"
        val desc = description ?: content ?: "Active updates from World Mondial match groups..."
        
        val isTitleAr = t.any { it.code in 0x0600..0x06FF }
        val titleAr = if (isTitleAr) t else "مقال: $t"
        val titleEn = if (isTitleAr) "Article: $t" else t
        
        val bodyAr = if (isTitleAr) desc else "تفاصيل وتطورات المقال الإخباري مع تغطية حية من فريق المونديال. $desc"
        val bodyEn = if (isTitleAr) "Official Mondial reporting and coverage. $desc" else desc

        val pubAt = publishedAt ?: java.time.Instant.now().toString()

        return NewsEntity(
            id = cleanUrl,
            titleAr = titleAr,
            titleEn = titleEn,
            bodyAr = bodyAr,
            bodyEn = bodyEn,
            bannerImageUrl = urlToImage,
            publicationDate = pubAt,
            categoryAr = "تغطية عاجلة",
            categoryEn = "Flash News",
            readTimeAr = "قراءة في 4 دقائق",
            readTimeEn = "4 min read",
            isTrending = true,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

@Serializable
data class NewsResponseDto(
    val status: String,
    val totalResults: Int,
    val articles: List<NewsArticleDto>
)
