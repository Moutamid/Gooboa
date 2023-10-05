package com.app.gobooa.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.gobooa.R;

//This activity is used display and hide large spinning/circular progress bar on entire screen..
public class BaseActivity extends AppCompatActivity {
    AlertDialog alertDialog;

    public void showProgressDialog() {
        AlertDialog.Builder dailogBuilder = new AlertDialog.Builder(BaseActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog, null);
        dailogBuilder.setView(dialogView);
        alertDialog = dailogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    public void hideProgressDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }
}