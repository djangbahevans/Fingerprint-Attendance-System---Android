package com.example.clockin

object Constants {
    // Message types sent from the BluetoothChatService Handler
    const val MESSAGE_STATE_CHANGE = 1
    const val MESSAGE_READ = 2
    const val MESSAGE_WRITE = 3
    const val MESSAGE_DEVICE_NAME = 4
    const val MESSAGE_TOAST = 5
    // Key names received from the BluetoothChatService Handler
    const val DEVICE_NAME = "device_name"
    const val TOAST = "toast"

    const val URL = "http://calm-temple-96942.herokuapp.com"
}