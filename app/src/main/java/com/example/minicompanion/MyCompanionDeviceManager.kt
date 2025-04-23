package com.example.minicompanion

import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothLeDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.concurrent.Executor
import java.util.regex.Pattern

object MyCompanionDeviceManager {

  private val deviceFilter: BluetoothLeDeviceFilter = BluetoothLeDeviceFilter.Builder()
    .setNamePattern(Pattern.compile(""))
    .build()

  private val pairingRequest: AssociationRequest = AssociationRequest.Builder()
    .addDeviceFilter(deviceFilter)
    .setSingleDevice(true)
    .build()

  fun associate(
    deviceManager: CompanionDeviceManager,
    onAssociationRequested: (intentSender: IntentSender) -> Unit,
    onAssociationCreated: (associationInfo: AssociationInfo) -> Unit
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      deviceManager.associateNew(
        onAssociationRequested = { onAssociationRequested(it)},
        onAssociationCreated = { onAssociationCreated(it) }
      )
    } else {
      deviceManager.associateOld(
        onAssociationRequested = { onAssociationRequested(it)},
        onAssociationCreated = { onAssociationCreated(it) }
      )
    }
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun CompanionDeviceManager.associateNew(
    onAssociationRequested: (intentSender: IntentSender) -> Unit,
    onAssociationCreated: (associationInfo: AssociationInfo) -> Unit
  ) {
    val executor = Executor { it.run() }
    associate(
      pairingRequest,
      executor,
      object : CompanionDeviceManager.Callback() {
        override fun onAssociationPending(intentSender: IntentSender) {
          onAssociationRequested(intentSender)
        }

        override fun onAssociationCreated(associationInfo: AssociationInfo) {
          println("COMPANION_TEST_LOG: Association created! associationInfo: $associationInfo")
          onAssociationCreated(associationInfo)
        }

        override fun onFailure(error: CharSequence?) {
          println("COMPANION_TEST_LOG: Association error! error: $error")
        }
      }
    )
  }

  private fun CompanionDeviceManager.associateOld(
    onAssociationRequested: (intentSender: IntentSender) -> Unit,
    onAssociationCreated: (associationInfo: AssociationInfo) -> Unit
  ) {
    associate(
      pairingRequest,
      object : CompanionDeviceManager.Callback() {
        override fun onDeviceFound(intentSender: IntentSender) {
          println("COMPANION_TEST_LOG: Device found: $intentSender")
          super.onDeviceFound(intentSender)
          onAssociationRequested(intentSender)
        }

        override fun onAssociationCreated(associationInfo: AssociationInfo) {
          super.onAssociationCreated(associationInfo)
          println("COMPANION_TEST_LOG: Association created! associationInfo: $associationInfo")
          onAssociationCreated(associationInfo)
        }

        override fun onFailure(error: CharSequence?) {
          println("COMPANION_TEST_LOG: Association error! error: $error")
        }
      },
      null
    )
  }

  fun disassociate(
    deviceManager: CompanionDeviceManager,
    associatedDevice: AssociatedDevice
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      deviceManager.disassociate(associatedDevice.associationId!!)
    } else {
      deviceManager.disassociate(associatedDevice.address)
    }
  }
}