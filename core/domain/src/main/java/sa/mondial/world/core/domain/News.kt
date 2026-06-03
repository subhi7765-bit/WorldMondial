package sa.mondial.world.core.domain

import java.time.Instant

/**
 * Immutable Domain Model representing Mondial News Flash.
 */
data class News(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val bodyAr: String,
    val bodyEn: String,
    val bannerImageUrl: String,
    val publicationDate: Instant,
    val categoryAr: String,
    val categoryEn: String,
    val readTimeAr: String,
    val readTimeEn: String,
    val isTrending: Boolean
)