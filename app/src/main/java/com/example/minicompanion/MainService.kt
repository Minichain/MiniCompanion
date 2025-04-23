package com.example.minicompanion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.companion.CompanionDeviceManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainService : Service() {

  companion object {
    const val CHANNEL_ID = "MyForegroundServiceChannel"
    const val CHANNEL_NAME = "Channel name"
    const val NOTIFICATION_ID = 1
  }

  private lateinit var deviceManager: CompanionDeviceManager
  private val scope = CoroutineScope(Dispatchers.Main + Job())

  override fun onCreate() {
    super.onCreate()
    println("COMPANION_TEST_LOG: MainService created!")
    deviceManager = getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    scope.launch {
      listenToEvents()
      listenToAssociations()
      runTestInBackground()
    }
  }

  private fun CoroutineScope.listenToEvents() {
    App.dataCommunicationBridge.events
      .filterIsInstance<RequestDeviceAssociation>()
      .onEach { associate() }
      .launchIn(this)

    App.dataCommunicationBridge.events
      .filterIsInstance<RequestDeviceDisassociation>()
      .onEach { disassociate() }
      .launchIn(this)
  }

  private fun associate() {
    MyCompanionDeviceManager.associate(
      deviceManager = deviceManager,
      onAssociationRequested = {
        scope.launch {
          println("COMPANION_TEST_LOG: Association pending...")
          println("COMPANION_TEST_LOG: creatorUid: ${it.creatorUid}")
          println("COMPANION_TEST_LOG: creatorPackage: ${it.creatorPackage}")
          println("COMPANION_TEST_LOG: creatorUserHandle: ${it.creatorUserHandle}")
          App.dataCommunicationBridge.events.emit(NotifyUserDeviceAssociation(it))
        }
      },
      onAssociationCreated = {

      }
    )
  }

  private fun disassociate() {
    getAssociatedDevice()?.let {
      MyCompanionDeviceManager.disassociate(deviceManager, it)
    }
  }

  private fun CoroutineScope.listenToAssociations() {
    launch {
      while (true) {
        App.dataCommunicationBridge.associatedDevice.emit(getAssociatedDevice())
        delay(1000)
      }
    }
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

  private fun CoroutineScope.runTestInBackground() {
    launch {
      while (true) {
        println("COMPANION_TEST_LOG: Test running in background. Timestamp: ${System.currentTimeMillis()}")
        delay(1000)
      }
    }
  }

  private fun getAssociatedDevice(): AssociatedDevice? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getAssociatedDeviceNew()
    else getAssociatedDeviceLegacy()

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun getAssociatedDeviceNew(): AssociatedDevice? =
    deviceManager.myAssociations.firstOrNull()?.let { associationInfo ->
      AssociatedDevice(
        associationId = associationInfo.id,
        displayName = associationInfo.displayName.toString(),
        address = associationInfo.deviceMacAddress.toString()
      )
    }

  private fun getAssociatedDeviceLegacy(): AssociatedDevice? =
    deviceManager.associations.firstOrNull()?.let { address ->
      AssociatedDevice(
        associationId = null,
        displayName = null,
        address = address
      )
    }
}