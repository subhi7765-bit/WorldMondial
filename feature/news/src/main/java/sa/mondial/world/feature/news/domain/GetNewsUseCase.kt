package sa.mondial.world.feature.news.domain

import kotlinx.coroutines.flow.Flow
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.domain.News
import sa.mondial.world.feature.news.data.NewsRepository
import javax.inject.Inject

class GetNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke(forceRefresh: Boolean = false): Flow<Result<List<News>>> {
        return newsRepository.getStreamedNews(forceRefresh)
    }
}