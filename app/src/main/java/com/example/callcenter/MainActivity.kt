package com.example.callcenter

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val permissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.POST_NOTIFICATIONS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { /* handle result if needed */ }

        permissionLauncher.launch(permissions)

        setContent {
            CallCenterApp()
        }
    }
}

@Composable
fun CallCenterApp() {
    val context = LocalContext.current
    val db = remember { CallLogDatabase.getDatabase(context) }
    val dao = db.callLogDao()
    val scope = rememberCoroutineScope()

    var missed by remember { mutableStateOf(0) }
    var received by remember { mutableStateOf(0) }
    var lastDelay by remember { mutableStateOf(0L) }
    var isOnline by remember { mutableStateOf(Utils.isNetworkAvailable(context)) }

    LaunchedEffect(Unit) {
        scope.launch {
            missed = dao.getMissedCount()
            received = dao.getReceivedCount()
            lastDelay = dao.getLastReceivedDelay()?.toLong() ?: 0L
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text("Missed Calls: $missed", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Received Calls: $received", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Last Receive Delay: $lastDelay sec", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text("Status: ${if (isOnline) "Online" else "Offline"}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
