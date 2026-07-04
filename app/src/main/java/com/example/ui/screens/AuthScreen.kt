package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.theme.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var isLoginMode by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    // Form states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    val isLoading by authViewModel.isLoading.collectAsState()
    val toastMessage by authViewModel.toastMessage.collectAsState()
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

    Scaffold(
        containerColor = PrimaryBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PrimaryBackground)
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
                    .padding(4.dp),
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

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    text = { Text("Log In", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
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
                    text = { Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
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
                text = "Email Address *",
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
                placeholder = { Text("Enter your email address", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = ColorDivider,
                    focusedContainerColor = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor
                ),
                modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
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
                    modifier = Modifier.fillMaxWidth().testTag("auth_username_field"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            Text(
                text = "Password *",
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
                placeholder = { Text("Enter your password", color = TextSecondary) },
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
                modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Action Button
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        if (password.isNotBlank()) {
                            if (isLoginMode) {
                                authViewModel.login(email, password)
                            } else {
                                if (username.isNotBlank()) {
                                    authViewModel.register(email, password, username)
                                } else {
                                    emailError = "Please choose a username"
                                }
                            }
                        } else {
                            emailError = "Please enter your password"
                        }
                    } else {
                        emailError = "Please enter your email address"
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = if (isLoading) AccentBlue.copy(alpha = 0.5f) else AccentBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isLoginMode) "SIGN IN" else "SIGN UP",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
}
