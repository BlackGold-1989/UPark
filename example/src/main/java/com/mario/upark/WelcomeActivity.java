package com.mario.upark;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mario.upark.common.Global;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(this.getColor(R.color.colorBackground));
        } else {
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

    }

    public void onClickSignUpBtn(View view) {
        Global.showOtherActivity(this, RegisterActivity.class, 0);
    }

    public void onClickLoginBtn(View view) {
        Global.showOtherActivity(this, LoginActivity.class, 0);
    }

    @Override
    public void onBackPressed() {
        // alertdialog for exit the app
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Salida");
        alertDialogBuilder
                .setMessage("¿De verdad quieres salir de Upark?")
                .setCancelable(false)
                .setPositiveButton("Si", (dialog, id) -> {
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                })

                .setNegativeButton("No", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
