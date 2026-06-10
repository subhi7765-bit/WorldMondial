package sa.mondial.world.core.network.api

import retrofit2.http.GET
import retrofit2.http.Query
import sa.mondial.world.core.network.dto.NewsResponseDto

interface NewsApiService {

    @GET("everything")
    suspend fun getNews(
        @Query("q") query: String = "football OR كورة OR كأس العالم",
        @Query("language") language: String = "ar",
        @Query("sortBy") sortBy: String = "publishedAt"
    ): NewsResponseDto
}
