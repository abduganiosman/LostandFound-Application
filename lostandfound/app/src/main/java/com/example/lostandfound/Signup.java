package com.example.lostandfound;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Signup extends AppCompatActivity {

    EditText name, email, password, nameet;
    Button signup;
    private FirebaseAuth mAuth;
    //ProgressDialog pdDialog;
    String URL_REGISTER="https://lfapp.000webhostapp.com/signup.php";
    String sname,scity,sphone;
    String URL_RESPONSE="";
    HttpURLConnection conn;
    URL url = null;
    String emailstr, passwordstr, usernamestr, namestr;

    Feedback feedback;
    CheckConnection checkConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkConnection = new CheckConnection(this);
        checkConnection.IsInternet();
        feedback = new Feedback(this);

        name = (EditText) findViewById(R.id.suetname);
        email = (EditText) findViewById(R.id.suetusername);
        password = (EditText) findViewById(R.id.suetpassword);
        nameet= (EditText) findViewById(R.id.namesu);


        signup = (Button) findViewById(R.id.subtnsignup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 emailstr = email.getText().toString();
                 passwordstr = password.getText().toString();
                 usernamestr = name.getText().toString();
                 namestr = nameet.getText().toString();

                 checkpassword(emailstr, passwordstr, namestr, usernamestr);
            }
        });
    }

    //checks if fields are not empty and password is not safe
    private void checkpassword(String emails, String passwords, String name, String username) {

        if(emails.isEmpty() && passwords.isEmpty() && name.isEmpty() && username.isEmpty() && passwords.length() > 7){
            Toast.makeText(Signup.this, "Fill in all fields", Toast.LENGTH_LONG);
        }
        else{
            new user_register().execute();
        }
    }

    //Sending user credentials to the database
    //Saving username and email in sharedprefernce
    public class user_register extends AsyncTask<Void, Void, String> {
        @Override
        protected  void onPreExecute() {

            super.onPreExecute();

            feedback.progress("Signing you up");

        }
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


                // Append parameters to URL
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("username", usernamestr)
                        .appendQueryParameter("password", passwordstr)
                        .appendQueryParameter("email",emailstr)
                        .appendQueryParameter("name", namestr);

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

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Signup.this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("username", name.getText().toString());
            editor.apply();

            startActivity(new Intent(Signup.this, home.class));

        }
    }


}
