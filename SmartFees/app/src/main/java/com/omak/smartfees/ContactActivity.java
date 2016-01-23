package com.omak.smartfees;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        WebView webView = (WebView)findViewById(R.id.web);
        webView.loadUrl("http://oddsoftsolutions.com/#contact");
    }
}
