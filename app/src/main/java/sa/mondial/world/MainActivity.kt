package sa.mondial.world

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen // Added Cleanly: Mandatory import for Android Splash Screen API
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import sa.mondial.world.core.data.LocalizationManager
import sa.mondial.world.core.data.ThemePreference
import sa.mondial.world.feature.matches.presentation.MatchesScreen
import sa.mondial.world.feature.matches.presentation.MatchesViewModel
import sa.mondial.world.feature.matches.presentation.MatchDetailsScreen
import sa.mondial.world.feature.matches.presentation.MatchDetailsViewModel
import sa.mondial.world.feature.news.presentation.NewsScreen
import sa.mondial.world.feature.news.presentation.NewsViewModel
import sa.mondial.world.feature.settings.presentation.SettingsScreen
import sa.mondial.world.feature.settings.presentation.SettingsViewModel
import sa.mondial.world.navigation.DashboardDestination
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var localizationManager: LocalizationManager

    private val deepLinkFlow = MutableSharedFlow<String>(replay = 1)

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Timber.i("MainActivity: POST_NOTIFICATIONS permission granted successfully.")
        } else {
            Timber.w("MainActivity: POST_NOTIFICATIONS permission denied by the end user.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Fixed Cleanly: Added installSplashScreen() as the absolute first line to prevent the initialization white screen freeze
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        checkAndRequestNotificationPermission()
        handleDeepLink(intent)

        lifecycleScope.launch {
            localizationManager.currentLanguage.collectLatest { language ->
                val currentLanguageTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                if (currentLanguageTags != language) {
                    Timber.i("MainActivity: Updating application locales dynamically to $language")
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
                }
            }
        }

        lifecycleScope.launch {
            localizationManager.themePreference.collectLatest { theme ->
                val mode = when (theme) {
                    ThemePreference.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                    ThemePreference.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                    ThemePreference.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                if (AppCompatDelegate.getDefaultNightMode() != mode) {
                    Timber.i("MainActivity: Applying theme change dynamically to $theme")
                    AppCompatDelegate.setDefaultNightMode(mode)
                }
            }
        }

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                LaunchedEffect(navController) {
                    deepLinkFlow.collect { matchId ->
                        navController.navigate(DashboardDestination.MatchDetailsRoute(matchId)) {
                            launchSingleTop = true
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val showBottomBar = currentDestination != null && (
                            currentDestination.hasRoute<DashboardDestination.Matches>() ||
                            currentDestination.hasRoute<DashboardDestination.News>() ||
                            currentDestination.hasRoute<DashboardDestination.Settings>()
                        )
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentDestination?.hasRoute<DashboardDestination.Matches>() == true,
                                    onClick = {
                                        navController.navigate(DashboardDestination.Matches) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Matches") },
                                    label = { Text("Matches") }
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.hasRoute<DashboardDestination.News>() == true,
                                    onClick = {
                                        navController.navigate(DashboardDestination.News) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Info, contentDescription = "News") },
                                    label = { Text("News") }
                                )
                                NavigationBarItem(
                                    selected = currentDestination?.hasRoute<DashboardDestination.Settings>() == true,
                                    onClick = {
                                        navController.navigate(DashboardDestination.Settings) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                    label = { Text("Settings") }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = DashboardDestination.Matches,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable<DashboardDestination.Matches> {
                            val viewModel: MatchesViewModel = hiltViewModel()
                            MatchesScreen(
                                viewModel = viewModel,
                                onNavigateToDetails = { id ->
                                    navController.navigate(DashboardDestination.MatchDetailsRoute(id))
                                }
                            )
                        }
                        composable<DashboardDestination.News> {
                            val viewModel: NewsViewModel = hiltViewModel()
                            NewsScreen(
                                viewModel = viewModel,
                                onNavigateToDetails = { id ->
                                    navController.navigate(DashboardDestination.MatchDetailsRoute(id))
                                }
                            )
                        }
                        composable<DashboardDestination.Settings> {
                            val viewModel: SettingsViewModel = hiltViewModel()
                            SettingsScreen(
                                viewModel = viewModel
                            )
                        }
                        composable<DashboardDestination.MatchDetailsRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<DashboardDestination.MatchDetailsRoute>()
                            val matchId = route.matchId
                            val viewModel: MatchDetailsViewModel = hiltViewModel(backStackEntry)
                            MatchDetailsScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "mondial" && uri.host == "match") {
            val matchId = uri.getQueryParameter("matchId")
            if (!matchId.isNullOrEmpty()) {
                lifecycleScope.launch {
                    deepLinkFlow.emit(matchId)
                }
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
