package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.AnalyticsViewModel
import com.example.ui.viewmodels.FunnelStep

@Composable
fun AnalyticsDashboardScreen(
    analyticsViewModel: AnalyticsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userActivity by analyticsViewModel.userActivity.collectAsState()
    val funnelData by analyticsViewModel.funnelData.collectAsState()
    val pmfMetrics by analyticsViewModel.pmfMetrics.collectAsState()
    val referralStats by analyticsViewModel.referralStats.collectAsState()
    val churnAlerts by analyticsViewModel.churnAlerts.collectAsState()
    val toastMessage by analyticsViewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val shareTelemetry = { format: String ->
        val content = analyticsViewModel.getExportContent(format)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Telemetry $format", content)
        clipboard.setPrimaryClip(clip)

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, "Creator Co-Op Telemetry ($format)")
            putExtra(Intent.EXTRA_TEXT, content)
            type = if (format == "JSON") "application/json" else "text/csv"
        }
        val chooser = Intent.createChooser(sendIntent, "Export Analytics Telemetry ($format)")
        context.startActivity(chooser)
        analyticsViewModel.exportData(format)
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            analyticsViewModel.resetToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PrimaryBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text("Analytics Telemetry", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = "Cohort Performance & Conversion Telemetry",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { shareTelemetry("CSV") }) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = AccentBlue)
                    }
                    IconButton(onClick = { shareTelemetry("JSON") }) {
                        Icon(Icons.Default.Share, contentDescription = "Export JSON", tint = NeonEmerald)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.weight(1f)
            ) {
                // ... (rest of the sections remain the same)
                // 1. PMF & STICKINESS
                item {
                    DashboardSectionHeader("PMF & STICKINESS")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pmfMetrics.forEach { (label, value) ->
                            AnalyticsCard(label, value.toString(), "", AccentBlue, modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 2. ACTIVATION FUNNEL
                item {
                    DashboardSectionHeader("ACTIVATION FUNNEL")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            funnelData.forEachIndexed { index, step ->
                                FunnelRow(step.name, step.count, if (index > 0) funnelData[index-1].count else 0)
                            }
                        }
                    }
                }

                // 3. BETA USER HEALTH
                item {
                    DashboardSectionHeader("BETA USER HEALTH (Active Ranking)")
                    if (userActivity.isEmpty()) {
                        Text("No activity recorded.", color = TextSecondary, fontSize = 12.sp)
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                userActivity.take(5).forEach { (userId, count) ->
                                    UserActivityRow(userId, count)
                                    HorizontalDivider(color = ColorDivider, modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }

                // 4. CHURN & ALERTS
                if (churnAlerts.isNotEmpty()) {
                    item {
                        DashboardSectionHeader("CHURN ALERTS", color = AccentRed)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            churnAlerts.forEach { alert ->
                                AlertCard(alert)
                            }
                        }
                    }
                }

                // 5. REFERRAL MONITORING
                item {
                    DashboardSectionHeader("REFERRAL MONITORING")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AnalyticsCard("Portfolio Shares", referralStats["Portfolio Shares"]?.toString() ?: "0", "", NeonEmerald, modifier = Modifier.weight(1f))
                        AnalyticsCard("Viral Coeff", "%.2f".format(referralStats["Viral Coeff"] ?: 0f), "", NeonEmerald, modifier = Modifier.weight(1f))
                    }
                }

                // EXPORT ACTIONS
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { analyticsViewModel.exportData("CSV") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Text("Table Export")
                        }
                        OutlinedButton(
                            onClick = { analyticsViewModel.exportData("JSON") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, ColorDivider)
                        ) {
                            Text("Raw Export")
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun DashboardSectionHeader(title: String, color: Color = Color.White) {
    Text(
        text = title,
        color = if (color == Color.White) TextSecondary else color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun FunnelRow(name: String, count: Int, prevCount: Int) {
    val conversion = if (prevCount > 0) (count.toFloat() / prevCount * 100).toInt() else 100
    val progress = if (prevCount > 0) (count.toFloat() / prevCount) else 1f
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("$count users", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (conversion >= 80) NeonEmerald.copy(alpha = 0.15f) else AccentBlue.copy(alpha = 0.15f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$conversion%",
                    color = if (conversion >= 80) NeonEmerald else AccentBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(ColorDivider.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0.01f, 1f))
                    .fillMaxHeight()
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(AccentBlue, if (conversion >= 80) NeonEmerald else AccentBlue)
                        )
                    )
            )
        }
    }
}

@Composable
fun UserActivityRow(userId: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(AccentBlue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Text(userId.take(1).uppercase(), color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(userId, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text("$count events", color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
fun AlertCard(message: String) {
    Surface(
        color = AccentRed.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, null, tint = AccentRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
fun AnalyticsCard(title: String, value: String, change: String, tint: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}
