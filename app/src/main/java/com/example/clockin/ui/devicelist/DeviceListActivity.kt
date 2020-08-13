package com.example.clockin.ui.devicelist

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clockin.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_device_list.*

class DeviceListActivity : AppCompatActivity() {

    companion object {
        var EXTRA_DEVICE_ADDRESS = "device_address"
    }

    private val devices = ArrayList<BluetoothDevice>()
    private val mAdapter: BluetoothRecyclerAdapter by lazy { BluetoothRecyclerAdapter(devices) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Snackbar.make(
                bluetooth_devices,
                "This device does not support bluetooth",
                Snackbar.LENGTH_INDEFINITE
            ).show()
        } else {
            val viewManager = LinearLayoutManager(this)
            bluetooth_devices.apply {
                layoutManager = viewManager
                adapter = mAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        this.context,
                        DividerItemDecoration.VERTICAL
                    )
                )
            }

            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter.bondedDevices
            pairedDevices?.forEach { device ->
                devices.add(device)
            }

            var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
            filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            registerReceiver(receiver, filter)
            filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            registerReceiver(receiver, filter)

            swipe_refresh.setOnRefreshListener {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothAdapter.startDiscovery()
                } else {
                    swipe_refresh.isRefreshing = false
                    Snackbar.make(
                        swipe_refresh,
                        "Please grant location permissions",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Grant Access") {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                0
                            )
                        } else {
                            val uri = Uri.fromParts("package", this.packageName, null)
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = uri
                            }
                            startActivity(intent)
                        }
                    }.show()
                }
            }
        }
    }

    private inner class BluetoothRecyclerAdapter(private val device: List<BluetoothDevice>) :
        RecyclerView.Adapter<BluetoothRecyclerAdapter.MyViewHolder>() {

        inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {
            val nameTextView: TextView by lazy { view.findViewById<TextView>(R.id.device_name) }
            val macNumberTextView: TextView by lazy { view.findViewById<TextView>(R.id.device_mac_number) }
            var device: BluetoothDevice? = null

            init {
                view.isClickable
                view.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                AlertDialog.Builder(this@DeviceListActivity).setTitle("Are you sure")
                    .setMessage("Do you want to connect to ${device?.name}")
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        val intent = Intent()
                        intent.putExtra(EXTRA_DEVICE_ADDRESS, device?.address)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }.setNegativeButton(android.R.string.no, null).show()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.bluetooth_device, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int = device.size

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.macNumberTextView.text = device[position].address
            holder.nameTextView.text = device[position].name
            holder.device = device[position]
        }

    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action!!) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    Toast.makeText(
                        context,
                        "Device discovered with name ${device.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (device.bondState != BluetoothDevice.BOND_BONDED && !devices.contains(device)) {
                        devices.add(device)
                        mAdapter.notifyItemInserted(devices.size - 1)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
//                    Snackbar.make(bluetooth_devices, "Discovery started", Snackbar.LENGTH_LONG).show()
                    Toast.makeText(context, "Discovery started", Toast.LENGTH_SHORT).show()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
//                    Snackbar.make(bluetooth_devices, "Discovery finished", Snackbar.LENGTH_LONG).show()
//                    Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show()
                    swipe_refresh.isRefreshing = false
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}
