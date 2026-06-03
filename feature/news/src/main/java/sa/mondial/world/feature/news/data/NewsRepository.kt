package sa.mondial.world.feature.news.data

import kotlinx.coroutines.flow.Flow
import sa.mondial.world.core.common.Result
import sa.mondial.world.core.domain.News

/**
 * Interface boundary governing RSS news feeds.
 */
interface NewsRepository {
    /**
     * Pulls latest Mondial news events with internal cache support if offline.
     */
    fun getStreamedNews(forceRefresh: Boolean): Flow<Result<List<News>>>
}