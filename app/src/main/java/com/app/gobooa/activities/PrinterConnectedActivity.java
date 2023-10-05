package com.app.gobooa.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.gobooa.R;
import com.fxn.stash.Stash;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class PrinterConnectedActivity extends AppCompatActivity {

    TextView lvNewDevices;

    TextView textViewSelectedDate;
    String selectedDate = "";
    ImageView imgBack;
    Button add_printer;
    Button btn_accept, deny;
    private static final int REQUEST_ENABLE_BT = 0;
    boolean is_bluetooth = false;
    CardView permission_bluetooth;
    BluetoothAdapter btAdapter;

    TextView no_paired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_connected);
        imgBack = findViewById(R.id.imgBack);
        add_printer = findViewById(R.id.add_printer);
        textViewSelectedDate = findViewById(R.id.tvDate);
        btn_accept = findViewById(R.id.btn_accept);
        deny = findViewById(R.id.deny);
        permission_bluetooth = findViewById(R.id.permission_bluetooth);
        lvNewDevices = (TextView) findViewById(R.id.lvNewDevices);
        no_paired = (TextView) findViewById(R.id.no_paired);
        btAdapter = BluetoothAdapter.getDefaultAdapter();


        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            CheckBluetoothState();
            permission_bluetooth.setVisibility(View.GONE);
        }


        String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        selectedDate = cDate;
        textViewSelectedDate.setText("Today’s date " + cDate);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PrinterConnectedActivity.this, MainActivity.class));
                finishAffinity();
            }
        });
        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        add_printer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    Intent intent = new Intent(PrinterConnectedActivity.this, SelectPrinterScreen.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(PrinterConnectedActivity.this, "Please Turn On Bluetooth to get add printer", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            is_bluetooth = true;
            permission_bluetooth.setVisibility(View.GONE);
            CheckBluetoothState();
            Toast.makeText(this, "Bluetooth in now ON", Toast.LENGTH_SHORT).show();
        }

    }

    private void CheckBluetoothState() {
        Stash.clear("device");
        ArrayList<String> bluetoothDevices = new ArrayList<>();

        // Listing paired devices
        Set<BluetoothDevice> devices = btAdapter.getBondedDevices();

        if (devices.size() > 0) {
            no_paired.setVisibility(View.GONE);
            for (BluetoothDevice device : devices) {
                bluetoothDevices.add(device.getAddress());
                if (!lvNewDevices.getText().toString().contains(device.getName())) {
                    lvNewDevices.append(device.getName() + "\n" + device + "\n\n");
                }
            }
            Stash.put("device", bluetoothDevices);
        } else {

            no_paired.setVisibility(View.VISIBLE);

        }

    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.

//        Log.d(TAG, "onItemClick: You Clicked on a device.");
//        String deviceName = mBTDevices.get(i).getName();
//        String deviceAddress = mBTDevices.get(i).getAddress();
//
//        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
//        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);
//
//        //create the bond.
//        //NOTE: Requires API 17+? I think this is JellyBean
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            Log.d(TAG, "Trying to pair with " + deviceName);
//            mBTDevices.get(i).createBond();
//
//            mBTDevice = mBTDevices.get(i);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            CheckBluetoothState();
            permission_bluetooth.setVisibility(View.GONE);
        }
    }
}