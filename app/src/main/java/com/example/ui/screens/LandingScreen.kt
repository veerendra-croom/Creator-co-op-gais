package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.GlobalViewModel

@Composable
fun LandingScreen(
    globalViewModel: GlobalViewModel,
    authViewModel: AuthViewModel,
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onLaunchUserDemo: () -> Unit,
    onLaunchAdminDemo: () -> Unit,
    onLaunchFullDemo: () -> Unit,
    onViewOnboarding: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = PrimaryBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PrimaryBackground)
                .testTag("landing_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            // Hero Top Banner with Gradient Fade
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Creator Co-Op Hero",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        PrimaryBackground.copy(alpha = 0.6f),
                                        PrimaryBackground
                                    )
                                )
                            )
                    )

                    // Top Live Status Tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceColor.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, ColorDivider),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NeonEmerald,
                                modifier = Modifier.size(7.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "22 LIVE MODULES ACTIVE",
                                color = NeonEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Brand Emblem & Identity
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-40).dp)
                            .size(86.dp)
                            .shadow(20.dp, shape = RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(SurfaceColor)
                            .border(1.5.dp, Brush.linearGradient(listOf(AccentBlue, NeonEmerald)), RoundedCornerShape(22.dp))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                        )
                    }

                    Text(
                        text = "CREATOR CO-OP",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.offset(y = (-24).dp)
                    )

                    Text(
                        text = "The Decentralized Production Syndicate for Independent Creators",
                        color = AccentBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .offset(y = (-20).dp)
                            .padding(horizontal = 12.dp)
                    )

                    Text(
                        text = "Syndicate talent, lock in SHA-256 milestone agreements, and co-produce premium multimedia with automated revenue allocation.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .offset(y = (-14).dp)
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            // ========================================================
            // FRONT & CENTER: THE FULL INTERACTIVE APP DEMO HUB
            // ========================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                        .testTag("landing_demo_hub_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(
                        listOf(NeonEmerald, AccentBlue, Color(0xFF8B5CF6))
                    ))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = NeonEmerald.copy(alpha = 0.2f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "FULL PLATFORM DEMO",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        letterSpacing = 0.8.sp
                                    )
                                    Text(
                                        text = "No Registration or Sign-In Required",
                                        color = NeonEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonEmerald.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, NeonEmerald)
                            ) {
                                Text(
                                    text = "INSTANT ACCESS",
                                    color = NeonEmerald,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Take a complete, hands-on guided walkthrough across all screens, forms, telemetry dashboards, and governance controls.",
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Launch Button: Complete Omni Demo (22 Stops)
                        Button(
                            onClick = onLaunchFullDemo,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("landing_launch_full_demo_button")
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "LAUNCH COMPLETE DEMO (22 STOPS)",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Split Direct Demo Mode Launchers (User Side vs Admin Side)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onLaunchUserDemo,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AccentBlue),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("landing_user_demo_button")
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "USER SIDE (12)",
                                    color = AccentBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }

                            OutlinedButton(
                                onClick = onLaunchAdminDemo,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AccentRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("landing_admin_demo_button")
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = AccentRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ADMIN SIDE (10)",
                                    color = AccentRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ========================================================
            // PRIMARY AUTHENTICATION & ONBOARDING GATEWAYS
            // ========================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sign In Button
                    Button(
                        onClick = onNavigateToSignIn,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("landing_signin_button")
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SIGN IN TO WORKSPACE",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Register Button
                    OutlinedButton(
                        onClick = onNavigateToSignUp,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ColorDivider),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("landing_signup_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CREATE CREATOR ACCOUNT / REGISTER",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Read Onboarding Manifesto Walkthrough Button
                    TextButton(
                        onClick = onViewOnboarding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("landing_onboarding_manifesto_button")
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = CrispAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Explore Onboarding Slides & Syndicate Manifesto",
                            color = CrispAmber,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
            }

            // ========================================================
            // PLATFORM PILLARS GRID (HIGH PRO MAX AESTHETIC)
            // ========================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                ) {
                    Text(
                        text = "CORE SYNDICATE ARCHITECTURE",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    val pillars = listOf(
                        Triple(Icons.Default.Workspaces, "Agile Kanban Workspaces", "Deliverable queues, subtask completion meters & encrypted cloud asset vaults."),
                        Triple(Icons.Default.Gavel, "Cryptographic Escrow", "SHA-256 multi-sig milestone contracts with automated disbursement triggers."),
                        Triple(Icons.Default.TravelExplore, "Syndicate Discovery", "Pitch incubator, talent filters & open paid role applications with rate cards."),
                        Triple(Icons.Default.Tune, "Zero-Downtime Governance", "Live feature flags, dynamic onboarding slide CMS & audit security logs.")
                    )

                    pillars.forEach { (icon, title, desc) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceLightColor,
                            border = BorderStroke(1.dp, ColorDivider),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AccentBlue.copy(alpha = 0.15f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = desc,
                                        color = TextSecondary,
                                        fontSize = 10.5.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ========================================================
            // EXECUTIVE FOUNDER INTEGRITY & CONTACT FOOTER
            // ========================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EXECUTIVE ATTRIBUTION & ESCALATION",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Botla Veerendra (Founder) • Macha Praveen (Co-Founder)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:veerendrabotla@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Inquiry for Founder Botla Veerendra")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Contact Botla Veerendra"))
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, ColorDivider),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Botla Veerendra", fontSize = 9.5.sp, color = TextPrimary)
                            }

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:praveenmacha777@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Inquiry for Co-Founder Macha Praveen")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Contact Macha Praveen"))
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, ColorDivider),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Macha Praveen", fontSize = 9.5.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
