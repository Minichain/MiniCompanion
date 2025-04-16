package com.example.minicompanion

import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.os.Build
import android.os.ParcelUuid
import java.util.concurrent.Executor
import java.util.regex.Pattern

object MyCompanionDeviceManager {

  private val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
    // Match only Bluetooth devices whose name matches the pattern.
    .setNamePattern(Pattern.compile("My device"))
    // Match only Bluetooth devices whose service UUID matches this pattern.
    .addServiceUuid(ParcelUuid(UUID(0x123abcL, -1L)), null)
    .build()

  private val pairingRequest: AssociationRequest = AssociationRequest.Builder()
    // Find only devices that match this request filter.
    .addDeviceFilter(deviceFilter)
    // Stop scanning as soon as one device matching the filter is found.
    .setSingleDevice(true)
    .build()

  fun associate(
    deviceManager: CompanionDeviceManager,
    onAssociationRequested: (intentSender: IntentSender) -> Unit
  ) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      deviceManager.associateNew(onAssociationRequested = { onAssociationRequested(it)} )
    } else {
      deviceManager.associateOld()
    }
  }

  private fun CompanionDeviceManager.associateNew(
    onAssociationRequested: (intentSender: IntentSender) -> Unit
  ) {
    val executor = Executor { it.run() }
    associate(
      pairingRequest,
      executor,
      object : CompanionDeviceManager.Callback() {
        override fun onAssociationPending(intentSender: IntentSender) {
          println("COMPANION_TEST_LOG: Association pending...")
          println("COMPANION_TEST_LOG: creatorUid: ${intentSender.creatorUid}")
          println("COMPANION_TEST_LOG: creatorPackage: ${intentSender.creatorPackage}")
          println("COMPANION_TEST_LOG: creatorUserHandle: ${intentSender.creatorUserHandle}")
          onAssociationRequested(intentSender)
        }

        override fun onAssociationCreated(associationInfo: AssociationInfo) {
          println("COMPANION_TEST_LOG: Association created! associationInfo: $associationInfo")
        }

        override fun onFailure(error: CharSequence?) {
          println("COMPANION_TEST_LOG: Association error! error: $error")
        }
      }
    )
  }

  private fun CompanionDeviceManager.associateOld() {
    associate(
      pairingRequest,
      object : CompanionDeviceManager.Callback() {
        override fun onDeviceFound(intentSender: IntentSender) {
          println("COMPANION_TEST_LOG: Device found: $intentSender")
          super.onDeviceFound(intentSender)
        }
        override fun onFailure(error: CharSequence?) {
          println("COMPANION_TEST_LOG: Association error! error: $error")
        }
      },
      null
    )
  }
}