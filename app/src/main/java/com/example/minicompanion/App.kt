package com.example.minicompanion

import android.app.Application

class App : Application() {
  companion object {
    val dataCommunicationBridge = DataCommunicationBridge()
  }
}
