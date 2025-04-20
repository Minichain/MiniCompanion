package com.example.minicompanion

import android.content.IntentSender

sealed class Event
data class NotifyUserDeviceAssociation(val intent: IntentSender): Event()
data object RequestDeviceAssociation: Event()
data object RequestDeviceDisassociation: Event()
