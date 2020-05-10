package com.example.lostandfound;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CheckConnection {

    String URL_FETCH = "https://lfapp.000webhostapp.com/fetch.php";
    private Context context;
    Boolean online;

    public CheckConnection(Context context){
        this.context = context;
    }

    public boolean IsInternet(){

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected == false){
            Feedback feedback = new Feedback(context);
            feedback.AlertDialog("No Internet Connection",
                    "Don't Worry, you can still use the app using our cache");
        }

        return isConnected;

    }

}
