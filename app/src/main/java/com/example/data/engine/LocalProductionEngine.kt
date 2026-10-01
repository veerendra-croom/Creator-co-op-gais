package com.example.data.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-Performance Local Production Engine.
 * Executes all brief syntheses, contract clause generation, task breakdowns,
 * and pitch writing deterministically offline with $0 API and server costs.
 */
object LocalProductionEngine {

    data class ContractClause(
        val clauseNumber: String,
        val title: String,
        val body: String,
        val category: String
    )

    data class ProductionTaskTemplate(
        val title: String,
        val description: String,
        val columnCategory: String,
        val estimatedHours: Int
    )

    /**
     * Synthesizes a rough personal draft into a professional, production-ready
     * creator brief entirely locally with deterministic rule mapping.
     */
    suspend fun synthesizeDraftToBrief(title: String, rawContent: String): String = withContext(Dispatchers.Default) {
        val sanitizedTitle = title.trim().ifEmpty { "Untitled Project" }
        val sanitizedContent = rawContent.trim().ifEmpty { "No additional draft notes provided." }
        
        val lowercaseTitle = sanitizedTitle.lowercase()
        val lowercaseContent = sanitizedContent.lowercase()
        
        val category = when {
            lowercaseTitle.contains("vfx") || lowercaseContent.contains("vfx") || lowercaseTitle.contains("cgi") || lowercaseTitle.contains("3d") -> "VFX & Cinematic Action"
            lowercaseTitle.contains("game") || lowercaseTitle.contains("gaming") || lowercaseContent.contains("game") || lowercaseTitle.contains("play") -> "Gaming & Let's Play Highlight"
            lowercaseTitle.contains("tech") || lowercaseTitle.contains("code") || lowercaseTitle.contains("review") || lowercaseTitle.contains("unboxing") -> "Tech Review & Product Showcase"
            lowercaseTitle.contains("vlog") || lowercaseTitle.contains("day") || lowercaseContent.contains("vlog") || lowercaseTitle.contains("travel") -> "Lifestyle Vlog & Daily Beat"
            lowercaseTitle.contains("podcast") || lowercaseTitle.contains("talk") || lowercaseContent.contains("interview") || lowercaseTitle.contains("discussion") -> "Interactive Talk & Podcast Episode"
            lowercaseTitle.contains("short") || lowercaseTitle.contains("reel") || lowercaseTitle.contains("tiktok") -> "High-Velocity Vertical Short"
            else -> "Digital Content Creator Standard"
        }
        
        val retentionHook = when (category) {
            "VFX & Cinematic Action" -> "Start with an explosive cold open showing the climax VFX shot in the first 3 seconds, followed by a sudden reverse speed-ramp to set up the premise."
            "Gaming & Let's Play Highlight" -> "Open with the absolute highest-energy reaction or clutch moment from the gameplay session. No intro, straight into the action."
            "Tech Review & Product Showcase" -> "Lead with an ultra-sharp macro B-roll of the product's sleekest angle while stating the single most controversial or surprising spec."
            "Lifestyle Vlog & Daily Beat" -> "Show a fast-paced 5-second cinematic montage of the highlight moments of the day set to an upbeat lo-fi track."
            "Interactive Talk & Podcast Episode" -> "State the most provocative or intriguing question asked during the discussion, cutting off just before the guest answers."
            "High-Velocity Vertical Short" -> "Hook viewer in 0.8s with immediate visual motion, bold yellow on-screen captions, and an unanswered curiosity loop."
            else -> "Deliver a high-impact direct statement explaining the precise value or payoff the viewer will get by watching until the end."
        }
        
        val visualDirection = when (category) {
            "VFX & Cinematic Action" -> "High-contrast neon color grading, dynamic camera tracking, and multiple render layers with glowing particle overlays."
            "Gaming & Let's Play Highlight" -> "Picture-in-picture facecam overlay with subtle green-screen keying, floating twitch chat graphics, and zoomed-in meme reaction inserts."
            "Tech Review & Product Showcase" -> "Slow sliding slider shots, manual focus pulls between product textures, and clean minimalist infographic labels pointing to specs."
            "Lifestyle Vlog & Daily Beat" -> "Warm natural lighting, handheld organic camera motion, occasional vintage film grain textures, and soft color palettes."
            "Interactive Talk & Podcast Episode" -> "Multi-camera setups (Wide master, Host close-up, Guest close-up) with automatic audio-threshold camera switching."
            "High-Velocity Vertical Short" -> "9:16 vertical crop, center-weighted composition, fast punch-ins on key syllables, and dynamic emoji callouts."
            else -> "Modern studio lighting (three-point setup) with contrasting rim-lights and occasional text call-out cards appearing on screen."
        }

        """
            ## 🎬 PRODUCTION BRIEF: $sanitizedTitle
            
            ### 📌 SUMMARY & CONCEPT STATEMENT
            Compiled locally via Creator Co-Op Deterministic Engine.
            **Category:** $category
            **Core Concept:** $sanitizedTitle
            **Draft Notes:** $sanitizedContent
            
            ### 🎯 AUDIENCE & RETENTION STRATEGY
            - **Target Audience:** Video creators, media enthusiasts, and modern audiences.
            - **Retention Trigger:** $retentionHook
            
            ### 📝 NARRATIVE STRUCTURE & BEATS
            - **Hook (0:00 - 0:30):** High-impact hook to lock in viewer retention immediately.
            - **The Setup (0:30 - 2:00):** Establish the primary narrative, introduce characters/topics, and present the main challenge or question.
            - **Core Payoffs / Climax:** Deliver the key payoff of the video clearly, ensuring high satisfaction and maximum shareability.
            - **Outro & CTA (End):** Swift call-to-action to subscribe, join the Creator Co-Op community, and watch the next related episode.
            
            ### 🎨 AUDIO-VISUAL CUES & VFX
            - **Visual Direction:** $visualDirection
            - **SFX & Music:** Structured ambient layers with subtle dynamic swells during transitions and high-tempo beats during climax moments.
            
            ---
            *Deterministic Local Synthesis: Zero Cloud Latency • $0 Operational Cost*
        """.trimIndent()
    }

    /**
     * Generates standard legal-grade Co-Op Contract Clauses based on project parameters.
     */
    fun generateContractClauses(
        workspaceName: String,
        leadCreator: String,
        revSharePercent: Int = 50
    ): List<ContractClause> {
        val partnerShare = 100 - revSharePercent
        return listOf(
            ContractClause(
                clauseNumber = "I",
                title = "Revenue Split & Escrow Distribution",
                body = "All net earnings generated by '$workspaceName' shall be split $revSharePercent% to $leadCreator and $partnerShare% to active co-op contributors. Payouts are reconciled on the 1st of each calendar month via cryptographic smart escrow.",
                category = "FINANCIAL"
            ),
            ContractClause(
                clauseNumber = "II",
                title = "Intellectual Property & Licensing Rights",
                body = "All master video footage, motion project files, and raw audio stems produced under '$workspaceName' are held under a perpetual shared creative co-op license for participating members.",
                category = "INTELLECTUAL_PROPERTY"
            ),
            ContractClause(
                clauseNumber = "III",
                title = "Milestone Deliverables & Quality SLA",
                body = "Contributors agree to deliver review-ready drafts within 72 hours of task assignment. Revisions shall not exceed two rounds unless structural scope adjustments are mutually ratified in writing.",
                category = "OPERATIONS"
            ),
            ContractClause(
                clauseNumber = "IV",
                title = "Dispute Mediation & Governance",
                body = "Any dispute regarding attribution, revenue splits, or termination shall be submitted to the Creator Co-Op Community Mediation Council before any external legal action is initiated.",
                category = "GOVERNANCE"
            )
        )
    }

    /**
     * Generates structured production tasks for a given video format.
     */
    fun generateTaskBreakdown(format: String): List<ProductionTaskTemplate> {
        val prefix = when (format) {
            "YouTube Long-Form Video" -> "YT Long"
            "Short-Form Reel/TikTok" -> "Shorts"
            "Podcast Episode" -> "Podcast"
            "Commercial Spot" -> "Commercial"
            else -> "Video"
        }

        return listOf(
            ProductionTaskTemplate(
                title = "$prefix: Script Drafting & Pacing",
                description = "Detail narrative hook, structural beats, visual pacing cues, and call-to-actions.",
                columnCategory = "IDEAS",
                estimatedHours = 4
            ),
            ProductionTaskTemplate(
                title = "$prefix: Asset Sourcing & Motion Graphics",
                description = "Gather appropriate motion vectors, Lottie animations, 3D assets, and high-retention audio stems.",
                columnCategory = "RESEARCH",
                estimatedHours = 3
            ),
            ProductionTaskTemplate(
                title = "$prefix: A-Roll Editing & Multi-Cam Sync",
                description = "Stitch principal host footage, align multi-cam captures, apply audio de-noising, and purge gaps.",
                columnCategory = "EDIT",
                estimatedHours = 6
            ),
            ProductionTaskTemplate(
                title = "$prefix: VFX Compositing & Motion Titles",
                description = "Incorporate customized text overlays, fluid transitions, motion tracked highlights, and subtitle frames.",
                columnCategory = "REVIEW",
                estimatedHours = 5
            ),
            ProductionTaskTemplate(
                title = "$prefix: Compliance & Publishing Handshake",
                description = "Conduct sound loudness diagnostics, render codec checks, verify contract rules, and lock metadata splits.",
                columnCategory = "PUBLISH",
                estimatedHours = 2
            )
        )
    }

    /**
     * Deterministically drafts a tailored creator pitch proposal for open roles.
     */
    fun generatePitchProposal(
        applicantName: String,
        roleTitle: String,
        workspaceName: String,
        specialty: String,
        yearsExperience: Int
    ): String {
        return """
            Hi $workspaceName team,
            
            I'm $applicantName, and I'd love to collaborate on $workspaceName as your $roleTitle.
            
            With over $yearsExperience years of hands-on experience specializing in $specialty, I streamline video post-production workflows, maintain tight turnaround times, and deliver retention-focused creative assets.
            
            Key strengths I bring:
            • High-retention pacing and motion graphics tailored for modern audiences
            • Seamless project handoffs and clean project file organization
            • Alignment with Creator Co-Op revenue-share agreements and milestone timelines
            
            Looking forward to discussing how we can create high-impact videos together!
            
            Best regards,
            $applicantName
        """.trimIndent()
    }
}
