package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MoreScreen(
    currentRole: String?,
    onNavigate: (String) -> Unit
) {
    val items = mutableListOf(
        MoreMenuItem("Discover", "Browse talents & projects", Icons.Default.Groups, AccentRed, "DISCOVERY"),
        MoreMenuItem("Commons", "Co-op discussion boards", Icons.Default.Forum, AccentBlue, "COMMONS"),
        MoreMenuItem("Search", "Global index lookup", Icons.Default.Search, CrispAmber, "SEARCH"),
        MoreMenuItem("Analytics", "Growth & engagement insights", Icons.Default.Leaderboard, NeonEmerald, "ANALYTICS"),
        MoreMenuItem("Pipeline", "Production task funnel", Icons.Default.Analytics, AccentBlue, "CONTENT_PIPELINE"),
        MoreMenuItem("Video Huddle", "Live team video room", Icons.Default.VideoCall, AccentRed, "VIDEO_HUDDLE"),
        MoreMenuItem("Knowledge", "Co-op manuals & resources", Icons.Default.AutoStories, CrispAmber, "KNOWLEDGE_BASE"),
        MoreMenuItem("Premium Pro", "Manage subscription", Icons.Default.Star, CrispAmber, "PREMIUM_SUBSCRIPTION"),
        MoreMenuItem("Connections", "Network requests", Icons.Default.PersonAdd, NeonEmerald, "CONNECTION_REQUESTS"),
        MoreMenuItem("Blocked Users", "Manage ignored profiles", Icons.Default.Block, TextMuted, "BLOCKED_USERS"),
        MoreMenuItem("Help Desk", "Contact developer support", Icons.Default.ContactSupport, TextSecondary, "SUPPORT_CENTER"),
        MoreMenuItem("Founder CRM", "Beta cohort & metrics manager", Icons.Default.ManageAccounts, NeonEmerald, "FOUNDER_CRM"),
        MoreMenuItem("Comm Center", "Announcements & campaigns", Icons.Default.Campaign, AccentRed, "COMM_CENTER"),
        MoreMenuItem("Platform Control", "Central settings & flags", Icons.Default.Tune, NeonEmerald, "PLATFORM_CONTROL"),
        MoreMenuItem("Founder Command", "Closed beta executive deck", Icons.Default.RocketLaunch, AccentBlue, "FOUNDER_COMMAND")
    )

    if (currentRole == "PLATFORM_ADMIN") {
        items.add(0, MoreMenuItem("Admin Console", "System administration tools", Icons.Default.AdminPanelSettings, AccentRed, "ADMIN"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp)
    ) {
        // Section Header
        Text(
            text = "CO-OP DIRECTORY",
            color = AccentBlue,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clickable { onNavigate(item.route) }
                        .testTag("more_menu_item_${item.route.lowercase()}"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(item.iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = item.iconColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

data class MoreMenuItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val route: String
)
