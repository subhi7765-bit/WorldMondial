package sa.mondial.world.core.network.api

import retrofit2.http.GET
import retrofit2.http.Query
import sa.mondial.world.core.network.dto.NewsResponseDto

/**
 * Retrofit API service contract handling external communication pipelines for localized news feeds.
 * Integrates search configurations, regional filters, and publication timelines.
 */
interface NewsApiService {

    /**
     * Queries and fetches international sports news stories and global announcements.
     * Note: Adjusted endpoints to safely build relative to the core network path specifications.
     *
     * @param query The specific filter keyword phrase (Defaults to "world cup").
     * @param language Explicit language target array filter (Defaults to Arabic and English support).
     * @param sortBy The classification sorting order parameter (Defaults to chronological release "publishedAt").
     * @return A mapped type-safe data transfer object [NewsResponseDto] wrapping the response payload.
     */
    @GET("news")
    suspend fun getNews(
        @Query("q") query: String = "world cup",
        @Query("language") language: String = "ar,en",
        @Query("sortBy") sortBy: String = "publishedAt"
    ): NewsResponseDto
}
