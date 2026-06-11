package sa.mondial.world.feature.matches.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage 
import com.valentinilk.shimmer.shimmer
import sa.mondial.world.core.common.UiState
import sa.mondial.world.core.domain.LineupPlayer
import sa.mondial.world.core.domain.MatchDetails
import sa.mondial.world.core.domain.MatchStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    viewModel: MatchDetailsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOffline by viewModel.isOfflineWithCache.collectAsState()
    val lastSyncTime by viewModel.lastSyncTimestamp.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val isAr = language == "ar"

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isAr) "تفاصيل المباراة" else "Match Details",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    MatchDetailsShimmerLoader()
                }
                is UiState.Error -> {
                    MatchDetailsError(
                        message = state.displayMessage,
                        isAr = isAr,
                        onRetry = { viewModel.loadDetails() }
                    )
                }
                is UiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize())
                }
                is UiState.Success -> {
                    val details = state.data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AnimatedVisibility(
                                visible = isOffline,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                OfflineWarningBanner(lastSyncTime, isAr)
                            }
                        }

                        // اللمسة الجمالية: رأس البطولة (شعار واسم الكأس/الدوري)
                        item {
                            CompetitionHeader(details = details, isAr = isAr)
                        }

                        item {
                            ScoreboardCard(details = details, isAr = isAr)
                        }

                        item {
                            MatchInfoCard(details = details, isAr = isAr)
                        }

                        item {
                            LineupSection(details = details, isAr = isAr)
                        }

                        item {
                            TimelineSection(details = details, isAr = isAr)
                        }
                    }
                }
            }
        }
    }
}

// تصميم رأس البطولة الفخم
@Composable
fun CompetitionHeader(details: MatchDetails, isAr: Boolean) {
    val compName = if (isAr) details.competitionNameAr else details.competitionNameEn
    if (compName.isNotEmpty() && compName != "Unknown Competition") {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (details.competitionEmblem.isNotEmpty()) {
                AsyncImage(
                    model = details.competitionEmblem,
                    contentDescription = "Competition Emblem",
                    modifier = Modifier.size(56.dp).padding(bottom = 8.dp)
                )
            }
            Text(
                text = compName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OfflineWarningBanner(lastSyncTime: String?, isAr: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Offline",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isAr) "وضع عدم الاتصال" else "Offline Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isAr) "آخر تحديث: ${lastSyncTime ?: "غير معروف"}" else "Last synced: ${lastSyncTime ?: "Unknown"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun ScoreboardCard(details: MatchDetails, isAr: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isAr) details.roundAr else details.roundEn,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // صاحب الأرض مع العلم الخاص به
                Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (details.homeTeamFlagUrl.isNotEmpty()) {
                        AsyncImage(
                            model = details.homeTeamFlagUrl,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp).padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = if (isAr) details.homeTeamNameAr else details.homeTeamNameEn,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // لوحة النتيجة الوسطى
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 8.dp).weight(0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = details.homeScore?.toString() ?: "-",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " : ",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = details.awayScore?.toString() ?: "-",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // الضيف مع العلم الخاص به
                Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (details.awayTeamFlagUrl.isNotEmpty()) {
                        AsyncImage(
                            model = details.awayTeamFlagUrl,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp).padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = if (isAr) details.awayTeamNameAr else details.awayTeamNameEn,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val statusText = when (details.matchStatus) {
                MatchStatus.LIVE -> if (isAr) "مباشر الآن" else "LIVE"
                MatchStatus.FINISHED -> if (isAr) "انتهت" else "Finished"
                MatchStatus.UPCOMING -> if (isAr) "لم تبدأ بعد" else "Upcoming"
            }
            val statusColor = if (details.matchStatus == MatchStatus.LIVE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MatchInfoCard(details: MatchDetails, isAr: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoRow(label = if (isAr) "الملعب" else "Venue", value = if (isAr) details.venueAr else details.venueEn)
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            InfoRow(label = if (isAr) "الحكم" else "Referee", value = if (isAr) details.refereeAr else details.refereeEn)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LineupSection(details: MatchDetails, isAr: Boolean) {
    if (details.homeStartingXI.isNotEmpty() || details.awayStartingXI.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isAr) "التشكيلة الأساسية" else "Starting XI",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontWeight = FontWeight.Bold
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) details.homeTeamNameAr else details.homeTeamNameEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                        details.homeStartingXI.forEach { player -> PlayerRow(player, isAr) }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) details.awayTeamNameAr else details.awayTeamNameEn,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                        details.awayStartingXI.forEach { player -> PlayerRow(player, isAr) }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerRow(player: LineupPlayer, isAr: Boolean) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "${player.number}.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(24.dp))
        Text(text = if (isAr) player.nameAr else player.nameEn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TimelineSection(details: MatchDetails, isAr: Boolean) {
    val events = if (isAr) details.timelineEventsAr else details.timelineEventsEn
    if (events.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isAr) "أحداث المباراة" else "Match Events",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp),
                    fontWeight = FontWeight.Bold
                )
                events.forEach { event ->
                    Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = event, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun MatchDetailsShimmerLoader() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).shimmer().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
        Box(modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(16.dp)).shimmer().background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
    }
}

@Composable
fun MatchDetailsError(message: String, isAr: Boolean, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = Icons.Default.Warning, contentDescription = "Error", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text(text = if (isAr) "إعادة المحاولة" else "Retry", color = MaterialTheme.colorScheme.onPrimary) }
    }
}
