package com.openmusic.app.ui.screens.settings

import android.os.StatFs
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import com.openmusic.app.ui.navigation.Screen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openmusic.app.domain.model.AudioQuality
import com.openmusic.app.ui.MainViewModel
import com.openmusic.app.ui.components.EqualizerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onNavigateToScreen: (Screen) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val streamingQuality by viewModel.streamingQuality.collectAsState()
    val downloadQuality by viewModel.downloadQuality.collectAsState()
    val isAmoledBlack by viewModel.isAmoledBlackTheme.collectAsState()
    val diskCacheBytes by viewModel.diskCacheBytes.collectAsState()
    val audioFxState by viewModel.audioFxState.collectAsState()

    var showExportDialog by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var crossfadeSeconds by remember { mutableFloatStateOf(3.0f) }

    // Device Storage calculation for dual-color storage breakdown gauge
    val statFs = remember {
        try {
            StatFs(context.cacheDir.absolutePath)
        } catch (e: Exception) {
            null
        }
    }
    val totalBytes = remember(statFs) { statFs?.totalBytes ?: (64L * 1024L * 1024L * 1024L) }
    val freeBytes = remember(statFs) { statFs?.availableBytes ?: (32L * 1024L * 1024L * 1024L) }
    val appDataBytes = 16L * 1024L * 1024L // Room SQLite database and image thumbnail cache

    LaunchedEffect(Unit) {
        viewModel.refreshDiskCacheBytes()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Storage",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Section 1: Visual Storage Breakdown Hub with Multi-Color Gauge
            item {
                SettingsSectionCard(
                    title = "Storage & Cache Management",
                    icon = Icons.Default.Storage
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Stream Cache & Disk Usage",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatByteSize(diskCacheBytes),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = {
                                    val freed = viewModel.clearDiskCache()
                                    Toast.makeText(
                                        context,
                                        "Cache Cleared! Freed ${formatByteSize(freed)}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear", maxLines = 1, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual Multi-Color Storage Gauge Bar
                        val safeTotal = totalBytes.coerceAtLeast(1L).toFloat()
                        val cacheWeight = (diskCacheBytes.toFloat() / safeTotal).coerceIn(0.01f, 0.40f)
                        val appWeight = (appDataBytes.toFloat() / safeTotal).coerceIn(0.01f, 0.20f)
                        val freeWeight = (freeBytes.toFloat() / safeTotal).coerceIn(0.10f, 0.90f)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                // 1. Audio Cache Segment (Primary / Purple)
                                Box(
                                    modifier = Modifier
                                        .weight(cacheWeight)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                // 2. App Data & Room Database Segment (Teal Cyan)
                                Box(
                                    modifier = Modifier
                                        .weight(appWeight)
                                        .fillMaxHeight()
                                        .background(Color(0xFF00B4D8))
                                )
                                // 3. Free Device Storage Segment (Emerald Green)
                                Box(
                                    modifier = Modifier
                                        .weight(freeWeight)
                                        .fillMaxHeight()
                                        .background(Color(0xFF2EC4B6))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Legend row for the gauge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StorageLegendChip(
                                color = MaterialTheme.colorScheme.primary,
                                label = "Cache",
                                value = formatByteSize(diskCacheBytes)
                            )
                            StorageLegendChip(
                                color = Color(0xFF00B4D8),
                                label = "App Data",
                                value = formatByteSize(appDataBytes)
                            )
                            StorageLegendChip(
                                color = Color(0xFF2EC4B6),
                                label = "Free Disk",
                                value = formatByteSize(freeBytes)
                            )
                        }
                    }
                }
            }

            // Section 2: Audio & Qualcomm Hexagon DSP Equalizer Shortcut Card
            item {
                SettingsSectionCard(
                    title = "Audio FX & Equalizer",
                    icon = Icons.Default.Equalizer
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hardware DSP & Audio FX",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "5-band parametric equalizer, bass boost, and loudness enhancer powered by Qualcomm Hexagon DSP.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalButton(
                                onClick = { showEqualizerDialog = true },
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open EQ")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Audio FX Status Badge Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (audioFxState.isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (audioFxState.isEnabled) "EQ: Active (${audioFxState.selectedPreset})" else "EQ: Off",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (audioFxState.isLoudnessNormalizerEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = if (audioFxState.isLoudnessNormalizerEnabled) "Loudness: On" else "Loudness: Off",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Audio Stream & Download Quality
            item {
                SettingsSectionCard(
                    title = "Audio Stream & Download Quality",
                    icon = Icons.Default.GraphicEq
                ) {
                    Text(
                        text = "Streaming Quality",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AudioQuality.entries.forEach { quality ->
                            FilterChip(
                                selected = streamingQuality == quality,
                                onClick = { viewModel.setStreamingQuality(quality) },
                                label = { Text(quality.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Download Quality",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AudioQuality.entries.forEach { quality ->
                            FilterChip(
                                selected = downloadQuality == quality,
                                onClick = { viewModel.setDownloadQuality(quality) },
                                label = { Text(quality.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Section 4: Gapless Playback & Crossfade Transitions
            item {
                SettingsSectionCard(
                    title = "Playback Transitions & Crossfade",
                    icon = Icons.Default.Timer
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Crossfade Duration",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Smoothly blends audio between consecutive tracks in your queue.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (crossfadeSeconds <= 0.1f) "Off" else "%.1f s".format(crossfadeSeconds),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = crossfadeSeconds,
                            onValueChange = { crossfadeSeconds = it },
                            valueRange = 0.0f..10.0f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Section 5: Appearance & Battery
            item {
                SettingsSectionCard(
                    title = "Appearance & Battery",
                    icon = Icons.Default.DarkMode
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setAmoledBlackTheme(!isAmoledBlack) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pure AMOLED Black Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Turns off display pixels (#000000) on OLED screens for deep contrast and maximum battery efficiency.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isAmoledBlack,
                            onCheckedChange = { viewModel.setAmoledBlackTheme(it) }
                        )
                    }
                }
            }

            // Section 6: Playlist Backup & Migration
            item {
                SettingsSectionCard(
                    title = "Playlist Backup & Migration",
                    icon = Icons.Default.FileUpload
                ) {
                    Text(
                        text = "Export and import your library playlists to open JSON or standard M3U files for easy migration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.exportPlaylistsAsJson()
                                    showExportDialog = json
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export JSON")
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val m3u = viewModel.exportPlaylistsAsM3u()
                                    showExportDialog = m3u
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export M3U")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Playlists from JSON")
                    }
                }
            }

            // Section 7: Creator & Legal Protection Hub
            item {
                SettingsSectionCard(
                    title = "Creator & Legal Protection",
                    icon = Icons.Default.VerifiedUser
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingsNavigationRow(
                            title = "Legal & Creator Hub",
                            subtitle = "Central access to all compliance, licensing & developer resources",
                            icon = Icons.Default.Security,
                            onClick = { onNavigateToScreen(Screen.LegalHub) }
                        )
                        SettingsNavigationRow(
                            title = "About Creator",
                            subtitle = "Ray & OpenMusic team story, architecture & DSP tuning",
                            icon = Icons.Default.Person,
                            onClick = { onNavigateToScreen(Screen.AboutCreator) }
                        )
                        SettingsNavigationRow(
                            title = "Contact Creator & Feedback",
                            subtitle = "Bug reports, hardware diagnostics, and feature requests",
                            icon = Icons.AutoMirrored.Filled.ContactSupport,
                            onClick = { onNavigateToScreen(Screen.ContactCreator) }
                        )
                        SettingsNavigationRow(
                            title = "Terms & Conditions",
                            subtitle = "Client-side metadata parser, fair use & DMCA takedown policy",
                            icon = Icons.Default.Gavel,
                            onClick = { onNavigateToScreen(Screen.TermsAndConditions) }
                        )
                        SettingsNavigationRow(
                            title = "User Data & Privacy Policy",
                            subtitle = "Zero telemetry guarantee, local Room storage & data wipe",
                            icon = Icons.Default.Policy,
                            onClick = { onNavigateToScreen(Screen.PrivacyPolicy) }
                        )
                        SettingsNavigationRow(
                            title = "App Logo & Versions Splash",
                            subtitle = "Replay the interactive welcome splash screen with app logo & build versions",
                            icon = Icons.Default.GraphicEq,
                            onClick = { onNavigateToScreen(Screen.Splash) }
                        )
                    }
                }
            }

            // Section 8: About OpenMusic & System Diagnostics
            item {
                SettingsSectionCard(
                    title = "System Diagnostics & Build",
                    icon = Icons.Default.Info
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DiagnosticRow(label = "Application Version", value = "OpenMusic v1.0.0")
                        DiagnosticRow(label = "Platform Profile", value = "Snapdragon 675 / Kryo 460")
                        DiagnosticRow(label = "DSP Acceleration", value = "Qualcomm Hexagon Audio DSP")
                        DiagnosticRow(label = "Media Engine", value = "AndroidX Media3 ExoPlayer 1.5.1")
                        DiagnosticRow(label = "Database Engine", value = "SQLite / Room Schema v5")
                        DiagnosticRow(label = "License", value = "MIT Open Source")

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://github.com/openmusic-app/openmusic")
                                    )
                                    context.startActivity(intent)
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Open Source on GitHub",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Direct Audio FX & Equalizer Dialog
    if (showEqualizerDialog) {
        EqualizerDialog(
            audioFxState = audioFxState,
            onToggleEnabled = { viewModel.setAudioFxEnabled(it) },
            onPresetSelected = { viewModel.setAudioFxPreset(it) },
            onBandLevelChange = { band, level -> viewModel.setAudioFxBandLevel(band, level) },
            onBassBoostChange = { strength -> viewModel.setAudioFxBassBoost(strength) },
            onLoudnessNormalizerChange = { viewModel.setAudioFxLoudnessNormalizerEnabled(it) },
            onTargetGainChange = { viewModel.setAudioFxTargetGainMb(it) },
            onDismiss = { showEqualizerDialog = false }
        )
    }

    // Export Dialog
    showExportDialog?.let { exportData ->
        AlertDialog(
            onDismissRequest = { showExportDialog = null },
            title = { Text("Exported Playlists") },
            text = {
                Column {
                    Text(
                        text = "Your playlist data has been generated. You can copy it to clipboard:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = exportData,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportData))
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = null
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Playlists") },
            text = {
                Column {
                    Text(
                        text = "Paste your OpenMusic JSON playlist export below:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        placeholder = { Text("[{\"title\":\"My Playlist\",\"songs\":[...]}]") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            coroutineScope.launch {
                                val result = viewModel.importPlaylistsFromJson(importJsonText)
                                result.onSuccess { count ->
                                    Toast.makeText(
                                        context,
                                        "Imported $count playlists successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showImportDialog = false
                                    importJsonText = ""
                                }.onFailure { error ->
                                    Toast.makeText(
                                        context,
                                        "Import failed: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StorageLegendChip(
    color: Color,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatByteSize(bytes: Long): String {
    return when {
        bytes <= 0L -> "0 B"
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024L * 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

