package com.example.minicompanion

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class DataCommunicationBridge {
  val events = MutableSharedFlow<Event>()
  val associatedDevice = MutableStateFlow<AssociatedDevice?>(null)
}