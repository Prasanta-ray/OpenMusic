package com.openmusic.app.ui.screens.legal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "User Data & Privacy Policy",
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
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Privacy Commitment Hero
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Zero Telemetry Guarantee",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Your listening habits belong to you alone.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Privacy Checklist
                        PrivacyCheckItem("No Tracking SDKs (Zero Firebase, Zero Google Analytics)")
                        PrivacyCheckItem("No User Accounts, Logins, or Profile Fingerprinting")
                        PrivacyCheckItem("100% Local SQLite Room Storage on your device")
                        PrivacyCheckItem("One-Tap Total Data Export & Database Wiping")
                    }
                }
            }

            // Section 1: Data Collection (None)
            item {
                PrivacySectionCard(
                    title = "1. Information We Collect: None",
                    icon = Icons.Default.Security,
                    content = "OpenMusic does not collect, harvest, aggregate, sell, or transmit any personal data, device identifiers (IMEI, MAC address, AAID), advertising IDs, or usage metrics.\n\n" +
                            "There are NO cloud servers operated by OpenMusic that store user telemetry. The application functions entirely offline for stored songs and as a direct client for internet streams."
                )
            }

            // Section 2: Local Storage Architecture
            item {
                PrivacySectionCard(
                    title = "2. On-Device Storage & SQLite Database",
                    icon = Icons.Default.Storage,
                    content = "All user configurations and music preferences are stored strictly inside your phone's private app sandbox:\n\n" +
                            "• Playlists & Favorites: Stored locally in Android Room database (`AppDatabase.db`).\n" +
                            "• Search History: Kept in a local SQLite table (`search_history`) and can be cleared with one click.\n" +
                            "• Cached & Downloaded Media: Audio tracks and album art are stored in your device's private cache folder (`context.cacheDir/media_cache`).\n\n" +
                            "No external application or remote entity can access this data without system-level device compromise."
                )
            }

            // Section 3: Network Communications
            item {
                PrivacySectionCard(
                    title = "3. Network Communication Transparency",
                    icon = Icons.Default.Wifi,
                    content = "When streaming or searching for music, OpenMusic makes direct outbound HTTP/HTTPS connections from your device to public endpoints:\n\n" +
                            "• Metadata Queries: Transmitted directly to YouTube Music InnerTube web endpoints to fetch search suggestions, artist information, and album tracks.\n" +
                            "• Audio Stream CDN: Media playback requests connect directly to Google's public audio Content Delivery Network (Googlevideo CDN).\n\n" +
                            "OpenMusic acts strictly as a transparent user agent. We operate no intermediate proxy servers, meaning your network traffic is direct and unintercepted by us."
                )
            }

            // Section 4: Device Permissions
            item {
                PrivacySectionCard(
                    title = "4. Android Device Permissions",
                    icon = Icons.Default.CheckCircle,
                    content = "OpenMusic requests only the minimum runtime permissions essential for media playback:\n\n" +
                            "• INTERNET & ACCESS_NETWORK_STATE: Required to query metadata and download audio streams.\n" +
                            "• POST_NOTIFICATIONS (Android 13+): Displays media playback controls (play, pause, next) in the notification shade and lock screen.\n" +
                            "• READ_MEDIA_AUDIO / READ_EXTERNAL_STORAGE (Optional): Requested only if you explicitly choose to scan and play audio files stored on your device.\n" +
                            "• FOREGROUND_SERVICE & WAKE_LOCK: Keeps audio playing smoothly in the background when the display turns off."
                )
            }

            // Section 5: Data Sovereignty & Right to Erasure
            item {
                PrivacySectionCard(
                    title = "5. Your Rights: Export & Complete Wipe",
                    icon = Icons.Default.DeleteSweep,
                    content = "Under international privacy standards (including GDPR, CCPA, and open source digital rights principles):\n\n" +
                            "• Data Portability: You can export your full playlist collection into JSON format anytime in Settings -> Storage -> Export Playlists.\n" +
                            "• Right to Erasure: You can clear your cache, delete your search history, or wipe the entire database with a single button in Settings.\n" +
                            "• Total Removal: Uninstalling the application immediately and irreversibly deletes 100% of stored user data from your device."
                )
            }

            // Section 6: Contact & Questions
            item {
                PrivacySectionCard(
                    title = "6. Privacy Inquiries",
                    icon = Icons.Default.Lock,
                    content = "For questions or audits regarding our open-source codebase and privacy practices, you can review the complete source code on GitHub or contact us at team.codelithlabs@gmail.com."
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Effective Date: October 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyCheckItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PrivacySectionCard(
    title: String,
    icon: ImageVector,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
        }
    }
}
