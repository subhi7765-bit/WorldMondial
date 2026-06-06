package sa.mondial.world.feature.news.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.domain.News

/**
 * Main Composable representing the Mondial localized news feed screen.
 * Implements standard Unidirectional Data Flow using the clean Material 3 design pipeline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val isAr = language == "ar"

    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAr) "تطورات المونديال" else "Mondial News Flash") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->

        // Built-in stable native Material 3 pull-to-refresh structure wrapper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullToRefresh(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = { viewModel.loadMondialNews(forceRefresh = true) }
                )
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    ShimmerNewsLoader()
                }
                is UiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.data, key = { it.id }) { newsItem ->
                            NewsCard(
                                news = newsItem,
                                isAr = isAr,
                                onClick = { onNavigateToDetails(newsItem.id) }
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    NewsErrorState(message = state.displayMessage) {
                        viewModel.loadMondialNews(forceRefresh = true)
                    }
                }
                is UiState.Empty -> {
                    NewsEmptyState(isAr = isAr)
                }
            }

            // Clean, non-blocking standard indicator alignment
            PullToRefreshContainer(
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun NewsCard(
    news: News,
    isAr: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Localized Category Badge Widget
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isAr) news.categoryAr else news.categoryEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Localized Dynamic Title
            Text(
                text = if (isAr) news.titleAr else news.titleEn,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Row mapping read times and metadata flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) news.readTimeAr else news.readTimeEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (news.isTrending) {
                    Text(
                        text = if (isAr) "🔥 شائع" else "🔥 Trending",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerNewsLoader() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.15f))
            )
        }
    }
}

@Composable
fun NewsErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("Re-load News Feed")
        }
    }
}

@Composable
fun NewsEmptyState(isAr: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isAr) "لا توجد أية أخبار متاحة حالياً." else "No news flash available.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
