package com.example.data.supabase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import io.github.jan.supabase.serializer.KotlinXSerializer

object SupabaseConfig {
    private const val TAG = "SupabaseConfig"

    val supabaseUrl: String by lazy {
        try {
            val url = BuildConfig.SUPABASE_URL
            if (url.isNullOrBlank() || url.startsWith("https://your-project")) {
                "https://your-project.supabase.co"
            } else {
                url
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to read SUPABASE_URL from BuildConfig", e)
            "https://your-project.supabase.co"
        }
    }

    val supabaseKey: String by lazy {
        try {
            val key = BuildConfig.SUPABASE_KEY
            if (key.isNullOrBlank() || key.startsWith("your-supabase-public")) {
                "your-supabase-public-anon-key"
            } else {
                key
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to read SUPABASE_KEY from BuildConfig", e)
            "your-supabase-public-anon-key"
        }
    }

    // Official Supabase SDK Client
    val client: SupabaseClient by lazy {
        val okHttpClient = OkHttpClient.Builder().build()

        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                encodeDefaults = true
            })
            httpEngine = OkHttp.create {
                preconfigured = okHttpClient
            }
            install(Postgrest)
            install(Auth) {
                sessionManager = object : io.github.jan.supabase.auth.SessionManager {
                    private var currentSession: io.github.jan.supabase.auth.user.UserSession? = null
                    override suspend fun saveSession(session: io.github.jan.supabase.auth.user.UserSession) {
                        currentSession = session
                    }
                    override suspend fun loadSession(): io.github.jan.supabase.auth.user.UserSession? {
                        return currentSession
                    }
                    override suspend fun deleteSession() {
                        currentSession = null
                    }
                }
                codeVerifierCache = object : io.github.jan.supabase.auth.CodeVerifierCache {
                    private var currentVerifier: String? = null
                    override suspend fun saveCodeVerifier(codeVerifier: String) {
                        currentVerifier = codeVerifier
                    }
                    override suspend fun loadCodeVerifier(): String? {
                        return currentVerifier
                    }
                    override suspend fun deleteCodeVerifier() {
                        currentVerifier = null
                    }
                }
            }
            install(Realtime)
            install(Storage)
        }
    }

    /**
     * Uploads a file to a Supabase Storage bucket.
     * @param bucket The name of the bucket (e.g., "avatars", "media").
     * @param path The destination path/filename in the bucket.
     * @param byteArray The file data.
     * @return The public URL of the uploaded file.
     */
    suspend fun uploadFile(bucket: String, path: String, byteArray: ByteArray): String? {
        return try {
            val bucketInstance = client.storage.from(bucket)
            bucketInstance.upload(path, byteArray) {
                upsert = true
            }
            client.storage.from(bucket).publicUrl(path)
        } catch (e: Exception) {
            Log.e(TAG, "Storage upload failed: ${e.message}", e)
            null
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
