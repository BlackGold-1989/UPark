package com.mario.upark;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mario.upark.common.Global;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Change Status Bar Color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setStatusBarColor(this.getColor(R.color.colorBackground));
        } else {
            this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorBackground));
        }

        initUIView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initUIView() {
        WebView webView = findViewById(R.id.web_result_code);
        WebSettings webSettings = webView.getSettings();
        CustomScriptInterface jInterface = new CustomScriptInterface(this);
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new CustomClient());
        webView.addJavascriptInterface(jInterface, "HtmlViewer");

//        String url = "http://softeck.dyndns.info/fmi/webd/Parking%20Web?script=TicketEscaneado&param=" + MainActivity.selectedValue;
        String url = "http://softeck.dyndns.info/fmi/webd/Parking%20Web?script=TicketEscaneado&param=";
        webView.loadUrl(url);

        Toast.makeText(this, jInterface.html, Toast.LENGTH_LONG).show();
    }

    private class CustomClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            view.addJavascriptInterface(new Object()
            {
                @JavascriptInterface
                public void performClick()
                {
                    Log.d("LOGIN::", "Clicked");
                    Toast.makeText(ResultActivity.this, "Login clicked", Toast.LENGTH_LONG).show();
                }
            }, "VALIDAR");
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
        }

    }

    public class CustomScriptInterface {

        private Context ctx;
        public String html;

        CustomScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void showHTML(String _html) {
            html = _html;
        }
    }

    public void onClickBackIcon(View view) {
        Global.showOtherActivity(this, MainActivity.class, 1);
    }

    @Override
    public void onBackPressed() {
        Global.showOtherActivity(this, MainActivity.class, 1);
    }

}
