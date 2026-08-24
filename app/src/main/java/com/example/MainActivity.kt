package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.ui.screens.*
import com.example.navigation.AppNavHost
import com.example.data.supabase.SupabaseConfig
import com.example.ui.theme.*
import com.example.ui.viewmodels.*
import io.github.jan.supabase.auth.auth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch




class MainActivity : ComponentActivity() {

    private val globalViewModel: GlobalViewModel by viewModels {
        AppViewModelFactory(applicationContext as CreatorCoopApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        intent?.data?.let { uri ->
            globalViewModel.handleDeepLink(uri)
        }

        setContent {
            val themeMode by globalViewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                AppNavHost(globalViewModel = globalViewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            globalViewModel.handleDeepLink(uri)
        }
    }
}
