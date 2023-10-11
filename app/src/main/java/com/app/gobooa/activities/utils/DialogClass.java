package com.app.gobooa.activities.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.gobooa.R;
import com.app.gobooa.activities.MainActivity;
import com.app.gobooa.models.MetaDataModelClass;
import com.app.gobooa.models.ProductModelClass;
import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;

import java.util.ArrayList;
import java.util.List;


public class DialogClass extends Dialog implements PrintingCallback {

    public Activity c;
    RecyclerView recyclerViewProductsList;
    String payment_method;
    Printing printing;
    TextView total;
    LinearLayout layout;
    TextView buttonPrint;

    public DialogClass(Activity a) {
        super(a);
        this.c = a;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.activity_preview);


        buttonPrint = findViewById(R.id.buttonPrint);
        total = findViewById(R.id.total);
        layout = findViewById(R.id.layout);

        recyclerViewProductsList = findViewById(R.id.recyclerView);
        recyclerViewProductsList.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(c, 1);
        recyclerViewProductsList.setLayoutManager(gridLayoutManager);
        if (printing != null) {
            printing.setPrintingCallback(this);
        }

        recyclerViewProductsList.setAdapter(null);
        EventsListAdapter adapter = new EventsListAdapter(c, MainActivity.modelClass.getLineItemsList());
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
                  c.startActivity(new Intent(c, PrinterConnectActivity.class));
                } else {
                    Bitmap bitmap = screenShot(layout);
                    ArrayList<Printable> printables = new ArrayList<>();
                    printing = Printooth.INSTANCE.printer();
                    printables.add(new ImagePrintable.Builder(bitmap).build());
                    printing.print(printables);
                    printing.setPrintingCallback((PrintingCallback) c);
                    dismiss();

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

            EventsListAdapter2 adapter2 = new EventsListAdapter2(c, product.getExtraData());
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
                GridLayoutManager gridLayoutManager = new GridLayoutManager(c, 1);
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
        public EventsListAdapter2.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.products_list_layout2, parent, false);
            return (new EventsListAdapter2.ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final EventsListAdapter2.ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

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