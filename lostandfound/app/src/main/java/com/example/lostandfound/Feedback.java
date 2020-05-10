package com.example.lostandfound;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ProgressBar;

public class Feedback {

    private Context context;
    ProgressDialog progressDialog;

    public Feedback(Context context){
        this.context = context;
    }


    public void progress(String txt){

        progressDialog = new ProgressDialog(context);

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        progressDialog.setMessage(txt);
        progressDialog.show();

    }

    public void progressFinsih(){

        progressDialog.dismiss();

    }

    public void AlertDialog(String title, String message){

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.no, null)
                .show();

    }


}
