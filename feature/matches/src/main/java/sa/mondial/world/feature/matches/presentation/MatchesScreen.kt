package sa.mondial.world.feature.matches.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.ShimmerBounds
import kotlinx.coroutines.delay
import sa.mondial.world.core.domain.Match
import sa.mondial.world.core.domain.MatchStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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
    
    val selectedDay by viewModel.selectedDay.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMondialMatches(forceRefresh = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAr) "مباريات اليوم" else "Today's Matches", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xE6111C24), 
                    titleContentColor = Color(0xFFD4AF37) 
                )
            )
        },
        containerColor = Color.Transparent, 
        modifier = modifier
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fixed Cleanly: Corrected .fillOuterWidth() typo to standard Android .fillMaxWidth() layout token
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val daysList = listOf(
                    SelectedDay.YESTERDAY to (if (isAr) "أمس" else "Yesterday"),
                    SelectedDay.TODAY to (if (isAr) "اليوم" else "Today"),
                    SelectedDay.TOMORROW to (if (isAr) "الغد" else "Tomorrow")
                )

                daysList.forEach { (dayEnum, label) ->
                    val isCurrentSelection = selectedDay == dayEnum
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isCurrentSelection) Color(0x33D4AF37) else Color.Transparent)
                            .clickable { viewModel.selectDay(dayEnum) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isCurrentSelection) Color(0xFFD4AF37) else Color(0xFF94A3B8),
                            fontWeight = if (isCurrentSelection) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
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

                            if (pagedMatches.loadState.append is LoadState.Loading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(strokeWidth = 3.dp, color = Color(0xFFD4AF37))
                                    }
                                }
                            }
                        }
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
        colors = CardDefaults.cardColors(containerColor = Color(0xCC18222C)), 
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    color = Color(0xFFD4AF37), 
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
                    color = Color(0xFF94A3B8)
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
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = if (isAr) TextAlign.Right else TextAlign.Left
                )

                Surface(
                    color = Color(0x4D334155), 
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
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " : ", 
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD4AF37), 
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Text(
                            text = match.awayScore?.toString() ?: "-",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = if (isAr) match.awayTeamNameAr else match.awayTeamNameEn,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = if (isAr) TextAlign.Left else TextAlign.Right
                )
            }

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
                        color = Color(0xCC7C2D12), 
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isAr) "يبدأ بعد: $countdownStr" else "Starts in: $countdownStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            var expandText = if (isAr) "عرض التشكيلة الرسمية" else "Official Lineups"
            if (isExpanded) {
                expandText = if (isAr) "إخفاء التشكيل" else "Hide Squad"
            }

            TextButton(
                onClick = { isExpanded = !isExpanded },
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD4AF37)),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            ) {
                Text(text = expandText, fontWeight = FontWeight.Bold)
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
                        .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isAr) "تشكيلة الأساس" else "Starting XI", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (match.homeLineup.isEmpty()) {
                            Text(if (isAr) "لم تتوفر بعد" else "Not available yet", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        } else {
                            match.homeLineup.forEach { player ->
                                Text("• $player", style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isAr) "الاحتياط" else "Bench Squad", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (match.awayLineup.isEmpty()) {
                            Text(if (isAr) "لم تتوفر بعد" else "Not available yet", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        } else {
                            match.awayLineup.forEach { player ->
                                Text("• $player", style = MaterialTheme.typography.bodySmall, color = Color.White)
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
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
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
            text = if (isAr) "لا توجد مباريات متاحة في تاريخ هذا اليوم." else "No matches scheduled for this specific date.",
            color = Color.White
        )
    }
}
