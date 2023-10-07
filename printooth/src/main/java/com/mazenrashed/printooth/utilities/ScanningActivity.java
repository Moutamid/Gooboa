package com.mazenrashed.printooth.utilities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.afollestad.assent.Permission;
import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.R;
import com.mazenrashed.printooth.data.DiscoveryCallback;
import com.mazenrashed.printooth.data.PairedPrinter;

import java.util.ArrayList;

public class ScanningActivity extends AppCompatActivity {
    Bluetooth bluetooth;
    SwipeRefreshLayout refreshLayout;
    ListView printers;
    ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    BluetoothDevicesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning2);
        refreshLayout= findViewById(R.id.refreshLayout);
        printers= findViewById(R.id.printers);
        adapter = new BluetoothDevicesAdapter(this);
        bluetooth = new Bluetooth(this);
        setup();
    }

    private void setup() {
        initViews();
        initListeners();
        initDeviceCallback();
    }
    private void initDeviceCallback() {
        bluetooth.setDiscoveryCallback(new DiscoveryCallback() {
            @Override
            public void onDeviceUnpaired(@NonNull BluetoothDevice device) {

            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDiscoveryStarted() {
                refreshLayout.setRefreshing(true);
                devices.clear();
                devices.addAll(bluetooth.getPairedDevices());
                adapter.notifyDataSetChanged();
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDiscoveryFinished() {
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onDeviceFound(BluetoothDevice device) {
                if (!devices.contains(device)) {
                    devices.add(device);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onDevicePaired(BluetoothDevice device) {
                Printooth.INSTANCE.setPrinter(device.getName(), device.getAddress());
                Toast.makeText(ScanningActivity.this, "Device Paired", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                setResult(Activity.RESULT_OK);
                ScanningActivity.this.finish();
            }



            @Override
            public void onError(String message) {
                Toast.makeText(ScanningActivity.this, "Error while pairing", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void initListeners() {
        refreshLayout.setOnRefreshListener(() -> bluetooth.startScanning());
        printers.setOnItemClickListener((parent, view, i, l) -> {
            BluetoothDevice device = devices.get(i);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Printooth.INSTANCE.setPrinter(device.getName(), device.getAddress());
                setResult(Activity.RESULT_OK);
                ScanningActivity.this.finish();
            } else if (device.getBondState() == BluetoothDevice.BOND_NONE)
                bluetooth.pair(devices.get(i));
            adapter.notifyDataSetChanged();
        });
    }

    private void initViews() {
        printers.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

            bluetooth.onStart();
            if (!bluetooth.isEnabled())
                bluetooth.enable();
            new Handler().postDelayed(() -> bluetooth.startScanning(), 1000);
        }



    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.onStop();
    }

    public static final int SCANNING_FOR_PRINTER = 115;

    public class BluetoothDevicesAdapter extends ArrayAdapter<BluetoothDevice> {

        private ArrayList<BluetoothDevice> devices;

        public BluetoothDevicesAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
            devices = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.bluetooth_device_row, parent, false);

            TextView nameTextView = view.findViewById(R.id.name);
            TextView pairStatusTextView = view.findViewById(R.id.pairStatus);
            ImageView pairedPrinterImageView = view.findViewById(R.id.pairedPrinter);

            BluetoothDevice device = devices.get(position);
            nameTextView.setText(device.getName().isEmpty() ? device.getAddress() : device.getName());
            pairStatusTextView.setVisibility(device.getBondState() != BluetoothDevice.BOND_NONE ? View.VISIBLE : View.INVISIBLE);
            pairStatusTextView.setText(device.getBondState() == BluetoothDevice.BOND_BONDED ? "Paired" : device.getBondState() == BluetoothDevice.BOND_BONDING ? "Pairing.." : "");
            pairedPrinterImageView.setVisibility(Printooth.INSTANCE.getPairedPrinter() != null && Printooth.INSTANCE.getPairedPrinter().getAddress().equals(device.getAddress()) ? View.VISIBLE : View.GONE)
            ;

            return view;
        }

        public void addDevice(BluetoothDevice device) {
            devices.add(device);
            notifyDataSetChanged();
        }

        public void removeDevice(BluetoothDevice device) {
            devices.remove(device);
            notifyDataSetChanged();
        }

        public void clearDevices() {
            devices.clear();
            notifyDataSetChanged();
        }
    }

}