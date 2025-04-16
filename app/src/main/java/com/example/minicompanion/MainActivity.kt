package com.example.minicompanion

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.minicompanion.ui.theme.MiniCompanionTheme
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {

  companion object {
    private const val SELECT_DEVICE_REQUEST_CODE = 0
  }

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
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Button(
              onClick = {
                mainViewModel.requestDeviceAssociation()
              }
            ) {
              Text("Associate")
            }
          }

          println("COMPANION_TEST_LOG: Start listening to event RequestDeviceAssociation")
          LaunchedEffect(Unit) {
            mainViewModel.deviceAssociationRequestedEvent
              .filterNotNull()
              .onEach {
                println("COMPANION_TEST_LOG: startIntentSenderForResult()")
                startIntentSenderForResult(it, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0)
              }
              .launchIn(this)
          }
        }
      }
    }
  }

  private fun startMainService() {
    println("COMPANION_TEST_LOG: start main service...")
    val intent = Intent(this, MainService::class.java)
    startForegroundService(intent)
  }
}
