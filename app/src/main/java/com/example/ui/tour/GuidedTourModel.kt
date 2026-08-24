package com.example.ui.tour

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class TourStatus {
    NOT_STARTED,
    COMPLETED,
    SKIPPED
}

enum class TooltipPosition {
    AUTO,
    ABOVE,
    BELOW,
    TOP,
    BOTTOM
}

data class GuidedTourStep(
    val targetKey: String,
    val title: String,
    val description: String,
    val preferredPosition: TooltipPosition = TooltipPosition.AUTO,
    val actionHint: String? = null,
    val icon: ImageVector? = null
)

data class GuidedTourConfig(
    val tourId: String,
    val screenTitle: String,
    val targetTab: String,
    val steps: List<GuidedTourStep>
)

object GuidedTourRepository {

    val DASHBOARD_TOUR = GuidedTourConfig(
        tourId = "dashboard_tour",
        screenTitle = "Dashboard Guided Tour",
        targetTab = "HOME",
        steps = listOf(
            GuidedTourStep(
                targetKey = "dashboard_header",
                title = "Creator Identity & Active Status",
                description = "This is your active Creator Profile card. Tap your circular avatar anytime to view or edit your public bio, skills, and portfolio projects.",
                actionHint = "Check your online status badge at the top right.",
                icon = Icons.Default.AccountCircle
            ),
            GuidedTourStep(
                targetKey = "dashboard_metrics_sparkline",
                title = "Performance Telemetry & Sparklines",
                description = "Real-time interactive canvas showing system states, latency metrics, and network diagnostics. Hover or swipe for direct, fluid updates.",
                actionHint = "Track live metrics changes.",
                icon = Icons.Default.BarChart
            ),
            GuidedTourStep(
                targetKey = "dashboard_quick_actions",
                title = "Operations Directory Shortcuts",
                description = "Rapidly execute core workflows like launching workspaces, posting creative role openings, or delivering project milestones.",
                actionHint = "Tap any operational tile to fast-track your work.",
                icon = Icons.Default.TouchApp
            ),
            GuidedTourStep(
                targetKey = "dashboard_karma_gauge",
                title = "Karma Reputation Score",
                description = "Your on-chain Proof-of-Contribution score (0-100), calculated algorithmically from on-time milestone deliveries and peer reviews. High Karma unlocks zero escrow fees!",
                actionHint = "Deliver milestones on schedule to increase your Karma rating.",
                icon = Icons.Default.Stars
            ),
            GuidedTourStep(
                targetKey = "dashboard_setup_checklist",
                title = "3-Step Setup Checklist",
                description = "Complete Profile Setup, Join or Create a Workspace, and Sign an Agreement to earn your verified Pro Syndicate badge.",
                actionHint = "Tap any checklist card to jump directly to that setup task.",
                icon = Icons.Default.CheckCircle
            )
        )
    )

    val WORKSPACES_TOUR = GuidedTourConfig(
        tourId = "workspaces_tour",
        screenTitle = "Production Workspaces Guide",
        targetTab = "WORKSPACES",
        steps = listOf(
            GuidedTourStep(
                targetKey = "workspace_sprint_tracker",
                title = "Workspace Sprint Tracker",
                description = "Monitor sprint status, milestones, and progress ratios in real time. Ensure your deadlines stay on track.",
                icon = Icons.Default.Speed
            ),
            GuidedTourStep(
                targetKey = "workspace_kanban_columns",
                title = "Agile Kanban Lanes",
                description = "Organize, move, and execute media tasks across production stages from Ideation to Sign-Off.",
                icon = Icons.Default.ViewWeek
            ),
            GuidedTourStep(
                targetKey = "workspace_task_assignment",
                title = "Task Assignment Control",
                description = "Assign production cards to team members dynamically to establish direct accountability.",
                icon = Icons.Default.AssignmentInd
            ),
            GuidedTourStep(
                targetKey = "workspaces_agreements_escrow",
                title = "SHA-256 Milestone Escrow",
                description = "Lock project milestone funds in digital escrow before work begins. Payments auto-release upon deliverable approval.",
                icon = Icons.Default.Gavel
            )
        )
    )

    val DISCOVERY_TOUR = GuidedTourConfig(
        tourId = "discovery_tour",
        screenTitle = "Discovery & Syndicate Matchmaker",
        targetTab = "DISCOVERY",
        steps = listOf(
            GuidedTourStep(
                targetKey = "matchmaker_pitch_filters",
                title = "Specialty & Niche Filters",
                description = "Quickly isolate collaboration pitches matching specific domains like VFX, Color Grading, Sound Design, or 3D Modeling.",
                icon = Icons.Default.FilterAlt
            ),
            GuidedTourStep(
                targetKey = "matchmaker_pitch_fab",
                title = "Syndicate Pitch Creation",
                description = "Tap the float action button to establish a new role opening, pitch a co-production, or build custom revenue splits.",
                icon = Icons.Default.Add
            )
        )
    )

    val AGREEMENT_VAULT_TOUR = GuidedTourConfig(
        tourId = "agreement_vault_tour",
        screenTitle = "Digital Agreement Vault",
        targetTab = "WORKSPACES", // Agreement is inside workspaces
        steps = listOf(
            GuidedTourStep(
                targetKey = "agreement_clause_editor",
                title = "Dynamic Clause Editor",
                description = "Review, customize, and edit programmatic contract terms, milestone schedules, and revenue split ratios directly.",
                icon = Icons.Default.EditNote
            ),
            GuidedTourStep(
                targetKey = "agreement_sign_button",
                title = "Cryptographic Signing Engine",
                description = "Authorize terms with an immutable SHA-256 digital signature to anchor the contract in escrow securely.",
                icon = Icons.Default.Fingerprint
            )
        )
    )

    val VIDEO_HUDDLE_TOUR = GuidedTourConfig(
        tourId = "video_huddle_tour",
        screenTitle = "Co-Op Video Huddle",
        targetTab = "VIDEO_HUDDLE",
        steps = listOf(
            GuidedTourStep(
                targetKey = "huddle_participants",
                title = "Live Peer Grid",
                description = "View real-time video streams and active indicators of online teammates participating in the huddle.",
                icon = Icons.Default.GridView
            ),
            GuidedTourStep(
                targetKey = "huddle_toggle_controls",
                title = "Media Stream Controls",
                description = "Instantly toggle microphone, camera, screen-share streams, or leave the active huddle gracefully.",
                icon = Icons.Default.SettingsVoice
            ),
            GuidedTourStep(
                targetKey = "huddle_chat_drawer",
                title = "Persistent Huddle Text Chat",
                description = "Discuss storyboard concepts, align timelines, or post quick links without disrupting the video conversation.",
                icon = Icons.Default.ChatBubble
            )
        )
    )

    val COMMONS_TOUR = GuidedTourConfig(
        tourId = "commons_tour",
        screenTitle = "Creator Commons Guide",
        targetTab = "COMMONS",
        steps = listOf(
            GuidedTourStep(
                targetKey = "commons_feed_card",
                title = "Showcase & Peer Feedback Feed",
                description = "Share work-in-progress video clips and 3D renders to get peer critiques and build your public portfolio.",
                icon = Icons.Default.Forum
            ),
            GuidedTourStep(
                targetKey = "commons_polls_card",
                title = "Syndicate Governance Polls",
                description = "Vote on platform proposals, revenue split standards, and community feature requests.",
                icon = Icons.Default.HowToVote
            )
        )
    )

    val PROFILE_TOUR = GuidedTourConfig(
        tourId = "profile_tour",
        screenTitle = "Profile & Portfolio Guide",
        targetTab = "PROFILE",
        steps = listOf(
            GuidedTourStep(
                targetKey = "profile_identity_card",
                title = "Verified Creator Profile",
                description = "Display your verified specialty badges, client reviews, bio, and showreel videos.",
                icon = Icons.Default.Person
            ),
            GuidedTourStep(
                targetKey = "profile_referral_box",
                title = "Referral & Bonus Engine",
                description = "Invite fellow creators with your unique code to earn +25 bonus Karma points upon their first completed milestone.",
                icon = Icons.Default.Share
            )
        )
    )

    val SETTINGS_TOUR = GuidedTourConfig(
        tourId = "settings_tour",
        screenTitle = "Settings & App Guide",
        targetTab = "SETTINGS",
        steps = listOf(
            GuidedTourStep(
                targetKey = "settings_help_tutorials",
                title = "Contextual Guided Tutorials",
                description = "Replay screen-specific guided tours anytime or reset all tutorial completion history.",
                icon = Icons.Default.HelpOutline
            ),
            GuidedTourStep(
                targetKey = "settings_theme_toggle",
                title = "Appearance & Preferences",
                description = "Toggle dark obsidian themes, adjust UI font size scaling, and configure haptic feedback.",
                icon = Icons.Default.Palette
            )
        )
    )

    val CONTENT_PIPELINE_TOUR = GuidedTourConfig(
        tourId = "content_pipeline_tour",
        screenTitle = "Content Release Pipeline Guide",
        targetTab = "CONTENT_PIPELINE",
        steps = listOf(
            GuidedTourStep(
                targetKey = "pipeline_funnel_board",
                title = "Multi-Channel Release Scheduler",
                description = "Track video release schedules across YouTube, TikTok, Vimeo, and Steam with automated countdown timers.",
                icon = Icons.Default.Analytics
            )
        )
    )

    val COMM_CENTER_TOUR = GuidedTourConfig(
        tourId = "comm_center_tour",
        screenTitle = "Communication Center Guide",
        targetTab = "COMM_CENTER",
        steps = listOf(
            GuidedTourStep(
                targetKey = "comm_center_broadcast",
                title = "Global Announcement Broadcasts",
                description = "Dispatch platform-wide in-app announcement banners and scheduled maintenance notifications.",
                icon = Icons.Default.Campaign
            )
        )
    )

    val FOUNDER_COMMAND_TOUR = GuidedTourConfig(
        tourId = "founder_command_tour",
        screenTitle = "Founder Command Cockpit Guide",
        targetTab = "FOUNDER_COMMAND",
        steps = listOf(
            GuidedTourStep(
                targetKey = "founder_command_cockpit",
                title = "Executive Governance Cockpit",
                description = "Executive control panel with founder attributions (Botla Veerendra & Macha Praveen) and live platform health telemetry.",
                icon = Icons.Default.Security
            )
        )
    )

    val ALL_TOURS: List<GuidedTourConfig> = listOf(
        DASHBOARD_TOUR,
        WORKSPACES_TOUR,
        DISCOVERY_TOUR,
        AGREEMENT_VAULT_TOUR,
        VIDEO_HUDDLE_TOUR,
        COMMONS_TOUR,
        PROFILE_TOUR,
        SETTINGS_TOUR,
        CONTENT_PIPELINE_TOUR,
        COMM_CENTER_TOUR,
        FOUNDER_COMMAND_TOUR
    )

    fun getTourById(tourId: String): GuidedTourConfig? {
        return ALL_TOURS.find { it.tourId == tourId }
    }
}
