package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.util.AdManager
import com.example.data.model.AdPlacement
import com.example.data.model.GlobalSetting

@Composable
fun AdBanner(
    slotId: String,
    screenName: String,
    isPremium: Boolean,
    placements: List<AdPlacement>,
    settings: List<GlobalSetting>
) {
    val (canShow, location) = AdManager.canShowAd(screenName, isPremium, placements, settings, slotId)
    
    if (canShow) {
        // Placeholder for real AdMob Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.LightGray)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Ad Placeholder - Slot: $slotId - Location: $location")
        }
    } else {
        Spacer(modifier = Modifier.height(0.dp))
    }
}
