package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodels.GlobalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferTeammateScreen(
    userProfile: UserProfile?,
    globalViewModel: GlobalViewModel,
    onBack: () -> Unit
) {
    if (userProfile == null) return

    val context = LocalContext.current
    var friendCode by remember { mutableStateOf("") }
    val referralsCount by remember(userProfile.id) { globalViewModel.getSuccessfulReferralCount(userProfile.id) }.collectAsState(initial = 0)
    var alreadyRedeemed by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile.id) {
        alreadyRedeemed = globalViewModel.hasAppliedReferralCode(userProfile.id)
    }

    // Ensure referral code is generated on screen enter if missing
    LaunchedEffect(userProfile) {
        globalViewModel.generateOrGetReferralCode(userProfile)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("refer_teammate_screen"),
        containerColor = PrimaryBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Visual Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(CrispAmber.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Reward",
                                tint = CrispAmber,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "Grow the Co-Op Network",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Invite content creators, editors, VFX artists, and animators. When they join and complete their profile, both of you unlock 7 days of Premium trial extension instantly.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Share Referral Card
            item {
                Text(
                    text = "YOUR REFERRAL PASS",
                    color = AccentBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Your Unique Referral Code",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val refCode = userProfile.referralCode?.ifEmpty { "CODE" } ?: "CODE"
                        val refUrl = "https://creatorcoop.com/join?ref=$refCode"
                        
                        val copyToClipboard = { textToCopy: String, label: String ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(label, textToCopy)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }

                        val shareReferralPass = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TITLE, "Join Creator Co-Op")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🚀 Join me on Creator Co-Op to collaborate on productions, split revenues fairly, and manage creative projects!\n\nUse my invite pass: $refCode\nJoin link: $refUrl"
                                )
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Creator Co-Op Invite Pass")
                            context.startActivity(shareIntent)
                        }

                        Surface(
                            color = PrimaryBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, ColorDivider),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { copyToClipboard(refCode, "Referral code") }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = userProfile.referralCode?.ifEmpty { "Generating..." } ?: "Generating...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    modifier = Modifier.testTag("my_referral_code")
                                )
                                IconButton(
                                    onClick = { shareReferralPass() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Code",
                                        tint = AccentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Direct Referral Link",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryBackground, RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, ColorDivider), RoundedCornerShape(8.dp))
                                .clickable { copyToClipboard(refUrl, "Referral link") }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = refUrl,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("referral_link")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Link",
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { shareReferralPass() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("share_invitation_pass_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Share Invitation Pass", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SHARE INVITATION PASS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Referral Status & Redeem Section
            item {
                Text(
                    text = "REDEEM & STATISTICS",
                    color = NeonEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Successful Invites",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$referralsCount completed",
                                fontWeight = FontWeight.Black,
                                color = NeonEmerald,
                                fontSize = 15.sp,
                                modifier = Modifier.testTag("referrals_count_text")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = ColorDivider)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (alreadyRedeemed) {
                            Surface(
                                color = NeonEmerald.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("Referral Pass Active", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("You have already redeemed an invitation pass on this account.", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Were you invited by someone?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = friendCode,
                                onValueChange = { friendCode = it.uppercase() },
                                textStyle = TextStyle(color = Color.White),
                                placeholder = { Text("Enter invite code...", color = TextMuted) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("friend_referral_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = AccentBlue,
                                    unfocusedBorderColor = ColorDivider,
                                    cursorColor = AccentBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (friendCode.isNotBlank()) {
                                        globalViewModel.applyReferralCode(userProfile.id, friendCode)
                                        friendCode = ""
                                        // re-check state
                                        alreadyRedeemed = true
                                    } else {
                                        Toast.makeText(context, "Please enter a valid code", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("apply_referral_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("REDEEM REFERRAL CODE", fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
