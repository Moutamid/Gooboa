package com.app.gobooa.activities;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.app.gobooa.R;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class AddPrinterDialogClass extends Dialog {

    public Activity c;
    Button button;
    ImageView imgBack;
    String name, address;

    public AddPrinterDialogClass(Activity a, String name, String address) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.name = name;
        this.address = address;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.add_printer_dailogue);
        imgBack = findViewById(R.id.imgBack);
        button = findViewById(R.id.add_printer);
        TextView name_tx = findViewById(R.id.name);
        TextView address_txt = findViewById(R.id.address);
        name_tx.setText(name);
        address_txt.setText(address);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
                boolean bond = device.createBond();
//
                if (bond) {
                    Intent intent = new Intent(c, AddPrinterActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("address", address);
                    c.startActivity(intent);
                    dismiss();
                } else {
                    Toast.makeText(c, "Could not connect to this device", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
//// Create a BluetoothSocket object for the device.
//                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
//                BluetoothSocket socket = null;
//                try {
//                    socket = device.createRfcommSocketToServiceRecord(uuid);
//// Connect to the device.
//                    socket.connect();
//                } catch (Exception e) {
//
//                    Log.d("exception", e.getMessage() + "");
//                }
//                
//                dismiss();

            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }


}