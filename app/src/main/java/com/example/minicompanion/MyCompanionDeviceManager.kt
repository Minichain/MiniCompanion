package com.example.minicompanion

import android.companion.AssociationInfo
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.IntentSender
import android.os.Build
import android.os.ParcelUuid
import androidx.annotation.RequiresApi
import java.util.UUID
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

  fun associate(deviceManager: CompanionDeviceManager) {
    val executor: Executor =  Executor { it.run() }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      deviceManager.associate(
        pairingRequest,
        executor,
        object : CompanionDeviceManager.Callback() {
          override fun onAssociationPending(intentSender: IntentSender) {

          }

          override fun onAssociationCreated(associationInfo: AssociationInfo) {
            // An association is created.
          }

          override fun onFailure(error: CharSequence?) {

          }
        }
      )
    } else {
      deviceManager.associate(
        pairingRequest,
        object : CompanionDeviceManager.Callback() {
          override fun onDeviceFound(intentSender: IntentSender) {
            super.onDeviceFound(intentSender)
          }
          override fun onFailure(error: CharSequence?) {

          }
        },
        null
      )
    }
  }
}