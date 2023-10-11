package com.app.gobooa.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.gobooa.R;
import com.app.gobooa.activities.utils.PrinterConnectActivity;
import com.app.gobooa.models.MetaDataModelClass;
import com.app.gobooa.models.ProductModelClass;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.RawPrintable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//This class/activity file is used to display details of order when user clicks on any order from list or when user clicks
// on order preview. The same screen/activity used for both purposes. We can just differentiate if user comes through clicking
// order from list or comes from order preview..
public class PreviewActivity extends BaseActivity implements PrintingCallback {

    RecyclerView recyclerViewProductsList;
    String payment_method;
    Printing printing;
    TextView total;
    LinearLayout layout;
    TextView buttonPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);


        buttonPrint = findViewById(R.id.buttonPrint);
        total = findViewById(R.id.total);
        layout = findViewById(R.id.layout);

        recyclerViewProductsList = findViewById(R.id.recyclerView);
        recyclerViewProductsList.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerViewProductsList.setLayoutManager(gridLayoutManager);
        if (printing != null) {
            printing.setPrintingCallback(this);
        }

        recyclerViewProductsList.setAdapter(null);
        EventsListAdapter adapter = new EventsListAdapter(PreviewActivity.this, MainActivity.modelClass.getLineItemsList());
        recyclerViewProductsList.setAdapter(adapter);
        if (MainActivity.modelClass.getPaymentMethod().equals("cod")) {
            payment_method = "Cash";
        } else {
            payment_method = "Card";
        }
    total.setText(MainActivity.modelClass.getTotal() + "\n" + payment_method);

        buttonPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Printooth.INSTANCE.hasPairedPrinter()) {
                    startActivity(new Intent(PreviewActivity.this, PrinterConnectActivity.class));
                } else {
                    Bitmap bitmap = screenShot(layout);
                    ArrayList<Printable> printables = new ArrayList<>();
                    printing = Printooth.INSTANCE.printer();
                    printables.add(new ImagePrintable.Builder(bitmap).build());
                    printing.print(printables);
                    printing.setPrintingCallback(PreviewActivity.this);
                }
            }
        });
    }

    public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.ImageViewHolder> {
        private Context mcontext;
        private List<ProductModelClass> muploadList;

        public EventsListAdapter(Context context, List<ProductModelClass> uploadList) {
            mcontext = context;
            muploadList = uploadList;
        }

        @Override
        public EventsListAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.products_list_layout, parent, false);
            return (new EventsListAdapter.ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final EventsListAdapter.ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final ProductModelClass product = muploadList.get(position);
            String finalName = product.getName().replaceAll("<span>", "");
            String finalName2 = finalName.replaceAll("</span>", "");
            holder.tvProduct.setText(finalName2 + " - " + product.getQty());
            holder.tvQty.setText(MainActivity.modelClass.getLineItemsList().get(position).getSubTotal() + "");

            EventsListAdapter2 adapter2 = new EventsListAdapter2(PreviewActivity.this, product.getExtraData());
            holder.recyclerView.setAdapter(adapter2);
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            public TextView tvProduct;
            public TextView tvQty;
            public RecyclerView recyclerView;

            public ImageViewHolder(View itemView) {
                super(itemView);

                tvProduct = itemView.findViewById(R.id.tvProduct);
                tvQty = itemView.findViewById(R.id.tvQty);

                recyclerView = itemView.findViewById(R.id.recyclerView);
                recyclerView.setHasFixedSize(true);
                recyclerView.setNestedScrollingEnabled(false);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
                recyclerView.setLayoutManager(gridLayoutManager);

            }
        }
    }

    public class EventsListAdapter2 extends RecyclerView.Adapter<EventsListAdapter2.ImageViewHolder> {
        private Context mcontext;
        private List<MetaDataModelClass> muploadList;

        public EventsListAdapter2(Context context, List<MetaDataModelClass> uploadList) {
            mcontext = context;
            muploadList = uploadList;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.products_list_layout2, parent, false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final MetaDataModelClass product = muploadList.get(position);
            holder.tvProduct.setText("-" + product.getKey() + ": " + product.getValue());


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            public TextView tvProduct;

            public ImageViewHolder(View itemView) {
                super(itemView);

                tvProduct = itemView.findViewById(R.id.tvProduct);

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
        Log.d("status", "connectingWithPrinter");

    }

    @Override
    public void printingOrderSentSuccessfully() {
        Log.d("status", "printingOrderSentSuccessfully");

    }

    @Override
    public void connectionFailed(@NonNull String error) {
        Log.d("status", "connectionFailed" + error);

    }

    @Override
    public void onError(@NonNull String error) {
        Log.d("status", "onError" + error);

    }

    @Override
    public void onMessage(@NonNull String message) {
        Log.d("status", "onMessage" + message);

    }

    @Override
    public void disconnected() {

    }

    public Bitmap screenShot(LinearLayout view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

}