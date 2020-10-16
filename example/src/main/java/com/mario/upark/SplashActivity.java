package com.mario.upark;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(this.getColor(R.color.colorBackground));
        } else {
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 1500ms
                onShowMainActivity();
            }
        }, 1500);
    }

    private void onShowMainActivity() {
        startActivity(new Intent(this, WelcomeActivity.class));
    }
}
