package com.mario.upark;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.mario.upark.common.Global;
import com.mario.upark.common.History;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class MainActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    public static final String SERVER_URL = "https://rule.daxiao-itdev.com/";
    private static final String BASE_URL = SERVER_URL +  "Backend/";

    private static final String CONFIG_CLIENT_SANDBOXID = "Ae3heUmxHwa30Ga2xHQwiTa5CzQIGh7D8ygS60HcaiFmilaq8wJ_EwThm3JAYBTChJYt8x3JvzXrdWzH";
    private static final String CONFIG_CLIENT_LIVEID = "Acng7EqArJcSQitpfgn1M9Clj9o0Z81tCc1tlgT-jenKAB1fpJbu0RhLn1z-aUTduVsNomK8b2olEHzM";

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    private static final int REQUEST_CODE_PAYMENT = 10001;
    private static final int REQUEST_CODE_FEATURE_PAYMENT = 10002;

    private String payCode = "";

    private static PayPalConfiguration configuration = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_LIVEID)
            .merchantName("My Product")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacey"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));
    private PayPalPayment payment;

    private ZBarScannerView mScannerView;

    boolean isShowMenu = false;
    private LinearLayout llt_menu;

    private boolean isScaning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(this.getColor(R.color.colorBackground));
        } else {
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            ViewGroup contentFrame = findViewById(R.id.content_frame);
            mScannerView = new ZBarScannerView(this);
            mScannerView.setAutoFocus(true);
            contentFrame.addView(mScannerView);

            initUIView();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        }

        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, configuration);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ViewGroup contentFrame = findViewById(R.id.content_frame);
                mScannerView = new ZBarScannerView(this);
                mScannerView.setAutoFocus(true);
                contentFrame.addView(mScannerView);

                initUIView();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_LONG).show();

                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        }
    }

    private void initUIView() {
        llt_menu = findViewById(R.id.llt_main_menu);
        llt_menu.setVisibility(View.GONE);
        YoYo.with(Techniques.FadeOut)
                .duration(0)
                .repeat(0)
                .playOn(findViewById(R.id.llt_main_menu));
    }

    private void onEventGetResult(String value) {
        History historyItem = new History();
        historyItem.id = Global.gUser.id;
        historyItem.code = value;

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
        historyItem.regdate = String.format("%d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, min, sec);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mRef = database.getReference().child("Histories").child(historyItem.id).child(historyItem.regdate);

        mRef.setValue(historyItem);
    }

    private void onShowAPIEvent(String value) {
        if (isScaning) {
            return;
        }
        isScaning = true;

        Map<String, String> params = new HashMap<>();
        params.put("time", value);

        ProgressDialog dialog = ProgressDialog.show(this
                , getString(R.string.progress_title)
                , getString(R.string.progress_detail));

        OkHttpUtils.get().url(BASE_URL + "get_mexico_time")
                .params(params)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(okhttp3.Call call, Exception e, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        isScaning = false;
                        onResume();
                        Toast.makeText(MainActivity.this, getString(R.string.alert_error_internet_detail), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject result = obj.getJSONObject("result");

                            String title = result.getString("title");
                            String pay = result.getString("pay");
                            String unit = result.getString("unit");

                            if (Global.isTestMode) {
                                pay = "5";
                            }
                            payCode = value;

                            getPaymentUpark(title, pay, unit);
                        } catch (JSONException e) {
                            onResume();
                            Toast.makeText(MainActivity.this, getString(R.string.alert_error_server_detail), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        isScaning = false;
                    }
                });
    }

    private void getPaymentUpark(String title, String pay, String unit) {
        payment = new PayPalPayment(new BigDecimal(pay), unit,
                title, PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, REQUEST_CODE_PAYMENT);
    }

    public void onClickDetectBtn(View view) {
        //
    }

    public void onClickMenuBtn(View view) {
        if (isShowMenu) {
            return;
        }

        final Handler handler = new Handler();
        handler.postDelayed(() -> isShowMenu = true, 500);
        llt_menu.setVisibility(View.VISIBLE);

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .repeat(0)
                .playOn(findViewById(R.id.llt_main_menu));

        YoYo.with(Techniques.FadeOut)
                .duration(500)
                .repeat(0)
                .playOn(findViewById(R.id.llt_main_menubtn));

        YoYo.with(Techniques.FadeOut)
                .duration(500)
                .repeat(0)
                .playOn(findViewById(R.id.llt_main_logo));

        YoYo.with(Techniques.FadeOut)
                .duration(500)
                .repeat(0)
                .playOn(findViewById(R.id.llt_main_detect));
    }


    public void onClickContactBtn(View view) {
        Global.showOtherActivity(this, ContactActivity.class, 0);
    }

    public void onClickMapBtn(View view) {
        Global.showOtherActivity(this, MapActivity.class, 0);
    }

    public void onClickRecordBtn(View view) {
        Global.showOtherActivity(this, RecordActivity.class, 0);
    }

    public void onClickCaptureBtn(View view) {
        Global.showOtherActivity(this, ManualActivity.class, 0);
    }

    public void onClickMainView(View view) {
        if (!isShowMenu) {
            return;
        }
        llt_menu.setVisibility(View.GONE);

        final Handler handler = new Handler();
        handler.postDelayed(() -> { isShowMenu = false; }, 500);
        YoYo.with(Techniques.FadeOut)
                .duration(500)
                .repeat(0)
                .playOn(findViewById(R.id.llt_main_menu));

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .repeat(0)
                .playOn(findViewById(R.id.llt_main_menubtn));

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .repeat(0)
                .playOn(findViewById(R.id.llt_main_logo));
        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .repeat(0)
                .playOn(findViewById(R.id.llt_main_detect));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mScannerView == null) {
            return;
        }
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mScannerView == null) {
            return;
        }
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        String value = rawResult.getContents();

        ProgressDialog dialog = Global.onShowProgressDialog(this, getResources().getString(R.string.login_server_connect), false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mRef = database.getReference().child("Histories");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Global.onDismissProgressDialog(dialog);
                boolean isContain = false;
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    for (DataSnapshot historySnapshot: postSnapshot.getChildren()) {
                        History history = historySnapshot.getValue(History.class);
                        if (history.code.equals(value)) {
                            isContain = true;
                            break;
                        }
                    }
                    if (isContain) {
                        break;
                    }
                }
                if (!isContain) {
                    onShowAPIEvent(value);
                } else {
                    onResume();
                    Toast.makeText(MainActivity.this, "Este es un código de barras usado antes.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Global.onDismissProgressDialog(dialog);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // alertdialog for exit the app
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Salida");
        alertDialogBuilder
                .setMessage("¿De verdad quieres salir de Upark?")
                .setCancelable(false)
                .setPositiveButton("Si", (dialog, id) -> {
                    if (mScannerView == null) {
                        return;
                    }
                    mScannerView.stopCamera();

                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                })

                .setNegativeButton("No", (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        JSONObject object = confirm.toJSONObject();
                        String response = object.getString("response_type");
                        if (response.equals("payment")) {
                            onEventGetResult(payCode);
                        }
                        System.out.println(confirm.toJSONObject().toString(4));
                        System.out.println(confirm.getPayment().toJSONObject().toString(4));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {

            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {

            }
        }
    }

}