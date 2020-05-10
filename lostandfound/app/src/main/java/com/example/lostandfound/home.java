package com.example.lostandfound;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class home extends AppCompatActivity implements OnMapReadyCallback, MaterialSearchBar.OnSearchActionListener {

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    Button addalert, alertbtn2, alertbtn3;

    private static final String CHANNEL_ID = "CHANNEL_ID";
    EditText alertinput, alertdescrip;


    String URL_FETCH = "https://lfapp.000webhostapp.com/fetch.php";
    String URL_ALERT="https://lfapp.000webhostapp.com/alert.php";
    String URL_SEARCH="https://lfapp.000webhostapp.com/search.php";

    HttpURLConnection conn;
    List<Item> productList;

    String itemname, itemdescrip;

    String bulkalerts[];

    ArrayList<String> locations;
    private GoogleMap mMap;
    private MaterialSearchBar searchBar;

    CharSequence search = "";
    SlidingUpPanelLayout slidingUpPanelLayout;

    Button profilebtn;
    ArrayList<ArrayList<String> > mastercache;

    DatabaseHandler myDb;

    Feedback feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkusersession();
        feedback = new Feedback(this);
        myDb = new DatabaseHandler(this);

        recyclerView = findViewById(R.id.recyleview);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        mastercache =
                new ArrayList<ArrayList<String> >(50);

        bulkalerts = new String[10];
        locations = new ArrayList<String>();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        slidingUpPanelLayout =   (SlidingUpPanelLayout)
                findViewById(R.id.sliding_layout);

        //Setting the achor point to the middle of the screen


        Button addentry = (Button) findViewById(R.id.addentry);
        addentry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(home.this, Entry.class));
            }
        });

        profilebtn = (Button) findViewById(R.id.profilebtn);
        profilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(home.this, profile.class));
            }
        });

        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        searchBar.setHint("E.g. Keys");
        searchBar.setSpeechMode(true);
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                productList.clear();
                search =  text;
                new SearchItems().execute();
                searchBar.clearFocus(); // close the keyboard on load

                slidingUpPanelLayout.setAnchorPoint(0.5f);
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

            }

            @Override
            public void onButtonClicked(int buttonCode) {
            }
        });


        addalert = (Button) findViewById(R.id.button4);
        addalert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertdialog("one");
            }
        });

        alertbtn2 = (Button) findViewById(R.id.button5);
        alertbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertdialog("two");
            }
        });

        alertbtn3 = (Button) findViewById(R.id.button6);
        alertbtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertdialog("three");

            }
        });

        onStartAlerts();

        productList = new ArrayList<>();


        CheckConnection checkConnection = new CheckConnection(this);
        if (checkConnection.IsInternet() == true){

            offlineDelete();
            loadProducts();
        }else{

            offlineFetch();
        }

        final Feedback feedback = new Feedback(this);
        //feedback.progress("Loading");


       // feedback.AlertDialog("No Internet Connection","Dont worry, You still view an offline cache of the application");

    }

    private void checkusersession() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = preferences.getString("username", "");

        if (username == ""){
            startActivity(new Intent(home.this, login.class));
        }

    }

    private void offlineDelete() {

        myDb = new DatabaseHandler(this);
        myDb.deleteAll();

    }

    private void offlineFetch() {

        Cursor res = myDb.getAllData();
        if(res.getCount() == 0){

            //Show message FAIL
            return;
        }else {
            StringBuffer buffer = new StringBuffer();
            while (res.moveToNext()) {

                productList.add(new Item(
                        res.getString(1),
                        res.getString(2),
                        res.getString(3),
                        res.getString(4),
                        res.getString(5),
                        "null",
                        "null",
                        res.getString(6),
                        res.getString(7)

                ));
            }

            ItemAdapter adapter = new ItemAdapter(home.this, productList);

            recyclerView.setAdapter(adapter);

        }
    }

    public void onStartAlerts(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String alertstr = preferences.getString("one", "");
        String alertdescripstr = preferences.getString("onedescrip", "");
        if (alertstr != "") {
            addalert.setText(alertstr);
            itemname = alertstr;
            itemdescrip = alertdescripstr;
            new item_alert().execute();
        }

        String alertstr2 = preferences.getString("two", "");
        String alertdescripstr2 = preferences.getString("onedescrip", "");
        if (alertstr2 != "") {
            alertbtn2.setText(alertstr2);
            itemname = alertstr2;
            itemdescrip = alertdescripstr2;
            new item_alert().execute();
        }

        String alertstr3 = preferences.getString("three", "");
        String alertdescripstr3 = preferences.getString("onedescrip", "");
        if (alertstr3 != "") {
            alertbtn3.setText(alertstr3);
            itemname = alertstr3;
            itemdescrip = alertdescripstr3;
            new item_alert().execute();
        }

    }

    public void alertdialog(final String count){

        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        final View dialogView = LayoutInflater.from(home.this).inflate(R.layout.alertdia, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(home.this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();

        alertinput = (EditText) dialogView.findViewById(R.id.etalertdia);
        alertdescrip = (EditText) dialogView.findViewById(R.id.etalertdiadeescrip);


        Button setalert = (Button) dialogView.findViewById(R.id.btnalertdia);
        setalert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String alertstr = alertinput.getText().toString();

                itemname = alertinput.getText().toString();
                itemdescrip = alertdescrip.getText().toString();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(home.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(count, alertstr);
                editor.putString(count + "descrip", itemdescrip);
                editor.apply();


                //  Toast.makeText(home.this, bulkalerts[0], Toast.LENGTH_LONG);

                new item_alert().execute();

                alertDialog.dismiss();

                if(count == "one"){
                    addalert.setText(alertstr);
                }else if(count == "two"){
                    alertbtn2.setText(alertstr);
                }else if(count == "three"){
                    alertbtn3.setText(alertstr);
                }

            }
        });

        alertDialog.show();
    }

    public class item_alert extends AsyncTask<Void, Void, String> {
    @Override
    protected  void onPreExecute() {

        super.onPreExecute();

    }
    @SuppressLint("WrongThread")
    @Override
    protected String doInBackground(Void... arg0) {
        try {
            URL url = new URL(URL_ALERT);

            conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);



            // Append parameters to URL
            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("name", itemname);


            String query = builder.build().getEncodedQuery();
            // Open connection for sending data
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();
            conn.connect();
            int response_code = conn.getResponseCode();
            // Check if successful connection made
            if (response_code == HttpURLConnection.HTTP_OK) {
                // Read data sent from server
                InputStream input = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                // Pass data to onPostExecute method
                return(result.toString());
            }else{
                return("unsuccessful");
            }
        }

        catch (IOException e)
        {
            Log.d("BLOOD","IOE response " + e.toString());
            // TODO Aut return 0
            //URL_RESPONSE="ERROR";
        }
        return null;
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        //   Toast.makeText(Alerts.this, result, Toast.LENGTH_LONG);
        //  alertet.setText(result);

        // Toast.makeText(home.this, result, Toast.LENGTH_LONG);

        if(result != null) {

            try {
                //converting the string to json array object
                JSONArray array = new JSONArray(result);

                int count = 0;

                //traversing through all the object
                for (int i = 0; i < array.length(); i++) {

                    //getting product object from json array
                    JSONObject product = array.getJSONObject(i);

                    String descrip = product.getString("description");

                    CosineSimilarity cs = new CosineSimilarity();

                    double res = cs.score(itemdescrip, descrip);

                    if (res > 0.5) {
                        count++;

                        bulkalerts[i] = product.getString("name") +
                                "/" + product.getString("description") +
                                "/" + product.getString("location") +
                                "/" + product.getString("date") +
                                "/" + product.getString("image") +
                                "/" + product.getString("ctime") +
                                "/" + product.getString("username") +
                                "/" + product.getString("extra") +
                                "/" + product.getString("id");

                    }
                }

                settingalerts(bulkalerts, count);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else{

            //Error handling


        }    }
}

    public void settingalerts(String[] name, int count){

        createNotificationChannel();
        LongOperation lo = new LongOperation(home.this);

        String[] x = new String[count];

        for(int i = 0; i < count; i++){

            x[i] = name[i];
        }

        lo.execute(x);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "name", importance);
            channel.setDescription("description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    class LongOperation extends AsyncTask<String, String, String> {

        private static final String TAG = "longoperation";
        private Context ctx;
        private AtomicInteger notificationId = new AtomicInteger(0);

        LongOperation(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            for (String s: params) {
                Log.e(TAG, s);

                publishProgress(s);

                for (int i = 0; i < 5; i++) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                }
            }
            return "Executed";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            for (String title: values) {
                sendNotification(title, notificationId.incrementAndGet());
            }
        }

        void sendNotification(String title, int notificationId) {

            String[] info = title.split("/");
            String name = info[0];
            String descritpion = info[1];
            String location = info[2];
            String date = info[3];
            String image = info[4];
            String ctime = info[5];
            String username = info[6];
            String extra = info[7];
            String id = info[8];

            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(ctx, Viewing.class);
            intent.putExtra("name", name);
            intent.putExtra("description", descritpion);
            intent.putExtra("date", date);
            intent.putExtra("location",location);
            intent.putExtra("image", image);
            intent.putExtra("ctime",ctime);
            intent.putExtra("username", username);
            intent.putExtra("id", id);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

            Intent snoozeIntent = new Intent(ctx, MyBroadcastReceiver.class);
            snoozeIntent.putExtra("one", notificationId);

            Log.e(TAG, snoozeIntent.getExtras().toString());

            PendingIntent snoozePendingIntent =
                    PendingIntent.getBroadcast(ctx, notificationId, snoozeIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("New match for a " + name)
                    .setContentText(descritpion)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent)
                    // Add the action button
                    .addAction(R.drawable.ic_launcher_foreground, ctx.getString(R.string.project_id),
                            snoozePendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(notificationId, builder.build());
        }

    }


    public class CosineSimilarity {

        private class Values {

            private int Descrip1;
            private int Descrip2;

            private Values(int v1, int v2) {
                this.Descrip1 = v1;
                this.Descrip2 = v2;
            }

            public void updateValues(int v1, int v2) {
                this.Descrip1 = v1;
                this.Descrip2 = v2;
            }
        }//end of class values


        public double score(String txt1, String txt2) {
            //1. Identify distinct words from both documents
            String[] text1Words = txt1.split(" ");
            String[] text2Words = txt2.split(" ");
            Map<String, Values> FrequencyOfWordVector = new HashMap<>();
            List<String> distinctWords = new ArrayList<>();

            //prepare word frequency vector by using Text1
            for (String text : text1Words) {
                String word = text.trim();
                if (!word.isEmpty()) {
                    if (FrequencyOfWordVector.containsKey(word)) {
                        CosineSimilarity.Values descrips1 = FrequencyOfWordVector.get(word);
                        int freq1 = descrips1.Descrip1 + 1;
                        int freq2 = descrips1.Descrip2;
                        descrips1.updateValues(freq1, freq2);
                        FrequencyOfWordVector.put(word, descrips1);
                    } else {
                        CosineSimilarity.Values descrips1 = new CosineSimilarity.Values(1, 0);
                        FrequencyOfWordVector.put(word, descrips1);
                        distinctWords.add(word);
                    }
                }
            }

            //prepare word frequency vector by using Text2
            for (String text : text2Words) {
                String word = text.trim();
                if (!word.isEmpty()) {
                    if (FrequencyOfWordVector.containsKey(word)) {
                        CosineSimilarity.Values descrips1 = FrequencyOfWordVector.get(word);
                        int freq1 = descrips1.Descrip1;
                        int freq2 = descrips1.Descrip2 + 1;
                        descrips1.updateValues(freq1, freq2);
                        FrequencyOfWordVector.put(word, descrips1);
                    } else {
                        CosineSimilarity.Values descrips1 = new CosineSimilarity.Values(0, 1);
                        FrequencyOfWordVector.put(word, descrips1);
                        distinctWords.add(word);
                    }
                }
            }

            //calculate the cosine similarity score.
            double vectorAB = 0.0000000;
            double vectorA = 0.0000000;
            double vectorB = 0.0000000;
            for (int i = 0; i < distinctWords.size(); i++) {
                CosineSimilarity.Values descrip12 = FrequencyOfWordVector.get(distinctWords.get(i));
                double frequency1 = descrip12.Descrip1;
                double frequency2 = descrip12.Descrip2;
                System.out.println(distinctWords.get(i) + "#" + frequency1 + "#" + frequency2);

                vectorAB = vectorAB + frequency1 * frequency2;
                vectorA = vectorA + frequency1 * frequency1;
                vectorB = vectorB + frequency2 * frequency2;
            }

            System.out.println("VectAB " + vectorAB + " VectA_Sq " + vectorA + " VectB_Sq " + vectorB);
            return ((vectorAB) / (Math.sqrt(vectorA) * Math.sqrt(vectorB)));
        }
    }

    private void signingout() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();

        startActivity(new Intent(home.this, login.class));

    }

    private void loadProducts() {

        /*
         * Creating a String Request
         * The request type is GET defined by first parameter
         * The URL is defined in the second parameter
         * Then we have a Response Listener and a Error Listener
         * In response listener we will get the JSON response as a String
         * */
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_FETCH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject product = array.getJSONObject(i);

                                //adding the product to product list
                                productList.add(new Item(
                                        product.getString("name"),
                                        product.getString("description"),
                                        product.getString("date"),
                                        product.getString("extra"),
                                        product.getString("location"),
                                        product.getString("image"),
                                        product.getString("id"),
                                        product.getString("username"),
                                        product.getString("ctime")
                                ));

                                myDb.insertData( product.getString("name"),
                                        product.getString("description"),
                                        product.getString("date"),
                                        product.getString("extra"),
                                        product.getString("location"),
                                        product.getString("username"),
                                        product.getString("ctime"));


                                loc(product.getString("location"), product.getString("name"));

                            }

                            //creating adapter object and setting it to recyclerview
                            ItemAdapter adapter = new ItemAdapter(home.this, productList);

                            recyclerView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        feedback.AlertDialog("Error", "please try again");

                    }
                });

        //adding our stringrequest to queue
        Volley.newRequestQueue(this).add(stringRequest);


    }

    public class SearchItems extends AsyncTask<Void, Void, String> {
        @Override
        protected  void onPreExecute() {

            super.onPreExecute();

            feedback.progress("Finding item");
        }
        @Override
        protected String doInBackground(Void... arg0) {
            try {
                URL url = new URL(URL_SEARCH);

                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("search", String.valueOf(search));

                String query = builder.build().getEncodedQuery();
                // Open connection for sending data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();
                int response_code = conn.getResponseCode();
                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {
                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    // Pass data to onPostExecute method
                    return(result.toString());
                }else{
                    return("unsuccessful");
                }
            }

            catch (IOException e)
            {
                Log.d("BLOOD","IOE response " + e.toString());
                // TODO Aut return 0
                //URL_RESPONSE="ERROR";
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                //converting the string to json array object
                JSONArray array = new JSONArray(result);

                //traversing through all the object
                for (int i = 0; i < array.length(); i++) {

                    //getting product object from json array
                    JSONObject product = array.getJSONObject(i);

                    //adding the product to product list
                    productList.add(new Item(
                            product.getString("name"),
                            product.getString("description"),
                            product.getString("date"),
                            product.getString("extra"),
                            product.getString("location"),
                            product.getString("image"),
                            product.getString("id"),
                            product.getString("username"),
                            product.getString("ctime")
                    ));
                }

                //creating adapter object and setting it to recyclerview
                ItemAdapter adapter = new ItemAdapter(home.this, productList);

                recyclerView.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            feedback.progressFinsih();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

            //  startActivity(new Intent(flame.this, home.class));

            //animateSuggestions(getListHeight(false), 0);
            //disableSearch();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //save last queries to disk
        //saveSearchSuggestionToDisk(searchBar.getLastSuggestions());
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        String s = enabled ? "enabled" : "disabled";
        /// Toast.makeText(flame.this, "Search " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        startSearch(text.toString(), true, null, true);
        //Toast.makeText(flame.this, text, Toast.LENGTH_SHORT).show();
        // tv.setText("Test");


    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
                // drawer.openDrawer(Gravity.LEFT);
                //startActivity(new Intent(home.this, home.class));
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                //  openVoiceRecognizer();
        }
    }


    public void loc(String locationstr, String name){
        Geocoder coder = new Geocoder(home.this);
        List<Address> address;

        try {

            address = coder.getFromLocationName(locationstr, 5);

            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            LatLng itemmarker = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.addMarker(new MarkerOptions().position(itemmarker).title(name));

        }
        catch (Exception e){ }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Geocoder coder = new Geocoder(home.this);
        List<Address> address;
    }

}