package sa.mondial.world.feature.matches.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.rememberShimmer
import kotlinx.coroutines.delay
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.domain.Match
import sa.mondial.world.core.domain.MatchStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullToRefreshState()
    Box(
        modifier = modifier.pullToRefresh(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        )
    ) {
        content()
        PullToRefreshContainer(
            state = state,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    SwipeToRefresh(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
    viewModel: MatchesViewModel,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isAr = language == "ar"
    
    val listState = rememberLazyListState()
    
    // Paging 3 Collection bindings (Lazy List integrations)
    val pagedMatches = viewModel.pagedMatchesFlow.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAr) "مباريات المونديال الذكية" else "Mondial Paged Matches") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->

        // Unification of Swipe-to-refresh pull structures
        SwipeToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = { 
                viewModel.loadMondialMatches(forceRefresh = true)
                pagedMatches.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Check loading states in Paging list
            when {
                pagedMatches.loadState.refresh is LoadState.Loading -> {
                    ShimmerMatchListLoader(modifier = Modifier.testTag("loading_indicator"))
                }
                pagedMatches.loadState.refresh is LoadState.Error -> {
                    val error = (pagedMatches.loadState.refresh as LoadState.Error).error
                    MatchErrorState(
                        message = error.localizedMessage ?: "Paging Error",
                        onRetry = { pagedMatches.retry() }
                    )
                }
                pagedMatches.itemCount == 0 -> {
                    MatchEmptyState(isAr = isAr)
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(pagedMatches.itemCount) { index ->
                            pagedMatches[index]?.let { match ->
                                MatchCard(
                                    match = match,
                                    isAr = isAr,
                                    onClick = { onNavigateToDetails(match.id) }
                                )
                            }
                        }

                        // Appending loading state indicating next page loads
                        if (pagedMatches.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(strokeWidth = 3.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    isAr: Boolean,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category round & local timestamp conversion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) match.roundAr else match.roundEn,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Pure local timezone formatting
                val formattedTime = remember(match.utcTime) {
                    match.utcTime
                        .atZone(ZoneId.systemDefault())
                        .format(
                            DateTimeFormatter
                                .ofLocalizedDateTime(FormatStyle.SHORT)
                                .withLocale(if (isAr) Locale("ar") else Locale.ENGLISH)
                        )
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual flags layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) match.homeTeamNameAr else match.homeTeamNameEn,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = match.homeScore?.toString() ?: "-",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(" : ", modifier = Modifier.padding(horizontal = 6.dp))
                    Text(
                        text = match.awayScore?.toString() ?: "-",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text = if (isAr) match.awayTeamNameAr else match.awayTeamNameEn,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                    color = Color.Unspecified
                )
            }

            // Countdown timer for UPCOMING matches
            if (match.matchStatus == MatchStatus.UPCOMING) {
                val remainingMillis by produceState(
                    initialValue = match.utcTime.toEpochMilli() - System.currentTimeMillis(),
                    key1 = match.utcTime
                ) {
                    while (value > 0) {
                        val now = System.currentTimeMillis()
                        val target = match.utcTime.toEpochMilli()
                        val nextDelay = if (target > now) target - now else 0
                        if (nextDelay > 0) {
                            delay(nextDelay)
                            value = target - System.currentTimeMillis()
                        } else {
                            value = 0
                        }
                        delay(1000L)
                    }
                }

                if (remainingMillis > 0) {
                    val totalSeconds = remainingMillis / 1000
                    val seconds = totalSeconds % 60
                    val totalMinutes = totalSeconds / 60
                    val minutes = totalMinutes % 60
                    val totalHours = totalMinutes / 60
                    val hours = totalHours % 24
                    val days = totalHours / 24

                    val countdownStr = "${days}d ${hours}h ${minutes}m ${seconds}s"

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isAr) "يبدأ بعد: $countdownStr" else "Starts in: $countdownStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Expanded Official XI Lineup
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            ) {
                Text(if (isExpanded) (if (isAr) "إخفاء التشكيل" else "Hide Squad") else (if (isAr) "عرض التشكيلة الرسمية" else "Official lineups"))
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isAr) "تشكيلة الأساس" else "Starting XI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        match.homeLineup.forEach { player ->
                            Text("• $player", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isAr) "الاحتياط" else "Bench squad", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        match.awayLineup.forEach { player ->
                            Text("• $player", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShimmerMatchListLoader(modifier: Modifier = Modifier) {
    val shimmerInstance = rememberShimmer()
    Box(modifier = modifier.shimmer(shimmerInstance).padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Gray.copy(alpha = 0.15f))
                )
            }
        }
    }
}

@Composable
fun ErrorBannerCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(text = message, modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
fun MatchErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 10.dp)) { Text("Retry Load") }
    }
}

@Composable
fun MatchEmptyState(isAr: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(if (isAr) "لا توجد مباريات حية حالياً." else "No live matches found.")
    }
}