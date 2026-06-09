package sa.mondial.world.feature.news.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.domain.News

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
                title = { Text(if (isAr) "أخبار" else "News", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xE6111C24), 
                    titleContentColor = Color(0xFFD4AF37) 
                )
            )
        },
        containerColor = Color.Transparent, 
        modifier = modifier
    ) { innerPadding ->

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
                                onClick = { onNavigateToDetails(newsItem.url) }
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

            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                color = Color(0xFFD4AF37)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xCC18222C)), 
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .background(Color(0x33D4AF37), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isAr) news.categoryAr else news.categoryEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD4AF37) 
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isAr) news.titleAr else news.titleEn,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) news.readTimeAr else news.readTimeEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
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
                    .background(Color.White.copy(alpha = 0.08f))
            )
        }
    }
}

@Composable
fun NewsErrorState(message: String, onRetry: () -> Unit) {
    // Fixed Cleanly: Added full layout verticalScroll modifier token to permit live pull-to-refresh gestures even during network failures
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
        ) {
            Text("Re-load News Feed", color = Color(0xFF111C24), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NewsEmptyState(isAr: Boolean) {
    // Fixed Cleanly: Added full layout verticalScroll modifier token to permit live pull-to-refresh gestures even during news payload absences
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isAr) "لا توجد أية أخبار متاحة حالياً." else "No news flash available.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
