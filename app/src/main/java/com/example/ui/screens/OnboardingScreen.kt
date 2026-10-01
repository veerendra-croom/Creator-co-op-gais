package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    globalViewModel: com.example.ui.viewmodels.GlobalViewModel,
    onComplete: () -> Unit
) {
    val dbSlides by globalViewModel.onboardingSlides.collectAsState()
    val scope = rememberCoroutineScope()

    val onboardingPages = remember(dbSlides) {
        val colorPalette = listOf(AccentBlue, CrispAmber, NeonEmerald, AccentRed, Color(0xFF8B5CF6))
        val imagePalette = listOf(
            com.example.R.drawable.onboarding_collaboration_1782754946609,
            com.example.R.drawable.onboarding_security_1782754961964,
            com.example.R.drawable.onboarding_reputation_1782754975727,
            com.example.R.drawable.img_hero_banner
        )

        if (dbSlides.isNotEmpty()) {
            dbSlides.sortedBy { it.stepIndex }.mapIndexed { idx, slide ->
                val icon = when (slide.iconName) {
                    "Hub" -> Icons.Default.Share
                    "Handshake" -> Icons.Default.Handshake
                    "Groups" -> Icons.Default.Groups
                    "Gavel" -> Icons.Default.Gavel
                    "Verified" -> Icons.Default.Verified
                    "Rocket" -> Icons.Default.RocketLaunch
                    "Security" -> Icons.Default.Security
                    else -> Icons.Default.Star
                }
                val color = colorPalette[idx % colorPalette.size]
                val imageRes = imagePalette[idx % imagePalette.size]
                OnboardingPage(
                    title = slide.title,
                    description = slide.description,
                    icon = icon,
                    color = color,
                    imageRes = imageRes
                )
            }
        } else {
            listOf(
                OnboardingPage(
                    title = "Welcome to the Co-Op",
                    description = "The first premium decentralized platform designed specifically for the Creator Economy.",
                    icon = Icons.Default.Groups,
                    color = AccentBlue,
                    imageRes = com.example.R.drawable.onboarding_collaboration_1782754946609
                ),
                OnboardingPage(
                    title = "Secure Agreements",
                    description = "Lock in your production terms with legal-grade agreements signed directly in-app.",
                    icon = Icons.Default.Gavel,
                    color = CrispAmber,
                    imageRes = com.example.R.drawable.onboarding_security_1782754961964
                ),
                OnboardingPage(
                    title = "Proof of Reputation",
                    description = "Build a verifiable trust score based on completed tasks and verified collaborations.",
                    icon = Icons.Default.Verified,
                    color = NeonEmerald,
                    imageRes = com.example.R.drawable.onboarding_reputation_1782754975727
                ),
                OnboardingPage(
                    title = "Ready to Build?",
                    description = "Establish your first workspace node and start collaborating with the elite creators.\n\nExecutive Attribution: Botla Veerendra (Founder) & Macha Praveen (Co-Founder).",
                    icon = Icons.Default.Star,
                    color = AccentRed,
                    imageRes = com.example.R.drawable.img_hero_banner
                )
            )
        }
    }

    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Counter Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorDivider)
                ) {
                    Text(
                        text = "STEP ${pagerState.currentPage + 1} OF ${onboardingPages.size}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Skip Button
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "SKIP",
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator
                Row(
                    Modifier
                        .height(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(onboardingPages.size) { iteration ->
                        val isSelected = pagerState.currentPage == iteration
                        val color = if (isSelected) AccentBlue else SurfaceLightColor
                        val width = if (isSelected) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                                .size(width = width, height = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("onboarding_back_button"),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ColorDivider),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BACK", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < onboardingPages.size - 1) {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                            } else {
                                onComplete()
                            }
                        },
                        modifier = Modifier
                            .weight(if (pagerState.currentPage > 0) 2f else 1f)
                            .height(48.dp)
                            .testTag("onboarding_continue_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (pagerState.currentPage < onboardingPages.size - 1) "CONTINUE" else "GET STARTED",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Continue", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Created by Founder Botla Veerendra & Co-Founder Macha Praveen", color = TextSecondary, fontSize = 9.sp)
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { pageIndex ->
            val page = onboardingPages[pageIndex]
            OnboardingPageView(page = page)
        }
    }
}

@Composable
fun OnboardingPageView(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(page.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = page.color,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.title.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val imageRes: Int
)
