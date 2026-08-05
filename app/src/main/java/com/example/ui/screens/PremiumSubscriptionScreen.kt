package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PremiumSubscriptionScreen(
    globalViewModel: com.example.ui.viewmodels.GlobalViewModel,
    userProfile: com.example.data.model.UserProfile?,
    onBack: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf("ANNUAL") } // MONTHLY or ANNUAL
    var showBillingDialog by remember { mutableStateOf(false) }

    val isPro = userProfile?.isVerifiedPro ?: false

    if (showBillingDialog && userProfile != null) {
        val price = if (selectedPlan == "MONTHLY") "$19.99" else "$159.99"
        val label = if (selectedPlan == "MONTHLY") "Creator Pro (Monthly)" else "Creator Pro (Annual)"
        val desc = if (selectedPlan == "MONTHLY") "Flexible month-to-month access to all pro features." else "Full year of premium tools. Save 20% compared to monthly plan."

        com.example.ui.components.BillingSimulatorDialog(
            skuName = label,
            skuPrice = price,
            skuDescription = desc,
            onDismiss = { showBillingDialog = false },
            onPurchaseSuccess = {
                if (selectedPlan == "MONTHLY") {
                    globalViewModel.purchaseProMonthly(userProfile.id)
                } else {
                    globalViewModel.purchaseProAnnual(userProfile.id)
                }
                showBillingDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Premium Status Card / Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPro) NeonEmerald.copy(alpha = 0.1f) else Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, if (isPro) NeonEmerald else ColorDivider)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(AccentBlue, NeonEmerald))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = "Pro Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isPro) "PRO STATUS ACTIVE" else "UPGRADE TO CREATOR PRO",
                            color = if (isPro) NeonEmerald else Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = if (isPro) "You have full access to all verified features and priority roles." else "Unlock premium search placement, zero platform ads, and Priority Discovery.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
                        )
                    }
                }
            }

            // Savings comparison / alert card (Best Value indicator)
            if (!isPro) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Percent,
                                contentDescription = "Promo",
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Save 20% with Annual Billing",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pay $159.99/year (~$13.33/mo) compared to $239.88/year with monthly billing.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Plan Selection (Only show if not Pro, or show disabled/current indicator)
            if (!isPro) {
                item {
                    Text(
                        text = "SELECT YOUR PLAN",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    // Monthly Card
                    Surface(
                        onClick = { selectedPlan = "MONTHLY" },
                        color = if (selectedPlan == "MONTHLY") AccentBlue.copy(alpha = 0.1f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = if (selectedPlan == "MONTHLY") 2.dp else 1.dp,
                            color = if (selectedPlan == "MONTHLY") AccentBlue else ColorDivider
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("select_monthly_plan_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Subscription",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$19.99 / month",
                                    color = if (selectedPlan == "MONTHLY") AccentBlue else Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Cancel anytime. Billed monthly.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            RadioButton(
                                selected = selectedPlan == "MONTHLY",
                                onClick = { selectedPlan = "MONTHLY" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AccentBlue,
                                    unselectedColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                item {
                    // Annual Card
                    Surface(
                        onClick = { selectedPlan = "ANNUAL" },
                        color = if (selectedPlan == "ANNUAL") AccentBlue.copy(alpha = 0.1f) else Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = if (selectedPlan == "ANNUAL") 2.dp else 1.dp,
                            color = if (selectedPlan == "ANNUAL") AccentBlue else ColorDivider
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("select_annual_plan_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Annual Subscription",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = NeonEmerald,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "SAVE 20%",
                                            color = PrimaryBackground,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$159.99 / year",
                                    color = if (selectedPlan == "ANNUAL") AccentBlue else Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Equates to just $13.33/month. Billed annually.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            RadioButton(
                                selected = selectedPlan == "ANNUAL",
                                onClick = { selectedPlan = "ANNUAL" },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AccentBlue,
                                    unselectedColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showBillingDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("subscribe_plan_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (selectedPlan == "MONTHLY") "GET MONTHLY PRO • $19.99" else "GET ANNUAL PRO • $159.99",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            } else {
                // If already Pro
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF11151D)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Your Active Subscription",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Verified Pro Level Access Active",
                                color = NeonEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Next Renewal Date: August 28, 2027",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Benefits checklist section (remains relevant for both subscribed and unsubscribed users)
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "VERIFIED PRO MEMBERSHIP INCLUDES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item { BenefitRow("Priority open roles placement in discovery feeds") }
            item { BenefitRow("Zero third-party or platform-sponsored ads") }
            item { BenefitRow("Exclusive badge and trust rank multiplier") }
            item { BenefitRow("Advanced portfolio analytics and views tracker") }
            item { BenefitRow("Unlimited Active Syndicates and Workspace Channels") }

            if (isPro) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            // Simulator action
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("manage_billing_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                        border = BorderStroke(1.dp, ColorDivider),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Manage Play Store Subscription", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitRow(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
        Text(title, color = Color.White, fontSize = 14.sp)
    }
}
