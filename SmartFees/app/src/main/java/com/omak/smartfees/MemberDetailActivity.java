package com.omak.smartfees;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;

public class MemberDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(MemberDetailActivity.this, Constants.SHARED_GYM_NAME));

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
