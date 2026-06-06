package sa.mondial.world.feature.matches.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
import sa.mondial.world.core.domain.Match
import sa.mondial.world.core.domain.MatchStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Main dashboard display listing Mondial matches with full Paging 3 support and an integrated refresh engine.
 */
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
    val pagedMatches = viewModel.pagedMatchesFlow.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAr) "مباريات المونديال الذكية" else "Mondial Paged Matches") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->

        // Native Material 3 pull-to-refresh structure wrapper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullToRefresh(
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = { 
                        viewModel.loadMondialMatches(forceRefresh = true)
                        pagedMatches.refresh()
                    }
                )
        ) {
            when {
                pagedMatches.loadState.refresh is LoadState.Loading -> {
                    ShimmerMatchListLoader(modifier = Modifier.testTag("loading_indicator"))
                }
                pagedMatches.loadState.refresh is LoadState.Error -> {
                    val error = (pagedMatches.loadState.refresh as LoadState.Error).error
                    MatchErrorState(
                        message = error.localizedMessage ?: "Paging Error occurred",
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

                        // Appending indicators for progressive layout pages
                        if (pagedMatches.loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(strokeWidth = 3.dp)
                                }
                            }
                        }
                    }
                }
            }

            // Material 3 loading container bound tightly over the Box scope hierarchy
            PullToRefreshContainer(
                isRefreshing = isRefreshing,
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) match.roundAr else match.roundEn,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isAr) match.homeTeamNameAr else match.homeTeamNameEn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = if (isAr) TextAlign.Right else TextAlign.Left
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = match.homeScore?.toString() ?: "-",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " : ", 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 4.git dp)
                        )
                        Text(
                            text = match.awayScore?.toString() ?: "-",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = if (isAr) match.awayTeamNameAr else match.awayTeamNameEn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = if (isAr) TextAlign.Left else TextAlign.Right
                )
            }

            // Real-time counter for match kickoffs
            if (match.matchStatus == MatchStatus.UPCOMING) {
                val remainingMillis by produceState(
                    initialValue = match.utcTime.toEpochMilli() - System.currentTimeMillis(),
                    key1 = match.utcTime
                ) {
                    while (value > 0) {
                        val target = match.utcTime.toEpochMilli()
                        val diff = target - System.currentTimeMillis()
                        value = if (diff > 0) diff else 0
                        delay(1000L)
                    }
                }

                if (remainingMillis > 0) {
                    val totalSeconds = remainingMillis / 1000
                    val seconds = totalSeconds % 60
                    val minutes = (totalSeconds / 60) % 60
                    val hours = (totalSeconds / 3600) % 24
                    val days = totalSeconds / 86400

                    val countdownStr = "${days}d ${hours}h ${minutes}m ${seconds}s"

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isAr) "يبدأ بعد: $countdownStr" else "Starts in: $countdownStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            ) {
                Text(if (isExpanded) (if (isAr) "إخفاء التشكيل" else "Hide Squad") else (if (isAr) "عرض التشكيلة الرسمية" else "Official Lineups"))
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
                        Text(if (isAr) "تشكيلة الأساس" else "Starting XI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (match.homeLineup.isEmpty()) {
                            Text(if (isAr) "لم تتوفر بعد" else "Not available yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        } else {
                            match.homeLineup.forEach { player ->
                                Text("• $player", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isAr) "الاحتياط" else "Bench Squad", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (match.awayLineup.isEmpty()) {
                            Text(if (isAr) "لم تتوفر بعد" else "Not available yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        } else {
                            match.awayLineup.forEach { player ->
                                Text("• $player", style = MaterialTheme.typography.bodySmall)
                            }
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmer(shimmerInstance)
                    .background(Color.Gray.copy(alpha = 0.15f))
            )
        }
    }
}

@Composable
fun MatchErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry Load") }
    }
}

@Composable
fun MatchEmptyState(isAr: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (isAr) "لا توجد مباريات حية حالياً." else "No live matches found.",
            color = MaterialTheme.colorScheme.outline
        )
    }
}
