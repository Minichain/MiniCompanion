package com.example.minicompanion

import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

  private val _deviceAssociationRequestedEvent = MutableStateFlow<IntentSender?>(null)
  val deviceAssociationRequestedEvent = _deviceAssociationRequestedEvent.asSharedFlow()

  private val _associatedDevice = MutableStateFlow<AssociatedDevice?>(null)
  val associatedDevice = _associatedDevice.asSharedFlow()

  init {
    viewModelScope.launch {
      listenToEvents()
      listenToAssociatedDevice()
    }
  }

  private fun CoroutineScope.listenToEvents() {
    App.dataCommunicationBridge.events
      .filterIsInstance<NotifyUserDeviceAssociation>()
      .map { it.intent }
      .onEach { _deviceAssociationRequestedEvent.emit(it) }
      .launchIn(this)
  }

  private fun CoroutineScope.listenToAssociatedDevice() {
    App.dataCommunicationBridge.associatedDevice
      .onEach { _associatedDevice.emit(it) }
      .launchIn(this)
  }

  fun requestDeviceAssociation() {
    viewModelScope.launch {
      App.dataCommunicationBridge.events.emit(RequestDeviceAssociation)
    }
  }

  fun requestDeviceDisassociation() {
    viewModelScope.launch {
      App.dataCommunicationBridge.events.emit(RequestDeviceDisassociation)
    }
  }
}