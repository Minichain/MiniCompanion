package com.example.minicompanion

import android.bluetooth.le.ScanResult
import android.companion.CompanionDeviceManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.minicompanion.ui.theme.MiniCompanionTheme
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {

  private val mainViewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    startMainService()

    enableEdgeToEdge()
    setContent {
      MiniCompanionTheme {
        Scaffold(
          modifier = Modifier.fillMaxSize()
        ) { innerPadding ->

          var associatedDevice by remember { mutableStateOf<AssociatedDevice?>(null) }

          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            if (associatedDevice == null) {
              Button(
                onClick = {
                  mainViewModel.requestDeviceAssociation()
                }
              ) {
                Text("Associate")
              }
            } else {
              Button(
                onClick = {
                  mainViewModel.requestDeviceDisassociation()
                }
              ) {
                Text("Disassociate")
              }
            }
            Spacer(
              modifier = Modifier.height(8.dp)
            )
            associatedDevice?.let { device ->
              Text(text = "Name: ${device.displayName}")
              Text(text = "Address: ${device.address}")
            }
          }

          println("COMPANION_TEST_LOG: Start listening to event RequestDeviceAssociation")
          LaunchedEffect(Unit) {
            mainViewModel.deviceAssociationRequestedEvent
              .filterNotNull()
              .onEach {
                println("COMPANION_TEST_LOG: startIntentSenderForResult()")
                val intentSenderRequest = IntentSenderRequest.Builder(it).build()
                associateDeviceLauncher.launch(intentSenderRequest)
              }
              .launchIn(this)
          }

          LaunchedEffect(Unit) {
            mainViewModel.associatedDevice
              .onEach { associatedDevice = it }
              .launchIn(this)
          }
        }
      }
    }
  }

  private val associateDeviceLauncher = registerForActivityResult(
    ActivityResultContracts.StartIntentSenderForResult()
  ) { result ->
    if (result.resultCode == RESULT_OK) {
      println("COMPANION_TEST_LOG: SELECT_DEVICE_REQUEST_CODE RESULT_OK")
      val intent = result.data
      val scanResult: ScanResult? = intent?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
      scanResult?.device?.let { device ->
        println("COMPANION_TEST_LOG: deviceToPair: $device")
        device.createBond()
      } ?: run {
        println("COMPANION_TEST_LOG: No device returned from association")
      }
    } else {
      println("COMPANION_TEST_LOG: Device association cancelled or failed")
    }
  }

  private fun startMainService() {
    println("COMPANION_TEST_LOG: start main service...")
    val intent = Intent(this, MainService::class.java)
    startForegroundService(intent)
  }
}
