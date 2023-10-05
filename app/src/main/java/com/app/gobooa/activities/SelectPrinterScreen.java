package com.app.gobooa.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.gobooa.R;
import com.app.gobooa.activities.Adapter.DeviceListAdapter;
import com.fxn.stash.Stash;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SelectPrinterScreen extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();

    public DeviceListAdapter mDeviceListAdapter;

    ListView lvNewDevices;

    @SuppressLint("MissingPermission")
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            ArrayList<String> device1 = Stash.getArrayList("device", String.class);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                if (device1.size() > 0) {
                    for (String device_address : device1) {
                        if (!device_address.equals(device.getAddress())) {
                            mBTDevices.add(device);
                            Log.d(TAG, "onReceive: " + device1 + device.getName() + ": " + device.getAddress());
                            mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                            lvNewDevices.setAdapter(mDeviceListAdapter);
                        }
                    }
                } else {
                    mBTDevices.add(device);
                    Log.d(TAG, "onReceive: " + device1 + device.getName() + ": " + device.getAddress());
                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    lvNewDevices.setAdapter(mDeviceListAdapter);

                }
            }

            }
        }

        ;

        TextView textViewSelectedDate;
        String selectedDate = "";
        ImageView imgBack, refresh;
        CardView card_main;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_select_printer_screen);
            imgBack = findViewById(R.id.imgBack);
            textViewSelectedDate = findViewById(R.id.tvDate);
            card_main = findViewById(R.id.card_main);
            refresh = findViewById(R.id.refresh);

            //This code 164-166 is getting today's date and setting it to textView of date
            String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            selectedDate = cDate;
            textViewSelectedDate.setText("Today’s date " + cDate);
            lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
            mBTDevices = new ArrayList<>();
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                btnDiscover();
            }

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver3, filter);


            lvNewDevices.setOnItemClickListener(SelectPrinterScreen.this);


            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });
            card_main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                        btnDiscover();
                    } else {
                        Toast.makeText(SelectPrinterScreen.this, "Please Turn On Bluetooth to get add printer", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        public void btnDiscover() {
            Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
                Log.d(TAG, "btnDiscover: Canceling discovery.");

                //check BT permissions in manifest
                checkBTPermissions();

                mBluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            }
            if (!mBluetoothAdapter.isDiscovering()) {

                //check BT permissions in manifest
                checkBTPermissions();

                mBluetoothAdapter.startDiscovery();
                IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            }
        }


        private void checkBTPermissions() {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if (permissionCheck != 0) {

                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                }
            } else {
                Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //first cancel discovery because its very memory intensive.
            mBluetoothAdapter.cancelDiscovery();

            Log.d(TAG, "onItemClick: You Clicked on a device.");
            String deviceName = mBTDevices.get(i).getName();
            String deviceAddress = mBTDevices.get(i).getAddress();

            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);
            AddPrinterDialogClass cdd = new AddPrinterDialogClass(SelectPrinterScreen.this, deviceName, deviceAddress);
            cdd.show();

        }
    }