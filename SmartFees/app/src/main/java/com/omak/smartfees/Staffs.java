package com.omak.smartfees;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.omak.smartfees.Adapter.StaffAdapter;
import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.omak.smartfees.Model.Staff;
import com.omak.smartfees.Network.RestClient;
import com.omak.smartfees.Network.Url;
import com.omak.smartfees.Parser.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;

public class Staffs extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private FetchStaffAsync mFetchStaff;
    private ProgressDialog dialog;
    private StaffAdapter adapter;
    EditText searchEdt;
    String searchTxt = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staffs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utils.getStringSharedPreference(Staffs.this, Constants.SHARED_GYM_NAME));

        mRecyclerView = (RecyclerView)findViewById(R.id.staff_list);
        mLayoutManager = new LinearLayoutManager(Staffs.this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        searchEdt = (EditText)findViewById(R.id.search_txt);
        findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTxt = searchEdt.getText().toString();
                mFetchStaff = new FetchStaffAsync();
                mFetchStaff.execute();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(Utils.getBooleanSharedPreference(Staffs.this, Constants.SHARED_PREF_IS_OWNER)){
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Staffs.this,AddStaffActivity.class);
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
        mFetchStaff = new FetchStaffAsync();
        mFetchStaff.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mFetchStaff != null){
            mFetchStaff.cancel(true);
        }
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }

    private class FetchStaffAsync extends AsyncTask<Void,Void,ArrayList<Staff>>{

        String param;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(Staffs.this);
            dialog.setMessage("Loading...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected ArrayList<Staff> doInBackground(Void... voids) {
            param = "gymtag=searchstaff&txtname=" + searchTxt +"&txtphone=&gym_id=" + Utils.getStringSharedPreference(Staffs.this,Constants.SHARED_GYM_ID);
            ArrayList<Staff> staffs = new ArrayList<Staff>();
            try {
                String response = RestClient.httpPost(Url.STAFF_URL, param);
                return JsonParser.parseStaffList(response);
            } catch (Exception e) {
                return staffs;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Staff> staffs) {
            super.onPostExecute(staffs);
            if(dialog != null && dialog.isShowing()){
                dialog.dismiss();
            }
            if(staffs != null){
                adapter = new StaffAdapter(staffs,Staffs.this);
                mRecyclerView.setAdapter(adapter);
            }
        }
    }

}
