package com.example.lostandfound;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Viewing extends FragmentActivity implements OnMapReadyCallback {

    TextView name, user, description, dol;
    ImageView image;
    Button editbtn, deletebtn;
    String namestr, userstr, descriptionstr, locationstr, dolstr, idstr, imagestr, recordname, ctimestr;
    DatabaseReference reff;
    private GoogleMap mMap;
    HttpURLConnection conn;
    URL url = null;
    String URL_DELETE="https://lfapp.000webhostapp.com/delete.php";
    String URL_EMAIL="https://lfapp.000webhostapp.com/getemail.php";

    Feedback feedback;
    CheckConnection checkConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewing);
        Toolbar toolbar = findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);

        feedback = new Feedback(this);
        checkConnection = new CheckConnection(this);
        checkConnection.IsInternet();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        name = (TextView) findViewById(R.id.viewname);
        user = (TextView) findViewById(R.id.viewuser);
        description = (TextView) findViewById(R.id.viewdesc);
        dol = (TextView) findViewById(R.id.viewdate);
        image = (ImageView) findViewById(R.id.imageread);


            // Do something else
            namestr = getIntent().getStringExtra("name");
            descriptionstr = getIntent().getStringExtra("description");
            dolstr = getIntent().getStringExtra("date");
            locationstr = getIntent().getStringExtra("location");
            imagestr = getIntent().getStringExtra("image");
            userstr = getIntent().getStringExtra("username");
            idstr = getIntent().getStringExtra("id");
            ctimestr = getIntent().getStringExtra("ctime");




        editbtn = (Button) findViewById(R.id.edit);
        editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle b = new Bundle();
                b.putBoolean("edit", true);
                Intent intent = new Intent(getBaseContext(), Entry.class);
                intent.putExtras(b);
                intent.putExtra("name", namestr);
                intent.putExtra("description", descriptionstr);
                intent.putExtra("dol", dolstr);
                intent.putExtra("location", locationstr);
                intent.putExtra("image", userstr);
                intent.putExtra("id", idstr);
                intent.putExtra("ctime", ctimestr);
                intent.putExtra("edit", true);
                startActivity(intent);
            }
        });

        deletebtn = (Button) findViewById(R.id.delete);
        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new delete().execute();


            }
        });

        Button sendemailbtn = (Button) findViewById(R.id.b1);
        sendemailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new getemail().execute();

            }
        });

        name.setText(namestr);
        description.setText(descriptionstr);
        dol.setText("Lost on: " + dolstr);
        user.setText("By " + userstr);

        if(!imagestr.contains("null")){

            feedback.progress("Loading");

            String loc = "uploads/" + imagestr;

            try {
                StorageReference storageRef =
                        FirebaseStorage.getInstance().getReference();
                storageRef.child(loc.trim()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        Glide.with(Viewing.this)
                                .load(uri)
                                .into(image);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                         feedback.progressFinsih();

                    }
                });
            }
            catch (Exception e){}

        }
        else{

            image.setImageResource(R.drawable.lostfoundicon);
        }


        checkuser();

    }

    public class getemail extends AsyncTask<Void, Void, String> {
        @Override
        protected  void onPreExecute() {

            super.onPreExecute();

            feedback.progress("Sending email");

        }
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Void... arg0) {
            try {
                URL url = new URL(URL_EMAIL);

                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", userstr);


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
        protected void onPostExecute(String email) {
            super.onPostExecute(email);

            feedback.progressFinsih();

            sendemail(email);

        }
    }


    private void sendemail(String email) {

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
        //emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Viewing.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }


    public class delete extends AsyncTask<Void, Void, String> {
        @Override
        protected  void onPreExecute() {

            super.onPreExecute();

            feedback.progress("Deleting");

        }
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Void... arg0) {
            try {
                URL url = new URL(URL_DELETE);

                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("id", idstr);


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

            feedback.progressFinsih();
            startActivity(new Intent(Viewing.this, home.class));

        }
    }


    private void checkuser() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = preferences.getString("username", "");

        if(!userstr.equals(username)){
            editbtn.setVisibility(View.GONE);
            deletebtn.setVisibility(View.GONE);
        }

    }

    private void getrecord(String recordname) {

        name.setText(recordname);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Geocoder coder = new Geocoder(Viewing.this);
        List<Address> address;
        try {
            address = coder.getFromLocationName(locationstr,5);

            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.addMarker(new MarkerOptions().position(sydney).title(namestr));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        } catch (IOException e) {
            e.printStackTrace();

        }

    }



}
