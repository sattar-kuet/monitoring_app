package com.example.callcenter

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    // ✅ সব প্রয়োজনীয় permission
    private val permissions = arrayOf(
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val allGranted = result.values.all { it }
            if (allGranted) {
                // Permission granted → Safe ভাবে CallMonitorService চালাও
                val serviceIntent = Intent(this, CallMonitorService::class.java)
                startForegroundService(serviceIntent)
            } else {
                // Permission deny হলে user কে alert দাও
                println("⚠ All required permissions not granted!")
            }
        }

        // Launch permission request
        permissionLauncher.launch(permissions)

        // ✅ Compose UI সেট
        setContent {
            val vm: MainViewModel = viewModel()

            Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text("📞 Call Center Monitor")
                    Spacer(Modifier.height(8.dp))
                    Text("Pending Uploads: ${vm.pendingCount}")
                    Text("Network Connected: ${vm.isConnected}")

                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.testMissedCall() }) { Text("Add Dummy Missed Call") }
                    Button(onClick = { vm.testReceivedCall() }) { Text("Add Dummy Received Call") }

                    Spacer(Modifier.height(16.dp))
                    Text("📕 Missed Calls:", style = MaterialTheme.typography.titleMedium)
                    vm.missedCallList.forEach { call ->
                        Text("- ${call.fromNumber} at ${call.dateTime}")
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("📗 Received Calls:", style = MaterialTheme.typography.titleMedium)
                    vm.receivedCallList.forEach { call ->
                        val sec = call.delayMs / 1000
                        Text("- ${call.fromNumber} after $sec sec at ${call.dateTime}")
                    }
                }
            }
        }
    }
}