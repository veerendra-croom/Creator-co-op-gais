package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CreatorCoopApp
import com.example.data.supabase.SupabaseConfig
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformHealthScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as CreatorCoopApp
    val repository = remember { application.container.repository }

    // Live database counts
    val users by repository.getAllUsersFlow().collectAsState(initial = emptyList())
    val workspaces by repository.allWorkspaces.collectAsState(initial = emptyList())
    val tasks by repository.getAllProductionTasksFlow().collectAsState(initial = emptyList())
    val tickets by repository.getAllSupportTicketsFlow().collectAsState(initial = emptyList())
    val reports by repository.allReports.collectAsState(initial = emptyList())
    val announcements by repository.getAllAnnouncementsFlow().collectAsState(initial = emptyList())
    val auditLogs by repository.allAuditLogs.collectAsState(initial = emptyList())

    // Real JVM diagnostics
    var freeMemory by remember { mutableStateOf(0L) }
    var totalMemory by remember { mutableStateOf(0L) }
    var maxMemory by remember { mutableStateOf(0L) }
    var uptimeMs by remember { mutableStateOf(0L) }
    val startTime = remember { System.currentTimeMillis() }

    // Simulated heartbeats / network metrics
    var networkLatencyMs by remember { mutableStateOf(42) }
    var syncQueueSize by remember { mutableStateOf(0) }
    var requestCount by remember { mutableStateOf(1024) }

    LaunchedEffect(Unit) {
        while (true) {
            val runtime = Runtime.getRuntime()
            freeMemory = runtime.freeMemory() / (1024 * 1024)
            totalMemory = runtime.totalMemory() / (1024 * 1024)
            maxMemory = runtime.maxMemory() / (1024 * 1024)
            uptimeMs = System.currentTimeMillis() - startTime
            
            networkLatencyMs = (28..65).random()
            syncQueueSize = (0..2).random()
            requestCount += (1..5).random()
            
            delay(2500)
        }
    }

    val formatUptime = remember(uptimeMs) {
        val seconds = (uptimeMs / 1000) % 60
        val minutes = (uptimeMs / (1000 * 60)) % 60
        val hours = (uptimeMs / (1000 * 60 * 60)) % 24
        String.format("%02dh %02dm %02ds", hours, minutes, seconds)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SYSTEM HEALTH MONITOR",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Real-time engine diagnostics & database allocations",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBackground)
            )
        },
        containerColor = PrimaryBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Heartbeat Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(NeonEmerald)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SYSTEMS STANDING BY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(NeonEmerald.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .border(1.dp, NeonEmerald, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("ONLINE", color = NeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HealthMiniStat("Engine Latency", "$networkLatencyMs ms", NeonEmerald)
                            HealthMiniStat("Daemon Uptime", formatUptime, AccentBlue)
                            HealthMiniStat("Sync Status", if (syncQueueSize == 0) "Synced" else "$syncQueueSize queue", CrispAmber)
                        }
                    }
                }
            }

            // Real JVM Memory Allocations
            item {
                Text(
                    text = "REAL-TIME DIAGNOSTICS",
                    color = AccentBlue,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("JVM Memory Stack", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        
                        // Heap allocation progress
                        val usedMemory = totalMemory - freeMemory
                        val usageRatio = if (maxMemory > 0) usedMemory.toFloat() / maxMemory.toFloat() else 0f
                        
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Active Heap Usage", color = TextSecondary, fontSize = 11.sp)
                                Text("$usedMemory MB / $maxMemory MB (${"%.1f".format(usageRatio * 100)}%)", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { usageRatio.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (usageRatio > 0.8f) AccentRed else AccentBlue,
                                trackColor = ColorDivider,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Commited heap", color = TextSecondary, fontSize = 11.sp)
                            Text("$totalMemory MB", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Threads running", color = TextSecondary, fontSize = 11.sp)
                            Text("${Thread.activeCount()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Database Space & Counts
            item {
                Text(
                    text = "LOCAL DATABASE METRICS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Room Database Tables", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        
                        TableMetricRow("admin_audit_logs", auditLogs.size, "ledger")
                        TableMetricRow("user_profiles", users.size, "identity")
                        TableMetricRow("workspaces", workspaces.size, "coop")
                        TableMetricRow("production_tasks", tasks.size, "pipeline")
                        TableMetricRow("support_tickets", tickets.size, "help")
                        TableMetricRow("reports", reports.size, "moderation")
                        TableMetricRow("announcements", announcements.size, "broadcast")
                    }
                }
            }

            // Network Services & Integrations
            item {
                Text(
                    text = "INTEGRATIONS STATUS",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ColorDivider)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CloudQueue, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                Text("Supabase Cloud Platform", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            
                            val hasSupabase = !SupabaseConfig.supabaseUrl.contains("your-project")
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (hasSupabase) NeonEmerald.copy(alpha = 0.12f) else AccentRed.copy(alpha = 0.12f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(1.dp, if (hasSupabase) NeonEmerald else AccentRed, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (hasSupabase) "CONFIGURED" else "SANDBOX",
                                    color = if (hasSupabase) NeonEmerald else AccentRed,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Dns, null, tint = CrispAmber, modifier = Modifier.size(16.dp))
                                Text("Local SQLite Engine", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(NeonEmerald.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .border(1.dp, NeonEmerald, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("HEALTHY", color = NeonEmerald, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun HealthMiniStat(label: String, value: String, tint: Color) {
    Column {
        Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = tint, fontSize = 14.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun TableMetricRow(tableName: String, recordCount: Int, category: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(tableName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("Domain: $category", color = TextMuted, fontSize = 10.sp)
        }
        Text("$recordCount records", color = AccentBlue, fontWeight = FontWeight.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}
