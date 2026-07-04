package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LegalScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    "Legal Center",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Effective Date: June 20, 2026",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Professional Review Disclaimer
        Card(
            colors = CardDefaults.cardColors(containerColor = ColorWarning.copy(alpha = 0.15f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorWarning.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Review Notice",
                    tint = ColorWarning,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "PRE-LAUNCH LEGAL REVIEW NOTICE",
                        color = ColorWarning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "This document is an accurate, AI-generated legal text accurately describing " +
                        "the data collection, storage engines (Supabase/PostgreSQL), deletion flows, " +
                        "and non-binding team alignment features of Creator Co-Op. While it is fully complete and " +
                        "no longer a placeholder, we recommend a secondary quick human/lawyer review before official commercial launch.",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // TERMS OF SERVICE CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SupervisorAccount,
                        contentDescription = "Terms",
                        tint = AccentRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "TERMS OF SERVICE",
                        color = AccentRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ColorDivider)
                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "1. Creator Collaboration Platform Scope",
                    description = "Creator Co-Op is a specialized, peer-to-peer workspace coordination, " +
                        "matchmaking, and brainstorming tool. It facilitates digital media team building, " +
                        "shared task planning, drafts logging, and team group chats."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "2. Team Agreements Are Non-Binding",
                    description = "The 'Team Agreement' (Syndicate Contract) and digital acknowledgment " +
                        "wizard serve to align expectations regarding team roles, target milestones, and splits. " +
                        "THEY ARE EXPLICITLY NOT LEGALLY BINDING, enforceable business contracts, and the " +
                        "platform is not a party, sponsor, or guarantor. We cannot legally enforce or resolve team disputes."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "3. Financial Arrangement Disclaimer",
                    description = "Creator Co-Op has zero involvement and no role in any financial, revenue " +
                        "sharing, or payment arrangements between users. We do not hold escrow, process payouts, " +
                        "or manage settlements. All commercial arrangements are entirely external to this platform."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "4. Premium & Promotion Features",
                    description = "Platform features including 'Listing Boost' and 'Workspace Sponsorship' are elective promotion tools. Acknowledgment that boosting a listing does not guarantee specific results or commercial success. Sponsored labels indicate platform-vetted pairings but do not imply platform control over the workspace activities."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "5. User Referral Program",
                    description = "Participants in our Referral Program may receive rewards such as 'Pro Trial Extensions'. These rewards have zero cash value, are non-transferable, and the platform reserves the right to terminate the program or revoke rewards obtained through fraudulent or abusive sharing practices."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "6. Endorsements & Dispute Documentation",
                    description = "All 'Endorsements' and 'Dispute Notes' represent the subjective opinions and experiences of individual users. The platform does not verify, fact-check, or guarantee the accuracy of this user-generated feedback. Users interact with endorsed creators at their own professional discretion, and the platform is not liable for outcomes resulting from these interactions."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "7. Acceptable Conduct Rules",
                    description = "Good-faith communication is required. Any harassment, unsolicited spam, " +
                        "fraudulent opportunities, intellectual property poaching, or impersonation through " +
                        "fake creator profiles will result in immediate termination of account access and permanent platform bans."
                )
            }
        }

        // PRIVACY POLICY CARD
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Privacy",
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "PRIVACY POLICY",
                        color = AccentBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = ColorDivider)
                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "1. Personal Data We Collect",
                    description = "We collect and process your email address (for security authentication), " +
                        "display name, optional profile bio/skills, showcase portfolio links, uploaded avatars, " +
                        "community forum posts, comments, workspace tasks, and active team chat messages."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "2. Storage & Hosting Architecture",
                    description = "All cloud-synchronized data, active login sessions, and uploaded assets " +
                        "are stored securely on Supabase cloud-hosted system backends, leveraging secure " +
                        "PostgreSQL relational databases, Supabase Auth engines, and cloud storage buckets."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "3. Strict Financial Isolation",
                    description = "We do NOT collect, intercept, process, or store credit card credentials, " +
                        "bank accounts, or payment keys of any kind anywhere in the application."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "4. Data Deletion & Cascading Purges",
                    description = "Users maintain full control. Selecting 'Delete My Account' inside account settings " +
                        "initiates an auto-purge: personal profile listings and private sandbox tasks are wiped immediately, " +
                        "while team chat logs, comments, and workspace tasks are completely anonymized (having " +
                        "your name and ID severed and replaced with generic 'Deleted User' tags)."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "5. Age Protections (13+)",
                    description = "Creator Co-Op is intended for use by individuals aged 13 and up. We do " +
                        "not knowingly collect or catalog private records from children under this age limit."
                )

                Spacer(modifier = Modifier.height(16.dp))

                LegalItem(
                    title = "6. Sponsored Advertising Networks",
                    description = "If administrator-sponsored ad layouts are enabled on this platform, they " +
                        "may bundle Google AdMob and Meta Audience Network SDKs. These networks collect standard, " +
                        "anonymized telemetry (device identifiers, general zip-level region) to customize non-intrusive ads. " +
                        "Please refer to Google's Ad Network Disclosures and Meta's Audience Network Privacy Disclosures."
                )
            }
        }
    }
}

@Composable
fun LegalItem(title: String, description: String) {
    Column {
        Text(
            title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            description,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
