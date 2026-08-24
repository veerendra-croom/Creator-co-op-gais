package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.GlobalViewModel

@Composable
fun ReportModerationScreen(
    globalViewModel: GlobalViewModel,
    currentUserId: String,
    targetType: String = "CONTENT",
    targetId: String = "general",
    onBack: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("report_moderation_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Report content", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        Text(
            text = "Please provide details about the issue. Our moderation team will review this in the Admin Queue.",
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = ColorDivider.copy(alpha = 0.5f)
        )

        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason for reporting") },
            placeholder = { Text("e.g. Spam, Harassment, Terms Violation") },
            modifier = Modifier.fillMaxWidth().testTag("report_reason_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = ColorDivider,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            label = { Text("Additional Details") },
            placeholder = { Text("Please provide context for the admin team...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .testTag("report_details_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = ColorDivider,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 5,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (reason.isNotBlank()) {
                    globalViewModel.submitReport(
                        reporterId = currentUserId,
                        targetType = targetType,
                        targetId = targetId,
                        reason = reason,
                        details = details
                    )
                    onBack()
                }
            },
            enabled = reason.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("submit_report_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Report, contentDescription = "Report", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submit Report to Moderation Queue")
        }
    }
}
