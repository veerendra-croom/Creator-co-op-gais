package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.example.data.model.GlobalSetting
import com.example.ui.viewmodels.AdManagementViewModel
import com.example.ui.viewmodels.AppViewModelFactory
import com.example.CreatorCoopApp

@Composable
fun AdManagementScreen(
    adminId: String,
    viewModel: AdManagementViewModel
) {
    val placements by viewModel.allPlacements.collectAsState()
    val settings by viewModel.globalSettings.collectAsState()
    
    val globalMode = settings.find { it.settingKey == "global_mode" }?.settingValue ?: "AUTOMATIC"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("AD MANAGEMENT", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Global Mode: $globalMode", style = MaterialTheme.typography.bodyLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("AUTOMATIC", "MANAGED", "OFF").forEach { mode ->
                Button(onClick = { viewModel.updateSetting(GlobalSetting("global_mode", mode), adminId) }, enabled = globalMode != mode) {
                    Text(mode)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Placements", style = MaterialTheme.typography.titleLarge)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(placements) { placement ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(placement.slotName, style = MaterialTheme.typography.titleMedium)
                            Text("Location: ${placement.screenLocation}", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = placement.isEnabled,
                            onCheckedChange = { viewModel.updatePlacement(placement.copy(isEnabled = it), adminId) }
                        )
                        IconButton(onClick = { viewModel.updatePlacement(placement.copy(isEnabled = false), adminId) }) {
                            Icon(Icons.Default.Close, contentDescription = "Disable")
                        }
                    }
                }
            }
        }
    }
}
