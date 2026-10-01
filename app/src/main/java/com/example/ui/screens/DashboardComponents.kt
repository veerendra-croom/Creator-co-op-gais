package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.data.model.ProductionTask
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardKPICard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun ActivityTimelineItem(title: String, description: String, timestamp: Long) {
    val relativeTime = remember(timestamp) { com.example.util.DateTimeUtils.getRelativeTimeSpanString(timestamp) }
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(AccentBlue))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(relativeTime, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun ProfileHero(name: String, role: String, availabilityStatus: String, onEdit: () -> Unit, onShare: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Hero Banner
            Image(
                painter = painterResource(id = com.example.R.drawable.profile_banner_creative_1782024514991),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.6f)
            )
            
            // Scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(AccentRed, AccentBlue)
                                )
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(PrimaryBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 24.sp
                        )
                        Text(
                            text = role,
                            color = AccentBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (availabilityStatus == "OPEN_TO_PROJECTS") NeonEmerald else AccentRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (availabilityStatus == "OPEN_TO_PROJECTS") "Open for Projects" else "Not Available",
                                color = if (availabilityStatus == "OPEN_TO_PROJECTS") NeonEmerald else AccentRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("share_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Profile",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("edit_profile_entry_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatsRow(completedWorkspacesCount: Int, endorsementsCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), label = "COMPLETED", value = "$completedWorkspacesCount", icon = Icons.Default.CheckCircle, color = NeonEmerald)
        StatCard(modifier = Modifier.weight(1f), label = "ENDORSED", value = "$endorsementsCount", icon = Icons.Default.Verified, color = AccentBlue)
        StatCard(modifier = Modifier.weight(1f), label = "LEVEL", value = "PRO", icon = Icons.Default.Star, color = CrispAmber)
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ProfileHeader(name: String, role: String, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(SurfaceColor, SurfaceColor.copy(alpha = 0.6f))
                    )
                )
                .border(
                    1.dp,
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(AccentRed.copy(alpha = 0.3f), AccentBlue.copy(alpha = 0.3f))
                    ),
                    RoundedCornerShape(24.dp)
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(AccentRed, AccentBlue)
                        )
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(PrimaryBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(text = role, color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .background(SurfaceLightColor, CircleShape)
                    .size(40.dp)
                    .testTag("edit_profile_entry_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = AccentRed,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun DashboardTaskItem(task: ProductionTask, workspaceName: String, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (task.kanbanLane.uppercase()) {
                            "TODO" -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            "SCRIPTING" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            "EDITING" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            "VFX" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                            "REVIEW" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (task.kanbanLane.uppercase()) {
                        "TODO" -> Icons.Default.Circle
                        "SCRIPTING" -> Icons.Default.EditNote
                        "EDITING" -> Icons.Default.VideoLibrary
                        "VFX" -> Icons.Default.AutoAwesome
                        "REVIEW" -> Icons.Default.RateReview
                        else -> Icons.Default.Task
                    },
                    contentDescription = null,
                    tint = when (task.kanbanLane.uppercase()) {
                        "TODO" -> MaterialTheme.colorScheme.outline
                        "SCRIPTING" -> MaterialTheme.colorScheme.primary
                        "EDITING" -> MaterialTheme.colorScheme.secondary
                        "VFX" -> MaterialTheme.colorScheme.error
                        "REVIEW" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = workspaceName,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Text(
                    text = task.kanbanLane,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SpecialtyChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun PortfolioItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(SurfaceColor, RoundedCornerShape(12.dp))
            .border(1.dp, ColorDivider, RoundedCornerShape(12.dp))
            .clickable { 
                /* View Masterpiece placeholder */
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with beautiful ambient overlay
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(AccentRed.copy(alpha = 0.8f), AccentBlue.copy(alpha = 0.8f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Published Masterpiece", color = TextSecondary, fontSize = 12.sp)
        }
        val context = androidx.compose.ui.platform.LocalContext.current
        IconButton(onClick = {
            val viewIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://youtube.com/hashtag/creatorcoop"))
            try {
                context.startActivity(viewIntent)
            } catch (e: Exception) {
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, title)
                    putExtra(android.content.Intent.EXTRA_TEXT, "Check out our published masterpiece on Creator Co-Op: $title\nhttps://creatorcoop.app/masterpieces")
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Masterpiece"))
            }
        }, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Launch, contentDescription = "View", tint = AccentBlue, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun BackHandler(onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onBack)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(modifier, horizontalArrangement, verticalArrangement, content = content)
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
        if (!isLast) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = ColorDivider)
        }
    }
}
