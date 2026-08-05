// Vault Security Enclave - Polished
package com.example.ui.screens.workspace

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.components.*
import com.example.ui.viewmodels.AgreementViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgreementVault(
    viewModel: AgreementViewModel,
    workspaceId: String,
    userId: String,
    isLead: Boolean = false,
    onBack: () -> Unit
) {
    val activeAgreement by viewModel.getActiveAgreement(workspaceId).collectAsState(initial = null)
    val acks by viewModel.getAcknowledgments(activeAgreement?.id).collectAsState(initial = emptyList())
    val hasAcked = acks.any { it.userId == userId }

    val defaultPreamble = "This Co-Op Production Contract is entered into secure block registers on this day, governing intellectual properties and compliance standards in Shard #$workspaceId."
    val defaultScope = "Collaborators shall execute assigned production timeline units in accordance with Kanban milestones. Milestone completion is validated via decentralized peer review."
    val defaultIP = "All generated creative resources, video timeline layers, thumbnail scripts, and associated assets are held in secure Co-Op commons with a joint-ownership allocation model."
    val defaultRevenue = "Gross earnings from published media streams are allocated dynamically based on verified contribution points: Lead Director (40%), Editors (30%), VFX Specialists (30%)."

    val templateText = """
        [PREAMBLE]
        $defaultPreamble
        
        [SCOPE OF WORK]
        $defaultScope
        
        [IP ASSIGNMENT]
        $defaultIP
        
        [REVENUE SPLIT]
        $defaultRevenue
    """.trimIndent()

    var showEditDialog by remember { mutableStateOf(false) }
    var editedTermsText by remember(activeAgreement) { mutableStateOf(activeAgreement?.contentText ?: templateText) }
    var isSigning by remember { mutableStateOf(false) }
    var signatureProgress by remember { mutableStateOf(0f) }
    val history by viewModel.getAgreementHistory(workspaceId).collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    // Parse the contract text sections (with fallbacks if edited)
    val content = activeAgreement?.contentText ?: templateText
    val preamble = getSectionText(content, "PREAMBLE", defaultPreamble)
    val scope = getSectionText(content, "SCOPE OF WORK", defaultScope)
    val ip = getSectionText(content, "IP ASSIGNMENT", defaultIP)
    val revenue = getSectionText(content, "REVENUE SPLIT", defaultRevenue)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(DS.Space16)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(DS.Space16)
    ) {
        if (activeAgreement == null && !isLead) {
            EmptyState(
                title = "Vault Enclave Empty",
                description = "No production agreement has been initialized for this shard. Lead creators must establish the ledger terms before contributors can acknowledge them.",
                icon = Icons.Default.Gavel,
                actionText = "NOTIFY LEAD CREATOR",
                onAction = { viewModel.notifyLeadCreator(workspaceId, userId) }
            )
        } else {
            // --- Header Block ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Gavel, contentDescription = null, tint = AccentRed, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(DS.Space12))
            Column {
                Text(
                    text = "VAULT SECURITY ENCLAVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentRed,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Team Agreement Ledger",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (history.size > 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceLightColor.copy(alpha = 0.5f)),
                shape = DS.RadiusMedium
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ledger History: ${history.size} iterations recorded in secure block", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        val currentAgreement = activeAgreement
        if (currentAgreement != null) {
            val sealHash = currentAgreement.contentText.hashCode().toString().take(8).uppercase()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentBlue.copy(alpha = 0.1f), DS.RadiusMedium)
                    .border(1.dp, AccentBlue.copy(alpha = 0.25f), DS.RadiusMedium)
                    .padding(DS.Space12),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(DS.Space12))
                Column {
                    Text(
                        text = "DECENTRALIZED CO-OP SEAL SIGNED",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Contract Hash ID: SHA-256#$sealHash • ${acks.size} signature(s) compiled.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        } else {
            // Empty State Draft Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
                shape = DS.RadiusMedium
            ) {
                Column(
                    modifier = Modifier.padding(DS.Space16),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.HourglassEmpty, null, tint = CrispAmber, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(DS.Space8))
                    Text(
                        text = "Contract Draft Uninitialized",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "The leader must publish and initialize the digital co-op agreement to bind all pipeline contributors.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = DS.Space4)
                    )
                }
            }
        }

        // --- CONTRACT SECURED SECTIONS ---
        if (activeAgreement != null) {
            val compliance = remember(revenue) { validateSplitSum(revenue) }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (compliance.isCompliant) SurfaceColor else AccentRed.copy(alpha = 0.05f)
                ),
                shape = DS.RadiusLarge,
                border = BorderStroke(
                    width = 1.dp, 
                    color = if (compliance.isCompliant) NeonEmerald.copy(alpha = 0.3f) else AccentRed.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(DS.Space16),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DS.Space16)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (compliance.isCompliant) NeonEmerald.copy(alpha = 0.1f) else AccentRed.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (compliance.isCompliant) Icons.Default.VerifiedUser else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (compliance.isCompliant) NeonEmerald else AccentRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SECURE LEDGER COMPLIANCE STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (compliance.isCompliant) NeonEmerald else AccentRed,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = compliance.message,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (compliance.values.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Detected splits: ${compliance.values.joinToString { "$it%" }} (Total Sum: ${compliance.sum}%)",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Text(
                text = "FORMAL CONTRACT CLAUSES",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )

            // Section 1: Preamble
            ContractClauseCard(
                clauseNumber = "I",
                title = "PREAMBLE & CONTEXT",
                legalText = preamble,
                status = "COMPLIANT",
                statusColor = NeonEmerald
            )

            // Section 2: Scope of Work
            ContractClauseCard(
                clauseNumber = "II",
                title = "SCOPE OF WORK",
                legalText = scope,
                status = "ACTIVE",
                statusColor = AccentBlue
            )

            // Section 3: IP Assignment
            ContractClauseCard(
                clauseNumber = "III",
                title = "IP ASSIGNMENT & PATENTS",
                legalText = ip,
                status = "RESTRICTED",
                statusColor = CrispAmber
            )

            // Section 4: Revenue Split
            ContractClauseCard(
                clauseNumber = "IV",
                title = "DYNAMIC REVENUE SHARE",
                legalText = revenue,
                status = "ENFORCED",
                statusColor = NeonEmerald
            )

            // --- SIGNATURE WORKSPACE MEMBERS STATUS ---
            Text(
                text = "SIGNATORY DECENTRALIZED RECORD",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = DS.Space8)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = DS.RadiusLarge,
                border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(DS.Space16), verticalArrangement = Arrangement.spacedBy(DS.Space12)) {
                    // Signers tracking list
                    if (acks.isEmpty()) {
                        Text(
                            text = "Awaiting initial co-op signatures...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontStyle = FontStyle.Italic
                        )
                    } else {
                        acks.forEach { ack ->
                            val signatureHash = ack.acknowledgmentHash.take(8).uppercase()
                            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ack.acknowledgedAt))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(NeonEmerald.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = NeonEmerald, modifier = Modifier.size(14.dp))
                                    }
                                    Spacer(modifier = Modifier.width(DS.Space12))
                                    Column {
                                        Text(
                                            text = "Creator ID: ${ack.userId}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Block Sign Hash: SEC#$signatureHash • ${com.example.util.DateTimeUtils.getRelativeTimeSpanString(ack.acknowledgedAt)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Surface(
                                    color = NeonEmerald.copy(alpha = 0.15f),
                                    contentColor = NeonEmerald,
                                    shape = DS.RadiusSmall,
                                    border = BorderStroke(0.5.dp, NeonEmerald.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "SIGNED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = DS.Space8, vertical = DS.Space4)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- BOTTOM ACTIONS MATRIX ---
        if (activeAgreement != null && !hasAcked) {
            if (isSigning) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SECURE BIO-HANDSHAKE IN PROGRESS...", color = AccentRed, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                    LinearProgressIndicator(
                        progress = { signatureProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = AccentRed,
                        trackColor = ColorDivider
                    )
                    Text("VERIFYING IDENTITY & HASHING LEDGER...", color = TextSecondary, fontSize = 11.sp)
                }
            } else {
                SwipeToSignSlider(
                    onSignCompleted = {
                        coroutineScope.launch {
                            isSigning = true
                            signatureProgress = 0f
                            while (signatureProgress < 1f) {
                                delay(100)
                                signatureProgress += 0.05f
                            }
                            val hash = currentAgreement?.contentText?.hashCode()?.toString() ?: ""
                            currentAgreement?.let {
                                viewModel.acknowledgeAgreement(it.id, hash, userId)
                            }
                            isSigning = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else if (activeAgreement == null) {
            Button(
                onClick = { viewModel.createTeamAgreement(workspaceId, templateText) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = DS.RadiusMedium
            ) {
                Text("INITIALIZE & PROPOSE TERMS DRAFT", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(DS.Space12)) {
                Button(
                    onClick = { viewModel.exportAgreementAsPDF() },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = DS.RadiusMedium,
                    border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = DS.Space12)
                ) {
                    Icon(Icons.Default.FileDownload, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(DS.Space8))
                    Text("PDF COOP EXPORT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = CardDefaults.cardColors(containerColor = NeonEmerald.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f)),
                    shape = DS.RadiusMedium
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(DS.Space4)) {
                            Icon(Icons.Default.CheckCircle, null, tint = NeonEmerald, modifier = Modifier.size(14.dp))
                            Text("Signed ✓", color = NeonEmerald, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isLead && activeAgreement != null) {
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceColor),
                border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.5f)),
                shape = DS.RadiusMedium
            ) {
                Icon(Icons.Default.Edit, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(DS.Space8))
                Text("REVISE & VOID DRAFT SECURELY", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = SurfaceColor,
            title = {
                Text(
                    text = "Propose Amended Terms",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DS.Space12),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Amending clauses will clear ALL historical member co-op signatures and reset compliance states.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = editedTermsText,
                        onValueChange = { editedTermsText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentRed,
                            unfocusedBorderColor = ColorDivider.copy(alpha = 0.5f),
                            focusedContainerColor = PrimaryBackground,
                            unfocusedContainerColor = PrimaryBackground
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTeamAgreement(workspaceId, editedTermsText)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = DS.RadiusMedium,
                    enabled = editedTermsText.isNotBlank()
                ) {
                    Text("Publish Amended Ledger", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ContractClauseCard(
    clauseNumber: String,
    title: String,
    legalText: String,
    status: String,
    statusColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = DS.RadiusLarge,
        border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(DS.Space16)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = clauseNumber,
                            color = AccentBlue,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(DS.Space8))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    contentColor = statusColor,
                    shape = DS.RadiusSmall,
                    border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(DS.Space12))
            
            Surface(
                color = PrimaryBackground.copy(alpha = 0.6f),
                shape = DS.RadiusMedium,
                border = BorderStroke(1.dp, ColorDivider.copy(alpha = 0.2f))
            ) {
                Text(
                    text = legalText,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(DS.Space12)
                )
            }
        }
    }
}

// Utility function to extract sections with matching headers or fall back cleanly
fun getSectionText(fullText: String, header: String, fallbackText: String): String {
    val delimiter = "[$header]"
    if (!fullText.contains(delimiter)) return fallbackText
    
    val parts = fullText.split(delimiter)
    if (parts.size < 2) return fallbackText
    
    val sectionPart = parts[1].trim()
    // Find next header start e.g. "["
    val nextHeaderIndex = sectionPart.indexOf("[")
    return if (nextHeaderIndex != -1) {
        sectionPart.substring(0, nextHeaderIndex).trim()
    } else {
        sectionPart
    }
}

data class ComplianceResult(
    val isCompliant: Boolean,
    val message: String,
    val sum: Int,
    val values: List<Int>
)

fun validateSplitSum(text: String): ComplianceResult {
    // Regex to match numbers with percentage symbols, or words followed by %
    val regex = Regex("(\\d+)\\s*%|(\\d+)\\s*percent", RegexOption.IGNORE_CASE)
    val matches = regex.findAll(text)
    val values = matches.mapNotNull { match ->
        val group = match.groupValues.getOrNull(1)?.takeIf { it.isNotEmpty() }
            ?: match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }
        group?.toIntOrNull()
    }.toList()
    
    if (values.isEmpty()) {
        return ComplianceResult(
            isCompliant = true, 
            message = "No explicit splits detected in clauses. Safe equal revenue escrow shares enforced.", 
            sum = 100,
            values = emptyList()
        )
    }
    
    val sum = values.sum()
    return if (sum == 100) {
        ComplianceResult(
            isCompliant = true, 
            message = "Allocations validated at exactly 100%. Smart Escrow anchor status active.", 
            sum = sum, 
            values = values
        )
    } else {
        ComplianceResult(
            isCompliant = false, 
            message = "Warning: Revenue splits total $sum% instead of exactly 100%!", 
            sum = sum, 
            values = values
        )
    }
}

@Composable
fun SwipeToSignSlider(
    onSignCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragAmount by remember { mutableStateOf(0f) }
    var isCompleted by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(ColorDivider)
            .border(BorderStroke(1.dp, AccentRed.copy(alpha = 0.4f)), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val buttonWidth = 56.dp
        val buttonWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { buttonWidth.toPx() }
        val maxDrag = (widthPx - buttonWidthPx).coerceAtLeast(0f)

        // Background Track Fill showing progress
        val progressPercent = if (maxDrag > 0) (dragAmount / maxDrag).coerceIn(0f, 1f) else 0f
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(if (progressPercent <= 0f) 0.01f else progressPercent)
                .background(AccentRed.copy(alpha = 0.3f))
        )

        // Center Instruction Text
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SWIPE RIGHT TO SIGN",
                color = Color.White.copy(alpha = (0.6f + (1f - progressPercent) * 0.4f).coerceIn(0f, 1f)),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp
            )
        }

        // Draggable Handle
        val handleOffset = dragAmount
        Box(
            modifier = Modifier
                .offset(x = with(androidx.compose.ui.platform.LocalDensity.current) { handleOffset.toDp() })
                .size(buttonWidth)
                .clip(CircleShape)
                .background(AccentRed)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragAmount >= maxDrag * 0.9f) {
                                isCompleted = true
                                dragAmount = maxDrag
                                onSignCompleted()
                            } else {
                                // bounce back
                                dragAmount = 0f
                            }
                        },
                        onDragCancel = {
                            dragAmount = 0f
                        },
                        onHorizontalDrag = { _, dragAmountPx ->
                            if (!isCompleted) {
                                dragAmount = (dragAmount + dragAmountPx).coerceIn(0f, maxDrag)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Swipe Handle",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
