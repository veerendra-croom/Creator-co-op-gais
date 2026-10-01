package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodels.GlobalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferFriendDialog(
    userProfile: com.example.data.model.UserProfile,
    globalViewModel: GlobalViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var friendCode by remember { mutableStateOf("") }
    val referralsCount by remember(userProfile.id) { globalViewModel.getSuccessfulReferralCount(userProfile.id) }.collectAsState(initial = 0)
    
    val activeCode = remember(userProfile.referralCode, userProfile.id) {
        userProfile.referralCode?.takeIf { it.isNotBlank() } ?: ("REF" + userProfile.id.take(6).uppercase())
    }
    val referralLink = "https://creatorcoop.com/join?ref=$activeCode"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = SurfaceColor,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, ColorDivider),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("refer_friend_dialog")
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Refer a Friend",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Invite friends to Creator Co-Op! When they complete their profile, both of you get 7 days of Premium trial extension.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(20.dp))

                // User's Own Code and Link
                Text("Your Referral Code", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = PrimaryBackground,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ColorDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activeCode,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.testTag("my_referral_code")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Referral Code", activeCode))
                                    Toast.makeText(context, "Referral Code Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy Code",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Join me on Creator Co-Op")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Join my creative crew on Creator Co-Op! Use my referral link: $referralLink (Code: $activeCode) to claim 7 days Premium extension."
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Referral Link"))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Referral Link", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = PrimaryBackground.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = referralLink,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f).testTag("referral_link")
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Referral Link", referralLink))
                                Toast.makeText(context, "Referral Link Copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = ColorDivider)
                Spacer(modifier = Modifier.height(16.dp))

                // Successful Referrals Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Successful Referrals:", fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = "$referralsCount completed",
                        fontWeight = FontWeight.Black,
                        color = ColorSuccess,
                        fontSize = 15.sp,
                        modifier = Modifier.testTag("referrals_count_text")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = ColorDivider)
                Spacer(modifier = Modifier.height(16.dp))

                // Enter Friend's Referral Code Section
                Text("Were You Referred by a Friend?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = friendCode,
                    onValueChange = { friendCode = it.uppercase() },
                    textStyle = TextStyle(color = Color.White),
                    placeholder = { Text("Enter their code...", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().testTag("friend_referral_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (friendCode.isNotBlank()) {
                            globalViewModel.applyReferralCode(userProfile.id, friendCode)
                            friendCode = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("apply_referral_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("REDEEM REFERRAL CODE", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Text("CLOSE", color = TextSecondary)
                }
            }
        }
    }
}
