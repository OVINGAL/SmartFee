package com.omak.smartfees;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.omak.smartfees.Adapter.MemberAdapter;
import com.omak.smartfees.Adapter.StaffAdapter;
import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Customer;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private TextView noList;

//    private FetchStaffAsync mFetchStaff;
    private ProgressDialog dialog;
    private MemberAdapter adapter;
    private ArrayList<Customer> membersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(HomeActivity.this, Constants.SHARED_GYM_NAME));

        mRecyclerView = (RecyclerView)findViewById(R.id.member_list);
        noList = (TextView)findViewById(R.id.no_member);
        mLayoutManager = new LinearLayoutManager(HomeActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(HomeActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()== android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        membersList = new ArrayList<Customer>();
        membersList = Customer.getAllMemberListInGym(HomeActivity.this,Utils.getStringSharedPreference(HomeActivity.this,Constants.SHARED_GYM_ID));
        if(membersList.size() > 0) {
            noList.setVisibility(View.GONE);
            adapter = new MemberAdapter(membersList, HomeActivity.this);
            mRecyclerView.setAdapter(adapter);
        }
    }
}
