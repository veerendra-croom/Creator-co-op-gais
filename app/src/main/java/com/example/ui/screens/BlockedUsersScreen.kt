package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.UserSetting
import com.example.ui.theme.*
import com.example.ui.viewmodels.GlobalViewModel
import kotlinx.coroutines.launch

@Composable
fun BlockedUsersScreen(
    onBack: () -> Unit,
    globalViewModel: GlobalViewModel,
    userProfile: UserProfile?
) {
    val application = LocalContext.current.applicationContext as com.example.CreatorCoopApp
    val repository = remember { application.container.repository }
    val currentUserId = userProfile?.id ?: "DemoUser"
    val snackbarHostState = remember { SnackbarHostState() }

    val creators by repository.getAllUsersFlow().collectAsState(initial = emptyList())
    var blockedIdsList by remember { mutableStateOf<List<String>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch from Settings Room table
    LaunchedEffect(currentUserId) {
        val rawValue = repository.userSettingsDao.getSetting(currentUserId, "blocked_users_csv") ?: ""
        blockedIdsList = if (rawValue.isBlank()) emptyList() else rawValue.split(",")
    }

    val updateBlockedSetting = { newBlockedList: List<String>, feedback: String ->
        blockedIdsList = newBlockedList
        val csv = newBlockedList.joinToString(",")
        coroutineScope.launch {
            repository.userSettingsDao.setSetting(
                UserSetting(
                    id = "${currentUserId}_blocked_users_csv",
                    userId = currentUserId,
                    key = "blocked_users_csv",
                    value = csv
                )
            )
            snackbarHostState.showSnackbar(feedback)
        }
    }

    // Map blocked list to User Profiles
    val blockedUsers = remember(creators, blockedIdsList) {
        creators.filter { it.id in blockedIdsList }
    }

    // Creators who are NOT blocked and not me
    val unblockedCreators = remember(creators, blockedIdsList, currentUserId) {
        creators.filter { it.id != currentUserId && it.id !in blockedIdsList }
    }

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
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Blocked Users Manager", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        Text(
            text = "Users you block will no longer be able to message you or see your public profile. Persistent setting resides in SQLite.",
            color = TextSecondary,
            fontSize = 13.sp
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    text = "BLOCKED CREATORS (${blockedUsers.size})",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            if (blockedUsers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No blocked users. Your network interactions are fully open.", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(blockedUsers, key = { "blocked_${it.id}" }) { user ->
                    BlockedUserItem(
                        name = user.displayName,
                        username = "@${user.username}",
                        onUnblock = {
                            val newList = blockedIdsList.toMutableList().apply { remove(user.id) }
                            updateBlockedSetting(newList, "Unblocked @${user.username}")
                        }
                    )
                }
            }

            // Divider Space
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DISCOVER TO BLOCK (CREATORS DIRECTORY)",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            if (unblockedCreators.isEmpty()) {
                item {
                    Text("No other active creators to block.", color = TextMuted, fontSize = 13.sp)
                }
            } else {
                items(unblockedCreators, key = { "unblocked_${it.id}" }) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ColorDivider)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape).background(ColorDivider),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(user.displayName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("@${user.username}", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    val newList = blockedIdsList.toMutableList().apply { add(user.id) }
                                    updateBlockedSetting(newList, "Blocked @${user.username}")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.2f), contentColor = AccentRed),
                                border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Block", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun BlockedUserItem(name: String, username: String, onUnblock: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(ColorDivider),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonOff, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(username, color = TextSecondary, fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = onUnblock,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, ColorDivider),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Unblock", fontSize = 12.sp)
            }
        }
    }
}
