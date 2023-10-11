package com.app.gobooa.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.gobooa.R;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class AddPrinterActivity extends AppCompatActivity {
    TextView textViewSelectedDate, name, address;
    String selectedDate = "";
    ImageView imgBack, details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_printer);
        name = findViewById(R.id.name);
        address = findViewById(R.id.address);
        imgBack = findViewById(R.id.imgBack);
        details = findViewById(R.id.details);
        textViewSelectedDate = findViewById(R.id.tvDate);
        String name_str = getIntent().getStringExtra("name");
        String address_str = getIntent().getStringExtra("address");
        name.setText(name_str);
        address.setText(address_str);
        String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        selectedDate = cDate;
        textViewSelectedDate.setText(cDate);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddPrinterActivity.this, PrinterConnectActivity.class));
                finish();
            }
        });
    }
}