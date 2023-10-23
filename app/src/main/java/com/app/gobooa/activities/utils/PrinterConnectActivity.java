package com.app.gobooa.activities.utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.gobooa.R;
import com.app.gobooa.activities.Adapter.PairedDevicesAdapter;
import com.app.gobooa.activities.MainActivity;
import com.app.gobooa.activities.SelectPrintScreen;
import com.fxn.stash.Stash;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PrinterConnectActivity extends AppCompatActivity {


    TextView textViewSelectedDate;
    String selectedDate = "";
    ImageView imgBack;
    Button add_printer;
    Button btn_accept, deny;
    private static final int REQUEST_ENABLE_BT = 0;
    boolean is_bluetooth = false;
    CardView permission_bluetooth;
    BluetoothAdapter mBluetoothAdapter;
    ArrayList<DeviceModel> resturantModels;
    RecyclerView content_rcv;
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
        no_paired = (TextView) findViewById(R.id.no_paired);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            permission_bluetooth.setVisibility(View.GONE);
        }
        String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        selectedDate = cDate;
        textViewSelectedDate.setText(cDate);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PrinterConnectActivity.this, MainActivity.class));
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
                    Intent intent = new Intent(PrinterConnectActivity.this, SelectPrintScreen.class);
                    startActivity(intent);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        askPermission();
                    } else {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }
            }
        });
        resturantModels = Stash.getArrayList(Constants.LIST, DeviceModel.class);
        PairedDevicesAdapter pairedDevicesAdapter;
        content_rcv = findViewById(R.id.content_rcv);

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    askPermission();
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        });
        if (resturantModels != null)
        {
            resturantModels = (ArrayList<DeviceModel>) resturantModels.stream().distinct().collect(Collectors.toList());
            no_paired.setVisibility(View.GONE);
            content_rcv.setLayoutManager(new LinearLayoutManager(this));
            pairedDevicesAdapter = new PairedDevicesAdapter(this, resturantModels);
            content_rcv.setAdapter(pairedDevicesAdapter);
        }
        else
        {
            no_paired.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            permission_bluetooth.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void askPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            permission_bluetooth.setVisibility(View.GONE);
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        }
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            Toast.makeText(PrinterConnectActivity.this, "Please turn On bluetooth to move next", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken permissoonToken) {
                        permissoonToken.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (!mBluetoothAdapter.isEnabled()) {
                permission_bluetooth.setVisibility(View.VISIBLE);
            }
        }

    }


}