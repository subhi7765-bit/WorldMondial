package sa.mondial.world.feature.matches.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
import coil.compose.AsyncImage 
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.ShimmerBounds
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
fun MatchesScreen(
    viewModel: MatchesViewModel,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val isAr = language == "ar"
    
    val listState = rememberLazyListState()
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val selectedDay by viewModel.selectedDay.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (isAr) "مباريات اليوم" else "Today's Matches", 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, 
                    titleContentColor = MaterialTheme.colorScheme.primary 
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
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
                            .background(if (isCurrentSelection) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewModel.selectDay(dayEnum) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isCurrentSelection) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrentSelection) FontWeight.Bold else FontWeight.Normal
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
                        onRefresh = { viewModel.loadMondialMatches(forceRefresh = true) }
                    )
            ) {
                when (val state = uiState) {
                    is UiState.Loading -> {
                        ShimmerMatchListLoader()
                    }
                    is UiState.Error -> {
                        MatchErrorState(
                            message = state.displayMessage,
                            isAr = isAr,
                            onRetry = { viewModel.loadMondialMatches(forceRefresh = true) }
                        )
                    }
                    is UiState.Empty -> {
                        MatchEmptyState(isAr = isAr)
                    }
                    is UiState.Success -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(state.data) { match ->
                                MatchCard(
                                    match = match,
                                    isAr = isAr,
                                    onClick = { onNavigateToDetails(match.id) }
                                )
                            }
                        }
                    }
                }

                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
fun MatchCard(match: Match, isAr: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isAr) match.roundAr else match.roundEn, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(match.utcTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(if (isAr) Locale("ar") else Locale.ENGLISH)), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                
                // الفريق الأول (صاحب الأرض)
                Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (match.homeTeamFlagUrl.isNotEmpty()) {
                        AsyncImage(
                            model = match.homeTeamFlagUrl,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = if (isAr) match.homeTeamNameAr else match.homeTeamNameEn, 
                        textAlign = TextAlign.Center, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // النتيجة
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(horizontal = 8.dp).weight(0.8f)) {
                    Text(
                        text = "${match.homeScore ?: "-"} : ${match.awayScore ?: "-"}", 
                        modifier = Modifier.padding(vertical = 12.dp), 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                
                // الفريق الثاني (الضيف)
                Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (match.awayTeamFlagUrl.isNotEmpty()) {
                        AsyncImage(
                            model = match.awayTeamFlagUrl,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = if (isAr) match.awayTeamNameAr else match.awayTeamNameEn, 
                        textAlign = TextAlign.Center, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // السر هنا: تم إزالة الـ IF القديمة، وأصبح التطبيق يعرض كل الحالات
            val statusText = when (match.matchStatus) {
                MatchStatus.LIVE -> if (isAr) "مباشر الآن" else "LIVE"
                MatchStatus.HALF_TIME -> if (isAr) "بين الشوطين" else "Half-Time"
                MatchStatus.FINISHED -> if (isAr) "انتهت" else "Finished"
                MatchStatus.UPCOMING -> if (isAr) "لم تبدأ بعد" else "Upcoming"
            }
            
            val statusColor = when (match.matchStatus) {
                MatchStatus.LIVE -> MaterialTheme.colorScheme.error // أحمر
                MatchStatus.HALF_TIME -> Color(0xFFE67E22) // برتقالي
                else -> MaterialTheme.colorScheme.primary // أزرق/أساسي
            }
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun ShimmerMatchListLoader() {
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.View)
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).shimmer(shimmerInstance).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
        }
    }
}

@Composable
fun MatchErrorState(message: String, isAr: Boolean, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Warning, contentDescription = "Error", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text(if (isAr) "إعادة المحاولة" else "Retry", color = MaterialTheme.colorScheme.onPrimary) }
    }
}

@Composable
fun MatchEmptyState(isAr: Boolean) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Info, contentDescription = "Empty", modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(if (isAr) "لا توجد مباريات متاحة في تاريخ هذا اليوم." else "No matches scheduled for this specific date.", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(if (isAr) "يرجى التحقق من الأيام الأخرى أو سحب الشاشة للتحديث." else "Check other days or pull down to refresh.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
