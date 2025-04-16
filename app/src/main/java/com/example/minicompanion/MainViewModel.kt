package com.example.minicompanion

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
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
      .onEach {
        println("COMPANION_TEST_LOG: _deviceAssociationRequestedEvent.emit(${it.intent})")
        _deviceAssociationRequestedEvent.emit(it.intent)
      }
      .launchIn(this)
  }

  fun requestDeviceAssociation() {
    viewModelScope.launch {
      App.dataCommunicationBridge.events.emit(RequestDeviceAssociation)
    }
  }
}