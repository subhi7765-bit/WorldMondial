package sa.mondial.world.feature.matches.presentation

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.rememberShimmer
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.domain.LineupPlayer
import sa.mondial.world.core.domain.MatchDetails
import sa.mondial.world.core.domain.MatchStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Detailed intelligence screen presenting formations, timelines, scores and metrics for a specific Mondial match.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    viewModel: MatchDetailsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val isAr = language == "ar"

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(key1 = viewModel) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { Text(if (isAr) "تفاصيل المباراة الذكية" else "Match Intelligence Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = if (isAr) "رجوع" else "Back"
                        )
                    }
                },
                actions = {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            val detailsObj = (uiState as? UiState.Success)?.data
                            if (detailsObj != null) {
                                val homeTeam = if (isAr) detailsObj.homeTeamNameAr else detailsObj.homeTeamNameEn
                                val awayTeam = if (isAr) detailsObj.awayTeamNameAr else detailsObj.awayTeamNameEn
                                val homeScore = detailsObj.homeScore?.toString() ?: "-"
                                val awayScore = detailsObj.awayScore?.toString() ?: "-"
                                val date = detailsObj.utcTime?.let {
                                    val zonedDateTime = java.time.ZonedDateTime.ofInstant(it, java.time.ZoneId.of("UTC"))
                                    val localZonedDateTime = zonedDateTime.withZoneSameInstant(java.time.ZoneId.systemDefault())
                                    localZonedDateTime.format(
                                        java.time.format.DateTimeFormatter
                                            .ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
                                            .withLocale(if (isAr) java.util.Locale("ar") else java.util.Locale.ENGLISH)
                                    )
                                } ?: ""
                                val venue = if (isAr) detailsObj.venueAr else detailsObj.venueEn
                                
                                // Fixed multiline string instantiation utilizing raw Kotlin triple quotes blocks
                                val shareText = if (isAr) {
                                    """🏆 $homeTeam × $awayTeam
⚽ النتيجة: $homeScore-$awayScore
📅 التاريخ: $date
🏟️ الملعب: $venue"""
                                } else {
                                    """🏆 $homeTeam vs $awayTeam
⚽ Score: $homeScore-$awayScore
📅 Date: $date
🏟️ Venue: $venue"""
                                }
                                
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, if (isAr) "مشاركة المباراة" else "Share Match")
                                context.startActivity(shareIntent)
                            }
                        },
                        enabled = uiState is UiState.Success
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Match Metrics"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val isOfflineWithCache by viewModel.isOfflineWithCache.collectAsState()
        val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isOfflineWithCache) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isAr) 
                                "⚠️ وضع غير متصل بالشبكة: تم تحميل البيانات المخزنة من آخر مزامنة: ${lastSyncTimestamp ?: ""}"
                            else 
                                "⚠️ Offline mode: Loaded cached data from last synchronization: ${lastSyncTimestamp ?: ""}",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        ShimmerMatchDetailsLoader()
                    }
                    is UiState.Success -> {
                        MatchDetailsContent(
                            details = state.data,
                            isAr = isAr
                        )
                    }
                    is UiState.Error -> {
                        MatchDetailsErrorState(
                            message = state.displayMessage,
                            onRetry = { viewModel.loadDetails() }
                        )
                    }
                    is UiState.Empty -> {
                        MatchDetailsEmptyState(isAr = isAr)
                    }
                }
            }
        }
    }
}

@Composable
fun MatchDetailsContent(
    details: MatchDetails,
    isAr: Boolean
) {
    var homeStartingExpanded by remember { mutableStateOf(true) }
    var homeSubsExpanded by remember { mutableStateOf(false) }
    var awayStartingExpanded by remember { mutableStateOf(true) }
    var awaySubsExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MatchHeaderCard(details = details, isAr = isAr)
        }

        // Native embedded stadium card rendering layout safely decoupling multi-feature modules paths
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAr) "🏟️ الاستاد والملعب" else "🏟️ Stadium Venue",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAr) details.venueAr else details.venueEn,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (details.timelineEventsAr.isNotEmpty()) {
            item {
                TimelineEventsCard(details = details, isAr = isAr)
            }
        }

        item {
            Text(
                text = if (isAr) "تشكيلة ${details.homeTeamNameAr}" else "${details.homeTeamNameEn} Squad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            CardAccordionSection(
                title = if (isAr) "التشكيلة الأساسية" else "Starting XI",
                isExpanded = homeStartingExpanded,
                onToggle = { homeStartingExpanded = !homeStartingExpanded }
            ) {
                LineupList(players = details.homeStartingXI, isAr = isAr)
            }
        }

        item {
            CardAccordionSection(
                title = if (isAr) "البدلاء" else "Substitutes",
                isExpanded = homeSubsExpanded,
                onToggle = { homeSubsExpanded = !homeSubsExpanded }
            ) {
                LineupList(players = details.homeSubstitutes, isAr = isAr)
            }
        }

        item {
            Text(
                text = if (isAr) "تشكيلة ${details.awayTeamNameAr}" else "${details.awayTeamNameEn} Squad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            CardAccordionSection(
                title = if (isAr) "التشكيلة الأساسية" else "Starting XI",
                isExpanded = awayStartingExpanded,
                onToggle = { awayStartingExpanded = !awayStartingExpanded }
            ) {
                LineupList(players = details.awayStartingXI, isAr = isAr)
            }
        }

        item {
            CardAccordionSection(
                title = if (isAr) "البدلاء" else "Substitutes",
                isExpanded = awaySubsExpanded,
                onToggle = { awaySubsExpanded = !awaySubsExpanded }
            ) {
                LineupList(players = details.awaySubstitutes, isAr = isAr)
            }
        }
    }
}

@Composable
fun MatchHeaderCard(
    details: MatchDetails,
    isAr: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isAr) details.roundAr else details.roundEn,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isAr) details.venueAr else details.venueEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = details.homeTeamFlagUrl,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAr) details.homeTeamNameAr else details.homeTeamNameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = details.homeScore?.toString() ?: "-",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Text(
                        text = details.awayScore?.toString() ?: "-",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = details.awayTeamFlagUrl,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isAr) details.awayTeamNameAr else details.awayTeamNameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val statusLabel = when (details.matchStatus) {
                MatchStatus.FINISHED -> if (isAr) "انتهت" else "Full Time"
                MatchStatus.LIVE -> if (isAr) "مباشر" else "LIVE Match"
                MatchStatus.UPCOMING -> if (isAr) "لم تبدأ بعد" else "Upcoming"
            }
            val statusColor = when (details.matchStatus) {
                MatchStatus.FINISHED -> MaterialTheme.colorScheme.outline
                MatchStatus.LIVE -> Color(0xFFC62828)
                MatchStatus.UPCOMING -> MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(1.dp, statusColor.copy(alpha = 0.3f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val localTime = remember(details.utcTime) {
                val zonedDateTime = java.time.ZonedDateTime.ofInstant(details.utcTime, java.time.ZoneId.of("UTC"))
                val localZonedDateTime = zonedDateTime.withZoneSameInstant(java.time.ZoneId.systemDefault())
                localZonedDateTime.format(
                    java.time.format.DateTimeFormatter
                        .ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM)
                        .withLocale(if (isAr) java.util.Locale("ar") else java.util.Locale.ENGLISH)
                )
            }

            Text(
                text = "${if (isAr) "حكم اللقاء: " else "Referee: "}${if (isAr) details.refereeAr else details.refereeEn}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${if (isAr) "توقيت محلي: " else "Local Time: "}$localTime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun TimelineEventsCard(
    details: MatchDetails,
    isAr: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isAr) "أحداث المباراة الهامة" else "Match Key Timeline",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(12.dp))
            val list = if (isAr) details.timelineEventsAr else details.timelineEventsEn
            list.forEachIndexed { index, event ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = event,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (index < list.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun CardAccordionSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun LineupList(players: List<LineupPlayer>, isAr: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        players.forEach { player ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (player.isGoalkeeper) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.number.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (player.isGoalkeeper) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = if (isAr) player.nameAr else player.nameEn,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (player.isCaptain) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (player.isCaptain) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAr) "(C)" else "(Capt)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC62828)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isAr) player.positionAr else player.positionEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerMatchDetailsLoader() {
    val shimmerInstance = rememberShimmer()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .shimmer(shimmerInstance)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(20.dp)
                .align(Alignment.Start)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Gray.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun MatchDetailsErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Retry loading")
        }
    }
}

@Composable
fun MatchDetailsEmptyState(isAr: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isAr) "لا تتوفر تفاصيل لهذه مباراة حالياً." else "No details available for this match.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
