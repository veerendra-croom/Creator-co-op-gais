package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CommunityGuidelinesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "Community Guidelines",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Building a safe home for creators",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                GuidelineHeader(
                    icon = Icons.Default.Gavel,
                    title = "OUR CORE RULES",
                    color = AccentRed
                )
                
                Spacer(modifier = Modifier.height(20.dp))

                GuidelineItem(
                    title = "1. Zero Tolerance for Harassment",
                    description = "We are a professional network. Bullying, hate speech, or persistent unwanted contact is strictly forbidden. We enforce this through our system-wide reporting and admin audit logs."
                )

                GuidelineItem(
                    title = "2. Professional Posting Conduct",
                    description = "Keep communications relevant. Forum threads, public chat groups, and Syndicate pitches must remain professional. Avoid unsolicited marketing, copy-paste spam, or unrelated commercial promotion."
                )

                GuidelineItem(
                    title = "3. Identity & Portfolio Integrity",
                    description = "Impersonation is a permanent ban offense. Profile showcases and Open Role listings must be your own work or your team's work. Do not create fake project proposals to harvest creator data."
                )

                GuidelineItem(
                    title = "4. Collective Collaboration in Workspaces",
                    description = "Workspaces are shared productive environments. Respect the task flow, honor milestone commitments, and keep internal Syndicate coordination transparent and honest."
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = AccentBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Info, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Violations of these guidelines are reviewed by the Admin Control System. Reports can be filed by any user for posts, comments, or private messages.",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun GuidelineHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun GuidelineItem(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            description,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
