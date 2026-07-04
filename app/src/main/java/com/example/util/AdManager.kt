package com.example.util

import com.example.data.model.AdPlacement
import com.example.data.model.GlobalSetting

object AdManager {
    val adFreeZones = listOf("WORKSPACE", "PERSONAL_SPACE", "TEAM_SPACE", "AGREEMENT_FLOW", "CHAT")

    fun canShowAd(
        screen: String, 
        isPremium: Boolean,
        placements: List<AdPlacement>,
        settings: List<GlobalSetting>,
        slotId: String
    ): Pair<Boolean, String> {
        if (isPremium) return false to ""
        if (adFreeZones.contains(screen)) return false to ""
        
        val globalMode = settings.find { it.settingKey == "global_mode" }?.settingValue ?: "AUTOMATIC"
        if (globalMode == "OFF") return false to ""
        
        val placement = placements.find { it.slotId == slotId }
        if (placement == null || !placement.isEnabled) return false to ""
        
        // If MANAGED, check if it's allowed on this screen
        if (globalMode == "MANAGED" && !placement.allowedLocations.contains(screen)) return false to ""
        
        return true to placement.screenLocation
    }
}
