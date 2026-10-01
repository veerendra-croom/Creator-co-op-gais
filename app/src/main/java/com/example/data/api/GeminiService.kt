package com.example.data.api

import com.example.data.engine.LocalProductionEngine

/**
 * Compatibility delegate for local brief synthesis.
 * All operations execute deterministically offline with $0 API and server costs.
 */
object GeminiService {
    /**
     * Synthesizes a rough personal draft into a professional creator brief locally.
     */
    suspend fun summarizeDraftToBrief(title: String, rawContent: String): String {
        return LocalProductionEngine.synthesizeDraftToBrief(title, rawContent)
    }
}

