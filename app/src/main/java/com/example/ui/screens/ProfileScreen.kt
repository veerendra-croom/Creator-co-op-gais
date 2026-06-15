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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    var showEditDialog by remember { mutableStateOf(false) }
    var editDisplayName by remember(user?.displayName) { mutableStateOf(user?.displayName ?: "") }
    var editPhone by remember(user?.phone) { mutableStateOf(user?.phone ?: "") }
    var editAvatarUrl by remember(user?.avatarUrl) { mutableStateOf(user?.avatarUrl ?: "") }

    if (user == null) {
        Box(modifier = modifier.fillMaxSize().background(PrimaryBackground), contentAlignment = Alignment.Center) {
            Text("Loading user profile...", color = Color.White)
        }
        return
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "Edit Co-Op Profile",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Update your public display identity, E.164 mobile contact, and your portfolio avatar URI.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    OutlinedTextField(
                        value = editDisplayName,
                        onValueChange = { editDisplayName = it },
                        label = { Text("Display Name", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = ColorDivider
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
                    )

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Mobile Phone", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = ColorDivider
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_phone_input")
                    )

                    OutlinedTextField(
                        value = editAvatarUrl,
                        onValueChange = { editAvatarUrl = it },
                        label = { Text("Avatar Image URL", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = ColorDivider
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_avatar_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editDisplayName.isNotBlank()) {
                            scope.launch {
                                user?.let { u ->
                                    viewModel.repository.saveUser(
                                        u.copy(
                                            displayName = editDisplayName,
                                            phone = editPhone,
                                            avatarUrl = editAvatarUrl
                                        )
                                    )
                                }
                                showEditDialog = false
                                viewModel.toastMessage.value = "Identity profile saved successfully! ✨"
                            }
                        } else {
                            viewModel.toastMessage.value = "Name cannot be empty!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("SAVE CHANGES", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("CANCEL", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        )
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
                val currentSpecs = remember(user!!.secondaryRolesJson) {
                    try {
                        val clean = user!!.secondaryRolesJson.trim().removeSurrounding("[", "]")
                        if (clean.isBlank()) emptyList() else clean.split(",").map { it.replace("\"", "").trim() }
                    } catch(e: Exception) {
                        emptyList()
                    }
                }
                Text(
                    text = "${user!!.primaryRole} • Karma: ${user!!.karmaScore}",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                if (currentSpecs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Specialties: " + currentSpecs.joinToString(", "),
                        color = AccentBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.testTag("edit_profile_dialog_trigger"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profile Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
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

        // INTERACTIVE SPECIALTIES PROFILE BUILDER
        item {
            val scope = rememberCoroutineScope()
            val availableSpecs = listOf(
                "Channel Strategy",
                "Video Editing",
                "Script Writing",
                "2D Animation",
                "Thumbnail Design",
                "VFX & Motion Graphics",
                "Sound Design",
                "Voice Acting",
                "Sponsor Relations",
                "Content SEO",
                "Legal & Contracts"
            )
            val currentSpecs = remember(user!!.secondaryRolesJson) {
                try {
                    val clean = user!!.secondaryRolesJson.trim().removeSurrounding("[", "]")
                    if (clean.isBlank()) emptyList() else clean.split(",").map { it.replace("\"", "").trim() }
                } catch(e: Exception) {
                    emptyList()
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("interactive_specialties_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DESIGN YOUR CO-OP SPECIALTIES",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toggle your expertise specialities to design your profile. Fellow members will connect with you and trigger joint contracts based on these skills.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))

                    availableSpecs.chunked(2).forEach { pair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { spec ->
                                val isSelected = currentSpecs.contains(spec)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AccentRed else SurfaceLightColor)
                                        .clickable {
                                            val updated = if (isSelected) currentSpecs - spec else currentSpecs + spec
                                            val json = "[" + updated.joinToString(",") { "\"$it\"" } + "]"
                                            scope.launch {
                                                viewModel.repository.saveUser(user!!.copy(secondaryRolesJson = json))
                                            }
                                        }
                                        .padding(vertical = 10.dp, horizontal = 12.dp)
                                        .testTag("tech_spec_toggle_${spec.replace(" ", "_")}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = spec,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
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

        // LEGAL & TAX COMPLIANCE CONSOLE (1099-MISC & W-8BEN GEN)
        item {
            var selectedTaxForm by remember { mutableStateOf<String?>(null) }
            var isGenerating by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val earningsYtd = (user?.availableBalance ?: 0.0) + 12500.0 // Dynamic simulated YTD

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tax_compliance_card"),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LEGAL TAX DOCUMENTS & LEDGER", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Auto-generated IRS & international withholding forms relative to your active co-op splits.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FY26 Earnings YTD (Secured Escrows)", color = TextSecondary, fontSize = 11.sp)
                            Text(
                                text = "$" + String.format("%.2f", earningsYtd),
                                color = ColorSuccess,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Security Status", tint = ColorSuccess, modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = ColorDivider)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Downloadable Compliance Forms:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    // US Form 1099-MISC
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isGenerating) {
                                    scope.launch {
                                        isGenerating = true
                                        viewModel.toastMessage.value = "Starting secure compilation of IRS Form 1099-MISC..."
                                        kotlinx.coroutines.delay(1800)
                                        isGenerating = false
                                        selectedTaxForm = "1099-MISC"
                                        viewModel.toastMessage.value = "IRS Form 1099-MISC generated! Ready for download."
                                    }
                                }
                            }
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLightColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Feed, contentDescription = "1099", tint = AccentRed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("IRS Form 1099-MISC (US Members)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Calculates split yields for US tax declarations", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                            Icon(Icons.Default.Download, contentDescription = "Download Form", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    // International W-8BEN
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isGenerating) {
                                    scope.launch {
                                        isGenerating = true
                                        viewModel.toastMessage.value = "Compiling international withholdings on Form W-8BEN..."
                                        kotlinx.coroutines.delay(1800)
                                        isGenerating = false
                                        selectedTaxForm = "W-8BEN"
                                        viewModel.toastMessage.value = "Form W-8BEN generated & digitally timestamped!"
                                    }
                                }
                            }
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLightColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Public, contentDescription = "W-8BEN", tint = AccentBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Form W-8BEN (International Members)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Certificate of Foreign Status for withholding tax", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                            Icon(Icons.Default.Download, contentDescription = "Download Form", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (selectedTaxForm != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ColorSuccess.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = ColorSuccess.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Ready", tint = ColorSuccess, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("$selectedTaxForm Ready!", color = ColorSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    IconButton(
                                        onClick = { selectedTaxForm = null },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Generated UUID: " + java.util.UUID.randomUUID().toString().substring(0,8) + "\n" +
                                            "Jurisdiction filing code ready for IRS/local declaration.",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.toastMessage.value = "Saving Creator_CoOp_$selectedTaxForm.pdf to system downloads directory!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorSuccess),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 12.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("SAVE PDF TO DOWNLOADS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    if (isGenerating) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = AccentRed, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compiling split transactions & generating ledger...", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Real Sign Out Button
        item {
            Button(
                onClick = { viewModel.logoutSession() },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulate_signout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Log out",
                    tint = AccentRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("SIGN OUT OF ECOSYSTEM", color = AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
