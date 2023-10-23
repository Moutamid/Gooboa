package com.app.gobooa.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.gobooa.R;
import com.app.gobooa.activities.utils.PrinterConnectActivity;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//This class/activity file is used to display details of order when user clicks on any order from list or when user clicks
// on order preview. The same screen/activity used for both purposes. We can just differentiate if user comes through clicking
// order from list or comes from order preview..
public class OrderDetailsActivity extends BaseActivity implements PrintingCallback {

    //Variables and views declaration..
    TextView textViewID, textViewLivrareCollectare, textViewUser, textViewPhone, textViewAddress,
            textViewDateCreated, textViewTimeMeta, textViewStatus, textViewHideStatus;
    TextView textViewPayMethod, textViewTodayDate, tvdetails;
    ImageView imageViewBackArrow;
    RecyclerView recyclerViewProductsList;
    Button buttonPrint;

    AlertDialog alertDialog;

    //Used to store order status(on-hold or cancelled) when user provide his/her response to change order status from pop up dialog..
    String status;

    //This key variable is used to check whether user comes through clicking
    //on order from orders list screen or comes from clicking on order preview from new order pop up..
    String key = "";
    String payment_method;
    Printing printing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        //Views initialization..
        imageViewBackArrow = findViewById(R.id.imgBack);
        tvdetails = findViewById(R.id.tvDetails);
        textViewTodayDate = findViewById(R.id.tvTodayDate);
        textViewID = findViewById(R.id.tvID);
        textViewLivrareCollectare = findViewById(R.id.tvLivrareCollectare);
        textViewUser = findViewById(R.id.tvUser);
        textViewPhone = findViewById(R.id.tvPhone);
        textViewAddress = findViewById(R.id.tvAddress);
        textViewDateCreated = findViewById(R.id.textViewDateCreated);
        textViewTimeMeta = findViewById(R.id.tvTimeMeta);
        textViewStatus = findViewById(R.id.tvStatus);
        textViewHideStatus = findViewById(R.id.tvS);
        textViewPayMethod = findViewById(R.id.tvPayMethod);
        buttonPrint = findViewById(R.id.btnPrint);
        recyclerViewProductsList = findViewById(R.id.recyclerView);
        recyclerViewProductsList.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerViewProductsList.setLayoutManager(gridLayoutManager);

        if (printing != null) {
            printing.setPrintingCallback(this);
        }
        // This code 83-84 is used to get today's date and display it to textViewTodayDate widget..
        String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        textViewTodayDate.setText(cDate);

        if (MainActivity.modelClass.getId() != 0) {
            textViewID.setText("ID: " + MainActivity.modelClass.getId());
        }
        //This code 90-95 is to make first letter capital of order status and display it to textViewStatus..
        status = MainActivity.modelClass.getStatus();
        String inputStringStatus = MainActivity.modelClass.getStatus();
        String firstLetterStatus = inputStringStatus.substring(0, 1).toUpperCase();
        String restOfStringStatus = inputStringStatus.substring(1);
        String resultStringStatus = firstLetterStatus + restOfStringStatus;
        textViewStatus.setText("  " + resultStringStatus + "  ");

        // This code 98-106 is used to change background color of order status widget according to order status
        if (MainActivity.modelClass.getStatus().equals("cancelled") || MainActivity.modelClass.getStatus().equals("refunded") || MainActivity.modelClass.getStatus().equals("failed")) {
            textViewStatus.setBackground(getResources().getDrawable(R.drawable.canceled));
        } else if (MainActivity.modelClass.getStatus().equals("processing") || MainActivity.modelClass.getStatus().equals("pending")) {
            textViewStatus.setBackground(getResources().getDrawable(R.drawable.processing));
        } else if (MainActivity.modelClass.getStatus().equals("completed")) {
            textViewStatus.setBackground(getResources().getDrawable(R.drawable.completed));
        } else if (MainActivity.modelClass.getStatus().equals("on-hold")) {
            textViewStatus.setBackground(getResources().getDrawable(R.drawable.onhold));
        }

        //This code 109-113 is to make first letter capital of order type (delivery or pickup) and display it to textViewLivrareCollectare..
        String inputString = MainActivity.modelClass.getMetaDataList().get(5).getValue();
        String firstLetter = inputString.substring(0, 1).toUpperCase();
        String restOfString = inputString.substring(1);
        String resultString = firstLetter + restOfString;
        textViewLivrareCollectare.setText(resultString);

        //This code 116-118 is displaying user first last name, phone and address1, address2 who made the order..
        textViewUser.setText(MainActivity.modelClass.getFirstName() + " " + MainActivity.modelClass.getLastName());
        textViewPhone.setText(MainActivity.modelClass.getPhone());
        textViewAddress.setText("Address 1: " + MainActivity.modelClass.getAddress1() + "\nAddress 2: " + MainActivity.modelClass.getAddress2());

        //This code 121-123 is formatting the date on which order is created according to date format you provided
        String[] arr = MainActivity.modelClass.getDateCreated().split("T");
        String[] arr2 = arr[0].split("-");
        textViewDateCreated.setText(arr2[2] + "/" + arr2[1] + "/" + arr2[0] + ", " + arr[1]);

        //This line 126 is displaying time and date from meta data variable of server
        textViewTimeMeta.setText("Time: " + MainActivity.modelClass.getMetaDataList().get(3).getValue() + ", Ora: " + MainActivity.modelClass.getMetaDataList().get(4).getValue());

        //This code 129-133 is used to display payment method of order, if method = code then Cash otherwise Card..
        if (MainActivity.modelClass.getPaymentMethod().equals("cod")) {
            textViewPayMethod.setText("Plata: Cash");
        } else {
            textViewPayMethod.setText("Plata: Card");
        }

        //This code 137-139 is displaying products of order in a list by using RecyclerView widget with the help of
        // EventsListAdapter an RecylerView adapter class
        recyclerViewProductsList.setAdapter(null);
        EventsListAdapter adapter = new EventsListAdapter(OrderDetailsActivity.this, MainActivity.modelClass.getLineItemsList());
        recyclerViewProductsList.setAdapter(adapter);
        if (MainActivity.modelClass.getPaymentMethod().equals("cod")) {
            payment_method = "Cash";
        } else {
            payment_method = "Card";
        }

        //This 143-150 is checking whether user is coming by cliking on order from orders list of coming from clicking on
        //order preview. If user is coming from order preview then simply hide print button and order status widgets...
        Intent intent = getIntent();
        key = intent.getStringExtra("key");
        if (key.equals("pop")) {
            buttonPrint.setVisibility(View.GONE);
            textViewHideStatus.setVisibility(View.GONE);
            textViewStatus.setVisibility(View.GONE);
            MainActivity.preview = true;
        }

        //This code 153-158 is to perform back arrow action that displayed on top left..
        imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //This code 163-219 is used to display change order status pop up dialog when user clicks on order status to change it.
        //This dialog display 2 status options from (completed,on-hold,cancelled) according to status of order and then call a function
        //postDataOnServer() that is used to change order status..
        textViewStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dailogBuilder = new AlertDialog.Builder(OrderDetailsActivity.this);
                LayoutInflater inflater = getLayoutInflater();

                final View dialogView = inflater.inflate(R.layout.dialog3, null);
                dailogBuilder.setView(dialogView);

                TextView tvDone = dialogView.findViewById(R.id.tvDone);
                TextView tvProgress = dialogView.findViewById(R.id.tvProgress);
                TextView tvCanceled = dialogView.findViewById(R.id.tvCanceled);
                View v1 = dialogView.findViewById(R.id.v1);
                View v2 = dialogView.findViewById(R.id.v2);

                if (MainActivity.modelClass.getStatus().equals("completed")) {
                    tvDone.setVisibility(View.GONE);
                    v1.setVisibility(View.GONE);
                } else if (MainActivity.modelClass.getStatus().equals("on-hold")) {
                    tvProgress.setVisibility(View.GONE);
                    v2.setVisibility(View.GONE);
                } else if (MainActivity.modelClass.getStatus().equals("cancelled")) {
                    tvCanceled.setVisibility(View.GONE);
                    v2.setVisibility(View.GONE);
                }

                tvDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        status = "completed";
                        postDataOnServer();
                    }
                });
                tvProgress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        status = "processing";
                        postDataOnServer();
                    }
                });
                tvCanceled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        status = "cancelled";
                        postDataOnServer();
                    }
                });
                alertDialog = dailogBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });

        buttonPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < MainActivity.modelClass.getLineItemsList().size(); i++)
                {
                    String finalName = MainActivity.modelClass.getLineItemsList().get(i).getName().replaceAll("<span>", "");
                    String[] separated = finalName.split("- </span>");
                    String s = separated[0];
                    String finalName2 = finalName.replaceAll("</span>", "");
                    Log.d("item_details", s );
                    Log.d("item_details", MainActivity.modelClass.getLineItemsList().get(i).getSubTotal() + "0\n");
                    for (int j = 0; j < MainActivity.modelClass.getLineItemsList().get(i).getExtraData().size(); j++) {
                        Log.d("item_details", "-" + MainActivity.modelClass.getLineItemsList().get(i).getExtraData().get(j).getKey() + ": " + MainActivity.modelClass.getLineItemsList().get(i).getExtraData().get(j).getValue() + "\n");
                    }
                }
                if (!Printooth.INSTANCE.hasPairedPrinter()) {
                    startActivity(new Intent(OrderDetailsActivity.this, PrinterConnectActivity.class));
                } else {
                    printing = Printooth.INSTANCE.printer();
                    ArrayList<Printable> printables = new ArrayList<>();

                    printables.add(new RawPrintable.Builder(new byte[]{27, 33, 0}).build());
                    printables.add(new TextPrintable.Builder()
                            .setText("CUPTORUL CU PIZZA\n\n")
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("Str. Castanilor, Lupeni\n\n")
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_NORMAL())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("123456789\n\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_NORMAL())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("--------------------------\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("Receipt\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("---------------------------\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("Description             Price\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD())
                            .build());

                    for (int i = 0; i < MainActivity.modelClass.getLineItemsList().size(); i++) {
                        String finalName = MainActivity.modelClass.getLineItemsList().get(i).getName().replaceAll("<span>", "");
                        String[] separated = finalName.split("- </span>");
                        String s = separated[0];
                        printables.add(new TextPrintable.Builder()
                                .setText(s)
                                .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_35())
                                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_LEFT())
                                .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD())
                                .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_SMALL())
                                .build());
                        printables.add(new TextPrintable.Builder()
                                .setText(MainActivity.modelClass.getLineItemsList().get(i).getSubTotal() + "0\n\n")
                                .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_35())
                                .setAlignment(DefaultPrinter.Companion.getALIGNMENT_RIGHT())
                                .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD())
                                .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_SMALL())
                                .build());

                        for (int j = 0; j < MainActivity.modelClass.getLineItemsList().get(i).getExtraData().size(); j++) {
                            printables.add(new TextPrintable.Builder()
                                    .setText(" " + MainActivity.modelClass.getLineItemsList().get(i).getExtraData().get(j).getKey() + ":" + MainActivity.modelClass.getLineItemsList().get(i).getExtraData().get(j).getValue() + "\n")
                                    .setFontSize(DefaultPrinter.Companion.getFONT_SIZE_VERY_SMALL())
                                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_LEFT())

                                    .build());
                        }

                    }
                    printables.add(new TextPrintable.Builder()
                            .setText("--------------------------\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("Total" + "                 " + MainActivity.modelClass.getTotal() + " " + payment_method + "\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_LEFT())
                            .build());
                      printables.add(new TextPrintable.Builder()
                            .setText("---------------------------\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .build());
                    printables.add(new TextPrintable.Builder()
                            .setText("VA MULTUMIM !\n\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .build());  printables.add(new TextPrintable.Builder()
                            .setText("\n\n")
                            .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                            .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                            .build());
                    printing.print(printables);
                    printing.setPrintingCallback(OrderDetailsActivity.this);
                }

//                DialogClass cdd = new DialogClass(OrderDetailsActivity.this);
//                cdd.show();
            }
        });
    }

    //This function is used to initiate backgroud thread to change order status according to user selected status from pop uo dialog..
    private void postDataOnServer() {
        showProgressDialog();
        new postDataTask().execute();
    }

    //Background thread class to update/modify order status on server
    class postDataTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... args) {
            String response = null;
            try {
                //Called executePost() method HttpRequest class to post/update order status data on server..
                response = HttpRequest.executePost(MainActivity.domain, MainActivity.consumerKey, MainActivity.consumerSecret, MainActivity.modelClass.getId(), status);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            hideProgressDialog();
            Log.d("resss", response);

            //This code 246-262 is used to change order status on textViewStatus widget change textViewStatus background color as well
            if (response.equals("true")) {
                MainActivity.check = true;
                String inputStringStatus = status;
                String firstLetterStatus = inputStringStatus.substring(0, 1).toUpperCase();
                String restOfStringStatus = inputStringStatus.substring(1);
                String resultStringStatus = firstLetterStatus + restOfStringStatus;
                textViewStatus.setText("  " + resultStringStatus + "  ");

                if (status.equals("cancelled")) {
                    textViewStatus.setBackground(getResources().getDrawable(R.drawable.canceled));
                } else if (status.equals("processing")) {
                    textViewStatus.setBackground(getResources().getDrawable(R.drawable.processing));
                } else if (status.equals("completed")) {
                    textViewStatus.setBackground(getResources().getDrawable(R.drawable.completed));
                }
                finish();
            } else if (response.equals("false")) {
                Toast.makeText(OrderDetailsActivity.this, "Error: Something went wrong while updating status!", Toast.LENGTH_LONG).show();
            }
        }
    }

    //This is an adapter class of RecyclerView to set the design of each item in a list. RecyclerView is used to display products list of order
    // How each product will look like in a list is designed by this adapter class..
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
            holder.tvQty.setText(MainActivity.modelClass.getLineItemsList().get(position).getSubTotal() + "0");
            EventsListAdapter2 adapter2 = new EventsListAdapter2(OrderDetailsActivity.this, product.getExtraData());
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

    /*This is adapter class of RecyclerView to display the extra details of each product in a list.
      Like every product has some extra variables in it meta data tag. SO this class is used to display list within list.
      e.g All products of order are displayed in outer RecyclerView and extra variables of each product fro its meta data tag
      is displayed in inner RecyclerView */
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


}