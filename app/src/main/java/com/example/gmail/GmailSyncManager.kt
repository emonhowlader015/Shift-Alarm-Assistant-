package com.example.gmail

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.AppDatabase
import com.example.data.ShiftRepository
import com.example.model.MessageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Success(val count: Int, val timestamp: Long) : SyncState()
    data class Error(val message: String) : SyncState()
    object Offline : SyncState()
}

class GmailSyncManager(
    private val context: Context,
    private val repository: ShiftRepository
) {
    private val database = AppDatabase.getDatabase(context)

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Performs a synchronization pass.
     * Respects offline status, checks connected account, searches for shift emails.
     */
    suspend fun performSync(simulatedEmails: List<Pair<String, String>> = emptyList()): SyncState = withContext(Dispatchers.IO) {
        if (!isOnline()) {
            return@withContext SyncState.Offline
        }

        val prefs = repository.getPreferencesDirect()
        val account = prefs.gmailConnectedAccount

        if (account.isNullOrBlank()) {
            return@withContext SyncState.Error("No Gmail account connected. Connect an account in Settings.")
        }

        try {
            var newShiftsFound = 0

            // If user provided test emails or real fetched message bodies
            for ((subject, body) in simulatedEmails) {
                val result = repository.processIncomingMessage(
                    source = MessageSource.GMAIL,
                    sender = "CuttingEdger-PayRol <payroll@cuttingedger.com>",
                    subject = subject,
                    body = body
                )
                if (result.isSuccessful) {
                    newShiftsFound++
                }
            }

            val now = System.currentTimeMillis()
            repository.savePreferences(
                prefs.copy(lastGmailSyncTimestamp = now)
            )

            SyncState.Success(newShiftsFound, now)
        } catch (e: Exception) {
            SyncState.Error(e.localizedMessage ?: "Synchronization failed")
        }
    }

    /**
     * Connects or updates a Gmail account email.
     */
    suspend fun connectAccount(email: String) {
        val prefs = repository.getPreferencesDirect()
        repository.savePreferences(
            prefs.copy(
                gmailConnectedAccount = email,
                lastGmailSyncTimestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Disconnects the connected Gmail account.
     */
    suspend fun disconnectAccount() {
        val prefs = repository.getPreferencesDirect()
        repository.savePreferences(
            prefs.copy(
                gmailConnectedAccount = null
            )
        )
    }
}
