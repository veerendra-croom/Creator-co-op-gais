package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onPurchaseMonthly: () -> Unit,
    onPurchaseAnnual: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf("MONTHLY") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            color = SurfaceColor,
            shape = RoundedCornerShape(32.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(AccentBlue, NeonEmerald))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Stars, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Switch to Verified Pro",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    "The professional toolkit for digital creators",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProFeatureItem("Priority role listing placement")
                    ProFeatureItem("Zero platform-sponsored ads")
                    ProFeatureItem("Advanced analytics & reach metrics")
                    ProFeatureItem("Unlimited active syndicates")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Plan Selection
                PlanCard(
                    title = "Monthly Access",
                    price = "$9.99 / mo",
                    subtitle = "Flexible month-to-month access",
                    isSelected = selectedPlan == "MONTHLY",
                    onClick = { selectedPlan = "MONTHLY" }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PlanCard(
                    title = "Annual Pro Plan",
                    price = "$79.99 / yr",
                    subtitle = "Save 33% • Best value for professionals",
                    isSelected = selectedPlan == "ANNUAL",
                    onClick = { selectedPlan = "ANNUAL" },
                    badge = "BEST VALUE"
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (selectedPlan == "MONTHLY") onPurchaseMonthly() else onPurchaseAnnual()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (selectedPlan == "MONTHLY") "GET MONTHLY PRO" else "GET ANNUAL PRO",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Maybe later", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badge: String? = null
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) AccentBlue.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) AccentBlue else ColorDivider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                badge?.let {
                    Surface(
                        color = NeonEmerald,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            it,
                            color = PrimaryBackground,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(price, color = if (isSelected) AccentBlue else Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(vertical = 4.dp))
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProFeatureItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Check, null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = TextPrimary, fontSize = 14.sp)
    }
}
