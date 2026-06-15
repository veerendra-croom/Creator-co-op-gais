package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.myUser.collectAsState()

    if (user == null) {
        Box(modifier = modifier.fillMaxSize().background(PrimaryBackground), contentAlignment = Alignment.Center) {
            Text("Loading user profile...", color = Color.White)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Character Profile Row
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(2.dp, AccentRed, CircleShape)
                ) {
                    AsyncImage(
                        model = user!!.avatarUrl,
                        contentDescription = "User Avatar",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user!!.displayName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Pro Status",
                        tint = ColorSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "${user!!.primaryRole} • Karma: ${user!!.karmaScore}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // FINANCIAL LEDGER WALLET CARD
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ledger_wallet_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TOTAL ESCROW SECURED BALANCES",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${String.format("%.2f", user!!.availableBalance)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Available Balance (Linked checking account Ready)",
                        color = ColorSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider(color = ColorDivider)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("PENDING CLEARING", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$${String.format("%.2f", user!!.pendingBalance)}", color = ColorWarning, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }

                        Column {
                            Text("TREASURY SAVINGS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$${String.format("%.2f", user!!.treasuryBalance)}", color = AccentRed, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.performStripeWithdrawal() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_balance_button")
                    ) {
                        Text(
                            text = "WITHDRAW VIA STRIPE CONNECT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }

        // BIOGRAPHICAL STATS & EXPERIENCE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CO-OP METADATA", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Email address", color = TextSecondary, fontSize = 12.sp)
                        Text(user!!.email, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("E.164 Mobile", color = TextSecondary, fontSize = 12.sp)
                        Text(user!!.phone, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Escrow Account Status", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = if (user!!.stripeAccountId.isNotEmpty()) "Connected VERIFIED" else "Pending Verification",
                            color = if (user!!.stripeAccountId.isNotEmpty()) ColorSuccess else ColorWarning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // PORTFOLIO VIRTUAL SHOWCASES
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PORTFOLIO VIRTUAL SHOWCASES", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    listOf(
                        "youtube.com/vibe_creative" to Icons.Default.SmartDisplay,
                        "behance.net/vibe_creative" to Icons.Default.InsertPhoto,
                        "github.com/vibe_creative" to Icons.Default.Terminal
                    ).forEach { (link, icon) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Icon(imageVector = icon, contentDescription = "Asset Link", tint = AccentRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = link,
                                color = AccentBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    viewModel.toastMessage.value = "Launching showcase browser..."
                                }
                            )
                        }
                    }
                }
            }
        }

        // Signout Simulation Link
        item {
            TextButton(
                onClick = {
                    viewModel.isOnboarded.value = false
                    viewModel.onboardingStep.value = 1
                    viewModel.toastMessage.value = "Session signed out. Re-launch onboarding."
                },
                modifier = Modifier.testTag("simulate_signout_button")
            ) {
                Text("Re-test App Onboarding Profile Setup", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
