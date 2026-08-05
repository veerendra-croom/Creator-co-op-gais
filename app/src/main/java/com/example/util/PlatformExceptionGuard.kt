package com.example.util

import android.util.Log
import com.example.data.model.AuditLog
import com.example.data.repository.AppRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object PlatformExceptionGuard {
    private const val TAG = "PlatformExceptionGuard"
    private var repository: AppRepository? = null

    fun initialize(repo: AppRepository) {
        this.repository = repo
        
        // Setup unhandled JVM Thread Exception Handler for robust platform safety
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught Exception caught on thread ${thread.name}", throwable)
            recordException(throwable, "UNCAUGHT_JVM_THREAD_EXCEPTION")
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * A CoroutineExceptionHandler designed to catch exceptions in asynchronous scopes
     * and log them directly to the audit logging table.
     */
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine Exception trapped", throwable)
        recordException(throwable, "COROUTINE_ASYNC_EXCEPTION")
    }

    fun logException(throwable: Throwable, actionType: String = "MANUAL_LOGGED_EXCEPTION") {
        recordException(throwable, actionType)
    }

    private fun recordException(throwable: Throwable, actionType: String) {
        val repo = repository ?: return
        val message = throwable.message ?: throwable.toString()
        val stackTraceStr = throwable.stackTrace.take(8).joinToString("\n") { 
            "at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
        }
        
        // Run asynchronously in IO thread to avoid blockages
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val log = AuditLog(
                    id = UUID.randomUUID().toString(),
                    adminId = "system_guard",
                    adminName = "Automated Exception Guard",
                    actionTaken = actionType,
                    targetType = "CRITICAL_EXCEPTION",
                    targetId = "ex_" + UUID.randomUUID().toString().take(6),
                    reason = "Type: ${throwable.javaClass.simpleName}\nMsg: $message\nStack:\n$stackTraceStr",
                    createdAt = System.currentTimeMillis()
                )
                repo.insertAuditLog(log)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist exception log: ${e.message}", e)
            }
        }
    }
}
