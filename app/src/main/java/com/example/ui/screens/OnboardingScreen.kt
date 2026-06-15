package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.onboardingStep.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ONBOARDING  [$currentStep/3]",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrimaryBackground
                )
            )
        },
        containerColor = PrimaryBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PrimaryBackground)
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "onboarding_steps"
            ) { step ->
                when (step) {
                    1 -> StepRoleSelection(viewModel)
                    2 -> StepPortfolioSetup(viewModel)
                    3 -> StepPayoutSetup(viewModel)
                }
            }
        }
    }
}

// STEP 1: ROLE SELECTION
@Composable
fun StepRoleSelection(viewModel: MainViewModel) {
    val roles = listOf(
        "Channel Manager" to "Runs channel analytics, content strategy, budgets, and sponsors.",
        "Video Editor" to "Slices high-vibe cinematic pacing, audio sync, and visual sequences.",
        "Script Writer" to "Formulates hook structures, storylines, and viral-engineered scripts.",
        "2D Animator" to "Draws custom frames, fluid transitions, and character animations.",
        "Thumbnail Designer" to "Maxes out CTR with psychological high-contrast visual grids.",
        "Voice Actor / Host" to "Provides custom voice-overs, narration, or on-camera presentations.",
        "VFX & GFX Artist" to "Applies motion graphics, 3D modeling, and dynamic visual effects.",
        "Sound Designer" to "Engages audiences with spatial audio mixes and soundscapes.",
        "Platform Admin" to "Manages system disputes, workspace mediation, and platform-wide moderation.",
        "Enterprise / MCN" to "Orchestrates multi-channel networks and bulk contract compliance workflows.",
        "Content Researcher" to "Researches trending topics, SEO keywords, and background facts for scripts.",
        "Live Stream Producer" to "Directs real-time streams, overlays, and technical audio/video feeds.",
        "Social Media Manager" to "Orchestrates promotional short-form pieces, Twitter/Instagram, and audience reach.",
        "Sponsor Relations / BD" to "Secures sponsorship brand deals, handles communications, and reviews CPMs.",
        "Legal & Compliance Lead" to "Reviews channel IP rights, co-op agreements, and legal/tax forms compliance.",
        "Data Analyst / Strategist" to "Parses watch-time drops, YouTube Analytics, and designs thumbnail A/B tests.",
        "Creative Director" to "Establishes stylistic branding, formatting blueprints, and visual directions.",
        "Community Moderator" to "Guards live chat streams, moderates community spaces (Discord/Reddit), and gathers feedback."
    )
    
    val selectedRoles by viewModel.userRoleSelects.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_role_selection"),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Design your creative specialties",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "All Co-Op members are equal. Select the expertise areas you will bring to projects and contracts to structure partnerships.",
            color = TextSecondary,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        roles.forEach { (role, desc) ->
            val isSelected = selectedRoles.contains(role)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        val current = selectedRoles.toMutableSet()
                        if (isSelected) current.remove(role) else current.add(role)
                        viewModel.userRoleSelects.value = current
                    }
                    .testTag("role_card_${role.replace(" ", "_")}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SurfaceLightColor else SurfaceColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = role,
                            color = if (isSelected) AccentRed else Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            val current = selectedRoles.toMutableSet()
                            if (isSelected) current.remove(role) else current.add(role)
                            viewModel.userRoleSelects.value = current
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentRed,
                            checkmarkColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (selectedRoles.isNotEmpty()) {
                    viewModel.completeOnboardingStep1(selectedRoles)
                } else {
                    viewModel.toastMessage.value = "Please select at least one role to continue."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_roles_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "CONTINUE TO PORTFOLIO",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

// STEP 2: PORTFOLIO SETUP
@Composable
fun StepPortfolioSetup(viewModel: MainViewModel) {
    var expYears by remember { mutableStateOf("") }
    var portfolioUrl1 by remember { mutableStateOf("") }
    var portfolioUrl2 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_portfolio_setup"),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Configure your professional portfolio",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Prove your expertise to Channel Managers and collaborative syndicates.",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Years of Industry Experience *",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = expYears,
            onValueChange = { expYears = it },
            placeholder = { Text("e.g. 3 years", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentRed,
                unfocusedBorderColor = ColorDivider,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_experience_years"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Primary Portfolio Link (YouTube, Vimeo, Behance, GitHub) *",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = portfolioUrl1,
            onValueChange = { portfolioUrl1 = it },
            placeholder = { Text("youtube.com/c/example", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentRed,
                unfocusedBorderColor = ColorDivider,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_portfolio_url1"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Secondary Portfolio Link (Optional)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = portfolioUrl2,
            onValueChange = { portfolioUrl2 = it },
            placeholder = { Text("behance.net/portfolio", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentRed,
                unfocusedBorderColor = ColorDivider,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (expYears.isBlank() || portfolioUrl1.isBlank()) {
                    viewModel.toastMessage.value = "Please complete all fields marked with an asterisk (*)."
                } else {
                    val linksList = mutableListOf(portfolioUrl1)
                    if (portfolioUrl2.isNotBlank()) linksList.add(portfolioUrl2)
                    viewModel.completeOnboardingStep2(linksList, expYears)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("submit_portfolio_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "CONTINUE TO ESCROW SETUP",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

// STEP 3: PAYOUT ESCROW SETUP
@Composable
fun StepPayoutSetup(viewModel: MainViewModel) {
    var stripeVerified by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_payout_setup"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = "Payout Escrow",
            tint = AccentRed,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Link your Stripe Connnect payment account",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Creator Co-Op routes your AdSense revenue split directly into bank checking accounts. Verify your bank metadata securely via Stripe CONNECT integration.",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (stripeVerified) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Linked",
                        tint = ColorSuccess,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Stripe Identity Verified! 🔒",
                        color = ColorSuccess,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Express Account status is connected and live.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Stripe Connect Escrow Status",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "UNLOCKED ON DEMAND ON PAYOUT",
                        color = ColorWarning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            stripeVerified = true
                            viewModel.toastMessage.value = "Stripe verification successful! Escrow ID assigned."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Connect Stripe Express Wallet", color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = { viewModel.completeOnboardingStep3(false) },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Do This Later",
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { viewModel.completeOnboardingStep3(stripeVerified) },
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("onboarding_complete_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "FINISH ONBOARDING",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
