package com.example.minicompanion

import kotlinx.coroutines.flow.MutableSharedFlow

class DataCommunicationBridge {
  val events = MutableSharedFlow<Event>()
}