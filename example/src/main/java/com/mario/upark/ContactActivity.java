package com.mario.upark;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mario.upark.common.Global;
import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ContactActivity extends AppCompatActivity {

    private static final String SENDGRID_APIKEY = "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";

    private EditText txt_email;
    private EditText txt_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(this.getColor(R.color.colorBackground));
        } else {
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        initUIView();
    }

    private void initUIView() {
        txt_email = findViewById(R.id.txt_contact_email);
        txt_content = findViewById(R.id.txv_contact_support);
    }

    public void onClickBackIcon(View view) {
        Global.showOtherActivity(this, MainActivity.class, 1);
    }

    public void onClickSendBtn(View view) {
        SendEmailASyncTask task = new SendEmailASyncTask(this,
                "lromo@publicity.com",
//                "bgold1118@gmail.com",
                txt_email.getText().toString(),
                "Equipo de soporte de Upark",
                txt_content.getText().toString(),
                null,
                null);
        task.execute();
    }

    private class SendEmailASyncTask extends AsyncTask<Void, Void, Void> {

        private Context mAppContext;
        private String mMsgResponse;

        private String mTo;
        private String mFrom;
        private String mSubject;
        private String mText;
        private Uri mUri;
        private String mAttachmentName;

        public SendEmailASyncTask(Context context, String mTo, String mFrom, String mSubject,
                                  String mText, Uri mUri, String mAttachmentName) {
            this.mAppContext = context.getApplicationContext();
            this.mTo = mTo;
            this.mFrom = mFrom;
            this.mSubject = mSubject;
            this.mText = mText;
            this.mUri = mUri;
            this.mAttachmentName = mAttachmentName;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SendGrid sendgrid = new SendGrid(SENDGRID_APIKEY);
                SendGrid.Email email = new SendGrid.Email();

                // Get values from edit text to compose email
                // TODO: Validate edit texts
                email.addTo(mTo);
                email.setFrom(mFrom);
                email.setSubject(mSubject);
                email.setText(mText);

                // Attach image
                if (mUri != null) {
                    email.addAttachment(mAttachmentName, mAppContext.getContentResolver().openInputStream(mUri));
                }

                // Send email, execute http request
                SendGrid.Response response = sendgrid.send(email);
                mMsgResponse = response.getMessage();

            } catch (SendGridException | IOException e) {
                //
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                JSONObject obj = new JSONObject(mMsgResponse);
                String message = obj.getString("message");
                if (message.equals("success")) {
                    Toast.makeText(mAppContext, "success", Toast.LENGTH_SHORT).show();
                    Global.showOtherActivity(ContactActivity.this, MainActivity.class, 1);
                } else {
                    Toast.makeText(mAppContext, mMsgResponse, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onBackPressed() {
        Global.showOtherActivity(this, MainActivity.class, 1);
    }

}
