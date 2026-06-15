package com.example.data.supabase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

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

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithHeaders = originalRequest.newBuilder()
            .header("apikey", supabaseKey)
            .header("Authorization", "Bearer $supabaseKey")
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation") // So writes return the representation
            .build()
        chain.proceed(requestWithHeaders)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
