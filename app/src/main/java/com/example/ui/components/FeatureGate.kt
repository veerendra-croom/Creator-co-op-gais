package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeatureFlag
import com.example.data.repository.AppRepository
import com.example.ui.theme.*

/**
 * Checks whether a feature flag is enabled for the specified role.
 *
 * @param flag The feature flag model (or null)
 * @param role "ORGANIZER", "PARTICIPANT", or null (general user)
 * @return True if permitted by global override and role permissions
 */
fun isFeatureAllowed(flag: FeatureFlag?, role: String = "PARTICIPANT"): Boolean {
    if (flag == null) return true // Default fallback if flag not found
    if (!flag.globalOverrideEnabled || !flag.isEnabled) return false
    
    val normalizedRole = role.uppercase()
    return when {
        normalizedRole == "ORGANIZER" || normalizedRole == "OWNER" || normalizedRole == "ADMIN" || normalizedRole == "LEAD CREATOR" || normalizedRole == "HEAD" -> flag.organizerEnabled
        else -> flag.participantEnabled
    }
}

/**
 * High-Pro-Max Reactive Feature Gate Guard.
 * Renders the protected content when allowed, or renders a sleek Obsidian Slate restriction banner / fallback.
 */
@Composable
fun FeatureGate(
    flagKey: String,
    featureFlags: List<FeatureFlag>,
    userRole: String = "PARTICIPANT",
    showBannerOnRestricted: Boolean = true,
    customRestrictedNotice: String? = null,
    modifier: Modifier = Modifier,
    fallback: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val flag = featureFlags.find { it.flagKey.equals(flagKey, ignoreCase = true) }
    val isAllowed = isFeatureAllowed(flag, userRole)

    if (isAllowed) {
        content()
    } else {
        if (fallback != null) {
            fallback()
        } else if (showBannerOnRestricted) {
            FeatureRestrictedBanner(
                flagKey = flagKey,
                reason = customRestrictedNotice ?: flag?.description ?: "This capability is currently restricted by platform administration.",
                modifier = modifier
            )
        }
    }
}

/**
 * AppRepository overload for reactive subscription to local Room / Supabase flags.
 */
@Composable
fun FeatureGate(
    flagKey: String,
    repository: AppRepository,
    userRole: String = "PARTICIPANT",
    showBannerOnRestricted: Boolean = true,
    customRestrictedNotice: String? = null,
    modifier: Modifier = Modifier,
    fallback: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val flags by repository.getAllFeatureFlagsFlow().collectAsState(initial = emptyList())
    FeatureGate(
        flagKey = flagKey,
        featureFlags = flags,
        userRole = userRole,
        showBannerOnRestricted = showBannerOnRestricted,
        customRestrictedNotice = customRestrictedNotice,
        modifier = modifier,
        fallback = fallback,
        content = content
    )
}

@Composable
fun FeatureRestrictedBanner(
    flagKey: String = "",
    reason: String = "",
    featureTitle: String? = null,
    restrictedMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val displayKey = featureTitle ?: flagKey
    val displayReason = restrictedMessage ?: reason.ifBlank { "This capability is currently restricted by platform governance." }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("feature_restricted_banner_${flagKey.ifBlank { "custom" }}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ColorDivider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(CrispAmber.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Feature Restricted",
                    tint = CrispAmber,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CAPABILITY RESTRICTED",
                        color = CrispAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                    if (displayKey.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[$displayKey]",
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = displayReason,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

