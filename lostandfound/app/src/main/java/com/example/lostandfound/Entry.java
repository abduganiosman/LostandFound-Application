package com.example.lostandfound;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Entry extends AppCompatActivity {

    //UI elements
    EditText name, description, dol, location;
    Button btnsave;
    ImageView imageview;
    ImageView imagecam;

    //Upload related
    String URL_REGISTER="https://lfapp.000webhostapp.com/entry.php";
    String URL_EDIT="https://lfapp.000webhostapp.com/edit.php";

    HttpURLConnection conn;
    URL url = null;
    private StorageReference mStorageRef;
    private StorageTask mUploadTask;
    int recordid;


    //Image related
    Uri selectedImage;
    Bitmap bitmap;

    //edit
    Boolean editb;
    String namestr, descriptionstr, dolstr, locationstr, imagestr, userstr, rewardstr, id;

    Feedback feedback;
    CheckConnection checkConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkConnection = new CheckConnection(this);
        checkConnection.IsInternet();
        feedback = new Feedback(this);

        imagecam = (ImageView) findViewById(R.id.imagecam);

        name = (EditText) findViewById(R.id.editText5);
        description = (EditText) findViewById(R.id.editText6);
        dol = (EditText) findViewById(R.id.editText7);
        location = (EditText) findViewById(R.id.editText9);

        HideDetails();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
             boolean editboo = bundle.getBoolean("edit");
            if(editboo) {

                namestr = getIntent().getStringExtra("name");
                descriptionstr = getIntent().getStringExtra("description");
                dolstr = getIntent().getStringExtra("dol");
                locationstr = getIntent().getStringExtra("location");
                imagestr = getIntent().getStringExtra("image");
                userstr = getIntent().getStringExtra("user");
                rewardstr = getIntent().getStringExtra("reward");
                editb = getIntent().getExtras().getBoolean("edit");
                id = getIntent().getStringExtra("id");


                String loc = "uploads/" + imagestr;

                StorageReference storageRef =
                        FirebaseStorage.getInstance().getReference();

                storageRef
                        .child(loc.trim())
                        .getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                Picasso.get().load(uri).into(imagecam);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                    }
                });

                name.setText(namestr);
                description.setText(descriptionstr);
                dol.setText(dolstr);
                location.setText(locationstr);
            }
        }



        imageview = (ImageView) findViewById(R.id.imageView);
        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);//one can be replaced with any action code 

            }
        });

        btnsave = (Button) findViewById(R.id.buttonsave);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = getIntent().getExtras();
                if(bundle != null){
                    boolean editboo = bundle.getBoolean("edit");
                    if(editboo){
                        new item_edit().execute();
                    }
                }else{
                    upload();
                }
            }
        });
    }

    public void HideDetails()
    {
        feedback.AlertDialog("Security", "Please try to hide key identifying details of items, for example the background image of a smart phone, also ensure all images are clear and show the item in its entirety.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode){
            case 1:
                if(resultCode == RESULT_OK){
                    selectedImage = imageReturnedIntent.getData();

                    try {
                        imagecam.setVisibility(View.INVISIBLE);
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        imageview.setImageBitmap(bitmap);

                        runDetector(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }

    }

    private String getFileExtension(Uri uri){

        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cr.getType(uri));


    }

    public void upload() {

        feedback.progress("Uploading");

        if(selectedImage != null){

            //create storage for image, id by time

            float idimg = System.currentTimeMillis();
            String extenimg = getFileExtension(selectedImage);

            final String finalidimg = idimg + "." + extenimg;

            mStorageRef = FirebaseStorage.getInstance().getReference();

            StorageReference filereference = mStorageRef.child("uploads").child(finalidimg);

            mUploadTask = filereference.putFile(selectedImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(Entry.this, "Uploaded", Toast.LENGTH_LONG).show();

                            feedback.progressFinsih();

                            new item_entry().execute();


                            startActivity(new Intent(Entry.this, home.class));

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            feedback.AlertDialog("Error", "please try again");

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                        }
                    });

        }else{

        }
    }

    public class item_entry extends AsyncTask<Void, Void, String> {
        @Override
        protected  void onPreExecute() {

            super.onPreExecute();

            feedback.progress("Adding item");

        }
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Void... arg0) {
            try {
                URL url = new URL(URL_REGISTER);

                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                recordid =  new Random().nextInt(1000000000) + 1;

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Entry.this);
                String username = preferences.getString("username", "");

                String nameup = name.getText().toString();
                String descriptionup = description.getText().toString();
                String dolup = dol.getText().toString();
                String locationup = location.getText().toString();

                float idimg = System.currentTimeMillis();
                String extenimg = getFileExtension(selectedImage);

                final String finalidimg = idimg + "." + extenimg;

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("name", nameup)
                        .appendQueryParameter("description", descriptionup)
                        .appendQueryParameter("date", dolup)
                        .appendQueryParameter("location", locationup)
                        .appendQueryParameter("image", finalidimg)
                        .appendQueryParameter("ctime", "TBA")
                        .appendQueryParameter("username", username)
                        .appendQueryParameter("extra","a")
                        .appendQueryParameter("id", String.valueOf(recordid));


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

            startActivity(new Intent(Entry.this, home.class));

        }
    }

    public class item_edit extends AsyncTask<Void, Void, String> {
        @Override
        protected  void onPreExecute() {

            super.onPreExecute();

            feedback.progress("Updating item");

        }
        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(Void... arg0) {
            try {
                URL url = new URL(URL_EDIT);

                conn = (HttpURLConnection)url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                recordid =  new Random().nextInt(1000000000) + 1;

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Entry.this);
                String username = preferences.getString("username", "");

                String nameup = name.getText().toString();
                String descriptionup = description.getText().toString();
                String dolup = dol.getText().toString();
                String locationup = location.getText().toString();

                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("name", nameup)
                        .appendQueryParameter("description", descriptionup)
                        .appendQueryParameter("date", dolup)
                        .appendQueryParameter("location", locationup)
                        .appendQueryParameter("id", id);


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

            startActivity(new Intent(Entry.this, home.class));

        }
    }

    private void runDetector(Bitmap bitmap) {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionCloudImageLabelerOptions options =
                new FirebaseVisionCloudImageLabelerOptions.Builder()
                        .setConfidenceThreshold(0.8f)
                        .build();


        FirebaseVisionImageLabeler detector = FirebaseVision.getInstance().getCloudImageLabeler(options);

        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                        processDataResult(firebaseVisionImageLabels);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                feedback.AlertDialog("Error", "please try again");

            }
        });


    }

    private void processDataResult(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
        for (FirebaseVisionImageLabel label: firebaseVisionImageLabels)
        {
            Toast.makeText(Entry.this, label.getText(), Toast.LENGTH_LONG);
            name.setText(label.getText());
        }
    }
}