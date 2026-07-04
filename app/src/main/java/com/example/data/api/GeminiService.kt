package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    // Configure client with 60s timeouts as requested by gemini-api skill instructions
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Uses gemini-3.5-flash to summarize and format a rough personal draft 
     * into a production-ready creator brief.
     */
    suspend fun summarizeDraftToBrief(title: String, rawContent: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is missing or is placeholder!")
            return@withContext "Error: Gemini API key is not configured in Secrets panel or is invalid."
        }

        val prompt = """
            You are an expert producer for digital media creators (YouTube, Instagram, TikTok).
            Your job is to take a raw, messy, or short concept note/draft and rewrite it into a highly professional, beautifully formatted, comprehensive Production-Ready Brief.
            
            Original Draft Title: $title
            Original Draft Notes:
            $rawContent
            
            Please organize the output into the following sections using clean markdown (bolding, lists, clear spacing):
            
            ## 🎬 PRODUCTION-READY CREATOR BRIEF: $title
            
            ### 📌 SUMMARY & CONCEPT STATEMENT
            [Write a highly engaging, concise overview of the video's core premise and value proposition.]
            
            ### 🎯 AUDIENCE & RETENTION STRATEGY
            - **Target Audience:** [Who is this video for?]
            - **Retention Trigger:** [What is the core hook to prevent drop-off in the first 10 seconds?]
            
            ### 📝 NARRATIVE STRUCTURE & BEATS
            - **Hook (0:00 - 0:30):** [Specific opening sequence, dialog, or visual action.]
            - **The Setup:** [Establish the problem or adventure.]
            - **Core Payoffs / Climax:** [The ultimate value, answer, or visual reveal.]
            
            ### 🎨 AUDIO-VISUAL CUES & VFX
            - **Visual Direction:** [Thumbnail ideas, camera angles, overlays, b-roll recommendations.]
            - **SFX & Music:** [Pacing, sound effects, soundtrack styles.]
            
            Keep the tone exciting, structured, and immediately actionable for a creative team (editors, writers, artists).
        """.trimIndent()

        try {
            // Build direct REST API request payload
            // Model: gemini-3.5-flash (Standard for text/summarization tasks)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = jsonPayload.toString().toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorMsg = response.body?.string() ?: "Unknown error"
                    Log.e(TAG, "Gemini API failure: code=${response.code} body=$errorMsg")
                    return@withContext "Error from Gemini API (${response.code}): $errorMsg"
                }

                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response body from Gemini."
                val responseJson = JSONObject(responseBody)
                
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext "Error: No candidates returned in response."
                }
                
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                if (content == null) {
                    return@withContext "Error: Content missing in candidate response."
                }
                
                val parts = content.optJSONArray("parts")
                if (parts == null || parts.length() == 0) {
                    return@withContext "Error: Parts list is empty in candidate response."
                }
                
                val text = parts.getJSONObject(0).optString("text")
                if (text.isNullOrBlank()) {
                    return@withContext "Error: No text extracted from response."
                }

                return@withContext text
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API request: ${e.message}", e)
            return@withContext "Error: Failed to process request due to an network exception: ${e.localizedMessage}"
        }
    }
}
