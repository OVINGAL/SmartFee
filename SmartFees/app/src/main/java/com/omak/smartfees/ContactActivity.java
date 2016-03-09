package com.omak.smartfees;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Logger;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Network.Url;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(ContactActivity.this, Constants.SHARED_GYM_NAME));

        WebView webView = (WebView)findViewById(R.id.web);
        webView.setWebViewClient(new WebViewClient());
        if(getIntent().getBooleanExtra("webApp",false)) {
            String name = Utils.getStringSharedPreference(ContactActivity.this, "username");
            String pass = Utils.getStringSharedPreference(ContactActivity.this, "pass");
            String url = "http://gymapp.oddsoftsolutions.com?sublogin&txtphone=" + name +"&txtpass="+pass;
            Logger.e(url);
            webView.loadUrl(url);
        } else {
            webView.loadUrl("http://oddsoftsolutions.com/#contact");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
