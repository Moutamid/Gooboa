package com.app.gobooa.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.gobooa.R;
import com.app.gobooa.models.MetaDataModelClass;
import com.app.gobooa.models.ProductModelClass;
import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.RawPrintable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;

import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends BaseActivity implements PrintingCallback {

    RecyclerView recyclerViewProductsList;
    TextView print_btn;
    Printing printing;

    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
//        recyclerViewProductsList = findViewById(R.id.recyclerView);
        print_btn = findViewById(R.id.print);
//        recyclerViewProductsList.setHasFixedSize(true);
//        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
//        recyclerViewProductsList.setLayoutManager(gridLayoutManager);
//        recyclerViewProductsList.setAdapter(null);
//        EventsListAdapter adapter = new EventsListAdapter(PreviewActivity.this, MainActivity.modelClass.getLineItemsList());
//        recyclerViewProductsList.setAdapter(adapter);
        if (printing != null) {
            printing.setPrintingCallback(this);
        }
        print_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestBlePermissions(PreviewActivity.this, 1);

//                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!Printooth.INSTANCE.hasPairedPrinter()) {
                    startActivityForResult(new Intent(PreviewActivity.this, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                } else {
                    printing = Printooth.INSTANCE.printer();
                    ArrayList<Printable> printables = new ArrayList<>();
                    printables.add(new RawPrintable.Builder(new byte[]{27, 100, 4}).build());
                    printables.add(new TextPrintable.Builder()
                            .setText("CUPTORUL CU PIZZA")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_60())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .setAlignment(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD())
                            .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_LARGE())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("Str. Castanilor, Lupeni")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_NORMAL())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("123456789")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_NORMAL())
                            .setNewLinesAfter(1)
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("------------------------------------------------")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .setNewLinesAfter(1)

                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("Receipt")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .setNewLinesAfter(1)

                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("------------------------------------------------")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .setNewLinesAfter(1)

                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("Product")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_LEFT())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("quantity")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_RIGHT())
                            .setNewLinesAfter(1)

                            .build());

                    printing.print(printables);
                    printing.setPrintingCallback(PreviewActivity.this);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK)
            try {
                initprinting();
            } catch (Exception e) {
                Log.d("Exception", e.getMessage());
            }
    }

    private void initprinting() {
        if (!Printooth.INSTANCE.hasPairedPrinter())
            printing = Printooth.INSTANCE.printer();
    }

    @Override
    public void connectingWithPrinter() {

        Toast.makeText(this, "connectingWithPrinter", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void printingOrderSentSuccessfully() {
        Toast.makeText(this, "printingOrderSentSuccessfully", Toast.LENGTH_SHORT).show();

        Log.d("status", "printingOrderSentSuccessfully");

    }

    @Override
    public void connectionFailed(@NonNull String error) {
        Toast.makeText(this, "connectionFailed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onError(@NonNull String error) {
        Toast.makeText(this, "onError" + error, Toast.LENGTH_SHORT).show();

        Log.d("status", "onError" + error);

    }

    @Override
    public void onMessage(@NonNull String message) {
        Log.d("status", "onMessage" + message);
        Toast.makeText(this,"onMessage" + message, Toast.LENGTH_SHORT).show();


    }

    @Override
    public void disconnected() {
        Toast.makeText(this,"Disconnected", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        print_btn.setText("Printer  "+Printooth.INSTANCE.getPairedPrinter().getName());

    }
//    public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.ImageViewHolder> {
//        private Context mcontext;
//        private List<ProductModelClass> muploadList;
//
//        public EventsListAdapter(Context context, List<ProductModelClass> uploadList) {
//            mcontext = context;
//            muploadList = uploadList;
//        }
//
//        @Override
//        public EventsListAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(mcontext).inflate(R.layout.products_list_layout, parent, false);
//            return (new EventsListAdapter.ImageViewHolder(v));
//        }
//
//        @Override
//        public void onBindViewHolder(final EventsListAdapter.ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {
//
//            final ProductModelClass product = muploadList.get(position);
//            String finalName = product.getName().replaceAll("<span>", "");
//            String finalName2 = finalName.replaceAll("</span>", "");
//            holder.tvProduct.setText(finalName2);
//            holder.tvQty.setText(product.getQty() + "");
//
//            EventsListAdapter2 adapter2 = new EventsListAdapter2(PreviewActivity.this, product.getExtraData());
//            holder.recyclerView.setAdapter(adapter2);
//        }
//
//        @Override
//        public int getItemCount() {
//            return muploadList.size();
//        }
//
//        public class ImageViewHolder extends RecyclerView.ViewHolder {
//            public TextView tvProduct;
//            public TextView tvQty;
//            public RecyclerView recyclerView;
//
//            public ImageViewHolder(View itemView) {
//                super(itemView);
//
//                tvProduct = itemView.findViewById(R.id.tvProduct);
//                tvQty = itemView.findViewById(R.id.tvQty);
//
//                recyclerView = itemView.findViewById(R.id.recyclerView);
//                recyclerView.setHasFixedSize(true);
//                recyclerView.setNestedScrollingEnabled(false);
//                GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
//                recyclerView.setLayoutManager(gridLayoutManager);
//
//            }
//        }
//    }
//
//    public class EventsListAdapter2 extends RecyclerView.Adapter<EventsListAdapter2.ImageViewHolder> {
//        private Context mcontext;
//        private List<MetaDataModelClass> muploadList;
//
//        public EventsListAdapter2(Context context, List<MetaDataModelClass> uploadList) {
//            mcontext = context;
//            muploadList = uploadList;
//        }
//
//        @Override
//        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(mcontext).inflate(R.layout.products_list_layout2, parent, false);
//            return (new ImageViewHolder(v));
//        }
//
//        @Override
//        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {
//
//            final MetaDataModelClass product = muploadList.get(position);
//            holder.tvProduct.setText("-" + product.getKey() + ": " + product.getValue());
//
//
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return muploadList.size();
//        }
//
//        public class ImageViewHolder extends RecyclerView.ViewHolder {
//            public TextView tvProduct;
//
//            public ImageViewHolder(View itemView) {
//                super(itemView);
//
//                tvProduct = itemView.findViewById(R.id.tvProduct);
//
//            }
//        }
//    }

    public static void requestBlePermissions(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        else
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);
    }
}