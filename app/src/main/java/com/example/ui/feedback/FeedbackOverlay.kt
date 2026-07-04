package com.example.ui.feedback

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FeedbackOverlay() {
    val message by FeedbackManager.currentMessage
    
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.fillMaxWidth()
    ) {
        message?.let { msg ->
            LaunchedEffect(msg.id) {
                delay(4000)
                FeedbackManager.clearMessage()
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 80.dp), // Avoid bottom nav
                contentAlignment = Alignment.BottomCenter
            ) {
                val containerColor = when (msg.type) {
                    FeedbackType.SUCCESS -> NeonEmerald
                    FeedbackType.ERROR -> AccentRed
                    FeedbackType.WARNING -> CrispAmber
                    FeedbackType.INFO -> AccentBlue
                }
                
                val icon = when (msg.type) {
                    FeedbackType.SUCCESS -> Icons.Default.CheckCircle
                    FeedbackType.ERROR -> Icons.Default.Error
                    FeedbackType.WARNING -> Icons.Default.Warning
                    FeedbackType.INFO -> Icons.Default.Info
                }

                Surface(
                    color = SurfaceColor,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, containerColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = containerColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = msg.message,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (msg.actionLabel != null) {
                            TextButton(onClick = { msg.onAction?.invoke() }) {
                                Text(
                                    text = msg.actionLabel,
                                    color = containerColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
