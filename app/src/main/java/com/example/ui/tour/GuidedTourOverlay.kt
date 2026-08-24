package com.example.ui.tour

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GuidedTourOverlay(
    tourConfig: GuidedTourConfig,
    currentStepIndex: Int,
    targetRects: Map<String, Rect>,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSkip: () -> Unit,
    onDone: () -> Unit
) {
    val steps = tourConfig.steps
    val step = steps.getOrElse(currentStepIndex) { steps.first() }
    val isLastStep = currentStepIndex == steps.size - 1

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val rawTarget = targetRects[step.targetKey]

    // Animate target rect changes for smooth transitions between steps
    val animatedLeft by animateFloatAsState(
        targetValue = rawTarget?.left ?: 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spotlight_left"
    )
    val animatedTop by animateFloatAsState(
        targetValue = rawTarget?.top ?: 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spotlight_top"
    )
    val animatedRight by animateFloatAsState(
        targetValue = rawTarget?.right ?: 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spotlight_right"
    )
    val animatedBottom by animateFloatAsState(
        targetValue = rawTarget?.bottom ?: 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "spotlight_bottom"
    )

    val targetRect = if (rawTarget != null && rawTarget.width > 0 && rawTarget.height > 0) {
        Rect(animatedLeft, animatedTop, animatedRight, animatedBottom)
    } else null

    // Pulse animation for target spotlight border
    val infiniteTransition = rememberInfiniteTransition(label = "spotlight_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val isTargetInBottomHalf = if (targetRect != null) {
        targetRect.center.y > screenHeightPx / 2f
    } else false

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("guided_tour_overlay")
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                // 1. Draw darkened scrim background
                drawRect(Color.Black.copy(alpha = 0.78f))

                // 2. Cut out spotlight rounded rect around target UI element
                if (targetRect != null) {
                    val padPx = 8.dp.toPx()
                    val cutoutLeft = targetRect.left - padPx
                    val cutoutTop = targetRect.top - padPx
                    val cutoutWidth = targetRect.width + (padPx * 2)
                    val cutoutHeight = targetRect.height + (padPx * 2)

                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(cutoutLeft, cutoutTop),
                        size = Size(cutoutWidth, cutoutHeight),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                        blendMode = BlendMode.Clear
                    )
                }
                drawContent()
            }
            .clickable(enabled = false) {}
    ) {
        // Spotlight Glowing Border Overlay
        if (targetRect != null) {
            val padDp = 8.dp
            val leftDp = with(density) { (targetRect.left - padDp.toPx()).toDp() }
            val topDp = with(density) { (targetRect.top - padDp.toPx()).toDp() }
            val widthDp = with(density) { (targetRect.width + padDp.toPx() * 2).toDp() }
            val heightDp = with(density) { (targetRect.height + padDp.toPx() * 2).toDp() }

            Box(
                modifier = Modifier
                    .offset(x = leftDp, y = topDp)
                    .size(width = widthDp, height = heightDp)
                    .border(
                        BorderStroke(2.dp, AccentBlue.copy(alpha = pulseAlpha)),
                        RoundedCornerShape(14.dp)
                    )
            )
        }

        // Contextual Tooltip Card Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = if (targetRect == null) {
                Alignment.Center
            } else if (isTargetInBottomHalf) {
                Alignment.TopCenter
            } else {
                Alignment.BottomCenter
            }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("guided_tour_tooltip_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBackground.copy(alpha = 0.98f)),
                border = BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(
                        listOf(AccentBlue.copy(alpha = 0.9f), ColorDivider, NeonEmerald.copy(alpha = 0.6f))
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header Row: Tour Name + Step Indicator + Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AccentBlue.copy(alpha = 0.2f),
                                border = BorderStroke(0.5.dp, AccentBlue.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = tourConfig.screenTitle.uppercase(),
                                    color = AccentBlue,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STEP ${currentStepIndex + 1} OF ${steps.size}",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onSkip,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("guided_tour_skip_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Skip Tour",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { (currentStepIndex + 1) / steps.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AccentBlue,
                        trackColor = SurfaceLightColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Step Content
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            shape = CircleShape,
                            color = NeonEmerald.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, NeonEmerald),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = step.icon ?: Icons.Default.Info,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = step.title,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = step.description,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Action Hint Banner (if any)
                    if (step.actionHint != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CrispAmber.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CrispAmber.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = CrispAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step.actionHint,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CrispAmber
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Controls Row: Skip | Back | Dots | Next
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Skip text action
                        Text(
                            text = "Skip Tour",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            modifier = Modifier
                                .clickable { onSkip() }
                                .padding(4.dp)
                        )

                        // Center step dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in steps.indices) {
                                val isCurrent = i == currentStepIndex
                                Box(
                                    modifier = Modifier
                                        .size(if (isCurrent) 8.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(if (isCurrent) AccentBlue else ColorDivider)
                                )
                            }
                        }

                        // Back & Next Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentStepIndex > 0) {
                                Button(
                                    onClick = onPrev,
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLightColor),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("guided_tour_prev_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "BACK",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (isLastStep) onDone() else onNext()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isLastStep) NeonEmerald else AccentBlue
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("guided_tour_next_button")
                            ) {
                                Text(
                                    text = if (isLastStep) "DONE 🎉" else "NEXT",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.5.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (isLastStep) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = if (isLastStep) "Done" else "Next",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
