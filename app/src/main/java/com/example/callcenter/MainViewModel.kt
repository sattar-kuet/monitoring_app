package com.example.callcenter

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var pendingCount by mutableStateOf(0)
        private set

    var isConnected by mutableStateOf(false)
        private set

    var missedCallList by mutableStateOf<List<MissedCallEntity>>(emptyList())
        private set

    var receivedCallList by mutableStateOf<List<ReceivedCallEntity>>(emptyList())
        private set

    private val db = AppDatabase.getInstance(application)
    private val repo = Repository(db, ApiClient.apiService, application)

    init {
        viewModelScope.launch {
            while (true) {
                refreshStats()
                loadCallLists()
                delay(5000)
            }
        }
    }

    private suspend fun refreshStats() {
        val missed = db.callDao().getUnsyncedMissed()
        val recv = db.callDao().getUnsyncedRecv()
        pendingCount = missed.size + recv.size

        val online = NetworkManager.isConnected(getApplication())
        isConnected = online

        if (online && pendingCount > 0) {
            repo.syncOfflineData()
            val missed2 = db.callDao().getUnsyncedMissed()
            val recv2 = db.callDao().getUnsyncedRecv()
            pendingCount = missed2.size + recv2.size
        }
    }

    private suspend fun loadCallLists() {
        missedCallList = db.callDao().getAllMissedCalls()
        receivedCallList = db.callDao().getAllReceivedCalls()
    }

    fun testMissedCall() {
        viewModelScope.launch {
            repo.saveMissedCall("01521739173", Util.currentDateTime())
            refreshStats()
            loadCallLists()
        }
    }

    fun testReceivedCall() {
        viewModelScope.launch {
            repo.saveReceivedCall(
                fromNumber = "01851364287",
                delay = 5000,
                fileBase64 = "test_base64_string",
                dateTime = Util.currentDateTime()
            )
            refreshStats()
            loadCallLists()
        }
    }
}