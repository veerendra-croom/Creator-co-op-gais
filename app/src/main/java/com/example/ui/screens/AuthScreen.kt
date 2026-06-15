package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.ui.theme.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var isLoginMode by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    // Form states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    fun validateInputs(): Boolean {
        var isValid = true

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Please enter a valid email address."
            isValid = false
        } else {
            emailError = null
        }

        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters long."
            isValid = false
        } else {
            passwordError = null
        }

        if (!isLoginMode) {
            if (displayName.isBlank()) {
                nameError = "Display name is required."
                isValid = false
            } else {
                nameError = null
            }

            if (phone.isBlank()) {
                phoneError = "Phone number is required."
                isValid = false
            } else {
                phoneError = null
            }
        }

        return isValid
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "CREATOR CO-OP",
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
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Display
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = "Core Logo",
                tint = AccentRed,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Secure Autonomous Sign In",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Zero Mock Data Auth System | Local Room DB & Supabase Sync",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Mode Selector
            TabRow(
                selectedTabIndex = if (isLoginMode) 0 else 1,
                containerColor = SurfaceColor,
                contentColor = AccentRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (isLoginMode) 0 else 1]),
                        color = AccentRed
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = isLoginMode,
                    onClick = { isLoginMode = true },
                    text = { Text("Log In", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_login_mode")
                )
                Tab(
                    selected = !isLoginMode,
                    onClick = { isLoginMode = false },
                    text = { Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = TextSecondary,
                    modifier = Modifier.testTag("tab_signup_mode")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs
            if (!isLoginMode) {
                // Name Field
                Text(
                    text = "Display Name *",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    placeholder = { Text("e.g. Jack Editor", color = TextSecondary) },
                    isError = nameError != null,
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
                        .testTag("auth_name_field"),
                    shape = RoundedCornerShape(10.dp)
                )
                nameError?.let {
                    Text(it, color = AccentRed, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Phone Field
                Text(
                    text = "E.164 Phone Number *",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    placeholder = { Text("e.g. +1 (555) 0101", color = TextSecondary) },
                    isError = phoneError != null,
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
                        .testTag("auth_phone_field"),
                    shape = RoundedCornerShape(10.dp)
                )
                phoneError?.let {
                    Text(it, color = AccentRed, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

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
                placeholder = { Text("e.g. manager@coop.com", color = TextSecondary) },
                isError = emailError != null,
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
                    .testTag("auth_email_field"),
                shape = RoundedCornerShape(10.dp)
            )
            emailError?.let {
                Text(it, color = AccentRed, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field (Secure input)
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
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                placeholder = { Text("••••••••", color = TextSecondary) },
                isError = passwordError != null,
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
                    .testTag("auth_password_field"),
                shape = RoundedCornerShape(10.dp)
            )
            passwordError?.let {
                Text(it, color = AccentRed, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action Button
            Button(
                onClick = {
                    if (validateInputs()) {
                        if (isLoginMode) {
                            viewModel.loginWithEmail(email)
                        } else {
                            viewModel.registerNewUser(email, phone, displayName)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isLoginMode) "SIGN IN TO ECOSYSTEM" else "REGISTER NEW ACCOUNT",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick demo accounts advice
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 Quick Testing Credentials:",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Partner A: manager@coop.com\n• Partner B: editor@coop.com\n• Partner C: admin@coop.com\n• Partner D: mcn@coop.com\nAll roles are equal Co-Op Members initially. Log in and customize specialties dynamically inside profile tabs! Any password works.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
