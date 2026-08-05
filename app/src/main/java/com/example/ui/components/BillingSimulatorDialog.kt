package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun BillingSimulatorDialog(
    skuName: String = "Open Role 48h Boost",
    skuPrice: String = "$4.99",
    skuDescription: String = "Pushes your listing to the top of discovery feeds with an illuminated badge.",
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var purchaseCompleted by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("billing_simulator_dialog"),
            color = Color(0xFF0F172A), // Dark Obsidian Theme (#0F172A)
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Google Play billing overlay mockup)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Secured",
                        tint = NeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Google Play Billing Service",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (!purchaseCompleted) {
                    // SKU details
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AccentBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Boost SKU",
                            tint = AccentBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = skuName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = skuDescription,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Billing summary card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SurfaceColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = "Visa",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Google Play •••• 9010",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = skuPrice,
                                color = AccentBlue,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = AccentBlue,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("billing_progress")
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Authorizing transaction securely...",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        Button(
                            onClick = {
                                isProcessing = true
                                // Simulate 1.5s transaction
                                onPurchaseSuccess()
                                purchaseCompleted = true
                                isProcessing = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("billing_confirm_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "1-TAP BUY",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = "Cancel transaction",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    // Success state
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Success",
                        tint = NeonEmerald,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Transaction Confirmed!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your 48-hour role boost is active. It will rank at the top of the Discovery feed instantly.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Awesome", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
