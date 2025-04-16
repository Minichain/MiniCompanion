package com.example.minicompanion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.IBinder
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.util.UUID
import java.util.concurrent.Executor
import java.util.regex.Pattern

class MainService : Service() {

  companion object {
    const val CHANNEL_ID = "MyForegroundServiceChannel"
    const val CHANNEL_NAME = "Channel name"
    const val NOTIFICATION_ID = 1
  }

  override fun onCreate() {
    super.onCreate()
    println("COMPANION_TEST_LOG: MainService created!")
    val deviceManager: CompanionDeviceManager = getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    MyCompanionDeviceManager.associate(deviceManager)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    println("COMPANION_TEST_LOG: MainService onStartCommand. Start foreground!")
    createNotificationChannel()
    startForeground(NOTIFICATION_ID, getNotification())
    return super.onStartCommand(intent, flags, startId)
  }

  private fun getNotification() =
    NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_launcher_background)
      .setContentTitle("Foreground Service")
      .setContentText("Foreground service is running")
      .build()

  private fun createNotificationChannel() {
    val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
    val notificationManager = getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(notificationChannel)
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }
}