package com.mario.upark;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mario.upark.common.Global;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//import android.support.v7.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText txt_name, txt_email, txt_phone, txt_pass;
    private CheckBox chk_term;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(this.getColor(R.color.colorBackground));
        } else {
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        mAuth = FirebaseAuth.getInstance();

        initUIView();
    }

    private void initUIView() {
        txt_email = findViewById(R.id.txt_register_email);
        txt_name = findViewById(R.id.txt_register_name);
        txt_phone = findViewById(R.id.txt_register_phone);
        txt_pass = findViewById(R.id.txt_register_pass);
        chk_term = findViewById(R.id.chk_register_term);
    }

    public void onClickSignUpBtn(View view) {
        String name = txt_name.getText().toString();
        if (name.length() == 0) {
            Toast.makeText(this, getString(R.string.register_name_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        String email = txt_email.getText().toString();
        if (email.length() == 0) {
            Toast.makeText(this, getString(R.string.register_email_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!email.contains("@") || !email.contains(".com")) {
            Toast.makeText(this, getString(R.string.register_email_format), Toast.LENGTH_SHORT).show();
            return;
        }
        String phone = txt_phone.getText().toString();
        if (phone.length() == 0) {
            Toast.makeText(this, getString(R.string.register_phone_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        String password = sha1Hash(txt_pass.getText().toString());
        if (password.length() == 0) {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (!chk_term.isChecked()) {
//            Toast.makeText(this, getString(R.string.register_term_privacy), Toast.LENGTH_SHORT).show();
//            return;
//        }

        Global.gUser.email = email;
        Global.gUser.nickname = name;
        Global.gUser.phone = phone;

        createAccoutn(email, password);
    }

    private String sha1Hash(String toString) {
        String hash = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
            byte[] bytes = toString.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = bytesToHex( bytes );
        }
        catch( NoSuchAlgorithmException e ){
            e.printStackTrace();
        }
        catch( UnsupportedEncodingException e ){
            e.printStackTrace();
        }
        return hash;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex( byte[] bytes )
    {
        char[] hexChars = new char[ bytes.length * 2 ];
        for( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[ j ] & 0xFF;
            hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
            hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
        }
        return new String( hexChars );
    }

    private void createAccoutn(String email, String password) {
        ProgressDialog dialog = Global.onShowProgressDialog(this, getResources().getString(R.string.login_server_connect), false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(RegisterActivity.this, "Success", Toast.LENGTH_SHORT).show();

                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        Global.gUser.id = currentUser.getUid();

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference mRef = database.getReference().child("Users").child(Global.gUser.id);

                        mRef.setValue(Global.gUser);

                        currentUser.sendEmailVerification()
                                .addOnCompleteListener(task1 -> {
                                    Global.onDismissProgressDialog(dialog);
                                    if (task1.isSuccessful()) {
                                        // after email is sent just logout the user and finish this activity
                                        FirebaseAuth.getInstance().signOut();
                                        Global.showOtherActivity(this, WelcomeActivity.class, 1);
                                        finish();
                                    }
                                    else
                                    {
                                        overridePendingTransition(0, 0);
                                        finish();
                                        overridePendingTransition(0, 0);
                                        startActivity(getIntent());
                                    }
                                });
                    } else {
                        // If sign in fails, display a message to the user.
                        Global.onDismissProgressDialog(dialog);
                        Toast.makeText(RegisterActivity.this, "Faid", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Global.showOtherActivity(this, WelcomeActivity.class, 1);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}
