package com.app.gobooa.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.app.gobooa.activities.utils.Constants
import com.app.gobooa.activities.utils.DeviceModel
import com.app.gobooa.activities.utils.PrinterConnectActivity
import com.fxn.stash.Stash
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.R
import com.mazenrashed.printooth.data.DiscoveryCallback
import com.mazenrashed.printooth.utilities.Bluetooth
import kotlinx.android.synthetic.main.activity_printer_connect.printers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SelectPrintScreen : AppCompatActivity() {
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
    var j = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.app.gobooa.R.layout.activity_select_print_screen)
        val cDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        selectedDate = cDate
        Toast.makeText(
            this@SelectPrintScreen,
            "Please wait, App is scanning for bluetooth device",
            Toast.LENGTH_SHORT
        ).show()

        findViewById<TextView>(com.app.gobooa.R.id.tvDate).setText(cDate)
        findViewById<ImageView>(com.app.gobooa.R.id.imgBack).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@SelectPrintScreen, PrinterConnectActivity::class.java))
            finish()
        })
        findViewById<ImageView>(com.app.gobooa.R.id.refresh).setOnClickListener(View.OnClickListener {
            findViewById<TextView>(com.app.gobooa.R.id.no_device).visibility = View.GONE
            findViewById<ProgressBar>(com.app.gobooa.R.id.progress_bar).visibility =
                View.VISIBLE

            bluetooth = Bluetooth(this)
            adapter = BluetoothDevicesAdapter(this)
            setup()
        })
        bluetooth = Bluetooth(this)
        adapter = BluetoothDevicesAdapter(this)
        setup()


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
//                devices.addAll(bluetooth.pairedDevices)
//                adapter.notifyDataSetChanged()
            }

            override fun onDiscoveryFinished() {
//                refreshLayout.isRefreshing = false
            }

            override fun onDeviceFound(device: BluetoothDevice) {
                val deviceModelList: java.util.ArrayList<DeviceModel> =
                    Stash.getArrayList<DeviceModel>(
                        Constants.LIST,
                        DeviceModel::class.java
                    )
                if (deviceModelList.size > 0) {
                    for (i in deviceModelList.indices) {
                        Log.d("data", deviceModelList.get(i).name + "  " + device.name)
                        if (device.name != null) {
                            if (!deviceModelList.get(i).name.contentEquals(device.name)) {
                                devices.add(device)
                            }
                        } else if (device.address != null) {
                            if (!deviceModelList.get(i).address.contentEquals(device.address)) {
                                devices.add(device)
                            }

                        }

                    }
                } else {
                    if (devices.size > 0) {
                        if (!devices.contains(device)) {
                            devices.add(device)

                        }
                    } else {
                        devices.add(device)
                    }
                }

                if (devices.size > 0) {
                    printers.visibility = View.VISIBLE;
                    findViewById<TextView>(com.app.gobooa.R.id.no_device).visibility = View.GONE
                    findViewById<ProgressBar>(com.app.gobooa.R.id.progress_bar).visibility =
                        View.GONE

                } else {
                    findViewById<TextView>(com.app.gobooa.R.id.no_device).visibility = View.VISIBLE
                    findViewById<ProgressBar>(com.app.gobooa.R.id.progress_bar).visibility =
                        View.GONE
                    printers.visibility = View.GONE;

                }
                adapter.notifyDataSetChanged()
//                }
            }

            override fun onDevicePaired(device: BluetoothDevice) {
                Printooth.setPrinter(device.name, device.address)
                Toast.makeText(this@SelectPrintScreen, "Device Paired", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
                setResult(Activity.RESULT_OK)
//                this@SelectPrintScreen.finish()
                val deviceModelList: java.util.ArrayList<DeviceModel> =
                    Stash.getArrayList<DeviceModel>(
                        Constants.LIST,
                        DeviceModel::class.java
                    )
                val deviceModel = DeviceModel()
                deviceModel.name = devices[j].name
                deviceModel.address = devices[j].address
                deviceModelList.add(deviceModel)
                Stash.put(Constants.LIST, deviceModelList)
                val intent = Intent(this@SelectPrintScreen, AddPrinterActivity::class.java)
                intent.putExtra("name", devices[j].name)
                intent.putExtra("address", devices[j].address)
                hideProgressDialog()
                startActivity(intent)
            }

            override fun onDeviceUnpaired(device: BluetoothDevice) {
//                Toast.makeText(this@SelectPrintScreen, "Device unpaired", Toast.LENGTH_SHORT).show()
//                val pairedPrinter = Printooth.getPairedPrinter()
//                if (pairedPrinter != null && pairedPrinter.address == device.address)
//                    Printooth.removeCurrentPrinter()
//                devices.remove(device)
//                adapter.notifyDataSetChanged()
//                bluetooth.startScanning()
            }

            override fun onError(message: String) {
                Toast.makeText(
                    this@SelectPrintScreen,
                    "Error while pairing",
                    Toast.LENGTH_SHORT
                ).show()
                adapter.notifyDataSetChanged()
                hideProgressDialog()
                finish()
            }
        })
    }

    private fun initListeners() {
//        refreshLayout.setOnRefreshListener {
//            refreshLayout.isRefreshing = false
//            bluetooth.startScanning()
//        }
        printers.setOnItemClickListener { _, _, i, _ ->
            val device = devices[i]
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                val layoutInflater = LayoutInflater.from(this)
                val promptsView: View =
                    layoutInflater.inflate(com.app.gobooa.R.layout.add_printer_dailogue, null)
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setView(promptsView)
                alertDialogBuilder.setCancelable(true)
                if (!devices[i].name.length.equals(null)) {
                    if (devices[i].name.length > 1) {
                        val alertDialog: AlertDialog = alertDialogBuilder.create()
                        alertDialog.show()
                        alertDialog.findViewById<TextView>(com.app.gobooa.R.id.name)
                            .setText(devices[i].name)
                        alertDialog.findViewById<TextView>(com.app.gobooa.R.id.address)
                            .setText(devices[i].address)
                        alertDialog.findViewById<Button>(com.app.gobooa.R.id.add_printer)
                            .setOnClickListener(View.OnClickListener {
                                bluetooth.pair(devices[i])
                                adapter.notifyDataSetChanged()
                                j = i

                                showProgressDialog();
                                alertDialog.dismiss()
                            })
                        alertDialog.findViewById<ImageView>(com.app.gobooa.R.id.imgBack)
                            .setOnClickListener(
                                View.OnClickListener {
                                    alertDialog.dismiss()

                                })
//            val cdd = AddPrinterDialogClass(this@SelectPrintScreen, bluetooth,devices, i, adapter, devices[i].name, devices[i].address)
//            cdd.show()
                    }
                }
            }
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
        bluetooth.onStop()
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

                    findViewById<TextView>(R.id.name).text =
                        if (devices[position].name.isNullOrEmpty()) devices[position].address else devices[position].name
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

    var alertDialog: androidx.appcompat.app.AlertDialog? = null

    fun showProgressDialog() {
        val dailogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView: View = inflater.inflate(com.app.gobooa.R.layout.dialog, null)
        dailogBuilder.setView(dialogView)
        alertDialog = dailogBuilder.create()
        alertDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog!!.setCanceledOnTouchOutside(false)
        alertDialog!!.show()
    }

    fun hideProgressDialog() {
        if (alertDialog != null && alertDialog!!.isShowing) {
            alertDialog!!.dismiss()
        }
    }

}
