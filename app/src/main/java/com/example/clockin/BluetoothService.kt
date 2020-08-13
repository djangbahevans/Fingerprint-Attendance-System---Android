package com.example.clockin

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


private const val TAG = "BluetoothService"

//private val MY_UUID: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class BluetoothService(private val context: Context, private val mHandler: Handler) {

    private val mAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState = STATE_NONE
    private var mNewState = mState

    companion object {
        const val STATE_NONE = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2
    }

    /**
     * Return the current connection state.
     */
    @Synchronized
    fun getState(): Int = mState

    @Synchronized
    private fun updateUserInterfaceTitle() {
        mState = getState()
        Log.d(
            TAG,
            "updateUserInterfaceTitle() $mNewState -> $mState"
        )
        mNewState = mState
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget()
    }

    @Synchronized
    fun start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        // Update UI title
        updateUserInterfaceTitle()
    }

    @Synchronized
    fun connect(device: BluetoothDevice) {
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device)
        mConnectThread!!.start()
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    fun connected(
        socket: BluetoothSocket?,
        device: BluetoothDevice
    ) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket!!)
        mConnectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        val msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(Constants.DEVICE_NAME, device.name)
        msg.data = bundle

        mHandler.sendMessage(msg)

        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        mState = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray?) { // Create temporary object
        var r: ConnectedThread
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread!!
        }
        // Perform the write unsynchronized
        r.write(out!!)
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() { // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, "Unable to connect device")
        msg.data = bundle
        mHandler.sendMessage(msg)
        mState = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()
        // Start the service over to restart listening mode
        this.start()
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() { // Send a failure message back to the Activity
        val msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(Constants.TOAST, "Device connection was lost")
        msg.data = bundle
        mHandler.sendMessage(msg)
        mState = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()
        // Start the service over to restart listening mode
        this.start()
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(private val mmDevice: BluetoothDevice) :
        Thread() {
        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID)
        }

        init {
            mState = STATE_CONNECTING
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType: Secure")
            name = "ConnectThreadSecure"

            mAdapter?.cancelDiscovery()

            try {
                mmSocket?.connect()
            } catch (e: IOException) {
                Log.d(TAG, "IOException while connecting $e")
                try {
                    mmSocket?.close()
                } catch (e2: IOException) {
                    Log.d(TAG, "Unable to close() socket during connection failure $e2")
                }
                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothService) { mConnectThread = null }

            connected(mmSocket, mmDevice)
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket ${e.message}")
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(
        private val mmSocket: BluetoothSocket
    ) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        init {
            mState = STATE_CONNECTED
        }

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (mState == STATE_CONNECTED) {
                // Read from the InputStream
                try {
                    numBytes = mmInStream.read(mmBuffer)
                    mHandler.obtainMessage(Constants.MESSAGE_READ, numBytes, -1, mmBuffer)
                        .sendToTarget()
                } catch (e: IOException) {
                    connectionLost()
                    break
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(buffer: ByteArray) {
            try {
                mmOutStream.write(buffer)
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when send data ${e.message}")
            }
        }

        // call this from the main activity to shut down the connection
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket ${e.message}")
            }
        }
    }
}