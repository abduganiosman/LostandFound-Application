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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.net.URL;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class login extends AppCompatActivity {

    EditText inputEmail, inputPassword;
    Button btnlogin, signupbtn;
    private FirebaseAuth auth;
    String URL_REGISTER="https://lfapp.000webhostapp.com/login.php";
    HttpURLConnection conn;

    String username, password;

    Feedback feedback;
    CheckConnection checkConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkConnection = new CheckConnection(this);
        checkConnection.IsInternet();
        feedback = new Feedback(this);

        checkusersession();

        inputEmail = (EditText) findViewById(R.id.editText3);
        inputPassword = (EditText) findViewById(R.id.editText4);


        btnlogin = (Button) findViewById(R.id.button2);
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 username = inputEmail.getText().toString();
                 password = inputPassword.getText().toString();


                new loggingin().execute();

            }
        });

        signupbtn = (Button) findViewById(R.id.btnsignup);
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(login.this, Signup.class));
            }
        });

    }

    private void checkusersession() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String username = preferences.getString("username", "");

        if (username != ""){
            startActivity(new Intent(login.this, home.class));
        }

    }

    public class loggingin extends AsyncTask<Void, Void, String> {
        @Override
        protected  void onPreExecute() {

            super.onPreExecute();

            feedback.progress("Logging in");

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
                        .appendQueryParameter("username", username)
                        .appendQueryParameter("password", password);

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

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(login.this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("username", username);
            editor.apply();

            if (result != null) {
                if (result.contains("pass")) {
                    startActivity(new Intent(login.this, home.class));
                } else if (result.contains("fail")) {
                    Toast.makeText(login.this, "incorrect username or password", Toast.LENGTH_LONG).show();
                }

            }
        }
    }


}