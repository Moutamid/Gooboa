package com.app.gobooa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

import com.app.gobooa.R;

//This java/activity file is used to display splash screen
public class SplashActivity extends AppCompatActivity {
    public static final int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        //This code sleep/pause screen for 3 seconds then move to next screen
        Thread obj = new Thread() {
            public void run() {
                try{
                    sleep(SPLASH_TIME_OUT);
                }catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    Intent i = new Intent(SplashActivity.this, SignInActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                    finish();
                }
            }
        };obj.start();

    }
}