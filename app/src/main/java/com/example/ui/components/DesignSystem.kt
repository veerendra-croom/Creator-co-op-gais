package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.composed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.interaction.collectIsPressedAsState
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// --- Spacing & Radius Constants ---
object DS {
    val Space2 = 2.dp
    val Space4 = 4.dp
    val Space6 = 6.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space20 = 20.dp
    val Space24 = 24.dp
    val Space32 = 32.dp
    val Space40 = 40.dp
    val Space48 = 48.dp

    val RadiusSmall = RoundedCornerShape(8.dp)
    val RadiusMedium = RoundedCornerShape(12.dp)
    val RadiusLarge = RoundedCornerShape(16.dp)
    val RadiusExtraLarge = RoundedCornerShape(24.dp)
    
    val BorderWidth = 1.dp
}

@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    action: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = DS.Space16),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DS.Space12)
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceColor, CircleShape)
                        .border(1.dp, ColorDivider, CircleShape)
                        .testTag("page_header_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(DS.Space4))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
        if (action != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DS.Space8)
            ) {
                action()
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    headline: String,
    supportingText: String,
    primaryCtaLabel: String,
    onPrimaryCta: () -> Unit,
    secondaryCtaLabel: String?,
    onSecondaryCta: () -> Unit,
    accentColor: Color,
    showFabCue: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DS.Space16)
        ) {
            // Minimal design accent
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DS.Space8)
            ) {
                Text(
                    text = headline,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = supportingText,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DS.Space12)
            ) {
                Button(
                    onClick = onPrimaryCta,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = DS.RadiusMedium
                ) {
                    val textColor = if (accentColor == CrispAmber) Color.Black else Color.White
                    Text(primaryCtaLabel.uppercase(), fontWeight = FontWeight.Black, color = textColor, fontSize = 14.sp, letterSpacing = 1.sp)
                }
                
                if (secondaryCtaLabel != null) {
                    TextButton(
                        onClick = onSecondaryCta,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(secondaryCtaLabel.uppercase(), color = accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.sp)
                    }
                }
            }

            if (showFabCue) {
                HorizontalDivider(color = ColorDivider.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = DS.Space8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(-45f)
                    )
                    Spacer(modifier = Modifier.width(DS.Space8))
                    Text(
                        text = "START FROM THE ENGINE FAB BELOW",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.05f),
        Color.White.copy(alpha = 0.15f),
        Color.White.copy(alpha = 0.05f),
    )

    this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim, y = translateAnim)
        )
    )
}

@Composable
fun MetricSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().height(80.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = DS.RadiusMedium,
        border = BorderStroke(DS.BorderWidth, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(DS.Space12)) {
            Box(modifier = Modifier.width(60.dp).height(12.dp).clip(DS.RadiusSmall).shimmer())
            Spacer(modifier = Modifier.height(DS.Space8))
            Box(modifier = Modifier.width(100.dp).height(24.dp).clip(DS.RadiusSmall).shimmer())
        }
    }
}

@Composable
fun WorkspaceSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).shimmer())
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.width(120.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.width(60.dp).height(10.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.width(50.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                    Box(modifier = Modifier.width(50.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                }
                Box(modifier = Modifier.width(80.dp).height(16.dp).clip(RoundedCornerShape(8.dp)).shimmer())
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DS.Space8),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
        if (action != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DS.Space8)
            ) {
                action()
            }
        }
    }
}

@Composable
fun KPICard(
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color = AccentBlue,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    if (isLoading) {
        MetricSkeleton(modifier = modifier)
        return
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = DS.RadiusLarge,
        colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DS.Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(DS.Space8))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun WorkspaceCard(
    name: String,
    platform: String,
    status: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = DS.RadiusLarge,
        colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DS.Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (status.equals("active", ignoreCase = true)) NeonEmerald.copy(alpha = 0.15f) else CrispAmber.copy(alpha = 0.15f),
                    shape = DS.RadiusSmall,
                    border = BorderStroke(1.dp, if (status.equals("active", ignoreCase = true)) NeonEmerald.copy(alpha = 0.4f) else CrispAmber.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = status.uppercase(),
                        color = if (status.equals("active", ignoreCase = true)) NeonEmerald else CrispAmber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space4)
                    )
                }
            }
            Spacer(modifier = Modifier.height(DS.Space4))
            Text(
                text = platform,
                style = MaterialTheme.typography.bodySmall,
                color = AccentBlue,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(DS.Space12))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DS.Space4)
            ) {
                Text(
                    text = "Go to Workspace",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun UserCard(
    name: String,
    role: String,
    avatarLetter: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(DS.RadiusMedium)
            .background(SurfaceLightColor)
            .border(1.dp, ColorDivider.copy(alpha = 0.5f), DS.RadiusMedium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(DS.Space12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.15f))
                .border(1.dp, AccentBlue.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarLetter.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = AccentBlue
            )
        }
        Spacer(modifier = Modifier.width(DS.Space12))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = role,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun TaskCard(
    title: String,
    lane: String,
    priority: String = "Medium",
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = DS.RadiusMedium,
        colors = CardDefaults.cardColors(containerColor = SurfaceLightColor),
        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DS.Space16)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ColorDivider,
                    shape = DS.RadiusSmall
                ) {
                    Text(
                        text = lane.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space2)
                    )
                }
                val priorityColor = when (priority.uppercase()) {
                    "HIGH" -> AccentRed
                    "LOW" -> NeonEmerald
                    else -> CrispAmber
                }
                Surface(
                    color = priorityColor.copy(alpha = 0.15f),
                    shape = DS.RadiusSmall,
                    border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = priority,
                        color = priorityColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = DS.Space6, vertical = DS.Space2)
                    )
                }
            }
            Spacer(modifier = Modifier.height(DS.Space12))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ActivityCard(
    icon: ImageVector,
    text: String,
    time: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = DS.Space8),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(DS.Space12))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(DS.Space2))
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: ImageVector = Icons.Default.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DS.Space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(DS.Space12))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(DS.Space16))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = DS.RadiusMedium
            ) {
                Text(text = actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(DS.Space32),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = AccentBlue,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DS.Space24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = AccentRed,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(DS.Space12))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = AccentRed,
            fontWeight = FontWeight.SemiBold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(DS.Space16))
            TextButton(
                onClick = onRetry
            ) {
                Text(text = "Retry", color = AccentBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                tint = TextSecondary
            )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = SurfaceColor,
            unfocusedContainerColor = SurfaceColor,
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = ColorDivider.copy(alpha = 0.5f)
        ),
        shape = DS.RadiusMedium,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FilterBar(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(DS.Space8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = option.equals(selectedOption, ignoreCase = true)
            Surface(
                modifier = Modifier
                    .clickable { onOptionSelected(option) },
                color = if (isSelected) AccentBlue else SurfaceLightColor,
                shape = DS.RadiusMedium,
                border = BorderStroke(1.dp, if (isSelected) AccentBlue else ColorDivider.copy(alpha = 0.5f))
            ) {
                Text(
                    text = option,
                    color = if (isSelected) Color.White else TextSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = DS.Space16, vertical = DS.Space8)
                )
            }
        }
    }
}

@Composable
fun KPISkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = DS.RadiusLarge,
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.width(80.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).shimmer())
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.width(100.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmer())
        }
    }
}

@Composable
fun TaskSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = DS.RadiusMedium,
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.width(60.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmer())
                Box(modifier = Modifier.width(40.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmer())
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).shimmer())
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.width(150.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmer())
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DS.Space32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SurfaceColor)
                .border(DS.BorderWidth, ColorDivider, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(DS.Space24))
        Text(
            text = title.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(DS.Space8))
        Text(
            text = description,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = DS.Space16)
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(DS.Space24))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = DS.RadiusMedium
            ) {
                Text(actionText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DS.Space16),
        colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.05f)),
        shape = DS.RadiusMedium,
        border = BorderStroke(DS.BorderWidth, AccentRed.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(DS.Space16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(DS.Space12))
            Column(modifier = Modifier.weight(1f)) {
                Text("OPERATIONAL FAULT", color = AccentRed, style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Black)
                Text(message, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
            }
            TextButton(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("RETRY", color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun TrustBadge(
    score: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NeonEmerald.copy(alpha = 0.1f),
        shape = DS.RadiusSmall,
        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = score.toString(),
                color = NeonEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun SecureShardBadge(modifier: Modifier = Modifier) {
    Surface(
        color = AccentBlue.copy(alpha = 0.12f),
        shape = DS.RadiusSmall,
        border = BorderStroke(DS.BorderWidth, AccentBlue.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Https,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = "SECURE SHARD",
                color = AccentBlue,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun UserRoleBadge(
    role: String,
    isPro: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isAdmin = role.equals("ADMIN", ignoreCase = true)
    val isCreator = role.equals("CREATOR", ignoreCase = true)

    val bgColor = when {
        isAdmin -> CrispAmber.copy(alpha = 0.15f)
        isPro -> NeonEmerald.copy(alpha = 0.15f)
        isCreator -> AccentBlue.copy(alpha = 0.15f)
        else -> SurfaceLightColor
    }
    val contentColor = when {
        isAdmin -> CrispAmber
        isPro -> NeonEmerald
        isCreator -> AccentBlue
        else -> TextSecondary
    }
    val label = when {
        isAdmin -> "ADMIN"
        isPro -> "PRO"
        isCreator -> "CREATOR"
        else -> "FREE"
    }
    val icon = when {
        isAdmin -> Icons.Default.Security
        isPro -> Icons.Default.Star
        isCreator -> Icons.Default.WorkspacePremium
        else -> Icons.Default.Person
    }

    Surface(
        color = bgColor,
        contentColor = contentColor,
        shape = DS.RadiusSmall,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(11.dp))
            Text(text = label, color = contentColor, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun ReliabilityBadge(
    badge: String,
    modifier: Modifier = Modifier
) {
    val bgColor = when (badge.lowercase()) {
        "diamond" -> AccentBlue.copy(alpha = 0.18f)
        "platinum" -> NeonEmerald.copy(alpha = 0.18f)
        "gold" -> CrispAmber.copy(alpha = 0.18f)
        "silver" -> Color(0xFFE2E8F0).copy(alpha = 0.15f)
        else -> SurfaceLightColor
    }
    val contentColor = when (badge.lowercase()) {
        "diamond" -> AccentBlue
        "platinum" -> NeonEmerald
        "gold" -> CrispAmber
        "silver" -> Color(0xFFE2E8F0)
        else -> TextMuted
    }
    val icon = when (badge.lowercase()) {
        "diamond" -> Icons.Default.Diamond
        "platinum" -> Icons.Default.WorkspacePremium
        "gold" -> Icons.Default.Stars
        "silver" -> Icons.Default.VerifiedUser
        else -> Icons.Default.Shield
    }

    Surface(
        color = bgColor,
        contentColor = contentColor,
        shape = DS.RadiusSmall,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(11.dp))
            Text(text = badge.uppercase(), color = contentColor, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun SubscriptionTierBadge(
    isPro: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isPro) NeonEmerald.copy(alpha = 0.15f) else ColorDivider.copy(alpha = 0.3f)
    val contentColor = if (isPro) NeonEmerald else TextMuted
    val label = if (isPro) "CREATOR PRO" else "STANDARD FREE"
    val icon = if (isPro) Icons.Default.Star else Icons.Default.Person

    Surface(
        color = bgColor,
        contentColor = contentColor,
        shape = DS.RadiusSmall,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(11.dp))
            Text(text = label, color = contentColor, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
fun StatusTagBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, contentColor) = when (status.uppercase()) {
        "ACTIVE", "VERIFIED", "RESOLVED", "SUCCESS", "APPROVED" -> Pair(NeonEmerald.copy(alpha = 0.15f), NeonEmerald)
        "PENDING", "IN_PROGRESS", "OPEN", "WAITING" -> Pair(CrispAmber.copy(alpha = 0.15f), CrispAmber)
        "CRITICAL", "FAILED", "SUSPENDED", "BLOCKED", "HIGH" -> Pair(AccentRed.copy(alpha = 0.15f), AccentRed)
        else -> Pair(AccentBlue.copy(alpha = 0.15f), AccentBlue)
    }

    Surface(
        color = bgColor,
        shape = DS.RadiusSmall,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Text(
            text = status.replace("_", " ").uppercase(),
            color = contentColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SearchEmptyState(
    message: String = "We couldn't find any results matching your filters or keywords.",
    suggestion: String = "Try adjusting your search terms, clearing filters, or typing a different query."
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DS.Space16),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.5f)),
        shape = DS.RadiusMedium,
        border = BorderStroke(DS.BorderWidth, ColorDivider.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(DS.Space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DS.Space12)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.1f))
                    .border(1.dp, AccentBlue.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = "NO RESULTS FOUND",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Text(
                text = suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

fun Modifier.bounceScale(interactionSource: MutableInteractionSource) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounceScale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.bounceScale() = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounceScale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.bounceClick(
    enabled: Boolean = true,
    onClick: () -> Unit
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounceClickScale"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            enabled = enabled,
            onClick = onClick
        )
}

fun Modifier.shake(trigger: Any?) = composed {
    var isShaking by remember { mutableStateOf(false) }
    
    LaunchedEffect(trigger) {
        if (trigger != null) {
            isShaking = true
            delay(350)
            isShaking = false
        }
    }
    
    val translationX by animateFloatAsState(
        targetValue = 0f,
        animationSpec = if (isShaking) {
            keyframes {
                durationMillis = 350
                0f at 0
                -10f at 50
                10f at 100
                -10f at 150
                10f at 200
                -5f at 250
                5f at 300
                0f at 350
            }
        } else {
            snap()
        },
        label = "shakeTranslation"
    )
    
    this.graphicsLayer {
        this.translationX = translationX
    }
}

fun Modifier.glowOnFocus(
    isFocused: Boolean,
    glowColor: Color = AccentBlue,
    shape: Shape = RoundedCornerShape(12.dp)
) = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    val borderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "borderAlpha"
    )
    
    this.then(
        if (isFocused) {
            Modifier.border(
                width = 1.5.dp,
                color = glowColor.copy(alpha = borderAlpha * glowAlpha),
                shape = shape
            )
        } else {
            Modifier
        }
    )
}

@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    prefix: String = ""
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (prefix.isNotEmpty()) {
            Text(text = prefix, style = style, color = color)
        }
        val valueString = value.toString()
        valueString.forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    } else {
                        (slideInVertically { height -> -height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> height } + fadeOut())
                    }.using(
                        SizeTransform(clip = false)
                    )
                },
                label = "counter_char_$index"
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    style = style,
                    color = color,
                    softWrap = false
                )
            }
        }
    }
}
