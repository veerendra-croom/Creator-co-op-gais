package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
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
        if (dbSlides.isNotEmpty()) {
            dbSlides.sortedBy { it.stepIndex }.map { slide ->
                val icon = when (slide.iconName) {
                    "Hub" -> Icons.Default.Share
                    "Handshake" -> Icons.Default.Verified
                    "Groups" -> Icons.Default.Groups
                    "Gavel" -> Icons.Default.Gavel
                    "Verified" -> Icons.Default.Verified
                    else -> Icons.Default.Star
                }
                val color = when (slide.stepIndex) {
                    0 -> AccentBlue
                    1 -> CrispAmber
                    2 -> NeonEmerald
                    else -> AccentRed
                }
                val imageRes = when (slide.stepIndex) {
                    0 -> com.example.R.drawable.onboarding_collaboration_1782754946609
                    1 -> com.example.R.drawable.onboarding_security_1782754961964
                    2 -> com.example.R.drawable.onboarding_reputation_1782754975727
                    else -> com.example.R.drawable.img_hero_banner
                }
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
                    description = "The first premium B2B SaaS platform designed specifically for the Creator Economy.",
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
                    description = "Establish your first workspace node and start collaborating with the elite 1% of creators.\n\nCreated by Founder Botla Veerendra & Co-Founder Macha Praveen.",
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
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
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
                        val color = if (pagerState.currentPage == iteration) AccentBlue else SurfaceLightColor
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (pagerState.currentPage < onboardingPages.size - 1) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage < onboardingPages.size - 1) "CONTINUE" else "GET STARTED",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Created by Founder Botla Veerendra & Co-Founder Macha Praveen", color = TextSecondary, fontSize = 10.sp)
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
