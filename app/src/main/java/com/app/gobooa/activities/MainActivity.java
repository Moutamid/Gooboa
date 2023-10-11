package com.app.gobooa.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.gobooa.R;
import com.app.gobooa.activities.utils.Constants;
import com.app.gobooa.activities.utils.PrinterConnectActivity;
import com.app.gobooa.models.MetaDataModelClass;
import com.app.gobooa.models.OrderModelClass;
import com.app.gobooa.models.ProductModelClass;
import com.app.gobooa.models.UserModeClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//This java file is an activity file to display list of orders on first screen after login
public class MainActivity extends AppCompatActivity {

    //All Variables and Widgets/Views declaration..

    //Firebase Realtime database reference variable
    DatabaseReference databaseReference;

    //Variable for storing url of server
    String url = "";

    //Variables for storing user data coming from real time database through login..
    public static String domain = "", consumerKey = "", consumerSecret = "";

    //Variable to store list of all the orders and its an List variable of OrderModelClass(Java getters and setters class of order variables)
    List<OrderModelClass> ordersList = new ArrayList<>();

    //This widget is used to display list of orders
    RecyclerView recyclerViewOrdersList;

    //These views are used to used to display text data on order list screen like Log out, Date and mo orders on selected date..
    TextView textViewNoOrders, textViewLogout, textViewSelectedDate;

    //This view is used for search image icon on top right
    ImageView imageViewSearchIcon;

    /*This is an object of EventsListAdapter java class that is an adapter class of RecyclerView. This class is used to make the design of each
    order in the list. This class is format the design that how each order will look like in list*/
    EventsListAdapter adapter;

    //These 2 List variables are used to store data of selected order when user click on any order from and moves to Order details screen.
    //On order details screen we get the data from these 2 varibales ti display selected order data Order details screen
    public static OrderModelClass modelClass;
    //This one is used when user clicks on order preview and go to order details screen.
    public static OrderModelClass previewModel;

    //This variable is used to display pop up notification of new order
    AlertDialog alertDialog;

    //Used to store today's date or selected date from date pop up dialog
    String selectedDate = "";

    //Used to store order status(on-hold or cancelled) when user provide his response from new order pop up accept or cancelled..
    String status = "";

    //Used to store order id of new order coming order to chaange this order status on server as on-hold or cancelled when user provide
    // his response from new order pop up accept or cancelled..
    int orderId = 0;

    //MediaPlayer object to play new order pop up sound
    private MediaPlayer mediaPlayer;

    //These are boolean variables to check if user change order status from order details screen or is user moved to order preview details screen or not?
    public static boolean check = false, preview = false;

    //This view/widget is used to display small circular progress bar on screen when data is loading from server. This view visibility is set to
    // Visible or Gone accordingly
    ProgressBar progressBarCircular;

    //This widget/object is used to count 1 minute time interval to refresh the data from server
    private CountDownTimer mCountDownTimer;

    //This variable to check if timer(to refresh data) is running or not
    boolean timerRunning = false;

    //Used to set the duration of timer running.. I set the timer to run for unlimited amount of time and never stop to refresh data after every 1 minute.
    long timeUntilNextYear = 0;

    public static int count = 0;
    ImageView menu;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //This code 133-141 is initializing all the views from xml file
        recyclerViewOrdersList = findViewById(R.id.recyclerView);
        recyclerViewOrdersList.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerViewOrdersList.setLayoutManager(gridLayoutManager);
        textViewNoOrders = findViewById(R.id.textView);
        textViewLogout = findViewById(R.id.tvLogout);
        textViewSelectedDate = findViewById(R.id.tvDate);
        imageViewSearchIcon = findViewById(R.id.imgSearchIcon);
        progressBarCircular = findViewById(R.id.pb);
        menu = findViewById(R.id.menu);
        Constants.checkApp(MainActivity.this);
        //This code 144-153 is used to set up the media player to play new order pop up souunf
        mediaPlayer = MediaPlayer.create(this, R.raw.new_order);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // When playback completes, restart the media player
                mediaPlayer.seekTo(0); // Seek to the beginning
                mediaPlayer.start();
            }
        });
        //This code 154-161 is used to implement log out feature
        textViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                finish();
            }
        });

        //This code 164-166 is getting today's date and setting it to textView of date
        String cDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        selectedDate = cDate;
        textViewSelectedDate.setText( cDate);

        //This code 169-174 is used to implement search icon click function to display date picker pop up dialog to display orders on selected date..
        imageViewSearchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate();
            }
        });

        //This code 177-182 is to implement data refresh from server code after every 1 minute
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.set(currentYear + 1, 0, 1, 0, 0, 0);
        Date nextYear = calendar.getTime();
        timeUntilNextYear = nextYear.getTime() - System.currentTimeMillis();
        timeUntilNextYear = timeUntilNextYear * 4;

        //Calling function that is getting user's data(domain,client key etc from real time database
//        loadUserDataFromFirebase();
        //Calling function to start refreshing data after every 1 minute
        startRefreshDataTimer();

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, menu);

                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setForceShowIcon(true);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override

                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.printer) {
                            startActivity(new Intent(MainActivity.this, PrinterConnectActivity.class));
                        } else if (menuItem.getItemId() == R.id.dates) {
                            selectDate();
                        } else if (menuItem.getItemId() == R.id.logout) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                            finish();
                        }
                        return true;

                    }

                });

                popupMenu.show();

            }
        });
    }

    //This function is used to start refreshing data after every 1 minute
    private void startRefreshDataTimer() {
        mCountDownTimer = new CountDownTimer(timeUntilNextYear, 60000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //After every 1 minute call the function that get user data from firebase and this loadUserDataFromFirebase() call another function
                // to fetch data from server
                loadUserDataFromFirebase();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
            }
        }.start();
        timerRunning = true;
    }

    //This function is used to pause the data refreshing code to pause refreshing data from server when user close the app or moved to next screen..
    private void pauseDataRefreshingFromServer() {
        if (timerRunning) {
            mCountDownTimer.cancel();
            timerRunning = false;
        }
    }

    //This function is used to resume the data refreshing code to resume refreshing data from server when user come back to orders list screen..
    private void resumeDataRefreshingFromServer() {
        if (!timerRunning) {
            startRefreshDataTimer();
        }
    }

    //This is built in function of android, it automatically called when user move to any other screen or close app temporarily..
    @Override
    protected void onPause() {
        super.onPause();
        pauseDataRefreshingFromServer();
    }

    //This is built in function of android, it automatically called when user come back to this orders list screen..
    @Override
    protected void onResume() {
        super.onResume();

        //If user change order status from order details screen, refresh the code to change order status on order list screen..
        if (check) {
            loadUserDataFromFirebase();
            check = false;
        } else {
            //When user come back from order preview details, again display the pop up for same order..
            if (preview) {
                displayPopUp(previewModel);
                preview = false;
            } else {
                resumeDataRefreshingFromServer();
            }
        }
    }

    //Built in function auto called when clicks on mobile back press button to close app..
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        finish();
    }

    /* This function is used to get/fetch currently logged in user data from firebase realtime database..
    After getting user data (domain, client key, client secret) this function generate server url and then call
    another function fetchDataFromServer() to fetch data from server against generated url*/
    private void loadUserDataFromFirebase() {
        stopMediaPlayer();
        progressBarCircular.setVisibility(View.VISIBLE);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserModeClass modeClass1 = snapshot.getValue(UserModeClass.class);

                url = modeClass1.getDomain() + "/wp-json/wc/v3/orders?consumer_key=" + modeClass1.getClientKey() + "&consumer_secret=" + modeClass1.getClientSecret() + "&per_page=100";

                Log.d("urlll", url);
                domain = modeClass1.getDomain();
                consumerKey = modeClass1.getClientKey();
                consumerSecret = modeClass1.getClientSecret();

                fetchDataFromServer();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    //This function is used to fetch data from server. This function initiate getDataTask background thread to get data from server..
    private void fetchDataFromServer() {
        ordersList.clear();
        new getDataTask().execute();
    }

    /*Background thread class to fetch data from server according to user generated url and key/secret.
      Background thread is used because fetching data from server is not allowed on main/front thread*/
    class getDataTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... args) {

            //HttpRequest java class executeGet() method is called to fetch data from server through HttpURLConnection class..
            //Server return data as json response. This response is passed to onPostExecute() function to parse it.
            String response = HttpRequest.executeGet(url, consumerKey, consumerSecret);
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            Log.d("resss", response);
            progressBarCircular.setVisibility(View.GONE);

            //After getting response from server, pass that response to parseServerResponse(response) function to
            // get each order variable from response separately
            parseServerResponse(response);
        }
    }

    /*This function is used to parse response fetched from server and set all data of orders in a list. parse means server
      returns data of all orders as a single string variable. So we need to parse that response to get data of each
      order(all variables separately) individually..*/
    private void parseServerResponse(String response) {
        try {
            JSONArray jsonArray = new JSONArray(response);

            ordersList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int id = jsonObject.getInt("id");
                String status = jsonObject.getString("status");
                String currency = jsonObject.getString("currency");
                String total = jsonObject.getString("total");
                String dateCreated = jsonObject.getString("date_created");
                String dateModified = jsonObject.getString("date_modified");
                String payment_method = jsonObject.getString("payment_method");
                Log.d("values", jsonObject + "    00");

                Log.e("date_created", dateCreated);
                // Parse other fields as needed

                Log.e("iddd", id + "");
                Log.e("statussss", status);

                // Parsing billing object
                JSONObject billingObject = jsonObject.getJSONObject("billing");
                String firstName = billingObject.getString("first_name");
                String lastName = billingObject.getString("last_name");
                String address_1 = billingObject.getString("address_1");
                String address_2 = billingObject.getString("address_2");
                String city = billingObject.getString("city");
                String state = billingObject.getString("state");
                String postcode = billingObject.getString("postcode");
                String phone = billingObject.getString("phone");
                Log.d("values", billingObject + "    0");

                Log.e("billingFirstName", firstName);
                Log.e("postcode", postcode);
                Log.e("address_1", address_1);

                //Parsing shipping object
                JSONObject shippingObject = jsonObject.getJSONObject("shipping");
                String shippingFirstName = shippingObject.getString("first_name");
                String shippingLastName = shippingObject.getString("last_name");
                String shippingAddress_1 = shippingObject.getString("address_1");
                String shippingAddress_2 = shippingObject.getString("address_2");
                String shippingCity = shippingObject.getString("city");
                String shippingState = shippingObject.getString("state");
                String shippingPostcode = shippingObject.getString("postcode");
                String shippingPhone = shippingObject.getString("phone");
                Log.e("shippingFirstName", shippingFirstName);
                Log.e("shippingLastName", shippingLastName);
                Log.d("values", shippingObject + "    1");

                // Parsing meta_data array
                JSONArray metaDataArray = jsonObject.getJSONArray("meta_data");
                List<MetaDataModelClass> metaDataList = new ArrayList<>();
                for (int j = 0; j < metaDataArray.length(); j++) {
                    JSONObject metaDataObject = metaDataArray.getJSONObject(j);
                    String metaDataKey = metaDataObject.getString("key");
                    String metaDataValue = metaDataObject.getString("value");
                    metaDataList.add(new MetaDataModelClass(metaDataKey, metaDataValue));
                    Log.d("values", metaDataObject + "    2");
                }
                // Parsing line_items array
                List<ProductModelClass> productsList = new ArrayList<>();
                JSONArray lineItemsArray = jsonObject.getJSONArray("line_items");
                for (int k = 0; k < lineItemsArray.length(); k++) {
                    JSONObject lineItemObject = lineItemsArray.getJSONObject(k);
                    String lineItemName = lineItemObject.getString("name");
                    String quantity = lineItemObject.getString("quantity");
                    int productId = lineItemObject.getInt("product_id");
                    double subtotal = lineItemObject.getDouble("subtotal");
                    Log.d("valuessssss", lineItemObject.getInt("product_id") + "    3");

                    List<MetaDataModelClass> extraData = new ArrayList<>();
                    JSONArray lineItemMetaArray = lineItemObject.getJSONArray("meta_data");
                    for (int h = 0; h < lineItemMetaArray.length(); h++) {
                        JSONObject lineItemMetaDataObject = lineItemMetaArray.getJSONObject(h);
                        String key = lineItemMetaDataObject.getString("display_key");
                        String val = lineItemMetaDataObject.getString("display_value");
                        extraData.add(new MetaDataModelClass(key, val));
                    }

                    productsList.add(new ProductModelClass(productId, lineItemName, Integer.parseInt(quantity), subtotal, extraData));
                }

                String[] arr = dateCreated.split("T");
                String[] arr2 = arr[0].split("-");
                String date = arr2[2] + "-" + arr2[1] + "-" + arr2[0];
                if(selectedDate.equals(date)){
                if (shippingFirstName.isEmpty() || shippingLastName.isEmpty() || shippingAddress_1.isEmpty() || shippingState.isEmpty() || shippingPhone.isEmpty()) {
                    String name1 = firstName;
                    String name2 = lastName;
                    String addr1 = address_1;
                    String addr2 = address_2;
                    String citty = city;
                    String statte = state;
                    String postalCode = postcode;
                    String phoneee = phone;

                    OrderModelClass modelClass = new OrderModelClass(id, status, payment_method, dateCreated, name1, name2, addr1, addr2,
                            citty, statte, postalCode, phoneee, metaDataList, productsList, currency, total);
                    ordersList.add(modelClass);
                } else {
                    String name1 = shippingFirstName;
                    String name2 = shippingFirstName;
                    String addr1 = shippingAddress_1;
                    String addr2 = shippingAddress_2;
                    String citty = shippingCity;
                    String statte = shippingState;
                    String postalCode = shippingPostcode;
                    String phoneee = shippingPhone;

                    OrderModelClass modelClass = new OrderModelClass(id, status, payment_method, dateCreated, name1, name2, addr1, addr2,
                            citty, statte, postalCode, phoneee, metaDataList, productsList, currency, total);
                    ordersList.add(modelClass);
                }
                }
            }
            Log.d("ordersList", ordersList.size() + "");

            //Set all orders data to list(recyclerViewOrdersList)
            textViewNoOrders.setText("");
            recyclerViewOrdersList.setAdapter(null);
            if (ordersList.size() > 0) {
                adapter = new EventsListAdapter(MainActivity.this, ordersList);
                recyclerViewOrdersList.setAdapter(adapter);
            } else {
                textViewNoOrders.setText("No orders on selected date!");
            }

            //Call function check if there's any new order status = processing
            checkNewOrder();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("parseError", "Error: " + e.toString());
        }
    }

    //This function is used to check if there's any new order with status = processing to display new order pop up..
    private void checkNewOrder() {
        OrderModelClass modelClass1 = new OrderModelClass();
        boolean flag = false;
        for (OrderModelClass modelClass : ordersList) {
            if (modelClass.getStatus().equals("processing")) {
                modelClass1 = modelClass;
                flag = true;
                break;
            }
        }
        if (flag) {
            flag = false;
            displayPopUp(modelClass1);
        }
    }

    //This function is used to display new order pop up on screen
    private void displayPopUp(OrderModelClass model) {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        count++;

        Log.d("countty", count + "");
        mediaPlayer.start();
        AlertDialog.Builder dailogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialog2, null);
        dailogBuilder.setView(dialogView);

        TextView tvLivrareCollectare = dialogView.findViewById(R.id.tvLivrareCollectare);
        TextView tvDateCreated = dialogView.findViewById(R.id.tvTime2);
        TextView tvTimeMeta = dialogView.findViewById(R.id.tvTimeMeta);
        TextView tvtotalCurrency = dialogView.findViewById(R.id.tvtotalCurrency);
        Button btnPreview = dialogView.findViewById(R.id.btnPreview);
        Button btnAccept = dialogView.findViewById(R.id.btnAccept);
        Button btnReject = dialogView.findViewById(R.id.btnReject);

        String inputString = model.getMetaDataList().get(5).getValue();
        String firstLetter = inputString.substring(0, 1).toUpperCase();
        String restOfString = inputString.substring(1);
        String resultString = firstLetter + restOfString;

        tvLivrareCollectare.setText(resultString);

        String[] arr = model.getDateCreated().split("T");
        String[] arr2 = arr[0].split("-");
        tvDateCreated.setText(arr2[2] + "/" + arr2[1] + "/" + arr2[0] + ", " + arr[1]);
        tvTimeMeta.setText("Time: " + model.getMetaDataList().get(3).getValue() + ", Ora: " + model.getMetaDataList().get(4).getValue());
        tvtotalCurrency.setText("Total: " + model.getTotal() + " " + model.getCurrency());
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modelClass = model;
                previewModel = model;
                alertDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), OrderDetailsActivity.class);
                intent.putExtra("key", "pop");
                startActivity(intent);
                stopMediaPlayer();
            }
        });
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "on-hold";
                orderId = model.getId();
                alertDialog.dismiss();
                stopMediaPlayer();
                postNewOrderStatusOnServer();
            }
        });
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = "cancelled";
                orderId = model.getId();
                alertDialog.dismiss();
                stopMediaPlayer();
                postNewOrderStatusOnServer();
            }
        });
        alertDialog = dailogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    //This function is used when user provide his/her response(as accepted or cancelled). This function is used to change
    // new order status from processing to on-hold or cancelled..
    private void postNewOrderStatusOnServer() {
        progressBarCircular.setVisibility(View.VISIBLE);
        //Initiating background thread
        new postDataTask().execute();
    }

    //This background thread class used to perform code that change new order status from processing to on-hold or cancelled..
    //Again background thread is used because whenever user need to interact with server that code must be executed on background thread.,
    class postDataTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... args) {
            String response = null;
            try {
                //HttpRequest java class executePost() method is called to change order status on server through HttpURLConnection class..
                response = HttpRequest.executePost(MainActivity.domain, MainActivity.consumerKey, MainActivity.consumerSecret, orderId, status);
            } catch (Exception e) {
                Log.d("Exception", "error" + e.getMessage());
            }
            return response;
        }

        //Check server response is order status changed or not
        @Override
        protected void onPostExecute(String response) {
            progressBarCircular.setVisibility(View.GONE);
            Log.d("resss", response);
            if (response.equals("true")) {
                if (status.equals("on-hold")) {
                    Toast.makeText(MainActivity.this, "Order accepted successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Order cancelled successfully", Toast.LENGTH_LONG).show();
                }
                loadUserDataFromFirebase();
            } else if (response.equals("false")) {
                Toast.makeText(MainActivity.this, "Error: Something went wrong while accepting order!", Toast.LENGTH_LONG).show();
            }
        }
    }

    //This is an adapter class of RecyclerView to set the design of each item in a list. RecyclerView is used to display orders list
    // How each order will look like in a list is designed by this adapter class
    public class EventsListAdapter extends RecyclerView.Adapter<EventsListAdapter.ImageViewHolder> {
        private Context mcontext;
        private List<OrderModelClass> muploadList;

        public EventsListAdapter(Context context, List<OrderModelClass> uploadList) {
            mcontext = context;
            muploadList = uploadList;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mcontext).inflate(R.layout.order_list_layout, parent, false);
            return (new ImageViewHolder(v));
        }

        @Override
        public void onBindViewHolder(final ImageViewHolder holder, @SuppressLint("RecyclerView") final int position) {

            final OrderModelClass order = muploadList.get(position);
            holder.tvID.setText("ID: " + order.getId());
            holder.tvPhone.setText("Phone: " + order.getPhone());

            String inputStringStatus = order.getStatus();
            String firstLetterStatus = inputStringStatus.substring(0, 1).toUpperCase();
            String restOfStringStatus = inputStringStatus.substring(1);
            String resultStringStatus = firstLetterStatus + restOfStringStatus;
            holder.tvStatus.setText("  " + resultStringStatus + "  ");

            if (order.getStatus().equals("cancelled") || order.getStatus().equals("refunded") || order.getStatus().equals("failed")) {
                holder.tvStatus.setBackground(getResources().getDrawable(R.drawable.canceled));
            } else if (order.getStatus().equals("processing") || order.getStatus().equals("pending")) {
                holder.tvStatus.setBackground(getResources().getDrawable(R.drawable.processing));
            } else if (order.getStatus().equals("completed")) {
                holder.tvStatus.setBackground(getResources().getDrawable(R.drawable.completed));
            } else if (order.getStatus().equals("on-hold")) {
                holder.tvStatus.setBackground(getResources().getDrawable(R.drawable.onhold));
            }

            String inputString = order.getMetaDataList().get(5).getValue();
            String firstLetter = inputString.substring(0, 1).toUpperCase();
            String restOfString = inputString.substring(1);
            String resultString = firstLetter + restOfString;
            holder.tvLivrareCollectare.setText(resultString);

            String[] arr = order.getDateCreated().split("T");
            holder.tvDate.setText(arr[1]);
//            holder.tvDate.setText(order.getMetaDataList().get(4).getValue());
            if (order.getPaymentMethod().equals("cod")) {
                holder.tvPayMethod.setText("Cash");
            } else {
                holder.tvPayMethod.setText("Card");
            }
            holder.tvtotalCurrency.setText(order.getTotal() + " " + order.getCurrency());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    modelClass = order;
                    Intent intent = new Intent(getApplicationContext(), OrderDetailsActivity.class);
                    intent.putExtra("key", "view");
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return muploadList.size();
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            public TextView tvID;
            public TextView tvPhone;
            public TextView tvStatus;
            public TextView tvLivrareCollectare;
            public TextView tvDate;
            public TextView tvPayMethod;
            public TextView tvtotalCurrency;

            public ImageViewHolder(View itemView) {
                super(itemView);

                tvID = itemView.findViewById(R.id.tvID);
                tvPhone = itemView.findViewById(R.id.tvPhone);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvLivrareCollectare = itemView.findViewById(R.id.tvLivrareCollectare);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvPayMethod = itemView.findViewById(R.id.tvPayMethod);
                tvtotalCurrency = itemView.findViewById(R.id.tvtotalCurrency);
            }
        }

        public void filterList(List<OrderModelClass> searchList) {
            this.muploadList = searchList;
            notifyDataSetChanged();
        }
    }

    //This function is used to display date picker pop up dialog to select specific date and display orders only on that date..
    private void selectDate() {
        try {

            final Calendar calendar = Calendar.getInstance();
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            final int month = calendar.get(Calendar.MONTH);
            final int year = calendar.get(Calendar.YEAR);

            DatePickerDialog datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    calendar.set(year, month, day);
                    selectedDate = sdf.format(calendar.getTime());
                    textViewSelectedDate.setText(selectedDate);
                    loadUserDataFromFirebase();
                }
            }, year, month, day);
            datePicker.setTitle("Select Date");
            datePicker.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //This function is used to stop sound/music of new pop up order
    private void stopMediaPlayer() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(this, R.raw.new_order);
        }
    }
}