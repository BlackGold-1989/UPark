package com.mario.upark;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mario.upark.common.Global;
import com.mario.upark.common.UserModel;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private EditText txt_email, txt_pass;
    private CheckBox chk_remember, chk_term;

    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(this.getColor(R.color.colorBackground));
        } else {
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initUIView();
    }

    private void initUIView() {
        txt_email = findViewById(R.id.txt_login_email);
        txt_pass = findViewById(R.id.txt_login_pass);

        chk_remember = findViewById(R.id.chk_login_remember);
        chk_term = findViewById(R.id.chk_login_term);

        SharedPreferences prefs = getSharedPreferences("com.mario.upark", MODE_PRIVATE);
        String email = prefs.getString("email", "");
        String pass = prefs.getString("password", "");

        txt_email.setText(email);
        txt_pass.setText(pass);

        assert email != null;
        if (!email.equals("")) {
            chk_remember.setChecked(true);
        }
    }

    public void onClickLoginBtn(View view) {
        String email = txt_email.getText().toString();
        if (email.length() == 0) {
            Toast.makeText(this, getString(R.string.register_email_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!email.contains("@") || !email.contains(".com")) {
            Toast.makeText(this, getString(R.string.register_email_format), Toast.LENGTH_SHORT).show();
            return;
        }
        String pass = txt_pass.getText().toString();
        String password = sha1Hash(pass);
        if (password.length() == 0) {
            Toast.makeText(this, getString(R.string.wrong_pass), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!chk_term.isChecked()) {
            Toast.makeText(this, getString(R.string.wrong_term), Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = Global.onShowProgressDialog(this, getResources().getString(R.string.login_server_connect), false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();

                        assert user != null;
                        if (user.isEmailVerified()) {
                            Global.gUser = new UserModel();
                            Global.gUser.id = user.getUid();

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference mRef = database.getReference().child("Users").child(Global.gUser.id);
                            mRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                    // This method is called once with the initial value and again
                                    // whenever data at this location is updated.
                                    Global.onDismissProgressDialog(dialog);

                                    SharedPreferences.Editor editor = getSharedPreferences("com.mario.upark", MODE_PRIVATE).edit();
                                    if (chk_remember.isChecked()) {
                                        editor.putString("email", email);
                                        editor.putString("password", pass);
                                        editor.apply();
                                    } else {
                                        editor.clear();
                                        editor.apply();
                                    }

                                    Global.gUser = dataSnapshot.getValue(UserModel.class);
                                    Global.showOtherActivity(LoginActivity.this, MainActivity.class, -1);
                                }

                                @Override
                                public void onCancelled(@NotNull DatabaseError error) {
                                    // Failed to read value
                                    Global.onDismissProgressDialog(dialog);
                                }
                            });
                        } else {
                            Global.onDismissProgressDialog(dialog);
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.common_check_email), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Global.onDismissProgressDialog(dialog);
                    }
                });
    }

    private String sha1Hash(String toString) {
        String hash = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
            byte[] bytes = toString.getBytes(StandardCharsets.UTF_8);
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = bytesToHex( bytes );
        }
        catch( NoSuchAlgorithmException e ){
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

    public void onClickGoogleBtn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }

    @Override
    public void onBackPressed() {
        Global.showOtherActivity(this, WelcomeActivity.class, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 100) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            assert account != null;
            Global.gUser.nickname = account.getDisplayName();
            Global.gUser.email = account.getEmail();
            Global.gUser.phone = "";
            Global.gUser.id = account.getId();

            Global.showOtherActivity(LoginActivity.this, MainActivity.class, -1);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onShowAgreementDialog() {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dialog_agreement);
        dialog.setTitle(null);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnAccept = dialog.findViewById(R.id.dialog_term_accept);
        btnAccept.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    public void onClickTermLbl(View view) {
        onShowAgreementDialog();
    }

}
