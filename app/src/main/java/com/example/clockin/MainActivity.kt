package com.example.clockin

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.clockin.ui.addemployee.AddEmployeeActivity
import com.example.clockin.ui.configuretime.ConfigureTimeActivity
import com.example.clockin.ui.configurewifi.WifiActivity
import com.example.clockin.ui.deleteemployee.DeleteEmployeeActivity
import com.example.clockin.ui.devicelist.DeviceListActivity
import com.example.clockin.ui.viewattendance.ViewAttendanceActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.time.LocalDateTime

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private var mBluetoothService: BluetoothService? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mOutStringBuffer: StringBuffer? = null
    private var mConnectedDeviceName: String? = null

    private val REQUEST_CONNECT_DEVICE = 1
    private val REQUEST_SEND_MESSAGE = 2
    private val REQUEST_ENABLE_BT = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            Toast.makeText(
                this,
                "Bluetooth is not supported on this device. Program closing.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

        connection_display_text.text = "Not connected"

        add_employee_button.setOnClickListener {
            val intent = Intent(this, AddEmployeeActivity::class.java)
            startActivityForResult(intent, REQUEST_SEND_MESSAGE)
        }
        configure_time_button.setOnClickListener {
            val jsonObject = JSONObject().apply {
                put("ins", "backlog")
            }
            sendMessage(jsonObject.toString())
        }
        set_wifi_button.setOnClickListener {
            val intent = Intent(this, WifiActivity::class.java)
            startActivityForResult(intent, REQUEST_SEND_MESSAGE)
        }
        delete_employee_button.setOnClickListener {
            val intent = Intent(this, DeleteEmployeeActivity::class.java)
            startActivityForResult(intent, REQUEST_SEND_MESSAGE)
        }
        view_attendance_button.setOnClickListener {
            val intent = Intent(this, ViewAttendanceActivity::class.java)
            startActivity(intent)
        }
        connect_device_button.setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            startActivityForResult(intent, REQUEST_CONNECT_DEVICE)
        }
    }

    private fun setStatus(subTitle: CharSequence) {
        connection_display_text.text = subTitle
    }

    override fun onStart() {
        super.onStart()

        if (mBluetoothAdapter == null) return
        if (!mBluetoothAdapter!!.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_ENABLE_BT)
        } else if (mBluetoothService == null) {
            setupChat()
        }
    }

    private fun setupChat() {
        mBluetoothService = BluetoothService(this, mHandler)
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = StringBuffer()
    }

    private fun sendMessage(message: String) { // Check that we're actually connected before trying anything
        Log.d(TAG, ".sendMessage: Sending message $message")
        if (mBluetoothService?.getState() != BluetoothService.STATE_CONNECTED) {
            Snackbar.make(
                add_employee_button,
                "Device is not connected",
                Snackbar.LENGTH_SHORT
            )
                .show()
            return
        }
        // Check that there's actually something to send
        if (message.isNotEmpty()) { // Get the message bytes and tell the BluetoothChatService to write
            val send = message.toByteArray()
            mBluetoothService?.write(send)
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer!!.setLength(0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CONNECT_DEVICE ->  // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data)
                }
            REQUEST_SEND_MESSAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val message = data?.getStringExtra("message")
                    if (message != null) {
                        sendMessage(message)
                    }
                } else if (resultCode == Activity.RESULT_FIRST_USER) {
                    Snackbar.make(
                        add_employee_button,
                        "Error! There was a problem processing request",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            REQUEST_ENABLE_BT ->  // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) { // Bluetooth is now enabled, so set up a chat session
                    setupChat()
                } else { // User did not enable Bluetooth or an error occurred
                    Snackbar.make(
                        connect_device_button,
                        "Bluetooth was not enabled. Quitting",
                        Snackbar.LENGTH_LONG
                    ).show()
                    finish()
                }
        }
    }

    private fun connectDevice(data: Intent?) {
        // Get the device MAC address
        val extras = data!!.extras ?: return
        val address = extras.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
        // Get the BluetoothDevice object
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        // Attempt to connect to the device
        mBluetoothService!!.connect(device)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the bluetooth service
        mBluetoothService?.stop()
    }

    override fun onResume() {
        super.onResume()
        if (mBluetoothAdapter == null) {
            if (mBluetoothService?.getState() == BluetoothService.STATE_NONE) {
                mBluetoothService?.start()
            }
        }
    }

    private val mHandler =
        @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    Constants.MESSAGE_WRITE -> {
                        val writeBuf: ByteArray = msg.obj as ByteArray
//                         construct a string from the buffer
                        val writeMessage = String(writeBuf)
                        Snackbar.make(connect_device_button, "Message sent", Snackbar.LENGTH_LONG)
                            .show()
                    }
                    Constants.MESSAGE_READ -> {
                        val readBuf: ByteArray = msg.obj as ByteArray
                        val readMessage = String(readBuf)
                        if (readMessage.toLowerCase().startsWith("/done")) {
                            Snackbar.make(
                                connect_device_button,
                                "Instruction complete",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            Log.d(TAG, readMessage)
                        }
                    }
                    Constants.MESSAGE_DEVICE_NAME -> {
                        // save the connected device's name
                        mConnectedDeviceName = msg.data.getString(Constants.DEVICE_NAME)
                        Snackbar.make(
                            connect_device_button,
                            "Connected to $mConnectedDeviceName",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    Constants.MESSAGE_STATE_CHANGE -> {
                        when (msg.arg1) {
                            BluetoothService.STATE_CONNECTED -> {
                                setStatus("Connected to $mConnectedDeviceName")
                                val today = LocalDateTime.now()
                                val jsonObject = JSONObject().apply {
                                    put("ins", "setTime")
                                    put("year", today.year)
                                    put("month", today.monthValue)
                                    put("day", today.dayOfMonth)
                                    put("hour", today.hour)
                                    put("minute", today.minute)
                                    put("seconds", today.second)
                                }

                                sendMessage(jsonObject.toString())
                            }
                            BluetoothService.STATE_CONNECTING -> setStatus("Connecting")
                            BluetoothService.STATE_NONE -> setStatus("Not connected")
                        }
                    }
                    Constants.MESSAGE_TOAST -> {
                        Snackbar.make(
                            connect_device_button,
                            msg.data.getString(Constants.TOAST)!!,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
}
