package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class BulkAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isPrimary: Boolean = false,
    val contentColor: Color = Color.White
)

@Composable
fun BulkActionBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    actions: List<BulkAction>,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, ColorDivider, RoundedCornerShape(14.dp))
                .testTag("bulk_operations_action_bar"),
            color = SurfaceColor,
            shape = RoundedCornerShape(14.dp),
            tonalElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onClearSelection,
                        modifier = Modifier.size(28.dp).testTag("clear_bulk_selection_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Selection", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(
                            text = "$selectedCount selected",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Batch Actions Available",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Actions List
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions.forEach { action ->
                        Button(
                            onClick = action.onClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (action.isPrimary) AccentBlue else SurfaceLightColor,
                                contentColor = action.contentColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("bulk_action_${action.label.lowercase().replace(" ", "_")}")
                        ) {
                            Icon(action.icon, null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(action.label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
