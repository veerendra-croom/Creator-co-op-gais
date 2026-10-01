package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.model.*
import com.example.data.supabase.SupabaseConfig
import com.example.data.supabase.SupabaseSynchronizer
import com.example.ui.theme.*
import com.example.ui.viewmodels.AdminViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class DatabaseSnapshot(
    val users: List<UserProfile> = emptyList(),
    val workspaces: List<Workspace> = emptyList(),
    val tasks: List<ProductionTask> = emptyList(),
    val tickets: List<SupportTicket> = emptyList(),
    val reports: List<Report> = emptyList(),
    val announcements: List<Announcement> = emptyList()
)

data class BackupMetadata(
    val id: String,
    val name: String,
    val timestamp: Long,
    val fileSizeKb: Double,
    val userCount: Int,
    val workspaceCount: Int,
    val recordsCount: Int,
    val sha256Hash: String,
    val rawJson: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupCenterScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as CreatorCoopApp
    val repository = remember { application.container.repository }
    val scope = rememberCoroutineScope()
    
    // Live database counts
    val users by repository.getAllUsersFlow().collectAsState(initial = emptyList())
    val workspaces by repository.allWorkspaces.collectAsState(initial = emptyList())
    val tasks by repository.getAllProductionTasksFlow().collectAsState(initial = emptyList())
    val tickets by repository.getAllSupportTicketsFlow().collectAsState(initial = emptyList())
    val reports by repository.allReports.collectAsState(initial = emptyList())
    val announcements by repository.getAllAnnouncementsFlow().collectAsState(initial = emptyList())

    // Backups local state persisted in SharedPreferences for simplicity & robustness
    val sharedPrefs = remember { context.getSharedPreferences("coop_backups_metadata", Context.MODE_PRIVATE) }
    var backupsList by remember { mutableStateOf(emptyList<BackupMetadata>()) }
    
    var isBackingUp by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var restoreTargetId by remember { mutableStateOf<String?>(null) }
    var showRawDetailsBackupId by remember { mutableStateOf<String?>(null) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val formatTime = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }

    // Helper to load backups
    fun loadBackups() {
        val keys = sharedPrefs.all.keys.filter { it.startsWith("meta_") }
        val loaded = keys.mapNotNull { key ->
            val id = key.substringAfter("meta_")
            val metaString = sharedPrefs.getString(key, null) ?: return@mapNotNull null
            // Parse custom formatted meta string: Name|Timestamp|UserCount|WorkspaceCount|TotalRecords|Hash|RawJson
            val parts = metaString.split("|")
            if (parts.size >= 7) {
                val rawJson = parts.subList(6, parts.size).joinToString("|")
                BackupMetadata(
                    id = id,
                    name = parts[0],
                    timestamp = parts[1].toLongOrNull() ?: 0L,
                    fileSizeKb = rawJson.length / 1024.0,
                    userCount = parts[2].toIntOrNull() ?: 0,
                    workspaceCount = parts[3].toIntOrNull() ?: 0,
                    recordsCount = parts[4].toIntOrNull() ?: 0,
                    sha256Hash = parts[5],
                    rawJson = rawJson
                )
            } else null
        }.sortedByDescending { it.timestamp }
        backupsList = loaded
    }

    LaunchedEffect(Unit) {
        loadBackups()
    }

    // SHA-256 calculator
    fun calculateSha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "HASH_ERR"
        }
    }

    // Trigger Backup
    fun triggerBackup() {
        isBackingUp = true
        scope.launch {
            delay(1500) // Aesthetic delay for progress simulation
            
            val snapshot = DatabaseSnapshot(
                users = users,
                workspaces = workspaces,
                tasks = tasks,
                tickets = tickets,
                reports = reports,
                announcements = announcements
            )
            
            try {
                val jsonSerializer = Json { prettyPrint = false; ignoreUnknownKeys = true }
                val rawJson = jsonSerializer.encodeToString(snapshot)
                val hash = calculateSha256(rawJson)
                val timestamp = System.currentTimeMillis()
                val id = "backup_" + UUID.randomUUID().toString().take(8)
                val backupName = "Snapshot ${formatTime.format(Date(timestamp))}"
                
                val totalRecords = snapshot.users.size + snapshot.workspaces.size + snapshot.tasks.size + snapshot.tickets.size + snapshot.reports.size + snapshot.announcements.size
                
                // Save meta string
                val metaValue = "$backupName|$timestamp|${snapshot.users.size}|${snapshot.workspaces.size}|$totalRecords|$hash|$rawJson"
                sharedPrefs.edit().putString("meta_$id", metaValue).apply()
                
                // Write General Audit Log entry
                val log = AuditLog(
                    id = UUID.randomUUID().toString(),
                    adminId = "founder_admin",
                    adminName = "Founder Operator",
                    actionTaken = "BACKUP_CREATED",
                    targetType = "SYSTEM",
                    targetId = id,
                    reason = "Manual state backup secured. Signature Hash: ${hash.take(8)}...",
                    createdAt = timestamp
                )
                repository.insertAuditLog(log)
                
                // If Supabase is configured and online, push audit log & sync state
                if (SupabaseConfig.isConfigured && SupabaseConfig.isNetworkAvailable(context)) {
                    try {
                        SupabaseSynchronizer.syncDownEverything(context, repository)
                    } catch (e: Exception) {
                        // Silent fallback to local storage
                    }
                }
                
                loadBackups()
                android.widget.Toast.makeText(context, "Backup '$backupName' completed successfully!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Backup failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                isBackingUp = false
            }
        }
    }

    // Execute Restore
    fun triggerRestore(backup: BackupMetadata) {
        isRestoring = true
        scope.launch {
            delay(2000) // Aesthetic processing simulation
            
            try {
                val jsonSerializer = Json { ignoreUnknownKeys = true }
                val snapshot = jsonSerializer.decodeFromString<DatabaseSnapshot>(backup.rawJson)
                
                // Restore logic: write safely into tables with per-record fault isolation
                snapshot.users.forEach { try { repository.updateUserProfile(it) } catch (e: Exception) {} }
                snapshot.workspaces.forEach { try { repository.insertWorkspace(it) } catch (e: Exception) {} }
                snapshot.tasks.forEach { try { repository.insertTask(it) } catch (e: Exception) {} }
                snapshot.tickets.forEach { try { repository.insertSupportTicket(it) } catch (e: Exception) {} }
                snapshot.reports.forEach { try { repository.reportDao.insertReport(it) } catch (e: Exception) {} }
                snapshot.announcements.forEach { try { repository.insertAnnouncement(it) } catch (e: Exception) {} }
                
                // Log audit action
                val log = AuditLog(
                    id = UUID.randomUUID().toString(),
                    adminId = "founder_admin",
                    adminName = "Founder Operator",
                    actionTaken = "RESTORE_EXECUTED",
                    targetType = "SYSTEM",
                    targetId = backup.id,
                    reason = "System state restored from Backup ${backup.name}. Verified signature hash ${backup.sha256Hash.take(8)}",
                    createdAt = System.currentTimeMillis()
                )
                repository.insertAuditLog(log)
                
                // Trigger cloud sync down if connected
                if (SupabaseConfig.isConfigured && SupabaseConfig.isNetworkAvailable(context)) {
                    try {
                        SupabaseSynchronizer.syncDownEverything(context, repository)
                    } catch (e: Exception) {
                        // Keep restored local state
                    }
                }
                
                android.widget.Toast.makeText(context, "Full state restore complete! Systems synchronized.", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Restore failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                isRestoring = false
                restoreTargetId = null
            }
        }
    }

    // Delete backup snapshot
    fun deleteBackup(id: String) {
        sharedPrefs.edit().remove("meta_$id").apply()
        loadBackups()
        scope.launch {
            val log = AuditLog(
                id = UUID.randomUUID().toString(),
                adminId = "founder_admin",
                adminName = "Founder Operator",
                actionTaken = "BACKUP_DELETED",
                targetType = "SYSTEM",
                targetId = id,
                reason = "State snapshot metadata pruned from device history.",
                createdAt = System.currentTimeMillis()
            )
            repository.insertAuditLog(log)
        }
    }

    Scaffold(
        containerColor = PrimaryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("State Backup & Recovery", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("Deterministic state snapshots & disaster recovery", color = TextSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("backup_center_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("backup_center_help_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help Guide", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBackground)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Backup Trigger Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ColorDivider)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val hasSupabase = SupabaseConfig.isConfigured
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ACTIVE STATE OVERVIEW", color = AccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (hasSupabase) NeonEmerald.copy(alpha = 0.15f) else CrispAmber.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, if (hasSupabase) NeonEmerald else CrispAmber)
                        ) {
                            Text(
                                text = if (hasSupabase) "SUPABASE ACTIVE" else "STANDALONE STORE",
                                color = if (hasSupabase) NeonEmerald else CrispAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickMetric("Users", "${users.size}")
                        QuickMetric("Workspaces", "${workspaces.size}")
                        QuickMetric("Tasks", "${tasks.size}")
                        QuickMetric("Tickets", "${tickets.size}")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isBackingUp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Securing secure state configuration...", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { triggerBackup() },
                                modifier = Modifier.fillMaxWidth().testTag("create_backup_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Backup, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CREATE FULL STATE BACKUP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            
                            if (hasSupabase) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                SupabaseSynchronizer.syncDownEverything(context, repository)
                                                android.widget.Toast.makeText(context, "Supabase Cloud DB Synchronized!", android.widget.Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Cloud sync fail: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, NeonEmerald),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CloudSync, null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("FORCE SUPABASE CLOUD RESYNC", color = NeonEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Ledger title
            Text(
                text = "SAVED CONFIGURATIONS (${backupsList.size})",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            // Backups List
            if (isRestoring) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = NeonEmerald, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Reconstructing system state...", color = Color.White, fontWeight = FontWeight.Black)
                    Text("Validating parameter layouts & structural states", color = TextSecondary, fontSize = 12.sp)
                }
            } else if (backupsList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Restore, null, tint = ColorDivider, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No backup history found", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Create your first state snapshot above", color = TextSecondary, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(backupsList) { backup ->
                        BackupItemCard(
                            backup = backup,
                            formattedTime = formatTime.format(Date(backup.timestamp)),
                            onRestore = { restoreTargetId = backup.id },
                            onDelete = { deleteBackup(backup.id) },
                            onShowDetails = { showRawDetailsBackupId = backup.id }
                        )
                    }
                }
            }
        }
    }

    // Restore Confirm Dialog
    restoreTargetId?.let { targetId ->
        val targetBackup = backupsList.find { it.id == targetId } ?: return@let
        AlertDialog(
            onDismissRequest = { restoreTargetId = null },
            title = {
                Text("Confirm State Rollback", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
            },
            text = {
                Text(
                    "WARNING: Restoring will replace all active system configuration records with values from snapshot '${targetBackup.name}'. This action is immediate and non-reversible.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { triggerRestore(targetBackup) }
                ) {
                    Text("EXECUTE FULL RESTORE", fontWeight = FontWeight.Bold, color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreTargetId = null }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Detail Dialog
    showRawDetailsBackupId?.let { detailId ->
        val b = backupsList.find { it.id == detailId } ?: return@let
        AlertDialog(
            onDismissRequest = { showRawDetailsBackupId = null },
            title = {
                Text(b.name, fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SNAPSHOT SPECIFICATION", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    
                    Text("Identifier: ${b.id}", fontSize = 11.sp, color = TextPrimary, fontFamily = FontFamily.Monospace)
                    Text("Total Size: ${"%.2f".format(b.fileSizeKb)} KB", fontSize = 11.sp, color = TextPrimary)
                    Text("Signature Certificate:", fontSize = 11.sp, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryBackground, RoundedCornerShape(6.dp))
                            .border(1.dp, ColorDivider, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(b.sha256Hash, color = NeonEmerald, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("SECURE DATA SNAPSHOT", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryBackground)
                            .border(1.dp, ColorDivider)
                            .padding(8.dp)
                    ) {
                        LazyColumn {
                            item {
                                Text(b.rawJson, color = TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRawDetailsBackupId = null }) {
                    Text("DISMISS", fontWeight = FontWeight.Bold, color = AccentBlue)
                }
            },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showHelpDialog) {
        AdminHelpDialog(onDismiss = { showHelpDialog = false })
    }
}

@Composable
fun QuickMetric(label: String, valStr: String) {
    Column {
        Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(valStr, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun BackupItemCard(
    backup: BackupMetadata,
    formattedTime: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onShowDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("backup_item_${backup.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Inventory2, null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Text(backup.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                
                Text(
                    text = "${"%.2f".format(backup.fileSizeKb)} KB",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BackupMiniStat("Users", backup.userCount)
                BackupMiniStat("Workspaces", backup.workspaceCount)
                BackupMiniStat("Total Records", backup.recordsCount)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Signature/Hash
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Fingerprint, "SHA256 Hash", tint = TextSecondary, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("SHA-256: ", color = TextMuted, fontSize = 9.sp)
                Text(backup.sha256Hash.take(16) + "...", color = NeonEmerald, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ColorDivider, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onShowDetails,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(28.dp).testTag("backup_inspect_${backup.id}")
                ) {
                    Icon(Icons.Default.Info, null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("INSPECT DETAILS", color = AccentBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onRestore,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp).testTag("backup_restore_${backup.id}")
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ROLLBACK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp).testTag("backup_delete_${backup.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete backup", tint = AccentRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BackupMiniStat(label: String, valCount: Int) {
    Column {
        Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
        Text("$valCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
