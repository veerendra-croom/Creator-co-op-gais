package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.theme.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.focus.onFocusChanged
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier,
    initialMode: String = "LOGIN",
    onNavigateToLanding: (() -> Unit)? = null,
    onLaunchDemo: (() -> Unit)? = null
) {
    var isLoginMode by remember(initialMode) { mutableStateOf(initialMode != "REGISTER") }
    var devTapCount by remember { mutableStateOf(0) }
    var developerModeEnabled by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    var showGoogleMockDialog by remember { mutableStateOf(false) }
    var customGoogleEmail by remember { mutableStateOf("") }
    var isCustomGoogleEmailExpanded by remember { mutableStateOf(false) }

    // Form states
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var inviteCode by rememberSaveable { mutableStateOf("") }
    var displayName by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    
    val isLoading by authViewModel.isLoading.collectAsState()
    val toastMessage by authViewModel.toastMessage.collectAsState()
    val platformSettings by authViewModel.platformSettings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.resetToast()
        }
    }

    // Form validation
    var emailError by remember { mutableStateOf<String?>(null) }
    val authError by authViewModel.authError.collectAsState()

    var emailFocused by remember { mutableStateOf(false) }
    var usernameFocused by remember { mutableStateOf(false) }
    var inviteCodeFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }
    var shakeTrigger by remember { mutableStateOf<Any?>(null) }

    Scaffold(
        containerColor = PrimaryBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (onNavigateToLanding != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBackground)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateToLanding,
                        modifier = Modifier.testTag("auth_back_to_landing_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back to Landing", tint = TextPrimary)
                    }

                    if (onLaunchDemo != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonEmerald.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, NeonEmerald),
                            modifier = Modifier
                                .clickable { onLaunchDemo() }
                                .testTag("auth_top_demo_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Demo", tint = NeonEmerald, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DEMO",
                                    color = NeonEmerald,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PrimaryBackground)
                .imePadding()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Widescreen hero banner (full width) with bottom rounding
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_hero_banner),
                    contentDescription = "Hero Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, PrimaryBackground.copy(alpha = 0.9f))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Premium brand logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceColor)
                    .padding(4.dp)
                    .clickable {
                        val isSupabasePlaceholder = com.example.data.supabase.SupabaseConfig.supabaseUrl.contains("your-project")
                        if (isSupabasePlaceholder) {
                            devTapCount++
                            if (devTapCount >= 7) {
                                developerModeEnabled = !developerModeEnabled
                                devTapCount = 0
                                authViewModel.showToast("Developer/Sandbox Mode: " + if (developerModeEnabled) "ENABLED" else "DISABLED")
                            }
                        } else {
                            developerModeEnabled = false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_app_logo),
                    contentDescription = "Creator Co-Op Logo",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "CREATOR CO-OP",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Connect, Align, and Collaborate with Top Creators",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, start = 24.dp, end = 24.dp)
            )

            // Dynamic platform settings notices (Maintenance, Registration limits, Invite Only)
            platformSettings?.let { settings ->
                Spacer(modifier = Modifier.height(12.dp))
                if (settings.maintenanceMode) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, AccentRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, "Maintenance Mode", tint = AccentRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SYSTEM MAINTENANCE ACTIVE: Only authorized operator logins are permitted at this time.",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                if (!settings.registrationEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, "Registration Disabled", tint = AccentRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REGISTRATION CLOSED: Platform administrators have temporarily disabled new sign-ups.",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )
                        }
                    }
                } else if (settings.inviteOnlyEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, AccentBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VpnKey, "Invite Only", tint = AccentBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INVITE-ONLY CO-OP: A valid platform referral or invite code is required to register.",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Demo Mode Quick Gateway
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (onLaunchDemo != null) onLaunchDemo()
                            else authViewModel.launchDemoMode(asAdmin = false)
                        }
                        .testTag("auth_explore_demo_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonEmerald.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Explore, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Preview App Without Signing In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Full 22-stop interactive User & Admin tour",
                                color = NeonEmerald,
                                fontSize = 10.sp
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                selectedTabIndex = if (isLoginMode) 0 else 1,
                containerColor = SurfaceColor,
                contentColor = AccentBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (isLoginMode) 0 else 1]),
                        color = AccentBlue
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    modifier = Modifier.heightIn(min = 48.dp),
                    selected = isLoginMode,
                    onClick = { 
                        isLoginMode = true 
                        emailError = null
                    },
                    text = { Text(stringResource(R.string.auth_sign_in), fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    modifier = Modifier.heightIn(min = 48.dp),
                    selected = !isLoginMode,
                    onClick = { 
                        isLoginMode = false 
                        emailError = null
                    },
                    text = { Text(stringResource(R.string.auth_join_coop), fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = TextSecondary
                )
            }

            AnimatedVisibility(visible = authError != null || emailError != null) {
                val err = authError ?: emailError
                err?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = AccentRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(message, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            if (message.contains("Email not confirmed", ignoreCase = true)) {
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(
                                    onClick = { authViewModel.resendConfirmationEmail(email) },
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                    colors = ButtonDefaults.textButtonColors(contentColor = AccentRed)
                                ) {
                                    Text("RESEND EMAIL", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Email Field
            Text(
                text = stringResource(R.string.auth_email_label) + " *",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                placeholder = { Text(stringResource(R.string.auth_email_placeholder), color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = ColorDivider,
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_field")
                    .onFocusChanged { emailFocused = it.isFocused }
                    .glowOnFocus(emailFocused, shape = RoundedCornerShape(10.dp))
                    .shake(shakeTrigger),
                shape = RoundedCornerShape(10.dp)
            )

            if (!isLoginMode) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Username *",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = { Text("Choose a unique username", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider,
                        focusedContainerColor = SurfaceColor,
                        unfocusedContainerColor = SurfaceColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_username_field")
                        .onFocusChanged { usernameFocused = it.isFocused }
                        .glowOnFocus(usernameFocused, shape = RoundedCornerShape(10.dp))
                        .shake(shakeTrigger),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                val isInviteRequired = platformSettings?.inviteOnlyEnabled == true
                Text(
                    text = if (isInviteRequired) "Invite / Referral Code *" else "Invite / Referral Code (Optional)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    placeholder = { Text(if (isInviteRequired) "Required code (e.g. ALEX123)" else "Optional code (e.g. ALEX123)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = ColorDivider,
                        focusedContainerColor = SurfaceColor,
                        unfocusedContainerColor = SurfaceColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_invite_code_field")
                        .onFocusChanged { inviteCodeFocused = it.isFocused }
                        .glowOnFocus(inviteCodeFocused, shape = RoundedCornerShape(10.dp))
                        .shake(shakeTrigger),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            Text(
                text = stringResource(R.string.auth_password_label) + " *",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                placeholder = { Text(stringResource(R.string.auth_password_placeholder), color = TextSecondary) },
                trailingIcon = {
                    val icon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (isPasswordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(imageVector = icon, contentDescription = description, tint = TextSecondary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = ColorDivider,
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_field")
                    .onFocusChanged { passwordFocused = it.isFocused }
                    .glowOnFocus(passwordFocused, shape = RoundedCornerShape(10.dp))
                    .shake(shakeTrigger),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            if (isLoginMode) {
                TextButton(
                    onClick = { 
                        if (email.isNotBlank()) {
                            authViewModel.resetPassword(email)
                        } else {
                            emailError = "Enter email to reset password"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.auth_forgot_password), color = AccentBlue, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action Button
            val submitInteractionSource = remember { MutableInteractionSource() }
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        if (password.isNotBlank()) {
                            if (isLoginMode) {
                                authViewModel.login(email, password)
                            } else {
                                if (username.isNotBlank()) {
                                    authViewModel.register(email, password, username, inviteCode)
                                } else {
                                    emailError = "Please choose a username"
                                    shakeTrigger = System.currentTimeMillis()
                                }
                            }
                        } else {
                            emailError = "Please enter your password"
                            shakeTrigger = System.currentTimeMillis()
                        }
                    } else {
                        emailError = "Please enter your email address"
                        shakeTrigger = System.currentTimeMillis()
                    }
                },
                enabled = !isLoading,
                interactionSource = submitInteractionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_submit_button")
                    .bounceScale(submitInteractionSource),
                colors = ButtonDefaults.buttonColors(containerColor = if (isLoading) AccentBlue.copy(alpha = 0.5f) else AccentBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isLoginMode) stringResource(R.string.auth_sign_in).uppercase() else stringResource(R.string.auth_join_coop).uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "or",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
            val googleInteractionSource = remember { MutableInteractionSource() }
            Button(
                onClick = { 
                    focusManager.clearFocus()
                    isCustomGoogleEmailExpanded = false
                    customGoogleEmail = ""
                    showGoogleMockDialog = true
                },
                interactionSource = googleInteractionSource,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_signin_button")
                    .bounceScale(googleInteractionSource),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.auth_google_sign_in).uppercase(),
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (developerModeEnabled) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        authViewModel.login("admin@creatorcoop.com", "any_password")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("auth_sandbox_bypass_button"),
                    border = BorderStroke(1.dp, AccentBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "SANDBOX ADMIN ACCESS (1-CLICK)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showGoogleMockDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showGoogleMockDialog = false 
                        isCustomGoogleEmailExpanded = false
                        customGoogleEmail = ""
                    },
                    containerColor = SurfaceColor,
                    titleContentColor = Color.White,
                    textContentColor = TextSecondary,
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_app_logo),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isCustomGoogleEmailExpanded) "Add Account" else stringResource(R.string.auth_google_sign_in),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.auth_google_continue),
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isCustomGoogleEmailExpanded) {
                                Text(
                                    text = "Enter any Google email address to simulate standard authorization:",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = customGoogleEmail,
                                    onValueChange = { customGoogleEmail = it },
                                    label = { Text("Google Email") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Done
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentBlue,
                                        unfocusedBorderColor = ColorDivider,
                                        focusedLabelColor = AccentBlue,
                                        unfocusedLabelColor = TextSecondary,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        val trimmed = customGoogleEmail.trim()
                                        if (trimmed.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
                                            showGoogleMockDialog = false
                                            authViewModel.login(trimmed.lowercase(), "bypass")
                                        } else {
                                            authViewModel.showToast("Please enter a valid Google email address")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Sign In with Google", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                TextButton(
                                    onClick = { isCustomGoogleEmailExpanded = false },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Back to Pre-configured Accounts", color = AccentBlue)
                                }
                            } else {
                                Text(
                                    "Simulate Google identity provider sign-in. Select a pre-configured beta account:",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                // Option 1: Sarah Jenkins (Video Editor)
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        showGoogleMockDialog = false
                                        authViewModel.login("alex.mercer@gmail.com", "bypass")
                                    },
                                    colors = CardDefaults.cardColors(containerColor = PrimaryBackground)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(AccentBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("AM", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Alex Mercer", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("alex.mercer@gmail.com", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }

                                // Option 2: Admin
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        showGoogleMockDialog = false
                                        authViewModel.login("admin@creatorcoop.com", "bypass")
                                    },
                                    colors = CardDefaults.cardColors(containerColor = PrimaryBackground)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(AccentRed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("AD", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Platform Admin", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("admin@creatorcoop.com", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }

                                // Option 3: User (Personalized)
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        showGoogleMockDialog = false
                                        authViewModel.login("appcroom@gmail.com", "bypass")
                                    },
                                    colors = CardDefaults.cardColors(containerColor = PrimaryBackground)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(CrispAmber),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("AC", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Croom (User)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("appcroom@gmail.com", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }

                                // Option 4: Use another Google account
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        isCustomGoogleEmailExpanded = true
                                    },
                                    colors = CardDefaults.cardColors(containerColor = PrimaryBackground)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.DarkGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Use another account", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("Type a custom Google email address", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { 
                                showGoogleMockDialog = false 
                                isCustomGoogleEmailExpanded = false
                                customGoogleEmail = ""
                            }
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
}
