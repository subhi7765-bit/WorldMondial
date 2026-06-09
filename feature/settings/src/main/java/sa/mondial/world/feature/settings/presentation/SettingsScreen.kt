package sa.mondial.world.feature.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sa.mondial.world.feature.settings.R 
import sa.mondial.world.core.data.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.currentLanguage.collectAsState()
    val theme by viewModel.themePreference.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xE6111C24), // Modern Dark Obsidian Luxury Top Bar
                    titleContentColor = Color(0xFFD4AF37) // Premium Golden Title Text
                )
            )
        },
        containerColor = Color.Transparent, // Ensures background image shows cleanly
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Language Selection Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37) // Golden Header Accent
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC18222C)) // Dark translucent glass container
                ) {
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        LanguageOption(
                            label = stringResource(id = R.string.language_english),
                            selected = language == "en",
                            onClick = { viewModel.setLanguage("en") }
                        )
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                        LanguageOption(
                            label = stringResource(id = R.string.language_arabic),
                            selected = language == "ar",
                            onClick = { viewModel.setLanguage("ar") }
                        )
                    }
                }
            }

            // Theme Selection Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.theme),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37) // Golden Header Accent
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC18222C)) // Dark translucent glass container
                ) {
                    Column(
                        modifier = Modifier.selectableGroup()
                    ) {
                        ThemeOption(
                            label = stringResource(id = R.string.theme_light),
                            selected = theme == ThemePreference.LIGHT,
                            onClick = { viewModel.setThemePreference(ThemePreference.LIGHT) }
                        )
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                        ThemeOption(
                            label = stringResource(id = R.string.theme_dark),
                            selected = theme == ThemePreference.DARK,
                            onClick = { viewModel.setThemePreference(ThemePreference.DARK) }
                        )
                        HorizontalDivider(color = Color(0x1AFFFFFF))
                        ThemeOption(
                            label = stringResource(id = R.string.theme_system),
                            selected = theme == ThemePreference.SYSTEM,
                            onClick = { viewModel.setThemePreference(ThemePreference.SYSTEM) }
                        )
                    }
                }
            }

            // Notification Switch Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.notifications),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4AF37) // Golden Header Accent
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC18222C)) // Dark translucent glass container
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .semantics {
                                role = Role.Switch
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.live_alerts),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(id = R.string.live_alerts_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8) // Soft readable slate gray text
                            )
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFD4AF37),
                                checkedTrackColor = Color(0xFF1E293B)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD4AF37), unselectedColor = Color.Gray)
        )
    }
}

@Composable
fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD4AF37), unselectedColor = Color.Gray)
        )
    }
}
