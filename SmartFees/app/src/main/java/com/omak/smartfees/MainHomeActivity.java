package com.omak.smartfees;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.omak.smartfees.BackgroundService.MemberUpdateService;
import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;

public class MainHomeActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        ((TextView)findViewById(R.id.name_com)).setText(Utils.getStringSharedPreference(this, Constants.SHARED_GYM_NAME).toUpperCase());
        findViewById(R.id.member_btn).setOnClickListener(this);
        findViewById(R.id.staff_btn).setOnClickListener(this);
        findViewById(R.id.profile_btn).setOnClickListener(this);
        findViewById(R.id.share_btn).setOnClickListener(this);
        findViewById(R.id.about_btn).setOnClickListener(this);
        if(Utils.checkNetwork(MainHomeActivity.this)){
            MemberUpdateService.startupdateAction(MainHomeActivity.this,Utils.getStringSharedPreference(MainHomeActivity.this,Constants.SHARED_GYM_ID));
        }
    }


    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.member_btn:
                intent = new Intent(MainHomeActivity.this,HomeActivity.class);
                startActivity(intent);
                break;
            case R.id.staff_btn:
                intent = new Intent(MainHomeActivity.this,Staffs.class);
                startActivity(intent);
                break;
            case R.id.profile_btn:
                intent = new Intent(MainHomeActivity.this,ProfileActivity.class);
                startActivity(intent);
                break;
            case R.id.share_btn:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out Smart Fee App on Google Play! https://play.google.com/store/apps/details?id=com.omak.smartfees");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.about_btn:
                intent = new Intent(MainHomeActivity.this,ContactActivity.class);
                startActivity(intent);
                break;
        }
    }
}
