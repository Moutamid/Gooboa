package com.app.gobooa.activities

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.app.gobooa.activities.utils.Constants
import com.app.gobooa.activities.utils.DeviceModel
import com.fxn.stash.Stash
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.Printooth.hasPairedPrinter
import com.mazenrashed.printooth.Printooth.printer
import com.mazenrashed.printooth.R
import com.mazenrashed.printooth.data.DiscoveryCallback
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.RawPrintable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter.Companion.ALIGNMENT_CENTER
import com.mazenrashed.printooth.data.printer.DefaultPrinter.Companion.ALIGNMENT_LEFT
import com.mazenrashed.printooth.data.printer.DefaultPrinter.Companion.ALIGNMENT_RIGHT
import com.mazenrashed.printooth.data.printer.DefaultPrinter.Companion.EMPHASIZED_MODE_BOLD
import com.mazenrashed.printooth.data.printer.DefaultPrinter.Companion.FONT_SIZE_LARGE
import com.mazenrashed.printooth.data.printer.DefaultPrinter.Companion.FONT_SIZE_NORMAL
import com.mazenrashed.printooth.data.printer.DefaultPrinter.Companion.LINE_SPACING_30
import com.mazenrashed.printooth.data.printer.DefaultPrinter.Companion.LINE_SPACING_60
import com.mazenrashed.printooth.ui.ScanningActivity
import com.mazenrashed.printooth.utilities.Bluetooth
import kotlinx.android.synthetic.main.activity_printer_connect.printers
import kotlinx.android.synthetic.main.activity_printer_connect.refreshLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrinterConnectActivity : AppCompatActivity() {
    private lateinit var bluetooth: Bluetooth
    private var devices = ArrayList<BluetoothDevice>()
    private lateinit var adapter: BluetoothDevicesAdapter

    var selectedDate = ""

    public val BLE_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    public val ANDROID_12_BLE_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.app.gobooa.R.layout.activity_printer_connect)
        val cDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        selectedDate = cDate
        findViewById<TextView>(com.app.gobooa.R.id.tvDate).setText("Today’s date $cDate")
        findViewById<ImageView>(com.app.gobooa.R.id.imgBack).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@PrinterConnectActivity, MainActivity::class.java))
            finishAffinity()
        })
        findViewById<Button>(com.app.gobooa.R.id.btn_accept).setOnClickListener(View.OnClickListener {
            requestBlePermissions(
                this@PrinterConnectActivity,
                1
            )
        })
        findViewById<Button>(com.app.gobooa.R.id.deny).setOnClickListener(View.OnClickListener { onBackPressed() })
        findViewById<Button>(com.app.gobooa.R.id.add_printer).setOnClickListener(View.OnClickListener {
            if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
                val intent = Intent(this@PrinterConnectActivity, SelectPrintScreen::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@PrinterConnectActivity,
                    "Please Turn On Bluetooth to add printer",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        bluetooth = Bluetooth(this)
        adapter = BluetoothDevicesAdapter(this)

        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            findViewById<CardView>(com.app.gobooa.R.id.permission_bluetooth).visibility =
                View.VISIBLE;
        } else {
            setup()
            findViewById<CardView>(com.app.gobooa.R.id.permission_bluetooth).visibility =
                View.GONE;

        }
    }

    override fun onResume() {
        super.onResume()
        bluetooth = Bluetooth(this)
        adapter = BluetoothDevicesAdapter(this)
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled) {
            findViewById<CardView>(com.app.gobooa.R.id.permission_bluetooth).visibility =
                View.VISIBLE;
        } else {
            setup()
            findViewById<CardView>(com.app.gobooa.R.id.permission_bluetooth).visibility =
                View.GONE;

        }
    }

    private fun setup() {
        initViews()
        initListeners()
        initDeviceCallback()
        runWithPermissions(Permission.ACCESS_FINE_LOCATION) {
            bluetooth.onStart()
            if (!bluetooth.isEnabled)
                bluetooth.enable()
            Handler().postDelayed({
                bluetooth.startScanning()
            }, 1000)
        }
    }

    private fun initDeviceCallback() {
        bluetooth.setDiscoveryCallback(object : DiscoveryCallback {
            override fun onDiscoveryStarted() {
                devices.clear()
                val resturantModelArrayList: java.util.ArrayList<String> =
                    Stash.getArrayList<String>(
                        Constants.LIST,
                        DeviceModel::class.java
                    )

                Toast.makeText(
                    this@PrinterConnectActivity,
                    resturantModelArrayList.size,
                    Toast.LENGTH_SHORT
                ).show()

                devices.addAll(bluetooth.pairedDevices)
                adapter.notifyDataSetChanged()
            }

            override fun onDiscoveryFinished() {
//                toolbar.title = if (devices.isNotEmpty()) "Select a Printer" else "No devices"
//                refreshLayout.isRefreshing = false
            }

            override fun onDeviceFound(device: BluetoothDevice) {
//                if (!devices.contains(device)) {
//                    devices.add(device)
//                    adapter.notifyDataSetChanged()
//                }
            }

            override fun onDevicePaired(device: BluetoothDevice) {
//                Printooth.setPrinter(device.name, device.address)
//                Toast.makeText(this@PrinterConnectActivity, "Device Paired", Toast.LENGTH_SHORT).show()
//                adapter.notifyDataSetChanged()
//                setResult(Activity.RESULT_OK)
//                this@PrinterConnectActivity.finish()
            }

            override fun onDeviceUnpaired(device: BluetoothDevice) {
//                Toast.makeText(this@PrinterConnectActivity, "Device unpaired", Toast.LENGTH_SHORT).show()
//                val pairedPrinter = Printooth.getPairedPrinter()
//                if (pairedPrinter != null && pairedPrinter.address == device.address)
//                    Printooth.removeCurrentPrinter()
//                devices.remove(device)
//                adapter.notifyDataSetChanged()
//                bluetooth.startScanning()
            }

            override fun onError(message: String) {
                Toast.makeText(
                    this@PrinterConnectActivity,
                    "Error while pairing",
                    Toast.LENGTH_SHORT
                ).show()
                adapter.notifyDataSetChanged()
            }
        })
    }

    private fun initListeners() {
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = false
            bluetooth.startScanning()
        }
        printers.setOnItemClickListener { _, _, i, _ ->
            Toast.makeText(this@PrinterConnectActivity, "Device Paired", Toast.LENGTH_SHORT).show()
            val device = devices[i]
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                Printooth.setPrinter(device.name, device.address)
                setResult(Activity.RESULT_OK)
                this@PrinterConnectActivity.finish()
            } else if (device.bondState == BluetoothDevice.BOND_NONE)
                bluetooth.pair(devices[i])
            adapter.notifyDataSetChanged()
        }
    }

    private fun initViews() {
        printers.adapter = adapter
    }

    override fun onStart() {
        super.onStart()


    }

    override fun onStop() {
        super.onStop()
    }

    companion object {
        const val SCANNING_FOR_PRINTER = 115
    }

    inner class BluetoothDevicesAdapter(context: Context) :
        ArrayAdapter<BluetoothDevice>(context, android.R.layout.simple_list_item_1) {
        override fun getCount(): Int {
            return devices.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return LayoutInflater.from(context)
                .inflate(R.layout.bluetooth_device_row, parent, false).apply {

                  findViewById<TextView>(R.id.name).text = devices[position].name
                    findViewById<TextView>(R.id.pairStatus).visibility =
                        if (devices[position].bondState != BluetoothDevice.BOND_NONE) View.VISIBLE else View.INVISIBLE
                    findViewById<TextView>(R.id.pairStatus).text =
                        when (devices[position].bondState) {
                            BluetoothDevice.BOND_BONDED -> "Paired"
                            BluetoothDevice.BOND_BONDING -> "Pairing.."
                            else -> ""
                        }
                    findViewById<ImageView>(R.id.pairedPrinter).visibility =
                        if (Printooth.getPairedPrinter()?.address == devices[position].address) View.VISIBLE else View.GONE
                }
        }
    }

    fun requestBlePermissions(activity: Activity?, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ActivityCompat.requestPermissions(
            activity!!, ANDROID_12_BLE_PERMISSIONS, requestCode
        ) else ActivityCompat.requestPermissions(
            activity!!, BLE_PERMISSIONS, requestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                bluetooth = Bluetooth(this)
                setup()
                findViewById<CardView>(com.app.gobooa.R.id.permission_bluetooth).visibility =
                    View.GONE;
            }

        }
    }

}
