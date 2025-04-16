package com.example.minicompanion

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

  private val _deviceAssociationRequestedEvent = MutableStateFlow<IntentSender?>(null)
  val deviceAssociationRequestedEvent = _deviceAssociationRequestedEvent

  init {
    viewModelScope.launch {
      listenToRequestDeviceAssociationEvents()
    }
  }

  private fun CoroutineScope.listenToRequestDeviceAssociationEvents() {
    App.dataCommunicationBridge.events
      .filterIsInstance<NotifyUserDeviceAssociation>()
      .map { it.intent }
      .onEach {
        println("COMPANION_TEST_LOG: _deviceAssociationRequestedEvent.emit(${it})")
        _deviceAssociationRequestedEvent.emit(it)
      }
      .launchIn(this)
  }

  fun requestDeviceAssociation() {
    viewModelScope.launch {
      App.dataCommunicationBridge.events.emit(RequestDeviceAssociation)
    }
  }
}