package com.example.ui.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun PremiumSnackbar(
    message: FeedbackMessage,
    onDismiss: () -> Unit
) {
    val themeColor = when (message.type) {
        FeedbackType.SUCCESS -> NeonEmerald
        FeedbackType.WARNING -> CrispAmber
        FeedbackType.ERROR -> AccentRed
        FeedbackType.INFO -> AccentBlue
    }

    val icon = when (message.type) {
        FeedbackType.SUCCESS -> Icons.Default.CheckCircle
        FeedbackType.WARNING -> Icons.Default.Warning
        FeedbackType.ERROR -> Icons.Default.Error
        FeedbackType.INFO -> Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(BorderStroke(1.dp, ColorDivider), RoundedCornerShape(16.dp))
            .testTag("premium_snackbar_${message.type.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual accent type indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(themeColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = message.type.name,
                    tint = themeColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.message,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp
                )
            }

            // Action Button
            if (message.actionLabel != null && message.onAction != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        message.onAction.invoke()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColor.copy(alpha = 0.15f),
                        contentColor = themeColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("snackbar_action_button")
                ) {
                    Text(
                        text = message.actionLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Close / Dismiss Button
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("snackbar_dismiss_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun GlobalSnackbarHost(
    hostState: SnackbarHostState
) {
    // Collect messages from the manager
    LaunchedEffect(Unit) {
        FeedbackManager.messages.collectLatest { feedback ->
            // Use current message duration
            val duration = feedback.duration
            
            // Show the Material 3 snackbar
            val result = hostState.showSnackbar(
                message = feedback.message,
                actionLabel = feedback.actionLabel,
                withDismissAction = true,
                duration = duration
            )
            
            // Execute action if clicked
            if (result == SnackbarResult.ActionPerformed) {
                feedback.onAction?.invoke()
            }
        }
    }

    // Standard Scaffold snackbar host utilizing custom visual composition
    SnackbarHost(
        hostState = hostState,
        modifier = Modifier.testTag("global_snackbar_host")
    ) { data ->
        // Retrieve matching feedback details from manager matching message text, or construct dynamic fallback
        val currentFeedback = remember(data.visuals.message) {
            val active = FeedbackManager.currentMessage.value
            if (active != null && active.message == data.visuals.message) {
                active
            } else {
                // Determine feedback type based on keywords
                val msgText = data.visuals.message.lowercase()
                val type = when {
                    msgText.contains("success") || msgText.contains("created") || msgText.contains("saved") || msgText.contains("joined") || msgText.contains("signed") || msgText.contains("accepted") || msgText.contains("completed") || msgText.contains("linked") || msgText.contains("copied") || msgText.contains("profile updated") || msgText.contains("invitation sent") || msgText.contains("dismissed") -> FeedbackType.SUCCESS
                    msgText.contains("error") || msgText.contains("fail") || msgText.contains("denied") || msgText.contains("block") -> FeedbackType.ERROR
                    msgText.contains("warning") || msgText.contains("attention") || msgText.contains("offline") -> FeedbackType.WARNING
                    else -> FeedbackType.INFO
                }
                FeedbackMessage(
                    message = data.visuals.message,
                    type = type,
                    actionLabel = data.visuals.actionLabel,
                    onAction = { data.performAction() }
                )
            }
        }
        
        PremiumSnackbar(
            message = currentFeedback,
            onDismiss = { data.dismiss() }
        )
    }
}
