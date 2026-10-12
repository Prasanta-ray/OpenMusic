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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
fun TermsAndConditionsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Terms & Conditions",
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

            // Prominent Non-Affiliation Disclaimer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Non-Affiliation Disclaimer",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "OpenMusic is an independent open-source client application. OpenMusic is NOT affiliated, authorized, maintained, sponsored, or endorsed in any way by Google LLC, YouTube, YouTube Music, Alphabet Inc., or any of their subsidiaries or affiliates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Article 1: Acceptance & Educational Purpose
            item {
                LegalClauseCard(
                    sectionNumber = "1",
                    title = "Acceptance of Terms & Nature of Application",
                    content = "By installing, running, or accessing OpenMusic (\"the Application\"), you acknowledge and agree to be bound by these Terms and Conditions. OpenMusic is an educational, free, and open-source software project distributed under the permissive MIT License. If you do not agree with any provision of these terms, please discontinue use and uninstall the Application immediately."
                )
            }

            // Article 2: Non-Hosting Guarantee
            item {
                LegalClauseCard(
                    sectionNumber = "2",
                    title = "Non-Hosting & Client-Side Architecture",
                    content = "The developers, contributors, and maintainers of OpenMusic DO NOT host, broadcast, transmit, upload, or store copyrighted audio recordings, musical compositions, or video streams on any proprietary or remote server infrastructure.\n\n" +
                            "OpenMusic functions strictly as a client-side user agent and media player interface. All metadata indexing, track search resolution, and audio stream routing occur entirely within the user's local device runtime utilizing publicly available endpoints. The Application is technically analogous to a specialized web browser."
                )
            }

            // Article 3: Fair Use & Interoperability
            item {
                LegalClauseCard(
                    sectionNumber = "3",
                    title = "Fair Use & Interoperability Rights",
                    content = "OpenMusic is developed for individual non-commercial use, research, interoperability testing, and accessibility enhancements in accordance with Fair Use doctrines codified under 17 U.S.C. § 107 and international copyright exceptions. The Application does not bypass digital rights management (DRM) or decryption safeguards; it accesses only unencrypted streams that are served publicly."
                )
            }

            // Article 4: DMCA Notice & Takedown Protocol
            item {
                LegalClauseCard(
                    sectionNumber = "4",
                    title = "DMCA & Copyright Policy",
                    content = "OpenMusic respects the intellectual property rights of musicians, record labels, and content creators worldwide. Because the Application hosts no content, the developers cannot remove content from third-party networks.\n\n" +
                            "However, if you are a copyright owner or an authorized agent and believe that any feature, parsing rule, or code component infringes upon your rights, please submit a formal notice containing:\n" +
                            "1. Clear identification of the copyrighted work.\n" +
                            "2. Specific description of the code or repository link at issue.\n" +
                            "3. Your official contact details (name, email, organization).\n" +
                            "4. A statement of good faith belief.\n\n" +
                            "Inquiries may be directed to our designated email at team.codelithlabs@gmail.com or via GitHub repository issue tracking. We act expeditiously to address legitimate inquiries."
                )
            }

            // Article 5: Limitation of Liability (MIT)
            item {
                LegalClauseCard(
                    sectionNumber = "5",
                    title = "Limitation of Liability & \"AS IS\" Warranty",
                    content = "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NONINFRINGEMENT.\n\n" +
                            "IN NO EVENT SHALL THE AUTHORS, DEVELOPERS, OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES, LOSS OF DATA, DEVICE DAMAGE, OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT, OR OTHERWISE, ARISING FROM, OUT OF, OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE."
                )
            }

            // Article 6: User Obligations
            item {
                LegalClauseCard(
                    sectionNumber = "6",
                    title = "User Obligations & Local Compliance",
                    content = "You are solely responsible for ensuring that your usage of OpenMusic complies with all applicable telecommunications, copyright, and privacy laws in your country or region. You agree not to utilize OpenMusic for commercial redistribution, mass downloading, rebroadcasting, or any unlawful enterprise."
                )
            }

            // Article 7: Modifications
            item {
                LegalClauseCard(
                    sectionNumber = "7",
                    title = "Modifications to Terms",
                    content = "We reserve the right to amend or replace these Terms and Conditions at any time. Updates are included within official version releases of the OpenMusic repository and in-app updates. Continued use of the Application following any modification signifies acceptance of updated terms."
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
                        text = "Last revised: October 2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LegalClauseCard(
    sectionNumber: String,
    title: String,
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
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = sectionNumber,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
