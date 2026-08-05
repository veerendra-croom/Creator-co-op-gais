package com.example.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object GeminiService {
    private const val TAG = "GeminiService"
    
    /**
     * Synthesizes a rough personal draft into a professional, production-ready
     * creator brief entirely locally, ensuring zero-cost, high reliability, and zero latency issues.
     */
    suspend fun summarizeDraftToBrief(title: String, rawContent: String): String = withContext(Dispatchers.IO) {
        // Simulate a brief local synthesis processing delay for natural UX
        delay(1500)
        
        val sanitizedTitle = title.trim().ifEmpty { "Untitled Project" }
        val sanitizedContent = rawContent.trim().ifEmpty { "No additional draft notes provided." }
        
        // Detect topic keywords to generate a tailored brief
        val lowercaseTitle = sanitizedTitle.lowercase()
        val lowercaseContent = sanitizedContent.lowercase()
        
        val category = when {
            lowercaseTitle.contains("vfx") || lowercaseContent.contains("vfx") || lowercaseTitle.contains("cgi") -> "VFX & Cinematic Action"
            lowercaseTitle.contains("game") || lowercaseTitle.contains("gaming") || lowercaseContent.contains("game") -> "Gaming & Let's Play Highlight"
            lowercaseTitle.contains("tech") || lowercaseTitle.contains("code") || lowercaseTitle.contains("review") -> "Tech Review & Product Showcase"
            lowercaseTitle.contains("vlog") || lowercaseTitle.contains("day") || lowercaseContent.contains("vlog") -> "Lifestyle Vlog & Daily Beat"
            lowercaseTitle.contains("podcast") || lowercaseTitle.contains("talk") || lowercaseContent.contains("interview") -> "Interactive Talk & Podcast Episode"
            else -> "Digital Content Creator Standard"
        }
        
        val retentionHook = when (category) {
            "VFX & Cinematic Action" -> "Start with an explosive cold open showing the climax VFX shot in the first 3 seconds, followed by a sudden reverse speed-ramp to set up the premise."
            "Gaming & Let's Play Highlight" -> "Open with the absolute highest-energy reaction or clutch moment from the gameplay session. No intro, straight into the action."
            "Tech Review & Product Showcase" -> "Lead with an ultra-sharp macro B-roll of the product's sleekest angle while stating the single most controversial or surprising spec."
            "Lifestyle Vlog & Daily Beat" -> "Show a fast-paced 5-second cinematic montage of the highlight moments of the day set to an upbeat lo-fi track."
            "Interactive Talk & Podcast Episode" -> "State the most provocative or intriguing question asked during the discussion, cutting off just before the guest answers."
            else -> "Deliver a high-impact direct statement explaining the precise value or payoff the viewer will get by watching until the end."
        }
        
        val visualDirection = when (category) {
            "VFX & Cinematic Action" -> "High-contrast neon color grading, dynamic camera tracking, and multiple render layers with glowing particle overlays."
            "Gaming & Let's Play Highlight" -> "Picture-in-picture facecam overlay with subtle green-screen keying, floating twitch chat graphics, and zoomed-in meme reaction inserts."
            "Tech Review & Product Showcase" -> "Slow sliding slider shots, manual focus pulls between product textures, and clean minimalist infographic labels pointing to specs."
            "Lifestyle Vlog & Daily Beat" -> "Warm natural lighting, handheld organic camera motion, occasional vintage film grain textures, and soft color palettes."
            "Interactive Talk & Podcast Episode" -> "Multi-camera setups (Wide master, Host close-up, Guest close-up) with automatic audio-threshold camera switching."
            else -> "Modern studio lighting (three-point setup) with contrasting rim-lights and occasional text call-out cards appearing on screen."
        }

        val structure = """
            ## 🎬 CO-OP LOCAL BRIEF: $sanitizedTitle
            
            ### 📌 SUMMARY & CONCEPT STATEMENT
            This project has been compiled into a high-fidelity $category brief. 
            **Core Concept:** $sanitizedTitle
            **Draft Notes Analysed:** $sanitizedContent
            
            ### 🎯 AUDIENCE & RETENTION STRATEGY
            - **Target Audience:** Digital media enthusiasts, modern creators, and target-demographic viewers.
            - **Retention Trigger:** $retentionHook
            
            ### 📝 NARRATIVE STRUCTURE & BEATS
            - **Hook (0:00 - 0:30):** High-impact hook to lock in viewer retention immediately.
            - **The Setup (0:30 - 2:00):** Establish the primary narrative, introduce characters/topics, and present the main challenge or question.
            - **Core Payoffs / Climax:** Reveal the key payoff of the video clearly, ensuring high satisfaction and maximum shareability.
            - **Outro & CTA (End):** Swift call-to-action to subscribe, join the Creator Co-Op community, and watch the next related episode.
            
            ### 🎨 AUDIO-VISUAL CUES & VFX
            - **Visual Direction:** $visualDirection
            - **SFX & Music:** Structured ambient layers with subtle dynamic swells during transitions and high-tempo beats during climax moments.
            
            ---
            *Processed successfully via Creator Co-Op Local Brief Engine. No active server fees incurred.*
        """.trimIndent()
        
        structure
    }
}
