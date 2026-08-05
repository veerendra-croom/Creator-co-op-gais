package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdPlacement
import com.example.data.model.GlobalSetting
import com.example.ui.theme.*
import com.example.util.AdManager

@Composable
fun AdBanner(
    slotId: String,
    screenName: String,
    isPremium: Boolean,
    placements: List<AdPlacement>,
    settings: List<GlobalSetting>,
    onUpgradeClick: (() -> Unit)? = null
) {
    val (canShow, location) = AdManager.canShowAd(screenName, isPremium, placements, settings, slotId)
    
    if (canShow) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("ad_banner_$slotId"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF11151D)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, ColorDivider)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CrispAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "SPONSORED PARTNER",
                            color = CrispAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AccentBlue.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "STANDARD FREE TIER",
                            color = AccentBlue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Unlock Unlimited Active Syndicates & Priority Placement",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Standard members are supported by curated co-op partner listings. Upgrade to Creator Pro for an ad-free workspace experience.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                if (onUpgradeClick != null) {
                    Button(
                        onClick = onUpgradeClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("ad_banner_upgrade_btn")
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("UPGRADE TO PRO FOR AD-FREE", fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    } else {
        Spacer(modifier = Modifier.height(0.dp))
    }
}

