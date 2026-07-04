package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdPlacement
import com.example.data.model.AuditLog
import com.example.data.model.GlobalSetting
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AdManagementViewModel(
    private val repository: AppRepository
) : ViewModel() {

    val allPlacements: StateFlow<List<AdPlacement>> = repository.allAdPlacements.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val globalSettings: StateFlow<List<GlobalSetting>> = repository.globalAdSettings.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updatePlacement(placement: AdPlacement, adminId: String) {
        viewModelScope.launch {
            repository.updateAdPlacement(placement.copy(lastModifiedAt = System.currentTimeMillis(), lastModifiedByAdminId = adminId))
            logAction(adminId, "UPDATE_AD_PLACEMENT", "AD_PLACEMENT", placement.slotId, "Updated placement ${placement.slotName}")
        }
    }

    fun updateSetting(setting: GlobalSetting, adminId: String) {
        viewModelScope.launch {
            repository.updateGlobalAdSetting(setting)
            logAction(adminId, "UPDATE_GLOBAL_SETTING", "GLOBAL_SETTING", setting.settingKey, "Updated setting ${setting.settingKey} to ${setting.settingValue}")
        }
    }

    private suspend fun logAction(adminId: String, action: String, targetType: String, targetId: String, reason: String) {
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            adminId = adminId,
            actionTaken = action,
            targetType = targetType,
            targetId = targetId,
            reason = reason,
            createdAt = System.currentTimeMillis()
        )
        repository.insertAuditLog(log)
    }
}
